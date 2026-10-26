package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.CreateRoleCommand;
import seedu.address.logic.commands.DeleteRoleCommand;
import seedu.address.logic.commands.EditRoleCommand;
import seedu.address.logic.commands.ListRoleCommand;
import seedu.address.model.role.Role;

class RoleCommandParserTest {
    @Test
    void parse_validCommandsAndInternalSpaces_returnsExpectedCommands() {
        assertParseSuccess(new RoleCommandParser("create_role"), " n/  Software Engineer  ",
                new CreateRoleCommand("Software Engineer"));
        assertParseSuccess(new RoleCommandParser("edit_role"), " r/Software Engineer n/Senior Engineer",
                new EditRoleCommand("Software Engineer", "Senior Engineer"));
        assertParseSuccess(new RoleCommandParser("edit_role"), " n/New r/Old",
                new EditRoleCommand("Old", "New"));
        assertParseSuccess(new RoleCommandParser("delete_role"), " r/Software Engineer",
                new DeleteRoleCommand("Software Engineer"));
        assertParseSuccess(new RoleCommandParser("list_role"), "",
                new ListRoleCommand());
        assertParseSuccess(new RoleCommandParser("create_role"), " n/ C/C++ Engineer",
                new CreateRoleCommand("C/C++ Engineer"));
        assertParseSuccess(new RoleCommandParser("create_role"), " n/C++ / .NET Engineer",
                new CreateRoleCommand("C++ / .NET Engineer"));
    }

    @Test
    void parse_missingOrUnexpectedArguments_rejects() {
        assertInvalidFormat("create_role", List.of("", " Engineer", " unexpected n/Engineer"));
        assertInvalidFormat("edit_role", List.of(" r/Engineer", " n/New"));
        assertInvalidFormat("delete_role", List.of(""));
        assertInvalidFormat("list_role", List.of(" extra", " n/Engineer"));
        assertParseFailure(new RoleCommandParser("create_role"), " r/Engineer",
                "Unexpected role parameter: r/. Usage: " + CreateRoleCommand.MESSAGE_USAGE);
        assertParseFailure(new RoleCommandParser("delete_role"), " n/Engineer",
                "Unexpected role parameter: n/. Usage: " + DeleteRoleCommand.MESSAGE_USAGE);
        assertParseFailure(new RoleCommandParser("create_role"), " n/Engineer p/123",
                "Unexpected role parameter: p/. Usage: " + CreateRoleCommand.MESSAGE_USAGE);
    }

    @Test
    void parse_blankOrControlCharacterName_rejectsWithConstraints() {
        assertParseFailure(new RoleCommandParser("create_role"), " n/", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(new RoleCommandParser("edit_role"), " r/Engineer n/  ", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(new RoleCommandParser("delete_role"), " r/ ", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(new RoleCommandParser("create_role"), " n/Line\nBreak", Role.MESSAGE_CONSTRAINTS);
    }

    @Test
    void parse_repeatedPrefixes_rejectsInsteadOfTakingLast() {
        String duplicateName = Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NAME);
        assertParseFailure(new RoleCommandParser("create_role"), " n/Engineer N/Designer", duplicateName);
        String duplicateRole = Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ROLE);
        assertParseFailure(new RoleCommandParser("delete_role"), " r/Engineer r/Designer", duplicateRole);
        assertParseFailure(new RoleCommandParser("edit_role"), " r/Old n/New n/Other", duplicateName);
    }

    private void assertInvalidFormat(String commandWord, List<String> inputs) {
        String usage = switch (commandWord) {
            case "create_role" -> CreateRoleCommand.MESSAGE_USAGE;
            case "edit_role" -> EditRoleCommand.MESSAGE_USAGE;
            case "delete_role" -> DeleteRoleCommand.MESSAGE_USAGE;
            case "list_role" -> ListRoleCommand.MESSAGE_USAGE;
            default -> throw new IllegalArgumentException("Unknown test command.");
        };
        for (String input : inputs) {
            assertParseFailure(new RoleCommandParser(commandWord), input,
                    "Invalid role command format. Usage: " + usage);
        }
    }
}
