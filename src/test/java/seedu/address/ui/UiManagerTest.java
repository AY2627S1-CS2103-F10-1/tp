package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.control.Alert.AlertType;

public class UiManagerTest {
    @TempDir
    Path temporaryFolder;

    @Test
    public void showRecoveryWarning_completedBackup_explainsBackupAndRestorationPath() {
        Path dataFile = temporaryFolder.resolve("candidates.json");
        Path backup = temporaryFolder.resolve("HRvest-example-corrupt.json");
        CapturingUiManager ui = new CapturingUiManager(dataFile);

        ui.showRecoveryWarning(backup);

        assertEquals(AlertType.WARNING, ui.type);
        assertEquals("Candidate data recovery", ui.title);
        assertTrue(ui.content.contains(backup.toString()));
        assertTrue(ui.content.contains(dataFile.toAbsolutePath().toString()));
        assertTrue(ui.content.contains("An empty candidate list has been saved"));
        assertTrue(ui.content.contains("Close HRvest"));
        assertTrue(ui.content.contains("repair that copy"));
        assertTrue(ui.content.contains("Keep the backup until recovery is complete"));
    }

    /**
     * Captures recovery alerts without opening a native window or requiring command logic.
     */
    private static class CapturingUiManager extends UiManager {
        private AlertType type;
        private String title;
        private String content;

        CapturingUiManager(Path dataFile) {
            super(null, dataFile);
        }

        @Override
        void showAlertDialogAndWait(AlertType type, String title, String headerText, String contentText) {
            this.type = type;
            this.title = title;
            content = contentText;
        }
    }
}
