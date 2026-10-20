package seedu.address.model.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.StatusCommand;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.testutil.PersonBuilder;

class RoleFoundationTest {
    @Test
    void createRole_caseAndWhitespaceConflict_leavesCatalogueUnchanged() {
        ModelManager model = new ModelManager();
        model.createRole("  Software Engineer  ");
        RoleCatalogue before = model.getRoleCatalogue();

        assertThrows(IllegalArgumentException.class, () -> model.createRole("software engineer"));
        assertThrows(IllegalArgumentException.class, () -> model.createRole(" NO ROLE "));
        assertThrows(IllegalArgumentException.class, () -> model.createRole(" \t "));
        assertEquals(before, model.getRoleCatalogue());
        assertEquals("Software Engineer", before.requireRole(" SOFTWARE ENGINEER ").getName());
        model.createRole("Software  Engineer");
        assertEquals(3, model.getRoleCatalogue().getRoles().size());
    }

    @Test
    void manageRole_protectedRole_rejectsWithoutMutation() {
        ModelManager model = new ModelManager();
        assertThrows(IllegalArgumentException.class, () -> model.renameRole("no role", "Other"));
        assertThrows(IllegalArgumentException.class, () -> model.deleteRole(" No role "));
        assertEquals(List.of(Role.NO_ROLE), model.getRoleCatalogue().getRoles());
    }

    @Test
    void renameRole_peopleOutsideFilter_updatesAllAndPreservesIdentity() {
        ModelManager model = assignedModel();
        Role original = model.getRoleCatalogue().requireRole("Engineer");
        model.updateFilteredPersonList(person -> false);

        model.renameRole(" engineer ", "Senior Engineer");

        Role renamed = model.getRoleCatalogue().requireRole("Senior Engineer");
        assertEquals(original.getId(), renamed.getId());
        assertTrue(model.getAddressBook().getPersonList().getFirst().getRoles().contains(renamed));
        assertFalse(model.getRoleCatalogue().find("Engineer").isPresent());
        assertTrue(model.getFilteredPersonList().isEmpty());
        model.renameRole("Senior Engineer", "SENIOR ENGINEER");
        assertEquals("SENIOR ENGINEER", model.getAddressBook().getPersonList().getFirst()
                .getRoles().iterator().next().getName());
    }

    @Test
    void deleteRole_remainingAndLastAssignments_preservesOthersThenUsesFallback() {
        ModelManager model = assignedModel();
        model.createRole("Designer");
        Person person = model.getAddressBook().getPersonList().getFirst();
        model.replacePersonRoles(person, List.of("Engineer", "Designer"));

        model.deleteRole("Engineer");
        assertEquals(Set.of(model.getRoleCatalogue().requireRole("Designer")),
                model.getAddressBook().getPersonList().getFirst().getRoles());
        model.deleteRole("Designer");
        assertEquals(Set.of(Role.NO_ROLE), model.getAddressBook().getPersonList().getFirst().getRoles());
    }

    @Test
    void replaceRoles_noRoleAlongsideOtherRole_preservesBoth() {
        ModelManager model = assignedModel();
        model.replacePersonRoles(model.getAddressBook().getPersonList().getFirst(), List.of("No role", "Engineer"));
        assertEquals(2, model.getAddressBook().getPersonList().getFirst().getRoles().size());
        model.deleteRole("Engineer");
        assertEquals(Set.of(Role.NO_ROLE), model.getAddressBook().getPersonList().getFirst().getRoles());
    }

    @Test
    void manageRoles_invalidOperations_preservesPeopleAndCatalogue() {
        ModelManager model = assignedModel();
        model.createRole("Designer");
        AddressBook before = new AddressBook(model.getAddressBook());
        Person person = before.getPersonList().getFirst();

        assertThrows(IllegalArgumentException.class, () -> model.renameRole("Engineer", " designer "));
        assertThrows(IllegalArgumentException.class, () -> model.deleteRole("Missing"));
        assertThrows(IllegalArgumentException.class, () -> model.replacePersonRoles(person, List.of()));
        assertThrows(IllegalArgumentException.class, () -> model.replacePersonRoles(person, List.of("Missing")));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    void existingCommands_personWithRoles_preservesAssignments() throws Exception {
        ModelManager model = assignedModel();
        Set<Role> roles = model.getAddressBook().getPersonList().getFirst().getRoles();
        new NoteCommand(Index.fromOneBased(1), new Note("Feedback")).execute(model);
        new StatusCommand(Index.fromOneBased(1), Status.ACCEPTED).execute(model);
        EditCommand.EditPersonDescriptor descriptor = new EditCommand.EditPersonDescriptor();
        descriptor.setName(new Name("Changed Name"));
        new EditCommand(Index.fromOneBased(1), descriptor).execute(model);
        assertEquals(roles, model.getAddressBook().getPersonList().getFirst().getRoles());
    }

    @Test
    void catalogue_invalidReferencesAndEntries_rejects() {
        Role role = Role.create("Engineer");
        assertThrows(IllegalArgumentException.class, () -> new RoleCatalogue(List.of(role)));
        assertThrows(IllegalArgumentException.class, () -> new RoleCatalogue(List.of(Role.NO_ROLE, role, role)));
        assertThrows(IllegalArgumentException.class, () -> new Role("bad-id", "Engineer"));
        assertThrows(IllegalArgumentException.class, () -> Role.create("No role"));
        assertThrows(IllegalArgumentException.class, () -> Role.create("Line\nBreak"));
        assertThrows(IllegalArgumentException.class, () -> new RoleCatalogue().validateReferences(Set.of(role)));
        Person person = new PersonBuilder().build();
        assertEquals(Set.of(Role.NO_ROLE), person.getRoles());
        assertThrows(IllegalArgumentException.class, () -> person.withRoles(Set.of()));
        assertThrows(UnsupportedOperationException.class, () -> person.getRoles().clear());
    }

    @Test
    void role_valueSemantics_preservesNameAndIdentity() {
        Role role = Role.create(" Engineer ");
        Role copy = new Role(role.getId(), "Engineer");
        assertEquals(role, copy);
        assertEquals(role.hashCode(), copy.hashCode());
        assertEquals("Engineer", role.toString());
        assertFalse(role.equals(role.renamed("Senior Engineer")));
        assertFalse(role.equals("Engineer"));
        RoleCatalogue first = new RoleCatalogue(List.of(role, Role.NO_ROLE));
        RoleCatalogue second = new RoleCatalogue(List.of(Role.NO_ROLE, copy));
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    private ModelManager assignedModel() {
        ModelManager model = new ModelManager();
        model.createRole("Engineer");
        Person person = new PersonBuilder().build();
        model.addPerson(person);
        model.replacePersonRoles(person, List.of("Engineer"));
        return model;
    }
}
