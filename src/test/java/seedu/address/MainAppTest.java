package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.stage.Stage;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;

public class MainAppTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void initModelManager_successfulRecovery_notifiesUiAfterStartingWithSavedEmptyData() throws Exception {
        Path dataFile = temporaryFolder.resolve("candidates.json");
        Files.writeString(dataFile, "invalid original");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        MainApp app = new MainApp();

        Model model = app.initModelManager(storage, new UserPrefs());

        assertEquals(new AddressBook(), model.getAddressBook());
        assertEquals(new AddressBook(), storage.readAddressBook().orElseThrow());
        RecoveryUiStub ui = new RecoveryUiStub();
        app.ui = ui;
        app.start(null);
        assertTrue(ui.hasStarted);
        assertTrue(ui.backup != null);
        assertEquals("invalid original", Files.readString(ui.backup));
    }

    @Test
    public void initModelManager_validEmptyFile_startsWithoutRecoveryWarning() throws Exception {
        Path dataFile = temporaryFolder.resolve("candidates.json");
        JsonAddressBookStorage dataStorage = new JsonAddressBookStorage(dataFile);
        dataStorage.saveAddressBook(new AddressBook());
        MainApp app = new MainApp();
        StorageManager storage = new StorageManager(dataStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));

        assertEquals(List.of(), app.initModelManager(storage, new UserPrefs()).getAddressBook().getPersonList());
        RecoveryUiStub ui = new RecoveryUiStub();
        app.ui = ui;
        app.start(null);

        assertTrue(ui.hasStarted);
        assertNull(ui.backup);
    }

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
        assertFalse(message.contains("invalid original"));
        assertEquals("invalid original", Files.readString(dataFile));
        assertNull(app.logic);
        assertNull(app.ui);
    }

    /**
     * Records startup recovery notifications without requiring a native window.
     */
    private static class RecoveryUiStub implements Ui {
        private boolean hasStarted;
        private Path backup;

        @Override
        public void start(Stage primaryStage) {
            hasStarted = true;
        }

        @Override
        public void showRecoveryWarning(Path backup) {
            assertTrue(hasStarted);
            this.backup = backup;
        }
    }
}
