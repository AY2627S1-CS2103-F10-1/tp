package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.model.person.Status;

public class FilterCommandParserTest {

    private static final String INVALID_STATUS_MESSAGE =
            Status.MESSAGE_CONSTRAINTS;

    private FilterCommandParser parser = new FilterCommandParser();

    @Test
    public void parse_validArgs_returnsFilterCommand() {
        FilterCommand expected = new FilterCommand(Status.INTERVIEWING);
        assertParseSuccess(parser, " s/INTERVIEWING", expected);
    }

    @Test
    public void parse_lowercaseStatus_returnsFilterCommand() {
        FilterCommand expected = new FilterCommand(Status.APPLIED);
        assertParseSuccess(parser, " s/applied", expected);
    }

    @Test
    public void parse_mixedCaseStatus_returnsFilterCommand() {
        FilterCommand expected = new FilterCommand(Status.ACCEPTED);
        assertParseSuccess(parser, " s/Accepted", expected);
    }

    @Test
    public void parse_uppercasePrefix_returnsFilterCommand() {
        FilterCommand expected = new FilterCommand(Status.OFFERED);
        assertParseSuccess(parser, " S/Offered", expected);
    }

    @Test
    public void parse_missingStatus_throwsParseException() {
        assertParseFailure(parser, "     ",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidStatus_throwsParseException() {
        assertParseFailure(parser, " s/INVALID", INVALID_STATUS_MESSAGE);
    }

    @Test
    public void parse_emptyStatus_throwsParseException() {
        assertParseFailure(parser, " s/", INVALID_STATUS_MESSAGE);
    }

    @Test
    public void parse_nonEmptyPreamble_throwsParseException() {
        assertParseFailure(parser, "garbage s/APPLIED",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_multipleStatuses_returnsFilterCommandForAnyStatus() {
        FilterCommand expected = new FilterCommand(List.of(Status.APPLIED, Status.ACCEPTED));
        assertParseSuccess(parser, " s/APPLIED s/accepted", expected);
        assertParseSuccess(parser, " s/accepted s/Applied s/applied", expected);
    }

    @Test
    public void parse_multipleStatusesWithOneInvalid_throwsParseException() {
        assertParseFailure(parser, " s/Applied s/Hired", INVALID_STATUS_MESSAGE);
        assertParseFailure(parser, " s/Applied s/", INVALID_STATUS_MESSAGE);
    }

}
