package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.util.SampleDataUtil;

/**
 * Loads candidate data, preserving rejected files before saving replacement data during startup.
 */
public class CandidateDataInitializer {
    private static final Logger logger = LogsCenter.getLogger(CandidateDataInitializer.class);

    private final Storage storage;
    private Path recoveryBackup;

    /**
     * Creates an initializer that loads, and if needed recovers, candidate data through {@code storage}.
     */
    public CandidateDataInitializer(Storage storage) {
        this.storage = storage;
    }

    /**
     * Returns loaded data, sample data for a missing file, or saved empty data after a successful backup.
     *
     * @throws IOException if recovery cannot safely finish before commands become available.
     */
    public ReadOnlyAddressBook load() throws IOException {
        try {
            Optional<ReadOnlyAddressBook> savedData = storage.readAddressBook();
            if (savedData.isEmpty()) {
                logger.info("No candidate data file found. Starting with sample candidates.");
            }
            return savedData.orElseGet(SampleDataUtil::getSampleAddressBook);
        } catch (DataLoadingException e) {
            // The failure may come from the role catalog, so neither file is singled out.
            logger.warning("Could not load the candidate data at " + storage.getAddressBookFilePath()
                    + " or the role catalog beside it. Backing them up and starting with no candidates or roles.");
            recoveryBackup = storage.backupAddressBook();
            logger.info("Backed up the data that could not be loaded to " + recoveryBackup);
            AddressBook replacement = new AddressBook();
            storage.saveAddressBook(replacement);
            return replacement;
        }
    }

    public Optional<Path> getRecoveryBackup() {
        return Optional.ofNullable(recoveryBackup);
    }
}
