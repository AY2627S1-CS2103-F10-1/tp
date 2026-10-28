package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalRoleAssignments.getAssignedModel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;

/** Verifies that ambiguous native JSON takes the byte-preserving startup recovery path. */
public class NativeJsonValidationTest {
    @TempDir
    public Path directory;

    @Test
    public void load_duplicatePropertiesOrTrailingContent_preservesBothOriginalFiles() throws Exception {
        int index = 0;
        for (String fileName : List.of("HRvest.json", "roles.json")) {
            for (String corruption : List.of("duplicate", "garbage", " {}", " {")) {
                Path folder = Files.createDirectory(directory.resolve("case" + index++));
                Path candidates = folder.resolve("HRvest.json");
                Path roles = folder.resolve("roles.json");
                StorageManager storage = new StorageManager(new JsonAddressBookStorage(candidates),
                        new JsonUserPrefsStorage(folder.resolve("prefs.json")));
                storage.saveAddressBook(getAssignedModel("Engineer").getAddressBook());
                Path target = folder.resolve(fileName);
                String json = Files.readString(target);
                if (corruption.equals("duplicate")) {
                    String property = fileName.equals("roles.json") ? "roles" : "persons";
                    json = json.stripTrailing();
                    json = json.substring(0, json.length() - 1) + ",\"" + property + "\":[]}";
                } else {
                    json += corruption;
                }
                Files.writeString(target, json);
                byte[] originalCandidates = Files.readAllBytes(candidates);
                byte[] originalRoles = Files.readAllBytes(roles);

                assertThrows(DataLoadingException.class, storage::readAddressBook);
                CandidateDataInitializer initializer = new CandidateDataInitializer(storage);
                assertEquals(new AddressBook(), initializer.load());

                Path backup = initializer.getRecoveryBackup().orElseThrow();
                assertArrayEquals(originalCandidates, Files.readAllBytes(backup));
                assertArrayEquals(originalRoles,
                        Files.readAllBytes(backup.resolveSibling(backup.getFileName() + ".roles.json")));
            }
        }
    }
}
