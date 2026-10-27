package seedu.address.ui;

import java.util.Comparator;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.model.role.Role;

/**
 * Displays a candidate's information.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private GridPane details;
    @FXML
    private Label name;
    @FXML
    private Label noteIcon;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private Label status;
    @FXML
    private Label note;
    @FXML
    private FlowPane tags;
    @FXML
    private FlowPane roles;

    /**
     * Creates a card with the given candidate and displayed index.
     */
    public PersonCard(Person candidate, int displayedIndex) {
        this(candidate, displayedIndex, false);
    }

    /** Creates a card with complete details and note text when expanded. */
    public PersonCard(Person candidate, int displayedIndex, boolean isExpanded) {
        super(FXML);
        person = candidate;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        noteIcon.setVisible(person.hasNote());
        noteIcon.setManaged(person.hasNote());
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);
        status.setText(person.getStatus().toString());
        configureExpandedView(isExpanded);
        configureBadgePane(roles);
        person.getRoles().stream()
                .sorted(Comparator.comparing(role -> Role.toLookupKey(role.getName())))
                .forEach(role -> addBadge(roles, role.getName(), true));
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> addBadge(tags, tag.tagName, isExpanded));
    }

    /** Adds a badge using the same styling for roles and tags. */
    private void addBadge(FlowPane pane, String text, boolean shouldWrapText) {
        Label label = new Label(text);
        if (shouldWrapText) {
            label.setWrapText(true);
            label.setMinWidth(0);
            label.maxWidthProperty().bind(pane.widthProperty());
            // FlowPane measures children without a width, so reserve the wrapped badge height.
            label.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                    label.prefHeight(pane.getWidth()), pane.widthProperty(),
                    label.fontProperty(), label.insetsProperty()));
        }
        pane.getChildren().add(label);
    }

    /** Remeasures wrapped badges when their available width changes. */
    private void configureBadgePane(FlowPane pane) {
        pane.setMinWidth(0);
        pane.setMinHeight(Region.USE_PREF_SIZE);
        pane.widthProperty().addListener((observable, oldWidth, newWidth) ->
                Platform.runLater(pane::requestLayout));
    }

    /** Configures note visibility and wrapping for the expanded record. */
    private void configureExpandedView(boolean isExpanded) {
        note.setVisible(isExpanded);
        note.setManaged(isExpanded);
        if (isExpanded) {
            cardPane.setMinWidth(0);
            configureBadgePane(tags);
            details.prefWidthProperty().bind(cardPane.prefWidthProperty());
            note.setText(person.getNote().map(value -> "Note: " + value.value).orElse("No note recorded."));
            for (Label field : new Label[] {name, phone, address, email, status, note}) {
                field.setWrapText(true);
                field.setMinWidth(0);
                field.setMaxWidth(Double.MAX_VALUE);
            }
            HBox.setHgrow(name, Priority.ALWAYS);
        }
    }

}
