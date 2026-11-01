package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalRoleAssignments.getAssignedModel;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;

/** Verifies that ambiguous native JSON takes the byte-preserving startup recovery path. */
public class NativeJsonValidationTest {
    @TempDir
    Path directory;

    private Path candidateFile;
    private Path roleFile;
    private StorageManager storage;

    @BeforeEach
    public void setUp() throws Exception {
        candidateFile = directory.resolve("HRvest.json");
        roleFile = directory.resolve("roles.json");
        storage = new StorageManager(new JsonAddressBookStorage(candidateFile),
                new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        storage.saveAddressBook(getAssignedModel("Engineer").getAddressBook());
    }

    @Test
    public void load_duplicateCandidateProperty_preservesBothOriginalFiles() throws Exception {
        assertDuplicatePropertyPreservesFiles(candidateFile, "persons");
    }

    @Test
    public void load_duplicateRoleProperty_preservesBothOriginalFiles() throws Exception {
        assertDuplicatePropertyPreservesFiles(roleFile, "roles");
    }

    @Test
    public void load_candidateFileWithTrailingGarbage_preservesBothOriginalFiles() throws Exception {
        assertRecoveryPreservesFiles(candidateFile, Files.readString(candidateFile) + "garbage");
    }

    @Test
    public void load_roleFileWithTrailingGarbage_preservesBothOriginalFiles() throws Exception {
        assertRecoveryPreservesFiles(roleFile, Files.readString(roleFile) + "garbage");
    }

    @Test
    public void load_candidateFileWithTrailingObject_preservesBothOriginalFiles() throws Exception {
        assertRecoveryPreservesFiles(candidateFile, Files.readString(candidateFile) + " {}");
    }

    @Test
    public void load_roleFileWithTrailingObject_preservesBothOriginalFiles() throws Exception {
        assertRecoveryPreservesFiles(roleFile, Files.readString(roleFile) + " {}");
    }

    @Test
    public void load_candidateFileWithIncompleteTrailingObject_preservesBothOriginalFiles() throws Exception {
        assertRecoveryPreservesFiles(candidateFile, Files.readString(candidateFile) + " {");
    }

    @Test
    public void load_roleFileWithIncompleteTrailingObject_preservesBothOriginalFiles() throws Exception {
        assertRecoveryPreservesFiles(roleFile, Files.readString(roleFile) + " {");
    }

    /**
     * Adds a duplicate top-level property and verifies recovery of both data files.
     */
    private void assertDuplicatePropertyPreservesFiles(Path target, String property) throws Exception {
        String json = Files.readString(target).stripTrailing();
        String invalidJson = json.substring(0, json.length() - 1) + ",\"" + property + "\":[]}";

        assertRecoveryPreservesFiles(target, invalidJson);
    }

    /**
     * Verifies that an invalid file is rejected and recovery backs up both files byte for byte.
     */
    private void assertRecoveryPreservesFiles(Path target, String invalidJson) throws Exception {
        Files.writeString(target, invalidJson);
        byte[] originalCandidates = Files.readAllBytes(candidateFile);
        byte[] originalRoles = Files.readAllBytes(roleFile);
        CandidateDataInitializer initializer = new CandidateDataInitializer(storage);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertEquals(new AddressBook(), initializer.load());

        Path backup = initializer.getRecoveryBackup().orElseThrow();
        assertArrayEquals(originalCandidates, Files.readAllBytes(backup));
        assertArrayEquals(originalRoles,
                Files.readAllBytes(backup.resolveSibling(backup.getFileName() + ".roles.json")));
    }
}
