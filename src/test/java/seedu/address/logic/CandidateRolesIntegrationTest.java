package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.role.Role;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Exercises role assignment commands, active filters, and coordinated persistence together. */
class CandidateRolesIntegrationTest {
    private static final String ALICE = "add n/Alice p/123 e/alice@example.com a/Street";
    private static final String BOB = "add n/Bob p/456 e/bob@example.com a/Other Street";

    @TempDir
    Path directory;

    private ModelManager model;
    private LogicManager logic;
    private StorageManager storage;

    @BeforeEach
    void setUp() throws Exception {
        storage = new StorageManager(new JsonAddressBookStorage(directory.resolve("HRvest.json")),
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        model = new ModelManager();
        logic = new LogicManager(model, storage);
        logic.execute("add_role n/Developer");
        logic.execute("add_role n/Designer");
        logic.execute("add_role n/Unused");
    }

    @Test
    void execute_addRepeatedRoles_resolvesCanonicalEntriesAndRemovesNoRole() throws Exception {
        logic.execute(ALICE + " R/  developer  r/Developer r/No role r/Designer");

        assertEquals(Set.of(getRole("Developer"), getRole("Designer")), getFirstPerson().getRoles());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void execute_addMissingRole_reportsExplicitRoleRequirement() {
        assertThrows(ParseException.class, AddCommand.MESSAGE_MISSING_ROLE, () -> logic.execute(ALICE));
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    void execute_addUnknownRole_preservesBothFiles() throws Exception {
        assertRejectedWithoutChanges(ALICE + " r/Developer r/Missing",
                "Role does not exist: Missing. Use add_role n/NAME.");
    }

    @Test
    void execute_addUnicodeAndMultiwordRoles_preservesNames() throws Exception {
        logic.execute("add_role n/工程师");
        logic.execute("add_role n/Senior Developer");
        logic.execute(ALICE + " r/工程师 r/ senior developer ");

        assertEquals(Set.of(getRole("工程师"), getRole("Senior Developer")), getFirstPerson().getRoles());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void execute_editRoles_replacesAllAssignmentsAndPreservesOtherDetails() throws Exception {
        logic.execute(ALICE + " r/Developer r/Designer t/backend s/Interviewing");
        logic.execute("note 1 no/Follow up");
        Person before = getFirstPerson();

        logic.execute("edit 1 r/No role");
        assertEquals(before.withRoles(Set.of(Role.NO_ROLE)), getFirstPerson());
        logic.execute("edit 1 r/Developer");
        assertEquals(before.withRoles(Set.of(getRole("Developer"))), getFirstPerson());
        logic.execute("edit 1 p/789");
        assertEquals(Set.of(getRole("Developer")), getFirstPerson().getRoles());
        assertEquals("789", getFirstPerson().getPhone().value);
        assertEquals(before.getNote(), getFirstPerson().getNote());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void execute_editUnknownRole_rejectsOtherFieldChangesToo() throws Exception {
        logic.execute(ALICE + " r/Developer");
        assertRejectedWithoutChanges("edit 1 p/789 r/Designer r/Missing",
                "Role does not exist: Missing. Use add_role n/NAME.");
    }

    @Test
    void execute_filterRoles_matchesAnyRoleAndReplacesPreviousFilter() throws Exception {
        logic.execute(ALICE + " r/Developer");
        logic.execute(BOB + " r/Designer");
        logic.execute("find Nobody");

        assertEquals("2 candidate(s) filtered with role(s): Designer or Developer",
                logic.execute("filter r/developer r/Developer r/Designer").getFeedbackToUser());
        assertEquals(2, model.getFilteredPersonList().size());
        assertEquals("0 candidate(s) filtered with role(s): Unused",
                logic.execute("filter r/Unused").getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    void execute_filterCombined_matchesAnyValueWithinEachFieldAndBothFields() throws Exception {
        logic.execute(ALICE + " r/Developer s/Interviewing");
        logic.execute(BOB + " r/Designer s/Applied");

        assertEquals("1 candidate(s) filtered with status(es): Interviewing or Offered"
                + " and role(s): Designer or Developer",
                logic.execute("filter r/Developer r/Designer s/Interviewing s/Offered").getFeedbackToUser());
        assertEquals(List.of(getFirstPerson()), model.getFilteredPersonList());
    }

    @Test
    void execute_filterNoRoleWithNamedRole_matchesBothGroupsWithoutChangingAssignments() throws Exception {
        logic.execute(ALICE + " r/No role");
        logic.execute(BOB + " r/Developer");
        AddressBook before = new AddressBook(model.getAddressBook());

        logic.execute("filter r/No role r/Developer");

        assertEquals(2, model.getFilteredPersonList().size());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    void execute_filterUnknownRole_preservesFilterAndExpandedView() throws Exception {
        logic.execute(ALICE + " r/Developer");
        logic.execute("expand 1");
        assertRejectedWithoutChanges("filter r/Developer r/Missing",
                "Role does not exist: Missing. Use list_role.");
        assertTrue(model.isExpandedViewProperty().get());
        assertEquals(List.of(getFirstPerson()), model.getFilteredPersonList());
    }

    @Test
    void execute_editRoleAssignments_preservesFilterAndExpandedSelection() throws Exception {
        logic.execute(ALICE + " r/Developer");
        logic.execute("filter r/Developer");
        logic.execute("edit 1 r/Designer");
        assertTrue(model.getFilteredPersonList().isEmpty());

        logic.execute("list");
        logic.execute("expand 1");
        logic.execute("edit 1 r/Developer");
        assertTrue(model.isExpandedViewProperty().get());
        assertEquals(List.of(getFirstPerson()), model.getFilteredPersonList());
    }

    @Test
    void execute_renameAndDeleteRole_keepsFilterBoundToStableIdentity() throws Exception {
        logic.execute(ALICE + " r/Developer");
        logic.execute("filter r/Developer");
        String identity = getRole("Developer").getId();

        logic.execute("edit_role r/Developer n/Senior Developer");
        assertEquals(List.of(getFirstPerson()), model.getFilteredPersonList());
        assertEquals(identity, getRole("Senior Developer").getId());
        logic.execute("delete_role r/Senior Developer");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(Set.of(Role.NO_ROLE), getFirstPerson().getRoles());
        logic.execute("add_role n/Developer");
        logic.execute("list");
        logic.execute("edit 1 r/Developer");
        assertFalse(identity.equals(getRole("Developer").getId()));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void execute_globalRoleUpdates_preservesExpandedCandidateAndIndex() throws Exception {
        logic.execute(ALICE + " r/Developer");
        logic.execute(BOB + " r/Designer");
        logic.execute("expand 1");

        logic.execute("edit_role r/Developer n/Senior Developer");

        assertTrue(model.isExpandedViewProperty().get());
        assertEquals(List.of(getFirstPerson()), model.getFilteredPersonList());
        assertEquals(Set.of(getRole("Senior Developer")), getFirstPerson().getRoles());

        logic.execute("delete_role r/Senior Developer");
        logic.execute("edit 1 p/789");

        assertTrue(model.isExpandedViewProperty().get());
        assertEquals(List.of(getFirstPerson()), model.getFilteredPersonList());
        assertEquals(Set.of(Role.NO_ROLE), getFirstPerson().getRoles());
        assertEquals("789", getFirstPerson().getPhone().value);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void execute_saveFailure_restoresAssignmentFilterAndExpandedView() throws Exception {
        logic.execute(ALICE + " r/Developer");
        logic.execute("expand 1");
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(directory.resolve("HRvest.json"));
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(directory.resolve("HRvest.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                throw new IOException("Test save failure");
            }
        };
        LogicManager failingLogic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));

        assertThrows(CommandException.class, () -> failingLogic.execute("edit 1 r/Designer"));

        assertEquals(before, model.getAddressBook());
        assertTrue(model.isExpandedViewProperty().get());
        assertEquals(List.of(getFirstPerson()), model.getFilteredPersonList());
        assertEquals(saved, Files.readString(directory.resolve("HRvest.json")));
    }

    @Test
    void execute_restart_preservesAssignmentsAndAllowsFurtherEdits() throws Exception {
        logic.execute(ALICE + " r/Developer r/Designer");
        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager restartedLogic = new LogicManager(restarted, storage);

        restartedLogic.execute("filter r/developer");
        assertEquals(List.of(getFirstPerson()), restarted.getFilteredPersonList());
        restartedLogic.execute("edit 1 r/No role");
        assertEquals(Set.of(Role.NO_ROLE), storage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getRoles());
    }

    private Role getRole(String name) {
        return model.getRoleCatalog().requireRole(name);
    }

    private Person getFirstPerson() {
        return model.getAddressBook().getPersonList().getFirst();
    }

    /** Verifies a rejected command preserves model data, the displayed list, and both saved files. */
    private void assertRejectedWithoutChanges(String input, String message) throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        String candidates = Files.readString(directory.resolve("HRvest.json"));
        String roles = Files.readString(directory.resolve("roles.json"));

        assertThrows(CommandException.class, message, () -> logic.execute(input));

        assertEquals(before, model.getAddressBook());
        assertEquals(displayed, model.getFilteredPersonList());
        assertEquals(candidates, Files.readString(directory.resolve("HRvest.json")));
        assertEquals(roles, Files.readString(directory.resolve("roles.json")));
    }
}
