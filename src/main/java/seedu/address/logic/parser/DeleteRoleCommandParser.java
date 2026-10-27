package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import seedu.address.logic.commands.DeleteRoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for {@link DeleteRoleCommand}. */
public class DeleteRoleCommandParser implements Parser<DeleteRoleCommand> {
    @Override
    public DeleteRoleCommand parse(String arguments) throws ParseException {
        String usage = DeleteRoleCommand.MESSAGE_USAGE;
        ArgumentMultimap values = RoleParserUtil.parseArguments(arguments, usage, PREFIX_ROLE);
        return new DeleteRoleCommand(RoleParserUtil.requireName(values, PREFIX_ROLE, usage));
    }
}
