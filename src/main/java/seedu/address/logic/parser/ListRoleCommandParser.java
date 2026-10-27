package seedu.address.logic.parser;

import seedu.address.logic.commands.ListRoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for {@link ListRoleCommand}. */
public class ListRoleCommandParser implements Parser<ListRoleCommand> {
    @Override
    public ListRoleCommand parse(String arguments) throws ParseException {
        if (!arguments.isBlank()) {
            throw RoleParserUtil.invalidFormat(ListRoleCommand.MESSAGE_USAGE);
        }
        return new ListRoleCommand();
    }
}
