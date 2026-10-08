package seedu.address.ui;

import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final ObservableBooleanValue isExpandedView;
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a panel observing the displayed persons and the expanded view state.
     */
    public PersonListPanel(ObservableList<Person> personList, ObservableBooleanValue isExpandedView) {
        super(FXML);
        this.isExpandedView = isExpandedView;
        personListView.setItems(personList);
        isExpandedView.addListener((observable, oldValue, newValue) -> personListView.refresh());
        personListView.setCellFactory(listView -> new PersonListViewCell());
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                Region card = new PersonCard(person, getIndex() + 1, isExpandedView.get()).getRoot();
                if (isExpandedView.get()) {
                    // Constrain the expanded card to the cell width minus padding so full text wraps on resize.
                    // Normal cards retain their existing single-line layout.
                    card.prefWidthProperty().bind(Bindings.createDoubleBinding(() ->
                            Math.max(0, getWidth() - getInsets().getLeft() - getInsets().getRight()),
                            widthProperty(), insetsProperty()));
                }
                setGraphic(card);
            }
        }
    }

}
