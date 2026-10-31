package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TestCaseUtil.assertAllCases;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ClearCommand;

public class ClearCommandParserTest {

    private final ClearCommandParser parser = new ClearCommandParser();

    @Test
    public void parse_noArguments_returnsUnconfirmedCommand() {
        assertParseSuccess(parser, "", new ClearCommand(false));
        assertParseSuccess(parser, "   ", new ClearCommand(false));
    }

    @Test
    public void parse_confirmKeyword_returnsConfirmedCommand() {
        assertAllCases(List.of(" confirm", "confirm", "  CONFIRM  ", " Confirm"), args -> {
            assertParseSuccess(parser, args, new ClearCommand(true));
        });
    }

    @Test
    public void parse_otherArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE);
        assertAllCases(List.of(" 1", " 3", " confirm 1", " yes", " confirmed", " all"), args -> {
            assertParseFailure(parser, args, expectedMessage);
        });
    }
}
