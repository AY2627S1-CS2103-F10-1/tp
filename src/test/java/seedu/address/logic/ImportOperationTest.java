package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.role.Role;
import seedu.address.model.role.RoleCatalog;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class ImportOperationTest {
    @TempDir
    public Path directory;

    private final Person existing = new PersonBuilder().withEmail("existing@example.com").build();
    private final Person incoming = new PersonBuilder().withName("Incoming Candidate")
            .withEmail("incoming@example.com").withTags("java", "Java").withNote("Comma, quote \" and text").build();
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

        assertEquals("Imported 1 candidate successfully.", logic.execute("import").getFeedbackToUser());

        assertEquals(List.of(existing, incoming), model.getAddressBook().getPersonList());
        assertEquals(List.of(existing, incoming), model.getFilteredPersonList());
        assertFalse(model.isExpandedViewProperty().get());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertArrayEquals(input, Files.readAllBytes(inputPath()));
    }

    @Test
    public void execute_maximumBatch_preservesOrderInputAndSavedData() throws Exception {
        List<Person> imported = IntStream.range(0, 10_000)
                .mapToObj(index -> new PersonBuilder(incoming).withEmail("batch" + index + "@example.com").build())
                .toList();
        writeInput(imported.toArray(Person[]::new));
        byte[] originalInput = Files.readAllBytes(inputPath());
        model.expandPerson(existing);

        assertEquals("Imported 10000 candidates successfully.", logic.execute("import").getFeedbackToUser());

        assertEquals(existing, model.getAddressBook().getPersonList().getFirst());
        assertEquals(imported, model.getAddressBook().getPersonList().subList(1, imported.size() + 1));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertFalse(model.isExpandedViewProperty().get());
        assertArrayEquals(originalInput, Files.readAllBytes(inputPath()));
    }

    @Test
    public void execute_exportClearConfirmImport_preservesCandidatesWithBom() throws Exception {
        model.createRole("Developer");
        Role developer = model.getRoleCatalog().requireRole("Developer");
        model.addPerson(new PersonBuilder(incoming).withRoles(developer).build());
        List<Person> expected = List.copyOf(model.getAddressBook().getPersonList());
        logic.execute("export");
        Files.copy(directory.resolve("data/export.csv"), inputPath());
        byte[] source = Files.readAllBytes(inputPath());
        assertEquals((byte) 0xef, source[0]);
        assertEquals((byte) 0xbb, source[1]);
        assertEquals((byte) 0xbf, source[2]);

        logic.execute("clear");
        assertEquals(expected, model.getAddressBook().getPersonList());
        assertEquals(ClearCommand.MESSAGE_SUCCESS, logic.execute("clear confirm").getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals("Imported 2 candidates successfully.", logic.execute("import").getFeedbackToUser());

        assertEquals(expected, model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertArrayEquals(source, Files.readAllBytes(inputPath()));
    }

    @Test
    public void execute_legacyNoteText_importsWithoutChangingInput() throws Exception {
        model.expandPerson(existing);
        int index = 0;
        for (String note : List.of("Unicode \u4e2d\u6587", "First\r\nsecond", "Tab\there")) {
            Person candidate = new PersonBuilder(incoming).withEmail("legacy" + index++ + "@example.com")
                    .withNote(note).build();
            writeInput(candidate);
            byte[] input = Files.readAllBytes(inputPath());

            logic.execute("import");

            assertEquals(candidate, model.getAddressBook().getPersonList().getLast());
            assertFalse(model.isExpandedViewProperty().get());
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
            assertArrayEquals(input, Files.readAllBytes(inputPath()));
        }
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
    public void execute_unknownRoles_createsEachRoleOnceAndReportsThem() throws Exception {
        writeInput(new PersonBuilder(incoming).withRoles(Role.create("QA"), Role.create("Designer")).build(),
                new PersonBuilder(incoming).withEmail("qa@example.com").withRoles(Role.create("qa")).build());

        assertEquals("Imported 2 candidates successfully. Created roles: Designer, QA.",
                logic.execute("import").getFeedbackToUser());

        assertEquals(List.of("Designer", "No role", "QA"),
                model.getRoleCatalog().getRoles().stream().map(Role::getName).toList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_saveFailureAfterCreatingRole_restoresCatalogAndAllowsRetry() throws Exception {
        writeInput(new PersonBuilder(incoming).withRoles(Role.create("QA")).build());
        RoleCatalog catalog = model.getRoleCatalog();
        storage.failure = new IOException("Injected write failure");

        assertThrows(CommandException.class, () -> logic.execute("import"));

        assertEquals(catalog, model.getRoleCatalog());
        storage.failure = null;
        assertEquals("Imported 1 candidate successfully. Created role: QA.",
                logic.execute("import").getFeedbackToUser());
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
    public void execute_missingOrInvalidInput_doesNotSaveOrChangeState() throws Exception {
        model.expandPerson(existing);
        int saves = storage.saves;
        assertThrows(CommandException.class, () -> logic.execute("import"));
        Files.writeString(inputPath(), "invalid CSV");
        assertThrows(CommandException.class, () -> logic.execute("import"));
        assertThrows(CommandException.class, () -> logic.execute("import other.csv"));
        assertEquals(saves, storage.saves);
        assertEquals(List.of(existing), model.getFilteredPersonList());
        assertTrue(model.isExpandedViewProperty().get());
    }

    @Test
    public void execute_extraArguments_usesFixedInputAndSavesNormally() throws Exception {
        writeInput(incoming);
        Path ignored = directory.resolve("other.csv");
        Files.writeString(ignored, "invalid CSV that must not be read");
        byte[] source = Files.readAllBytes(inputPath());

        assertEquals("Imported 1 candidate successfully.",
                logic.execute("import " + ignored + " ignored arguments").getFeedbackToUser());

        assertEquals(List.of(existing, incoming), model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertArrayEquals(source, Files.readAllBytes(inputPath()));
        assertEquals("invalid CSV that must not be read", Files.readString(ignored));
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

    @Test
    public void execute_nullOriginalFilter_restoresShowAllFilter() throws Exception {
        writeInput(incoming);
        ModelManager nullFilterModel = new ModelManager(model.getAddressBook(), model.getUserPrefs()) {
            @Override
            public Predicate<? super Person> getPersonFilter() {
                return null;
            }
        };
        storage.failure = new IOException("Injected save failure");

        assertThrows(CommandException.class, () -> new ImportOperation(nullFilterModel, storage).execute());

        assertEquals(List.of(existing), nullFilterModel.getAddressBook().getPersonList());
        nullFilterModel.addPerson(incoming);
        assertEquals(List.of(existing, incoming), nullFilterModel.getFilteredPersonList());
    }

    @Test
    public void execute_rollbackDoesNotRestoreCandidates_reportsVerificationFailure() throws Exception {
        writeInput(incoming);
        ModelManager failingModel = new ModelManager(model.getAddressBook(), model.getUserPrefs()) {
            private int applications;

            @Override
            public void setAddressBook(ReadOnlyAddressBook book) {
                applications++;
                if (applications == 1) {
                    super.setAddressBook(book);
                }
            }
        };
        storage.failure = new IOException("Injected save failure");

        assertRollbackVerificationFailure(failingModel);
        assertEquals(List.of(existing, incoming), failingModel.getAddressBook().getPersonList());
    }

    @Test
    public void execute_rollbackDoesNotRestoreExpandedView_reportsVerificationFailure() throws Exception {
        writeInput(incoming);
        ModelManager failingModel = new ModelManager(model.getAddressBook(), model.getUserPrefs()) {
            private int expansions;

            @Override
            public void expandPerson(Person person) {
                expansions++;
                if (expansions == 1) {
                    super.expandPerson(person);
                }
            }
        };
        failingModel.expandPerson(existing);
        storage.failure = new IOException("Injected save failure");

        assertRollbackVerificationFailure(failingModel);
        assertEquals(List.of(existing), failingModel.getAddressBook().getPersonList());
        assertFalse(failingModel.isExpandedViewProperty().get());
    }

    @Test
    public void execute_sameApplicationAndRollbackException_skipsSelfSuppression() throws Exception {
        writeInput(incoming);
        IllegalStateException failure = new IllegalStateException("Injected persistent failure");
        ModelManager failingModel = new ModelManager(model.getAddressBook(), model.getUserPrefs()) {
            @Override
            public void setAddressBook(ReadOnlyAddressBook book) {
                throw failure;
            }
        };

        CommandException thrown = org.junit.jupiter.api.Assertions.assertThrows(CommandException.class, () ->
                new ImportOperation(failingModel, storage).execute());

        assertEquals("Import failed and rollback failed. State could not be restored; "
                + "stop using HRvest and check the saved data before restarting.", thrown.getMessage());
        assertSame(failure, thrown.getCause());
        assertEquals(0, failure.getSuppressed().length);
    }

    private void assertRollbackVerificationFailure(Model failingModel) throws Exception {
        byte[] disk = Files.readAllBytes(storage.getAddressBookFilePath());
        CommandException thrown = org.junit.jupiter.api.Assertions.assertThrows(CommandException.class, () ->
                new ImportOperation(failingModel, storage).execute());

        assertEquals("Import failed and rollback failed. State could not be restored; "
                + "stop using HRvest and check the saved data before restarting.", thrown.getMessage());
        assertTrue(thrown.getCause() instanceof IllegalStateException);
        assertEquals("Rollback verification failed.", thrown.getCause().getMessage());
        assertEquals(1, thrown.getCause().getSuppressed().length);
        assertArrayEquals(disk, Files.readAllBytes(storage.getAddressBookFilePath()));
    }

    private void writeInput(Person... persons) throws IOException {
        StringBuilder csv = new StringBuilder(record(List.of("name", "phone", "email", "address", "status",
                "tags", "note", "roles")));
        ObjectMapper mapper = new ObjectMapper();
        for (Person person : persons) {
            String tags = mapper.writeValueAsString(person.getTags().stream().map(tag -> tag.tagName).toList());
            String roles = mapper.writeValueAsString(person.getRoles().stream().map(Role::getName).toList());
            csv.append(record(List.of(person.getName().fullName, person.getPhone().value, person.getEmail().value,
                    person.getAddress().value, person.getStatus().toString(), tags,
                    person.getNote().map(note -> note.value).orElse(""), roles)));
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
