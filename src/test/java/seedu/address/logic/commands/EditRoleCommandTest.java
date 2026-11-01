package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalRoleAssignments.getAssignedModel;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.role.Role;
import seedu.address.model.role.RoleCatalog;

class EditRoleCommandTest {
    @Test
    void editRole_personOutsideDisplayedList_renamesGloballyWithoutReplacingAssignments() throws Exception {
        ModelManager model = getAssignedModel("Engineer", "No role");
        model.updateFilteredPersonList(person -> false);
        String id = model.getRoleCatalog().requireRole("Engineer").getId();

        assertEquals("Renamed role: engineer -> Senior Engineer",
                new EditRoleCommand("engineer", "Senior Engineer").execute(model).getFeedbackToUser());

        assertEquals(id, model.getRoleCatalog().requireRole("Senior Engineer").getId());
        Set<Role> assignments = model.getAddressBook().getPersonList().getFirst().getRoles();
        assertEquals(Set.of(model.getRoleCatalog().requireRole("Senior Engineer")), assignments);
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    void equalsHashCodeAndToString_sameAndDifferentArguments_preservesValueSemantics() {
        EditRoleCommand edit = new EditRoleCommand("Engineer", "Designer");
        assertEquals(edit, new EditRoleCommand("Engineer", "Designer"));
        assertEquals(edit.hashCode(), new EditRoleCommand("Engineer", "Designer").hashCode());
        assertFalse(edit.equals(new EditRoleCommand("Engineer", "Other")));
        assertEquals("edit_role Engineer -> Designer", edit.toString());
    }

    @Test
    void execute_invalidManagementOperations_preservesCatalogAndPeople() {
        ModelManager model = getAssignedModel("Engineer", "No role");
        model.createRole("Designer");
        AddressBook before = new AddressBook(model.getAddressBook());

        assertThrows(CommandException.class, RoleCatalog.MESSAGE_DUPLICATE_NAME, () ->
                new EditRoleCommand("Engineer", "designer").execute(model));
        assertThrows(CommandException.class, Role.MESSAGE_PROTECTED_ROLE, () ->
                new EditRoleCommand("No role", "Other").execute(model));
        assertEquals(before, model.getAddressBook());
    }

}
