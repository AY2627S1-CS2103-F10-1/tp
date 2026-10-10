package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for replacing a candidate's note.
 */
public class NoteCommandParser implements Parser<NoteCommand> {

    public static final String MESSAGE_MISSING_PREFIX =
            "The note prefix " + PREFIX_NOTE.getPrefix() + " is required.";

    private static final Pattern NOTE_PREFIX_PATTERN = Pattern.compile(
            "\\p{javaWhitespace}" + Pattern.quote(PREFIX_NOTE.getPrefix()), Pattern.CASE_INSENSITIVE);

    @Override
    public NoteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        // Normalize only reserved prefixes; preserve the capitalization and spacing within note text.
        String normalizedArgs = NOTE_PREFIX_PATTERN.matcher(args)
                .replaceAll(Matcher.quoteReplacement(" " + PREFIX_NOTE.getPrefix()));
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(normalizedArgs, PREFIX_NOTE);
        if (arguments.getValue(PREFIX_NOTE).isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_PREFIX + "\n" + NoteCommand.MESSAGE_USAGE);
        }

        Index index;
        try {
            index = ParserUtil.parseIndex(arguments.getPreamble());
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + NoteCommand.MESSAGE_USAGE, e);
        }

        arguments.verifyNoDuplicatePrefixesFor(PREFIX_NOTE);
        return new NoteCommand(index, ParserUtil.parseNote(arguments.getValue(PREFIX_NOTE).orElseThrow()));
    }
}
