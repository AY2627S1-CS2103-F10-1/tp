package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxTestUtil.runOnFxThread;

import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxTestUtil;
import seedu.address.testutil.PersonBuilder;

/** Verifies feedback visibility in the native Windows JavaFX layout without a headless-toolkit dependency. */
@EnabledOnOs(OS.WINDOWS)
public class ResultDisplayTest {
    private static final double LAYOUT_TOLERANCE = 1;
    private static final int WINDOW_WIDTH = 1100;
    private static final int WINDOW_HEIGHT = 650;

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        JavaFxTestUtil.initializeToolkit();
    }

    @Test
    public void listRole_ordinaryCatalog_showsEveryNameWithoutScrolling() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.createRole("Designer");
            model.createRole("Developer");
            model.createRole("QA");
            MainWindow window = createWindow(model);
            try {
                executeCommand(window, "list_role");
                TextArea feedback = feedback(window);
                assertEquals("Roles:\nDesigner\nDeveloper\nNo role\nQA", feedback.getText());
                assertEntireTextVisible(feedback);
                assertFalse(verticalScrollBar(feedback).isVisible());
                assertTrue(candidateArea(window).getHeight() >= 150);
            } finally {
                window.getPrimaryStage().hide();
            }
        });
    }

    @Test
    public void setFeedbackToUser_largeCatalog_boundsHeightAndKeepsLastEntryAccessible() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(new ModelManager());
            try {
                TextArea feedback = feedback(window);
                String catalog = IntStream.range(0, 1000).mapToObj(index -> "Role " + index)
                        .collect(Collectors.joining("\n", "Roles:\n", ""));
                feedback.setText(catalog);
                layout(window.getPrimaryStage().getScene().getRoot());

                Region resultArea = (Region) feedback.getParent().getParent();
                assertTrue(resultArea.getHeight() <= 200 + LAYOUT_TOLERANCE);
                assertTrue(candidateArea(window).getHeight() >= 150);
                assertTrue(verticalScrollBar(feedback).isVisible());
                ScrollPane scrollPane = (ScrollPane) feedback.lookup(".scroll-pane");
                scrollPane.setVvalue(scrollPane.getVmax());
                layout(window.getPrimaryStage().getScene().getRoot());

                assertEquals(catalog, feedback.getText());
                assertTrue(feedback.getScrollTop() > 0);
                Bounds textBounds = text(feedback).localToScene(text(feedback).getBoundsInLocal());
                Region viewport = (Region) scrollPane.lookup(".viewport");
                Bounds viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());
                assertTrue(textBounds.getMaxY() <= viewportBounds.getMaxY() + LAYOUT_TOLERANCE);
            } finally {
                window.getPrimaryStage().hide();
            }
        });
    }

    @Test
    public void setFeedbackToUser_shorterCommand_shrinksAndStartsAtFirstLine() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                TextArea feedback = feedback(window);
                feedback.setText("Long feedback\n".repeat(100));
                layout(window.getPrimaryStage().getScene().getRoot());
                double expandedHeight = feedback.getHeight();
                feedback.setScrollTop(Double.MAX_VALUE);

                executeCommand(window, "list");

                assertEquals("Listed all 1 candidate(s).", feedback.getText());
                assertTrue(feedback.getHeight() < expandedHeight);
                assertEquals(0, feedback.getScrollTop(), LAYOUT_TOLERANCE);
                assertEntireTextVisible(feedback);
            } finally {
                window.getPrimaryStage().hide();
            }
        });
    }

    @Test
    public void layout_widthAndFontChanges_recomputesWrappedHeightWithinWindowLimit() throws Exception {
        runOnFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            Region candidateArea = new Region();
            VBox root = new VBox(display.getRoot(), candidateArea);
            VBox.setVgrow(candidateArea, Priority.ALWAYS);
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            root.resize(900, 600);
            display.setFeedbackToUser("Roles:\n" + "Senior Software Engineer ".repeat(8));
            layout(root);
            double wideHeight = display.getRoot().getHeight();

            root.resize(450, 600);
            layout(root);
            double narrowHeight = display.getRoot().getHeight();
            assertTrue(narrowHeight > wideHeight, "Feedback height: wide=" + wideHeight + ", narrow=" + narrowHeight
                    + ", root width=" + root.getWidth() + ", display width=" + display.getRoot().getWidth());

            TextArea feedback = (TextArea) display.getRoot().lookup("#resultDisplay");
            feedback.setFont(Font.font(feedback.getFont().getFamily(), feedback.getFont().getSize() * 1.5));
            layout(root);
            assertTrue(display.getRoot().getHeight() >= narrowHeight);
            assertTrue(display.getRoot().getHeight() <= scene.getHeight() * 0.3 + LAYOUT_TOLERANCE);
            assertTrue(candidateArea.getHeight() >= 400);
        });
    }

    private MainWindow createWindow(ModelManager model) {
        model.addPerson(new PersonBuilder().build());
        // A saved size keeps the fixture independent of the monitor's scaled visual bounds.
        model.setGuiSettings(new GuiSettings(WINDOW_WIDTH, WINDOW_HEIGHT, 0, 0));
        Path dataFile = temporaryFolder.resolve("HRvest.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage), dataFile);
        window.fillInnerParts();
        window.show();
        layout(window.getPrimaryStage().getScene().getRoot());
        return window;
    }

    private static void executeCommand(MainWindow window, String command) {
        TextField commandBox = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
        commandBox.setText(command);
        commandBox.getOnAction().handle(new ActionEvent());
        layout(window.getPrimaryStage().getScene().getRoot());
    }

    private static TextArea feedback(MainWindow window) {
        return (TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay");
    }

    private static Region candidateArea(MainWindow window) {
        return (Region) window.getPrimaryStage().getScene().lookup("#personList");
    }

    private static Text text(TextArea feedback) {
        return (Text) feedback.lookup(".text");
    }

    private static ScrollBar verticalScrollBar(TextArea feedback) {
        return (ScrollBar) feedback.lookup(".scroll-bar:vertical");
    }

    private static void assertEntireTextVisible(TextArea feedback) {
        Bounds textBounds = text(feedback).localToScene(text(feedback).getBoundsInLocal());
        Region viewport = (Region) feedback.lookup(".viewport");
        Bounds viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());
        assertTrue(textBounds.getMinY() >= viewportBounds.getMinY() - LAYOUT_TOLERANCE);
        assertTrue(textBounds.getMaxY() <= viewportBounds.getMaxY() + LAYOUT_TOLERANCE);
    }

    private static void layout(Parent root) {
        // A second layout settles a preferred-height change caused by the first layout assigning the text width.
        root.applyCss();
        root.layout();
        root.requestLayout();
        root.layout();
        // TextAreaSkin schedules its fit-to-height update after layout; process it before the final layout.
        Object layoutLoop = new Object();
        Platform.runLater(() -> Platform.exitNestedEventLoop(layoutLoop, null));
        Platform.enterNestedEventLoop(layoutLoop);
        root.requestLayout();
        root.layout();
    }
}
