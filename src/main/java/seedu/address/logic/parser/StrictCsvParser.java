package seedu.address.logic.parser;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.CandidateCsvFormat;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses candidate CSV with strict quoting and column counts, accepting LF and CRLF separators. */
final class StrictCsvParser {
    static final int MAX_FIELD_LENGTH = 65_536;
    static final int MAX_CANDIDATES = 10_000;
    private static final int COLUMN_COUNT = CandidateCsvFormat.HEADERS.size();

    private final String input;
    private int position;
    private int record = 1;

    StrictCsvParser(String input) {
        this.input = input;
    }

    List<List<String>> parse() throws ParseException {
        List<List<String>> records = new ArrayList<>();
        while (position < input.length()) {
            if (isLineEnding()) {
                readTrailingBlankLines(records.isEmpty());
                break;
            }
            if (records.size() > MAX_CANDIDATES) {
                throw error("Candidate count exceeds 10,000.");
            }
            List<String> fields = new ArrayList<>();
            for (int column = 0; column < COLUMN_COUNT; column++) {
                fields.add(readField());
                if (column < COLUMN_COUNT - 1) {
                    expect(',');
                }
            }
            if (position < input.length()) {
                readLineEnding();
            }
            records.add(fields);
            record++;
        }
        return records;
    }

    /** Reads a field without consuming its delimiter, counting decoded Unicode code points. */
    private String readField() throws ParseException {
        boolean quoted = position < input.length() && input.charAt(position) == '"';
        if (quoted) {
            position++;
        }
        StringBuilder value = new StringBuilder();
        int length = 0;
        while (position < input.length()) {
            int codePoint = input.codePointAt(position);
            if (!quoted && (codePoint == ',' || isLineEnding())) {
                return value.toString();
            }
            position += Character.charCount(codePoint);
            if (codePoint == '"') {
                if (!quoted) {
                    throw error("Quotes must enclose the entire field; escape embedded quotes by doubling them.");
                }
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
        if (quoted) {
            throw error("Unterminated quoted field.");
        }
        return value.toString();
    }

    /** Allows empty lines only after at least one complete record and only at the end of input. */
    private void readTrailingBlankLines(boolean noRecords) throws ParseException {
        if (noRecords) {
            throw error("CSV header is required before blank lines.");
        }
        while (position < input.length()) {
            if (!isLineEnding()) {
                throw error("Blank lines are allowed only at the end of the file.");
            }
            readLineEnding();
        }
    }

    private boolean isLineEnding() {
        return position < input.length() && (input.charAt(position) == '\r' || input.charAt(position) == '\n');
    }

    private void readLineEnding() throws ParseException {
        if (position < input.length() && input.charAt(position) == '\r') {
            position++;
        }
        expect('\n');
    }

    private void expect(char token) throws ParseException {
        if (position >= input.length() || input.charAt(position) != token) {
            throw error("Expected seven-column CSV with LF or CRLF record endings and valid field quoting.");
        }
        position++;
    }

    private ParseException error(String reason) {
        return new ParseException("CSV record " + record + ": " + reason);
    }
}
