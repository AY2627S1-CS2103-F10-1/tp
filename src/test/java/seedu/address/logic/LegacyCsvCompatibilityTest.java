package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Checks that exported legacy data can be restored without relaxing command input rules. */
public class LegacyCsvCompatibilityTest {
    @TempDir
    public Path directory;

    @Test
    public void execute_exportedLegacyUnicode_restoresThroughImportAndJson() throws Exception {
        Person candidate = new PersonBuilder().withAddress("Caf\u00e9 Road")
                .withNote("R\u00e9sum\u00e9 reviewed\nFollow up").build();
        AddressBook source = new AddressBook();
        source.addPerson(candidate);
        Path sourcePath = directory.resolve("original.json");
        JsonAddressBookStorage original = new JsonAddressBookStorage(sourcePath);
        original.saveAddressBook(source);
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(directory.resolve("restored.json")),
                new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        Path exported = storage.exportAddressBook(original.readAddressBook().orElseThrow());
        Files.copy(exported, directory.resolve("import.csv"));
        ModelManager model = new ModelManager();

        new LogicManager(model, storage).execute("import");

        assertEquals(source, model.getAddressBook());
        assertEquals(source, storage.readAddressBook().orElseThrow());
        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(source, restarted.getAddressBook());
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(candidate.getAddress().value));
        assertThrows(ParseException.class, () -> ParserUtil.parseNote(candidate.getNote().orElseThrow().value));
    }
}
