package seedu.address.ui;

/**
 * Describes how many candidates the list panel shows, so users can tell when a filter hides candidates.
 */
public class ListSummary {
    public static final String MESSAGE_NO_CANDIDATES = "No candidates.";
    public static final String MESSAGE_ONLY_CANDIDATE = "Showing 1 candidate.";
    public static final String MESSAGE_ALL_SHOWN = "Showing all %1$d candidates.";
    public static final String MESSAGE_FILTERED = "Showing %1$d of %2$d %3$s (filtered). Type list to show all.";
    public static final String MESSAGE_EXPANDED_ONLY_CANDIDATE = "Expanded view of the only candidate.";
    public static final String MESSAGE_EXPANDED = "Expanded view of 1 of %1$d candidates. Type list to show all.";

    private ListSummary() {
    }

    /**
     * Returns a one-line summary of the displayed candidates.
     *
     * @param shownCount Number of candidates in the displayed list.
     * @param totalCount Number of candidates in HRvest, including hidden ones.
     * @param isExpanded Whether the list shows one candidate in expanded view.
     * @return The summary to show above the list.
     */
    public static String describe(int shownCount, int totalCount, boolean isExpanded) {
        assert shownCount >= 0 && shownCount <= totalCount : "Displayed candidates must be a subset of all candidates";
        if (totalCount == 0) {
            return MESSAGE_NO_CANDIDATES;
        }
        if (isExpanded) {
            return totalCount == 1 ? MESSAGE_EXPANDED_ONLY_CANDIDATE : String.format(MESSAGE_EXPANDED, totalCount);
        }
        if (shownCount == totalCount) {
            return totalCount == 1 ? MESSAGE_ONLY_CANDIDATE : String.format(MESSAGE_ALL_SHOWN, totalCount);
        }
        return String.format(MESSAGE_FILTERED, shownCount, totalCount, pluralize(totalCount));
    }

    private static String pluralize(int count) {
        return count == 1 ? "candidate" : "candidates";
    }
}
