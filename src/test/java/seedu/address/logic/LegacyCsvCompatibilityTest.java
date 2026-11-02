package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.LogicTestFixture;
import seedu.address.testutil.PersonBuilder;

/** Checks that legacy JSON and export remain supported while import follows command field rules. */
public class LegacyCsvCompatibilityTest {
    @TempDir
    public Path directory;

    @Test
    public void execute_exportedUnicodeNote_preservesJsonAndExportButRejectsImport() throws Exception {
        Person candidate = new PersonBuilder().withAddress("Cafe Road")
                .withNote("R\u00e9sum\u00e9 reviewed\nFollow up").build();
        AddressBook source = new AddressBook();
        source.addPerson(candidate);
        Path sourcePath = directory.resolve("original.json");
        JsonAddressBookStorage original = new JsonAddressBookStorage(sourcePath);
        original.saveAddressBook(source);
        ModelManager model = new ModelManager();
        LogicTestFixture fixture = new LogicTestFixture(directory, model,
                new JsonAddressBookStorage(directory.resolve("restored.json")));
        StorageManager storage = fixture.getStorage();
        Path exported = storage.exportAddressBook(original.readAddressBook().orElseThrow());
        Files.copy(exported, directory.resolve("import.csv"));
        byte[] exportedBytes = Files.readAllBytes(exported);
        byte[] originalBytes = Files.readAllBytes(sourcePath);

        assertThrows(CommandException.class, "Import failed: CSV record 2, field note: "
                + String.format(ParserUtil.MESSAGE_UNSUPPORTED_CHARACTER, "Note", 0x00e9), () ->
                fixture.getLogic().execute("import"));

        assertEquals(new AddressBook(), model.getAddressBook());
        assertFalse(Files.exists(directory.resolve("restored.json")));
        assertEquals(source, original.readAddressBook().orElseThrow());
        assertArrayEquals(originalBytes, Files.readAllBytes(sourcePath));
        assertArrayEquals(exportedBytes, Files.readAllBytes(exported));
        assertArrayEquals(exportedBytes, Files.readAllBytes(directory.resolve("import.csv")));
        assertEquals(candidate.getAddress(), ParserUtil.parseAddress(candidate.getAddress().value));
        assertThrows(ParseException.class, () -> ParserUtil.parseNote(candidate.getNote().orElseThrow().value));
    }
}
