package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class ImportOperationTest {
    @TempDir
    public Path directory;

    private final Person existing = new PersonBuilder().withEmail("existing@example.com").build();
    private final Person incoming = new PersonBuilder().withName("Incoming Candidate")
            .withEmail("incoming@example.com").withTags("java", "Java").withNote("Comma, quote \" and\nline").build();
    private Model model;
    private TestStorage storage;
    private Logic logic;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(existing);
        storage = new TestStorage();
        storage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_validCsv_appendsSavesAndResetsExpandedView() throws Exception {
        writeInput(incoming);
        byte[] input = Files.readAllBytes(inputPath());
        model.expandPerson(existing);

        assertEquals("Imported 1 candidates successfully.", logic.execute("import").getFeedbackToUser());

        assertEquals(List.of(existing, incoming), model.getAddressBook().getPersonList());
        assertEquals(List.of(existing, incoming), model.getFilteredPersonList());
        assertFalse(model.isExpandedViewProperty().get());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertArrayEquals(input, Files.readAllBytes(inputPath()));
    }

    @Test
    public void execute_existingDuplicateHiddenByFilter_rejectsWholeBatch() throws Exception {
        Person duplicate = new PersonBuilder(existing).withEmail("EXISTING@example.com").build();
        writeInput(incoming, duplicate);
        model.updateFilteredPersonList(person -> false);
        byte[] disk = Files.readAllBytes(storage.getAddressBookFilePath());

        assertThrows(CommandException.class, "Import failed: CSV record 3, field email: duplicate candidate "
                + "conflicts with existing HRvest data.", () -> logic.execute("import"));

        assertEquals(List.of(existing), model.getAddressBook().getPersonList());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertArrayEquals(disk, Files.readAllBytes(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_internalDuplicate_rejectsWholeBatch() throws Exception {
        writeInput(incoming);
        String record = Files.readString(inputPath()).split("\r\n", 2)[1];
        Files.writeString(inputPath(), record, StandardOpenOption.APPEND);

        assertThrows(CommandException.class, "Import failed: CSV record 3, field email: duplicate candidate "
                + "conflicts with CSV record 2.", () -> logic.execute("import"));
        assertEquals(List.of(existing), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_saveFailure_restoresUnsavedDataAndExpandedViewAndAllowsRetry() throws Exception {
        writeInput(incoming);
        Person unsaved = new PersonBuilder().withEmail("unsaved@example.com").build();
        model.addPerson(unsaved);
        model.expandPerson(unsaved);
        byte[] disk = Files.readAllBytes(storage.getAddressBookFilePath());
        byte[] input = Files.readAllBytes(inputPath());
        storage.failure = new IOException("Injected write failure");

        assertThrows(CommandException.class, () -> logic.execute("import"));

        assertEquals(List.of(existing, unsaved), model.getAddressBook().getPersonList());
        assertEquals(List.of(unsaved), model.getFilteredPersonList());
        assertTrue(model.isExpandedViewProperty().get());
        assertArrayEquals(disk, Files.readAllBytes(storage.getAddressBookFilePath()));
        assertArrayEquals(input, Files.readAllBytes(inputPath()));
        storage.failure = null;
        logic.execute("import");
        assertEquals(List.of(existing, unsaved, incoming), model.getFilteredPersonList());
    }

    @Test
    public void execute_permissionFailure_preservesFilterForFutureCandidates() throws Exception {
        writeInput(incoming);
        model.updateFilteredPersonList(person -> person.getEmail().value.startsWith("incoming"));
        storage.failure = new AccessDeniedException("Injected denial");

        assertThrows(CommandException.class, () -> logic.execute("import"));
        assertTrue(model.getFilteredPersonList().isEmpty());
        AddressBook updated = new AddressBook(model.getAddressBook());
        updated.addPerson(incoming);
        model.setAddressBook(updated);
        assertEquals(List.of(incoming), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyAndHeaderOnly_runsSaveAndResetsView() throws Exception {
        for (boolean header : List.of(false, true)) {
            if (header) {
                writeInput();
            } else {
                Files.write(inputPath(), new byte[0]);
            }
            model.expandPerson(existing);
            int saves = storage.saves;
            assertEquals("Imported 0 candidates successfully.", logic.execute("import").getFeedbackToUser());
            assertEquals(saves + 1, storage.saves);
            assertEquals(List.of(existing), model.getFilteredPersonList());
            assertFalse(model.isExpandedViewProperty().get());
        }
    }

    @Test
    public void execute_emptySaveFailure_restoresViewWithoutReadingJson() throws Exception {
        Files.write(inputPath(), new byte[0]);
        Files.delete(storage.getAddressBookFilePath());
        model.expandPerson(existing);
        storage.failure = new IOException("Injected failure");

        assertThrows(CommandException.class, () -> logic.execute("import"));
        assertTrue(model.isExpandedViewProperty().get());
        assertEquals(List.of(existing), model.getFilteredPersonList());
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_missingInvalidOrArguments_doesNotSaveOrChangeState() throws Exception {
        model.expandPerson(existing);
        int saves = storage.saves;
        assertThrows(CommandException.class, () -> logic.execute("import"));
        Files.writeString(inputPath(), "invalid CSV");
        assertThrows(CommandException.class, () -> logic.execute("import"));
        assertThrows(ParseException.class, () -> logic.execute("import other.csv"));
        assertEquals(saves, storage.saves);
        assertEquals(List.of(existing), model.getFilteredPersonList());
        assertTrue(model.isExpandedViewProperty().get());
    }

    @Test
    public void execute_repeatedImport_rejectsWithoutChangingSavedData() throws Exception {
        writeInput(incoming);
        logic.execute("import");
        byte[] disk = Files.readAllBytes(storage.getAddressBookFilePath());
        assertThrows(CommandException.class, () -> logic.execute("import"));
        assertEquals(List.of(existing, incoming), model.getFilteredPersonList());
        assertArrayEquals(disk, Files.readAllBytes(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_rollbackFailure_reportsRecoveryFailure() throws Exception {
        writeInput(incoming);
        ModelManager failingModel = new ModelManager(model.getAddressBook(), model.getUserPrefs()) {
            @Override
            public void setAddressBook(ReadOnlyAddressBook book) {
                throw new IllegalStateException("Injected persistent application failure");
            }
        };
        Logic failingLogic = new LogicManager(failingModel, storage);
        String message = "Import failed and rollback failed. State could not be restored; "
                + "stop using HRvest and check the saved data before restarting.";
        assertThrows(CommandException.class, message, () -> failingLogic.execute("import"));
    }

    @Test
    public void execute_partialApplicationFailure_restoresSnapshot() throws Exception {
        writeInput(incoming);
        ModelManager failingModel = new ModelManager(model.getAddressBook(), model.getUserPrefs()) {
            private boolean shouldFail = true;

            @Override
            public void setAddressBook(ReadOnlyAddressBook book) {
                super.setAddressBook(book);
                if (shouldFail) {
                    shouldFail = false;
                    throw new IllegalStateException("Injected application failure");
                }
            }
        };
        failingModel.expandPerson(existing);
        Logic failingLogic = new LogicManager(failingModel, storage);
        assertThrows(CommandException.class, () -> failingLogic.execute("import"));
        assertEquals(List.of(existing), failingModel.getAddressBook().getPersonList());
        assertTrue(failingModel.isExpandedViewProperty().get());
    }

    private void writeInput(Person... persons) throws IOException {
        StringBuilder csv = new StringBuilder(record(List.of("name", "phone", "email", "address", "status",
                "tags", "note")));
        ObjectMapper mapper = new ObjectMapper();
        for (Person person : persons) {
            String tags = mapper.writeValueAsString(person.getTags().stream().map(tag -> tag.tagName).toList());
            csv.append(record(List.of(person.getName().fullName, person.getPhone().value, person.getEmail().value,
                    person.getAddress().value, person.getStatus().toString(), tags,
                    person.getNote().map(note -> note.value).orElse(""))));
        }
        Files.writeString(inputPath(), csv);
    }

    private String record(List<String> fields) {
        return fields.stream().map(field -> "\"" + field.replace("\"", "\"\"") + "\"")
                .collect(Collectors.joining(",")) + "\r\n";
    }

    private Path inputPath() {
        return directory.resolve("data/import.csv");
    }

    private class TestStorage extends StorageManager {
        private IOException failure;
        private int saves;

        TestStorage() {
            super(new JsonAddressBookStorage(directory.resolve("data/HRvest.json")),
                    new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
            saves++;
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(book);
        }
    }
}
