package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CreateRoleCommand;
import seedu.address.logic.commands.DeleteRoleCommand;
import seedu.address.logic.commands.EditRoleCommand;
import seedu.address.logic.commands.ListRoleCommand;
import seedu.address.model.role.Role;

class RoleCommandParsersTest {
    @Test
    void parse_validCommandsAndInternalSpaces_returnsExpectedCommands() {
        assertParseSuccess(new CreateRoleCommandParser(), " n/  Software Engineer  ",
                new CreateRoleCommand("Software Engineer"));
        assertParseSuccess(new EditRoleCommandParser(), " r/Software Engineer n/Senior Engineer",
                new EditRoleCommand("Software Engineer", "Senior Engineer"));
        assertParseSuccess(new EditRoleCommandParser(), " n/New r/Old",
                new EditRoleCommand("Old", "New"));
        assertParseSuccess(new DeleteRoleCommandParser(), " r/Software Engineer",
                new DeleteRoleCommand("Software Engineer"));
        assertParseSuccess(new ListRoleCommandParser(), "",
                new ListRoleCommand());
        assertParseSuccess(new CreateRoleCommandParser(), " n/ C/C++ Engineer",
                new CreateRoleCommand("C/C++ Engineer"));
        assertParseSuccess(new CreateRoleCommandParser(), " n/C++ / .NET Engineer",
                new CreateRoleCommand("C++ / .NET Engineer"));
    }

    @Test
    void parse_missingOrUnexpectedArguments_rejects() {
        assertInvalidFormat("create_role", List.of("", " Engineer", " unexpected n/Engineer"));
        assertInvalidFormat("edit_role", List.of(" r/Engineer", " n/New"));
        assertInvalidFormat("delete_role", List.of(""));
        assertInvalidFormat("list_role", List.of(" extra", " n/Engineer"));
        assertParseFailure(new CreateRoleCommandParser(), " r/Engineer",
                "Unexpected role parameter: r/. Usage: " + CreateRoleCommand.MESSAGE_USAGE);
        assertParseFailure(new DeleteRoleCommandParser(), " n/Engineer",
                "Unexpected role parameter: n/. Usage: " + DeleteRoleCommand.MESSAGE_USAGE);
        assertParseFailure(new CreateRoleCommandParser(), " n/Engineer p/123",
                "Unexpected role parameter: p/. Usage: " + CreateRoleCommand.MESSAGE_USAGE);
    }

    @Test
    void parse_blankOrControlCharacterName_rejectsWithConstraints() {
        assertParseFailure(new CreateRoleCommandParser(), " n/", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(new EditRoleCommandParser(), " r/Engineer n/  ", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(new DeleteRoleCommandParser(), " r/ ", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(new CreateRoleCommandParser(), " n/Line\nBreak", Role.MESSAGE_CONSTRAINTS);
    }

    @Test
    void parse_repeatedPrefixes_rejectsInsteadOfTakingLast() {
        String duplicateName = Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NAME);
        assertParseFailure(new CreateRoleCommandParser(), " n/Engineer N/Designer", duplicateName);
        String duplicateRole = Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ROLE);
        assertParseFailure(new DeleteRoleCommandParser(), " r/Engineer r/Designer", duplicateRole);
        assertParseFailure(new EditRoleCommandParser(), " r/Old n/New n/Other", duplicateName);
    }

    private void assertInvalidFormat(String commandWord, List<String> inputs) {
        Parser<? extends Command> parser = switch (commandWord) {
            case "create_role" -> new CreateRoleCommandParser();
            case "edit_role" -> new EditRoleCommandParser();
            case "delete_role" -> new DeleteRoleCommandParser();
            case "list_role" -> new ListRoleCommandParser();
            default -> throw new IllegalArgumentException("Unknown test command.");
        };
        String usage = switch (commandWord) {
            case "create_role" -> CreateRoleCommand.MESSAGE_USAGE;
            case "edit_role" -> EditRoleCommand.MESSAGE_USAGE;
            case "delete_role" -> DeleteRoleCommand.MESSAGE_USAGE;
            case "list_role" -> ListRoleCommand.MESSAGE_USAGE;
            default -> throw new IllegalArgumentException("Unknown test command.");
        };
        for (String input : inputs) {
            assertParseFailure(parser, input,
                    "Invalid role command format. Usage: " + usage);
        }
    }
}
