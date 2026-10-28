package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.model.role.Role;
import seedu.address.testutil.JavaFxTestUtil;
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
        JavaFxTestUtil.initializeToolkit();
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

    @Test
    public void constructor_multipleRoles_displaysSortedDistinctBadges() throws Exception {
        runOnFxThread(() -> {
            Role developer = new Role("00000000-0000-0000-0000-000000000001", "Developer");
            Role designer = new Role("00000000-0000-0000-0000-000000000002", "Designer");
            Region card = new PersonCard(new PersonBuilder().withRoles(developer, designer, Role.NO_ROLE)
                    .withTags("backend").build(), 1).getRoot();
            layoutCard(card);

            FlowPane roles = (FlowPane) card.lookup("#roles");
            assertEquals(2, roles.getChildren().size());
            Label firstRole = (Label) roles.getChildren().getFirst();
            assertEquals("Designer", firstRole.getText());
            assertEquals("Developer", ((Label) roles.getChildren().getLast()).getText());
            Label tag = (Label) ((FlowPane) card.lookup("#tags")).getChildren().getFirst();
            assertNotEquals(tag.getBackground(), firstRole.getBackground());
            assertEquals(tag.getPadding(), firstRole.getPadding());
        });
    }

    @Test
    public void constructor_noNamedRole_displaysNoRoleBadge() throws Exception {
        runOnFxThread(() -> {
            Region card = new PersonCard(new PersonBuilder().build(), 1).getRoot();
            layoutCard(card);
            FlowPane roles = (FlowPane) card.lookup("#roles");
            assertEquals(1, roles.getChildren().size());
            Label badge = (Label) roles.getChildren().getFirst();
            assertEquals("No role", badge.getText());
            assertEquals(Color.web("#b23b48"), badge.getBackground().getFills().getFirst().getFill());
        });
    }

    @Test
    public void constructor_longRole_wrapsWithinOrdinaryAndExpandedCards() throws Exception {
        runOnFxThread(() -> {
            Role role = new Role("00000000-0000-0000-0000-000000000001",
                    "Long role name ".repeat(20).strip());
            Person candidate = new PersonBuilder().withRoles(role).build();
            for (boolean expanded : new boolean[] {false, true}) {
                Region card = new PersonCard(candidate, 1, expanded).getRoot();
                layoutCard(card);
                FlowPane roles = (FlowPane) card.lookup("#roles");
                Label badge = (Label) roles.getChildren().getFirst();
                assertEquals(role.getName(), badge.getText());
                assertTrue(badge.isWrapText());
                assertTrue(badge.getWidth() <= roles.getWidth());
                assertTrue(badge.getHeight() > badge.getFont().getSize() * 2);
            }
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
