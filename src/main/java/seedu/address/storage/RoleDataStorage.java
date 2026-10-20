package seedu.address.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
import seedu.address.model.role.RoleCatalogue;
import seedu.address.model.util.SampleDataUtil;

/** Coordinates candidate and catalogue files using a rollback journal with fixed local destinations. */
class RoleDataStorage {
    private final JsonAddressBookStorage candidates;
    private final Path rolesPath;
    private final Path journalPath;
    private boolean recoveryRequired;

    RoleDataStorage(JsonAddressBookStorage candidates) {
        this.candidates = candidates;
        rolesPath = candidates.getAddressBookFilePath().resolveSibling("roles.json");
        journalPath = candidates.getAddressBookFilePath().resolveSibling("roles-transaction.json");
    }

    Optional<ReadOnlyAddressBook> read() throws DataLoadingException {
        try {
            recover();
            RoleCatalogue catalogue = readCatalogue();
            Optional<ReadOnlyAddressBook> loaded = candidates.readAddressBook(candidates.getAddressBookFilePath(),
                    catalogue);
            if (loaded.isEmpty() && exists(rolesPath)) {
                AddressBook sample = new AddressBook(SampleDataUtil.getSampleAddressBook());
                sample.setRoleCatalogue(catalogue);
                return Optional.of(sample);
            }
            return loaded;
        } catch (IOException | IllegalValueException e) {
            throw new DataLoadingException(e);
        }
    }

    private RoleCatalogue readCatalogue() throws DataLoadingException, IllegalValueException, IOException {
        if (!exists(rolesPath)) {
            return new RoleCatalogue();
        }
        if (!Files.isRegularFile(rolesPath)) {
            throw new IOException("The role catalogue path must be a readable regular file.");
        }
        JsonRoleCatalogue stored = JsonUtil.readJsonFile(rolesPath, JsonRoleCatalogue.class)
                .orElseThrow(() -> new IOException("The role catalogue could not be read."));
        return stored.toModelType();
    }

    void save(ReadOnlyAddressBook data) throws IOException {
        if (recoveryRequired || exists(journalPath)) {
            throw new IOException("An interrupted data save needs recovery. Restart HRvest before saving again.");
        }
        // Validate the complete pair before touching either destination.
        new AddressBook(data);
        Journal journal = new Journal(1, Snapshot.capture(candidates.getAddressBookFilePath()),
                Snapshot.capture(rolesPath));
        Path permissions = exists(candidates.getAddressBookFilePath())
                ? candidates.getAddressBookFilePath() : rolesPath;
        JsonAddressBookStorage.writeBytes(JsonUtil.toJsonString(journal).getBytes(StandardCharsets.UTF_8),
                journalPath, permissions);
        try {
            candidates.saveAddressBook(data);
            writeCatalogue(data.getRoleCatalogue());
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

    /** Provides a narrow fault-injection seam for testing a failure between the two file commits. */
    void writeCatalogue(RoleCatalogue catalogue) throws IOException {
        JsonAddressBookStorage.writeJson(new JsonRoleCatalogue(catalogue), rolesPath);
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
                throw new IOException("The data transaction journal is invalid; retain it for repair.");
            }
            restore(journal);
            recoveryRequired = false;
        }
    }

    private void restore(Journal journal) throws IOException {
        journal.candidates.restore(candidates.getAddressBookFilePath());
        journal.roles.restore(rolesPath);
        Files.delete(journalPath);
    }

    /** Preserves both originals before recovery can replace either file. */
    Path backup() throws IOException {
        if (recoveryRequired || exists(journalPath)) {
            throw new IOException("Repair the interrupted transaction before replacing candidate or role data.");
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
            return Integer.valueOf(1).equals(version) && candidates != null && roles != null
                    && candidates.isValid() && roles.isValid();
        }
    }

    /** Distinguishes a previously absent file from a present file containing any bytes. */
    static class Snapshot {
        private final Boolean present;
        private final byte[] bytes;

        @JsonCreator
        Snapshot(@JsonProperty("present") Boolean present, @JsonProperty("bytes") byte[] bytes) {
            this.present = present;
            this.bytes = bytes == null ? null : bytes.clone();
        }

        static Snapshot capture(Path path) throws IOException {
            return exists(path) ? new Snapshot(true, Files.readAllBytes(path)) : new Snapshot(false, null);
        }

        boolean isValid() {
            return present != null && (present ? bytes != null : bytes == null);
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
