package seedu.address.logic.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.role.Role;

/** Resolves command role names before any model changes are applied. */
final class RoleCommandUtil {
    private RoleCommandUtil() {
    }

    /** Resolves every name and supplies command-specific guidance for unknown roles. */
    static Set<Role> resolveRoles(Model model, Collection<String> names, String suggestion)
            throws CommandException {
        Set<Role> roles = new HashSet<>();
        for (String name : names) {
            try {
                roles.add(model.getRoleCatalog().find(name).orElseThrow(() ->
                        new CommandException("Role does not exist: " + name.strip()
                                + ". Use " + suggestion + ".")));
            } catch (IllegalArgumentException e) {
                throw new CommandException(e.getMessage(), e);
            }
        }
        return roles;
    }
}
