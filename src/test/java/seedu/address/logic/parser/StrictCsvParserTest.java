package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestCaseUtil.assertAllCases;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

public class StrictCsvParserTest {
    private static final String EMPTY_RECORD = "\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"\r\n";

    // The header counts as a record, but not as a candidate.
    private static final int MAX_RECORDS = StrictCsvParser.MAX_CANDIDATES + 1;

    @Test
    public void parse_recordCountBoundary_acceptsHeaderPlusLimitRejectsExtraCandidate() throws Exception {
        assertEquals(MAX_RECORDS, new StrictCsvParser(EMPTY_RECORD.repeat(MAX_RECORDS)).parse().size());
        assertThrows(ParseException.class, "CSV record " + (MAX_RECORDS + 1)
                + ": Candidate count exceeds 10,000.", () ->
                new StrictCsvParser(EMPTY_RECORD.repeat(MAX_RECORDS + 1)).parse());
    }

    @Test
    public void parse_fieldLengthBoundary_countsUnicodeCodePoints() throws Exception {
        String text = "\ud83d\ude00".repeat(65_536);
        String record = "\"" + text + "\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"\r\n";
        assertEquals(text, new StrictCsvParser(record).parse().getFirst().getFirst());
        assertThrows(ParseException.class, () -> new StrictCsvParser(record.replace(text, text + "x")).parse());
    }

    @Test
    public void parse_mixedQuotingAndEmbeddedSeparators_preservesFields() throws Exception {
        String input = "plain,\"comma,value\",\"double \"\"quote\"\"\",\"first\r\nsecond\nthird\",,,last,";
        List<String> expected = List.of("plain", "comma,value", "double \"quote\"",
                "first\r\nsecond\nthird", "", "", "last", "");
        assertEquals(List.of(expected), new StrictCsvParser(input).parse());
        assertEquals(List.of(expected, expected), new StrictCsvParser(input + "\n" + input + "\r\n\n").parse());
    }

    @Test
    public void parse_unquotedLimits_enforcesFieldAndRecordLimits() throws Exception {
        String text = "a".repeat(65_536);
        assertEquals(text, new StrictCsvParser(text + ",,,,,,,").parse().getFirst().getFirst());
        assertThrows(ParseException.class, () -> new StrictCsvParser(text + "a,,,,,,,").parse());
        String row = ",,,,,,,\n";
        assertEquals(MAX_RECORDS, new StrictCsvParser(row.repeat(MAX_RECORDS) + "\n\n").parse().size());
        assertThrows(ParseException.class, "CSV record " + (MAX_RECORDS + 1)
                + ": Candidate count exceeds 10,000.", () -> new StrictCsvParser(row.repeat(MAX_RECORDS + 1)).parse());
    }

    @Test
    public void parse_malformedFlexibleCsv_rejects() {
        assertAllCases(List.of("\n", "\r\n", "\n" + EMPTY_RECORD, EMPTY_RECORD + "\n" + EMPTY_RECORD,
                EMPTY_RECORD + " \n", "a,b,c,d,e,f", "a,b,c,d,e,f,g,h,i", "a,b,c,d,e,f,g,h\r",
                "a,b,c,d,e,f,g,h\rx", "un\"quoted,b,c,d,e,f,g", "\"closed\"junk,b,c,d,e,f,g",
                "a,b,c,d,e,f,\"unterminated", "a,b,c,d,e,f,\"closed\"junk", EMPTY_RECORD + "\r"), input -> {
                    assertThrows(ParseException.class, () -> new StrictCsvParser(input).parse());
                });
    }

    @Test
    public void parse_missingColumns_identifiesFieldAndEnding() {
        assertAllCases(List.of("", "\n", "\r\n"), ending -> {
            String reason = ending.isEmpty() ? "end of file" : "record ending";
            assertThrows(ParseException.class, "CSV record 2, field 6 (tags): Too few columns: expected 8, "
                    + "found 6 before " + reason + ". Add the missing comma-separated fields; use an empty field "
                    + "for an absent optional value.", () ->
                    new StrictCsvParser(EMPTY_RECORD + "a,b,c,d,e,f" + ending).parse());
        });
    }

    @Test
    public void parse_extraColumn_identifiesRecordAndColumnCount() {
        assertThrows(ParseException.class, "CSV record 2: Too many columns: expected 8; "
                + "found an extra column after field 8 (roles).", () ->
                new StrictCsvParser(EMPTY_RECORD + "a,b,c,d,e,f,g,h,i").parse());
    }

    @Test
    public void parse_misplacedQuote_identifiesFieldWithoutExposingValue() {
        assertThrows(ParseException.class, "CSV record 2, field 3 (email): Unexpected quote in an unquoted field. "
                + "Enclose the entire field in double quotes and escape embedded quotes by doubling them.", () ->
                new StrictCsvParser(EMPTY_RECORD + "a,b,private\"value,d,e,f,g").parse());
    }

    @Test
    public void parse_contentAfterClosingQuote_identifiesFieldAndExpectedDelimiter() {
        assertAllCases(List.of("private", " "), suffix -> {
            assertThrows(ParseException.class, "CSV record 2, field 7 (note): Unexpected content after a closing "
                    + "quote. Expected a comma, LF/CRLF record ending, or end of file; remove text or spaces "
                    + "outside the quotes.", () ->
                    new StrictCsvParser(EMPTY_RECORD + "a,b,c,d,e,f,\"note\"" + suffix).parse());
        });
    }

    @Test
    public void parse_unterminatedQuote_identifiesFieldAndRemedy() {
        assertThrows(ParseException.class, "CSV record 2, field 7 (note): Unterminated quoted field. "
                + "Add a closing double quote; escape embedded quotes by doubling them.", () ->
                new StrictCsvParser(EMPTY_RECORD + "a,b,c,d,e,f,\"private").parse());
    }

    @Test
    public void parse_bareCr_identifiesRecordAndSupportedEndings() {
        assertAllCases(List.of("", "x"), suffix -> {
            assertThrows(ParseException.class, "CSV record 2: Bare CR record ending. "
                    + "Use LF or CRLF line endings instead.", () ->
                    new StrictCsvParser(EMPTY_RECORD + "a,b,c,d,e,f,g,h\r" + suffix).parse());
        });
    }

    @Test
    public void parse_oversizedField_identifiesFieldAndLimit() {
        assertThrows(ParseException.class, "CSV record 2, field 1 (name): Field exceeds 65,536 characters.", () ->
                new StrictCsvParser(EMPTY_RECORD + "a".repeat(65_537) + ",,,,,,").parse());
    }

    @Test
    public void parse_wrongColumnCountOrEscaping_rejects() {
        assertAllCases(List.of("\"\"\r\n", EMPTY_RECORD.replace("\r\n", ",\"\"\r\n"),
                "\"bad\"quote\",\"\",\"\",\"\",\"\",\"\",\"\"\r\n"), input -> {
                    assertThrows(ParseException.class, () -> new StrictCsvParser(input).parse());
                });
    }
}
