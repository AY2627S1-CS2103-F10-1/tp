package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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
     * Creates a header displaying counts from the given summary provider.
     */
    public CandidateSummaryHeader(CandidateSummary summary) {
        super(FXML, new HeaderLayout());
        requireNonNull(summary);
        AppLogo logo = new AppLogo();
        logoPlaceholder.getChildren().add(logo.getRoot());
        HeaderLayout layout = (HeaderLayout) getRoot();
        layout.setContent(logoPlaceholder, summaryCards);
        addCard("Total candidates", "total", summary.getTotalCounts());
        for (Status status : Status.values()) {
            addCard(status.toString(), status.name().toLowerCase(Locale.ROOT), summary.getStatusCounts(status));
        }
    }

    private void addCard(String title, String id, CandidateSummary.Counts counts) {
        CandidateSummaryCard card = new CandidateSummaryCard(title, id, counts);
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
