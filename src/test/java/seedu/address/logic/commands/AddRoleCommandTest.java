package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalRoleAssignments.getAssignedModel;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.role.RoleCatalog;

class AddRoleCommandTest {
    @Test
    void execute_unusedRole_createsWithoutCandidates() throws Exception {
        ModelManager model = new ModelManager();

        assertEquals(String.format(AddRoleCommand.MESSAGE_SUCCESS, "Zoologist"),
                new AddRoleCommand("Zoologist").execute(model).getFeedbackToUser());

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    void equalsHashCodeAndToString_sameAndDifferentArguments_preservesValueSemantics() {
        AddRoleCommand create = new AddRoleCommand("Engineer");
        assertEquals(create, create);
        assertEquals(create, new AddRoleCommand("Engineer"));
        assertFalse(create.equals(null));
        assertFalse(create.equals("Engineer"));
        assertFalse(create.equals(new AddRoleCommand(" Engineer ")));
        assertEquals(create.hashCode(), new AddRoleCommand("Engineer").hashCode());
        assertFalse(create.equals(new AddRoleCommand("Designer")));
        assertEquals("add_role Engineer", create.toString());
    }

    @Test
    void execute_invalidManagementOperations_preservesCatalogAndPeople() {
        ModelManager model = getAssignedModel("Engineer", "No role");
        model.createRole("Designer");
        AddressBook before = new AddressBook(model.getAddressBook());

        assertThrows(CommandException.class, RoleCatalog.MESSAGE_DUPLICATE_NAME, () ->
                new AddRoleCommand(" ENGINEER ").execute(model));
        assertThrows(CommandException.class, () ->
                new AddRoleCommand("No role").execute(model));
        assertEquals(before, model.getAddressBook());
    }

}
