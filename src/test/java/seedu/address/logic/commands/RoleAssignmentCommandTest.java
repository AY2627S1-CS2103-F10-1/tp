package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.model.role.Role;
import seedu.address.testutil.PersonBuilder;

/** Tests command-boundary invariants independently of parser validation. */
class RoleAssignmentCommandTest {
    @Test
    void constructor_emptyAddRoles_rejectsMissingAssignments() {
        Person person = new PersonBuilder().build();
        assertThrows(IllegalArgumentException.class, AddCommand.MESSAGE_MISSING_ROLE, () ->
                new AddCommand(person, Set.of()));
    }

    @Test
    void setRoleNames_emptySet_rejectsClearingAllAssignments() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        assertThrows(IllegalArgumentException.class, AddCommand.MESSAGE_MISSING_ROLE, () ->
                descriptor.setRoleNames(Set.of()));
    }

    @Test
    void constructor_emptyFilterCriteria_rejectsUnrestrictedFilter() {
        assertThrows(IllegalArgumentException.class, "At least one filter criterion is required.", () ->
                new FilterCommand(List.of(), Set.of()));
    }

    @Test
    void setRoleNames_mutatedSourceAndCopy_preservesIndependentAssignments() {
        Set<String> names = new HashSet<>(Set.of("Developer"));
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setRoleNames(names);
        EditPersonDescriptor copy = new EditPersonDescriptor(descriptor);

        names.clear();
        descriptor.setRoleNames(Set.of("Designer"));

        assertEquals(Set.of("Developer"), copy.getRoleNames().orElseThrow());
        assertEquals(Set.of("Designer"), descriptor.getRoleNames().orElseThrow());
        assertThrows(UnsupportedOperationException.class, () -> copy.getRoleNames().orElseThrow().clear());
        assertFalse(copy.equals(descriptor));
    }

    @Test
    void equals_sameDetailsDifferentRoles_returnsFalse() {
        Person person = new PersonBuilder().build();
        assertFalse(new AddCommand(person, Set.of("Developer"))
                .equals(new AddCommand(person, Set.of("Designer"))));
        assertFalse(new FilterCommand(List.of(Status.APPLIED), Set.of("Developer"))
                .equals(new FilterCommand(List.of(Status.APPLIED), Set.of("Designer"))));
    }

    @Test
    void execute_invalidRoleNameWithoutParser_reportsCommandErrorAndPreservesModel() {
        Person person = new PersonBuilder().build();
        Set<String> invalidNames = Set.of("Developer\tRole");
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setRoleNames(invalidNames);
        List<Command> commands = List.of(new AddCommand(person, invalidNames),
                new EditCommand(Index.fromOneBased(1), descriptor), new FilterCommand(List.of(), invalidNames));
        for (Command command : commands) {
            ModelManager model = new ModelManager();
            model.addPerson(person);
            assertCommandFailure(command, model, Role.MESSAGE_CONSTRAINTS);
        }
    }
}
