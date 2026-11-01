package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.List;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Status;

/**
 * Parses input arguments and creates a new FilterCommand object
 */
public class FilterCommandParser implements Parser<FilterCommand> {

    public static final String MESSAGE_UNSUPPORTED_PREFIX =
            "The filter command does not support %1$s. Use only s/STATUS and r/ROLE.";

    // Other commands' prefixes are tokenized too, so that they are reported instead of being read as a value.
    private static final List<Prefix> UNSUPPORTED_PREFIXES =
            List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG, PREFIX_NOTE);

    /**
     * Parses the given {@code String} of arguments in the context of the FilterCommand
     * and returns a FilterCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FilterCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_STATUS, PREFIX_ROLE,
                PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG, PREFIX_NOTE);

        for (Prefix prefix : UNSUPPORTED_PREFIXES) {
            if (argMultimap.getValue(prefix).isPresent()) {
                throw new ParseException(String.format(MESSAGE_UNSUPPORTED_PREFIX, prefix));
            }
        }

        if ((argMultimap.getValue(PREFIX_STATUS).isEmpty() && argMultimap.getValue(PREFIX_ROLE).isEmpty())
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }

        List<Status> statuses = new ArrayList<>();
        for (String status : argMultimap.getAllValues(PREFIX_STATUS)) {
            statuses.add(ParserUtil.parseStatus(status));
        }

        return new FilterCommand(statuses, ParserUtil.parseRoleNames(argMultimap.getAllValues(PREFIX_ROLE)));
    }

}
