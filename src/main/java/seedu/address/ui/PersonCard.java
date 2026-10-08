package seedu.address.ui;

import java.util.Comparator;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
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
    private Label expandedTags;
    @FXML
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        this(person, displayedIndex, false);
    }

    /** Creates a card with complete details and note text when expanded. */
    public PersonCard(Person person, int displayedIndex, boolean expanded) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);
        status.setText(person.getStatus().toString());
        configureExpandedView(expanded);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
    }

    /** Configures note visibility and wrapping for the expanded record. */
    private void configureExpandedView(boolean expanded) {
        note.setVisible(expanded);
        note.setManaged(expanded);
        tags.setVisible(!expanded);
        tags.setManaged(!expanded);
        expandedTags.setVisible(expanded && !person.getTags().isEmpty());
        expandedTags.setManaged(expanded && !person.getTags().isEmpty());
        if (expanded) {
            cardPane.setMinWidth(0);
            details.prefWidthProperty().bind(cardPane.prefWidthProperty());
            expandedTags.setText("Tags: " + person.getTags().stream()
                    .map(tag -> tag.tagName).sorted().collect(Collectors.joining(", ")));
            note.setText(person.getNote().map(value -> "Note: " + value.value).orElse("No note recorded."));
            for (Label field : new Label[] {name, phone, address, email, status, expandedTags, note}) {
                field.setWrapText(true);
                field.setMinWidth(0);
                field.setMaxWidth(Double.MAX_VALUE);
            }
            HBox.setHgrow(name, Priority.ALWAYS);
        }
    }

}
