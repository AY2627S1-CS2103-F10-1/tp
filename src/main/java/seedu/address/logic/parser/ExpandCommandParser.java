package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.ExpandCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the displayed index for an expand command. */
public class ExpandCommandParser implements Parser<ExpandCommand> {
    public static final String MESSAGE_INVALID_INDEX = "Invalid index! Index must be a positive integer.";

    @Override
    public ExpandCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (args.isBlank()) {
            throw new ParseException("Missing index. " + ExpandCommand.MESSAGE_USAGE);
        }
        return new ExpandCommand(ParserUtil.parseDisplayedIndex(args, MESSAGE_INVALID_INDEX));
    }
}
