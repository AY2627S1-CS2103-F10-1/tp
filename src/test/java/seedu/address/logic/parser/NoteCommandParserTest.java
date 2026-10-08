package seedu.address.logic.parser;

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
        assertParseSuccess(parser, " \t 1   no/  " + note + " \t ",
                new NoteCommand(INDEX_FIRST_PERSON, new Note(note)));
        assertParseSuccess(parser, " 2147483647 no/x",
                new NoteCommand(Index.fromOneBased(Integer.MAX_VALUE), new Note("x")));
    }

    @Test
    public void parse_mixedCasePrefixAndWhitespace_preservesNoteContents() {
        String note = "Round 2:\t Strong on SQL\nFollow up soon";
        assertParseSuccess(parser, "\t1\tNO/ " + note + "\t",
                new NoteCommand(INDEX_FIRST_PERSON, new Note(note)));
        assertParseSuccess(parser, " 1\nNo/" + note,
                new NoteCommand(INDEX_FIRST_PERSON, new Note(note)));
    }

    @Test
    public void parse_missingPrefix_throwsParseException() {
        String message = NoteCommandParser.MESSAGE_MISSING_PREFIX + "\n" + NoteCommand.MESSAGE_USAGE;
        for (String args : new String[] {"", " 1", " 1 n/hi", " 1 hi", " 1no/hi"}) {
            assertParseFailure(parser, args, message);
        }
    }

    @Test
    public void parse_missingIndex_throwsParseException() {
        assertParseFailure(parser, " no/hi", ParserUtil.MESSAGE_INVALID_INDEX + "\n" + NoteCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        String message = ParserUtil.MESSAGE_INVALID_INDEX + "\n" + NoteCommand.MESSAGE_USAGE;
        for (String index : new String[] {"abc", "0", "-1", "+1", "1.5", "1 2", "2147483648"}) {
            assertParseFailure(parser, " " + index + " no/hi", message);
        }
    }

    @Test
    public void parse_blankNote_throwsParseException() {
        assertParseFailure(parser, " 1 no/ \t ", Note.MESSAGE_BLANK);
        assertParseFailure(parser, " 1 no/\u2003", Note.MESSAGE_BLANK);
    }

    @Test
    public void parse_noteLengthBoundaries_validatesTrimmedLength() {
        assertParseSuccess(parser, " 1 no/ " + "x".repeat(500) + " ",
                new NoteCommand(INDEX_FIRST_PERSON, new Note("x".repeat(500))));
        assertParseFailure(parser, " 1 no/" + "x".repeat(501), Note.MESSAGE_TOO_LONG);
    }

    @Test
    public void parse_repeatedNotePrefix_throwsParseException() {
        String message = Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NOTE);
        assertParseFailure(parser, " 1 no/first no/second", message);
        assertParseFailure(parser, " 1 no/hi no/hi", message);
        assertParseFailure(parser, " 1 no/ no/", message);
        assertParseFailure(parser, " 1 NO/first no/second", message);
        assertParseFailure(parser, " 1 no/first\tNo/second", message);
        assertParseFailure(parser, " 1 no/first\nNO/second", message);
        assertParseFailure(parser, " 1 no/first\u2003No/second", message);
    }

    @Test
    public void parse_prefixInsideWord_preservesLiteralText() {
        String note = "Answer: yes/no/maybe, techno/feedback, YES/NO/MAYBE";
        assertParseSuccess(parser, " 1 no/" + note, new NoteCommand(INDEX_FIRST_PERSON, new Note(note)));
    }
}
