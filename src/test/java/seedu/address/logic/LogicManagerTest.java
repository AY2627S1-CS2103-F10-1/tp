package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        // The model starts empty, so list reports that there are no candidates.
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_NO_CANDIDATES, model);
    }

    @Test
    public void getAllPersonList_filteredView_includesHiddenPersons() throws Exception {
        Person hidden = new PersonBuilder().withName("Hidden Person").withEmail("hidden@example.com").build();
        model.addPerson(AMY);
        model.addPerson(hidden);

        logic.execute("find " + AMY.getName().fullName.split(" ")[0]);

        assertEquals(List.of(AMY), logic.getFilteredPersonList());
        assertEquals(List.of(AMY, hidden), logic.getAllPersonList());
        assertThrows(UnsupportedOperationException.class, () -> logic.getAllPersonList().remove(0));
    }

    @Test
    public void execute_addCommand_savesAppliedAndResetsFilter() throws Exception {
        Person existing = new PersonBuilder().withName("Existing Candidate").withEmail("existing@example.com").build();
        model.addPerson(existing);
        model.updateFilteredPersonList(person -> false);
        Person expected = new PersonBuilder(AMY).withTags().withStatus(Status.APPLIED).build();

        CommandResult result = logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(expected)), result.getFeedbackToUser());
        assertEquals(List.of(existing, expected), model.getFilteredPersonList());
        assertEquals(Status.APPLIED, model.getFilteredPersonList().get(1).getStatus());
        JsonAddressBookStorage saved = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(model.getAddressBook(), saved.readAddressBook().orElseThrow());
        assertEquals(Status.APPLIED, saved.readAddressBook().orElseThrow().getPersonList().get(1).getStatus());
    }

    @Test
    public void execute_addWithExplicitStatus_savesCanonicalStatus() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + ADDRESS_DESC_AMY + " s/  iNtErViEwInG  ");

        assertEquals(Status.INTERVIEWING, model.getFilteredPersonList().get(0).getStatus());
        JsonAddressBookStorage saved = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(Status.INTERVIEWING, saved.readAddressBook().orElseThrow().getPersonList().get(0).getStatus());
        assertTrue(Files.readString(saved.getAddressBookFilePath()).contains("\"status\" : \"Interviewing\""));
    }

    @Test
    public void execute_addWithInvalidOrRepeatedStatus_preservesSavedData() throws Exception {
        String input = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        logic.execute(input);
        Path savedPath = temporaryFolder.resolve("addressBook.json");
        String original = Files.readString(savedPath);

        assertParseException(input + " s/Hired", Status.MESSAGE_CONSTRAINTS);
        assertParseException(input + " s/", Status.MESSAGE_CONSTRAINTS);
        assertParseException(input + " s/Applied s/Offered", Messages.getErrorMessageForDuplicatePrefixes(
                PREFIX_STATUS));

        assertEquals(original, Files.readString(savedPath));
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(Status.APPLIED, model.getAddressBook().getPersonList().get(0).getStatus());
    }

    @Test
    public void execute_duplicateAdd_preservesSavedData() throws Exception {
        String input = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        logic.execute(input);
        Path savedPath = temporaryFolder.resolve("addressBook.json");
        String original = Files.readString(savedPath);

        assertCommandException(input, AddCommand.MESSAGE_DUPLICATE_PERSON);

        assertEquals(original, Files.readString(savedPath));
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(Status.APPLIED, model.getAddressBook().getPersonList().get(0).getStatus());
    }

    @Test
    public void execute_duplicateEmailWithCaseAndWhitespace_preservesSavedData() throws Exception {
        logic.execute("add n/Alice p/123 e/alice@example.com a/Street");
        Path savedPath = temporaryFolder.resolve("addressBook.json");
        String original = Files.readString(savedPath);

        assertCommandException("add n/Bob p/456 e/  ALICE@EXAMPLE.COM  a/Other Street",
                AddCommand.MESSAGE_DUPLICATE_PERSON);
        assertEquals(original, Files.readString(savedPath));
        logic.execute("add n/Bob p/456 e/bob@example.com a/Other Street");
        String beforeEdit = Files.readString(savedPath);
        assertCommandException("edit 2 e/  ALICE@EXAMPLE.COM  ", EditCommand.MESSAGE_DUPLICATE_PERSON);

        assertEquals(beforeEdit, Files.readString(savedPath));
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertEquals("alice@example.com", model.getAddressBook().getPersonList().getFirst().getEmail().value);
    }

    @Test
    public void execute_invalidAdd_doesNotChangeOrSaveData() {
        assertParseException("add", String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertFalse(Files.exists(temporaryFolder.resolve("addressBook.json")));
    }

    @Test
    public void execute_statusCommand_savesUpdatedStatus() throws Exception {
        Person candidate = new PersonBuilder().build();
        model.addPerson(candidate);
        String expectedMessage = String.format("Updated status of %s: Applied -> Offered", candidate.getName());

        CommandResult result = logic.execute("status 1 s/oFfErEd");

        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(Status.OFFERED, model.getFilteredPersonList().get(0).getStatus());
        JsonAddressBookStorage saved = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(Status.OFFERED, saved.readAddressBook().get().getPersonList().get(0).getStatus());
    }

    @Test
    public void execute_invalidStatus_doesNotChangeOrSaveData() throws Exception {
        model.addPerson(new PersonBuilder().build());

        assertParseException("status 1 s/Hired", Status.MESSAGE_CONSTRAINTS);

        assertEquals(Status.APPLIED, model.getFilteredPersonList().get(0).getStatus());
        JsonAddressBookStorage saved = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertFalse(saved.readAddressBook().isPresent());
    }

    @Test
    public void execute_expandCommand_preservesStoredData() throws Exception {
        Person candidate = new PersonBuilder().withNote("Feedback\n" + "x".repeat(490)).build();
        model.addPerson(candidate);
        JsonAddressBookStorage saved = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        saved.saveAddressBook(model.getAddressBook());
        String original = Files.readString(saved.getAddressBookFilePath());

        CommandResult result = logic.execute("expand 1");

        assertEquals("Expanding candidate: " + candidate.getName() + ".", result.getFeedbackToUser());
        assertTrue(logic.isExpandedViewProperty().get());
        assertEquals(original, Files.readString(saved.getAddressBookFilePath()));
        assertEquals(model.getAddressBook(), saved.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_invalidExpand_preservesCurrentView() throws Exception {
        model.addPerson(new PersonBuilder().build());
        logic.execute("expand 1");
        Path savedPath = temporaryFolder.resolve("addressBook.json");
        String savedData = Files.readString(savedPath);

        assertThrows(ParseException.class, () -> logic.execute("expand 999999999999999999999"));
        assertThrows(CommandException.class, () -> logic.execute("expand 2"));

        assertTrue(logic.isExpandedViewProperty().get());
        assertEquals(1, logic.getFilteredPersonList().size());
        assertEquals(savedData, Files.readString(savedPath));
    }

    @Test
    public void execute_listAfterExpand_restoresNormalView() throws Exception {
        model.addPerson(new PersonBuilder().build());
        logic.execute("expand 1");

        logic.execute("list");

        assertFalse(logic.isExpandedViewProperty().get());
        assertEquals(1, logic.getFilteredPersonList().size());
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withTags().withStatus(Status.APPLIED).build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
