package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ExpandCommand;

public class ExpandCommandParserTest {
    private final ExpandCommandParser parser = new ExpandCommandParser();

    @Test
    public void parse_validIndex_acceptsSurroundingWhitespace() {
        assertParseSuccess(parser, " \t2 ", new ExpandCommand(Index.fromOneBased(2)));
        assertParseSuccess(parser, "2147483647", new ExpandCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_missingIndex_reportsUsage() {
        for (String input : new String[] {"", " \t "}) {
            assertParseFailure(parser, input, "Missing index. " + ExpandCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_invalidIndex_reportsPositiveIntegerRequirement() {
        for (String input : new String[] {"0", "-1", "1.5", "abc", "1 2", "1 n/Alice",
            "2147483648", "999999999999999999999"}) {
            assertParseFailure(parser, input, ExpandCommandParser.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parseCommand_expand_dispatchesToExpandParser() throws Exception {
        for (String command : new String[] {"expand 1", "EXPAND 1", "eXpAnD 1"}) {
            assertEquals(new ExpandCommand(Index.fromOneBased(1)), new AddressBookParser().parseCommand(command));
        }
    }
}
