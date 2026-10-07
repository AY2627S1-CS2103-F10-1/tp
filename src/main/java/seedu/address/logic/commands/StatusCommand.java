package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;

/**
 * Updates a candidate's recruitment status.
 */
public class StatusCommand extends Command {
    public static final String COMMAND_WORD = "status";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Updates a candidate's recruitment status.\n"
            + "Parameters: INDEX (must be a positive integer) " + PREFIX_STATUS + "STATUS\n"
            + "Example: status 3 s/Interviewing";
    public static final String MESSAGE_SUCCESS = "Updated status of %s: %s -> %s";
    public static final String MESSAGE_INVALID_INDEX = "The candidate index provided is invalid.";

    private final Index index;
    private final Status status;

    /**
     * Creates a command for the candidate at the displayed index.
     */
    public StatusCommand(Index index, Status status) {
        this.index = requireNonNull(index);
        this.status = requireNonNull(status);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(MESSAGE_INVALID_INDEX);
        }

        Person candidate = displayedPersons.get(index.getZeroBased());
        Person updatedCandidate = new Person(candidate.getName(), candidate.getPhone(), candidate.getEmail(),
                candidate.getAddress(), candidate.getTags(), status);
        model.setPerson(candidate, updatedCandidate);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_SUCCESS, candidate.getName(), candidate.getStatus(), status));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof StatusCommand command
                && index.equals(command.index) && status == command.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, status);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("status", status).toString();
    }
}
