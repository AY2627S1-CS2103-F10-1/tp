package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CreateRoleCommand;
import seedu.address.logic.commands.DeleteRoleCommand;
import seedu.address.logic.commands.EditRoleCommand;
import seedu.address.logic.commands.ListRoleCommand;

class RoleCommandDispatchTest {
    private final AddressBookParser dispatcher = new AddressBookParser();

    @Test
    void parseCommand_uppercaseAndMixedCaseCommands_preservesNameCase() throws Exception {
        for (String command : new String[] {"CREATE_ROLE", "Create_Role"}) {
            assertEquals(new CreateRoleCommand("Engineer"), dispatcher.parseCommand(command + " N/Engineer"));
        }
        for (String command : new String[] {"EDIT_ROLE", "Edit_Role"}) {
            assertEquals(new EditRoleCommand("Engineer", "Designer"),
                    dispatcher.parseCommand(command + " R/Engineer N/Designer"));
        }
        for (String command : new String[] {"DELETE_ROLE", "Delete_Role"}) {
            assertEquals(new DeleteRoleCommand("Engineer"), dispatcher.parseCommand(command + " R/Engineer"));
        }
        for (String command : new String[] {"LIST_ROLE", "List_Role"}) {
            assertEquals(new ListRoleCommand(), dispatcher.parseCommand(command));
        }
    }

    @Test
    void parseCommand_lowercaseCommandAndUppercasePrefixes_preservesNameCase() throws Exception {
        assertEquals(new CreateRoleCommand("Engineer"), dispatcher.parseCommand("create_role N/Engineer"));
        assertEquals(new EditRoleCommand("Engineer", "Designer"),
                dispatcher.parseCommand("edit_role R/Engineer N/Designer"));
        assertEquals(new DeleteRoleCommand("Engineer"), dispatcher.parseCommand("delete_role R/Engineer"));
        assertEquals(new ListRoleCommand(), dispatcher.parseCommand("list_role"));
    }

    @Test
    void parseCommand_surroundingWhitespace_preservesInternalSpacing() throws Exception {
        assertEquals(new CreateRoleCommand("Software  Engineer"),
                dispatcher.parseCommand("  create_role   n/ Software  Engineer   "));
        assertEquals(new CreateRoleCommand("Engineer"), dispatcher.parseCommand("create_role\tn/Engineer"));
        assertEquals(new EditRoleCommand("Old", "New"), dispatcher.parseCommand("edit_role\tr/Old\tn/New"));
    }
}
