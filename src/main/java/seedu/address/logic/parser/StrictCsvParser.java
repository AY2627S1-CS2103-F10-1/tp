package seedu.address.logic.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
            // Records include the header, so allow one more record than MAX_CANDIDATES.
            if (records.size() > MAX_CANDIDATES) {
                throw error(String.format(Locale.ROOT, "Candidate count exceeds %,d.", MAX_CANDIDATES));
            }
            records.add(readRecord());
            record++;
        }
        return records;
    }

    /** Reads one record, including its separators and optional line ending. */
    private List<String> readRecord() throws ParseException {
        List<String> fields = new ArrayList<>();
        for (int column = 0; column < COLUMN_COUNT; column++) {
            fields.add(readField(column));
            if (column < COLUMN_COUNT - 1) {
                readColumnSeparator(column);
            }
        }
        if (position < input.length()) {
            if (input.charAt(position) == ',') {
                throw error(String.format(Locale.ROOT,
                        "Too many columns: expected %d; found an extra column after field %d (%s).",
                        COLUMN_COUNT, COLUMN_COUNT, CandidateCsvFormat.HEADERS.getLast()));
            }
            readLineEnding();
        }
        return fields;
    }

    /** Reads a field without consuming its delimiter, counting decoded Unicode code points. */
    private String readField(int column) throws ParseException {
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
            if (codePoint == '"' && readQuote(column, quoted)) {
                return value.toString();
            }
            if (++length > MAX_FIELD_LENGTH) {
                throw fieldError(column, String.format(Locale.ROOT, "Field exceeds %,d characters.", MAX_FIELD_LENGTH));
            }
            value.appendCodePoint(codePoint);
        }
        if (quoted) {
            throw fieldError(column, "Unterminated quoted field. Add a closing double quote; "
                    + "escape embedded quotes by doubling them.");
        }
        return value.toString();
    }

    /** Consumes an escaped quote or validates a closing quote, returning whether the field ends. */
    private boolean readQuote(int column, boolean quoted) throws ParseException {
        if (!quoted) {
            throw fieldError(column, "Unexpected quote in an unquoted field. Enclose the entire field "
                    + "in double quotes and escape embedded quotes by doubling them.");
        }
        if (position < input.length() && input.charAt(position) == '"') {
            position++;
            return false;
        }
        if (position < input.length() && input.charAt(position) != ',' && !isLineEnding()) {
            throw fieldError(column, "Unexpected content after a closing quote. Expected a comma, "
                    + "LF/CRLF record ending, or end of file; remove text or spaces outside the quotes.");
        }
        return true;
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
        if (input.charAt(position) == '\r') {
            position++;
            if (position >= input.length() || input.charAt(position) != '\n') {
                throw error("Bare CR record ending. Use LF or CRLF line endings instead.");
            }
        }
        position++;
    }

    private void readColumnSeparator(int column) throws ParseException {
        if (position >= input.length() || isLineEnding()) {
            String ending = position >= input.length() ? "end of file" : "record ending";
            throw fieldError(column, "Too few columns: expected " + COLUMN_COUNT + ", found " + (column + 1)
                    + " before " + ending + ". Add the missing comma-separated fields; use an empty field "
                    + "for an absent optional value.");
        }
        position++;
    }

    private ParseException fieldError(int column, String reason) {
        return new ParseException("CSV record " + record + ", field " + (column + 1) + " ("
                + CandidateCsvFormat.HEADERS.get(column) + "): " + reason);
    }

    private ParseException error(String reason) {
        return new ParseException("CSV record " + record + ": " + reason);
    }
}
