package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.model.person.Note;

public class NoteCommandParserTest {

    private final NoteCommandParser parser = new NoteCommandParser();

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_validArguments_preservesNoteContents() {
        String note = "Strong on system design,  weak on SQL. [Round 2] n/A p/B e/C a/D t/E /expand";
        assertParseSuccess(parser, " \t 1   no/  " + note + "   ",
                new NoteCommand(INDEX_FIRST_PERSON, new Note(note)));
        assertParseSuccess(parser, " 01 no/x", new NoteCommand(INDEX_FIRST_PERSON, new Note("x")));
        assertParseSuccess(parser, " 2147483647 no/x",
                new NoteCommand(Index.fromOneBased(Integer.MAX_VALUE), new Note("x")));
    }

    @Test
    public void parse_uppercasePrefix_returnsCommand() {
        assertParseSuccess(parser, " 1 NO/Follow up",
                new NoteCommand(INDEX_FIRST_PERSON, new Note("Follow up")));
    }

    @Test
    public void parse_missingPrefix_throwsParseException() {
        String message = NoteCommandParser.MESSAGE_MISSING_PREFIX + "\n" + NoteCommand.MESSAGE_USAGE;
        for (String args : new String[] { "", " 1", " 1 n/hi", " 1 hi", " 1no/hi", " 0", " 99999999999" }) {
            assertParseFailure(parser, args, message);
        }
    }

    @Test
    public void parse_missingIndex_throwsParseException() {
        assertParseFailure(parser, " no/hi",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_notWholeNumberIndex_throwsFormatError() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE);
        for (String index : new String[] { "abc", "+1", "1.5", "1 2" }) {
            assertParseFailure(parser, " " + index + " no/hi", message);
        }
    }

    @Test
    public void parse_zeroOrNegativeIndex_throwsInvalidIndex() {
        for (String index : new String[] { "0", "000", "-0", "-1", "-99999999999" }) {
            assertParseFailure(parser, " " + index + " no/hi", ParserUtil.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_indexTooLargeForAnyList_throwsInvalidDisplayedIndex() {
        for (String index : new String[] { "2147483648", "99999999999", "9".repeat(100) }) {
            assertParseFailure(parser, " " + index + " no/hi",
                    Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
    }

    @Test
    public void parse_mixedErrors_preservesCharacterAndPrefixPrecedence() {
        assertParseFailure(parser, " 0 no/\t",
                String.format(ParserUtil.MESSAGE_UNSUPPORTED_CHARACTER, "no/", 9));
        assertParseFailure(parser, " 0 no/first no/second", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " 99999999999 no/first no/second",
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void parse_blankNote_returnsClearCommand() {
        for (String args : new String[] { " 1 no/", " 1 no/   " }) {
            assertParseSuccess(parser, args, new NoteCommand(INDEX_FIRST_PERSON));
        }
    }

    @Test
    public void parse_noteLengthBoundaries_validatesTrimmedLength() {
        for (String character : new String[] { "x", "~" }) {
            String note = character.repeat(Note.MAX_LENGTH);
            assertParseSuccess(parser, " 1 no/ " + note + " ",
                    new NoteCommand(INDEX_FIRST_PERSON, new Note(note)));
            assertParseFailure(parser, " 1 no/" + note + character, Note.MESSAGE_TOO_LONG);
        }
    }

    @Test
    public void parse_invisibleOrUnicodeNote_throwsParseException() {
        for (String text : new String[] { "\t", "\u2003", "\u00A0", "\u200B", "\uD83D\uDE00" }) {
            String message = String.format(ParserUtil.MESSAGE_UNSUPPORTED_CHARACTER, "no/",
                    text.codePointAt(0));
            assertParseFailure(parser, " 1 no/" + text, message);
        }
    }

    @Test
    public void parse_repeatedNotePrefix_throwsParseException() {
        String message = Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NOTE);
        assertParseFailure(parser, " 1 no/first no/second", message);
        assertParseFailure(parser, " 1 no/hi no/hi", message);
        assertParseFailure(parser, " 1 no/ no/", message);
    }

    @Test
    public void parse_prefixInsideWord_preservesLiteralText() {
        String note = "Answer: yes/no/maybe, techno/feedback, YES/NO/MAYBE";
        assertParseSuccess(parser, " 1 no/" + note, new NoteCommand(INDEX_FIRST_PERSON, new Note(note)));
    }
}
