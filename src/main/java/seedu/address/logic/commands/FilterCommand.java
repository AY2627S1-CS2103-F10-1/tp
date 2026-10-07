package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

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
            + "Parameters: "
            + PREFIX_STATUS + "STATUS\n"
            + "Valid statuses: APPLIED, INTERVIEWING, OFFERED, REJECTED, HIRED\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_STATUS + "INTERVIEWING";

    public static final String MESSAGE_SUCCESS = "Filtered %1$d candidate(s) with status: %2$s";

    private final StatusContainsKeywordPredicate predicate;

    /**
     * Constructs a FilterCommand to filter candidates by the specified status.
     *
     * @param status the status to filter by
     */
    public FilterCommand(Status status) {
        this.predicate = new StatusContainsKeywordPredicate(status);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        return new CommandResult(
                String.format(MESSAGE_SUCCESS, model.getFilteredPersonList().size(),
                        predicate.getStatus().getDisplayName()));
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
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
