package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ExpandCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Runs expanded-card regression checks in an isolated native JavaFX process. */
public class ExpandUiTestApp {
    /** Starts the JavaFX checks and exits with a non-zero status if they fail. */
    public static void main(String[] args) {
        Platform.startup(() -> {
            try {
                checkExpandedCard();
            } catch (Exception | AssertionError e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    /** Checks full text, wrapping, and view restoration after JavaFX has laid out the scene. */
    private static void checkExpandedCard() throws Exception {
        ModelManager model = new ModelManager();
        String noteText = "Feedback\n" + "x".repeat(480) + "\n中文 😀";
        String tagText = "tag" + "x".repeat(250);
        Person candidate = new PersonBuilder().withName("Example Candidate " + "Long Name ".repeat(8))
                .withAddress("123 Example Road " + "Example District ".repeat(8))
                .withTags(tagText).withNote(noteText).build();
        model.addPerson(candidate);
        PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList(), model.expandedViewProperty());
        Scene scene = new Scene(panel.getRoot(), 360, 640);
        scene.getStylesheets().add(ExpandUiTestApp.class.getResource("/view/DarkTheme.css").toExternalForm());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        new ExpandCommand(Index.fromOneBased(1)).execute(model);

        new AnimationTimer() {
            private int frames;

            @Override
            public void handle(long now) {
                if (++frames < 3) {
                    return;
                }
                stop();
                try {
                    assertEquals("Note: " + noteText, label(scene, "note").getText());
                    assertEquals("Tags: " + tagText, label(scene, "expandedTags").getText());
                    assertEquals(candidate.getName().fullName, label(scene, "name").getText());
                    assertWrappedWithinScene(scene, "name");
                    assertWrappedWithinScene(scene, "address");
                    assertWrappedWithinScene(scene, "expandedTags");
                    assertWrappedWithinScene(scene, "note");
                    Bounds tags = label(scene, "expandedTags").localToScene(
                            label(scene, "expandedTags").getBoundsInLocal());
                    Bounds phone = label(scene, "phone").localToScene(label(scene, "phone").getBoundsInLocal());
                    assertTrue(tags.getMaxY() <= phone.getMinY(), "Wrapped tags overlap contact fields");

                    new ListCommand().execute(model);
                    panel.getRoot().applyCss();
                    panel.getRoot().layout();
                    assertFalse(label(scene, "note").isVisible());
                    assertFalse(label(scene, "note").isManaged());
                    assertTrue(scene.lookup("#tags").isManaged());
                    model.setAddressBook(new AddressBook());
                    Person noNote = new PersonBuilder().withTags().build();
                    model.addPerson(noNote);
                    new ExpandCommand(Index.fromOneBased(1)).execute(model);
                    panel.getRoot().applyCss();
                    panel.getRoot().layout();
                    assertEquals("No note recorded.", label(scene, "note").getText());
                    assertFalse(label(scene, "expandedTags").isManaged());
                    stage.close();
                    Platform.exit();
                } catch (Exception | AssertionError e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }.start();
    }

    /** Returns a label from the scene by its FXML id. */
    private static Label label(Scene scene, String id) {
        return (Label) scene.lookup("#" + id);
    }

    /** Confirms a long field wraps without extending beyond the visible card width. */
    private static void assertWrappedWithinScene(Scene scene, String id) {
        Label field = label(scene, id);
        Bounds bounds = field.localToScene(field.getBoundsInLocal());
        assertTrue(field.isWrapText(), id + " does not wrap");
        assertTrue(field.getHeight() > 20, id + " is truncated to one line");
        assertTrue(bounds.getMaxX() <= scene.getWidth(), id + " extends beyond the viewport");
    }
}
