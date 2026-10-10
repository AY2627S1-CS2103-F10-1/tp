package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.NoteCommandParser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class NoteIntegrationTest {

    @TempDir
    Path temporaryFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private JsonAddressBookStorage addressBookStorage;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("candidates.json"));
        logic = createLogic(addressBookStorage);
    }

    @Test
    public void execute_noteTwice_replacesNoteOnDiskAndSurvivesReload() throws Exception {
        String first = "Strong on system design, weak on SQL";
        String replacement = "Passed round 2,  schedule final interview [C++]; \u4e2d\u6587";
        assertEquals("Updated note for " + BENSON.getName() + ": " + first,
                logic.execute("note 2 no/" + first).getFeedbackToUser());
        assertEquals(new Note(first), readCandidate(1).getNote().orElseThrow());

        assertEquals("Updated note for " + BENSON.getName() + ": " + replacement,
                logic.execute("note 2 no/  " + replacement + "  ").getFeedbackToUser());
        assertEquals(new Note(replacement), readCandidate(1).getNote().orElseThrow());
        Model reloaded = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(),
                new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertTrue(readCandidate(0).getNote().isEmpty());
    }

    @Test
    public void execute_identicalNote_savesAndReturnsNormalSuccess() throws Exception {
        String command = "note 1 no/Passed round 2";
        logic.execute(command);
        assertEquals("Updated note for " + ALICE.getName() + ": Passed round 2",
                logic.execute(command).getFeedbackToUser());
        assertEquals(new Note("Passed round 2"), readCandidate(0).getNote().orElseThrow());
    }

    @Test
    public void execute_unicodeNoteLengthBoundary_preservesSavedNote() throws Exception {
        String note = "\uD83D\uDE00".repeat(Note.MAX_LENGTH);
        logic.execute("note 2 no/  " + note + "  ");

        assertEquals(new Note(note), readCandidate(1).getNote().orElseThrow());
        String fileBefore = Files.readString(addressBookStorage.getAddressBookFilePath());
        assertThrows(ParseException.class, Note.MESSAGE_TOO_LONG, () ->
                logic.execute("note 2 no/" + note + "\uD83D\uDE00"));

        assertEquals(new Note(note), model.getAddressBook().getPersonList().get(1).getNote().orElseThrow());
        assertEquals(fileBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void execute_noteInExpandedView_savesSelectedCandidateAndCollapsesList() throws Exception {
        logic.execute("expand 2");
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertTrue(logic.isExpandedViewProperty().get());

        logic.execute("note 1 no/Follow up next week");

        assertEquals(new Note("Follow up next week"), readCandidate(1).getNote().orElseThrow());
        assertTrue(readCandidate(0).getNote().isEmpty());
        assertEquals(getTypicalAddressBook().getPersonList().size(), model.getFilteredPersonList().size());
        assertFalse(logic.isExpandedViewProperty().get());
        logic.execute("expand 2");
        assertTrue(logic.isExpandedViewProperty().get());
        assertEquals(new Note("Follow up next week"),
                model.getFilteredPersonList().getFirst().getNote().orElseThrow());
    }

    @Test
    public void execute_invalidNoteInExpandedView_keepsDataAndViewUnchanged() throws Exception {
        logic.execute("note 2 no/Existing note");
        logic.execute("expand 2");
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        String fileBefore = Files.readString(addressBookStorage.getAddressBookFilePath());

        assertThrows(ParseException.class, Note.MESSAGE_BLANK, () -> logic.execute("note 1 no/ "));
        assertThrows(CommandException.class, NoteCommand.MESSAGE_INVALID_CANDIDATE_INDEX, () ->
                logic.execute("note 2 no/New note"));

        assertEquals(before, model.getAddressBook());
        assertEquals(displayedBefore, model.getFilteredPersonList());
        assertTrue(logic.isExpandedViewProperty().get());
        assertEquals(fileBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void execute_filteredIndex_savesDisplayedCandidateAndShowsAllCandidates() throws Exception {
        logic.execute("find Benson");
        assertEquals(List.of(BENSON), model.getFilteredPersonList());

        logic.execute("note 1 no/Follow up next week");

        assertEquals(getTypicalAddressBook().getPersonList().size(), model.getFilteredPersonList().size());
        assertEquals(new Note("Follow up next week"), readCandidate(1).getNote().orElseThrow());
        assertTrue(readCandidate(0).getNote().isEmpty());
    }

    @Test
    public void execute_editAfterNote_keepsSavedNote() throws Exception {
        logic.execute("note 1 no/Interview feedback");
        logic.execute("edit 1 p/12345678");

        assertEquals(new Note("Interview feedback"), readCandidate(0).getNote().orElseThrow());
        assertEquals("12345678", readCandidate(0).getPhone().value);
    }

    @Test
    public void execute_noteAfterStatus_keepsSavedStatus() throws Exception {
        logic.execute("status 2 s/Interviewing");
        logic.execute("find Benson");

        logic.execute("NoTe 1 NO/Follow up next week");

        Person savedCandidate = readCandidate(1);
        assertEquals(Status.INTERVIEWING, savedCandidate.getStatus());
        assertEquals(new Note("Follow up next week"), savedCandidate.getNote().orElseThrow());
        assertEquals(savedCandidate, model.getAddressBook().getPersonList().get(1));
        assertTrue(readCandidate(0).getNote().isEmpty());
    }

    @Test
    public void execute_statusAfterNote_keepsSavedNote() throws Exception {
        logic.execute("note 2 no/Interview feedback");

        logic.execute("status 2 s/Offered");

        Person savedCandidate = readCandidate(1);
        assertEquals(Status.OFFERED, savedCandidate.getStatus());
        assertEquals(new Note("Interview feedback"), savedCandidate.getNote().orElseThrow());
        assertEquals(savedCandidate, model.getAddressBook().getPersonList().get(1));
    }

    @Test
    public void execute_invalidNotes_keepsMemoryDiskAndFilterUnchanged() throws Exception {
        logic.execute("note 2 no/Existing note");
        logic.execute("find Benson");
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        String fileBefore = Files.readString(addressBookStorage.getAddressBookFilePath());
        String[] commands = {"note 1", "note abc no/hi", "note 1 no/ ",
            "note 1 no/" + "x".repeat(501), "note 1 no/first no/second"};
        String[] messages = {NoteCommandParser.MESSAGE_MISSING_PREFIX + "\n" + NoteCommand.MESSAGE_USAGE,
            ParserUtil.MESSAGE_INVALID_INDEX + "\n" + NoteCommand.MESSAGE_USAGE,
            Note.MESSAGE_BLANK, Note.MESSAGE_TOO_LONG,
            Messages.MESSAGE_DUPLICATE_FIELDS + "no/"};
        for (int i = 0; i < commands.length; i++) {
            String command = commands[i];
            assertThrows(ParseException.class, messages[i], () -> logic.execute(command));
        }
        for (String command : new String[] {"note 2 no/New note", "note 2147483647 no/New note"}) {
            assertThrows(CommandException.class, NoteCommand.MESSAGE_INVALID_CANDIDATE_INDEX, () ->
                    logic.execute(command));
        }

        assertEquals(before, model.getAddressBook());
        assertEquals(displayedBefore, model.getFilteredPersonList());
        assertEquals(fileBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void execute_storageIoFailure_keepsExistingNoteAndFilter() throws Exception {
        assertFailedSaveKeepsState(new IOException("Test write failure"),
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "Test write failure"), false);
    }

    @Test
    public void execute_storagePermissionFailure_keepsExistingNoteAndFilter() throws Exception {
        assertFailedSaveKeepsState(new AccessDeniedException("Test permission failure"),
                String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, "Test permission failure"),
                false);
    }

    @Test
    public void execute_storageIoFailureInExpandedView_keepsDataAndViewUnchanged() throws Exception {
        assertFailedSaveKeepsState(new IOException("Test write failure"),
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "Test write failure"), true);
    }

    @Test
    public void execute_storagePermissionFailureInExpandedView_keepsDataAndViewUnchanged() throws Exception {
        assertFailedSaveKeepsState(new AccessDeniedException("Test permission failure"),
                String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, "Test permission failure"),
                true);
    }

    /**
     * Asserts that a failed note save preserves live data, saved data, and the original filter predicate.
     */
    private void assertFailedSaveKeepsState(IOException error, String message, boolean isExpanded)
            throws Exception {
        Person notedBenson = new PersonBuilder(BENSON).withNote("Existing note").build();
        model.setPerson(BENSON, notedBenson);
        logic.execute(isExpanded ? "expand 2" : "find Benson");
        AddressBook before = new AddressBook(model.getAddressBook());
        String fileBefore = Files.readString(addressBookStorage.getAddressBookFilePath());
        Path filePath = addressBookStorage.getAddressBookFilePath();
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(filePath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw error;
            }
        };
        Logic failingLogic = createLogic(failingStorage);

        assertThrows(CommandException.class, message, () -> failingLogic.execute("note 1 no/Replacement"));

        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(notedBenson), model.getFilteredPersonList());
        assertEquals(isExpanded, logic.isExpandedViewProperty().get());
        assertEquals(fileBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
        if (!isExpanded) {
            model.setPerson(notedBenson, new PersonBuilder(notedBenson).withPhone("12345678").build());
            assertEquals(1, model.getFilteredPersonList().size());
        }
    }

    /**
     * Creates logic using the live model, supplied candidate storage, and temporary preferences storage.
     */
    private Logic createLogic(JsonAddressBookStorage storage) {
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(
                temporaryFolder.resolve("preferences.json"));
        return new LogicManager(model, new StorageManager(storage, prefsStorage));
    }

    /**
     * Reads the candidate at the given zero-based index from saved data.
     */
    private Person readCandidate(int index) throws Exception {
        return addressBookStorage.readAddressBook().orElseThrow().getPersonList().get(index);
    }
}
