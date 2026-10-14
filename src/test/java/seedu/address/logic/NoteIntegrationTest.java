package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
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
    public void execute_noteReplacement_persistsAcrossRepeatAndReload() throws Exception {
        String first = "Strong on system design, weak on SQL";
        String replacement = "Passed round 2,  schedule final interview [C++]; \u4e2d\u6587";
        assertEquals("Updated note for " + BENSON.getName() + ": " + first,
                logic.execute("note 2 no/" + first).getFeedbackToUser());
        assertEquals(new Note(first), readCandidate(1).getNote().orElseThrow());

        assertEquals("Updated note for " + BENSON.getName() + ": " + replacement,
                logic.execute("note 2 no/  " + replacement + "  ").getFeedbackToUser());
        assertEquals(new Note(replacement), readCandidate(1).getNote().orElseThrow());
        assertEquals("Updated note for " + BENSON.getName() + ": " + replacement,
                logic.execute("note 2 no/" + replacement).getFeedbackToUser());
        Model reloaded = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(),
                new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertTrue(readCandidate(0).getNote().isEmpty());
    }

    @Test
    public void execute_noteInExpandedView_savesSelectedCandidateAndKeepsExpandedView() throws Exception {
        logic.execute("expand 2");
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertTrue(logic.isExpandedViewProperty().get());

        logic.execute("note 1 no/Follow up next week");

        assertEquals(new Note("Follow up next week"), readCandidate(1).getNote().orElseThrow());
        assertTrue(readCandidate(0).getNote().isEmpty());
        assertEquals(1, model.getFilteredPersonList().size());
        assertTrue(logic.isExpandedViewProperty().get());
        logic.execute("expand 1");
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

        assertThrows(ParseException.class, Note.MESSAGE_TOO_LONG, () -> logic.execute("note 1 no/" + "x".repeat(501)));
        assertThrows(CommandException.class, NoteCommand.MESSAGE_INVALID_CANDIDATE_INDEX, () ->
                logic.execute("note 2 no/New note"));

        assertEquals(before, model.getAddressBook());
        assertEquals(displayedBefore, model.getFilteredPersonList());
        assertTrue(logic.isExpandedViewProperty().get());
        assertEquals(fileBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void execute_filteredIndex_savesDisplayedCandidateAndKeepsFilter() throws Exception {
        logic.execute("status 2 s/Interviewing");
        Person original = model.getAddressBook().getPersonList().get(1);
        logic.execute("find Benson");
        assertEquals(List.of(original), model.getFilteredPersonList());

        logic.execute("note 1 no/Follow up next week");

        assertEquals(1, model.getFilteredPersonList().size());
        Person expected = new PersonBuilder(original).withNote("Follow up next week").build();
        assertEquals(expected, readCandidate(1));
        assertEquals(expected, model.getAddressBook().getPersonList().get(1));
        assertTrue(readCandidate(0).getNote().isEmpty());
    }

    @Test
    public void execute_contactAndStatusEditsAfterNote_keepsSavedNote() throws Exception {
        Person original = model.getAddressBook().getPersonList().getFirst();
        logic.execute("note 1 no/Interview feedback");
        logic.execute("edit 1 p/12345678");
        assertEquals(new PersonBuilder(original).withNote("Interview feedback").withPhone("12345678").build(),
                readCandidate(0));
        logic.execute("status 1 s/Offered");

        Person saved = readCandidate(0);
        assertEquals(Status.OFFERED, saved.getStatus());
        assertEquals(new Note("Interview feedback"), saved.getNote().orElseThrow());
        assertEquals("12345678", saved.getPhone().value);
    }

    @Test
    public void execute_invalidNotes_keepsMemoryDiskAndFilterUnchanged() throws Exception {
        logic.execute("note 2 no/Existing note");
        logic.execute("find Benson");
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        String fileBefore = Files.readString(addressBookStorage.getAddressBookFilePath());
        String[] commands = {"note 1", "note abc no/hi",
            "note 1 no/" + "x".repeat(501), "note 1 no/first no/second"};
        String[] messages = {NoteCommandParser.MESSAGE_MISSING_PREFIX + "\n" + NoteCommand.MESSAGE_USAGE,
            ParserUtil.MESSAGE_INVALID_INDEX + "\n" + NoteCommand.MESSAGE_USAGE,
            Note.MESSAGE_TOO_LONG,
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
    public void execute_storageIoFailure_throwsCommandException() {
        assertSaveFailure(new IOException("Test write failure"),
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "Test write failure"));
    }

    @Test
    public void execute_storagePermissionFailure_throwsCommandException() {
        assertSaveFailure(new AccessDeniedException("Test permission failure"),
                String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, "Test permission failure"));
    }

    /**
     * Asserts that a note command reports the given storage error when saving fails.
     */
    private void assertSaveFailure(IOException error, String message) {
        Path filePath = addressBookStorage.getAddressBookFilePath();
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(filePath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw error;
            }
        };
        Logic failingLogic = createLogic(failingStorage);

        assertThrows(CommandException.class, message, () -> failingLogic.execute("note 1 no/Replacement"));
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
