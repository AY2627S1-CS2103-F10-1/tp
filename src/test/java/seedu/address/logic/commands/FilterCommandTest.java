package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.CandidateMatchesFilterPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.model.role.Role;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for FilterCommand.
 * Each pipeline test builds a candidate set with varied statuses and asserts an explicit
 * expected list so a filter that matches everything or nothing would still fail.
 */
public class FilterCommandTest {

    private static final Person APPLIED_ONE = new PersonBuilder().withName("Amy Ng")
            .withEmail("amy@example.com").withStatus(Status.APPLIED).build();
    private static final Person APPLIED_TWO = new PersonBuilder().withName("Alex Lim")
            .withEmail("alex@example.com").withStatus(Status.APPLIED).build();
    private static final Person INTERVIEWING = new PersonBuilder().withName("Ivy Tan")
            .withEmail("ivy@example.com").withStatus(Status.INTERVIEWING).build();
    private static final Person OFFERED = new PersonBuilder().withName("Omar Lee")
            .withEmail("omar@example.com").withStatus(Status.OFFERED).build();
    private static final Person ACCEPTED = new PersonBuilder().withName("Carol Lim")
            .withEmail("carol@example.com").withStatus(Status.ACCEPTED).build();

    private Model pipelineModel() {
        AddressBook ab = new AddressBook();
        ab.setPersons(List.of(APPLIED_ONE, APPLIED_TWO, INTERVIEWING, OFFERED, ACCEPTED));
        return new ModelManager(ab, new UserPrefs());
    }

    @Test
    public void execute_filterByStatus_showsOnlyMatchingCandidates() {
        Model model = pipelineModel();
        Model expectedModel = pipelineModel();
        FilterCommand command = new FilterCommand(Status.APPLIED);
        expectedModel.updateFilteredPersonList(
                new CandidateMatchesFilterPredicate(List.of(Status.APPLIED), Set.<Role>of()));
        String expectedMessage = String.format(FilterCommand.MESSAGE_SUCCESS, 2, Status.APPLIED);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(APPLIED_ONE, APPLIED_TWO), model.getFilteredPersonList());
    }

    @Test
    public void execute_filterByStatusNoMatch_showsZeroCount() {
        Model model = pipelineModel();
        Model expectedModel = pipelineModel();
        FilterCommand command = new FilterCommand(Status.REJECTED);
        expectedModel.updateFilteredPersonList(
                new CandidateMatchesFilterPredicate(List.of(Status.REJECTED), Set.<Role>of()));
        String expectedMessage = String.format(FilterCommand.MESSAGE_SUCCESS, 0, Status.REJECTED);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_multipleStatuses_showsCandidatesWithAnyStatus() {
        Model model = pipelineModel();
        Model expectedModel = pipelineModel();
        FilterCommand command = new FilterCommand(List.of(Status.OFFERED, Status.INTERVIEWING));
        expectedModel.updateFilteredPersonList(new CandidateMatchesFilterPredicate(
                List.of(Status.INTERVIEWING, Status.OFFERED), Set.<Role>of()));
        String expectedMessage = String.format(FilterCommand.MESSAGE_SUCCESS, 2,
                Status.INTERVIEWING + " or " + Status.OFFERED);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(INTERVIEWING, OFFERED), model.getFilteredPersonList());
    }

    @Test
    public void execute_threeStatuses_joinsWithCommasAndFinalOr() {
        Model model = pipelineModel();
        Model expectedModel = pipelineModel();
        FilterCommand command = new FilterCommand(
                List.of(Status.OFFERED, Status.APPLIED, Status.INTERVIEWING));
        expectedModel.updateFilteredPersonList(new CandidateMatchesFilterPredicate(
                List.of(Status.APPLIED, Status.INTERVIEWING, Status.OFFERED), Set.<Role>of()));
        String expectedMessage = "4 candidate(s) filtered with status(es): Applied, Interviewing or Offered";

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(APPLIED_ONE, APPLIED_TWO, INTERVIEWING, OFFERED),
                model.getFilteredPersonList());
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
        String expected = FilterCommand.class.getCanonicalName()
                + "{statuses=[Applied], roleNames=[]}";
        assertEquals(expected, filterCommand.toString());
    }

    @Test
    public void constructor_emptyFilterCriteria_rejectsUnrestrictedFilter() {
        assertThrows(IllegalArgumentException.class, "At least one filter criterion is required.", () ->
                new FilterCommand(List.of(), Set.of()));
    }

    @Test
    public void equals_sameDetailsDifferentRoles_returnsFalse() {
        assertFalse(new FilterCommand(List.of(Status.APPLIED), Set.of("Developer"))
                .equals(new FilterCommand(List.of(Status.APPLIED), Set.of("Designer"))));
    }

    @Test
    public void execute_invalidRoleNameWithoutParser_reportsCommandErrorAndPreservesModel() {
        Person person = new PersonBuilder().build();
        Set<String> invalidNames = Set.of("Developer\tRole");
        ModelManager model = new ModelManager();
        model.addPerson(person);

        assertCommandFailure(new FilterCommand(List.of(), invalidNames), model, Role.MESSAGE_CONSTRAINTS);
    }
}
