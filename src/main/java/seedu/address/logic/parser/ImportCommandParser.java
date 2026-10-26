package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ImportCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Rejects arguments to the fixed-path import command. */
public class ImportCommandParser implements Parser<ImportCommand> {
    @Override
    public ImportCommand parse(String args) throws ParseException {
        if (!args.isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ImportCommand.MESSAGE_USAGE));
        }
        return new ImportCommand();
    }
}
