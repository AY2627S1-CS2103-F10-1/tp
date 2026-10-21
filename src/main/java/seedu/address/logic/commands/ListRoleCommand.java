package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.stream.Collectors;

import seedu.address.model.Model;
import seedu.address.model.role.Role;

/** Lists every catalogue entry, including unused roles, in normalized name order. */
public class ListRoleCommand extends Command {
    public static final String COMMAND_WORD = "list_role";
    public static final String MESSAGE_USAGE = "list_role";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        String names = model.getRoleCatalogue().getRoles().stream().map(Role::getName)
                .collect(Collectors.joining("\n"));
        return new CommandResult("Roles:\n" + names);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ListRoleCommand;
    }

    @Override
    public int hashCode() {
        return ListRoleCommand.class.hashCode();
    }

    @Override
    public String toString() {
        return COMMAND_WORD;
    }
}
