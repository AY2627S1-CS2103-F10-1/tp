package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddRoleCommand;
import seedu.address.logic.commands.DeleteRoleCommand;
import seedu.address.logic.commands.EditRoleCommand;
import seedu.address.logic.commands.ListRoleCommand;

class RoleCommandDispatchTest {
    private final AddressBookParser dispatcher = new AddressBookParser();

    @Test
    void parseCommand_uppercaseAndMixedCaseCommands_preservesNameCase() throws Exception {
        for (String command : new String[] {"ADD_ROLE", "Add_Role"}) {
            assertEquals(new AddRoleCommand("Engineer"), dispatcher.parseCommand(command + " N/Engineer"));
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
        assertEquals(new AddRoleCommand("Engineer"), dispatcher.parseCommand("add_role N/Engineer"));
        assertEquals(new EditRoleCommand("Engineer", "Designer"),
                dispatcher.parseCommand("edit_role R/Engineer N/Designer"));
        assertEquals(new DeleteRoleCommand("Engineer"), dispatcher.parseCommand("delete_role R/Engineer"));
        assertEquals(new ListRoleCommand(), dispatcher.parseCommand("list_role"));
    }

    @Test
    void parseCommand_surroundingWhitespace_preservesInternalSpacing() throws Exception {
        assertEquals(new AddRoleCommand("Software  Engineer"),
                dispatcher.parseCommand("  add_role   n/ Software  Engineer   "));
        assertEquals(new AddRoleCommand("Engineer"), dispatcher.parseCommand("add_role\tn/Engineer"));
        assertEquals(new EditRoleCommand("Old", "New"), dispatcher.parseCommand("edit_role\tr/Old\tn/New"));
    }
}
