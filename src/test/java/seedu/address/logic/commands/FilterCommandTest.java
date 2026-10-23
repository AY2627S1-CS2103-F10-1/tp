package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.model.person.StatusContainsKeywordPredicate;
import seedu.address.testutil.PersonBuilder;

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
                expectedModel.getFilteredPersonList().size(), status);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filterByStatusNoMatch_showsZeroCount() {
        Status status = Status.ACCEPTED;
        FilterCommand command = new FilterCommand(status);
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(new StatusContainsKeywordPredicate(status));
        assertTrue(expectedModel.getFilteredPersonList().isEmpty());
        String expectedMessage = String.format(FilterCommand.MESSAGE_SUCCESS, 0, status);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_multipleStatuses_showsCandidatesWithAnyStatus() {
        Person interviewing = new PersonBuilder().withName("Ivy Tan").withEmail("ivy@example.com")
                .withStatus(Status.INTERVIEWING).build();
        Person offered = new PersonBuilder().withName("Omar Lee").withEmail("omar@example.com")
                .withStatus(Status.OFFERED).build();
        Person applied = new PersonBuilder().withName("Amy Ng").withEmail("amy@example.com")
                .withStatus(Status.APPLIED).build();
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(interviewing, offered, applied));
        Model pipelineModel = new ModelManager(addressBook, new UserPrefs());
        FilterCommand command = new FilterCommand(List.of(Status.OFFERED, Status.INTERVIEWING));

        Model expectedModel = new ModelManager(addressBook, new UserPrefs());
        expectedModel.updateFilteredPersonList(
                new StatusContainsKeywordPredicate(List.of(Status.INTERVIEWING, Status.OFFERED)));
        String expectedMessage = String.format(FilterCommand.MESSAGE_SUCCESS, 2,
                Status.INTERVIEWING + " or " + Status.OFFERED);

        assertCommandSuccess(command, pipelineModel, expectedMessage, expectedModel);
        assertEquals(List.of(interviewing, offered), pipelineModel.getFilteredPersonList());
    }

    @Test
    public void equals() {
        FilterCommand filterAppliedCommand = new FilterCommand(Status.APPLIED);
        FilterCommand filterAcceptedCommand = new FilterCommand(Status.ACCEPTED);

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
        assertFalse(filterAppliedCommand.equals(filterAcceptedCommand));
    }

    @Test
    public void hashCode_equalCommands_sameHash() {
        FilterCommand c1 = new FilterCommand(Status.APPLIED);
        FilterCommand c2 = new FilterCommand(Status.APPLIED);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void toStringMethod() {
        FilterCommand filterCommand = new FilterCommand(Status.APPLIED);
        StatusContainsKeywordPredicate predicate = new StatusContainsKeywordPredicate(Status.APPLIED);
        String expected = FilterCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, filterCommand.toString());
    }

}
