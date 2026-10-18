package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.role.Role;

/**
 * Tests that a {@code Person}'s {@code Role} set contains the given role, case-insensitively.
 */
public class RoleContainsKeywordPredicate implements Predicate<Person> {
    private final Role role;

    /**
     * Constructs a predicate that matches candidates holding the given role.
     *
     * @param role the role to look for
     */
    public RoleContainsKeywordPredicate(Role role) {
        requireNonNull(role);
        this.role = role;
    }

    @Override
    public boolean test(Person person) {
        return person.getRoles().contains(role);
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RoleContainsKeywordPredicate otherPredicate)) {
            return false;
        }
        return role.equals(otherPredicate.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("role", role).toString();
    }
}
