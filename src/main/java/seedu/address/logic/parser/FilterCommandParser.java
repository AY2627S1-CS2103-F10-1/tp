package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Status;

/**
 * Parses input arguments and creates a new FilterCommand object
 */
public class FilterCommandParser implements Parser<FilterCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FilterCommand
     * and returns a FilterCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FilterCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_STATUS);

        if (!argMultimap.getValue(PREFIX_STATUS).isPresent()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }

        String statusString = argMultimap.getValue(PREFIX_STATUS).get().toUpperCase();
        Status status;
        try {
            status = Status.valueOf(statusString);
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid status. Valid statuses: APPLIED, INTERVIEWING, OFFERED, REJECTED, HIRED");
        }

        return new FilterCommand(status);
    }

}
