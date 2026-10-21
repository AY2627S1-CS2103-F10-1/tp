package seedu.address.storage;

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

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.role.Role;
import seedu.address.model.role.RoleCatalogue;
import seedu.address.testutil.PersonBuilder;

class RoleDataStorageTest {
    @TempDir
    Path directory;

    @Test
    void save_reservedCandidatePath_rejectsBeforeCreatingFiles() throws Exception {
        for (String name : List.of("roles.json", "roles-transaction.json")) {
            RoleDataStorage storage = new RoleDataStorage(new JsonAddressBookStorage(directory.resolve(name)));

            assertThrows(IOException.class, () -> storage.save(assignedModel().getAddressBook()));

            assertFalse(Files.exists(directory.resolve("roles.json")));
            assertFalse(Files.exists(directory.resolve("roles-transaction.json")));
        }
    }

    @Test
    void initialize_reservedCandidatePath_preservesOriginalFiles() throws Exception {
        for (String name : List.of("roles.json", "roles-transaction.json")) {
            Path destination = directory.resolve(name);
            Files.writeString(destination, "original data");
            Storage storage = new StorageManager(new JsonAddressBookStorage(destination),
                    new JsonUserPrefsStorage(directory.resolve("preferences.json")));

            assertThrows(IOException.class, () -> new CandidateDataInitializer(storage).load());

            assertEquals("original data", Files.readString(destination));
            Files.delete(destination);
        }
    }

    @Test
    void save_symbolicLinkCollisions_preservesTargetsAndLinks() throws Exception {
        Path target = directory.resolve("shared.json");
        Files.writeString(target, "original data");
        List<Path> destinations = List.of(candidatePath(), directory.resolve("roles.json"),
                directory.resolve("roles-transaction.json"));
        for (int i = 0; i < destinations.size(); i++) {
            for (int j = i + 1; j < destinations.size(); j++) {
                Path first = Files.createSymbolicLink(destinations.get(i), target);
                Path second = Files.createSymbolicLink(destinations.get(j), target);
                RoleDataStorage storage = new RoleDataStorage(new JsonAddressBookStorage(candidatePath()));

                assertThrows(IOException.class, () -> storage.save(assignedModel().getAddressBook()));
                assertThrows(DataLoadingException.class, storage::read);
                assertThrows(IOException.class, storage::backup);

                assertEquals("original data", Files.readString(target));
                assertTrue(Files.isSymbolicLink(first));
                assertTrue(Files.isSymbolicLink(second));
                Files.delete(first);
                Files.delete(second);
                for (Path destination : destinations) {
                    assertFalse(Files.exists(destination));
                }
            }
        }
    }

    @Test
    void saveAndRead_unusedRolesAndMultipleAssignments_surviveRestart() throws Exception {
        ModelManager model = assignedModel();
        model.createRole("Unused role");
        model.replacePersonRoles(model.getAddressBook().getPersonList().getFirst(), List.of("Engineer", "No role"));

        storage().saveAddressBook(model.getAddressBook());

        assertEquals(model.getAddressBook(), storage().readAddressBook().orElseThrow());
        assertTrue(Files.readString(directory.resolve("roles.json")).contains("Unused role"));
        assertFalse(Files.exists(directory.resolve("roles-transaction.json")));
    }

    @Test
    void renameAndDelete_saveAndReload_preservesConsistentReferences() throws Exception {
        ModelManager model = assignedModel();
        String id = model.getRoleCatalogue().requireRole("Engineer").getId();
        model.renameRole("Engineer", "Senior Engineer");
        storage().saveAddressBook(model.getAddressBook());
        ModelManager reloaded = new ModelManager(storage().readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(id, reloaded.getRoleCatalogue().requireRole("Senior Engineer").getId());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());

        reloaded.deleteRole("Senior Engineer");
        storage().saveAddressBook(reloaded.getAddressBook());
        assertEquals(Set.of(Role.NO_ROLE), storage().readAddressBook().orElseThrow()
                .getPersonList().getFirst().getRoles());
    }

    @Test
    void read_legacyCandidateWithoutRoles_migratesToNoRole() throws Exception {
        AddressBook legacy = new AddressBook();
        legacy.addPerson(new PersonBuilder().build());
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(legacy))
                .replaceAll(",?\\s*\"roles\"\\s*:\\s*\\[\\s*\"no-role\"\\s*]", "");
        Files.writeString(candidatePath(), json);

        ReadOnlyAddressBook loaded = storage().readAddressBook().orElseThrow();

