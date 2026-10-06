package seedu.address.model.person;

import java.util.function.Predicate;

/**
 * Tests that a Person's status matches the given keyword.
 */
public class StatusContainsKeywordPredicate implements Predicate<Person> {
    private final Status status;

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
}
