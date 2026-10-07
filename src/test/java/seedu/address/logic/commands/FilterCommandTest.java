package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Status;
import seedu.address.model.person.StatusContainsKeywordPredicate;

/**
 * Contains integration tests (interaction with the Model) and unit tests for FilterCommand.
 */
public class FilterCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_filterByStatus_success() {
        Status status = Status.APPLIED;
        FilterCommand command = new FilterCommand(status);
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(new StatusContainsKeywordPredicate(status));
        String expectedMessage = String.format(FilterCommand.MESSAGE_SUCCESS,
                expectedModel.getFilteredPersonList().size(), status.getDisplayName());
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filterByStatusNoMatch_showsZeroCount() {
        Status status = Status.HIRED;
        FilterCommand command = new FilterCommand(status);
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(new StatusContainsKeywordPredicate(status));
        String expectedMessage = String.format(FilterCommand.MESSAGE_SUCCESS,
                expectedModel.getFilteredPersonList().size(), status.getDisplayName());
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void equals() {
        FilterCommand filterAppliedCommand = new FilterCommand(Status.APPLIED);
        FilterCommand filterHiredCommand = new FilterCommand(Status.HIRED);

        // same object -> returns true
        assertEquals(filterAppliedCommand, filterAppliedCommand);

        // same values -> returns true
        FilterCommand filterAppliedCommandCopy = new FilterCommand(Status.APPLIED);
        assertEquals(filterAppliedCommand, filterAppliedCommandCopy);

        // different types -> returns false
        assertFalse(filterAppliedCommand.equals(1));

        // null -> returns false
        assertFalse(filterAppliedCommand.equals(null));

        // different status -> returns false
        assertFalse(filterAppliedCommand.equals(filterHiredCommand));
    }

    @Test
    public void toStringMethod() {
        FilterCommand filterCommand = new FilterCommand(Status.APPLIED);
        StatusContainsKeywordPredicate predicate = new StatusContainsKeywordPredicate(Status.APPLIED);
        String expected = FilterCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, filterCommand.toString());
    }

}
