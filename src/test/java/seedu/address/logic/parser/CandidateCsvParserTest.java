package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;

public class CandidateCsvParserTest {
    private static final String HEADER = record(List.of("name", "phone", "email", "address", "status", "tags", "note"));
    private final CandidateCsvParser parser = new CandidateCsvParser();

    @Test
    public void parse_exportFormat_preservesLiteralValuesAndNormalizesTagsAndStatus() throws Exception {
        Person person = parseRow("[\"java\",\" Java \",\"java\"]", "  iNtErViEwInG ",
                "Unicode \u4e2d\u6587, \"quoted\"\r\nsecond line");

        assertEquals(Status.INTERVIEWING, person.getStatus());
        assertEquals(2, person.getTags().size());
        assertEquals("Unicode \u4e2d\u6587, \"quoted\"\r\nsecond line", person.getNote().orElseThrow().value);
    }

    @Test
    public void parse_emptyAndHeaderOnly_returnsNoCandidates() throws Exception {
        assertEquals(List.of(), parser.parse(new byte[0]));
        assertEquals(List.of(), parser.parse(HEADER.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void parse_invalidEncodingOrDialect_rejects() {
        assertThrows(ParseException.class, () -> parser.parse(new byte[] {(byte) 0xc3, 0x28}));
        for (String input : List.of(" ", "\ufeff" + HEADER, HEADER.replace("\r\n", "\n"),
                HEADER.stripTrailing(), HEADER.replace("\"name\"", "name"), HEADER + "\r\n",
                HEADER.replace("name", "Name"), HEADER + "trailing", HEADER + "\"unterminated")) {
            assertThrows(ParseException.class, () -> parser.parse(input.getBytes(StandardCharsets.UTF_8)));
        }
    }

    @Test
    public void parse_invalidTags_rejectsWithoutExposingValues() {
        for (String tags : List.of("null", "{}", "[null]", "[1]", "[[\"java\"]]", "[{}]", "[\"bad tag\"]",
                "[\"java\"] true", "[\"private", "[\"java\",]")) {
            assertThrows(ParseException.class, () -> parseRow(tags, "Applied", ""));
        }
    }

    @Test
    public void parse_tagLimits_checksBeforeDeduplicationAndTrimming() throws Exception {
        String hundred = "[" + String.join(",", Collections.nCopies(100, "\"java\"")) + "]";
        assertEquals(1, parseRow(hundred, "Applied", "").getTags().size());
        assertThrows(ParseException.class, () -> parseRow(hundred.replace("]", ",\"java\"]"), "Applied", ""));
        assertEquals(1, parseRow("[\"" + "a".repeat(256) + "\"]", "Applied", "").getTags().size());
        assertThrows(ParseException.class, () -> parseRow("[\"" + "a".repeat(257) + "\"]", "Applied", ""));
    }

    @Test
    public void parse_noteAndStatusValidation_matchesModel() throws Exception {
        assertEquals(500, parseRow("[]", "Applied", "a".repeat(500)).getNote().orElseThrow().value.length());
        assertThrows(ParseException.class, () -> parseRow("[]", "Applied", "a".repeat(501)));
        assertThrows(ParseException.class, () -> parseRow("[]", "", ""));
        assertThrows(ParseException.class, () -> parseRow("[]", "Hired", ""));
        for (Status status : Status.values()) {
            assertEquals(status, parseRow("[]", status.toString(), "").getStatus());
        }
    }

    @Test
    public void parse_invalidCandidateField_identifiesRecordAndField() {
        String input = HEADER + record(List.of("Candidate", "invalid", "candidate@example.com", "Address",
                "Applied", "[]", ""));
        assertThrows(ParseException.class, "CSV record 2, field phone: Phone numbers should only contain digits, "
                + "and should be at least 3 digits long", () -> parser.parse(input.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void parse_longEmail_handlesWithoutCrashing() throws Exception {
        String email = "a.".repeat(10_000) + "a@example.com";
        String input = HEADER + record(List.of("Candidate", "12345", email, "Address", "Applied", "[]", ""));
        assertEquals(email, parser.parse(input.getBytes(StandardCharsets.UTF_8)).getFirst().getEmail().value);
    }

    private Person parseRow(String tags, String status, String note) throws ParseException {
        String input = HEADER + record(List.of("Candidate", "12345", "candidate@example.com", "Address",
                status, tags, note));
        return parser.parse(input.getBytes(StandardCharsets.UTF_8)).getFirst();
    }

    private static String record(List<String> fields) {
        return fields.stream().map(field -> "\"" + field.replace("\"", "\"\"") + "\"")
                .collect(Collectors.joining(",")) + "\r\n";
    }
}
