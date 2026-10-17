package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/** Requests a CSV export of the complete address book without changing the model. */
public class ExportCommand extends Command {
    public static final String COMMAND_WORD = "export";
    public static final String MESSAGE_USAGE = "Usage: export\n"
            + "Arguments, including custom data locations and output paths, are not supported.";
    public static final String MESSAGE_SUCCESS = "Exported %d candidates to %s.";

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
