package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Status;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class NoteRemovalIntegrationTest {
    @TempDir
    Path temporaryFolder;

    private Model model;
    private JsonAddressBookStorage storage;
    private Logic logic;

    @BeforeEach
    public void setUp() throws IOException {
        AddressBook candidates = getTypicalAddressBook();
        candidates.setPerson(ALICE, new PersonBuilder(ALICE).withNote("Keep this note").build());
        candidates.setPerson(BENSON, new PersonBuilder(BENSON).withStatus(Status.INTERVIEWING)
                .withNote("Remove this note").build());
        model = new ModelManager(candidates, new UserPrefs());
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("candidates.json"));
        storage.saveAddressBook(model.getAddressBook());
        logic = createLogic(storage);
    }

    @Test
    public void execute_clearFilteredCandidate_removesOnlySelectedNoteAndPersistsAcrossReload() throws Exception {
        logic.execute("find Benson");
        String address = BENSON.getAddress().value;

        assertEquals(String.format(NoteCommand.MESSAGE_CLEAR_SUCCESS, BENSON.getName()),
                logic.execute("note 1 no/").getFeedbackToUser());

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(BENSON.getEmail(), model.getFilteredPersonList().getFirst().getEmail());
        assertFalse(model.getFilteredPersonList().getFirst().hasNote());
        ReadOnlyAddressBook reloaded = storage.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), reloaded);
        assertTrue(reloaded.getPersonList().getFirst().hasNote());
        assertFalse(reloaded.getPersonList().get(1).hasNote());
        assertEquals(Status.INTERVIEWING, reloaded.getPersonList().get(1).getStatus());
        assertEquals(address, reloaded.getPersonList().get(1).getAddress().value);
        logic.execute("list");
        logic.execute("note 2 no/   ");
        assertEquals(reloaded, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_clearExpandedCandidate_removesSelectedNote() throws Exception {
        logic.execute("expand 2");

        logic.execute("note 1 no/");

        assertTrue(logic.isExpandedViewProperty().get());
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(BENSON.getEmail(), model.getFilteredPersonList().getFirst().getEmail());
        assertFalse(model.getFilteredPersonList().getFirst().hasNote());
        assertFalse(storage.readAddressBook().orElseThrow().getPersonList().get(1).hasNote());
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().getFirst().hasNote());
    }

    @Test
    public void execute_clearOutsideFilteredList_preservesSavedData() throws Exception {
        logic.execute("find Benson");
        String before = Files.readString(storage.getAddressBookFilePath());

        assertThrows(CommandException.class, NoteCommand.MESSAGE_INVALID_CANDIDATE_INDEX, () ->
                logic.execute("note 2 no/"));

        assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
        assertTrue(model.getFilteredPersonList().getFirst().hasNote());
    }

    @Test
    public void execute_clearSaveFailure_preservesPreviouslySavedNoteAndReportsError() throws Exception {
        logic.execute("list");
        String before = Files.readString(storage.getAddressBookFilePath());
        JsonAddressBookStorage failing = new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook candidates) throws IOException {
                throw new IOException("Test save failure");
            }
        };
        Logic failingLogic = createLogic(failing);

        String message = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "Test save failure");
        assertThrows(CommandException.class, message, () -> failingLogic.execute("note 1 no/"));

        assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().getFirst().hasNote());
    }

    private Logic createLogic(JsonAddressBookStorage dataStorage) {
        return new LogicManager(model, new StorageManager(dataStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
