package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import seedu.address.logic.commands.EditRoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for {@link EditRoleCommand}. */
public class EditRoleCommandParser implements Parser<EditRoleCommand> {
    @Override
    public EditRoleCommand parse(String arguments) throws ParseException {
        String usage = EditRoleCommand.MESSAGE_USAGE;
        ArgumentMultimap values = RoleParserUtil.parseArguments(arguments, usage, PREFIX_ROLE, PREFIX_NAME);
        return new EditRoleCommand(RoleParserUtil.requireName(values, PREFIX_ROLE, usage),
                RoleParserUtil.requireName(values, PREFIX_NAME, usage));
    }
}
