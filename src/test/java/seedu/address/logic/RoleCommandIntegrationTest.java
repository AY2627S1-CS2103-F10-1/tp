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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
import seedu.address.testutil.PersonBuilder;

class RoleCommandIntegrationTest {
    @TempDir
    Path directory;

    @Test
    void execute_manageRolesAcrossRestarts_preservesCatalogAndAssignments() throws Exception {
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, storage());
        logic.execute("create_role n/Engineer");
        logic.execute("create_role n/Unused");
        assertTrue(storage().readAddressBook().orElseThrow().getRoleCatalog().find("Unused").isPresent());
        Person person = new PersonBuilder().build();
        model.addPerson(person);
        model.replacePersonRoles(person, List.of("Engineer", "No role"));
        String id = model.getRoleCatalog().requireRole("Engineer").getId();
        logic.execute("edit_role r/engineer n/Senior Engineer");

        ModelManager restarted = new ModelManager(storage().readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(id, restarted.getRoleCatalog().requireRole("Senior Engineer").getId());
        assertEquals(Set.of(restarted.getRoleCatalog().requireRole("Senior Engineer")),
                restarted.getAddressBook().getPersonList().getFirst().getRoles());
        LogicManager restartedLogic = new LogicManager(restarted, storage());
        assertEquals("Roles:\nNo role\nSenior Engineer\nUnused",
                restartedLogic.execute("list_role").getFeedbackToUser());
        restartedLogic.execute("delete_role r/Senior Engineer");
        assertEquals(Set.of(Role.NO_ROLE), storage().readAddressBook().orElseThrow()
                .getPersonList().getFirst().getRoles());
        assertFalse(storage().readAddressBook().orElseThrow().getRoleCatalog().find("Senior Engineer").isPresent());
    }

    @Test
    void execute_invalidOperations_leavesModelAndBothFilesUnchanged() throws Exception {
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, storage());
        logic.execute("create_role n/Engineer");
        String candidates = Files.readString(directory.resolve("HRvest.json"));
        String catalog = Files.readString(directory.resolve("roles.json"));
        AddressBook before = new AddressBook(model.getAddressBook());

        assertThrows(CommandException.class, () -> logic.execute("create_role n/ ENGINEER "));
        assertThrows(CommandException.class, () -> logic.execute("edit_role r/Engineer n/No role"));
        assertThrows(CommandException.class, () -> logic.execute("delete_role r/No role"));
        assertThrows(ParseException.class, () -> logic.execute("edit_role r/Engineer n/New n/Other"));

        assertEquals(before, model.getAddressBook());
        assertEquals(candidates, Files.readString(directory.resolve("HRvest.json")));
        assertEquals(catalog, Files.readString(directory.resolve("roles.json")));
    }

    @Test
    void execute_rolesCreatedThroughSharedApi_canBeRenamedAndDeletedAfterRestart() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        for (String name : List.of("n/Engineer", "C/C++ Engineer", "Engineer x/Tools", "Software  Engineer")) {
            model.createRole(name);
            model.replacePersonRoles(model.getAddressBook().getPersonList().getFirst(), List.of(name));
            storage().saveAddressBook(model.getAddressBook());
            model = new ModelManager(storage().readAddressBook().orElseThrow(), new UserPrefs());
            LogicManager logic = new LogicManager(model, storage());

            logic.execute("edit_role r/" + name + " n/Renamed role");
            logic.execute("delete_role r/Renamed role");

            assertEquals(Set.of(Role.NO_ROLE), model.getAddressBook().getPersonList().getFirst().getRoles());
            assertEquals(model.getAddressBook(), storage().readAddressBook().orElseThrow());
        }
    }

    @Test
    void execute_createRoleSaveFails_restoresModelAndPreviouslySavedCatalog() throws Exception {
        ModelManager model = new ModelManager();
        storage().saveAddressBook(model.getAddressBook());
        String catalog = Files.readString(directory.resolve("roles.json"));
        JsonAddressBookStorage failingCandidates = new JsonAddressBookStorage(directory.resolve("HRvest.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                throw new IOException("Injected role save failure");
            }
        };
        StorageManager failing = new StorageManager(failingCandidates,
                new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        LogicManager logic = new LogicManager(model, failing);

        assertThrows(CommandException.class, () -> logic.execute("create_role n/Engineer"));

        assertEquals(List.of(Role.NO_ROLE), model.getRoleCatalog().getRoles());
        assertEquals(catalog, Files.readString(directory.resolve("roles.json")));
        assertEquals(model.getAddressBook(), storage().readAddressBook().orElseThrow());
    }

    @Test
    void execute_clearCandidates_preservesRolesAcrossRestart() throws Exception {
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, storage());
        logic.execute("create_role n/Engineer");
        logic.execute("create_role n/Unused");
        Person person = new PersonBuilder().build();
        model.addPerson(person);
        model.replacePersonRoles(person, List.of("Engineer"));
        storage().saveAddressBook(model.getAddressBook());
        AddressBook before = new AddressBook(model.getAddressBook());

        logic.execute("clear");

        assertEquals(before, model.getAddressBook());
        assertEquals(before, storage().readAddressBook().orElseThrow());

        logic.execute("clear confirm");

        ReadOnlyAddressBook restarted = storage().readAddressBook().orElseThrow();
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(restarted.getPersonList().isEmpty());
        assertEquals(before.getRoleCatalog(), model.getRoleCatalog());
        assertEquals(before.getRoleCatalog(), restarted.getRoleCatalog());

        logic.execute("clear confirm");

        assertEquals(before.getRoleCatalog(), storage().readAddressBook().orElseThrow().getRoleCatalog());
    }

    private StorageManager storage() {
        return new StorageManager(new JsonAddressBookStorage(directory.resolve("HRvest.json")),
                new JsonUserPrefsStorage(directory.resolve("prefs.json")));
    }
}
