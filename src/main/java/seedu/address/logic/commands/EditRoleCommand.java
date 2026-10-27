package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** Renames a catalog role globally without replacing candidate role assignments. */
public class EditRoleCommand extends Command {
    public static final String COMMAND_WORD = "edit_role";
    public static final String MESSAGE_USAGE = "edit_role r/EXISTING_NAME n/NEW_NAME";

    private final String oldName;
    private final String newName;

    /** Creates a command to rename an existing role using prepared display names. */
    public EditRoleCommand(String oldName, String newName) {
        this.oldName = requireNonNull(oldName);
        this.newName = requireNonNull(newName);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.renameRole(oldName, newName);
            return new CommandResult("Renamed role: " + oldName + " -> " + newName);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EditRoleCommand command
                && oldName.equals(command.oldName) && newName.equals(command.newName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldName, newName);
    }

    @Override
    public String toString() {
        return COMMAND_WORD + " " + oldName + " -> " + newName;
    }
}
