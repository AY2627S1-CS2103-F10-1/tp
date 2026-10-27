package seedu.address.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.role.RoleCatalog;
import seedu.address.model.util.SampleDataUtil;

/** Coordinates candidate and catalog files using a rollback journal with fixed local destinations. */
class RoleDataStorage {
    private static final int JOURNAL_VERSION = 1;
    private static final String ROLES_FILE_NAME = "roles.json";
    private static final String JOURNAL_FILE_NAME = "roles-transaction.json";
    private final JsonAddressBookStorage candidates;
    private final Path rolesPath;
    private final Path journalPath;
    private boolean recoveryRequired;

    RoleDataStorage(JsonAddressBookStorage candidates) {
        this.candidates = candidates;
        rolesPath = candidates.getAddressBookFilePath().resolveSibling(ROLES_FILE_NAME);
        journalPath = candidates.getAddressBookFilePath().resolveSibling(JOURNAL_FILE_NAME);
    }

    Optional<ReadOnlyAddressBook> read() throws DataLoadingException {
        try {
            validateDestinations();
            recover();
            RoleCatalog catalog = readCatalog();
            Optional<ReadOnlyAddressBook> loaded = candidates.readAddressBook(candidates.getAddressBookFilePath(),
                    catalog);
            if (loaded.isEmpty() && exists(rolesPath)) {
                AddressBook sample = new AddressBook(SampleDataUtil.getSampleAddressBook());
                sample.setRoleCatalog(catalog);
                return Optional.of(sample);
            }
            return loaded;
        } catch (IOException | IllegalValueException e) {
            throw new DataLoadingException(e);
        }
    }

    private RoleCatalog readCatalog() throws DataLoadingException, IllegalValueException, IOException {
        if (!exists(rolesPath)) {
            return new RoleCatalog();
        }
        if (!Files.isRegularFile(rolesPath)) {
            throw new IOException("The role catalog path must be a readable regular file.");
        }
        JsonRoleCatalog stored = JsonUtil.readJsonFile(rolesPath, JsonRoleCatalog.class)
                .orElseThrow(() -> new IOException("The role catalog could not be read."));
        return stored.toModelType();
    }

    void save(ReadOnlyAddressBook data) throws IOException {
        validateDestinations();
        if (recoveryRequired || exists(journalPath)) {
            throw new IOException("An interrupted data save needs recovery. Restart HRvest before saving again.");
        }
        // Validate the complete pair before touching either destination.
        new AddressBook(data);
        Journal journal = writeJournal();
        try {
            candidates.saveAddressBook(data);
            writeCatalog(data.getRoleCatalog());
            Files.delete(journalPath);
        } catch (IOException e) {
            try {
                restore(journal);
            } catch (IOException rollbackError) {
                recoveryRequired = true;
                e.addSuppressed(rollbackError);
            }
            throw e;
        }
    }

    /** Captures and persists both before-images with existing data-file permissions before either file changes. */
    private Journal writeJournal() throws IOException {
        Journal journal = new Journal(JOURNAL_VERSION, Snapshot.capture(candidates.getAddressBookFilePath()),
                Snapshot.capture(rolesPath));
        Path permissions = exists(candidates.getAddressBookFilePath())
                ? candidates.getAddressBookFilePath() : rolesPath;
        JsonAddressBookStorage.writeBytes(JsonUtil.toJsonString(journal).getBytes(StandardCharsets.UTF_8),
                journalPath, permissions);
        return journal;
    }

    /** Provides a narrow fault-injection seam for testing a failure between the two file commits. */
    void writeCatalog(RoleCatalog catalog) throws IOException {
        JsonAddressBookStorage.writeJson(new JsonRoleCatalog(catalog), rolesPath);
    }

    private void recover() throws IOException {
        if (exists(journalPath)) {
            recoveryRequired = true;
            final Journal journal;
            try {
                journal = JsonUtil.readJsonFile(journalPath, Journal.class)
                        .orElseThrow(() -> new IOException("The data transaction journal is missing."));
            } catch (DataLoadingException e) {
                throw new IOException("Cannot recover the data transaction journal; retain it for repair.", e);
            }
            if (!journal.isValid()) {
                throw new IOException("The data transaction journal is invalid or uses an unsupported version; "
                        + "retain it for manual repair.");
            }
            restore(journal);
            recoveryRequired = false;
        }
    }

    private void restore(Journal journal) throws IOException {
        if (!journal.candidates.matchesDestination(candidates.getAddressBookFilePath())
                || !journal.roles.matchesDestination(rolesPath)) {
            throw new IOException("The data transaction journal belongs to different destinations. "
                    + "Retain it, restore the original data paths or symbolic-link targets, and restart HRvest.");
        }
        journal.candidates.restore(candidates.getAddressBookFilePath());
        journal.roles.restore(rolesPath);
        Files.delete(journalPath);
    }

