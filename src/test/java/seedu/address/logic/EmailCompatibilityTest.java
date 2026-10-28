package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.CandidateCsvParser;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Exercises email validation through commands, persisted records, and CSV compatibility. */
public class EmailCompatibilityTest {
    @TempDir
    public Path directory;

    @Test
    public void execute_validDotAtomsAndDomains_roundTripsWithoutChangingIdentityRules() throws Exception {
        int index = 0;
        for (String email : List.of("anne.o'brien@example.com", "alice!team@example.com",
                "alice@xn--bcher-kva.de", "alice@recruit--team.example.com")) {
            Path folder = Files.createDirectory(directory.resolve("case" + index++));
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(folder.resolve("HRvest.json")),
                    new JsonUserPrefsStorage(folder.resolve("prefs.json")));
            ModelManager model = new ModelManager();
            LogicManager logic = new LogicManager(model, storage);
            logic.execute("add n/Candidate p/123 e/" + email + " a/Street r/No role");
            logic.execute("edit 1 e/" + email.toUpperCase(Locale.ROOT));

            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
            Path exported = storage.exportAddressBook(model.getAddressBook());
            assertEquals(model.getAddressBook().getPersonList(),
                    CandidateCsvParser.parse(Files.readAllBytes(exported)));
            assertThrows(CommandException.class, () -> logic.execute(
                    "add n/Other p/456 e/" + email + " a/Other Street r/No role"));
            assertEquals(1, model.getAddressBook().getPersonList().size());
        }
    }
}
