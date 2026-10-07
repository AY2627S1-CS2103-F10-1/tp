package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;

public class StatusCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexFilteredList_updatesCandidateAndShowsAll() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person candidate = model.getFilteredPersonList().get(0);
        StatusCommand command = new StatusCommand(INDEX_FIRST_PERSON, Status.ACCEPTED);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Person updated = new Person(candidate.getName(), candidate.getPhone(), candidate.getEmail(),
                candidate.getAddress(), candidate.getTags(), Status.ACCEPTED);
        expectedModel.setPerson(candidate, updated);
        String expectedMessage = String.format(StatusCommand.MESSAGE_SUCCESS,
                candidate.getName(), candidate.getStatus(), Status.ACCEPTED);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(expectedModel.getAddressBook().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidIndex_throwsCommandExceptionWithoutChange() {
        StatusCommand command = new StatusCommand(Index.fromOneBased(model.getFilteredPersonList().size() + 1),
                Status.OFFERED);
        assertCommandFailure(command, model, StatusCommand.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void constructor_nullStatus_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StatusCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void equals_matchingValues_returnsTrueAndMatchingHashCode() {
        StatusCommand first = new StatusCommand(INDEX_FIRST_PERSON, Status.REJECTED);
        StatusCommand same = new StatusCommand(INDEX_FIRST_PERSON, Status.REJECTED);
        assertTrue(first.equals(same));
        assertEquals(first.hashCode(), same.hashCode());
        assertFalse(first.equals(new StatusCommand(INDEX_SECOND_PERSON, Status.REJECTED)));
    }
}
