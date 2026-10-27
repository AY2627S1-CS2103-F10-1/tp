package seedu.address.model.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalRoleAssignments.getAssignedModel;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ClearCommand;
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
    void constructor_unpreparedOrInvalidName_rejects() {
        String id = "11111111-1111-1111-1111-111111111111";
        for (String name : List.of("", " ", " Engineer", "Engineer ", "Line\nBreak")) {
            assertThrows(IllegalArgumentException.class, Role.MESSAGE_CONSTRAINTS, () -> new Role(id, name));
        }
        assertThrows(NullPointerException.class, () -> new Role(id, null));
        assertThrows(NullPointerException.class, () -> new Role(null, "Engineer"));
        assertThrows(IllegalArgumentException.class,
                "The built-in No role identity and name are reserved.", () -> new Role(id, "NO ROLE"));
    }

    @Test
    void factoriesAndModel_paddedNames_preservePreparedDisplayAndLookup() {
        Role role = Role.create("\u2003 Software  Engineer \u2003");
        assertEquals("Software  Engineer", role.getName());
        Role renamed = role.renamed("\t Senior  Engineer \t");
        assertEquals("Senior  Engineer", renamed.getName());
        assertEquals(role.getId(), renamed.getId());
        assertEquals("senior  engineer", Role.toLookupKey(" Senior  ENGINEER "));
        ModelManager model = new ModelManager();
        model.createRole(" Engineer ");
        model.renameRole(" ENGINEER ", " Senior  Engineer ");
        assertEquals("Senior  Engineer", model.getRoleCatalog().requireRole(" senior  ENGINEER ").getName());
        assertThrows(NullPointerException.class, () -> Role.create(null));
        assertThrows(NullPointerException.class, () -> role.renamed(null));
    }

    @Test
    void createRole_caseAndWhitespaceConflict_leavesCatalogUnchanged() {
        ModelManager model = new ModelManager();
        model.createRole("  Software Engineer  ");
        RoleCatalog before = model.getRoleCatalog();

        assertThrows(IllegalArgumentException.class, () -> model.createRole("software engineer"));
        assertThrows(IllegalArgumentException.class, () -> model.createRole(" NO ROLE "));
        assertThrows(IllegalArgumentException.class, () -> model.createRole(" \t "));
        assertEquals(before, model.getRoleCatalog());
        assertEquals("Software Engineer", before.requireRole(" SOFTWARE ENGINEER ").getName());
        model.createRole("Software  Engineer");
        assertEquals(3, model.getRoleCatalog().getRoles().size());
    }

    @Test
    void manageRole_protectedRole_rejectsWithoutMutation() {
        ModelManager model = new ModelManager();
        assertThrows(IllegalArgumentException.class, () -> model.renameRole("no role", "Other"));
        assertThrows(IllegalArgumentException.class, () -> model.deleteRole(" No role "));
        assertEquals(List.of(Role.NO_ROLE), model.getRoleCatalog().getRoles());
    }

    @Test
    void renameRole_peopleOutsideFilter_updatesAllAndPreservesIdentity() {
        ModelManager model = getAssignedModel("Engineer");
        Role original = model.getRoleCatalog().requireRole("Engineer");
        model.updateFilteredPersonList(person -> false);

        model.renameRole(" engineer ", "Senior Engineer");

        Role renamed = model.getRoleCatalog().requireRole("Senior Engineer");
        assertEquals(original.getId(), renamed.getId());
        assertTrue(model.getAddressBook().getPersonList().getFirst().getRoles().contains(renamed));
        assertFalse(model.getRoleCatalog().find("Engineer").isPresent());
        assertTrue(model.getFilteredPersonList().isEmpty());
        model.renameRole("Senior Engineer", "SENIOR ENGINEER");
        assertEquals("SENIOR ENGINEER", model.getAddressBook().getPersonList().getFirst()
                .getRoles().iterator().next().getName());
    }

    @Test
    void renameRole_expandedAssignedPerson_preservesExpandedSelection() {
        ModelManager model = getAssignedModel("Engineer");
        model.addPerson(new PersonBuilder().withName("Other Candidate")
                .withPhone("81234567").withEmail("other@example.com").build());
        model.expandPerson(model.getFilteredPersonList().getFirst());

        model.renameRole("Engineer", "Senior Engineer");

        assertTrue(model.isExpandedViewProperty().get());
        assertEquals(List.of(model.getAddressBook().getPersonList().getFirst()), model.getFilteredPersonList());
        assertEquals(Set.of(model.getRoleCatalog().requireRole("Senior Engineer")),
                model.getFilteredPersonList().getFirst().getRoles());
    }

    @Test
    void deleteRole_expandedAssignedPerson_preservesExpandedSelection() {
        ModelManager model = getAssignedModel("Engineer");
        model.addPerson(new PersonBuilder().withName("Other Candidate")
                .withPhone("81234567").withEmail("other@example.com").build());
        model.expandPerson(model.getFilteredPersonList().getFirst());

        model.deleteRole("Engineer");

        assertTrue(model.isExpandedViewProperty().get());
        assertEquals(List.of(model.getAddressBook().getPersonList().getFirst()), model.getFilteredPersonList());
        assertEquals(Set.of(Role.NO_ROLE), model.getFilteredPersonList().getFirst().getRoles());
    }

    @Test
    void roleUpdates_expandedUnassignedPerson_preservesExpandedSelection() {
        ModelManager model = getAssignedModel("Engineer");
        Person unassignedPerson = new PersonBuilder().withName("Other Candidate")
                .withPhone("81234567").withEmail("other@example.com").build();
        model.addPerson(unassignedPerson);
        model.expandPerson(unassignedPerson);

        model.renameRole("Engineer", "Senior Engineer");

        assertTrue(model.isExpandedViewProperty().get());
        assertEquals(List.of(unassignedPerson), model.getFilteredPersonList());

        model.deleteRole("Senior Engineer");

        assertTrue(model.isExpandedViewProperty().get());
        assertEquals(List.of(unassignedPerson), model.getFilteredPersonList());
    }

    @Test
    void deleteRole_remainingAndLastAssignments_preservesOthersThenUsesFallback() {
        ModelManager model = getAssignedModel("Engineer");
        model.createRole("Designer");
        Person person = model.getAddressBook().getPersonList().getFirst();
        model.replacePersonRoles(person, List.of("Engineer", "Designer"));

        model.deleteRole("Engineer");
        assertEquals(Set.of(model.getRoleCatalog().requireRole("Designer")),
                model.getAddressBook().getPersonList().getFirst().getRoles());
        model.deleteRole("Designer");
        assertEquals(Set.of(Role.NO_ROLE), model.getAddressBook().getPersonList().getFirst().getRoles());
    }

    @Test
    void replaceRoles_noRoleAlongsideOtherRole_removesNoRole() {
        ModelManager model = getAssignedModel("Engineer");
        model.replacePersonRoles(model.getAddressBook().getPersonList().getFirst(), List.of("No role", "Engineer"));
        assertEquals(Set.of(model.getRoleCatalog().requireRole("Engineer")),
                model.getAddressBook().getPersonList().getFirst().getRoles());
        model.deleteRole("Engineer");
        assertEquals(Set.of(Role.NO_ROLE), model.getAddressBook().getPersonList().getFirst().getRoles());
    }

    @Test
    void manageRoles_invalidOperations_preservesPeopleAndCatalog() {
        ModelManager model = getAssignedModel("Engineer");
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
        ModelManager model = getAssignedModel("Engineer");
        Set<Role> roles = model.getAddressBook().getPersonList().getFirst().getRoles();
        new NoteCommand(Index.fromOneBased(1), new Note("Feedback")).execute(model);
        new StatusCommand(Index.fromOneBased(1), Status.ACCEPTED).execute(model);
        EditCommand.EditPersonDescriptor descriptor = new EditCommand.EditPersonDescriptor();
        descriptor.setName(new Name("Changed Name"));
        new EditCommand(Index.fromOneBased(1), descriptor).execute(model);
        assertEquals(roles, model.getAddressBook().getPersonList().getFirst().getRoles());
    }

    @Test
    void catalog_invalidReferencesAndEntries_rejects() {
        Role role = Role.create("Engineer");
        assertThrows(IllegalArgumentException.class, () -> new RoleCatalog(List.of(role)));
        assertThrows(IllegalArgumentException.class, () -> new RoleCatalog(List.of(Role.NO_ROLE, role, role)));
        assertThrows(IllegalArgumentException.class, () -> new Role("bad-id", "Engineer"));
        assertThrows(IllegalArgumentException.class, () -> Role.create("No role"));
        assertThrows(IllegalArgumentException.class, () -> Role.create("Line\nBreak"));
        assertThrows(IllegalArgumentException.class, () -> new RoleCatalog().validateReferences(Set.of(role)));
        Person person = new PersonBuilder().build();
        assertEquals(Set.of(Role.NO_ROLE), person.getRoles());
        assertThrows(IllegalArgumentException.class, () -> person.withRoles(Set.of()));
        assertThrows(UnsupportedOperationException.class, () -> person.getRoles().clear());
    }

    @Test
    void role_reservedIdentityAndName_preservesValidation() {
        assertEquals(Role.NO_ROLE, new Role(Role.NO_ROLE_ID, Role.NO_ROLE_NAME));
        assertThrows(IllegalArgumentException.class, () -> new Role(Role.NO_ROLE_ID, "Engineer"));
        assertThrows(IllegalArgumentException.class, () -> new Role(Role.NO_ROLE_ID, "no role"));
        assertThrows(IllegalArgumentException.class, () -> Role.create(" NO ROLE "));
    }

    @Test
    void createRole_embeddedControlCharacters_rejectsWithExamples() {
        for (String name : List.of("Software\tEngineer", "Software\nEngineer", "Software\rEngineer")) {
            assertThrows(IllegalArgumentException.class, Role.MESSAGE_CONSTRAINTS, () -> Role.create(name));
        }
    }

    @Test
    void manageRole_reservedCommandSyntax_rejectsWithoutMutation() {
        ModelManager model = getAssignedModel("Engineer");
        AddressBook before = new AddressBook(model.getAddressBook());
        String id = model.getRoleCatalog().requireRole("Engineer").getId();
        String message = Role.MESSAGE_RESERVED_SYNTAX;
        for (String prefix : List.of("n/", "r/", "p/", "e/", "a/", "t/", "no/", "s/", "N/", "NO/")) {
            String name = "Senior " + prefix + "Engineer";

            assertThrows(IllegalArgumentException.class, message, () -> model.createRole(name));
            assertThrows(IllegalArgumentException.class, message, () -> model.renameRole("Engineer", name));
            assertThrows(IllegalArgumentException.class, message, () -> new Role(id, name));
            assertEquals(before, model.getAddressBook());
        }
    }

    @Test
    void role_valueSemantics_preservesNameAndIdentity() {
        Role role = Role.create(" \tEngineer\t ");
        Role copy = new Role(role.getId(), "Engineer");
        assertEquals(role, copy);
        assertEquals(role.hashCode(), copy.hashCode());
        assertEquals("Engineer", role.toString());
        assertFalse(role.equals(role.renamed("Senior Engineer")));
        assertFalse(role.equals("Engineer"));
        RoleCatalog first = new RoleCatalog(List.of(role, Role.NO_ROLE));
        RoleCatalog second = new RoleCatalog(List.of(Role.NO_ROLE, copy));
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void clear_peopleWithRoles_preservesUnusedCatalogEntries() {
        ModelManager model = getAssignedModel("Engineer");
        RoleCatalog catalog = model.getRoleCatalog();
        new ClearCommand(true).execute(model);
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(catalog, model.getRoleCatalog());
    }
}
