package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.EnumMap;
import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableLongValue;
import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;

/**
 * Provides observable candidate counts without depending on UI nodes or layout.
 */
public class CandidateSummary {
    private final Counts totalCounts;
    private final Map<Status, Counts> statusCounts = new EnumMap<>(Status.class);

    /**
     * Creates counts observing the displayed candidates and all candidates loaded in memory.
     */
    public CandidateSummary(ObservableList<Person> shownPersons, ObservableList<Person> allPersons) {
        requireNonNull(shownPersons);
        requireNonNull(allPersons);
        totalCounts = new Counts(
                Bindings.createLongBinding(() -> (long) shownPersons.size(), shownPersons),
                Bindings.createLongBinding(() -> (long) allPersons.size(), allPersons));
        for (Status status : Status.values()) {
            // Observe each list separately: the full list can notify before the FilteredList
            // has processed the same change, so iterating both in one binding is unsafe.
            statusCounts.put(status, new Counts(
                    Bindings.createLongBinding(() -> countPersons(shownPersons, status), shownPersons),
                    Bindings.createLongBinding(() -> countPersons(allPersons, status), allPersons)));
        }
    }

    /**
     * Returns the displayed and overall counts across all statuses.
     */
    public Counts getTotalCounts() {
        return totalCounts;
    }

    /**
     * Returns the displayed and overall counts for the given status.
     */
    public Counts getStatusCounts(Status status) {
        return statusCounts.get(requireNonNull(status));
    }

    private long countPersons(ObservableList<Person> persons, Status status) {
        return persons.stream().filter(person -> person.getStatus() == status).count();
    }

    /**
     * Holds read-only observable counts for the displayed list and the full in-memory list.
     *
     * @param shown Count in the displayed list.
     * @param total Count in the full in-memory list.
     */
    public record Counts(ObservableLongValue shown, ObservableLongValue total) {
        /**
         * Creates a pair of non-null observable counts.
         */
        public Counts {
            requireNonNull(shown);
            requireNonNull(total);
        }
    }
}
