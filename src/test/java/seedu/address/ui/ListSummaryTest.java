package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ListSummaryTest {

    @Test
    public void describe_noCandidates_reportsEmpty() {
        assertEquals("No candidates.", ListSummary.describe(0, 0, false));
        assertEquals("No candidates.", ListSummary.describe(0, 0, true));
    }

    @Test
    public void describe_allShown_reportsTotal() {
        assertEquals("Showing all 6 candidates.", ListSummary.describe(6, 6, false));
        assertEquals("Showing all 2 candidates.", ListSummary.describe(2, 2, false));
    }

    @Test
    public void describe_onlyCandidateShown_omitsAll() {
        assertEquals("Showing 1 candidate.", ListSummary.describe(1, 1, false));
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
    }

    @Test
    public void describe_expandedOnlyCandidate_omitsListHint() {
        assertEquals("Expanded view of the only candidate.", ListSummary.describe(1, 1, true));
    }

    @Test
    public void describe_moreShownThanTotal_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> ListSummary.describe(3, 2, false));
    }
}
