package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.CandidateMatchesFilterPredicate;
import seedu.address.model.person.Status;
import seedu.address.model.role.Role;

/**
 * Filters candidates by status, role, or both and displays them.
 */
public class FilterCommand extends Command {

    public static final String COMMAND_WORD = "filter";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Filters candidates by status and/or role. "
            + "Repeat " + PREFIX_STATUS + " to show candidates in any of several statuses.\n"
            + "Parameters: "
            + "[" + PREFIX_STATUS + "STATUS]... [" + PREFIX_ROLE + "ROLE]...\n"
            + "Supply at least one status or role. Matches any value within each field "
            + "and both fields when supplied.\n"
            + Status.MESSAGE_CONSTRAINTS + "\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_STATUS + "Interviewing " + PREFIX_STATUS + "Offered";

    public static final String MESSAGE_SUCCESS = "Filtered %1$d candidate(s) with status(es): %2$s";

    public static final String MESSAGE_ROLE_SUCCESS = "Filtered %1$d candidate(s) with role(s): %2$s";
    public static final String MESSAGE_COMBINED_SUCCESS =
            "Filtered %1$d candidate(s) with status(es): %2$s and role(s): %3$s";

    private static final String NAME_SEPARATOR = ", ";
    private static final String LAST_NAME_SEPARATOR = " or ";

    private final Set<Status> statuses;
    private final Set<String> roleNames;

    /**
     * Constructs a FilterCommand to filter candidates by the specified status.
     *
     * @param status the status to filter by
     */
    public FilterCommand(Status status) {
        this(List.of(status));
    }

    /**
     * Constructs a FilterCommand to show candidates with any of the specified statuses.
     *
     * @param statuses the statuses to filter by; must not be empty
     */
    public FilterCommand(Collection<Status> statuses) {
        this(statuses, Set.of());
    }

    /** Creates a filter with at least one status or role name; roles are resolved during execution. */
    public FilterCommand(Collection<Status> statuses, Collection<String> roleNames) {
        requireNonNull(statuses);
        requireNonNull(roleNames);
        checkArgument(!statuses.isEmpty() || !roleNames.isEmpty(),
                "At least one filter criterion is required.");
        this.statuses = statuses.isEmpty() ? Set.of()
                : Collections.unmodifiableSet(EnumSet.copyOf(statuses));
        this.roleNames = Set.copyOf(roleNames);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Set<Role> roles = RoleCommandUtil.resolveRoles(model, roleNames, ListRoleCommand.COMMAND_WORD);
        model.updateFilteredPersonList(new CandidateMatchesFilterPredicate(statuses, roles));
        return createFilterResult(model.getFilteredPersonList().size(), roles);
    }

    /** Creates feedback describing the result count and selected filter criteria. */
    private CommandResult createFilterResult(int count, Set<Role> roles) {
        if (roles.isEmpty()) {
            return new CommandResult(String.format(MESSAGE_SUCCESS, count,
                    joinNames(statuses.stream().map(Status::toString).toList())));
        }
        String roleSummary = joinNames(roles.stream().sorted(Comparator.comparing(role ->
                Role.toLookupKey(role.getName()))).map(Role::getName).toList());
        if (statuses.isEmpty()) {
            return new CommandResult(String.format(MESSAGE_ROLE_SUCCESS, count, roleSummary));
        }
        return new CommandResult(String.format(MESSAGE_COMBINED_SUCCESS, count,
                joinNames(statuses.stream().map(Status::toString).toList()), roleSummary));
    }

    /**
     * Joins names as "A", "A or B", or "A, B or C".
     * The list must not be empty.
     */
    private static String joinNames(List<String> names) {
        assert !names.isEmpty();
        int lastIndex = names.size() - 1;
        if (lastIndex == 0) {
            return names.get(0);
        }
        return String.join(NAME_SEPARATOR, names.subList(0, lastIndex))
                + LAST_NAME_SEPARATOR + names.get(lastIndex);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof FilterCommand)) {
            return false;
        }
        FilterCommand otherCommand = (FilterCommand) other;
        return statuses.equals(otherCommand.statuses)
                && roleNames.equals(otherCommand.roleNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statuses, roleNames);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("statuses", statuses)
                .add("roleNames", roleNames)
                .toString();
    }
}
