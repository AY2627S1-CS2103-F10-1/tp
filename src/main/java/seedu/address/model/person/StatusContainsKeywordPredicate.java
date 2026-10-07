package seedu.address.model.person;

import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Status} matches the given status.
 */
public class StatusContainsKeywordPredicate implements Predicate<Person> {
    private final Status status;

    /**
     * Constructs a StatusContainsKeywordPredicate to filter persons by the specified status.
     *
     * @param status the status to filter by
     */
    public StatusContainsKeywordPredicate(Status status) {
        this.status = status;
    }

    @Override
    public boolean test(Person person) {
        return person.getStatus().equals(status);
    }

    public Status getStatus() {
        return status;
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
        return status.equals(otherPredicate.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("status", status).toString();
    }
}
