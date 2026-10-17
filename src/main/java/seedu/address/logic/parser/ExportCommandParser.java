package seedu.address.logic.parser;

import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the argument-free export command. */
public class ExportCommandParser implements Parser<ExportCommand> {
    @Override
    public ExportCommand parse(String args) throws ParseException {
        if (!args.isBlank()) {
            throw new ParseException(ExportCommand.MESSAGE_USAGE);
        }
        return new ExportCommand();
    }
}
