package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableAddressBook> jsonAddressBook = JsonUtil.readJsonFile(
                filePath, JsonSerializableAddressBook.class);
        if (!jsonAddressBook.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonAddressBook.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     * Writes a temporary file before atomically replacing the destination to preserve data on failed writes.
     * Resolves symbolic links to existing targets before saving; dangling links cause an {@link IOException}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        Path destination = filePath.toAbsolutePath();
        if (Files.isSymbolicLink(destination)) {
            destination = destination.toRealPath();
        }
        Path parent = destination.getParent();
        if (parent == null) {
            throw new IOException("The address book file path must refer to a file, not a filesystem root.");
        }
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "HRvest-", ".tmp");
        try {
            copyExistingAcl(Files.getFileAttributeView(destination, AclFileAttributeView.class),
                    Files.getFileAttributeView(temporaryFile, AclFileAttributeView.class));
            copyExistingPosixPermissions(destination, temporaryFile);
            JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), temporaryFile);
            Files.move(temporaryFile, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    /**
     * Copies the existing destination's ACL to the temporary file before candidate data is written.
     * Leaves the default ACL unchanged when the destination is absent or ACLs are unsupported.
     *
     * @param destinationAclView The destination's ACL view, or null if ACLs are unsupported.
     * @param temporaryAclView The temporary file's ACL view.
     * @throws IOException if reading or applying the ACL fails.
     */
    static void copyExistingAcl(AclFileAttributeView destinationAclView, AclFileAttributeView temporaryAclView)
            throws IOException {
        if (destinationAclView == null) {
            return;
        }
        List<AclEntry> acl;
        try {
            acl = destinationAclView.getAcl();
        } catch (NoSuchFileException e) {
            return;
        }
        temporaryAclView.setAcl(acl);
    }

    /**
     * Copies existing POSIX permissions to the temporary file before candidate data is written.
     * Leaves the default permissions unchanged when the destination is absent or POSIX attributes are unsupported.
     *
     * @throws IOException if reading or applying the permissions fails.
     */
    private static void copyExistingPosixPermissions(Path destination, Path temporaryFile) throws IOException {
        PosixFileAttributeView posixView = Files.getFileAttributeView(destination, PosixFileAttributeView.class);
        if (posixView == null) {
            return;
        }
        PosixFileAttributes attributes;
        try {
            attributes = posixView.readAttributes();
        } catch (NoSuchFileException e) {
            return;
        }
        Files.setPosixFilePermissions(temporaryFile, attributes.permissions());
    }

}
