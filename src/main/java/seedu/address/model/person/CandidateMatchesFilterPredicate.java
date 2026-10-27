package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import seedu.address.model.role.Role;

/** Matches any status and any role within each supplied group, requiring both groups when supplied. */
public class CandidateMatchesFilterPredicate implements Predicate<Person> {
    private final Set<Status> statuses;
    private final Set<String> roleIds;

    /** Creates a filter with at least one criterion, retaining role identities across renames. */
    public CandidateMatchesFilterPredicate(Collection<Status> statuses, Collection<Role> roles) {
        requireAllNonNull(statuses, roles);
        checkArgument(!statuses.isEmpty() || !roles.isEmpty(), "At least one filter criterion is required.");
        this.statuses = Set.copyOf(statuses);
        this.roleIds = roles.stream().map(Role::getId).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean test(Person person) {
        boolean matchesStatus = statuses.isEmpty() || statuses.contains(person.getStatus());
        boolean matchesRole = roleIds.isEmpty()
                || person.getRoles().stream().anyMatch(role -> roleIds.contains(role.getId()));
        return matchesStatus && matchesRole;
    }
}
