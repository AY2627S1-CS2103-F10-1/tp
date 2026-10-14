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
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

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
        logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("candidates.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_noteAfterFind_nextDeleteTargetsDisplayedCandidate() throws Exception {
        assertFindUpdateKeepsSelection("note 1 no/Follow up");
    }

    @Test
    public void execute_statusAfterFind_nextDeleteTargetsDisplayedCandidate() throws Exception {
        assertFindUpdateKeepsSelection("status 1 s/Offered");
    }

    @Test
    public void execute_editAfterFind_nextDeleteTargetsDisplayedCandidate() throws Exception {
        assertFindUpdateKeepsSelection("edit 1 p/91234567");
    }

    /**
     * Verifies that a later index command still targets the filtered candidate.
     */
    private void assertFindUpdateKeepsSelection(String command) throws Exception {
        logic.execute("find Benson");

        logic.execute(command);

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(BENSON.getEmail(), model.getFilteredPersonList().getFirst().getEmail());
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
        assertTrue(model.hasPerson(ALICE));
        assertEquals(2, model.getAddressBook().getPersonList().size());
        logic.execute("list");
        assertEquals(2, model.getFilteredPersonList().size());
        assertFalse(logic.isExpandedViewProperty().get());
    }

    @Test
    public void execute_noteAfterExpansion_keepsUpdatedCandidateExpanded() throws Exception {
        assertExpandedUpdateKeepsSelection("note 1 no/Follow up");
    }

    @Test
    public void execute_statusAfterExpansion_keepsUpdatedCandidateExpanded() throws Exception {
        assertExpandedUpdateKeepsSelection("status 1 s/Offered");
    }

    @Test
    public void execute_editAfterExpansion_keepsUpdatedCandidateExpanded() throws Exception {
        assertExpandedUpdateKeepsSelection("edit 1 n/New Name");
    }

    /**
     * Verifies that replacing a candidate record does not lose the expanded selection.
     */
    private void assertExpandedUpdateKeepsSelection(String command) throws Exception {
        logic.execute("filter s/Applied");
        logic.execute("expand 2");

        logic.execute(command);

        assertTrue(logic.isExpandedViewProperty().get());
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(BENSON.getEmail(), model.getFilteredPersonList().getFirst().getEmail());
        assertEquals(model.getAddressBook().getPersonList().get(1), model.getFilteredPersonList().getFirst());
    }
}
