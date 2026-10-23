package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.Collection;
import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.Status;
import seedu.address.model.person.StatusContainsKeywordPredicate;

/**
 * Filters candidates by their status and displays them.
 */
public class FilterCommand extends Command {

    public static final String COMMAND_WORD = "filter";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Filters candidates by status. "
            + "Repeat " + PREFIX_STATUS + " to show candidates in any of several statuses.\n"
            + "Parameters: "
            + PREFIX_STATUS + "STATUS [" + PREFIX_STATUS + "MORE_STATUS]...\n"
            + Status.MESSAGE_CONSTRAINTS + "\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_STATUS + "Interviewing " + PREFIX_STATUS + "Offered";

    public static final String MESSAGE_SUCCESS = "Filtered %1$d candidate(s) with status: %2$s";

    private static final String STATUS_SEPARATOR = " or ";

    private final StatusContainsKeywordPredicate predicate;

    /**
     * Constructs a FilterCommand to filter candidates by the specified status.
     *
     * @param status the status to filter by
     */
    public FilterCommand(Status status) {
        this.predicate = new StatusContainsKeywordPredicate(status);
    }

    /**
     * Constructs a FilterCommand to show candidates with any of the specified statuses.
     *
     * @param statuses the statuses to filter by; must not be empty
     */
    public FilterCommand(Collection<Status> statuses) {
        this.predicate = new StatusContainsKeywordPredicate(statuses);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        String statuses = predicate.getStatuses().stream()
                .map(Status::toString)
                .collect(Collectors.joining(STATUS_SEPARATOR));
        return new CommandResult(String.format(MESSAGE_SUCCESS, model.getFilteredPersonList().size(), statuses));
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
        return predicate.equals(otherCommand.predicate);
    }

    @Override
    public int hashCode() {
        return predicate.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
