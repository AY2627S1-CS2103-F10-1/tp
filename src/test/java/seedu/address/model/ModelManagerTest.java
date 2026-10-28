package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
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
}
