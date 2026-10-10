package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ExpandCommandTest {
    private final ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_firstAndLastIndex_showsOnlySelectedPerson() {
        AddressBook original = new AddressBook(model.getAddressBook());
        for (int index : new int[] {1, original.getPersonList().size()}) {
            model.updateFilteredPersonList(person -> true);
            Person selected = model.getFilteredPersonList().get(index - 1);
            ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
            expectedModel.expandPerson(selected);

            assertCommandSuccess(new ExpandCommand(Index.fromOneBased(index)), model,
                    String.format(ExpandCommand.MESSAGE_SUCCESS, selected.getName()), expectedModel);
            assertTrue(model.isExpandedViewProperty().get());
            assertEquals(original, model.getAddressBook());
        }
    }

    @Test
    public void execute_filteredList_usesDisplayedIndex() {
        Person selected = model.getFilteredPersonList().get(2);
        model.updateFilteredPersonList(selected::equals);
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(selected::equals);
        expectedModel.expandPerson(selected);

        assertCommandSuccess(new ExpandCommand(Index.fromOneBased(1)), model,
                String.format(ExpandCommand.MESSAGE_SUCCESS, selected.getName()), expectedModel);

        assertEquals(List.of(selected), model.getFilteredPersonList());
        assertTrue(model.isExpandedViewProperty().get());
    }

    @Test
    public void execute_candidateWithNote_preservesFullNote() {
        Person original = model.getFilteredPersonList().getFirst();
        String text = "Feedback\n" + "\ud83d\ude00".repeat(491);
        Person selected = new PersonBuilder(original).withNote(text).build();
        model.setPerson(original, selected);
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.expandPerson(selected);

        assertCommandSuccess(new ExpandCommand(Index.fromOneBased(1)), model,
                String.format(ExpandCommand.MESSAGE_SUCCESS, selected.getName()), expectedModel);

        assertEquals(text, model.getFilteredPersonList().getFirst().getNote().orElseThrow().value);
    }

    @Test
    public void execute_outOfRange_throwsCommandExceptionAndPreservesExpandedView() {
        Person selected = model.getFilteredPersonList().getFirst();
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.expandPerson(selected);
        assertCommandSuccess(new ExpandCommand(Index.fromOneBased(1)), model,
                String.format(ExpandCommand.MESSAGE_SUCCESS, selected.getName()), expectedModel);

        assertCommandFailure(new ExpandCommand(Index.fromOneBased(2)), model,
                String.format(ExpandCommand.MESSAGE_OUT_OF_RANGE, 1));

        assertEquals(List.of(selected), model.getFilteredPersonList());
        assertTrue(model.isExpandedViewProperty().get());
    }

    @Test
    public void execute_emptyList_throwsCommandExceptionAndReportsEmptyList() {
        model.updateFilteredPersonList(person -> false);

        assertCommandFailure(new ExpandCommand(Index.fromOneBased(1)), model, ExpandCommand.MESSAGE_EMPTY_LIST);
        assertFalse(model.isExpandedViewProperty().get());
    }

    @Test
    public void execute_repeatedExpansion_remainsExpanded() {
        Person selected = model.getFilteredPersonList().getFirst();
        ExpandCommand command = new ExpandCommand(Index.fromOneBased(1));
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.expandPerson(selected);

        assertCommandSuccess(command, model, String.format(ExpandCommand.MESSAGE_SUCCESS, selected.getName()),
                expectedModel);
        assertCommandSuccess(command, model, String.format(ExpandCommand.MESSAGE_SUCCESS, selected.getName()),
                expectedModel);
        assertTrue(model.isExpandedViewProperty().get());
    }

    @Test
    public void equals_sameAndDifferentIndices_obeysValueSemantics() {
        ExpandCommand command = new ExpandCommand(Index.fromOneBased(1));
        ExpandCommand copy = new ExpandCommand(Index.fromOneBased(1));

        assertEquals(command, copy);
        assertEquals(command.hashCode(), copy.hashCode());
        assertNotEquals(command, new ExpandCommand(Index.fromOneBased(2)));
        assertNotEquals(command, null);
        assertNotEquals(command, "expand");
        assertTrue(command.toString().contains("targetIndex="));
    }

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ExpandCommand(null));
    }
}
