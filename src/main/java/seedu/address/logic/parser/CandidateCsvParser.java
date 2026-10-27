package seedu.address.logic.parser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/** Converts the strict exported CSV format into validated candidates without touching live data. */
public final class CandidateCsvParser {
    private static final List<String> HEADERS = List.of("name", "phone", "email", "address", "status", "tags", "note");
    private static final int MAX_TAGS = 100;
    private static final int MAX_TAG_LENGTH = 256;

    private CandidateCsvParser() {
    }

    /** Decodes strict UTF-8 and validates every record, accepting zero bytes as a zero-candidate import. */
    public static List<Person> parse(byte[] bytes) throws ParseException {
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
            throw new ParseException("CSV header must be name,phone,email,address,status,tags,note in that order.");
        }
        List<Person> persons = new ArrayList<>();
        for (int index = 1; index < records.size(); index++) {
            persons.add(parsePerson(records.get(index), index + 1));
        }
        return persons;
    }

    private static Person parsePerson(List<String> fields, int record) throws ParseException {
        return new Person(parseField(fields, record, 0, ParserUtil::parseName),
                parseField(fields, record, 1, ParserUtil::parsePhone),
                parseField(fields, record, 2, ParserUtil::parseEmail),
                parseField(fields, record, 3, ParserUtil::parseAddress),
                parseField(fields, record, 5, CandidateCsvParser::parseTags),
                parseField(fields, record, 4, ParserUtil::parseStatus),
                parseField(fields, record, 6, CandidateCsvParser::parseNote));
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
        return value.isBlank() ? Optional.empty() : Optional.of(new Note(value));
    }

    private static Set<Tag> parseTags(String value) throws ParseException {
        List<String> names = new ArrayList<>();
        try (JsonParser parser = new JsonFactory().createParser(value)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new ParseException("Tags must be a JSON array of strings.");
            }
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                if (parser.getCurrentToken() != JsonToken.VALUE_STRING) {
                    throw new ParseException("Tags must contain strings only, without nested arrays or objects.");
                }
                if (names.size() >= MAX_TAGS) {
                    throw new ParseException("Tags exceed the 100-entry limit.");
                }
                String name = parser.getText();
                if (name.codePointCount(0, name.length()) > MAX_TAG_LENGTH) {
                    throw new ParseException("Tag name exceeds 256 characters.");
                }
                names.add(name);
            }
            if (parser.nextToken() != null) {
                throw new ParseException("Unexpected content after the tags array.");
            }
        } catch (IOException e) {
            // Jackson messages can contain source values; keep candidate contents out of errors.
            throw new ParseException("Malformed tags JSON; expected an array of strings.");
        }
        return ParserUtil.parseTags(names);
    }

    @FunctionalInterface
    private interface FieldParser<T> {
        T parse(String value) throws ParseException;
    }
}
