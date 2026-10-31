package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;

public class CandidateDataInitializerTest {
    private static final Path TEST_DATA_FOLDER = Path.of("src", "test", "data");

    @TempDir
    Path temporaryFolder;

    @Test
    public void load_malformedJson_preservesOriginalBeforeCommandsCanSave() throws Exception {
        assertRejectedFileBackedUpBeforeCommands("{invalid JSON");
    }

    @Test
    public void load_invalidCandidate_preservesOriginalBeforeCommandsCanSave() throws Exception {
        Path fixture = TEST_DATA_FOLDER.resolve("JsonAddressBookStorageTest/invalidAndValidPersonAddressBook.json");
        assertRejectedFileBackedUpBeforeCommands(Files.readString(fixture));
    }

    @Test
    public void load_duplicateCandidate_preservesOriginalBeforeCommandsCanSave() throws Exception {
        Path fixture = TEST_DATA_FOLDER.resolve("JsonSerializableAddressBookTest/duplicatePersonAddressBook.json");
        assertRejectedFileBackedUpBeforeCommands(Files.readString(fixture));
    }

    @Test
    public void load_repeatedCorruption_keepsEveryBackup() throws Exception {
        Path path = temporaryFolder.resolve("candidates.json");
        CandidateDataInitializer first = new CandidateDataInitializer(createStorage(new JsonAddressBookStorage(path)));
        Files.writeString(path, "first invalid file");
        first.load();
        Path firstBackup = first.getRecoveryBackup().orElseThrow();
        Files.writeString(path, "second invalid file");
        CandidateDataInitializer second = new CandidateDataInitializer(createStorage(new JsonAddressBookStorage(path)));

        second.load();

        Path secondBackup = second.getRecoveryBackup().orElseThrow();
        assertNotEquals(firstBackup, secondBackup);
        assertEquals("first invalid file", Files.readString(firstBackup));
        assertEquals("second invalid file", Files.readString(secondBackup));
    }

    @Test
    public void load_backupFailure_abortsWithoutSavingOrChangingOriginal() throws Exception {
        Path path = temporaryFolder.resolve("candidates.json");
        Files.writeString(path, "invalid file");
        JsonAddressBookStorage failing = new JsonAddressBookStorage(path) {
            @Override
            public Path backupAddressBook() throws IOException {
                throw new IOException("Backup unavailable");
            }

            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) {
                throw new AssertionError("Must not save before backup succeeds");
            }
        };
        CandidateDataInitializer initializer = new CandidateDataInitializer(createStorage(failing));

        assertThrows(IOException.class, "Backup unavailable", initializer::load);

        assertEquals("invalid file", Files.readString(path));
        assertTrue(initializer.getRecoveryBackup().isEmpty());
        assertEquals(1, listFiles().size());
    }

    @Test
    public void load_replacementSaveFailure_keepsOriginalAndCompletedBackup() throws Exception {
        Path path = temporaryFolder.resolve("candidates.json");
        Files.writeString(path, "invalid file");
        JsonAddressBookStorage failing = new JsonAddressBookStorage(path) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                throw new IOException("Replacement unavailable");
            }
        };
        CandidateDataInitializer initializer = new CandidateDataInitializer(createStorage(failing));

        assertThrows(IOException.class, "Replacement unavailable", initializer::load);

        assertEquals("invalid file", Files.readString(path));
        assertEquals("invalid file", Files.readString(initializer.getRecoveryBackup().orElseThrow()));
        assertEquals(2, listFiles().size());
    }

    @Test
    public void load_missingFile_returnsSamplesWithoutBackup() throws Exception {
        Path path = temporaryFolder.resolve("missing.json");
        CandidateDataInitializer initializer = new CandidateDataInitializer(
                createStorage(new JsonAddressBookStorage(path)));

        assertEquals(SampleDataUtil.getSampleAddressBook(), initializer.load());
        assertTrue(initializer.getRecoveryBackup().isEmpty());
        assertFalse(Files.exists(path));
    }

    @Test
    public void load_validFile_preservesCandidatesWithoutBackup() throws Exception {
        Path path = temporaryFolder.resolve("candidates.json");
        JsonAddressBookStorage dataStorage = new JsonAddressBookStorage(path);
        dataStorage.saveAddressBook(getTypicalAddressBook());
        byte[] original = Files.readAllBytes(path);
        CandidateDataInitializer initializer = new CandidateDataInitializer(createStorage(dataStorage));

        assertEquals(getTypicalAddressBook(), initializer.load());
        assertTrue(initializer.getRecoveryBackup().isEmpty());
        assertArrayEquals(original, Files.readAllBytes(path));
    }

    /**
     * Verifies that recovery backs up rejected data before later commands save the replacement.
     */
    private void assertRejectedFileBackedUpBeforeCommands(String json) throws Exception {
        Path path = temporaryFolder.resolve("candidates.json");
        Files.writeString(path, json);
        byte[] original = Files.readAllBytes(path);
        JsonAddressBookStorage dataStorage = new JsonAddressBookStorage(path);
        StorageManager storage = createStorage(dataStorage);
        CandidateDataInitializer initializer = new CandidateDataInitializer(storage);

        ReadOnlyAddressBook replacement = initializer.load();

        assertEquals(new AddressBook(), replacement);
        Path backup = initializer.getRecoveryBackup().orElseThrow();
        assertArrayEquals(original, Files.readAllBytes(backup));
        assertEquals(replacement, dataStorage.readAddressBook().orElseThrow());
        Logic logic = new LogicManager(new ModelManager(replacement, new UserPrefs()), storage);
        for (String command : List.of("list", "find Alice", "help")) {
            logic.execute(command);
            assertArrayEquals(original, Files.readAllBytes(backup), command);
        }
        CandidateDataInitializer nextLaunch = new CandidateDataInitializer(storage);
        assertEquals(new AddressBook(), nextLaunch.load());
        assertTrue(nextLaunch.getRecoveryBackup().isEmpty());
    }

    private StorageManager createStorage(JsonAddressBookStorage dataStorage) {
        return new StorageManager(dataStorage, new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }

    private List<Path> listFiles() throws IOException {
        try (Stream<Path> files = Files.list(temporaryFolder)) {
            return files.toList();
        }
    }
}
