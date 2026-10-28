package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.role.Role;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a candidate to HRvest. "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_EMAIL + "EMAIL "
            + PREFIX_ADDRESS + "ADDRESS "
            + PREFIX_ROLE + "ROLE [" + PREFIX_ROLE + "MORE_ROLE]... "
            + "[" + PREFIX_STATUS + "STATUS] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_ADDRESS + "311, Clementi Ave 2, #02-25 "
            + PREFIX_ROLE + Role.NO_ROLE_NAME + " "
            + PREFIX_STATUS + "Interviewing "
            + PREFIX_TAG + "backend "
            + PREFIX_TAG + "Java";

    public static final String MESSAGE_SUCCESS = "New candidate added: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "This candidate already exists in HRvest.";

    public static final String MESSAGE_MISSING_ROLE =
            "Specify at least one role. Use " + PREFIX_ROLE + Role.NO_ROLE_NAME + " if no role applies.";

    private final Person toAdd;
    private final Set<String> roleNames;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        this(person, person.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
    }

    /** Creates an add command whose role names are resolved when executed. */
    public AddCommand(Person person, Collection<String> roleNames) {
        requireNonNull(person);
        requireNonNull(roleNames);
        checkArgument(!roleNames.isEmpty(), MESSAGE_MISSING_ROLE);
        toAdd = person;
        this.roleNames = Set.copyOf(roleNames);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person candidate = toAdd.withRoles(
                RoleCommandUtil.resolveRoles(model, roleNames, CreateRoleCommand.MESSAGE_USAGE));
        if (model.hasPerson(candidate)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.addPerson(candidate);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(candidate)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd) && roleNames.equals(otherAddCommand.roleNames);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .add("roleNames", roleNames)
                .toString();
    }
}
