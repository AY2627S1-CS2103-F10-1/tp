package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.role.Role;

/** Removes a catalogue role and updates every affected candidate. */
public class DeleteRoleCommand extends Command {
    public static final String COMMAND_WORD = "delete_role";
    public static final String MESSAGE_USAGE = "delete_role r/NAME";

    private final String name;

    /** Creates a command for the supplied role name. */
    public DeleteRoleCommand(String name) {
        Role.normalize(name);
        this.name = name.strip();
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.deleteRole(name);
            return new CommandResult("Deleted role: " + name);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DeleteRoleCommand command && name.equals(command.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return COMMAND_WORD + " " + name;
    }
}