        assertEquals(Set.of(Role.NO_ROLE), loaded.getPersonList().getFirst().getRoles());
        storage().saveAddressBook(loaded);
        assertEquals(loaded, storage().readAddressBook().orElseThrow());
        assertTrue(Files.exists(directory.resolve("roles.json")));
    }

    @Test
    void read_missingCatalogueWithStoredRoleReferences_rejectsWithoutOverwriting() throws Exception {
        ModelManager model = assignedModel();
        new JsonAddressBookStorage(candidatePath()).saveAddressBook(model.getAddressBook());
        String before = Files.readString(candidatePath());

        assertThrows(DataLoadingException.class, () -> storage().readAddressBook());

        assertEquals(before, Files.readString(candidatePath()));
        assertFalse(Files.exists(directory.resolve("roles.json")));
    }

    @Test
    void read_invalidCatalogueAndReferences_rejectsWithoutDiscardingData() throws Exception {
        ModelManager model = assignedModel();
        storage().saveAddressBook(model.getAddressBook());
        Path roles = directory.resolve("roles.json");
        String validRoles = Files.readString(roles);
        for (String invalid : List.of("null", "{}", "{\"roles\":[null]}", "{\"roles\":[]}")) {
            Files.writeString(roles, invalid);
            assertThrows(DataLoadingException.class, () -> storage().readAddressBook());
            assertEquals(invalid, Files.readString(roles));
        }
        Files.writeString(roles, validRoles);
        String candidates = Files.readString(candidatePath());
        Files.writeString(candidatePath(), candidates.replace(model.getRoleCatalogue().requireRole("Engineer").getId(),
                "missing-role"));
        assertThrows(DataLoadingException.class, () -> storage().readAddressBook());
    }

    @Test
    void save_catalogueWriteFails_restoresBothOriginalFilesExactly() throws Exception {
        ModelManager model = assignedModel();
        storage().saveAddressBook(model.getAddressBook());
        String beforeCandidates = Files.readString(candidatePath());
        String beforeRoles = Files.readString(directory.resolve("roles.json"));
        model.deleteRole("Engineer");
        RoleDataStorage failing = new RoleDataStorage(new JsonAddressBookStorage(candidatePath())) {
            @Override
            void writeCatalogue(RoleCatalogue catalogue) throws IOException {
                Files.writeString(directory.resolve("roles.json"), "partial catalogue write");
                throw new IOException("Injected catalogue failure");
            }
        };

        assertThrows(IOException.class, () -> failing.save(model.getAddressBook()));

        assertEquals(beforeCandidates, Files.readString(candidatePath()));
        assertEquals(beforeRoles, Files.readString(directory.resolve("roles.json")));
        assertFalse(Files.exists(directory.resolve("roles-transaction.json")));
    }

    @Test
    void save_firstCatalogueWriteFails_restoresPreviouslyAbsentFiles() {
        RoleDataStorage failing = new RoleDataStorage(new JsonAddressBookStorage(candidatePath())) {
            @Override
            void writeCatalogue(RoleCatalogue catalogue) throws IOException {
                throw new IOException("Injected catalogue failure");
            }
        };

        assertThrows(IOException.class, () -> failing.save(assignedModel().getAddressBook()));

        assertFalse(Files.exists(candidatePath()));
        assertFalse(Files.exists(directory.resolve("roles.json")));
        assertFalse(Files.exists(directory.resolve("roles-transaction.json")));
    }

    @Test
    void read_interruptedSave_restoresJournalPairBeforeResolvingReferences() throws Exception {
        ModelManager model = assignedModel();
        storage().saveAddressBook(model.getAddressBook());
        ReadOnlyAddressBook before = new AddressBook(model.getAddressBook());
        RoleDataStorage.Journal journal = new RoleDataStorage.Journal(1,
                RoleDataStorage.Snapshot.capture(candidatePath()),
                RoleDataStorage.Snapshot.capture(directory.resolve("roles.json")));
        JsonUtil.saveJsonFile(journal, directory.resolve("roles-transaction.json"));
        model.deleteRole("Engineer");
        new JsonAddressBookStorage(candidatePath()).saveAddressBook(model.getAddressBook());

        assertEquals(before, storage().readAddressBook().orElseThrow());
        assertFalse(Files.exists(directory.resolve("roles-transaction.json")));
    }

    @Test
    void read_invalidJournal_blocksRecoveryAndRetainsOriginals() throws Exception {
        storage().saveAddressBook(assignedModel().getAddressBook());
        String before = Files.readString(candidatePath());
        Path journal = directory.resolve("roles-transaction.json");
        Files.writeString(journal, "{}");
        Storage storage = storage();

        assertThrows(IOException.class, () -> new CandidateDataInitializer(storage).load());
        assertEquals(before, Files.readString(candidatePath()));
        assertEquals("{}", Files.readString(journal));
        assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
    }

    @Test
    void initialize_invalidRoleData_preservesBothFilesBeforeConsistentRecovery() throws Exception {
        storage().saveAddressBook(assignedModel().getAddressBook());
        String before = Files.readString(candidatePath());
        Files.writeString(directory.resolve("roles.json"), "invalid catalogue");
        CandidateDataInitializer initializer = new CandidateDataInitializer(storage());

        ReadOnlyAddressBook recovered = initializer.load();

        Path backup = initializer.getRecoveryBackup().orElseThrow();
        assertEquals(before, Files.readString(backup));
        Path roleBackup = backup.resolveSibling(backup.getFileName() + ".roles.json");
        assertEquals("invalid catalogue", Files.readString(roleBackup));
        assertTrue(recovered.getPersonList().isEmpty());
        assertEquals(List.of(Role.NO_ROLE), recovered.getRoleCatalogue().getRoles());
        assertEquals(recovered, storage().readAddressBook().orElseThrow());
    }

    @Test
    void read_unusedCatalogueWithoutCandidateFile_preservesCatalogue() throws Exception {
        ModelManager model = new ModelManager();
        model.createRole("Unused");
        JsonUtil.saveJsonFile(new JsonRoleCatalogue(model.getRoleCatalogue()), directory.resolve("roles.json"));
        assertTrue(storage().readAddressBook().orElseThrow().getRoleCatalogue().find("Unused").isPresent());
    }

    @Test
    void initialize_catalogueCannotBeBackedUp_preservesCandidateOriginal() throws Exception {
        storage().saveAddressBook(assignedModel().getAddressBook());
        String before = Files.readString(candidatePath());
        Path roles = directory.resolve("roles.json");
        Files.delete(roles);
        Files.createDirectory(roles);
        assertThrows(IOException.class, () -> new CandidateDataInitializer(storage()).load());
        assertEquals(before, Files.readString(candidatePath()));
        assertTrue(Files.isDirectory(roles));
    }

    @Test
    void read_emptyDuplicateAndNullAssignments_rejectsExplicitInvalidRoles() throws Exception {
        storage().saveAddressBook(assignedModel().getAddressBook());
        String original = Files.readString(candidatePath());
        for (String assignments : List.of("[]", "[null]", "[\"no-role\",\"no-role\"]")) {
            Files.writeString(candidatePath(), original.replaceAll("\"roles\"\\s*:\\s*\\[[^]]*]",
                    "\"roles\": " + assignments));
            assertThrows(DataLoadingException.class, () -> storage().readAddressBook());
        }
    }

    @Test
    void read_duplicateCatalogueNamesAndProtectedRoleTampering_rejects() throws Exception {
        ModelManager model = assignedModel();
        model.createRole("Designer");
        storage().saveAddressBook(model.getAddressBook());
        Path roles = directory.resolve("roles.json");
        String original = Files.readString(roles);
        for (String invalid : List.of(original.replace("Designer", " engineer "),
                original.replace("No role", "Renamed built-in"),
                original.replace(model.getRoleCatalogue().requireRole("Designer").getId(),
                        model.getRoleCatalogue().requireRole("Engineer").getId()))) {
            Files.writeString(roles, invalid);
            assertThrows(DataLoadingException.class, () -> storage().readAddressBook());
        }
    }

    @Test
    void save_rollbackCannotFinish_blocksWritesUntilRestartRecovery() throws Exception {
        ModelManager model = assignedModel();
        storage().saveAddressBook(model.getAddressBook());
        ReadOnlyAddressBook before = new AddressBook(model.getAddressBook());
        RoleDataStorage failing = new RoleDataStorage(new JsonAddressBookStorage(candidatePath())) {
            @Override
            void writeCatalogue(RoleCatalogue catalogue) throws IOException {
                Files.move(candidatePath(), directory.resolve("interrupted-candidates.json"));
                Files.createDirectory(candidatePath());
                throw new IOException("Injected rollback obstruction");
            }
        };
        model.deleteRole("Engineer");

        assertThrows(IOException.class, () -> failing.save(model.getAddressBook()));
        assertTrue(Files.exists(directory.resolve("roles-transaction.json")));
        assertThrows(IOException.class, () -> failing.save(model.getAddressBook()));
        Files.delete(candidatePath());
        assertEquals(before, storage().readAddressBook().orElseThrow());
        assertFalse(Files.exists(directory.resolve("roles-transaction.json")));
    }

    @Test
    void initialize_catalogueOnlyInvalidData_preservesCatalogueBeforeRecovery() throws Exception {
        Path roles = directory.resolve("roles.json");
        Files.writeString(roles, "invalid catalogue");
        CandidateDataInitializer initializer = new CandidateDataInitializer(storage());
        assertTrue(initializer.load().getPersonList().isEmpty());
        Path backup = initializer.getRecoveryBackup().orElseThrow();
        assertTrue(backup.getFileName().toString().endsWith(".roles.json"));
        assertEquals("invalid catalogue", Files.readString(backup));
        assertEquals(List.of(Role.NO_ROLE), storage().readAddressBook().orElseThrow().getRoleCatalogue().getRoles());
    }

    @Test
    void read_malformedJournal_preservesJournalForManualRepair() throws Exception {
        Files.writeString(directory.resolve("roles-transaction.json"), "{");
        assertThrows(DataLoadingException.class, () -> storage().readAddressBook());
        assertThrows(IOException.class, () -> storage().backupAddressBook());
        assertEquals("{", Files.readString(directory.resolve("roles-transaction.json")));
    }

    private ModelManager assignedModel() {
        ModelManager model = new ModelManager();
        model.createRole("Engineer");
        Person person = new PersonBuilder().build();
        model.addPerson(person);
        model.replacePersonRoles(person, List.of("Engineer"));
        return model;
    }

    private StorageManager storage() {
        return new StorageManager(new JsonAddressBookStorage(candidatePath()),
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
    }

    private Path candidatePath() {
        return directory.resolve("HRvest.json");
    }
}
