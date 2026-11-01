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
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TestCaseUtil.assertAllCases;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {

    private static final String TAG_EMPTY = " " + PREFIX_TAG;

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, VALID_NAME_AMY, MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // negative index
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, ParserUtil.MESSAGE_INVALID_INDEX);

        // zero index
        assertParseFailure(parser, "0" + NAME_DESC_AMY, ParserUtil.MESSAGE_INVALID_INDEX);

        // index too large for any displayed list
        assertParseFailure(parser, "99999999999" + NAME_DESC_AMY, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        // invalid arguments being parsed as preamble
        assertParseFailure(parser, "1 some random string", MESSAGE_INVALID_FORMAT);

        // invalid prefix being parsed as preamble
        assertParseFailure(parser, "1 i/ string", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS); // invalid name
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS); // invalid phone
        assertParseFailure(parser, "1" + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS); // invalid email
        assertParseFailure(parser, "1" + INVALID_ADDRESS_DESC, Address.MESSAGE_CONSTRAINTS); // invalid address
        assertParseFailure(parser, "1 " + PREFIX_STATUS + "unknown", Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 " + PREFIX_STATUS, Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_TAG_DESC, Tag.MESSAGE_CONSTRAINTS); // invalid tag

        // invalid phone followed by valid email
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC + EMAIL_DESC_AMY, Phone.MESSAGE_CONSTRAINTS);

        // while parsing {@code PREFIX_TAG} alone will reset the tags of the {@code Person} being edited,
        // parsing it together with a valid tag results in error
        assertParseFailure(parser, "1" + TAG_DESC_FRIEND + TAG_DESC_HUSBAND + TAG_EMPTY, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + TAG_DESC_FRIEND + TAG_EMPTY + TAG_DESC_HUSBAND, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + TAG_EMPTY + TAG_DESC_FRIEND + TAG_DESC_HUSBAND, Tag.MESSAGE_CONSTRAINTS);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser, "1" + INVALID_NAME_DESC + INVALID_EMAIL_DESC + VALID_ADDRESS_AMY + VALID_PHONE_AMY,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + TAG_DESC_HUSBAND
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + NAME_DESC_AMY + TAG_DESC_FRIEND
                + " " + PREFIX_STATUS + "Interviewing";

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY).withAddress(VALID_ADDRESS_AMY)
                .withStatus(Status.INTERVIEWING).withTags(VALID_TAG_HUSBAND, VALID_TAG_FRIEND).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_statusOnlyCaseInsensitive_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withStatus(Status.INTERVIEWING).build();

        assertParseSuccess(parser, "3 " + PREFIX_STATUS + "interviewing",
                new EditCommand(INDEX_THIRD_PERSON, descriptor));
    }

    @Test
    public void parse_statusWithOtherField_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withStatus(Status.OFFERED).build();

        assertParseSuccess(parser, "1" + PHONE_DESC_BOB + " " + PREFIX_STATUS + "Offered",
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + EMAIL_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_addressLikeConnectorAfterNameWithoutAddress_failure() {
        // Regression: "a/L Street" was silently read as part of the name, leaving the address unchanged.
        assertParseFailure(parser, "1 n/John a/L Street",
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "a/L"));
        assertParseFailure(parser, "1 n/John a/P Road",
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "a/P"));
        assertParseFailure(parser, "1 n/Siti a/p Ahmad",
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "a/p"));
    }

    @Test
    public void parse_twoAddressLikeConnectorsWithoutAddress_failure() {
        // Regression: each a/ counted as the other's address, so both were swallowed into the name.
        assertParseFailure(parser, "1 n/Siti a/p Ahmad a/L Street",
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "a/p"));
        assertParseFailure(parser, "1 n/Siti a/l Ahmad a/P Road",
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "a/l"));
        assertParseFailure(parser, "1 n/Ravi s/o Kumar a/L Street",
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "a/L"));
    }

    @Test
    public void parse_connectorImmediatelyBeforeAddress_failure() {
        assertParseFailure(parser, "1 n/John a/L Street a/9 Road",
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "a/L"));
        assertParseFailure(parser, "1 n/John A/P Road A/9 Road",
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "A/P"));
        assertParseFailure(parser, "1 n/Siti a/p Ahmad a/1 Road",
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "a/p"));
    }

    @Test
    public void parse_addressLikeAddressBeforeConnectorName_editsAddressAndName() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("Siti a/p Ahmad")
                .withAddress("L Street").build();

        assertParseSuccess(parser, "1 a/L Street n/Siti a/p Ahmad", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_addressBeforeName_editsAddressAndName() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("John")
                .withAddress("L Street").build();

        assertParseSuccess(parser, "1 a/L Street n/John", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_addressLikeConnectorWithSeparatedAddress_keepsConnectorInName() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("Siti a/p Ahmad")
                .withAddress("1 Road").build();

        assertParseSuccess(parser, "1 n/Siti a/p Ahmad s/Applied a/1 Road",
                new EditCommand(INDEX_FIRST_PERSON,
                        new EditPersonDescriptorBuilder(descriptor).withStatus(Status.APPLIED).build()));
        assertParseSuccess(parser, "1 a/1 Road n/Siti a/p Ahmad", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_connectorWithTwoExplicitAddresses_failure() {
        assertParseFailure(parser, "1 a/1 Road n/Siti a/p Ahmad a/9 Road",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));
    }

    @Test
    public void parse_statusConnectorWithoutAddress_keepsConnectorInName() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("Ravi s/o Kumar").build();

        assertParseSuccess(parser, "1 n/Ravi s/o Kumar", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        // name
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + NAME_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // phone
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // email
        userInput = targetIndex.getOneBased() + EMAIL_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // address
        userInput = targetIndex.getOneBased() + ADDRESS_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withAddress(VALID_ADDRESS_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // tags
        userInput = targetIndex.getOneBased() + TAG_DESC_FRIEND;
        descriptor = new EditPersonDescriptorBuilder().withTags(VALID_TAG_FRIEND).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        // More extensive testing of duplicate parameter detections is done in
        // AddCommandParserTest#parse_repeatedNonTagValue_failure()

        // valid followed by invalid
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + PHONE_DESC_BOB;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid followed by valid
        userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + INVALID_PHONE_DESC;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple valid fields repeated
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY
                + TAG_DESC_FRIEND + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY + TAG_DESC_FRIEND
                + PHONE_DESC_BOB + ADDRESS_DESC_BOB + EMAIL_DESC_BOB + TAG_DESC_HUSBAND;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS));

        // multiple invalid values
        userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + INVALID_ADDRESS_DESC + INVALID_EMAIL_DESC
                + INVALID_PHONE_DESC + INVALID_ADDRESS_DESC + INVALID_EMAIL_DESC;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS));
    }

    @Test
    public void parse_repeatedStatus_failure() {
        assertParseFailure(parser, "1 " + PREFIX_STATUS + "Applied " + PREFIX_STATUS + "invalid",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_STATUS));
    }

    @Test
    public void parse_uppercasePrefixes_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withAddress("12 Kent Ridge Drive")
                .withStatus(Status.OFFERED).withEmail(VALID_EMAIL_AMY).build();

        assertParseSuccess(parser, "1 A/12 Kent Ridge Drive S/Offered E/" + VALID_EMAIL_AMY,
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_uppercaseRepeatedPrefix_failure() {
        assertParseFailure(parser, "1 s/Applied S/Offered",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_STATUS));
    }

    @Test
    public void parse_notePrefix_failure() {
        List<String> inputs = List.of("1 no/Strong candidate", "1 no/", "1 NO/Strong candidate",
                "1" + ADDRESS_DESC_AMY + " no/hello", "1" + ADDRESS_DESC_AMY + "\tno/hello",
                "no/Strong candidate");

        assertAllCases(inputs, args -> {
            assertParseFailure(parser, args, EditCommandParser.MESSAGE_UNSUPPORTED_NOTE);
        });
    }

    @Test
    public void parse_notePrefixInsideWord_preservesLiteralAddress() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withAddress("Blk 1 yes/no/feedback").build();

        assertParseSuccess(parser, "1 a/Blk 1 yes/no/feedback", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_resetTags_success() {
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + TAG_EMPTY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags().build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }
}
