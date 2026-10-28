package seedu.address.logic.commands;

import seedu.address.model.Model;

/** Requests a coordinated CSV import through the logic and storage components. */
public class ImportCommand extends Command {
    public static final String COMMAND_WORD = "import";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Imports candidates from import.csv.\n"
            + "Example: " + COMMAND_WORD;

    @Override
    public CommandResult execute(Model model) {
        // LogicManager coordinates input, model rollback and persistence before reporting success.
        return CommandResult.createImportRequest();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ImportCommand;
    }

    @Override
    public int hashCode() {
        return ImportCommand.class.hashCode();
    }
}
