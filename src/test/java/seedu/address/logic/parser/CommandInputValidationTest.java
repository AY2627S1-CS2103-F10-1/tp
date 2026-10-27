package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.StatusCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Note;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;

/** Exercises the character policy through the full command parser, including pre-trimming boundaries. */
public class CommandInputValidationTest {
    private static final String ADD_ARGUMENTS = " n/Alice p/123 e/alice@example.com a/Block 1 t/backend";
    private static final String[] FIELD_PREFIXES = {"n/", "p/", "e/", "a/", "t/"};
    private static final List<String> UNSUPPORTED_CHARACTERS = List.of(
            "\u0000", "\u001F", "\u007F", "\t", "\r", "\n", "\u00A0", "\u200B", "é", "😀");

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void verifyPrintableAscii_allPrintableCharactersAndEmpty_accepts() throws Exception {
        StringBuilder printable = new StringBuilder();
        for (int codePoint = 0x20; codePoint <= 0x7E; codePoint++) {
            printable.appendCodePoint(codePoint);
        }
        assertDoesNotThrow(() -> ParserUtil.verifyPrintableAscii(printable.toString(), "Test field"));
        assertDoesNotThrow(() -> ParserUtil.verifyPrintableAscii("", "Test field"));
    }

    @Test
    public void verifyPrintableAscii_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.verifyPrintableAscii(null, "Field"));
        assertThrows(NullPointerException.class, () -> ParserUtil.verifyPrintableAscii("text", null));
    }

    @Test
    public void parseCommand_unsupportedCharactersInEveryField_rejectsBeforeFieldValidation() {
        for (String character : UNSUPPORTED_CHARACTERS) {
            for (String prefix : FIELD_PREFIXES) {
                String expected = unsupportedMessage(prefix, character);
                assertThrows(ParseException.class, expected, () ->
                        parser.parseCommand("add" + ADD_ARGUMENTS + " " + prefix + character));
                assertThrows(ParseException.class, expected, () ->
                        parser.parseCommand("edit 1 " + prefix + character));
            }
            assertThrows(ParseException.class, unsupportedMessage("no/", character), () ->
                    parser.parseCommand("note 1 no/" + character));
            assertThrows(ParseException.class, unsupportedMessage("Search keywords", character), () ->
                    parser.parseCommand("find " + character));
        }
    }

    @Test
    public void parseCommand_unsupportedCharactersAtValueBoundaries_rejectsWithoutTrimming() {
        for (String character : UNSUPPORTED_CHARACTERS) {
            for (String value : List.of(character + "Block", "Bl" + character + "ock", "Block" + character)) {
                assertThrows(ParseException.class, unsupportedMessage("a/", character), () ->
                        parser.parseCommand("edit 1 a/" + value));
                assertThrows(ParseException.class, unsupportedMessage("no/", character), () ->
                        parser.parseCommand("note 1 no/" + value));
                assertThrows(ParseException.class, unsupportedMessage("Search keywords", character), () ->
                        parser.parseCommand("find " + value));
            }
        }
    }

    @Test
    public void parseCommand_unsupportedEarlierTag_rejectsEveryRepeatedValue() {
        assertThrows(ParseException.class, unsupportedMessage("t/", "\u200B"), () ->
                parser.parseCommand("edit 1 t/\u200B t/backend"));
    }

    @Test
    public void parseCommand_excludedArguments_retainsDedicatedValidation() throws Exception {
        Index index = Index.fromOneBased(1);
        assertEquals(new DeleteCommand(index), parser.parseCommand("delete \t1\n"));
        assertEquals(new ClearCommand(true), parser.parseCommand("clear \tCoNfIrM\n"));
        assertEquals(new StatusCommand(index, Status.OFFERED), parser.parseCommand("status 1 s/\tOffered\n"));
        assertEquals(new FilterCommand(Status.OFFERED), parser.parseCommand("filter s/\tOffered\n"));
        assertEquals(new NoteCommand(index), parser.parseCommand("note \t1 no/   "));
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setStatus(Status.OFFERED);
        assertEquals(new EditCommand(index, descriptor), parser.parseCommand("edit \t1 s/\tOffered\n"));
    }

    @Test
    public void parseNote_printablePunctuation_preservesValue() throws Exception {
        String text = "Quotes: \"' backslash: \\ commas, brackets[]{} punctuation!@#$%^&*()_+-=;:<>?/~`";
        assertEquals(new Note(text), ParserUtil.parseNote(text));
    }

    @Test
    public void parseFields_printableButInvalidValues_retainsFieldRules() {
        assertThrows(ParseException.class, Phone.MESSAGE_CONSTRAINTS, () -> ParserUtil.parsePhone("Office: 12"));
        assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseEmail("not an email"));
    }

    @Test
    public void parseFields_unsupportedSurroundingWhitespace_rejectsBeforeTrimming() {
        String value = "\tvalue\n";
        assertThrows(ParseException.class, unsupportedMessage("Name", "\t"), () -> ParserUtil.parseName(value));
        assertThrows(ParseException.class, unsupportedMessage("Phone", "\t"), () -> ParserUtil.parsePhone(value));
        assertThrows(ParseException.class, unsupportedMessage("Email", "\t"), () -> ParserUtil.parseEmail(value));
        assertThrows(ParseException.class, unsupportedMessage("Address", "\t"), () -> ParserUtil.parseAddress(value));
        assertThrows(ParseException.class, unsupportedMessage("Tag", "\t"), () -> ParserUtil.parseTag(value));
        assertThrows(ParseException.class, unsupportedMessage("Note", "\t"), () -> ParserUtil.parseNote(value));
    }

    private static String unsupportedMessage(String field, String character) {
        return String.format(ParserUtil.MESSAGE_UNSUPPORTED_CHARACTER, field, character.codePointAt(0));
    }
}
