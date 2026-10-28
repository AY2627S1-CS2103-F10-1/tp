package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalRoleAssignments.getAssignedModel;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.role.Role;

class RoleCommandsTest {
    @Test
    void createAndList_unusedRoles_listsAllInPredictableOrder() throws Exception {
        ModelManager model = new ModelManager();
        assertEquals("Created role: Zoologist",
                new AddRoleCommand("Zoologist").execute(model).getFeedbackToUser());
        new AddRoleCommand("Analyst").execute(model);
        assertEquals("Roles:\nAnalyst\nNo role\nZoologist",
                new ListRoleCommand().execute(model).getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    void editRole_personOutsideDisplayedList_renamesGloballyWithoutReplacingAssignments() throws Exception {
        ModelManager model = getAssignedModel("Engineer", "No role");
        model.updateFilteredPersonList(person -> false);
        String id = model.getRoleCatalog().requireRole("Engineer").getId();

        assertEquals("Renamed role: engineer -> Senior Engineer",
                new EditRoleCommand("engineer", "Senior Engineer").execute(model).getFeedbackToUser());

        assertEquals(id, model.getRoleCatalog().requireRole("Senior Engineer").getId());
        Set<Role> assignments = model.getAddressBook().getPersonList().getFirst().getRoles();
        assertEquals(Set.of(Role.NO_ROLE, model.getRoleCatalog().requireRole("Senior Engineer")), assignments);
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    void deleteRole_multipleThenLastRole_preservesOthersAndUsesFallback() throws Exception {
        ModelManager model = getAssignedModel("Engineer", "No role");
        model.replacePersonRoles(model.getAddressBook().getPersonList().getFirst(), List.of("Engineer"));
        model.createRole("Designer");
        model.replacePersonRoles(model.getAddressBook().getPersonList().getFirst(), List.of("Engineer", "Designer"));

        assertEquals("Deleted role: Engineer",
                new DeleteRoleCommand("Engineer").execute(model).getFeedbackToUser());
        assertEquals(Set.of(model.getRoleCatalog().requireRole("Designer")),
                model.getAddressBook().getPersonList().getFirst().getRoles());
        new DeleteRoleCommand("Designer").execute(model);
        assertEquals(Set.of(Role.NO_ROLE), model.getAddressBook().getPersonList().getFirst().getRoles());
    }

    @Test
    void execute_invalidManagementOperations_preservesCatalogAndPeople() {
        ModelManager model = getAssignedModel("Engineer", "No role");
        model.createRole("Designer");
        AddressBook before = new AddressBook(model.getAddressBook());

        String duplicateMessage = "A role with this name already exists.";
        String protectedMessage = "No role cannot be renamed or deleted.";
        Command duplicate = new AddRoleCommand(" ENGINEER ");
        Command protectedRename = new EditRoleCommand("No role", "Other");
        assertThrows(CommandException.class, duplicateMessage, () -> duplicate.execute(model));
        assertThrows(CommandException.class, duplicateMessage, () ->
                new EditRoleCommand("Engineer", "designer").execute(model));
        assertThrows(CommandException.class, "Role does not exist: Missing", () ->
                new DeleteRoleCommand("Missing").execute(model));
        assertThrows(CommandException.class, protectedMessage, () -> protectedRename.execute(model));
        assertThrows(CommandException.class, protectedMessage, () -> new DeleteRoleCommand(" NO ROLE ").execute(model));
        assertThrows(CommandException.class, () -> new AddRoleCommand("No role").execute(model));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    void commands_valueSemantics_useTheirArguments() {
        AddRoleCommand create = new AddRoleCommand("Engineer");
        assertEquals(create, create);
        assertEquals(create, new AddRoleCommand("Engineer"));
        assertFalse(create.equals(null));
        assertFalse(create.equals("Engineer"));
        assertFalse(create.equals(new AddRoleCommand(" Engineer ")));
        assertEquals(create.hashCode(), new AddRoleCommand("Engineer").hashCode());
        assertFalse(create.equals(new AddRoleCommand("Designer")));
        assertEquals("add_role Engineer", create.toString());
        EditRoleCommand edit = new EditRoleCommand("Engineer", "Designer");
        assertEquals(edit, new EditRoleCommand("Engineer", "Designer"));
        assertEquals(edit.hashCode(), new EditRoleCommand("Engineer", "Designer").hashCode());
        assertFalse(edit.equals(new EditRoleCommand("Engineer", "Other")));
        assertEquals("edit_role Engineer -> Designer", edit.toString());
        DeleteRoleCommand delete = new DeleteRoleCommand("Engineer");
        assertEquals(delete, new DeleteRoleCommand("Engineer"));
        assertEquals(delete.hashCode(), new DeleteRoleCommand("Engineer").hashCode());
        assertFalse(delete.equals(new DeleteRoleCommand("Designer")));
        assertEquals("delete_role Engineer", delete.toString());
        assertEquals(new ListRoleCommand(), new ListRoleCommand());
        assertEquals(new ListRoleCommand().hashCode(), new ListRoleCommand().hashCode());
        assertEquals("list_role", new ListRoleCommand().toString());
    }
}
