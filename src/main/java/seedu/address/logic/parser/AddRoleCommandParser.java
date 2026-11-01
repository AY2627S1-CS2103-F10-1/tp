package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import seedu.address.logic.commands.AddRoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for {@link AddRoleCommand}. */
public class AddRoleCommandParser implements Parser<AddRoleCommand> {
    @Override
    public AddRoleCommand parse(String arguments) throws ParseException {
        String usage = AddRoleCommand.MESSAGE_USAGE;
        ArgumentMultimap values = RoleParserUtil.parseArguments(arguments, usage, PREFIX_NAME);
        return new AddRoleCommand(RoleParserUtil.requireName(values, PREFIX_NAME, usage));
    }
}
