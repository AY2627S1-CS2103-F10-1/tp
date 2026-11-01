package seedu.address.logic.commands;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

    /**
     * Checks that the index refers to a candidate in the displayed list.
     *
     * @param index Displayed index of the candidate.
     * @param displayedListSize Number of candidates in the displayed list.
     * @throws CommandException If the index is beyond the displayed list.
     */
    protected static void checkDisplayedIndex(Index index, int displayedListSize) throws CommandException {
        if (index.getZeroBased() >= displayedListSize) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
    }

}
