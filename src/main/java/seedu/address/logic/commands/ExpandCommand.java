package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Shows one displayed candidate's full record, including the complete note.
 */
public class ExpandCommand extends Command {
    public static final String COMMAND_WORD = "expand";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows a candidate's full record.\n"
            + "Parameters: INDEX (a positive integer in the displayed list)\nExample: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_SUCCESS = "Expanding candidate: %s.";
    public static final String MESSAGE_EMPTY_LIST =
            "No candidates currently listed. Please use the filter or list command.";
    public static final String MESSAGE_OUT_OF_RANGE =
            "No candidate found with that index. Enter an index between 1 and %d.";

    private final Index targetIndex;

    /** Constructs a command for the given displayed index. */
    public ExpandCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        int size = model.getFilteredPersonList().size();
        if (size == 0) {
            throw new CommandException(MESSAGE_EMPTY_LIST);
        }
        if (targetIndex.getZeroBased() >= size) {
            throw new CommandException(String.format(MESSAGE_OUT_OF_RANGE, size));
        }
        Person person = model.getFilteredPersonList().get(targetIndex.getZeroBased());
        model.expandPerson(person);
        return new CommandResult(String.format(MESSAGE_SUCCESS, person.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ExpandCommand command && targetIndex.equals(command.targetIndex);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(targetIndex.getZeroBased());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).toString();
    }
}
