package seedu.address.logic.commands;

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
        String expectedMessage = String.format(FilterCommand.MESSAGE_SUCCESS, 7, status.getDisplayName());
        FilterCommand command = new FilterCommand(status);
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(new StatusContainsKeywordPredicate(status));
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

}
