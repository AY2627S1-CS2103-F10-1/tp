package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** Creates an unused catalog role. */
public class CreateRoleCommand extends Command {
    public static final String COMMAND_WORD = "create_role";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + PREFIX_NAME + "NAME";

    public static final String MESSAGE_SUCCESS = "Created role: %1$s";

    private final String name;

    /** Creates a command for the prepared role display name. */
    public CreateRoleCommand(String name) {
        this.name = requireNonNull(name);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.createRole(name);
            return new CommandResult(String.format(MESSAGE_SUCCESS, name));
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
