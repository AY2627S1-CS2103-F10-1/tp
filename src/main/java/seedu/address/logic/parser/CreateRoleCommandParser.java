package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import seedu.address.logic.commands.CreateRoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for {@link CreateRoleCommand}. */
public class CreateRoleCommandParser implements Parser<CreateRoleCommand> {
    @Override
    public CreateRoleCommand parse(String arguments) throws ParseException {
        String usage = CreateRoleCommand.MESSAGE_USAGE;
        ArgumentMultimap values = RoleParserUtil.parseArguments(arguments, usage, PREFIX_NAME);
        return new CreateRoleCommand(RoleParserUtil.requireName(values, PREFIX_NAME, usage));
    }
}
