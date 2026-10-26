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
    void parseCommand_uppercaseCommandAndPrefixes_preservesNameCase() throws Exception {
        assertEquals(new CreateRoleCommand("Engineer"), dispatcher.parseCommand("CREATE_ROLE N/Engineer"));
        assertEquals(new EditRoleCommand("Engineer", "Designer"),
                dispatcher.parseCommand("EDIT_ROLE R/Engineer N/Designer"));
        assertEquals(new DeleteRoleCommand("Engineer"), dispatcher.parseCommand("DELETE_ROLE R/Engineer"));
        assertEquals(new ListRoleCommand(), dispatcher.parseCommand("LIST_ROLE"));
    }

    @Test
    void parseCommand_surroundingWhitespace_preservesInternalSpacing() throws Exception {
        assertEquals(new CreateRoleCommand("Software  Engineer"),
                dispatcher.parseCommand("  create_role   n/ Software  Engineer   "));
        assertEquals(new CreateRoleCommand("Engineer"), dispatcher.parseCommand("create_role\tn/Engineer"));
        assertEquals(new EditRoleCommand("Old", "New"), dispatcher.parseCommand("edit_role\tr/Old\tn/New"));
    }
}
