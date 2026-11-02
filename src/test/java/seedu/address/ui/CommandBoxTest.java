package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.application.Platform;
import javafx.event.ActionEvent;
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
 * Native JavaFX tests for arrow-key history navigation in {@link CommandBox}.
 * Covers the draft-preservation regressions raised in the beta audit (B02) and in
 * review feedback L112 and L116: a corrected rejected draft, an unchanged rejection,
 * a repeated restore cycle, and a no-op Down at the newest entry.
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
            Fixture fixture = Fixture.withRejecting("bad");
            try {
                runEnter(fixture.field, "list");
                runEnter(fixture.field, "bad");
                setText(fixture.field, "edit 1 p/999");

                press(fixture.field, KeyCode.UP);
                assertEquals("list", fixture.field.getText());
                press(fixture.field, KeyCode.DOWN);
                assertEquals("bad", fixture.field.getText());
                press(fixture.field, KeyCode.DOWN);
                assertEquals("edit 1 p/999", fixture.field.getText());
            } finally {
                fixture.close();
            }
        });
    }

    @Test
    public void arrowNavigation_unchangedRejection_upShowsPreviousCommand() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = Fixture.withRejecting("bad");
            try {
                runEnter(fixture.field, "list");
                runEnter(fixture.field, "bad");

                press(fixture.field, KeyCode.UP);
                assertEquals("list", fixture.field.getText());
            } finally {
                fixture.close();
            }
        });
    }

    @Test
    public void arrowNavigation_restoredDraftThenAnotherCycle_stillPreservesDraft() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = Fixture.withRejecting("bad");
            try {
                runEnter(fixture.field, "list");
                runEnter(fixture.field, "bad");
                setText(fixture.field, "edit 1 p/999");

                press(fixture.field, KeyCode.UP);
                press(fixture.field, KeyCode.DOWN);
                press(fixture.field, KeyCode.DOWN);
                assertEquals("edit 1 p/999", fixture.field.getText());

                press(fixture.field, KeyCode.UP);
                assertEquals("bad", fixture.field.getText());
                press(fixture.field, KeyCode.DOWN);
                assertEquals("edit 1 p/999", fixture.field.getText());
            } finally {
                fixture.close();
            }
        });
    }

    @Test
    public void arrowNavigation_downAtNewest_doesNotLockEarlierDraft() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = Fixture.alwaysSucceeding();
            try {
                runEnter(fixture.field, "list");
                setText(fixture.field, "draft A");

                press(fixture.field, KeyCode.DOWN);
                setText(fixture.field, "draft B");

                press(fixture.field, KeyCode.UP);
                assertEquals("list", fixture.field.getText());
                press(fixture.field, KeyCode.DOWN);
                assertEquals("draft B", fixture.field.getText());
            } finally {
                fixture.close();
            }
        });
    }

    private static void setText(TextField field, String text) {
        field.setText(text);
    }

    private static void runEnter(TextField field, String text) {
        field.setText(text);
        Event.fireEvent(field, new ActionEvent());
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

    /** Owns the Stage so each test can close it in a finally block. */
    private static final class Fixture {
        private final Stage stage;
        private final TextField field;

        private Fixture(Stage stage, TextField field) {
            this.stage = stage;
            this.field = field;
        }

        static Fixture withRejecting(String rejected) {
            return create(text -> {
                if (rejected.equals(text)) {
                    throw new ParseException("rejected");
                }
                return new CommandResult("ok");
            });
        }

        static Fixture alwaysSucceeding() {
            return create(text -> new CommandResult("ok"));
        }

        private static Fixture create(CommandBox.CommandExecutor executor) {
            CommandBox box = new CommandBox(executor, new CommandHistory());
            TextField field = (TextField) box.getRoot().lookup("#commandTextField");
            Stage stage = new Stage();
            stage.setScene(new Scene(box.getRoot()));
            stage.show();
            return new Fixture(stage, field);
        }

        void close() {
            stage.hide();
        }
    }
}
