package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalRoleAssignments.getAssignedModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.role.Role;
import seedu.address.model.role.RoleCatalog;
import seedu.address.testutil.PersonBuilder;

class RoleDataStorageTest {
    @TempDir
    Path directory;

    @Test
    void save_reservedCandidatePath_rejectsBeforeCreatingFiles() throws Exception {
        for (String name : List.of("roles.json", "roles-transaction.json")) {
            RoleDataStorage storage = new RoleDataStorage(new JsonAddressBookStorage(directory.resolve(name)));

            assertThrows(IOException.class, () -> storage.save(getAssignedModel("Engineer").getAddressBook()));

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

                assertThrows(IOException.class, () -> storage.save(getAssignedModel("Engineer").getAddressBook()));
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
        ModelManager model = getAssignedModel("Engineer");
        model.createRole("Unused role");
        model.replacePersonRoles(model.getAddressBook().getPersonList().getFirst(), List.of("Engineer", "No role"));

        storage().saveAddressBook(model.getAddressBook());

        assertEquals(model.getAddressBook(), storage().readAddressBook().orElseThrow());
        assertTrue(Files.readString(directory.resolve("roles.json")).contains("Unused role"));
        assertFalse(Files.exists(directory.resolve("roles-transaction.json")));
    }

    @Test
    void renameAndDelete_saveAndReload_preservesConsistentReferences() throws Exception {
        ModelManager model = getAssignedModel("Engineer");
        String id = model.getRoleCatalog().requireRole("Engineer").getId();
        model.renameRole("Engineer", "Senior Engineer");
        storage().saveAddressBook(model.getAddressBook());
        ModelManager reloaded = new ModelManager(storage().readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(id, reloaded.getRoleCatalog().requireRole("Senior Engineer").getId());
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
    void read_missingCatalogWithStoredRoleReferences_rejectsWithoutOverwriting() throws Exception {
        ModelManager model = getAssignedModel("Engineer");
        new JsonAddressBookStorage(candidatePath()).saveAddressBook(model.getAddressBook());
        String before = Files.readString(candidatePath());

        assertThrows(DataLoadingException.class, () -> storage().readAddressBook());

        assertEquals(before, Files.readString(candidatePath()));
        assertFalse(Files.exists(directory.resolve("roles.json")));
    }

    @Test
    void read_invalidCatalogAndReferences_rejectsWithoutDiscardingData() throws Exception {
        ModelManager model = getAssignedModel("Engineer");
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
        Files.writeString(candidatePath(), candidates.replace(model.getRoleCatalog().requireRole("Engineer").getId(),
                "missing-role"));
        assertThrows(DataLoadingException.class, () -> storage().readAddressBook());
    }

    @Test
    void save_catalogWriteFails_restoresBothOriginalFilesExactly() throws Exception {
        ModelManager model = getAssignedModel("Engineer");
        storage().saveAddressBook(model.getAddressBook());
        String beforeCandidates = Files.readString(candidatePath());
        String beforeRoles = Files.readString(directory.resolve("roles.json"));
        model.deleteRole("Engineer");
        RoleDataStorage failing = new RoleDataStorage(new JsonAddressBookStorage(candidatePath())) {
            @Override
            void writeCatalog(RoleCatalog catalog) throws IOException {
                Files.writeString(directory.resolve("roles.json"), "partial catalog write");
                throw new IOException("Injected catalog failure");
            }
        };

        assertThrows(IOException.class, () -> failing.save(model.getAddressBook()));

        assertEquals(beforeCandidates, Files.readString(candidatePath()));
        assertEquals(beforeRoles, Files.readString(directory.resolve("roles.json")));
        assertFalse(Files.exists(directory.resolve("roles-transaction.json")));
    }

    @Test
    void save_firstCatalogWriteFails_restoresPreviouslyAbsentFiles() {
        RoleDataStorage failing = new RoleDataStorage(new JsonAddressBookStorage(candidatePath())) {
            @Override
            void writeCatalog(RoleCatalog catalog) throws IOException {
                throw new IOException("Injected catalog failure");
            }
        };

        assertThrows(IOException.class, () -> failing.save(getAssignedModel("Engineer").getAddressBook()));

        assertFalse(Files.exists(candidatePath()));
        assertFalse(Files.exists(directory.resolve("roles.json")));
        assertFalse(Files.exists(directory.resolve("roles-transaction.json")));
    }

    @Test
    void execute_roleChangeCatalogWriteFails_restoresFilesAssignmentsAndExpandedView() throws Exception {
        ModelManager model = getAssignedModel("Engineer");
        model.addPerson(new PersonBuilder().withEmail("other@example.com").build());
        model.expandPerson(model.getFilteredPersonList().getFirst());
        storage().saveAddressBook(model.getAddressBook());
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        byte[] candidatesBefore = Files.readAllBytes(candidatePath());
        byte[] rolesBefore = Files.readAllBytes(directory.resolve("roles.json"));
        RoleDataStorage failing = new RoleDataStorage(new JsonAddressBookStorage(candidatePath())) {
            @Override
            void writeCatalog(RoleCatalog catalog) throws IOException {
                Files.writeString(directory.resolve("roles.json"), "partial catalog write");
                throw new IOException("Injected catalog failure");
            }
        };
        StorageManager failingStorage = new StorageManager(new JsonAddressBookStorage(candidatePath()),
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                failing.save(data);
            }
        };
        LogicManager logic = new LogicManager(model, failingStorage);
        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "Injected catalog failure");

        for (String command : List.of("edit_role r/Engineer n/Senior Engineer", "delete_role r/Engineer")) {
            assertThrows(CommandException.class, expectedMessage, () -> logic.execute(command));

            assertEquals(before, model.getAddressBook());
            assertEquals(displayedBefore, model.getFilteredPersonList());
            assertTrue(model.isExpandedViewProperty().get());
            assertArrayEquals(candidatesBefore, Files.readAllBytes(candidatePath()));
            assertArrayEquals(rolesBefore, Files.readAllBytes(directory.resolve("roles.json")));
            assertFalse(Files.exists(directory.resolve("roles-transaction.json")));
            assertEquals(before, storage().readAddressBook().orElseThrow());
        }
    }

    @Test
    void read_interruptedSave_restoresJournalPairBeforeResolvingReferences() throws Exception {
        ModelManager model = getAssignedModel("Engineer");
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
    void read_journalForDifferentCandidatePath_preservesBothFilesAndJournal() throws Exception {
        ModelManager model = getAssignedModel("Engineer");
        storage().saveAddressBook(model.getAddressBook());
        Path roles = directory.resolve("roles.json");
        RoleDataStorage.Journal journal = new RoleDataStorage.Journal(1,
                RoleDataStorage.Snapshot.capture(candidatePath()), RoleDataStorage.Snapshot.capture(roles));
        Path journalPath = directory.resolve("roles-transaction.json");
        JsonUtil.saveJsonFile(journal, journalPath);
        Path otherPath = directory.resolve("other.json");
        AddressBook other = new AddressBook();
        other.addPerson(new PersonBuilder().withName("Other Candidate").withEmail("other@example.com").build());
        new JsonAddressBookStorage(otherPath).saveAddressBook(other);
        String originalCandidates = Files.readString(candidatePath());
        String originalOther = Files.readString(otherPath);
        String originalRoles = Files.readString(roles);
        String originalJournal = Files.readString(journalPath);
        Storage otherStorage = new StorageManager(new JsonAddressBookStorage(otherPath),
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));

        assertThrows(IOException.class, () -> new CandidateDataInitializer(otherStorage).load());

        assertEquals(originalCandidates, Files.readString(candidatePath()));
        assertEquals(originalOther, Files.readString(otherPath));
        assertEquals(originalRoles, Files.readString(roles));
        assertEquals(originalJournal, Files.readString(journalPath));
        assertThrows(IOException.class, () -> otherStorage.saveAddressBook(other));
        assertEquals(model.getAddressBook(), storage().readAddressBook().orElseThrow());
        assertFalse(Files.exists(journalPath));
    }

    @Test
    void read_reservedSyntaxInCatalog_preservesOriginalsForRecovery() throws Exception {
        storage().saveAddressBook(getAssignedModel("Engineer").getAddressBook());
        Path roles = directory.resolve("roles.json");
        String invalidRoles = Files.readString(roles).replace("Engineer", "Senior n/Engineer");
        Files.writeString(roles, invalidRoles);
        String candidates = Files.readString(candidatePath());

        assertThrows(DataLoadingException.class, () -> storage().readAddressBook());

        assertEquals(invalidRoles, Files.readString(roles));
        assertEquals(candidates, Files.readString(candidatePath()));
    }

    @Test
    void read_journalForPreviouslyAbsentCandidate_preservesOtherCandidateFile() throws Exception {
        storage().saveAddressBook(getAssignedModel("Engineer").getAddressBook());
        RoleDataStorage.Journal journal = new RoleDataStorage.Journal(1,
                RoleDataStorage.Snapshot.capture(directory.resolve("absent.json")),
                RoleDataStorage.Snapshot.capture(directory.resolve("roles.json")));
        Path journalPath = directory.resolve("roles-transaction.json");
        JsonUtil.saveJsonFile(journal, journalPath);
        String candidates = Files.readString(candidatePath());
        String roles = Files.readString(directory.resolve("roles.json"));
        String pending = Files.readString(journalPath);

        assertThrows(DataLoadingException.class, () -> storage().readAddressBook());

        assertEquals(candidates, Files.readString(candidatePath()));
        assertEquals(roles, Files.readString(directory.resolve("roles.json")));
        assertEquals(pending, Files.readString(journalPath));
        assertFalse(Files.exists(directory.resolve("absent.json")));
    }

    @Test
    void read_retargetedCatalogLink_preservesCandidatesAndBothTargets() throws Exception {
        ModelManager model = getAssignedModel("Engineer");
        storage().saveAddressBook(model.getAddressBook());
        Path roles = directory.resolve("roles.json");
        String originalRoles = Files.readString(roles);
        Path originalTarget = Files.move(roles, directory.resolve("original-roles.json"));
        Files.createSymbolicLink(roles, originalTarget);
        RoleDataStorage.Journal journal = new RoleDataStorage.Journal(1,
                RoleDataStorage.Snapshot.capture(candidatePath()), RoleDataStorage.Snapshot.capture(roles));
        Path journalPath = directory.resolve("roles-transaction.json");
        JsonUtil.saveJsonFile(journal, journalPath);
        model.deleteRole("Engineer");
        new JsonAddressBookStorage(candidatePath()).saveAddressBook(model.getAddressBook());
        String candidates = Files.readString(candidatePath());
        Path otherTarget = directory.resolve("other-roles.json");
        Files.writeString(otherTarget, "other catalog bytes");
        Files.delete(roles);
        Files.createSymbolicLink(roles, otherTarget);

        assertThrows(DataLoadingException.class, () -> storage().readAddressBook());

        assertEquals(candidates, Files.readString(candidatePath()));
        assertEquals(originalRoles, Files.readString(originalTarget));
        assertEquals("other catalog bytes", Files.readString(otherTarget));
        assertTrue(Files.isSymbolicLink(roles));
        assertTrue(Files.exists(journalPath));
    }

    @Test
    void read_candidateLinkToOriginalDestination_recoversAndPreservesLink() throws Exception {
        ModelManager model = getAssignedModel("Engineer");
        storage().saveAddressBook(model.getAddressBook());
        RoleDataStorage.Journal journal = new RoleDataStorage.Journal(1,
                RoleDataStorage.Snapshot.capture(candidatePath()),
                RoleDataStorage.Snapshot.capture(directory.resolve("roles.json")));
        Path journalPath = directory.resolve("roles-transaction.json");
        JsonUtil.saveJsonFile(journal, journalPath);
        Path link = Files.createSymbolicLink(directory.resolve("candidate-link.json"), candidatePath());
        RoleDataStorage storage = new RoleDataStorage(new JsonAddressBookStorage(link));

        assertEquals(model.getAddressBook(), storage.read().orElseThrow());

        assertTrue(Files.isSymbolicLink(link));
        assertFalse(Files.exists(journalPath));
    }

    @Test
    void read_journalWithoutDestinations_blocksRecoveryAndPreservesOriginals() throws Exception {
        storage().saveAddressBook(getAssignedModel("Engineer").getAddressBook());
        String candidates = Files.readString(candidatePath());
        String roles = Files.readString(directory.resolve("roles.json"));
        String invalid = "{\"version\":1,\"candidates\":{\"present\":true,\"bytes\":\"\"},"
                + "\"roles\":{\"present\":false,\"bytes\":null}}";
        Path journalPath = directory.resolve("roles-transaction.json");
        Files.writeString(journalPath, invalid);
        Storage storage = storage();

        assertThrows(IOException.class, () -> new CandidateDataInitializer(storage).load());

        assertEquals(candidates, Files.readString(candidatePath()));
        assertEquals(roles, Files.readString(directory.resolve("roles.json")));
        assertEquals(invalid, Files.readString(journalPath));
        assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
    }

    @Test
    void read_invalidJournalDestination_reportsLoadFailureWithoutOverwriting() throws Exception {
        storage().saveAddressBook(getAssignedModel("Engineer").getAddressBook());
        String candidates = Files.readString(candidatePath());
        Path roles = directory.resolve("roles.json");
        String catalog = Files.readString(roles);
        RoleDataStorage.Journal journal = new RoleDataStorage.Journal(1,
                new RoleDataStorage.Snapshot("\u0000", true, new byte[0]), RoleDataStorage.Snapshot.capture(roles));
        Path journalPath = directory.resolve("roles-transaction.json");
        JsonUtil.saveJsonFile(journal, journalPath);

        assertThrows(DataLoadingException.class, () -> storage().readAddressBook());

        assertEquals(candidates, Files.readString(candidatePath()));
        assertEquals(catalog, Files.readString(roles));
        assertTrue(Files.exists(journalPath));
    }

    @Test
    void read_invalidJournal_blocksRecoveryAndRetainsOriginals() throws Exception {
        storage().saveAddressBook(getAssignedModel("Engineer").getAddressBook());
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
        storage().saveAddressBook(getAssignedModel("Engineer").getAddressBook());
        String before = Files.readString(candidatePath());
        Files.writeString(directory.resolve("roles.json"), "invalid catalog");
        CandidateDataInitializer initializer = new CandidateDataInitializer(storage());

        ReadOnlyAddressBook recovered = initializer.load();

        Path backup = initializer.getRecoveryBackup().orElseThrow();
        assertEquals(before, Files.readString(backup));
        Path roleBackup = backup.resolveSibling(backup.getFileName() + ".roles.json");
        assertEquals("invalid catalog", Files.readString(roleBackup));
        assertTrue(recovered.getPersonList().isEmpty());
        assertEquals(List.of(Role.NO_ROLE), recovered.getRoleCatalog().getRoles());
        assertEquals(recovered, storage().readAddressBook().orElseThrow());
    }

    @Test
    void read_unusedCatalogWithoutCandidateFile_preservesCatalog() throws Exception {
        ModelManager model = new ModelManager();
        model.createRole("Unused");
        JsonUtil.saveJsonFile(new JsonRoleCatalog(model.getRoleCatalog()), directory.resolve("roles.json"));
        assertTrue(storage().readAddressBook().orElseThrow().getRoleCatalog().find("Unused").isPresent());
    }

    @Test
    void initialize_catalogCannotBeBackedUp_preservesCandidateOriginal() throws Exception {
        storage().saveAddressBook(getAssignedModel("Engineer").getAddressBook());
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
        storage().saveAddressBook(getAssignedModel("Engineer").getAddressBook());
        String original = Files.readString(candidatePath());
        for (String assignments : List.of("[]", "[null]", "[\"no-role\",\"no-role\"]")) {
            Files.writeString(candidatePath(), original.replaceAll("\"roles\"\\s*:\\s*\\[[^]]*]",
                    "\"roles\": " + assignments));
            assertThrows(DataLoadingException.class, () -> storage().readAddressBook());
        }
    }

    @Test
    void read_duplicateCatalogNamesAndProtectedRoleTampering_rejects() throws Exception {
        ModelManager model = getAssignedModel("Engineer");
        model.createRole("Designer");
        storage().saveAddressBook(model.getAddressBook());
        Path roles = directory.resolve("roles.json");
        String original = Files.readString(roles);
        for (String invalid : List.of(original.replace("Designer", " engineer "),
                original.replace("No role", "Renamed built-in"),
                original.replace(model.getRoleCatalog().requireRole("Designer").getId(),
                        model.getRoleCatalog().requireRole("Engineer").getId()))) {
            Files.writeString(roles, invalid);
            assertThrows(DataLoadingException.class, () -> storage().readAddressBook());
        }
    }

    @Test
    void save_rollbackCannotFinish_blocksWritesUntilRestartRecovery() throws Exception {
        ModelManager model = getAssignedModel("Engineer");
        storage().saveAddressBook(model.getAddressBook());
        ReadOnlyAddressBook before = new AddressBook(model.getAddressBook());
        RoleDataStorage failing = new RoleDataStorage(new JsonAddressBookStorage(candidatePath())) {
            @Override
            void writeCatalog(RoleCatalog catalog) throws IOException {
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
    void initialize_catalogOnlyInvalidData_preservesCatalogBeforeRecovery() throws Exception {
        Path roles = directory.resolve("roles.json");
        Files.writeString(roles, "invalid catalog");
        CandidateDataInitializer initializer = new CandidateDataInitializer(storage());
        assertTrue(initializer.load().getPersonList().isEmpty());
        Path backup = initializer.getRecoveryBackup().orElseThrow();
        assertTrue(backup.getFileName().toString().endsWith(".roles.json"));
        assertEquals("invalid catalog", Files.readString(backup));
        assertEquals(List.of(Role.NO_ROLE), storage().readAddressBook().orElseThrow().getRoleCatalog().getRoles());
    }

    @Test
    void read_malformedJournal_preservesJournalForManualRepair() throws Exception {
        Files.writeString(directory.resolve("roles-transaction.json"), "{");
        assertThrows(DataLoadingException.class, () -> storage().readAddressBook());
        assertThrows(IOException.class, () -> storage().backupAddressBook());
        assertEquals("{", Files.readString(directory.resolve("roles-transaction.json")));
    }

    private StorageManager storage() {
        return new StorageManager(new JsonAddressBookStorage(candidatePath()),
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
    }

    private Path candidatePath() {
        return directory.resolve("HRvest.json");
    }
}
