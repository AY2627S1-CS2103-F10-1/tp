package seedu.address.logic.parser;

import static seedu.address.commons.core.CandidateCsvFormat.HEADERS;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.role.Role;
import seedu.address.model.role.RoleCatalog;
import seedu.address.model.tag.Tag;

/** Converts the strict exported CSV format into validated candidates without touching live data. */
public final class CandidateCsvParser {
    private static final int MAX_NAMES = 100;
    private static final int MAX_NAME_LENGTH = 256;

    private CandidateCsvParser() {
    }

    /**
     * Decodes strict UTF-8 and validates every record, accepting zero bytes as a zero-candidate import.
     * Role names missing from {@code roleCatalog} become new roles, shared by every record that names them.
     */
    public static List<Person> parse(byte[] bytes, RoleCatalog roleCatalog) throws ParseException {
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            throw new ParseException("Input must be valid UTF-8.");
        }
        if (text.startsWith("\ufeff")) {
            // Export includes this encoding marker for spreadsheet compatibility; strip only the leading marker.
            text = text.substring(1);
            if (text.isEmpty()) {
                throw new ParseException("CSV header is required after the UTF-8 BOM.");
            }
        }
        List<List<String>> records = new StrictCsvParser(text).parse();
        if (records.isEmpty()) {
            return List.of();
        }
        if (!records.getFirst().equals(HEADERS)) {
            throw new ParseException("CSV header must be " + String.join(",", HEADERS) + " in that order.");
        }
        Map<String, Role> knownRoles = new HashMap<>();
        roleCatalog.getRoles().forEach(role -> knownRoles.put(Role.toLookupKey(role.getName()), role));
        List<Person> persons = new ArrayList<>();
        for (int index = 1; index < records.size(); index++) {
            persons.add(parsePerson(records.get(index), index + 1, knownRoles));
        }
        return persons;
    }

    private static Person parsePerson(List<String> fields, int record, Map<String, Role> knownRoles)
            throws ParseException {
        return new Person(parseField(fields, record, 0, ParserUtil::parseName),
                parseField(fields, record, 1, ParserUtil::parsePhone),
                parseField(fields, record, 2, ParserUtil::parseEmail),
                parseField(fields, record, 3, value -> new Address(value.trim())),
                parseField(fields, record, 5, CandidateCsvParser::parseTags),
                parseField(fields, record, 4, ParserUtil::parseStatus),
                parseField(fields, record, 6, CandidateCsvParser::parseNote),
                parseField(fields, record, 7, value -> parseRoles(value, knownRoles)));
    }

    private static <T> T parseField(List<String> fields, int record, int column, FieldParser<T> parser)
            throws ParseException {
        try {
            return parser.parse(fields.get(column));
        } catch (ParseException | IllegalArgumentException e) {
            throw new ParseException("CSV record " + record + ", field " + HEADERS.get(column) + ": " + e.getMessage());
        }
    }

    private static Optional<Note> parseNote(String value) {
        // CSV also restores legacy persisted text, which is not restricted to the command character allowlist.
        return value.isBlank() ? Optional.empty() : Optional.of(new Note(value.strip()));
    }

    private static Set<Tag> parseTags(String value) throws ParseException {
        return ParserUtil.parseTags(parseNames(value, "Tags"));
    }

    /** Matches role names as role commands do, creating each unknown role once per import. */
    private static Set<Role> parseRoles(String value, Map<String, Role> knownRoles) throws ParseException {
        List<String> names = parseNames(value, "Roles");
        if (names.isEmpty()) {
            throw new ParseException("Each candidate needs at least one role; "
                    + "use [\"No role\"] if no role applies.");
        }
        Set<Role> roles = new HashSet<>();
        for (String name : names) {
            String roleName = ParserUtil.parseRoleName(name);
            roles.add(knownRoles.computeIfAbsent(Role.toLookupKey(roleName), key -> Role.create(roleName)));
        }
        return roles;
    }

    /** Reads a flat JSON array of strings, checking its limits before any trimming or deduplication. */
    private static List<String> parseNames(String value, String field) throws ParseException {
        String arrayName = field.toLowerCase(Locale.ROOT);
        List<String> names = new ArrayList<>();
        try (JsonParser parser = new JsonFactory().createParser(value)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new ParseException(field + " must be a JSON array of strings.");
            }
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                if (parser.getCurrentToken() != JsonToken.VALUE_STRING) {
                    throw new ParseException(field
                            + " must contain strings only, without nested arrays or objects.");
                }
                if (names.size() >= MAX_NAMES) {
                    throw new ParseException(field + " exceed the 100-entry limit.");
                }
                String name = parser.getText();
                if (name.codePointCount(0, name.length()) > MAX_NAME_LENGTH) {
                    throw new ParseException(field + " must not contain a name longer than 256 characters.");
                }
                names.add(name);
            }
            if (parser.nextToken() != null) {
                throw new ParseException("Unexpected content after the " + arrayName + " array.");
            }
        } catch (IOException e) {
            // Jackson messages can contain source values; keep candidate contents out of errors.
            throw new ParseException("Malformed " + arrayName + " JSON; expected an array of strings.");
        }
        return names;
    }

    @FunctionalInterface
    private interface FieldParser<T> {
        T parse(String value) throws ParseException;
    }
}
