package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import seedu.address.testutil.JavaFxTestUtil;

/** Tests native startup alerts on Windows, where CI provides a desktop. */
@EnabledOnOs(OS.WINDOWS)
public class MainAppDialogTest {
    @BeforeAll
    public static void setUpToolkit() throws Exception {
        JavaFxTestUtil.initializeToolkit();
    }

    @Test
    public void showStartupError_dismissedAlert_exitsAfterShowingMessage() throws Exception {
        runOnFxThread(() -> {
            AtomicBoolean hasExited = new AtomicBoolean();
            AtomicBoolean hasShown = new AtomicBoolean();
            MainApp app = new MainApp() {
                @Override
                void showStartupError(String message, Consumer<Alert> display, Runnable exit) {
                    super.showStartupError(message, alert -> {
                        assertEquals(Alert.AlertType.ERROR, alert.getAlertType());
                        assertEquals("HRvest could not start", alert.getTitle());
                        assertEquals("HRvest could not start", alert.getHeaderText());
                        assertEquals("Close the first instance", alert.getContentText());
                        assertFalse(hasExited.get());
                        alert.setOnShown(event -> {
                            hasShown.set(true);
                            Platform.runLater(() -> alert.setResult(ButtonType.OK));
                        });
                        display.accept(alert);
                        assertEquals(ButtonType.OK, alert.getResult());
                    }, () -> hasExited.set(true));
                }
            };
            app.showStartupError("Close the first instance");
            assertTrue(hasShown.get());
            assertTrue(hasExited.get());
        });
    }

    @Test
    public void showStartupError_displayFails_stillRequestsExit() throws Exception {
        runOnFxThread(() -> {
            AtomicBoolean hasExited = new AtomicBoolean();
            assertThrows(IllegalStateException.class, "Display failed", () ->
                    new MainApp().showStartupError("Startup failed", alert -> {
                        throw new IllegalStateException("Display failed");
                    }, () -> hasExited.set(true)));
            assertTrue(hasExited.get());
        });
    }

    private void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
