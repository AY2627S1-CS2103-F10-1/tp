package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestCaseUtil.assertAllCases;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;
import seedu.address.model.role.Role;
import seedu.address.model.role.RoleCatalog;

public class CandidateCsvParserTest {
    private static final String HEADER = record(List.of("name", "phone", "email", "address", "status",
            "tags", "note", "roles"));
    private static final String NO_ROLES = "[\"No role\"]";
    private static final String NO_ROLES_CELL = "\"[\"\"No role\"\"]\"";

    @Test
    public void parse_roleNames_matchesCatalogAndCreatesEachUnknownRoleOnce() throws Exception {
        Role developer = Role.create("Developer");
        String input = HEADER + candidate("first@example.com", "[\" developer \",\"QA\",\"No role\"]")
                + candidate("second@example.com", "[\"qa\"]");

        List<Person> persons = CandidateCsvParser.parse(input.getBytes(StandardCharsets.UTF_8),
                new RoleCatalog(List.of(Role.NO_ROLE, developer)));

        Role qa = persons.get(1).getRoles().iterator().next();
        assertEquals("QA", qa.getName());
        assertEquals(Set.of(developer, qa), persons.get(0).getRoles());
    }

    @Test
    public void parse_emptyRoles_suggestsNoRole() {
        assertThrows(ParseException.class, "CSV record 2, field roles: Each candidate needs "
                + "at least one role; use [\"No role\"] if no role applies.", () -> parseRoles("[]"));
    }

    @Test
    public void parse_invalidRoles_rejects() {
        List<String> inputs = List.of("null", "[1]", "[\"\"]", "[\"QA r/Lead\"]", "[\"QA\\tLead\"]",
                "[\"" + "a".repeat(257) + "\"]", "[" + "\"QA\",".repeat(100) + "\"QA\"]");

        assertAllCases(inputs, roles -> {
            assertThrows(ParseException.class, () -> parseRoles(roles));
        });
    }

    @Test
    public void parse_noBreakSpacesInRoleNames_identifiesRecordAndField() {
        assertAllCases(List.of("Designer\u00a0", "No role\u00a0", "QA\u00a0Lead"), name -> {
            assertThrows(ParseException.class, "CSV record 2, field roles: "
                    + String.format(ParserUtil.MESSAGE_INVISIBLE_ROLE_CHARACTER, 0x00a0), () ->
                    parseRoles("[\"" + name + "\"]"));
        });
    }

    @Test
    public void parse_formatCharactersInRoleNames_identifiesRecordAndField() {
        assertAllCases(List.of("\u200b", "QA\u200bLead"), name -> {
            assertThrows(ParseException.class, "CSV record 2, field roles: "
                    + String.format(ParserUtil.MESSAGE_INVISIBLE_ROLE_CHARACTER, 0x200b), () ->
                    parseRoles("[\"" + name + "\"]"));
        });
        assertThrows(ParseException.class, "CSV record 2, field roles: "
                + String.format(ParserUtil.MESSAGE_INVISIBLE_ROLE_CHARACTER, 0xfeff), () ->
                parseRoles("[\"QA\ufeffLead\"]"));
    }

    @Test
    public void parse_controlCharactersInRoleNames_identifiesRecordAndField() {
        assertAllCases(List.of("QA\\tLead", "QA\\rLead", "QA\\nLead", "QA\\u0000Lead", "QA\\u007fLead"), name -> {
            assertThrows(ParseException.class, "CSV record 2, field roles: " + Role.MESSAGE_CONSTRAINTS, () ->
                    parseRoles("[\"" + name + "\"]"));
        });
    }

    @Test
    public void parse_visibleUnicodeAndQuotedRoleNames_preservesNames() throws Exception {
        Person candidate = parseRoles("[\" Caf\u00e9, \\\"Design\\\" \",\"R\\\\D\"]");

        assertEquals(Set.of("Caf\u00e9, \"Design\"", "R\\D"), candidate.getRoles().stream()
                .map(Role::getName).collect(Collectors.toSet()));
    }

    @Test
    public void parse_equivalentUnicodeRoleName_matchesCatalog() throws Exception {
        Role cafe = Role.create("Caf\u00e9");
        String input = HEADER + candidate("candidate@example.com", "[\" CAFE\u0301 \"]");

        List<Person> candidates = CandidateCsvParser.parse(input.getBytes(StandardCharsets.UTF_8),
                new RoleCatalog(List.of(Role.NO_ROLE, cafe)));

        assertEquals(Set.of(cafe), candidates.getFirst().getRoles());
    }

    @Test
    public void parse_exportFormat_preservesLiteralValuesAndNormalizesTagsAndStatus() throws Exception {
        Person person = parseRow("[\"java\",\" Java \",\"java\"]", "  iNtErViEwInG ",
                "Follow up, \"quoted\" feedback");

        assertEquals(Status.INTERVIEWING, person.getStatus());
        assertEquals(2, person.getTags().size());
        assertEquals("Follow up, \"quoted\" feedback", person.getNote().orElseThrow().value);
    }

