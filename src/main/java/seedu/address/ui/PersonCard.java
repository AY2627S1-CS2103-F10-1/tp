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
    private static final String NOTE_PREFIX = "Note: ";
    private static final String MESSAGE_NO_NOTE = "No note recorded.";

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
                .forEach(this::addRoleBadge);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> addTagBadge(tag.tagName, isExpanded));
    }

    /** Adds a role badge, highlighting the built-in fallback separately. */
    private void addRoleBadge(Role role) {
        Label label = addWrappingBadge(roles, role.getName());
        if (role.equals(Role.NO_ROLE)) {
            label.getStyleClass().add("no-role");
        }
    }

    /** Adds a tag badge, wrapping its text only in the expanded view. */
    private void addTagBadge(String tagName, boolean isExpanded) {
        if (isExpanded) {
            addWrappingBadge(tags, tagName);
        } else {
            addBadge(tags, tagName);
        }
    }

    /** Adds a single-line badge and returns it for further styling. */
    private Label addBadge(FlowPane pane, String text) {
        Label label = new Label(text);
        pane.getChildren().add(label);
        return label;
    }

    /** Adds a badge whose text wraps within the pane width and returns it for further styling. */
    private Label addWrappingBadge(FlowPane pane, String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.maxWidthProperty().bind(pane.widthProperty());
        // FlowPane measures children without a width, so reserve the wrapped badge height.
        label.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                label.prefHeight(pane.getWidth()), pane.widthProperty(),
                label.fontProperty(), label.insetsProperty()));
        pane.getChildren().add(label);
        return label;
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
        if (!isExpanded) {
            return;
        }
        cardPane.setMinWidth(0);
        configureBadgePane(tags);
        details.prefWidthProperty().bind(cardPane.prefWidthProperty());
        note.setText(person.getNote().map(value -> NOTE_PREFIX + value.value).orElse(MESSAGE_NO_NOTE));
        enableWrapping(name, phone, address, email, status, note);
        HBox.setHgrow(name, Priority.ALWAYS);
    }

    /** Lets each label shrink below its text width and wrap onto further lines. */
    private static void enableWrapping(Label... fields) {
        for (Label field : fields) {
            field.setWrapText(true);
            field.setMinWidth(0);
            field.setMaxWidth(Double.MAX_VALUE);
        }
    }

}
