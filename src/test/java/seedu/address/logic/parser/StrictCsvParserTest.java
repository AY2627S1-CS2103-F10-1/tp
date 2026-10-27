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
    public void parse_wrongColumnCountOrEscaping_rejects() {
        for (String input : new String[] {"\"\"\r\n", EMPTY_RECORD.replace("\r\n", ",\"\"\r\n"),
            "\"bad\"quote\",\"\",\"\",\"\",\"\",\"\",\"\"\r\n"}) {
            assertThrows(ParseException.class, () -> new StrictCsvParser(input).parse());
        }
    }
}
