package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalRoleAssignments.getAssignedModel;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.StatusCommand;
import seedu.address.model.person.Name;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.model.role.Role;
import seedu.address.model.role.RoleCatalog;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void findSamePerson_nullCandidate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.findSamePerson(null));
    }

    @Test
    public void findSamePerson_distinctEmail_returnsEmpty() {
        modelManager.addPerson(ALICE);

        assertEquals(Optional.empty(), modelManager.findSamePerson(BENSON));
    }

    @Test
    public void findSamePerson_hiddenCaseInsensitiveMatch_returnsExistingRecordWithoutChangingView() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.expandPerson(BENSON);
        Person candidate = new PersonBuilder(ALICE).withName("Different Name")
                .withEmail(ALICE.getEmail().value.toUpperCase(Locale.ROOT)).build();

        assertEquals(Optional.of(ALICE), modelManager.findSamePerson(candidate));
        assertEquals(List.of(BENSON), modelManager.getFilteredPersonList());
        assertTrue(modelManager.isExpandedViewProperty().get());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }

    @Test
    public void expandPerson_personOutsideDisplayedList_preservesView() {
        ModelManager model = createModelWithTypicalPersons();
        Person selected = model.getFilteredPersonList().getFirst();
        Person other = model.getFilteredPersonList().get(1);
        model.expandPerson(selected);

        assertThrows(IllegalArgumentException.class, () -> model.expandPerson(other));

        assertTrue(model.isExpandedViewProperty().get());
        assertEquals(selected, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void updateFilteredPersonList_singlePersonList_resetsExpandedView() {
        ModelManager model = createModelWithTypicalPersons();
        Person selected = model.getFilteredPersonList().getFirst();
        model.expandPerson(selected);

        model.updateFilteredPersonList(selected::equals);

        assertFalse(model.isExpandedViewProperty().get());
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void updateFilteredPersonList_showAll_restoresAllPersons() {
        ModelManager model = createModelWithTypicalPersons();
        int size = model.getFilteredPersonList().size();
        model.expandPerson(model.getFilteredPersonList().getFirst());

        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);

        assertFalse(model.isExpandedViewProperty().get());
        assertEquals(size, model.getFilteredPersonList().size());
    }

    @Test
    public void deletePerson_expandedPerson_resetsExpandedView() {
        ModelManager model = createModelWithTypicalPersons();
        Person selected = model.getFilteredPersonList().getFirst();
        model.expandPerson(selected);

        model.deletePerson(selected);

        assertFalse(model.isExpandedViewProperty().get());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void setPerson_expandedPerson_keepsUpdatedCandidateExpanded() {
        ModelManager model = createModelWithTypicalPersons();
        Person selected = model.getFilteredPersonList().getFirst();
        model.expandPerson(selected);

        Person updated = new PersonBuilder(selected).withPhone("91234567").build();
        model.setPerson(selected, updated);

        assertTrue(model.isExpandedViewProperty().get());
        assertEquals(List.of(updated), model.getFilteredPersonList());
    }

    @Test
    public void setPerson_hiddenCandidate_keepsDifferentCandidateExpanded() {
        ModelManager model = createModelWithTypicalPersons();
        Person selected = model.getFilteredPersonList().getFirst();
        Person hidden = model.getFilteredPersonList().get(1);
        model.expandPerson(selected);

        model.setPerson(hidden, new PersonBuilder(hidden).withPhone("91234567").build());

        assertTrue(model.isExpandedViewProperty().get());
        assertEquals(List.of(selected), model.getFilteredPersonList());
    }

    @Test
    public void setAddressBook_clearData_resetsExpandedView() {
        ModelManager model = createModelWithTypicalPersons();
        model.expandPerson(model.getFilteredPersonList().getFirst());

        model.setAddressBook(new AddressBook());

        assertFalse(model.isExpandedViewProperty().get());
    }

    @Test
    public void equals_differentExpandedView_returnsFalse() {
        ModelManager collapsedModel = createModelWithTypicalPersons();
        ModelManager expandedModel = createModelWithTypicalPersons();
        Person selected = expandedModel.getFilteredPersonList().getFirst();
        collapsedModel.updateFilteredPersonList(selected::equals);
        expandedModel.expandPerson(selected);

        assertEquals(collapsedModel.getFilteredPersonList(), expandedModel.getFilteredPersonList());
        assertFalse(collapsedModel.isExpandedViewProperty().get());
        assertTrue(expandedModel.isExpandedViewProperty().get());
        assertFalse(collapsedModel.equals(expandedModel));
    }

    private ModelManager createModelWithTypicalPersons() {
        return new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void factoriesAndModel_paddedNames_preservePreparedDisplayAndLookup() {
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
    public void createRole_caseAndWhitespaceConflict_leavesCatalogUnchanged() {
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
    public void createAndRenameRole_canonicalUnicodeNames_shareLookupWithoutChangingDisplayOrIdentity() {
        ModelManager model = new ModelManager();
        model.createRole("Cafe\u0301");
        Role original = model.getRoleCatalog().requireRole("CAF\u00c9");
        assertEquals("Cafe\u0301", original.getName());
        assertThrows(IllegalArgumentException.class, RoleCatalog.MESSAGE_DUPLICATE_NAME, () ->
                model.createRole("Caf\u00e9"));
        model.createRole("Other");
        RoleCatalog before = model.getRoleCatalog();
        assertThrows(IllegalArgumentException.class, RoleCatalog.MESSAGE_DUPLICATE_NAME, () ->
                model.renameRole("Other", "Caf\u00e9"));
        assertEquals(before, model.getRoleCatalog());

        model.renameRole("Caf\u00e9", "CAF\u00c9");

        Role renamed = model.getRoleCatalog().requireRole("Cafe\u0301");
        assertEquals(original.getId(), renamed.getId());
        assertEquals("CAF\u00c9", renamed.getName());
        model.createRole("Caf e\u0301");
        assertEquals(4, model.getRoleCatalog().getRoles().size());
    }

    @Test
    public void manageRole_protectedRole_rejectsWithoutMutation() {
        ModelManager model = new ModelManager();
        assertThrows(IllegalArgumentException.class, () -> model.renameRole("no role", "Other"));
        assertThrows(IllegalArgumentException.class, () -> model.deleteRole(" No role "));
        assertEquals(List.of(Role.NO_ROLE), model.getRoleCatalog().getRoles());
    }

    @Test
    public void renameRole_peopleOutsideFilter_updatesAllAndPreservesIdentity() {
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
    public void renameRole_expandedAssignedPerson_preservesExpandedSelection() {
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
    public void deleteRole_expandedAssignedPerson_preservesExpandedSelection() {
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
    public void roleUpdates_expandedUnassignedPerson_preservesExpandedSelection() {
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
    public void deleteRole_remainingAndLastAssignments_preservesOthersThenUsesFallback() {
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
    public void replaceRoles_noRoleAlongsideOtherRole_removesNoRole() {
        ModelManager model = getAssignedModel("Engineer");
        model.replacePersonRoles(model.getAddressBook().getPersonList().getFirst(), List.of("No role", "Engineer"));
        assertEquals(Set.of(model.getRoleCatalog().requireRole("Engineer")),
                model.getAddressBook().getPersonList().getFirst().getRoles());
        model.deleteRole("Engineer");
        assertEquals(Set.of(Role.NO_ROLE), model.getAddressBook().getPersonList().getFirst().getRoles());
    }

    @Test
    public void manageRoles_invalidOperations_preservesPeopleAndCatalog() {
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
    public void existingCommands_personWithRoles_preservesAssignments() throws Exception {
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
    public void manageRole_reservedCommandSyntax_rejectsWithoutMutation() {
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
    public void clear_peopleWithRoles_preservesUnusedCatalogEntries() {
        ModelManager model = getAssignedModel("Engineer");
        RoleCatalog catalog = model.getRoleCatalog();
        new ClearCommand(true).execute(model);
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(catalog, model.getRoleCatalog());
    }
}
