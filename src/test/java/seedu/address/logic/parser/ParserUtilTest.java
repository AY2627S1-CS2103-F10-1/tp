package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;
import seedu.address.model.role.Role;
import seedu.address.model.tag.Tag;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "65+1234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "123456";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_TAG_1 = "friend";
    private static final String VALID_TAG_2 = "neighbour";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseRoleName_paddedDisplayName_preservesCapitalizationAndInternalSpaces() throws Exception {
        assertEquals("Software  Engineer", ParserUtil.parseRoleName("\u2003 Software  Engineer \t"));
    }

    @Test
    public void parseRoleName_invalidName_throwsSpecificError() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseRoleName(null));
        for (String name : List.of("", " ", "Software\tEngineer", "Line\nBreak")) {
            assertThrows(ParseException.class, Role.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseRoleName(name));
        }
        assertThrows(ParseException.class, Role.MESSAGE_RESERVED_SYNTAX, () ->
                ParserUtil.parseRoleName("Senior N/Engineer"));
    }

    @Test
    public void parseNote_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseNote(null));
    }

    @Test
    public void parseNote_blank_throwsParseException() {
        for (String note : List.of("", " ", "   ")) {
            assertThrows(ParseException.class, Note.MESSAGE_BLANK, () -> ParserUtil.parseNote(note));
        }
    }

    @Test
    public void parseNote_surroundingWhitespace_preservesInternalContents() throws Exception {
        String contents = "Strong on C++,  weak on SQL; [round 2] / follow-up YES";
        assertEquals(new Note(contents), ParserUtil.parseNote("   " + contents + "   "));
    }

    @Test
    public void parseNote_lengthBoundaries_validatesStrippedCodePointCount() throws Exception {
        String contents = "a".repeat(Note.MAX_LENGTH);
        assertEquals(new Note(contents), ParserUtil.parseNote("   " + contents + "   "));
        assertThrows(ParseException.class, Note.MESSAGE_TOO_LONG, () ->
                ParserUtil.parseNote("   " + contents + "a   "));
    }

    @Test
    public void parseNote_unsupportedCharacters_throwsParseException() {
        for (String character : List.of("\t", "\r", "\n", "\u2003", "\u4e2d", "\ud83d\ude00")) {
            String message = String.format(ParserUtil.MESSAGE_UNSUPPORTED_CHARACTER,
                    "Note", character.codePointAt(0));
            for (String note : List.of(character, character + "text", "text" + character, "a" + character + "b")) {
                assertThrows(ParseException.class, message, () -> ParserUtil.parseNote(note));
            }
        }
    }

    @Test
    public void parseStatus_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseStatus(null));
    }

    @Test
    public void parseStatus_invalidValue_throwsParseException() {
        for (String status : List.of("", WHITESPACE, "Unknown", "Short listed")) {
            assertThrows(ParseException.class, Status.MESSAGE_CONSTRAINTS, () ->
                    ParserUtil.parseStatus(status));
        }
    }

    @Test
    public void parseStatus_validValues_returnsStatus() throws Exception {
        for (Status status : Status.values()) {
            assertEquals(status, ParserUtil.parseStatus(status.toString()));
            assertEquals(status, ParserUtil.parseStatus(status.toString().toLowerCase(Locale.ROOT)));
            assertEquals(status, ParserUtil.parseStatus(status.toString().toUpperCase(Locale.ROOT)));
        }
    }

    @Test
    public void parseStatus_mixedCaseWithWhitespace_returnsStatus() throws Exception {
        assertEquals(Status.INTERVIEWING, ParserUtil.parseStatus(WHITESPACE + "iNtErViEwInG" + WHITESPACE));
    }

    @Test
    public void verifyNoNotePrefix_notePrefixPresent_throwsParseException() {
        for (String args : List.of("no/hi", " no/", " a/Blk 1 no/hi", " a/Blk 1\tno/hi", " NO/hi", " No/hi")) {
            assertThrows(ParseException.class, "message", () -> ParserUtil.verifyNoNotePrefix(args, "message"));
        }
    }

    @Test
    public void verifyNoNotePrefix_notePrefixAbsentOrInsideWord_doesNotThrow() throws Exception {
        for (String args : List.of("", " a/Blk 1", " a/Blk 1 yes/no/feedback", " a/techno/house", " n/Nolan")) {
            ParserUtil.verifyNoNotePrefix(args, "message");
        }
    }

    @Test
    public void verifyNoNotePrefix_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.verifyNoNotePrefix(null, "message"));
    }

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseDisplayedIndex_notWholeNumber_throwsFormatMessage() {
        for (String index : List.of("", WHITESPACE, "a", "10 a", "1.5", "+1", "1-", "--1")) {
            assertThrows(ParseException.class, "format", () -> ParserUtil.parseDisplayedIndex(index, "format"));
        }
    }

    @Test
    public void parseDisplayedIndex_zeroOrNegative_throwsInvalidIndexMessage() {
        for (String index : List.of("0", "000", "-0", "-1", "-99999999999")) {
            assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, () ->
                    ParserUtil.parseDisplayedIndex(index, "format"));
        }
    }

    @Test
    public void parseDisplayedIndex_tooLarge_throwsInvalidDisplayedIndexMessage() {
        for (String index : List.of(Long.toString(Integer.MAX_VALUE + 1L), "99999999999999999999")) {
            assertThrows(ParseException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                    ParserUtil.parseDisplayedIndex(index, "format"));
        }
    }

    @Test
    public void parseDisplayedIndex_validInput_success() throws Exception {
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseDisplayedIndex("1", "format"));
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseDisplayedIndex(WHITESPACE + "001" + WHITESPACE, "format"));
        assertEquals(Index.fromOneBased(Integer.MAX_VALUE),
                ParserUtil.parseDisplayedIndex(Integer.toString(Integer.MAX_VALUE), "format"));
    }

    @Test
    public void parseDisplayedIndex_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseDisplayedIndex(null, "format"));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = "  " + VALID_NAME + "  ";
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = "  " + VALID_PHONE + "  ";
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        String addressWithWhitespace = "  " + VALID_ADDRESS + "  ";
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = "  " + VALID_EMAIL + "  ";
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        String tagWithWhitespace = "  " + VALID_TAG_1 + "  ";
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(tagWithWhitespace));
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));

        assertEquals(expectedTagSet, actualTagSet);
    }
}
