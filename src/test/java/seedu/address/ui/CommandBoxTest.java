package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.testutil.JavaFxTestUtil;

/**
 * Native JavaFX tests for arrow-key history navigation in {@link CommandBox},
 * including the draft-preservation regression described in bug B02.
 */
@EnabledOnOs(OS.WINDOWS)
public class CommandBoxTest {
    private static final int TIMEOUT_SECONDS = 10;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        JavaFxTestUtil.initializeToolkit();
    }

    @Test
    public void arrowNavigation_editAfterRejection_preservesCorrectedDraft() throws Exception {
        runOnFxThread(() -> {
            CommandHistory history = new CommandHistory();
            CommandBox box = new CommandBox(
                    text -> {
                        if ("bad".equals(text)) {
                            throw new ParseException("rejected");
                        }
                        return new CommandResult("ok");
                    },
                    history);
            TextField field = showField(box);

            runEnter(field, "list");
            runEnter(field, "bad");
            setText(field, "edit 1 p/999");

            press(field, KeyCode.UP);
            assertEquals("list", field.getText());
            press(field, KeyCode.DOWN);
            assertEquals("bad", field.getText());
            press(field, KeyCode.DOWN);
            assertEquals("edit 1 p/999", field.getText());
        });
    }

    @Test
    public void arrowNavigation_unchangedRejection_upShowsPreviousCommand() throws Exception {
        runOnFxThread(() -> {
            CommandHistory history = new CommandHistory();
            CommandBox box = new CommandBox(
                    text -> {
                        if ("bad".equals(text)) {
                            throw new ParseException("rejected");
                        }
                        return new CommandResult("ok");
                    },
                    history);
            TextField field = showField(box);

            runEnter(field, "list");
            runEnter(field, "bad");

            press(field, KeyCode.UP);
            assertEquals("list", field.getText());
        });
    }

    private static TextField showField(CommandBox box) {
        TextField field = (TextField) box.getRoot().lookup("#commandTextField");
        Stage stage = new Stage();
        stage.setScene(new Scene(box.getRoot()));
        stage.show();
        return field;
    }

    private static void setText(TextField field, String text) {
        field.setText(text);
    }

    private static void runEnter(TextField field, String text) {
        field.setText(text);
        Event.fireEvent(field, new javafx.event.ActionEvent());
    }

    private static void press(TextField field, KeyCode code) {
        Event.fireEvent(field, new KeyEvent(KeyEvent.KEY_PRESSED,
                "", "", code, false, false, false, false));
    }

    private static void runOnFxThread(Runnable task) throws Exception {
        FutureTask<Void> future = new FutureTask<>(task, null);
        Platform.runLater(future);
        future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
