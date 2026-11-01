package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;
import java.util.Map;
import java.util.Set;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    public static final String MESSAGE_UNSUPPORTED_NOTE = "The add command does not support no/. "
            + "Add the candidate first, then use note INDEX no/NOTE_TEXT.";

    private static final List<Map.Entry<Prefix, String>> REQUIRED_FIELDS = List.of(
            Map.entry(PREFIX_NAME, "n/NAME"), Map.entry(PREFIX_PHONE, "p/PHONE_NUMBER"),
            Map.entry(PREFIX_EMAIL, "e/EMAIL"), Map.entry(PREFIX_ADDRESS, "a/ADDRESS"));

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ParserUtil.verifyNoNotePrefix(args, MESSAGE_UNSUPPORTED_NOTE);

        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenizeWithValidation(args,
                        Set.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG),
                        PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                        PREFIX_STATUS, PREFIX_TAG, PREFIX_ROLE);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }
        List<String> missingFields = REQUIRED_FIELDS.stream()
                .filter(field -> argMultimap.getValue(field.getKey()).isEmpty())
                .map(Map.Entry::getValue).toList();
        if (!missingFields.isEmpty()) {
            throw new ParseException("Missing " + String.join(", ", missingFields) + ".");
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                PREFIX_STATUS);
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get());
        Email email = ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get());
        Address address = ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get());
        Status status = argMultimap.getValue(PREFIX_STATUS).isPresent()
                ? ParserUtil.parseStatus(argMultimap.getValue(PREFIX_STATUS).get()) : Status.APPLIED;
        Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));

        Person person = new Person(name, phone, email, address, tagList, status);

        Set<String> roleNames = ParserUtil.parseRoleNames(argMultimap.getAllValues(PREFIX_ROLE));
        if (roleNames.isEmpty()) {
            throw new ParseException(AddCommand.MESSAGE_MISSING_ROLE);
        }
        return new AddCommand(person, roleNames);
    }
}
