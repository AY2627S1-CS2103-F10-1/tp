package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.testutil.LogicTestFixture;
import seedu.address.testutil.PersonBuilder;

public class CandidateFilterIntegrationTest {
    @TempDir
    Path temporaryFolder;

    private Model model;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        AddressBook candidates = new AddressBook();
        candidates.addPerson(ALICE);
        candidates.addPerson(BENSON);
        model = new ModelManager(candidates, new UserPrefs());
        logic = new LogicTestFixture(temporaryFolder, model).getLogic();
    }

    @Test
    public void execute_noteAfterFind_nextDeleteTargetsDisplayedCandidate() throws Exception {
        Person expectedCandidate = new PersonBuilder(BENSON).withNote("Follow up").build();
        assertFindUpdateKeepsSelection("note 1 no/Follow up", expectedCandidate);
    }

    @Test
    public void execute_statusAfterFind_nextDeleteTargetsDisplayedCandidate() throws Exception {
        Person expectedCandidate = new PersonBuilder(BENSON).withStatus(Status.OFFERED).build();
        assertFindUpdateKeepsSelection("status 1 s/Offered", expectedCandidate);
    }

    @Test
    public void execute_editAfterFind_nextDeleteTargetsDisplayedCandidate() throws Exception {
        Person expectedCandidate = new PersonBuilder(BENSON).withPhone("91234567").build();
        assertFindUpdateKeepsSelection("edit 1 p/91234567", expectedCandidate);
    }

    /**
     * Verifies that a later index command still targets the filtered candidate.
     */
    private void assertFindUpdateKeepsSelection(String command, Person expectedCandidate) throws Exception {
        logic.execute("find Benson");

        logic.execute(command);

        assertEquals(List.of(expectedCandidate), model.getFilteredPersonList());
        assertEquals(List.of(ALICE, expectedCandidate), model.getAddressBook().getPersonList());
        logic.execute("delete 1");
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_statusAfterFilter_removesCandidateThatNoLongerMatches() throws Exception {
        assertStatusUpdateKeepsFilter("status 1 s/Offered");
    }

    @Test
    public void execute_statusEditAfterFilter_removesCandidateThatNoLongerMatches() throws Exception {
        assertStatusUpdateKeepsFilter("edit 1 s/Offered");
    }

    /**
     * Verifies that changed statuses are reevaluated against the current filter.
     */
    private void assertStatusUpdateKeepsFilter(String command) throws Exception {
        logic.execute("status 2 s/Shortlisted");
        logic.execute("filter s/Shortlisted");

        logic.execute(command);

        assertTrue(model.getFilteredPersonList().isEmpty());
        Person expectedCandidate = new PersonBuilder(BENSON).withStatus(Status.OFFERED).build();
        assertEquals(List.of(ALICE, expectedCandidate), model.getAddressBook().getPersonList());
        logic.execute("list");
        assertEquals(List.of(ALICE, expectedCandidate), model.getFilteredPersonList());
        assertFalse(logic.isExpandedViewProperty().get());
    }

    @Test
    public void execute_noteAfterExpansion_keepsUpdatedCandidateExpanded() throws Exception {
        Person expectedCandidate = new PersonBuilder(BENSON).withNote("Follow up").build();
        assertExpandedUpdateKeepsSelection("note 1 no/Follow up", expectedCandidate);
    }

    @Test
    public void execute_statusAfterExpansion_keepsUpdatedCandidateExpanded() throws Exception {
        Person expectedCandidate = new PersonBuilder(BENSON).withStatus(Status.OFFERED).build();
        assertExpandedUpdateKeepsSelection("status 1 s/Offered", expectedCandidate);
    }

    @Test
    public void execute_editAfterExpansion_keepsUpdatedCandidateExpanded() throws Exception {
        Person expectedCandidate = new PersonBuilder(BENSON).withName("New Name").build();
        assertExpandedUpdateKeepsSelection("edit 1 n/New Name", expectedCandidate);
    }

    /**
     * Verifies that replacing a candidate record does not lose the expanded selection.
     */
    private void assertExpandedUpdateKeepsSelection(String command, Person expectedCandidate) throws Exception {
        logic.execute("filter s/Applied");
        logic.execute("expand 2");

        logic.execute(command);

        assertTrue(logic.isExpandedViewProperty().get());
        assertEquals(List.of(expectedCandidate), model.getFilteredPersonList());
        assertEquals(List.of(ALICE, expectedCandidate), model.getAddressBook().getPersonList());
    }
}
