package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ListSummaryTest {

    @Test
    public void describe_noCandidates_reportsEmpty() {
        assertEquals(ListSummary.MESSAGE_NO_CANDIDATES, ListSummary.describe(0, 0, false));
        assertEquals(ListSummary.MESSAGE_NO_CANDIDATES, ListSummary.describe(0, 0, true));
    }

    @Test
    public void describe_allShown_reportsTotal() {
        assertEquals("Showing all 6 candidates.", ListSummary.describe(6, 6, false));
        assertEquals("Showing all 1 candidate.", ListSummary.describe(1, 1, false));
    }

    @Test
    public void describe_someHidden_reportsFilteredCount() {
        assertEquals("Showing 2 of 6 candidates (filtered). Type list to show all.",
                ListSummary.describe(2, 6, false));
        assertEquals("Showing 0 of 1 candidate (filtered). Type list to show all.",
                ListSummary.describe(0, 1, false));
    }

    @Test
    public void describe_expanded_reportsExpandedView() {
        assertEquals("Expanded view of 1 of 6 candidates. Type list to show all.",
                ListSummary.describe(1, 6, true));
        assertEquals("Expanded view of 1 of 1 candidate. Type list to show all.",
                ListSummary.describe(1, 1, true));
    }

    @Test
    public void describe_moreShownThanTotal_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> ListSummary.describe(3, 2, false));
    }
}
