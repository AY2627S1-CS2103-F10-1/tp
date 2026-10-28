package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.role.Role;

/** Verifies canonical Unicode lookup, stable assignments, and safe recovery of legacy name collisions. */
public class UnicodeRoleCompatibilityTest {
    @TempDir
    public Path directory;

    @Test
    public void execute_equivalentNames_shareAssignmentsAcrossRenameAndRestart() throws Exception {
        StorageManager storage = storage();
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("add_role n/Cafe\u0301");
        logic.execute("add n/Candidate p/123 e/candidate@example.com a/Street r/Caf\u00e9");
        Role original = model.getRoleCatalog().requireRole("Caf\u00e9");
        assertThrows(CommandException.class, () -> logic.execute("add_role n/Caf\u00e9"));
        logic.execute("filter r/Caf\u00e9");
        assertEquals(1, model.getFilteredPersonList().size());
        logic.execute("edit_role r/Caf\u00e9 n/CAF\u00c9");
        assertEquals(1, model.getFilteredPersonList().size());

        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Role renamed = restarted.getRoleCatalog().requireRole("Cafe\u0301");
        assertEquals(original.getId(), renamed.getId());
        assertEquals(Set.of(renamed), restarted.getAddressBook().getPersonList().getFirst().getRoles());
        new LogicManager(restarted, storage).execute("delete_role r/Cafe\u0301");
        assertEquals(Set.of(Role.NO_ROLE), restarted.getAddressBook().getPersonList().getFirst().getRoles());
    }

    @Test
    public void load_legacyCanonicalCollision_backsUpWithoutMergingRoleIds() throws Exception {
        StorageManager storage = storage();
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("add_role n/Caf\u00e9");
        logic.execute("add_role n/Other");
        logic.execute("add n/Candidate p/123 e/candidate@example.com a/Street r/Caf\u00e9 r/Other");
        Path candidates = directory.resolve("HRvest.json");
        Path catalog = directory.resolve("roles.json");
        byte[] originalCandidates = Files.readAllBytes(candidates);
        byte[] validCatalog = Files.readAllBytes(catalog);
        Files.writeString(catalog, Files.readString(catalog).replace("Other", "Cafe\u0301"));
        byte[] invalidCatalog = Files.readAllBytes(catalog);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        CandidateDataInitializer initializer = new CandidateDataInitializer(storage);
        assertEquals(new AddressBook(), initializer.load());

        Path backup = initializer.getRecoveryBackup().orElseThrow();
        assertArrayEquals(originalCandidates, Files.readAllBytes(backup));
        assertArrayEquals(invalidCatalog,
                Files.readAllBytes(backup.resolveSibling(backup.getFileName() + ".roles.json")));
        Files.write(candidates, originalCandidates);
        Files.write(catalog, validCatalog);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertTrue(Files.exists(backup));
    }

    private StorageManager storage() {
        return new StorageManager(new JsonAddressBookStorage(directory.resolve("HRvest.json")),
                new JsonUserPrefsStorage(directory.resolve("prefs.json")));
    }
}
