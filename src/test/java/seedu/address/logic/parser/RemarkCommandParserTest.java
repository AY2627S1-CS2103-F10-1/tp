package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);

    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_invalidPreamble_failure() {
        assertParseFailure(parser, "-5 " + PREFIX_REMARK + "Call later", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "0 " + PREFIX_REMARK + "Call later", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 some text " + PREFIX_REMARK + "Call later", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_repeatedRemarkPrefix_failure() {
        assertParseFailure(parser, "1 " + PREFIX_REMARK + "First " + PREFIX_REMARK + "Second",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_REMARK));
    }

    @Test
    public void parse_validRemark_success() {
        String remark = "Call after 6pm";
        RemarkCommand expectedCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(remark));

        assertParseSuccess(parser, "1 " + PREFIX_REMARK + remark, expectedCommand);
    }

    @Test
    public void parse_emptyRemark_success() {
        RemarkCommand expectedCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));

        assertParseSuccess(parser, "1 " + PREFIX_REMARK, expectedCommand);
    }
}
