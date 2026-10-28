package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.StatusCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Status;

public class StatusCommandParserTest {
    private final StatusCommandParser parser = new StatusCommandParser();

    @Test
    public void parse_validStatus_returnsCommand() {
        assertParseSuccess(parser, "1 s/iNtErViEwInG", new StatusCommand(INDEX_FIRST_PERSON, Status.INTERVIEWING));
        assertParseSuccess(parser, "01 s/Applied", new StatusCommand(INDEX_FIRST_PERSON, Status.APPLIED));
    }

    @Test
    public void parse_allStatusesWithDifferentCases_returnsCommand() {
        for (Status status : Status.values()) {
            StatusCommand expectedCommand = new StatusCommand(INDEX_FIRST_PERSON, status);
            assertParseSuccess(parser, "1 s/" + status, expectedCommand);
            assertParseSuccess(parser, "1 s/" + status.toString().toLowerCase(Locale.ROOT), expectedCommand);
            assertParseSuccess(parser, "1 s/" + status.toString().toUpperCase(Locale.ROOT), expectedCommand);
        }
    }

    @Test
    public void parse_surroundingWhitespace_returnsCommand() {
        assertParseSuccess(parser, " \t1 \t s/ \t\r\niNtErViEwInG \t\r\n",
                new StatusCommand(INDEX_FIRST_PERSON, Status.INTERVIEWING));
    }

    @Test
    public void parse_uppercasePrefix_returnsCommand() {
        assertParseSuccess(parser, "1 S/Offered", new StatusCommand(INDEX_FIRST_PERSON, Status.OFFERED));
    }

    @Test
    public void parse_missingPrefix_throwsParseException() {
        assertParseFailure(parser, "1 Interviewing",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, StatusCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1", String.format(MESSAGE_INVALID_COMMAND_FORMAT, StatusCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_nonIntegerIndex_throwsParseException() {
        for (String index : List.of("abc", "1.5", "+1", "1 2")) {
            assertParseFailure(parser, index + " s/Applied",
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, StatusCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_nonPositiveIndex_throwsParseException() {
        for (String index : List.of("0", "000", "-0", "-1", "-99999999999")) {
            assertParseFailure(parser, index + " s/Applied", ParserUtil.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_indexOverflow_throwsParseException() {
        for (String index : List.of("2147483648", "99999999999", "9".repeat(100))) {
            assertParseFailure(parser, index + " s/Applied", Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
    }

    @Test
    public void parse_maximumIntegerIndex_returnsCommand() {
        assertParseSuccess(parser, Integer.MAX_VALUE + " s/Applied",
                new StatusCommand(Index.fromOneBased(Integer.MAX_VALUE), Status.APPLIED));
    }

    @Test
    public void parse_missingIndex_throwsParseException() {
        assertParseFailure(parser, " s/Applied",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, StatusCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidStatus_throwsParseException() {
        assertParseFailure(parser, "1 s/Hired", Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 s/Short listed", Status.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidStatus_preservesExceptionCause() {
        assertThrows(ParseException.class, Status.MESSAGE_CONSTRAINTS, () -> {
            try {
                parser.parse("1 s/Hired");
            } catch (ParseException exception) {
                assertInstanceOf(IllegalArgumentException.class, exception.getCause());
                assertEquals(Status.MESSAGE_CONSTRAINTS, exception.getCause().getMessage());
                throw exception;
            }
        });
    }

    @Test
    public void parse_blankStatus_throwsParseException() {
        for (String status : List.of("", " \t\r\n", "\u2003")) {
            assertParseFailure(parser, "1 s/" + status, Status.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unicodeWhitespaceAroundStatus_throwsParseException() {
        assertParseFailure(parser, "1 s/\u2003Applied\u2003", Status.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedPrefix_throwsParseException() {
        assertParseFailure(parser, "1 s/Applied s/Rejected", Messages.MESSAGE_DUPLICATE_FIELDS + "s/");
        assertParseFailure(parser, "1 s/Applied S/Applied", Messages.MESSAGE_DUPLICATE_FIELDS + "s/");
    }

    @Test
    public void parse_duplicatePrefixesWithInvalidStatus_reportsDuplicatePrefixes() {
        assertParseFailure(parser, "1 s/Hired s/", Messages.MESSAGE_DUPLICATE_FIELDS + "s/");
    }

    @Test
    public void parse_invalidIndexWithInvalidStatusOrPrefixes_reportsIndexFirst() {
        assertParseFailure(parser, "0 s/Hired", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "0 s/Applied s/Rejected", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "0", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "99999999999 s/Hired s/", Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }
}
