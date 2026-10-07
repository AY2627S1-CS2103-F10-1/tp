package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.StatusCommand;
import seedu.address.model.person.Status;

public class StatusCommandParserTest {
    private final StatusCommandParser parser = new StatusCommandParser();

    @Test
    public void parse_validStatus_returnsCommand() {
        assertParseSuccess(parser, "1 s/iNtErViEwInG", new StatusCommand(INDEX_FIRST_PERSON, Status.INTERVIEWING));
    }

    @Test
    public void parse_missingPrefix_throwsParseException() {
        assertParseFailure(parser, "1 Interviewing",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, StatusCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_nonIntegerIndex_throwsParseException() {
        assertParseFailure(parser, "abc s/Applied",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, StatusCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidStatus_throwsParseException() {
        assertParseFailure(parser, "1 s/Hired", Status.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedPrefix_throwsParseException() {
        assertParseFailure(parser, "1 s/Applied s/Rejected", Messages.MESSAGE_DUPLICATE_FIELDS + "s/");
    }
}
