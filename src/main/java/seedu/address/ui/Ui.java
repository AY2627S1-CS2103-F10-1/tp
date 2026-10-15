package seedu.address.ui;

import java.nio.file.Path;

import javafx.stage.Stage;

/**
 * API of the UI component
 */
public interface Ui {

    /** Starts the UI (and the App).  */
    void start(Stage primaryStage);

    /**
     * Shows the backup location and restoration instructions after startup recovery.
     */
    void showRecoveryWarning(Path backup);

}
