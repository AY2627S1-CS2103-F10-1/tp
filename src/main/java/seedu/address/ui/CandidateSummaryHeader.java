package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.LongBinding;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;

/**
 * Displays the text logo and an all-or-nothing row of live candidate summary cards.
 */
public class CandidateSummaryHeader extends Region {
    private static final double CARD_GAP = 8;
    private static final double LOGO_GAP = 24;
    private static final double MIN_CARD_WIDTH = 105;

    private final Text logo = new Text("HRVest");
    private final HBox cards = new HBox(CARD_GAP);

    /**
     * Creates a header observing the displayed candidates and all candidates loaded in memory.
     */
    public CandidateSummaryHeader(ObservableList<Person> shownPersons, ObservableList<Person> allPersons) {
        requireNonNull(shownPersons);
        requireNonNull(allPersons);
        getStyleClass().add("candidate-summary-header");
        logo.getStyleClass().add("header-logo");
        cards.setId("summaryCards");
        cards.getChildren().add(createCard("Total candidates", "total", shownPersons, allPersons, null));
        for (Status status : Status.values()) {
            cards.getChildren().add(createCard(status.toString(), status.name().toLowerCase(Locale.ROOT),
                    shownPersons, allPersons, status));
        }
        getChildren().addAll(logo, cards);
    }

    /**
     * Builds a card whose natural width accommodates its full label and current count text.
     * A null status denotes the total across all statuses.
     */
    private VBox createCard(String title, String id, ObservableList<Person> shownPersons,
            ObservableList<Person> allPersons, Status status) {
        Text heading = new Text(title);
        heading.getStyleClass().add("summary-card-title");
        Text count = new Text();
        count.setId(id + "Count");
        count.getStyleClass().add("summary-card-count");
        // Compute each count only on its own list's notifications. The full list may notify
        // before a FilteredList has processed the same change, so do not iterate both together.
        LongBinding shownCount = Bindings.createLongBinding(() -> countPersons(shownPersons, status), shownPersons);
        LongBinding totalCount = Bindings.createLongBinding(() -> countPersons(allPersons, status), allPersons);
        count.textProperty().bind(Bindings.concat(shownCount, " / ", totalCount));
        // An unmanaged group does not propagate layout requests to this header.
        count.textProperty().addListener(observable -> requestLayout());
        Text legend = new Text("shown / total");
        legend.getStyleClass().add("summary-card-legend");
        VBox card = new VBox(heading, count, legend);
        card.setId(id + "SummaryCard");
        card.getStyleClass().addAll("summary-card", "summary-" + id);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMinWidth(MIN_CARD_WIDTH);
        return card;
    }

    private long countPersons(ObservableList<Person> persons, Status status) {
        return status == null ? persons.size()
                : persons.stream().filter(person -> person.getStatus() == status).count();
    }

    /**
     * Measures every card explicitly, even when the card group is unmanaged and invisible.
     */
    private double computeCardsWidth() {
        double width = Math.max(0, cards.getChildren().size() - 1) * cards.getSpacing();
        for (Node card : cards.getChildren()) {
            width += Math.ceil(Math.max(MIN_CARD_WIDTH, card.prefWidth(-1)));
        }
        return width;
    }

    @Override
    protected double computeMinWidth(double height) {
        return snappedLeftInset() + logo.getLayoutBounds().getWidth() + snappedRightInset();
    }

    @Override
    protected double computePrefWidth(double height) {
        return computeMinWidth(height) + LOGO_GAP + computeCardsWidth();
    }

    @Override
    protected double computePrefHeight(double width) {
        return snappedTopInset() + Math.max(logo.getLayoutBounds().getHeight(), cards.prefHeight(-1))
                + snappedBottomInset();
    }

    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    /**
     * Keeps the decision independent of the group's visibility to prevent resize oscillation.
     */
    @Override
    protected void layoutChildren() {
        double availableWidth = getWidth() - snappedLeftInset() - snappedRightInset();
        double availableHeight = getHeight() - snappedTopInset() - snappedBottomInset();
        double logoWidth = logo.getLayoutBounds().getWidth();
        double cardsWidth = computeCardsWidth();
        boolean canShowCards = availableWidth >= logoWidth + LOGO_GAP + cardsWidth;
        cards.setVisible(canShowCards);
        cards.setManaged(canShowCards);
        logo.relocate(snappedLeftInset(), snappedTopInset()
                + (availableHeight - logo.getLayoutBounds().getHeight()) / 2);
        if (canShowCards) {
            cards.resizeRelocate(getWidth() - snappedRightInset() - cardsWidth,
                    snappedTopInset(), cardsWidth, availableHeight);
        }
    }
}
