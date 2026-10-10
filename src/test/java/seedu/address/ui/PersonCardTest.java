package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests real candidate cards using the native Windows JavaFX toolkit.
 * Linux CI has no display server, so these tests run on Windows instead of requiring a headless dependency.
 */
@EnabledOnOs(OS.WINDOWS)
public class PersonCardTest {
    private static final int CARD_WIDTH = 320;
    private static final int CARD_HEIGHT = 300;
    private static final int TIMEOUT_SECONDS = 10;
    private static final String NOTE_TEXT = "Follow up about interview feedback";

    @TempDir
    Path temporaryFolder;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        CompletableFuture<Void> ready = new CompletableFuture<>();
        Platform.startup(() -> ready.complete(null));
        ready.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @AfterAll
    public static void tearDownToolkit() {
        Platform.exit();
    }

    @Test
    public void noteCommand_addAndReplace_refreshesCollapsedAndExpandedCards() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
            Logic logic = new LogicManager(model, new StorageManager(
                    new JsonAddressBookStorage(temporaryFolder.resolve("candidates.json")),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
            assertDoesNotThrow(() -> logic.execute("find Benson"));
            Region panel = new PersonListPanel(logic.getFilteredPersonList(),
                    logic.isExpandedViewProperty()).getRoot();
            layoutCard(panel);

            assertFalse(panel.lookup("#noteIcon").isVisible());
            assertDoesNotThrow(() -> logic.execute("note 1 no/" + NOTE_TEXT));
            assertFalse(logic.isExpandedViewProperty().get());
            assertDoesNotThrow(() -> logic.execute("find Benson"));
            panel.applyCss();
            panel.layout();
            assertTrue(panel.lookup("#noteIcon").isVisible());
            assertFalse(panel.lookup("#note").isVisible());

            assertDoesNotThrow(() -> logic.execute("expand 1"));
            panel.applyCss();
            panel.layout();
            assertTrue(panel.lookup("#noteIcon").isVisible());
            assertTrue(panel.lookup("#note").isVisible());
            assertEquals("Note: " + NOTE_TEXT, ((Label) panel.lookup("#note")).getText());

            assertDoesNotThrow(() -> logic.execute("note 1 no/Replacement feedback"));
            assertFalse(logic.isExpandedViewProperty().get());
            assertDoesNotThrow(() -> logic.execute("find Benson"));
            panel.applyCss();
            panel.layout();
            assertTrue(panel.lookup("#noteIcon").isVisible());
            assertFalse(panel.lookup("#note").isVisible());
            assertDoesNotThrow(() -> logic.execute("expand 1"));
            panel.applyCss();
            panel.layout();
            assertEquals("Note: Replacement feedback", ((Label) panel.lookup("#note")).getText());
        });
    }

    @Test
    public void constructor_withoutNote_omitsIndicatorSpace() throws Exception {
        runOnFxThread(() -> {
            Region withoutNote = new PersonCard(new PersonBuilder().build(), 1).getRoot();
            Region withNote = new PersonCard(new PersonBuilder().withNote(NOTE_TEXT).build(), 1).getRoot();
            layoutCard(withoutNote);
            layoutCard(withNote);

            Label absentIcon = (Label) withoutNote.lookup("#noteIcon");
            Label presentIcon = (Label) withNote.lookup("#noteIcon");
            assertFalse(absentIcon.isVisible());
            assertFalse(absentIcon.isManaged());
            HBox absentNameRow = (HBox) absentIcon.getParent();
            HBox presentNameRow = (HBox) presentIcon.getParent();
            assertTrue(absentNameRow.prefWidth(-1) < presentNameRow.prefWidth(-1));
        });
    }

    @Test
    public void constructor_withNote_showsAccessibleIndicator() throws Exception {
        runOnFxThread(() -> {
            Region card = new PersonCard(new PersonBuilder().withNote(NOTE_TEXT).build(), 1).getRoot();
            layoutCard(card);

            Label icon = (Label) card.lookup("#noteIcon");
            assertTrue(icon.isVisible());
            assertTrue(icon.isManaged());
            assertEquals("Note available", icon.getTooltip().getText());
            assertEquals("Candidate has a note", icon.getAccessibleText());
            assertTrue(card.lookupAll(".label").stream()
                    .noneMatch(node -> ((Label) node).getText().contains(NOTE_TEXT)));
        });
    }

    @Test
    public void constructor_expandedWithNote_showsNoteAndIndicator() throws Exception {
        runOnFxThread(() -> {
            Region card = new PersonCard(new PersonBuilder().withNote(NOTE_TEXT).build(), 1, true).getRoot();
            layoutCard(card);

            Label icon = (Label) card.lookup("#noteIcon");
            Label note = (Label) card.lookup("#note");
            assertTrue(icon.isVisible());
            assertTrue(icon.isManaged());
            assertTrue(note.isVisible());
            assertTrue(note.isManaged());
            assertEquals("Note: " + NOTE_TEXT, note.getText());
        });
    }

    @Test
    public void constructor_expandedWithoutNote_hidesIndicator() throws Exception {
        runOnFxThread(() -> {
            Region card = new PersonCard(new PersonBuilder().build(), 1, true).getRoot();
            layoutCard(card);

            Label icon = (Label) card.lookup("#noteIcon");
            Label note = (Label) card.lookup("#note");
            assertFalse(icon.isVisible());
            assertFalse(icon.isManaged());
            assertTrue(note.isVisible());
            assertTrue(note.isManaged());
            assertEquals("No note recorded.", note.getText());
        });
    }

    @Test
    public void constructor_expandedLongNote_wrapsTextAndPreservesIndicator() throws Exception {
        runOnFxThread(() -> {
            String noteText = "Follow up about technical interview feedback. ".repeat(10).strip();
            Person candidate = new PersonBuilder().withName("Long Candidate Name ".repeat(20))
                    .withNote(noteText).build();
            Region card = new PersonCard(candidate, 1, true).getRoot();
            layoutCard(card);
            // Expanded list cells use their preferred height so long records can scroll.
            card.resize(CARD_WIDTH, card.prefHeight(CARD_WIDTH));
            card.layout();

            Label note = (Label) card.lookup("#note");
            Label icon = (Label) card.lookup("#noteIcon");
            Bounds noteBounds = note.localToScene(note.getBoundsInLocal());
            Bounds iconBounds = icon.localToScene(icon.getBoundsInLocal());
            assertEquals("Note: " + noteText, note.getText());
            assertTrue(note.isWrapText());
            assertTrue(note.getHeight() > ((Label) card.lookup("#phone")).getHeight(),
                    "Long notes occupy multiple lines: " + note.getHeight());
            assertTrue(note.getHeight() >= note.prefHeight(note.getWidth()),
                    "The entire wrapped note fits at its preferred height");
            assertEquals(note.getText(), ((Text) note.lookup(".text")).getText());
            assertTrue(noteBounds.getMinX() >= 0);
            assertTrue(noteBounds.getMaxX() <= CARD_WIDTH);
            assertTrue(icon.isVisible());
            assertTrue(iconBounds.getMinX() >= 0);
            assertTrue(iconBounds.getMaxX() <= CARD_WIDTH);
        });
    }

    @Test
    public void constructor_longName_preservesIndicatorAndStatus() throws Exception {
        runOnFxThread(() -> {
            Person candidate = new PersonBuilder().withName("Long Candidate Name ".repeat(20))
                    .withNote(NOTE_TEXT).build();
            Person rejectedCandidate = new Person(candidate.getName(), candidate.getPhone(), candidate.getEmail(),
                    candidate.getAddress(), candidate.getTags(), Status.REJECTED, candidate.getNote());
            Region card = new PersonCard(rejectedCandidate, 1).getRoot();
            layoutCard(card);

            Label name = (Label) card.lookup("#name");
            Label icon = (Label) card.lookup("#noteIcon");
            Bounds iconBounds = icon.localToScene(icon.getBoundsInLocal());
            assertTrue(name.getWidth() < name.prefWidth(-1));
            assertTrue(icon.getWidth() >= icon.prefWidth(-1));
            assertTrue(iconBounds.getMinX() >= 0);
            assertTrue(iconBounds.getMaxX() <= CARD_WIDTH);
            assertEquals(Status.REJECTED.toString(), ((Label) card.lookup("#status")).getText());
        });
    }

    /**
     * Applies the real card stylesheet and lays out the card in a narrow scene without opening a window.
     */
    private static void layoutCard(Region card) {
        Scene scene = new Scene(card, CARD_WIDTH, CARD_HEIGHT);
        scene.getStylesheets().add(PersonCard.class.getResource("/view/DarkTheme.css").toExternalForm());
        card.resize(CARD_WIDTH, CARD_HEIGHT);
        card.applyCss();
        card.layout();
    }

    /**
     * Runs assertions on the JavaFX application thread and propagates failures to the test thread.
     */
    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            assertions.run();
            return null;
        });
        Platform.runLater(task);
        task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
