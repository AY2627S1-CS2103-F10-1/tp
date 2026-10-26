package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.role.Role;

/** Creates an unused catalog role. */
public class CreateRoleCommand extends Command {
    public static final String COMMAND_WORD = "create_role";
    public static final String MESSAGE_USAGE = "create_role n/NAME";

    private final String name;

    /** Creates a command for the supplied role name. */
    public CreateRoleCommand(String name) {
        Role.normalize(name);
        this.name = name.strip();
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.createRole(name);
            return new CommandResult("Created role: " + name);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof CreateRoleCommand command && name.equals(command.name);
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
