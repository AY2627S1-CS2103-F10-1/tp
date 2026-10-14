package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainAppTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void initModelManager_failedRecovery_printsActionableErrorBeforeUiOrLogicExists() throws Exception {
        Path dataFile = temporaryFolder.resolve("candidates.json");
        Files.writeString(dataFile, "invalid original");
        JsonAddressBookStorage failing = new JsonAddressBookStorage(dataFile) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook candidates) throws IOException {
                throw new IOException("Save failed");
            }
        };
        StorageManager storage = new StorageManager(failing,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        MainApp app = new MainApp();
        ByteArrayOutputStream capturedError = new ByteArrayOutputStream();
        PrintStream originalError = System.err;

        try (PrintStream redirectedError = new PrintStream(capturedError, true, StandardCharsets.UTF_8)) {
            System.setErr(redirectedError);
            assertThrows(IOException.class, "Save failed", () -> app.initModelManager(storage, new UserPrefs()));
        } finally {
            System.setErr(originalError);
        }

        String message = capturedError.toString(StandardCharsets.UTF_8);
        assertTrue(message.contains(dataFile.toString()));
        assertTrue(message.contains("Startup stopped"));
        assertTrue(message.contains("Check file and folder permissions"));
        assertTrue(message.contains("Backup retained at:"));
        assertEquals("invalid original", Files.readString(dataFile));
        assertNull(app.logic);
        assertNull(app.ui);
    }
}
