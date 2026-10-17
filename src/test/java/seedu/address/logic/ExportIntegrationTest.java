package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class ExportIntegrationTest {
    @TempDir
    public Path directory;

    @Test
    public void execute_filteredAndExpanded_exportsAllWithoutSavingOrChangingState() throws Exception {
        ModelManager model = new ModelManager();
        var alice = new PersonBuilder().withName("Alice").withEmail("alice@example.com").build();
        var bob = new PersonBuilder().withName("Bob").withEmail("bob@example.com").build();
        model.addPerson(alice);
        model.addPerson(bob);
        model.updateFilteredPersonList(person -> person.equals(bob));
        model.expandPerson(bob);
        Path dataFile = directory.resolve("data/book.json");
        Files.createDirectories(dataFile.getParent());
        Files.writeString(dataFile, "unchanged JSON sentinel");
        Logic logic = new LogicManager(model, createStorage(dataFile));

        CommandResult result = logic.execute("export");

        assertEquals("Exported 2 candidates to " + directory.resolve("export.csv") + ".", result.getFeedbackToUser());
        assertEquals(List.of(alice, bob), model.getAddressBook().getPersonList());
        assertEquals(List.of(bob), model.getFilteredPersonList());
        assertTrue(model.isExpandedViewProperty().get());
        List<String> records = Files.readAllLines(directory.resolve("export.csv"));
        assertEquals(3, records.size());
        assertTrue(records.get(1).startsWith("\"Alice\""));
        assertTrue(records.get(2).startsWith("\"Bob\""));
        assertEquals("unchanged JSON sentinel", Files.readString(dataFile));
    }

    @Test
    public void execute_emptyBook_reportsZero() throws Exception {
        Logic logic = new LogicManager(new ModelManager(), createStorage(directory.resolve("data/book.json")));
        assertEquals("Exported 0 candidates to " + directory.resolve("export.csv") + ".",
                logic.execute("export").getFeedbackToUser());
        assertEquals(1, Files.readAllLines(directory.resolve("export.csv")).size());
    }

    @Test
    public void execute_exportFailure_preservesStateAndDoesNotSaveJson() throws Exception {
        ModelManager model = new ModelManager();
        var person = new PersonBuilder().withEmail("alice@example.com").build();
        model.addPerson(person);
        model.expandPerson(person);
        Files.createDirectory(directory.resolve("export.csv"));
        Logic logic = new LogicManager(model, createStorage(directory.resolve("data/book.json")));

        assertThrows(CommandException.class, () -> logic.execute("export"));

        assertEquals(List.of(person), model.getAddressBook().getPersonList());
        assertEquals(List.of(person), model.getFilteredPersonList());
        assertTrue(model.isExpandedViewProperty().get());
        assertTrue(Files.isDirectory(directory.resolve("export.csv")));
    }

    private StorageManager createStorage(Path dataFile) {
        return new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                throw new AssertionError("Export must not save JSON");
            }
        };
    }
}
