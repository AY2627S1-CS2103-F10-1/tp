package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Status} is one of the given statuses.
 */
public class StatusContainsKeywordPredicate implements Predicate<Person> {
    private final Set<Status> statuses;

    /**
     * Constructs a StatusContainsKeywordPredicate to filter persons by the specified status.
     *
     * @param status the status to filter by
     */
    public StatusContainsKeywordPredicate(Status status) {
        this(EnumSet.of(requireNonNull(status)));
    }

    /**
     * Constructs a StatusContainsKeywordPredicate that matches persons with any of the specified statuses.
     *
     * @param statuses the statuses to filter by; must not be empty
     */
    public StatusContainsKeywordPredicate(Collection<Status> statuses) {
        requireNonNull(statuses);
        checkArgument(!statuses.isEmpty(), "At least one status is required.");
        this.statuses = Collections.unmodifiableSet(EnumSet.copyOf(statuses));
    }

    @Override
    public boolean test(Person person) {
        return statuses.contains(person.getStatus());
    }

    /**
     * Returns the statuses to match, in hiring-pipeline order.
     */
    public Set<Status> getStatuses() {
        return statuses;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof StatusContainsKeywordPredicate)) {
            return false;
        }
        StatusContainsKeywordPredicate otherPredicate = (StatusContainsKeywordPredicate) other;
        return statuses.equals(otherPredicate.statuses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statuses);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("statuses", statuses).toString();
    }
}
