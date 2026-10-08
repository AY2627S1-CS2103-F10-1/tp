package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
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
