package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;

/**
 * Displays the text logo and an all-or-nothing row of live candidate summary cards.
 */
public class CandidateSummaryHeader extends UiPart<Region> {
    private static final String FXML = "CandidateSummaryHeader.fxml";

    @FXML
    private StackPane logoPlaceholder;

    @FXML
    private HBox summaryCards;

    /**
     * Creates a header observing the displayed candidates and all candidates loaded in memory.
     */
    public CandidateSummaryHeader(ObservableList<Person> shownPersons, ObservableList<Person> allPersons) {
        super(FXML, new HeaderLayout());
        requireNonNull(shownPersons);
        requireNonNull(allPersons);
        AppLogo logo = new AppLogo();
        logoPlaceholder.getChildren().add(logo.getRoot());
        HeaderLayout layout = (HeaderLayout) getRoot();
        layout.setContent(logoPlaceholder, summaryCards);
        addCard(new CandidateSummaryCard(shownPersons, allPersons));
        for (Status status : Status.values()) {
            addCard(new CandidateSummaryCard(status, shownPersons, allPersons));
        }
    }

    private void addCard(CandidateSummaryCard card) {
        // An unmanaged group does not propagate layout requests to this header.
        card.countTextProperty().addListener(observable -> getRoot().requestLayout());
        summaryCards.getChildren().add(card.getRoot());
    }

    /**
     * Arranges the FXML nodes without allowing the card row to force the window wider.
     */
    private static class HeaderLayout extends HBox {
        private Region logo;
        private HBox cards;

        private void setContent(Region logo, HBox cards) {
            this.logo = logo;
            this.cards = cards;
        }

        /**
         * Measures every card explicitly, even when the card group is unmanaged and invisible.
         */
        private double computeCardsWidth() {
            double width = Math.max(0, cards.getChildren().size() - 1) * cards.getSpacing();
            for (Node card : cards.getChildren()) {
                width += Math.ceil(Math.max(card.minWidth(-1), card.prefWidth(-1)));
            }
            return width;
        }

        @Override
        protected double computeMinWidth(double height) {
            return snappedLeftInset() + logo.minWidth(-1) + snappedRightInset();
        }

        @Override
        protected double computePrefWidth(double height) {
            return snappedLeftInset() + logo.prefWidth(-1) + getSpacing()
                    + computeCardsWidth() + snappedRightInset();
        }

        @Override
        protected double computePrefHeight(double width) {
            return snappedTopInset() + Math.max(logo.prefHeight(-1), cards.prefHeight(-1))
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
            double requiredWidth = logo.prefWidth(-1) + getSpacing() + computeCardsWidth();
            boolean canShowCards = availableWidth >= requiredWidth;
            cards.setVisible(canShowCards);
            cards.setManaged(canShowCards);
            super.layoutChildren();
        }
    }
}
