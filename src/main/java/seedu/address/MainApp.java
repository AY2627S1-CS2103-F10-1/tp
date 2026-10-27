package seedu.address;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.CandidateDataInitializer;
import seedu.address.storage.DataDirectoryLock;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V0.0.1";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = Paths.get("data", "HRvest.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    private final Path preferencesPath;
    private final Path candidatesPath;
    private Optional<Path> recoveryBackup = Optional.empty();
    private DataDirectoryLock dataDirectoryLock;

    /** Creates the application using its standard data and preference locations. */
    public MainApp() {
        this(USER_PREFS_FILE_PATH, ADDRESS_BOOK_FILE_PATH);
    }

    /** Creates an application with isolated storage locations for lifecycle tests. */
    MainApp(Path preferencesPath, Path candidatesPath) {
        this.preferencesPath = requireNonNull(preferencesPath);
        this.candidatesPath = requireNonNull(candidatesPath);
    }

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing HRvest ]===========================");
        super.init();
        try {
            dataDirectoryLock = DataDirectoryLock.acquire(candidatesPath.toAbsolutePath().getParent());
            JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(preferencesPath);
            UserPrefs userPrefs = initPrefs(userPrefsStorage);
            JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(candidatesPath);
            storage = new StorageManager(addressBookStorage, userPrefsStorage);
            model = initModelManager(storage, userPrefs);
            logic = new LogicManager(model, storage);
            ui = new UiManager(logic, storage.getAddressBookFilePath());
        } catch (IOException | RuntimeException e) {
            System.err.println("HRvest could not start: " + e.getMessage()
                    + " Check folder permissions if no other instance is running.");
            // Resource cleanup retains the startup error if closing also fails.
            try (DataDirectoryLock lock = dataDirectoryLock) {
                throw e;
            } finally {
                dataDirectoryLock = null;
            }
        }
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}. <br>
     * The data from the sample address book will be used instead if {@code storage}'s address book is not found,
     * or an empty address book will be saved after rejected data has been safely backed up.
     *
     * @throws IOException if preserving or replacing rejected data fails.
     */
    Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) throws IOException {
        logger.info("Using data file : " + storage.getAddressBookFilePath());

        CandidateDataInitializer initializer = new CandidateDataInitializer(storage);
        try {
            Model initializedModel = new ModelManager(initializer.load(), userPrefs);
            recoveryBackup = initializer.getRecoveryBackup();
            return initializedModel;
        } catch (IOException e) {
            String message = "HRvest could not safely recover " + storage.getAddressBookFilePath()
                    + ". Startup stopped; no commands can overwrite the original file."
                    + " Check file and folder permissions, repair the data file, and restart."
                    + initializer.getRecoveryBackup().map(path -> " Backup retained at: " + path).orElse("");
            logger.severe(message);
            System.err.println(message);
            throw e;
        }
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting HRvest " + MainApp.VERSION);
        ui.start(primaryStage);
        recoveryBackup.ifPresent(ui::showRecoveryWarning);
    }

    @Override
    public void stop() throws IOException {
        logger.info("============================ [ Stopping HRvest ] =============================");
        try (DataDirectoryLock lock = dataDirectoryLock) {
            try {
                if (storage != null && model != null) {
                    storage.saveUserPrefs(model.getUserPrefs());
                }
            } catch (IOException e) {
                logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
            }
        } finally {
            dataDirectoryLock = null;
        }
    }
}
