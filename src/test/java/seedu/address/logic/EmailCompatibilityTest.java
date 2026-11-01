package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestCaseUtil.assertAllCases;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.CandidateCsvParser;
import seedu.address.model.ModelManager;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.LogicTestFixture;

/** Exercises email validation through commands, persisted records, and CSV compatibility. */
public class EmailCompatibilityTest {
    @TempDir
    public Path directory;

    @Test
    public void execute_validDotAtomsAndDomains_roundTripsWithoutChangingIdentityRules() throws Exception {
        List<String> inputs = List.of("anne.o'brien@example.com", "alice!team@example.com",
                "alice@xn--bcher-kva.de", "alice@recruit--team.example.com");

        assertAllCases(inputs, email -> {
            Path folder = Files.createTempDirectory(directory, "email-case-");
            ModelManager model = new ModelManager();
            LogicTestFixture fixture = new LogicTestFixture(folder, model);
            StorageManager storage = fixture.getStorage();
            Logic logic = fixture.getLogic();
            logic.execute("add n/Candidate p/123 e/" + email + " a/Street r/No role");
            logic.execute("edit 1 e/" + email.toUpperCase(Locale.ROOT));

            assertEquals(model.getAddressBook(), fixture.readSaved());
            Path exported = storage.exportAddressBook(model.getAddressBook());
            assertEquals(model.getAddressBook().getPersonList(),
                    CandidateCsvParser.parse(Files.readAllBytes(exported), model.getRoleCatalog()));
            assertThrows(CommandException.class, () -> logic.execute(
                    "add n/Other p/456 e/" + email + " a/Other Street r/No role"));
            assertEquals(1, model.getAddressBook().getPersonList().size());
        });
    }
}
