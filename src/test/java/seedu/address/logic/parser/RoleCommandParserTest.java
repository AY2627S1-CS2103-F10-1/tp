package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.CreateRoleCommand;
import seedu.address.logic.commands.DeleteRoleCommand;
import seedu.address.logic.commands.EditRoleCommand;
import seedu.address.logic.commands.ListRoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.role.Role;

class RoleCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    void parse_validCommandsAndInternalSpaces_returnsExpectedCommands() throws Exception {
        assertEquals(new CreateRoleCommand("Software Engineer"),
                parser.parseCommand("create_role n/  Software Engineer  "));
        assertEquals(new EditRoleCommand("Software Engineer", "Senior Engineer"),
                parser.parseCommand("edit_role r/Software Engineer n/Senior Engineer"));
        assertEquals(new EditRoleCommand("Old", "New"), parser.parseCommand("edit_role n/New r/Old"));
        assertEquals(new DeleteRoleCommand("Software Engineer"),
                parser.parseCommand("delete_role r/Software Engineer"));
        assertEquals(new ListRoleCommand(), parser.parseCommand("list_role"));
        assertEquals(new CreateRoleCommand("C/C++ Engineer"), parser.parseCommand("create_role n/ C/C++ Engineer"));
        assertEquals(new CreateRoleCommand("C++ / .NET Engineer"),
                parser.parseCommand("create_role n/C++ / .NET Engineer"));
    }

    @Test
    void parse_uppercaseCommandAndPrefixes_preservesNameCase() throws Exception {
        assertEquals(new CreateRoleCommand("Engineer"), parser.parseCommand("CREATE_ROLE N/Engineer"));
        assertEquals(new EditRoleCommand("Engineer", "Designer"),
                parser.parseCommand("EDIT_ROLE R/Engineer N/Designer"));
        assertEquals(new DeleteRoleCommand("Engineer"), parser.parseCommand("DELETE_ROLE R/Engineer"));
        assertEquals(new ListRoleCommand(), parser.parseCommand("LIST_ROLE"));
    }

    @Test
    void parse_missingOrUnexpectedArguments_rejects() {
        for (String input : List.of("create_role", "create_role Engineer", "create_role unexpected n/Engineer",
                "create_role r/Engineer", "edit_role r/Engineer", "edit_role n/New", "delete_role",
                "delete_role n/Engineer", "list_role extra", "list_role n/Engineer")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
        }
        String message = "Unexpected role parameter: p/. Usage: create_role n/NAME";
        assertThrows(ParseException.class, message, () -> parser.parseCommand("create_role n/Engineer p/123"));
    }

    @Test
    void parse_blankOrControlCharacterName_rejectsWithConstraints() {
        for (String input : List.of("create_role n/", "edit_role r/Engineer n/  ", "delete_role r/ ",
                "create_role n/Line\nBreak")) {
            assertThrows(ParseException.class, Role.MESSAGE_CONSTRAINTS, () -> parser.parseCommand(input));
        }
    }

    @Test
    void parse_repeatedPrefixes_rejectsInsteadOfTakingLast() {
        String duplicateName = Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NAME);
        String duplicateCreate = "create_role n/Engineer N/Designer";
        assertThrows(ParseException.class, duplicateName, () -> parser.parseCommand(duplicateCreate));
        String duplicateRole = Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ROLE);
        String duplicateDelete = "delete_role r/Engineer r/Designer";
        assertThrows(ParseException.class, duplicateRole, () -> parser.parseCommand(duplicateDelete));
        assertThrows(ParseException.class, () -> parser.parseCommand("edit_role r/Old n/New n/Other"));
    }

    @Test
    void parse_surroundingWhitespace_preservesInternalSpacing() throws Exception {
        assertEquals(new CreateRoleCommand("Software  Engineer"),
                parser.parseCommand("  create_role   n/ Software  Engineer   "));
        assertEquals(new CreateRoleCommand("Engineer"), parser.parseCommand("create_role\tn/Engineer"));
        assertEquals(new EditRoleCommand("Old", "New"), parser.parseCommand("edit_role\tr/Old\tn/New"));
    }
}