    @Test
    public void parse_emptyAndHeaderOnly_returnsNoCandidates() throws Exception {
        assertEquals(List.of(), parse(new byte[0]));
        assertEquals(List.of(), parse(HEADER.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void parse_leadingBom_acceptsHeaderAndPreservesLiteralFieldContents() throws Exception {
        String note = "Follow up, \"quoted\" feedback";
        String input = HEADER + record(List.of("Candidate", "12345", "candidate@example.com", "Address",
                "Applied", "[]", note, NO_ROLES));
        assertEquals(parse(input.getBytes(StandardCharsets.UTF_8)),
                parse(("\ufeff" + input).getBytes(StandardCharsets.UTF_8)));
        assertEquals(note, parse(("\ufeff" + input).getBytes(StandardCharsets.UTF_8))
                .getFirst().getNote().orElseThrow().value);
        assertEquals(List.of(), parse(("\ufeff" + HEADER).getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void parse_invalidEncodingOrDialect_rejects() {
        assertThrows(ParseException.class, () -> parse(new byte[] {(byte) 0xc3, 0x28}));
        for (String input : List.of(" ", "\ufeff", "\ufeff\ufeff" + HEADER, " \ufeff" + HEADER,
                HEADER.replace("name", "Name"), HEADER.replace(",\"roles\"", ""), HEADER + "trailing",
                HEADER + "\"unterminated")) {
            assertThrows(ParseException.class, () -> parse(input.getBytes(StandardCharsets.UTF_8)));
        }
    }

    @Test
    public void parse_commonCsvFormats_acceptsEquivalentCandidates() throws Exception {
        String header = "name,phone,email,address,status,tags,note,roles";
        String row = "Candidate,12345,candidate@example.com,Address,Applied,[],Follow up," + NO_ROLES_CELL;
        Person expected = parseRow("[]", "Applied", "Follow up");
        for (String separator : List.of("\n", "\r\n")) {
            for (String ending : List.of("", separator, separator.repeat(3))) {
                String input = header + separator + row + ending;
                assertEquals(List.of(expected), parse(input.getBytes(StandardCharsets.UTF_8)));
                assertEquals(List.of(expected), parse(("\ufeff" + input).getBytes(StandardCharsets.UTF_8)));
            }
        }
        for (String input : List.of(HEADER.stripTrailing(), HEADER + "\n", HEADER.replace("\r\n", "\n"))) {
            assertEquals(List.of(), parse(input.getBytes(StandardCharsets.UTF_8)));
        }
    }

    @Test
    public void parse_unquotedLegacyNote_acceptsUnicode() throws Exception {
        String input = "name,phone,email,address,status,tags,note,roles\n"
                + "Candidate,12345,candidate@example.com,Address,Applied,[],Unicode \u4e2d," + NO_ROLES_CELL;
        assertEquals("Unicode \u4e2d", parse(input.getBytes(StandardCharsets.UTF_8))
                .getFirst().getNote().orElseThrow().value);
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
    public void parse_noteWithSurroundingSpaces_trimsBeforeLengthValidation() throws Exception {
        String note = "a".repeat(500);
        assertEquals(note, parseRow("[]", "Applied", "  " + note + "  ").getNote().orElseThrow().value);
        assertEquals("Follow  up", parseRow("[]", "Applied", "  Follow  up  ").getNote().orElseThrow().value);
        String tooLongNote = " " + "a".repeat(501) + " ";
        assertThrows(ParseException.class, "CSV record 2, field note: " + Note.MESSAGE_TOO_LONG, () ->
                parseRow("[]", "Applied", tooLongNote));
    }

    @Test
    public void parse_blankNote_returnsNoNote() throws Exception {
        for (String note : List.of("", "   ", "\t\r\n")) {
            assertEquals(Optional.empty(), parseRow("[]", "Applied", note).getNote());
        }
    }

    @Test
    public void parse_legacyNoteCharacters_preservesInternalContents() throws Exception {
        for (String character : List.of("\u4e2d", "\ufeff", "\t", "\r", "\n", "\u007f")) {
            String note = "Note" + character + "text";
            assertEquals(note, parseRow("[]", "Applied", note).getNote().orElseThrow().value);
        }
    }

    @Test
    public void parse_unicodeNoteLength_countsCodePoints() throws Exception {
        String note = "\ud83d\udcdd".repeat(500);
        assertEquals(note, parseRow("[]", "Applied", note).getNote().orElseThrow().value);
        assertThrows(ParseException.class, "CSV record 2, field note: " + Note.MESSAGE_TOO_LONG, () ->
                parseRow("[]", "Applied", note + "x"));
    }

    @Test
    public void parse_invalidCandidateField_identifiesRecordAndField() {
        String input = HEADER + record(List.of("Candidate", "invalid", "candidate@example.com", "Address",
                "Applied", "[]", "", NO_ROLES));
        assertThrows(ParseException.class, "CSV record 2, field phone: " + Phone.MESSAGE_CONSTRAINTS, () ->
                parse(input.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void parse_longEmail_handlesWithoutCrashing() throws Exception {
        String email = "a.".repeat(10_000) + "a@example.com";
        String input = HEADER + record(List.of("Candidate", "12345", email, "Address", "Applied", "[]", "",
                NO_ROLES));
        assertEquals(email, parse(input.getBytes(StandardCharsets.UTF_8)).getFirst().getEmail().value);
    }

    private Person parseRow(String tags, String status, String note) throws ParseException {
        String input = HEADER + record(List.of("Candidate", "12345", "candidate@example.com", "Address",
                status, tags, note, NO_ROLES));
        return parse(input.getBytes(StandardCharsets.UTF_8)).getFirst();
    }

    private Person parseRoles(String roles) throws ParseException {
        String input = HEADER + candidate("candidate@example.com", roles);
        return parse(input.getBytes(StandardCharsets.UTF_8)).getFirst();
    }

    private static List<Person> parse(byte[] bytes) throws ParseException {
        return CandidateCsvParser.parse(bytes, new RoleCatalog());
    }

    private static String candidate(String email, String roles) {
        return record(List.of("Candidate", "12345", email, "Address", "Applied", "[]", "", roles));
    }

    private static String record(List<String> fields) {
        return fields.stream().map(field -> "\"" + field.replace("\"", "\"\"") + "\"")
                .collect(Collectors.joining(",")) + "\r\n";
    }
}
