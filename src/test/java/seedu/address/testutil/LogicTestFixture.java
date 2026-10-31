package seedu.address.testutil;

import java.io.IOException;
import java.nio.file.Path;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Connects a supplied model to real logic and storage in a test's temporary directory. */
public final class LogicTestFixture {
    private final JsonAddressBookStorage candidateStorage;
    private final StorageManager storage;
    private final Logic logic;

    /** Creates a fixture without reading or saving candidate data. */
    public LogicTestFixture(Path directory, Model model) {
        this(directory, model, new JsonAddressBookStorage(directory.resolve("candidates.json")));
    }

    /** Creates a fixture with supplied candidate storage, including storage that injects a failure. */
    public LogicTestFixture(Path directory, Model model, JsonAddressBookStorage candidateStorage) {
        this.candidateStorage = candidateStorage;
        storage = new StorageManager(candidateStorage,
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        logic = new LogicManager(model, storage);
    }

    public JsonAddressBookStorage getCandidateStorage() {
        return candidateStorage;
    }

    public StorageManager getStorage() {
        return storage;
    }

    public Logic getLogic() {
        return logic;
    }

    /** Reads the saved candidates, failing if the scenario has not saved a file. */
    public ReadOnlyAddressBook readSaved() throws IOException, DataLoadingException {
        return storage.readAddressBook().orElseThrow();
    }
}
