package seedu.address.logic.parser;

import java.util.ArrayList;
import java.util.List;

import seedu.address.logic.parser.exceptions.ParseException;

/** Parses only fully quoted, seven-column CSV with CRLF record separators. */
final class StrictCsvParser {
    static final int MAX_FIELD_LENGTH = 65_536;
    static final int MAX_CANDIDATES = 10_000;
    private static final int COLUMN_COUNT = 7;

    private final String input;
    private int position;
    private int record = 1;

    StrictCsvParser(String input) {
        this.input = input;
    }

    List<List<String>> parse() throws ParseException {
        List<List<String>> records = new ArrayList<>();
        while (position < input.length()) {
            if (records.size() > MAX_CANDIDATES) {
                throw error("Candidate count exceeds 10,000.");
            }
            List<String> fields = new ArrayList<>();
            for (int column = 0; column < COLUMN_COUNT; column++) {
                fields.add(readField());
                expect(column == COLUMN_COUNT - 1 ? "\r\n" : ",");
            }
            records.add(fields);
            record++;
        }
        return records;
    }

    private String readField() throws ParseException {
        expect("\"");
        StringBuilder value = new StringBuilder();
        int length = 0;
        while (position < input.length()) {
            int codePoint = input.codePointAt(position);
            position += Character.charCount(codePoint);
            if (codePoint == '"') {
                if (position >= input.length() || input.charAt(position) != '"') {
                    return value.toString();
                }
                position++;
            }
            if (++length > MAX_FIELD_LENGTH) {
                throw error("Field exceeds 65,536 characters.");
            }
            value.appendCodePoint(codePoint);
        }
        throw error("Unterminated quoted field.");
    }

    private void expect(String token) throws ParseException {
        if (!input.startsWith(token, position)) {
            throw error("Expected fully quoted seven-column CSV with CRLF record endings.");
        }
        position += token.length();
    }

    private ParseException error(String reason) {
        return new ParseException("CSV record " + record + ": " + reason);
    }
}
