package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.AddCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class AddNotePrefixIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_addWithNotePrefix_keepsCandidatesViewAndSavedFileUnchanged() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("candidates.json"));
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("find Benson");
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        String fileBefore = Files.readString(storage.getAddressBookFilePath());

        for (String command : List.of("add n/A p/123 e/a@x.com a/Blk 1 no/great candidate",
                "add n/A p/123 e/a@x.com a/Blk 1 no/")) {
            assertThrows(ParseException.class, AddCommandParser.MESSAGE_UNSUPPORTED_NOTE, () -> logic.execute(command));
        }

        assertEquals(before, model.getAddressBook());
        assertEquals(displayedBefore, model.getFilteredPersonList());
        assertEquals(fileBefore, Files.readString(storage.getAddressBookFilePath()));
    }
}
