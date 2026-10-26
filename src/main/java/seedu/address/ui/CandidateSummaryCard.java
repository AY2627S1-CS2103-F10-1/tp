package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableStringValue;
import javafx.fxml.FXML;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

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
     * Creates a card displaying the supplied counts, with an ID used for styling and lookup.
     */
    public CandidateSummaryCard(String title, String id, CandidateSummary.Counts counts) {
        super(FXML);
        requireNonNull(title);
        requireNonNull(id);
        requireNonNull(counts);
        heading.setText(title);
        count.setId(id + "Count");
        count.textProperty().bind(Bindings.concat(counts.shown(), " / ", counts.total()));
        getRoot().setId(id + "SummaryCard");
        getRoot().getStyleClass().add("summary-" + id);
    }

    /**
     * Returns the displayed count text so the header can react to width changes even while hidden.
     */
    public ObservableStringValue countTextProperty() {
        return count.textProperty();
    }
}
