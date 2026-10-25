package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book after the user confirms.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String CONFIRM_KEYWORD = "confirm";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Permanently deletes every candidate, "
            + "including those hidden by the current filter. This cannot be undone.\n"
            + "Parameters: " + CONFIRM_KEYWORD + "\n"
            + "Example: " + COMMAND_WORD + " " + CONFIRM_KEYWORD;

    public static final String MESSAGE_CONFIRM = "This will permanently delete all %1$d candidate(s), "
            + "including any hidden by the current filter. This cannot be undone.\n"
            + "To proceed, enter: " + COMMAND_WORD + " " + CONFIRM_KEYWORD;
    public static final String MESSAGE_NO_CANDIDATES = "There are no candidates to clear.";
    public static final String MESSAGE_SUCCESS = "All candidates have been cleared.";

    private final boolean isConfirmed;

    /**
     * Creates a command that clears every candidate if {@code isConfirmed}, or only explains how to confirm.
     */
    public ClearCommand(boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        int candidateCount = model.getAddressBook().getPersonList().size();
        if (candidateCount == 0) {
            return new CommandResult(MESSAGE_NO_CANDIDATES);
        }
        if (!isConfirmed) {
            return new CommandResult(String.format(MESSAGE_CONFIRM, candidateCount));
        }
        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClearCommand command && isConfirmed == command.isConfirmed;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(isConfirmed);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("isConfirmed", isConfirmed).toString();
    }
}
