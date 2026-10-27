package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.stage.Stage;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.DataDirectoryLock;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;

public class MainAppTest {
    @TempDir
    Path temporaryFolder;

    @Test
    public void init_secondInstance_doesNotTouchPreferencesOrCandidateData() throws Exception {
        Path preferences = temporaryFolder.resolve("preferences.json");
        Path candidates = temporaryFolder.resolve("data/HRvest.json");
        MainApp first = new MainApp(preferences, candidates);
        first.init();
        try {
            first.logic.execute("add n/Protected Candidate p/123 e/protected@example.com a/Block 1");
            String beforeCandidates = Files.readString(candidates);
            String beforePreferences = Files.readString(preferences);
            StartupErrorApp second = new StartupErrorApp(preferences, candidates);

            second.init();
            second.start(null);
            assertTrue(second.error.contains("Another HRvest instance is already using"));
            assertFalse(second.error.contains("Check folder permissions"));
            second.stop();

            assertEquals(beforeCandidates, Files.readString(candidates));
            assertEquals(beforePreferences, Files.readString(preferences));
            assertNull(second.storage);
            assertNull(second.model);
            assertNull(second.logic);
            assertNull(second.ui);
            first.logic.execute("list");
            assertTrue(Files.readString(candidates).contains("protected@example.com"));
        } finally {
            first.stop();
        }
        MainApp restarted = new MainApp(preferences, candidates);
        try {
            restarted.init();
            assertTrue(restarted.model.getAddressBook().getPersonList().stream()
                    .anyMatch(person -> person.getEmail().value.equals("protected@example.com")));
        } finally {
            restarted.stop();
        }
    }

    @Test
    public void init_failedInitialization_releasesDirectoryLock() throws Exception {
        Path candidates = temporaryFolder.resolve("data/HRvest.json");
        StartupErrorApp failing = new StartupErrorApp(temporaryFolder.resolve("preferences.json"), candidates) {
            @Override
            protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
                throw new IllegalStateException("Initialization failed");
            }
        };
        failing.init();
        failing.start(null);
        assertEquals("HRvest could not start: Initialization failed", failing.error);

        try (DataDirectoryLock lock = DataDirectoryLock.acquire(candidates.getParent())) {
            assertTrue(Files.exists(candidates.getParent().resolve(DataDirectoryLock.LOCK_FILE_NAME)));
        }
        failing.stop();
    }

    @Test
    public void stop_preferenceSaveFailure_stillReleasesDirectoryLock() throws Exception {
        Path preferences = temporaryFolder.resolve("preferences.json");
        Path candidates = temporaryFolder.resolve("data/HRvest.json");
        MainApp app = new MainApp(preferences, candidates);
        app.init();
        Files.delete(preferences);
        Files.createDirectory(preferences);

        app.stop();

        try (DataDirectoryLock lock = DataDirectoryLock.acquire(candidates.getParent())) {
            assertTrue(Files.isDirectory(preferences));
        }
    }

    @Test
    public void init_modelLoadingFails_releasesLockAndAllowsSafeStop() throws Exception {
        Path candidates = temporaryFolder.resolve("data/HRvest.json");
        StartupErrorApp failing = new StartupErrorApp(temporaryFolder.resolve("preferences.json"), candidates) {
            @Override
            Model initModelManager(Storage storage, ReadOnlyUserPrefs preferences) throws IOException {
                throw new IOException("Data loading failed");
            }
        };

        failing.init();
        failing.start(null);
        assertEquals("HRvest could not start: Data loading failed", failing.error);
        failing.stop();

        assertNull(failing.model);
        assertNull(failing.logic);
        try (DataDirectoryLock lock = DataDirectoryLock.acquire(candidates.getParent())) {
            assertTrue(Files.isRegularFile(candidates.getParent().resolve(DataDirectoryLock.LOCK_FILE_NAME)));
        }
    }

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
    public void initModelManager_failedRecovery_throwsActionableErrorBeforeUiOrLogicExists() throws Exception {
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
        IOException failure = org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () ->
                 app.initModelManager(storage, new UserPrefs()));

        String message = failure.getMessage();
        assertEquals("Save failed", failure.getCause().getMessage());
        assertTrue(message.contains(dataFile.toString()));
        assertTrue(message.contains("Startup stopped"));
        assertTrue(message.contains("Check file and folder permissions"));
        assertTrue(message.contains("Backup retained at:"));
        assertFalse(message.contains("invalid original"));
        assertEquals("invalid original", Files.readString(dataFile));
        assertNull(app.logic);
        assertNull(app.ui);
    }

    private static class StartupErrorApp extends MainApp {
        private String error;

        StartupErrorApp(Path preferences, Path candidates) {
            super(preferences, candidates);
        }

        @Override
        void showStartupError(String message) {
            error = message;
        }
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
