package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import seedu.address.model.person.Person;

/**
 * Displays live shown / total counts across all candidates, regardless of status.
 */
public class CandidateSummaryCard extends UiPart<Region> {
    private static final String FXML = "CandidateSummaryCard.fxml";

    @FXML
    private Text count;

    /**
     * Creates a total card observing the displayed candidates and all candidates loaded in memory.
     */
    public CandidateSummaryCard(ObservableList<Person> shownPersons, ObservableList<Person> allPersons) {
        super(FXML);
        requireNonNull(shownPersons);
        requireNonNull(allPersons);
        count.textProperty().bind(Bindings.concat(Bindings.size(shownPersons), " / ", Bindings.size(allPersons)));
    }

    /**
     * Returns the displayed count text so the header can react to width changes even while hidden.
     */
    public ObservableStringValue countTextProperty() {
        return count.textProperty();
    }
}
