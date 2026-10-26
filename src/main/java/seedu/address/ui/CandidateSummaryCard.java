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
 * Displays a title and live shown / total counts in a reusable summary card.
 */
public class CandidateSummaryCard extends UiPart<Region> {
    private static final String FXML = "CandidateSummaryCard.fxml";

    @FXML
    private Text heading;

    @FXML
    private Text count;

    /**
     * Creates a total card observing the displayed candidates and all candidates loaded in memory.
     */
    public CandidateSummaryCard(ObservableList<Person> shownPersons, ObservableList<Person> allPersons) {
        this("Total candidates", "total", shownPersons, allPersons, null);
    }

    /**
     * Creates a card counting candidates with the given status in the displayed and full lists.
     */
    public CandidateSummaryCard(Status status, ObservableList<Person> shownPersons, ObservableList<Person> allPersons) {
        this(requireNonNull(status).toString(), status.name().toLowerCase(Locale.ROOT),
                shownPersons, allPersons, status);
    }

    /**
     * Creates a card with live counts. A null status denotes the total across all statuses.
     */
    private CandidateSummaryCard(String title, String id, ObservableList<Person> shownPersons,
            ObservableList<Person> allPersons, Status status) {
        super(FXML);
        requireNonNull(shownPersons);
        requireNonNull(allPersons);
        heading.setText(title);
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
        return status == null ? persons.size()
                : persons.stream().filter(person -> person.getStatus() == status).count();
    }

    /**
     * Returns the displayed count text so the header can react to width changes even while hidden.
     */
    public ObservableStringValue countTextProperty() {
        return count.textProperty();
    }
}
