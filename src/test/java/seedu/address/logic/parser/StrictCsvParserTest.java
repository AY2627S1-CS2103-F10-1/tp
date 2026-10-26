package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

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
    public void parse_wrongColumnCountOrEscaping_rejects() {
        for (String input : new String[] {"\"\"\r\n", EMPTY_RECORD.replace("\r\n", ",\"\"\r\n"),
            "\"bad\"quote\",\"\",\"\",\"\",\"\",\"\",\"\"\r\n"}) {
            assertThrows(ParseException.class, () -> new StrictCsvParser(input).parse());
        }
    }
}
