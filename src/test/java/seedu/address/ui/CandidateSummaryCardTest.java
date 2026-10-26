package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleLongProperty;
import javafx.scene.text.Text;

public class CandidateSummaryCardTest {
    @Test
    public void constructor_validCounts_setsTitleIdsAndStyle() {
        CandidateSummary.Counts counts = new CandidateSummary.Counts(new SimpleLongProperty(2),
                new SimpleLongProperty(8));
        CandidateSummaryCard card = new CandidateSummaryCard("Interviewing", "interviewing", counts);

        assertEquals("Interviewing", ((Text) card.getRoot().lookup("#heading")).getText());
        assertEquals("interviewingSummaryCard", card.getRoot().getId());
        assertTrue(card.getRoot().getStyleClass().contains("summary-card"));
        assertTrue(card.getRoot().getStyleClass().contains("summary-interviewing"));
        assertEquals("2 / 8", card.countTextProperty().get());
        assertEquals("shown / total", ((Text) card.getRoot().lookup(".summary-card-legend")).getText());
    }

    @Test
    public void counts_valuesChange_updatesDisplayedText() {
        SimpleLongProperty shown = new SimpleLongProperty(0);
        SimpleLongProperty total = new SimpleLongProperty(0);
        CandidateSummaryCard card = new CandidateSummaryCard("Total candidates", "total",
                new CandidateSummary.Counts(shown, total));
        assertEquals("0 / 0", card.countTextProperty().get());

        total.set(Integer.MAX_VALUE);
        shown.set(1000);
        assertEquals("1000 / 2147483647", card.countTextProperty().get());
        shown.set(0);
        total.set(0);
        assertEquals("0 / 0", card.countTextProperty().get());
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        CandidateSummary.Counts counts = new CandidateSummary.Counts(new SimpleLongProperty(),
                new SimpleLongProperty());
        assertThrows(NullPointerException.class, () -> new CandidateSummaryCard(null, "total", counts));
        assertThrows(NullPointerException.class, () -> new CandidateSummaryCard("Total candidates", null, counts));
        assertThrows(NullPointerException.class, () -> new CandidateSummaryCard("Total candidates", "total", null));
    }
}