    /** Preserves both originals before recovery can replace either file. */
    Path backup() throws IOException {
        validateDestinations();
        if (recoveryRequired || exists(journalPath)) {
            throw new IOException("Cannot replace data while transaction recovery is blocked. Retain " + journalPath
                    + ", restore the original data paths or symbolic-link targets, and restart HRvest. "
                    + "Invalid journals need manual repair.");
        }
        Path candidateBackup = exists(candidates.getAddressBookFilePath()) ? candidates.backupAddressBook() : null;
        Path roleBackup = exists(rolesPath) ? new JsonAddressBookStorage(rolesPath).backupAddressBook() : null;
        if (candidateBackup != null && roleBackup != null) {
            Path companion = candidateBackup.resolveSibling(candidateBackup.getFileName() + ".roles.json");
            Files.move(roleBackup, companion);
        }
        if (candidateBackup == null && roleBackup == null) {
            throw new IOException("No original data files could be preserved.");
        }
        if (candidateBackup == null) {
            Path primary = roleBackup.resolveSibling(roleBackup.getFileName() + ".roles.json");
            Files.move(roleBackup, primary);
            return primary;
        }
        return candidateBackup;
    }

    /** Rejects aliases before any journal recovery, backup, or save can modify the data files. */
    private void validateDestinations() throws IOException {
        Path[] paths = {candidates.getAddressBookFilePath(), rolesPath, journalPath};
        for (int i = 0; i < paths.length; i++) {
            for (int j = i + 1; j < paths.length; j++) {
                if (resolveDestination(paths[i]).equals(resolveDestination(paths[j]))
                        || (exists(paths[i]) && exists(paths[j]) && Files.isSameFile(paths[i], paths[j]))) {
                    throw new IOException("Candidate, role catalog, and transaction journal paths must refer "
                            + "to different files. Change the candidate data path or conflicting symbolic links.");
                }
            }
        }
    }

    /** Resolves existing links and parent directories while allowing a new destination to be absent. */
    private static Path resolveDestination(Path path) throws IOException {
        Path absolute = path.toAbsolutePath();
        if (exists(absolute)) {
            return absolute.toRealPath();
        }
        Path parent = absolute.getParent();
        return parent == null ? absolute : resolveDestination(parent).resolve(absolute.getFileName()).normalize();
    }

    private static boolean exists(Path path) throws IOException {
        try {
            Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            return true;
        } catch (NoSuchFileException e) {
            return false;
        }
    }

    /** Stores before-images only, so an interrupted save is always rolled back as one pair. */
    static class Journal {
        private final Integer version;
        private final Snapshot candidates;
        private final Snapshot roles;

        @JsonCreator
        Journal(@JsonProperty("version") Integer version, @JsonProperty("candidates") Snapshot candidates,
                @JsonProperty("roles") Snapshot roles) {
            this.version = version;
            this.candidates = candidates;
            this.roles = roles;
        }

        boolean isValid() {
            return Integer.valueOf(JOURNAL_VERSION).equals(version) && candidates != null && roles != null
                    && candidates.isValid() && roles.isValid();
        }
    }

    /** Distinguishes a previously absent file from a present file containing any bytes. */
    static class Snapshot {
        private final String destination;
        private final Boolean present;
        private final byte[] bytes;

        @JsonCreator
        Snapshot(@JsonProperty("destination") String destination, @JsonProperty("present") Boolean present,
                @JsonProperty("bytes") byte[] bytes) {
            this.destination = destination;
            this.present = present;
            this.bytes = bytes == null ? null : bytes.clone();
        }

        static Snapshot capture(Path path) throws IOException {
            String destination = resolveDestination(path).toString();
            return exists(path) ? new Snapshot(destination, true, Files.readAllBytes(path))
                    : new Snapshot(destination, false, null);
        }

        boolean isValid() {
            return destination != null && !destination.isBlank()
                    && present != null && (present ? bytes != null : bytes == null);
        }

        /** Compares stored identities without following paths supplied by the journal. */
        boolean matchesDestination(Path path) throws IOException {
            try {
                return resolveDestination(path).equals(Path.of(destination));
            } catch (InvalidPathException e) {
                return false;
            }
        }

        void restore(Path path) throws IOException {
            if (present) {
                JsonAddressBookStorage.writeBytes(bytes, path);
            } else {
                Files.deleteIfExists(path);
            }
        }
    }
}
