package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ExpandedViewTest {
    private final ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void expandPerson_personOutsideDisplayedList_preservesView() {
        Person selected = model.getFilteredPersonList().getFirst();
        Person other = model.getFilteredPersonList().get(1);
        model.expandPerson(selected);

        assertThrows(IllegalArgumentException.class, () -> model.expandPerson(other));

        assertTrue(model.expandedViewProperty().get());
        assertEquals(selected, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void updateFilteredPersonList_singlePersonList_resetsExpandedView() {
        Person selected = model.getFilteredPersonList().getFirst();
        model.expandPerson(selected);

        model.updateFilteredPersonList(selected::equals);

        assertFalse(model.expandedViewProperty().get());
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void updateFilteredPersonList_showAll_restoresAllPersons() {
        int size = model.getFilteredPersonList().size();
        model.expandPerson(model.getFilteredPersonList().getFirst());

        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);

        assertFalse(model.expandedViewProperty().get());
        assertEquals(size, model.getFilteredPersonList().size());
    }

    @Test
    public void deletePerson_expandedPerson_resetsExpandedView() {
        Person selected = model.getFilteredPersonList().getFirst();
        model.expandPerson(selected);

        model.deletePerson(selected);

        assertFalse(model.expandedViewProperty().get());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void setPerson_expandedPerson_resetsExpandedView() {
        Person selected = model.getFilteredPersonList().getFirst();
        model.expandPerson(selected);

        model.setPerson(selected, new PersonBuilder(selected).withPhone("91234567").build());

        assertFalse(model.expandedViewProperty().get());
    }

    @Test
    public void setAddressBook_clearData_resetsExpandedView() {
        model.expandPerson(model.getFilteredPersonList().getFirst());

        model.setAddressBook(new AddressBook());

        assertFalse(model.expandedViewProperty().get());
    }
}
