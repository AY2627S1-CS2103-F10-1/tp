package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TestCaseUtil.assertAllCases;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private static final String NOTE_REQUIRED_FIELDS = " n/A p/123 e/a@x.com a/Blk 1";

    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND)
                .withStatus(Status.APPLIED).build();

        // whitespace only preamble
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND + " r/No role", new AddCommand(expectedPerson));


        // multiple tags - all accepted
        Person expectedPersonMultipleTags = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND)
                .withStatus(Status.APPLIED).build();
        assertParseSuccess(parser,
                NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                        + TAG_DESC_HUSBAND + TAG_DESC_FRIEND
                        + " r/No role",
                new AddCommand(expectedPersonMultipleTags));
    }

    @Test
    public void parse_uppercasePrefixes_success() {
        Person expectedPerson = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND)
                .withStatus(Status.OFFERED).build();

        assertParseSuccess(parser, " N/" + VALID_NAME_BOB + " P/" + VALID_PHONE_BOB + " E/" + VALID_EMAIL_BOB
                + " A/" + VALID_ADDRESS_BOB + " S/Offered T/" + VALID_TAG_FRIEND + " r/No role",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_repeatedNonTagValue_failure() {
        String validExpectedPersonString = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND;

        // multiple names
        assertParseFailure(parser, NAME_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // multiple phones
        assertParseFailure(parser, PHONE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple emails
        assertParseFailure(parser, EMAIL_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // multiple addresses
        assertParseFailure(parser, ADDRESS_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // multiple fields repeated
        assertParseFailure(parser,
                validExpectedPersonString + PHONE_DESC_AMY + EMAIL_DESC_AMY + NAME_DESC_AMY + ADDRESS_DESC_AMY
                        + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_ADDRESS, PREFIX_EMAIL, PREFIX_PHONE));

        // invalid value followed by valid value

        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, INVALID_EMAIL_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, INVALID_PHONE_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid address
        assertParseFailure(parser, INVALID_ADDRESS_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // valid value followed by invalid value

        // invalid name
        assertParseFailure(parser, validExpectedPersonString + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, validExpectedPersonString + INVALID_EMAIL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, validExpectedPersonString + INVALID_PHONE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid address
        assertParseFailure(parser, validExpectedPersonString + INVALID_ADDRESS_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));
    }

    @Test
    public void parse_optionalFieldsMissing_success() {
        // zero tags
        Person expectedPerson = new PersonBuilder(AMY).withTags().withStatus(Status.APPLIED).build();
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY
                + " r/No role",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_usageExample_success() {
        String example = AddCommand.MESSAGE_USAGE.split("Example: ", 2)[1];
        Person expected = new PersonBuilder().withName("John Doe").withPhone("98765432")
                .withEmail("johnd@example.com").withAddress("311, Clementi Ave 2, #02-25")
                .withTags("backend", "Java").withStatus(Status.INTERVIEWING).build();

        assertParseSuccess(parser, example.substring(AddCommand.COMMAND_WORD.length()), new AddCommand(expected));
    }

    @Test
    public void parse_nameWithConnector_success() {
        Person expected = new PersonBuilder().withName("Ravi s/o Kumar").withPhone("91234567")
                .withEmail("ravi@example.com").withAddress("1 Road").withTags().withStatus(Status.OFFERED).build();

        assertParseSuccess(parser, " n/Ravi s/o Kumar p/91234567 e/ravi@example.com a/1 Road s/Offered r/No role",
                new AddCommand(expected));
    }

    @Test
    public void parse_nameWithMalayConnectorBeforeAddress_success() {
        Person expected = new PersonBuilder().withName("Siti a/p Ahmad").withPhone("91234567")
                .withEmail("siti@example.com").withAddress("1 Road").withTags().withStatus(Status.APPLIED).build();

        assertParseSuccess(parser, " n/Siti a/p Ahmad p/91234567 e/siti@example.com a/1 Road r/No role",
                new AddCommand(expected));
    }

    @Test
    public void parse_addressLikeConnectorAfterNameWithoutAddress_failure() {
        assertParseFailure(parser, " n/John a/L Street p/91234567 e/john@example.com r/No role",
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "a/L"));
    }

    @Test
    public void parse_connectorImmediatelyBeforeAddress_failure() {
        assertParseFailure(parser, " p/91234567 e/john@example.com n/John a/L Street a/9 Road r/No role",
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "a/L"));
        assertParseFailure(parser, " p/91234567 e/siti@example.com n/Siti a/p Ahmad a/1 Road r/No role",
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "a/p"));
    }

    @Test
    public void parse_addressBeforeConnectorName_success() {
        Person expected = new PersonBuilder().withName("Siti a/p Ahmad").withPhone("91234567")
                .withEmail("siti@example.com").withAddress("L Street").withTags().withStatus(Status.APPLIED).build();

        assertParseSuccess(parser, " a/L Street n/Siti a/p Ahmad p/91234567 e/siti@example.com r/No role",
                new AddCommand(expected));
    }

    @Test
    public void parse_punctuatedNameAndLabelledPhone_success() {
        Person expected = new PersonBuilder().withName("Anne O'Brien Jr.")
                .withPhone("+65 6123 4567 (Office) ext 89").withEmail("anne.o'brien@example.com")
                .withAddress("123 Example Road").withTags().withStatus(Status.APPLIED).build();

        assertParseSuccess(parser, " n/Anne O'Brien Jr. p/+65 6123 4567 (Office) ext 89"
                + " e/anne.o'brien@example.com a/123 Example Road r/No role", new AddCommand(expected));
    }

    @Test
    public void parse_eachExplicitStatus_success() {
        String details = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;
        for (Status status : Status.values()) {
            Person expected = new PersonBuilder(BOB).withTags().withStatus(status).build();

            assertParseSuccess(parser, details + " s/" + status + " r/No role", new AddCommand(expected));
        }
    }

    @Test
    public void parse_mixedCaseStatusWithWhitespace_success() {
        Person expected = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND).withStatus(Status.INTERVIEWING).build();

        assertParseSuccess(parser, " s/  iNtErViEwInG  " + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND + " r/No role", new AddCommand(expected));
    }

    @Test
    public void parse_invalidOrBlankStatus_failure() {
        String details = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;
        String message = Status.MESSAGE_CONSTRAINTS;

        assertParseFailure(parser, details + " s/Hired", message);
        assertParseFailure(parser, details + " s/", message);
        assertParseFailure(parser, details + " s/   ", message);
    }

    @Test
    public void parse_repeatedStatus_failure() {
        String details = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;
        String message = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_STATUS);

        assertParseFailure(parser, details + " s/Applied s/Offered", message);
        assertParseFailure(parser, details + " s/Applied s/Applied", message);
        assertParseFailure(parser, details + " s/Hired s/Applied", message);
    }

    @Test
    public void parse_invalidAddressBeforeInvalidStatus_reportsAddress() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_ADDRESS_DESC
                + " s/Hired", Address.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidStatusBeforeInvalidTag_reportsStatus() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + " s/Hired" + INVALID_TAG_DESC, Status.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // Missing name with a non-empty preamble retains preamble guidance.
        assertParseFailure(parser, VALID_NAME_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                expectedMessage);
        assertParseFailure(parser, PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB, "Missing n/NAME.");

        // missing phone prefix
        assertParseFailure(parser, NAME_DESC_BOB + VALID_PHONE_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                "Missing p/PHONE_NUMBER.");

        // missing email prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + VALID_EMAIL_BOB + ADDRESS_DESC_BOB,
                "Missing e/EMAIL.");

        // missing address prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + VALID_ADDRESS_BOB,
                "Missing a/ADDRESS.");

        // all prefixes missing
        assertParseFailure(parser, VALID_NAME_BOB + VALID_PHONE_BOB + VALID_EMAIL_BOB + VALID_ADDRESS_BOB,
                expectedMessage);
    }

    @Test
    public void parse_multipleMissingFields_listsAllInSyntaxOrder() {
        String allMissing = "Missing n/NAME, p/PHONE_NUMBER, e/EMAIL, a/ADDRESS.";
        assertParseFailure(parser, "", allMissing);
        assertParseFailure(parser, " s/Applied t/backend", allMissing);
        assertParseFailure(parser, EMAIL_DESC_BOB + NAME_DESC_BOB, "Missing p/PHONE_NUMBER, a/ADDRESS.");
        assertParseFailure(parser, " N/Bob E/bob@example.com", "Missing p/PHONE_NUMBER, a/ADDRESS.");
    }

    @Test
    public void parse_blankRequiredFields_reportsFieldValidation() {
        assertParseFailure(parser, " n/" + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + " p/" + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + " e/" + ADDRESS_DESC_BOB,
                Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " a/",
                Address.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_mixedErrors_preservesAgreedPrecedence() {
        assertParseFailure(parser, " preamble n/Bob no/note", AddCommandParser.MESSAGE_UNSUPPORTED_NOTE);
        assertParseFailure(parser, " preamble n/Bob\u4e2d", String.format(ParserUtil.MESSAGE_UNSUPPORTED_CHARACTER,
                "n/", 0x4e2d));
        assertParseFailure(parser, " preamble n/Bob", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                AddCommand.MESSAGE_USAGE));
        assertParseFailure(parser, NAME_DESC_BOB + NAME_DESC_BOB, "Missing p/PHONE_NUMBER, e/EMAIL, a/ADDRESS.");
        assertParseFailure(parser, " n/" + EMAIL_DESC_BOB, "Missing p/PHONE_NUMBER, a/ADDRESS.");
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Name.MESSAGE_CONSTRAINTS);

        // invalid phone
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Phone.MESSAGE_CONSTRAINTS);

        // invalid email
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC + ADDRESS_DESC_BOB
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Email.MESSAGE_CONSTRAINTS);

        // invalid address
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_ADDRESS_DESC
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Address.MESSAGE_CONSTRAINTS);

        // invalid tag
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + INVALID_TAG_DESC + VALID_TAG_FRIEND, Tag.MESSAGE_CONSTRAINTS);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_ADDRESS_DESC,
                Name.MESSAGE_CONSTRAINTS);

        // non-empty preamble
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_notePrefixAfterAddress_reportsUnsupportedNoteInsteadOfStoringIt() {
        assertAllCases(List.of("great candidate", "", " \t ", "first no/second"), note -> {
            assertParseFailure(parser, NOTE_REQUIRED_FIELDS + " no/" + note,
                    AddCommandParser.MESSAGE_UNSUPPORTED_NOTE);
        });
    }

    @Test
    public void parse_notePrefixAmongOtherFields_reportsUnsupportedNote() {
        List<String> inputs = List.of(" no/feedback" + NOTE_REQUIRED_FIELDS,
                " n/A no/feedback p/123 e/a@x.com a/Blk 1",
                NOTE_REQUIRED_FIELDS + " s/Applied no/feedback t/backend",
                NOTE_REQUIRED_FIELDS + " t/backend no/feedback");

        assertAllCases(inputs, args -> {
            assertParseFailure(parser, args, AddCommandParser.MESSAGE_UNSUPPORTED_NOTE);
        });
    }

    @Test
    public void parse_notePrefixAfterNonSpaceWhitespace_reportsUnsupportedNote() {
        assertAllCases(List.of("\t", "\n", " \t"), separator -> {
            assertParseFailure(parser, NOTE_REQUIRED_FIELDS + separator + "no/feedback",
                    AddCommandParser.MESSAGE_UNSUPPORTED_NOTE);
        });
    }

    @Test
    public void parse_notePrefixInOtherCase_reportsUnsupportedNote() {
        assertAllCases(List.of("NO/", "No/", "nO/"), prefix -> {
            assertParseFailure(parser, NOTE_REQUIRED_FIELDS + " " + prefix + "feedback",
                    AddCommandParser.MESSAGE_UNSUPPORTED_NOTE);
        });
    }

    @Test
    public void parse_notePrefixInsideAddressWord_preservesLiteralAddress() {
        AddCommand expected = new AddCommand(new PersonBuilder().withName("A").withPhone("123")
                .withEmail("a@x.com").withAddress("Blk 1 yes/no/feedback").build());

        assertParseSuccess(parser, NOTE_REQUIRED_FIELDS + " yes/no/feedback" + " r/No role", expected);
    }
}
