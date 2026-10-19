package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ExportCommand;
import seedu.address.model.ModelManager;

public class ExportCommandParserTest {
    private final ExportCommandParser parser = new ExportCommandParser();

    @Test
    public void parse_blankArguments_returnsExportCommand() throws Exception {
        assertParseSuccess(parser, "", new ExportCommand());
        assertParseSuccess(parser, " \t\n", new ExportCommand());
        assertEquals(new ExportCommand(), new AddressBookParser().parseCommand(" \t export \n"));
        assertTrue(new ExportCommand().execute(new ModelManager()).isExport());
        assertEquals(new ExportCommand().hashCode(), new ExportCommand().hashCode());
        assertEquals("export", new ExportCommand().toString());
    }

    @Test
    public void parse_extraArguments_returnsExportCommand() throws Exception {
        for (String args : new String[] {" file.csv", " data/", " p/output.csv", " junk",
            " ../../other.csv", " data/elsewhere p/other.csv", " \t junk\nmore"}) {
            assertParseSuccess(parser, args, new ExportCommand());
            assertEquals(new ExportCommand(), new AddressBookParser().parseCommand("export" + args));
        }
    }
}
