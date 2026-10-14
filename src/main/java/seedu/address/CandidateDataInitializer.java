package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.Storage;

/**
 * Loads candidate data, preserving rejected files before saving replacement data during startup.
 */
class CandidateDataInitializer {
    private final Storage storage;
    private Path recoveryBackup;

    CandidateDataInitializer(Storage storage) {
        this.storage = storage;
    }

    /**
     * Returns loaded data, sample data for a missing file, or saved empty data after a successful backup.
     *
     * @throws IOException if recovery cannot safely finish before commands become available.
     */
    public ReadOnlyAddressBook load() throws IOException {
        try {
            return storage.readAddressBook().orElseGet(SampleDataUtil::getSampleAddressBook);
        } catch (DataLoadingException e) {
            recoveryBackup = storage.backupAddressBook();
            AddressBook replacement = new AddressBook();
            storage.saveAddressBook(replacement);
            return replacement;
        }
    }

    public Optional<Path> getRecoveryBackup() {
        return Optional.ofNullable(recoveryBackup);
    }
}
