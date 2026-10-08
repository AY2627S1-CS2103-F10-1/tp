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
        try {
            return new ExpandCommand(ParserUtil.parseIndex(args));
        } catch (ParseException e) {
            throw new ParseException(MESSAGE_INVALID_INDEX, e);
        }
    }
}
