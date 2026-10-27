package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
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

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_EMPTY_ROLE =
            "Role name cannot be empty. Use r/No role if no role applies.";

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    public static final String MESSAGE_UNSUPPORTED_CHARACTER =
            "%s contains unsupported character U+%04X. Use printable ASCII only (U+0020-U+007E).";

    private static final int FIRST_PRINTABLE_ASCII = 0x20;
    private static final int LAST_PRINTABLE_ASCII = 0x7E;

    // ArgumentTokenizer only recognises prefixes after a space, so a tab before no/ must be caught separately.
    private static final Pattern NOTE_PREFIX_PATTERN =
            Pattern.compile("(^|\\s)" + Pattern.quote(PREFIX_NOTE.getPrefix()), Pattern.CASE_INSENSITIVE);

    /** Parses a role display name using the model's shared validation rules. */
    public static String parseRoleName(String name) throws ParseException {
        String preparedName = Role.prepareName(name);
        if (!Role.isValidName(preparedName)) {
            throw new ParseException(Role.MESSAGE_CONSTRAINTS);
        }
        if (!Role.hasNoReservedSyntax(preparedName)) {
            throw new ParseException(Role.MESSAGE_RESERVED_SYNTAX);
        }
        return preparedName;
    }

    /** Parses repeated role names without resolving catalogue membership. */
    public static Set<String> parseRoleNames(Collection<String> names) throws ParseException {
        requireNonNull(names);
        Set<String> roles = new HashSet<>();
        for (String name : names) {
            if (Role.prepareName(name).isEmpty()) {
                throw new ParseException(MESSAGE_EMPTY_ROLE);
            }
            roles.add(parseRoleName(name));
        }
        return roles;
    }

    /**
     * Rejects the first non-printable-ASCII code point without modifying the supplied value.
     * Empty values are left to the existing command-specific validation.
     *
     * @throws ParseException If the value contains an unsupported character.
     */
    public static void verifyPrintableAscii(String value, String field) throws ParseException {
        requireNonNull(value);
        requireNonNull(field);
        for (int offset = 0; offset < value.length();) {
            int codePoint = value.codePointAt(offset);
            if (codePoint < FIRST_PRINTABLE_ASCII || codePoint > LAST_PRINTABLE_ASCII) {
                throw new ParseException(String.format(MESSAGE_UNSUPPORTED_CHARACTER, field, codePoint));
            }
            offset += Character.charCount(codePoint);
        }
    }

    /**
     * Rejects {@code args} if it contains the note prefix, so that commands without a note field
     * do not silently store note text in another field.
     *
     * @param args Arguments of a command that does not accept a note.
     * @param message Error message that points the user to the note command.
     * @throws ParseException If {@code args} contains the note prefix after whitespace or at its start.
     */
    public static void verifyNoNotePrefix(String args, String message) throws ParseException {
        requireNonNull(args);
        if (NOTE_PREFIX_PATTERN.matcher(args).find()) {
            throw new ParseException(message);
        }
    }

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses {@code oneBasedIndex} like {@link #parseIndex(String)}, but explains why a whole number is rejected.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @param oneBasedIndex Index typed by the user, from the displayed list.
     * @param formatErrorMessage Message used when {@code oneBasedIndex} is not a whole number.
     * @return The parsed index.
     * @throws ParseException If {@code oneBasedIndex} is not a whole number, is zero or negative,
     *     or is too large to identify any displayed candidate.
     */
    public static Index parseDisplayedIndex(String oneBasedIndex, String formatErrorMessage)
            throws ParseException {
        requireNonNull(oneBasedIndex);
        String trimmedIndex = oneBasedIndex.trim();
        if (!isWholeNumber(trimmedIndex)) {
            throw new ParseException(formatErrorMessage);
        }
        if (isZeroOrNegative(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            // Too large for an int, so no displayed list can be this long.
            throw new ParseException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    private static boolean isWholeNumber(String text) {
        return text.matches("-?\\d+");
    }

    private static boolean isZeroOrNegative(String wholeNumber) {
        return wholeNumber.startsWith("-") || wholeNumber.matches("0+");
    }

    /**
     * Parses a note, trimming its surrounding whitespace and preserving its internal contents.
     *
     * @throws ParseException If the note contains unsupported characters, is blank or exceeds the length limit.
     */
    public static Note parseNote(String note) throws ParseException {
        verifyPrintableAscii(note, "Note");
        String strippedNote = note.strip();
        if (strippedNote.isBlank()) {
            throw new ParseException(Note.MESSAGE_BLANK);
        }
        if (!Note.isValidNote(strippedNote)) {
            throw new ParseException(Note.MESSAGE_TOO_LONG);
        }
        return new Note(strippedNote);
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        verifyPrintableAscii(name, "Name");
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        verifyPrintableAscii(phone, "Phone");
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        verifyPrintableAscii(address, "Address");
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        verifyPrintableAscii(email, "Email");
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String status} into a {@code Status}, ignoring case.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code status} is invalid.
     */
    public static Status parseStatus(String status) throws ParseException {
        requireNonNull(status);
        String trimmedStatus = status.trim();
        try {
            return Status.fromString(trimmedStatus);
        } catch (IllegalArgumentException e) {
            throw new ParseException(Status.MESSAGE_CONSTRAINTS, e);
        }
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        verifyPrintableAscii(tag, "Tag");
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }
}
