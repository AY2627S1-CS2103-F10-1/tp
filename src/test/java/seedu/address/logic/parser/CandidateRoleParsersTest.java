package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.model.person.Status;
import seedu.address.model.role.Role;
import seedu.address.testutil.PersonBuilder;

class CandidateRoleParsersTest {
    private static final String DETAILS = " n/Amy Bee p/85355255 e/amy@gmail.com"
            + " a/123, Jurong West Ave 6, #08-111";

    @Test
    void parse_addMultipleRoles_keepsNamesForExecutionTimeLookup() {
        assertParseSuccess(new AddCommandParser(), DETAILS + " r/Developer R/Designer",
                new AddCommand(new PersonBuilder().build(), Set.of("Developer", "Designer")));
        assertParseSuccess(new AddCommandParser(), DETAILS + " r/未知职位",
                new AddCommand(new PersonBuilder().build(), Set.of("未知职位")));
    }

    @Test
    void parse_addWithoutRoles_failsWithNoRoleSuggestion() {
        assertParseFailure(new AddCommandParser(), DETAILS, "Missing r/ROLE. Use r/No role if no role applies.");
    }

    @Test
    void parse_emptyRoleAmongValidRoles_rejectsEveryCommand() {
        for (String roles : List.of(" r/", " r/   ", " r/Developer r/", " r/ r/Developer")) {
            assertParseFailure(new AddCommandParser(), DETAILS + roles, ParserUtil.MESSAGE_EMPTY_ROLE);
            assertParseFailure(new EditCommandParser(), "1" + roles, ParserUtil.MESSAGE_EMPTY_ROLE);
            assertParseFailure(new FilterCommandParser(), roles, ParserUtil.MESSAGE_EMPTY_ROLE);
        }
    }

    @Test
    void parse_invalidRoleCharacters_rejectsEveryCommand() {
        assertParseFailure(new AddCommandParser(), DETAILS + " r/Dev\teloper", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(new EditCommandParser(), "1 r/Dev\neloper", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(new FilterCommandParser(), " r/Dev\reloper", Role.MESSAGE_CONSTRAINTS);
    }

    @Test
    void parse_invisibleRoleCharacters_rejectsEveryCommand() {
        String noBreakSpaceError = String.format(ParserUtil.MESSAGE_INVISIBLE_ROLE_CHARACTER, 0x00A0);
        assertParseFailure(new AddCommandParser(), DETAILS + " r/Designer\u00A0", noBreakSpaceError);
        assertParseFailure(new EditCommandParser(), "1 r/No\u00A0role", noBreakSpaceError);
        assertParseFailure(new FilterCommandParser(), " r/\u00A0", noBreakSpaceError);
        assertParseFailure(new AddRoleCommandParser(), " n/Design\u200Ber",
                String.format(ParserUtil.MESSAGE_INVISIBLE_ROLE_CHARACTER, 0x200B));
    }

    @Test
    void parse_unicodeSpacesAroundRole_trimsThem() {
        assertParseSuccess(new FilterCommandParser(), " r/\u2003Designer\u2003",
                new FilterCommand(List.of(), Set.of("Designer")));
    }

    @Test
    void parse_editRoles_followsDescriptorReplacementPattern() {
        EditPersonDescriptor expected = new EditPersonDescriptor();
        expected.setRoleNames(Set.of("Developer", "Designer"));
        assertParseSuccess(new EditCommandParser(), "1 r/Developer r/Designer",
                new EditCommand(Index.fromOneBased(1), expected));
        assertEquals(expected, new EditPersonDescriptor(expected));
    }

    @Test
    void parse_filterRolesAndStatuses_acceptsEitherOrBothFields() {
        assertParseSuccess(new FilterCommandParser(), " r/Developer r/Designer",
                new FilterCommand(List.of(), Set.of("Developer", "Designer")));
        assertParseSuccess(new FilterCommandParser(), " r/Developer s/Interviewing s/Offered",
                new FilterCommand(List.of(Status.INTERVIEWING, Status.OFFERED), Set.of("Developer")));
    }
}
