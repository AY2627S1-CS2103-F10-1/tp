package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.LongBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;

/**
 * Displays live shown / total candidate counts for one recruitment status.
 */
public class StatusSummaryCard extends UiPart<Region> {
    private static final String FXML = "StatusSummaryCard.fxml";

    @FXML
    private Text heading;

    @FXML
    private Text count;

    /**
     * Creates a card observing candidates with the given status in the displayed and full lists.
     */
    public StatusSummaryCard(Status status, ObservableList<Person> shownPersons, ObservableList<Person> allPersons) {
        super(FXML);
        requireNonNull(status);
        requireNonNull(shownPersons);
        requireNonNull(allPersons);
        String id = status.name().toLowerCase(Locale.ROOT);
        heading.setText(status.toString());
        count.setId(id + "Count");
        // Observe each list separately: the full list can notify before the FilteredList
        // has processed the same change, so iterating both in one binding is unsafe.
        LongBinding shownCount = Bindings.createLongBinding(() -> countPersons(shownPersons, status), shownPersons);
        LongBinding totalCount = Bindings.createLongBinding(() -> countPersons(allPersons, status), allPersons);
        count.textProperty().bind(Bindings.concat(shownCount, " / ", totalCount));
        getRoot().setId(id + "SummaryCard");
        getRoot().getStyleClass().add("summary-" + id);
    }

    private long countPersons(ObservableList<Person> persons, Status status) {
        return persons.stream().filter(person -> person.getStatus() == status).count();
    }

    /**
     * Returns the displayed count text so the header can react to width changes even while hidden.
     */
    public ObservableStringValue countTextProperty() {
        return count.textProperty();
    }
}
