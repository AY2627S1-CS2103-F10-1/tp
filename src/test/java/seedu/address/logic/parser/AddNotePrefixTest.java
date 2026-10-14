package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.testutil.PersonBuilder;

public class AddNotePrefixTest {
    private static final String REQUIRED_FIELDS = " n/A p/123 e/a@x.com a/Blk 1";
    private final AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_notePrefixAfterAddress_reportsUnsupportedNoteInsteadOfStoringIt() {
        for (String note : List.of("great candidate", "", " \t ", "first no/second")) {
            assertParseFailure(parser, REQUIRED_FIELDS + " no/" + note,
                    AddCommandParser.MESSAGE_UNSUPPORTED_NOTE);
        }
    }

    @Test
    public void parse_notePrefixAmongOtherFields_reportsUnsupportedNote() {
        for (String args : List.of(" no/feedback" + REQUIRED_FIELDS,
                " n/A no/feedback p/123 e/a@x.com a/Blk 1",
                REQUIRED_FIELDS + " s/Applied no/feedback t/backend",
                REQUIRED_FIELDS + " t/backend no/feedback")) {
            assertParseFailure(parser, args, AddCommandParser.MESSAGE_UNSUPPORTED_NOTE);
        }
    }

    @Test
    public void parse_notePrefixInsideAddressWord_preservesLiteralAddress() {
        AddCommand expected = new AddCommand(new PersonBuilder().withName("A").withPhone("123")
                .withEmail("a@x.com").withAddress("Blk 1 yes/no/feedback").build());

        assertParseSuccess(parser, REQUIRED_FIELDS + " yes/no/feedback", expected);
    }
}
