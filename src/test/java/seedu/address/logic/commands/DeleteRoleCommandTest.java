package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalRoleAssignments.getAssignedModel;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.role.Role;
import seedu.address.model.role.RoleCatalog;

class DeleteRoleCommandTest {
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
    void equalsHashCodeAndToString_sameAndDifferentArguments_preservesValueSemantics() {
        DeleteRoleCommand delete = new DeleteRoleCommand("Engineer");
        assertEquals(delete, new DeleteRoleCommand("Engineer"));
        assertEquals(delete.hashCode(), new DeleteRoleCommand("Engineer").hashCode());
        assertFalse(delete.equals(new DeleteRoleCommand("Designer")));
        assertEquals("delete_role Engineer", delete.toString());
    }

    @Test
    void execute_invalidManagementOperations_preservesCatalogAndPeople() {
        ModelManager model = getAssignedModel("Engineer", "No role");
        model.createRole("Designer");
        AddressBook before = new AddressBook(model.getAddressBook());

        assertThrows(CommandException.class, String.format(RoleCatalog.MESSAGE_UNKNOWN_ROLE, "Missing"), () ->
                new DeleteRoleCommand("Missing").execute(model));
        assertThrows(CommandException.class, Role.MESSAGE_PROTECTED_ROLE, () ->
                new DeleteRoleCommand(" NO ROLE ").execute(model));
        assertEquals(before, model.getAddressBook());
    }

}
