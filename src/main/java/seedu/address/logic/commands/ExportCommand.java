package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/** Requests a CSV export of the complete address book without changing the model. */
public class ExportCommand extends Command {
    public static final String COMMAND_WORD = "export";
    public static final String MESSAGE_USAGE = "Usage: export\n"
            + "Extra arguments are ignored. Custom data locations and output paths cannot be specified.";

    /**
     * CH: This command is bespoke in that most of the command is handled in LogicManager with side effects.
     * As such the CommandResult is used to trigger the LogicManager where it has access to the model and storage
     * If not we would need to pass in the model and storage to all execute functions
     */

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult("", false, false, true);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ExportCommand;
    }

    @Override
    public int hashCode() {
        return ExportCommand.class.hashCode();
    }

    @Override
    public String toString() {
        return COMMAND_WORD;
    }
}
