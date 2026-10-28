package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

public class StrictCsvParserTest {
    private static final String EMPTY_RECORD = "\"\",\"\",\"\",\"\",\"\",\"\",\"\"\r\n";

    @Test
    public void parse_recordCountBoundary_acceptsLimitRejectsExtraRecord() throws Exception {
        assertEquals(10_001, new StrictCsvParser(EMPTY_RECORD.repeat(10_001)).parse().size());
        assertThrows(ParseException.class, () -> new StrictCsvParser(EMPTY_RECORD.repeat(10_002)).parse());
    }

    @Test
    public void parse_fieldLengthBoundary_countsUnicodeCodePoints() throws Exception {
        String text = "\ud83d\ude00".repeat(65_536);
        String record = "\"" + text + "\",\"\",\"\",\"\",\"\",\"\",\"\"\r\n";
        assertEquals(text, new StrictCsvParser(record).parse().getFirst().getFirst());
        assertThrows(ParseException.class, () -> new StrictCsvParser(record.replace(text, text + "x")).parse());
    }

    @Test
    public void parse_mixedQuotingAndEmbeddedSeparators_preservesFields() throws Exception {
        String input = "plain,\"comma,value\",\"double \"\"quote\"\"\",\"first\r\nsecond\nthird\",,last,";
        List<String> expected = List.of("plain", "comma,value", "double \"quote\"",
                "first\r\nsecond\nthird", "", "last", "");
        assertEquals(List.of(expected), new StrictCsvParser(input).parse());
        assertEquals(List.of(expected, expected), new StrictCsvParser(input + "\n" + input + "\r\n\n").parse());
    }

    @Test
    public void parse_unquotedLimits_enforcesFieldAndRecordLimits() throws Exception {
        String text = "a".repeat(65_536);
        assertEquals(text, new StrictCsvParser(text + ",,,,,,").parse().getFirst().getFirst());
        assertThrows(ParseException.class, () -> new StrictCsvParser(text + "a,,,,,,").parse());
        String row = ",,,,,,\n";
        assertEquals(10_001, new StrictCsvParser(row.repeat(10_001) + "\n\n").parse().size());
        assertThrows(ParseException.class, () -> new StrictCsvParser(row.repeat(10_002)).parse());
    }

    @Test
    public void parse_malformedFlexibleCsv_rejects() {
        for (String input : List.of("\n", "\r\n", "\n" + EMPTY_RECORD, EMPTY_RECORD + "\n" + EMPTY_RECORD,
                EMPTY_RECORD + " \n", "a,b,c,d,e,f", "a,b,c,d,e,f,g,h", "a,b,c,d,e,f,g\r",
                "a,b,c,d,e,f,g\rx", "un\"quoted,b,c,d,e,f,g", "\"closed\"junk,b,c,d,e,f,g",
                "a,b,c,d,e,f,\"unterminated", "a,b,c,d,e,f,\"closed\"junk", EMPTY_RECORD + "\r")) {
            assertThrows(ParseException.class, () -> new StrictCsvParser(input).parse());
        }
    }

    @Test
    public void parse_missingColumns_identifiesFieldAndEnding() {
        for (String ending : List.of("", "\n", "\r\n")) {
            String reason = ending.isEmpty() ? "end of file" : "record ending";
            assertThrows(ParseException.class, "CSV record 2, field 6 (tags): Too few columns: expected 7, "
                    + "found 6 before " + reason + ". Add the missing comma-separated fields; use an empty field "
                    + "for an absent optional value.", () ->
                    new StrictCsvParser(EMPTY_RECORD + "a,b,c,d,e,f" + ending).parse());
        }
    }

    @Test
    public void parse_extraColumn_identifiesRecordAndColumnCount() {
        assertThrows(ParseException.class, "CSV record 2: Too many columns: expected 7; "
                + "found an extra column after field 7 (note).", () ->
                new StrictCsvParser(EMPTY_RECORD + "a,b,c,d,e,f,g,h").parse());
    }

    @Test
    public void parse_misplacedQuote_identifiesFieldWithoutExposingValue() {
        assertThrows(ParseException.class, "CSV record 2, field 3 (email): Unexpected quote in an unquoted field. "
                + "Enclose the entire field in double quotes and escape embedded quotes by doubling them.", () ->
                new StrictCsvParser(EMPTY_RECORD + "a,b,private\"value,d,e,f,g").parse());
    }

    @Test
    public void parse_contentAfterClosingQuote_identifiesFieldAndExpectedDelimiter() {
        for (String suffix : List.of("private", " ")) {
            assertThrows(ParseException.class, "CSV record 2, field 7 (note): Unexpected content after a closing "
                    + "quote. Expected a comma, LF/CRLF record ending, or end of file; remove text or spaces "
                    + "outside the quotes.", () ->
                    new StrictCsvParser(EMPTY_RECORD + "a,b,c,d,e,f,\"note\"" + suffix).parse());
        }
    }

    @Test
    public void parse_unterminatedQuote_identifiesFieldAndRemedy() {
        assertThrows(ParseException.class, "CSV record 2, field 7 (note): Unterminated quoted field. "
                + "Add a closing double quote; escape embedded quotes by doubling them.", () ->
                new StrictCsvParser(EMPTY_RECORD + "a,b,c,d,e,f,\"private").parse());
    }

    @Test
    public void parse_bareCr_identifiesRecordAndSupportedEndings() {
        for (String suffix : List.of("", "x")) {
            assertThrows(ParseException.class, "CSV record 2: Bare CR record ending. "
                    + "Use LF or CRLF line endings instead.", () ->
                    new StrictCsvParser(EMPTY_RECORD + "a,b,c,d,e,f,g\r" + suffix).parse());
        }
    }

    @Test
    public void parse_oversizedField_identifiesFieldAndLimit() {
        assertThrows(ParseException.class, "CSV record 2, field 1 (name): Field exceeds 65,536 characters.", () ->
                new StrictCsvParser(EMPTY_RECORD + "a".repeat(65_537) + ",,,,,,").parse());
    }

    @Test
    public void parse_wrongColumnCountOrEscaping_rejects() {
        for (String input : new String[] {"\"\"\r\n", EMPTY_RECORD.replace("\r\n", ",\"\"\r\n"),
            "\"bad\"quote\",\"\",\"\",\"\",\"\",\"\",\"\"\r\n"}) {
            assertThrows(ParseException.class, () -> new StrictCsvParser(input).parse());
        }
    }
}
