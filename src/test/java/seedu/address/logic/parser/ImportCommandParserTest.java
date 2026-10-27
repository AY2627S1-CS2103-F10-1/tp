package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ImportCommand;

public class ImportCommandParserTest {
    private final ImportCommandParser parser = new ImportCommandParser();

    @Test
    public void parse_noArguments_returnsImportCommand() throws Exception {
        assertParseSuccess(parser, " \t", new ImportCommand());
        assertEquals(new ImportCommand(), new AddressBookParser().parseCommand("import"));
        assertEquals(new ImportCommand().hashCode(), new ImportCommand().hashCode());
        assertNotEquals(null, new ImportCommand());
        assertNotEquals("import", new ImportCommand());
    }

    @Test
    public void parse_extraArguments_ignoresArguments() throws Exception {
        for (String args : List.of(" other.csv", " ../../other.csv", " n/ignored p/123", " extra\narguments")) {
            assertParseSuccess(parser, args, new ImportCommand());
            assertEquals(new ImportCommand(), new AddressBookParser().parseCommand("import" + args));
        }
    }
}
