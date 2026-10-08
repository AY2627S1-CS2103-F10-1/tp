package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ExpandCommandTest {
    private final ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_firstAndLastIndex_showsOnlySelectedPerson() throws Exception {
        AddressBook original = new AddressBook(model.getAddressBook());
        for (int index : new int[] {1, original.getPersonList().size()}) {
            model.updateFilteredPersonList(person -> true);
            Person selected = model.getFilteredPersonList().get(index - 1);

            CommandResult result = new ExpandCommand(Index.fromOneBased(index)).execute(model);

            assertEquals(String.format(ExpandCommand.MESSAGE_SUCCESS, selected.getName()), result.getFeedbackToUser());
            assertEquals(List.of(selected), model.getFilteredPersonList());
            assertTrue(model.isExpandedViewProperty().get());
            assertEquals(original, model.getAddressBook());
        }
    }

    @Test
    public void execute_filteredList_usesDisplayedIndex() throws Exception {
        Person selected = model.getFilteredPersonList().get(2);
        model.updateFilteredPersonList(selected::equals);

        new ExpandCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(selected), model.getFilteredPersonList());
        assertTrue(model.isExpandedViewProperty().get());
    }

    @Test
    public void execute_candidateWithNote_preservesFullNote() throws Exception {
        Person original = model.getFilteredPersonList().getFirst();
        String text = "Feedback\n" + "\ud83d\ude00".repeat(491);
        Person selected = new PersonBuilder(original).withNote(text).build();
        model.setPerson(original, selected);

        new ExpandCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(text, model.getFilteredPersonList().getFirst().getNote().orElseThrow().value);
    }

    @Test
    public void execute_outOfRange_preservesExpandedView() throws Exception {
        new ExpandCommand(Index.fromOneBased(1)).execute(model);
        Person selected = model.getFilteredPersonList().getFirst();

        assertThrows(CommandException.class, String.format(ExpandCommand.MESSAGE_OUT_OF_RANGE, 1), () ->
                new ExpandCommand(Index.fromOneBased(2)).execute(model));

        assertEquals(List.of(selected), model.getFilteredPersonList());
        assertTrue(model.isExpandedViewProperty().get());
    }

    @Test
    public void execute_emptyList_reportsEmptyList() {
        model.updateFilteredPersonList(person -> false);

        assertThrows(CommandException.class, ExpandCommand.MESSAGE_EMPTY_LIST, () ->
                new ExpandCommand(Index.fromOneBased(1)).execute(model));
        assertFalse(model.isExpandedViewProperty().get());
    }

    @Test
    public void execute_repeatedExpansion_remainsExpanded() throws Exception {
        ExpandCommand command = new ExpandCommand(Index.fromOneBased(1));
        CommandResult first = command.execute(model);

        assertEquals(first, command.execute(model));
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
