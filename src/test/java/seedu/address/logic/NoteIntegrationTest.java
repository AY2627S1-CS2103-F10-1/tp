package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestCaseUtil.assertAllCases;
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
import seedu.address.logic.parser.AddCommandParser;
import seedu.address.logic.parser.NoteCommandParser;
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
import seedu.address.testutil.LogicTestFixture;
import seedu.address.testutil.PersonBuilder;

public class NoteIntegrationTest {

    @TempDir
    Path temporaryFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private JsonAddressBookStorage addressBookStorage;
    private Logic logic;
    private LogicTestFixture fixture;

    @BeforeEach
    public void setUp() {
        fixture = new LogicTestFixture(temporaryFolder, model);
        addressBookStorage = fixture.getCandidateStorage();
        logic = fixture.getLogic();
    }

    @Test
    public void execute_noteReplacement_persistsAcrossRepeatAndReload() throws Exception {
        String first = "Strong on system design, weak on SQL";
        String replacement = "Passed round 2,  schedule final interview [C++]; \"follow up\"";
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
    public void execute_overlongNoteInExpandedView_keepsDataAndViewUnchanged() throws Exception {
        logic.execute("note 2 no/Existing note");
        logic.execute("expand 2");
        assertTrue(logic.isExpandedViewProperty().get());

        assertRejectedCommandPreservesState("note 1 no/" + "x".repeat(501), ParseException.class,
                Note.MESSAGE_TOO_LONG);
    }

    @Test
    public void execute_outOfRangeNoteInExpandedView_keepsDataAndViewUnchanged() throws Exception {
        logic.execute("note 2 no/Existing note");
        logic.execute("expand 2");
        assertTrue(logic.isExpandedViewProperty().get());

        assertRejectedCommandPreservesState("note 2 no/New note", CommandException.class,
                NoteCommand.MESSAGE_INVALID_CANDIDATE_INDEX);
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
    public void execute_missingNotePrefix_keepsMemoryDiskAndFilterUnchanged() throws Exception {
        assertInvalidFilteredNoteRejected("note 1", ParseException.class,
                NoteCommandParser.MESSAGE_MISSING_PREFIX + "\n" + NoteCommand.MESSAGE_USAGE);
    }

    @Test
    public void execute_nonNumericNoteIndex_keepsMemoryDiskAndFilterUnchanged() throws Exception {
        assertInvalidFilteredNoteRejected("note abc no/hi", ParseException.class,
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
    }

    @Test
    public void execute_overlongNote_keepsMemoryDiskAndFilterUnchanged() throws Exception {
        assertInvalidFilteredNoteRejected("note 1 no/" + "x".repeat(501), ParseException.class,
                Note.MESSAGE_TOO_LONG);
    }

    @Test
    public void execute_duplicateNotePrefix_keepsMemoryDiskAndFilterUnchanged() throws Exception {
        assertInvalidFilteredNoteRejected("note 1 no/first no/second", ParseException.class,
                Messages.MESSAGE_DUPLICATE_FIELDS + "no/");
    }

    @Test
    public void execute_noteIndexOutsideFilteredList_keepsMemoryDiskAndFilterUnchanged() throws Exception {
        assertInvalidFilteredNoteRejected("note 2 no/New note", CommandException.class,
                NoteCommand.MESSAGE_INVALID_CANDIDATE_INDEX);
    }

    @Test
    public void execute_maximumIntegerNoteIndex_keepsMemoryDiskAndFilterUnchanged() throws Exception {
        assertInvalidFilteredNoteRejected("note 2147483647 no/New note", CommandException.class,
                NoteCommand.MESSAGE_INVALID_CANDIDATE_INDEX);
    }

    @Test
    public void execute_unsupportedInput_keepsMemoryDiskAndFilterUnchanged() throws Exception {
        logic.execute("note 2 no/Keep this note");
        logic.execute("find Benson");
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        String fileBefore = Files.readString(addressBookStorage.getAddressBookFilePath());
        for (String command : List.of("add n/Alice p/123 e/alice@example.com a/Block\u00A01",
                "edit 1 a/Block\u200B1", "note 1 no/\u200B", "note 1 no/\t", "find Ben\u00A0son")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(before, model.getAddressBook());
            assertEquals(displayedBefore, model.getFilteredPersonList());
            assertEquals(fileBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
        }
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

    @Test
    public void execute_clearFilteredCandidate_removesOnlySelectedNoteAndPersistsAcrossReload() throws Exception {
        prepareCandidatesWithNotes();
        logic.execute("find Benson");
        String address = BENSON.getAddress().value;

        assertEquals(String.format(NoteCommand.MESSAGE_CLEAR_SUCCESS, BENSON.getName()),
                logic.execute("note 1 no/").getFeedbackToUser());

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(BENSON.getEmail(), model.getFilteredPersonList().getFirst().getEmail());
        assertFalse(model.getFilteredPersonList().getFirst().hasNote());
        ReadOnlyAddressBook reloaded = addressBookStorage.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), reloaded);
        assertTrue(reloaded.getPersonList().getFirst().hasNote());
        assertFalse(reloaded.getPersonList().get(1).hasNote());
        assertEquals(Status.INTERVIEWING, reloaded.getPersonList().get(1).getStatus());
        assertEquals(address, reloaded.getPersonList().get(1).getAddress().value);
        logic.execute("list");
        logic.execute("note 2 no/   ");
        assertEquals(reloaded, addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_clearExpandedCandidate_removesSelectedNote() throws Exception {
        prepareCandidatesWithNotes();
        logic.execute("expand 2");

        logic.execute("note 1 no/");

        assertTrue(logic.isExpandedViewProperty().get());
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(BENSON.getEmail(), model.getFilteredPersonList().getFirst().getEmail());
        assertFalse(model.getFilteredPersonList().getFirst().hasNote());
        assertFalse(addressBookStorage.readAddressBook().orElseThrow().getPersonList().get(1).hasNote());
        assertTrue(addressBookStorage.readAddressBook().orElseThrow().getPersonList().getFirst().hasNote());
    }

    @Test
    public void execute_clearOutsideFilteredList_preservesSavedData() throws Exception {
        prepareCandidatesWithNotes();
        logic.execute("find Benson");
        String before = Files.readString(addressBookStorage.getAddressBookFilePath());

        assertThrows(CommandException.class, NoteCommand.MESSAGE_INVALID_CANDIDATE_INDEX, () ->
                logic.execute("note 2 no/"));

        assertEquals(before, Files.readString(addressBookStorage.getAddressBookFilePath()));
        assertTrue(model.getFilteredPersonList().getFirst().hasNote());
    }

    @Test
    public void execute_clearSaveFailure_preservesPreviouslySavedNoteAndReportsError() throws Exception {
        prepareCandidatesWithNotes();
        logic.execute("list");
        String before = Files.readString(addressBookStorage.getAddressBookFilePath());
        JsonAddressBookStorage failing = new JsonAddressBookStorage(addressBookStorage.getAddressBookFilePath()) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook candidates) throws IOException {
                throw new IOException("Test save failure");
            }
        };
        Logic failingLogic = createLogic(failing);

        String message = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "Test save failure");
        assertThrows(CommandException.class, message, () -> failingLogic.execute("note 1 no/"));

        assertEquals(before, Files.readString(addressBookStorage.getAddressBookFilePath()));
        assertTrue(addressBookStorage.readAddressBook().orElseThrow().getPersonList().getFirst().hasNote());
    }

    @Test
    public void execute_addWithNotePrefix_keepsCandidatesViewAndSavedFileUnchanged() throws Exception {
        addressBookStorage.saveAddressBook(model.getAddressBook());
        logic.execute("find Benson");

        List<String> inputs = List.of("add n/A p/123 e/a@x.com a/Blk 1 no/great candidate",
                "add n/A p/123 e/a@x.com a/Blk 1 no/",
                "add n/A p/123 e/a@x.com a/Blk 1\tno/great candidate");

        assertAllCases(inputs, command -> {
            assertRejectedCommandPreservesState(command, ParseException.class,
                    AddCommandParser.MESSAGE_UNSUPPORTED_NOTE);
        });
    }

    /**
     * Saves candidates with notes for the note-removal scenarios.
     */
    private void prepareCandidatesWithNotes() throws IOException {
        AddressBook candidates = getTypicalAddressBook();
        candidates.setPerson(ALICE, new PersonBuilder(ALICE).withNote("Keep this note").build());
        candidates.setPerson(BENSON, new PersonBuilder(BENSON).withStatus(Status.INTERVIEWING)
                .withNote("Remove this note").build());
        model.setAddressBook(candidates);
        addressBookStorage.saveAddressBook(model.getAddressBook());
    }

    /**
     * Prepares a saved note and filtered selection before checking a rejected note command.
     */
    private void assertInvalidFilteredNoteRejected(String command, Class<? extends Throwable> expectedException,
            String expectedMessage) throws Exception {
        logic.execute("note 2 no/Existing note");
        logic.execute("find Benson");

        assertRejectedCommandPreservesState(command, expectedException, expectedMessage);
    }

    /**
     * Asserts that a rejected command preserves the saved data, candidates, selection, and view mode.
     */
    private void assertRejectedCommandPreservesState(String command, Class<? extends Throwable> expectedException,
            String expectedMessage) throws IOException {
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        String fileBefore = Files.readString(addressBookStorage.getAddressBookFilePath());
        boolean wasExpanded = logic.isExpandedViewProperty().get();

        assertThrows(expectedException, expectedMessage, () -> logic.execute(command));

        assertEquals(before, model.getAddressBook());
        assertEquals(displayedBefore, model.getFilteredPersonList());
        assertEquals(wasExpanded, logic.isExpandedViewProperty().get());
        assertEquals(fileBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
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
        return new LogicTestFixture(temporaryFolder, model, storage).getLogic();
    }

    /**
     * Reads the candidate at the given zero-based index from saved data.
     */
    private Person readCandidate(int index) throws Exception {
        return fixture.readSaved().getPersonList().get(index);
    }
}
