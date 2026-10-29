package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.role.RoleCatalog;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);
    // Sorts backups by creation time when listed by name.
    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private Path filePath;
    private final Clock clock;

    public JsonAddressBookStorage(Path filePath) {
        this(filePath, Clock.systemDefaultZone());
    }

    /**
     * Creates a storage whose recovery backups are timestamped using {@code clock}.
     */
    JsonAddressBookStorage(Path filePath, Clock clock) {
        this.filePath = filePath;
        this.clock = clock;
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
        return readAddressBook(filePath, new RoleCatalog());
    }

    /** Reads candidate references against a separately loaded catalog. */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath, RoleCatalog catalog)
            throws DataLoadingException {
        requireNonNull(filePath);
        if (!isExistingDataFile(filePath)) {
            return Optional.empty();
        }
        Optional<JsonSerializableAddressBook> jsonAddressBook = JsonUtil.readJsonFile(
                filePath, JsonSerializableAddressBook.class);
        if (!jsonAddressBook.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonAddressBook.get().toModelType(catalog));
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Distinguishes a genuinely missing file from inaccessible paths and dangling symbolic links.
     */
    private static boolean isExistingDataFile(Path filePath) throws DataLoadingException {
        try {
            if (!Files.readAttributes(filePath, BasicFileAttributes.class).isRegularFile()) {
                throw new IOException("The candidate data path must be a regular file.");
            }
            return true;
        } catch (NoSuchFileException e) {
            if (!Files.isSymbolicLink(filePath)) {
                return false;
            }
            throw new DataLoadingException(e);
        } catch (IOException e) {
            throw new DataLoadingException(e);
        }
    }

    /**
     * Creates a persistent, timestamped, uniquely named backup with the original bytes and access restrictions.
     * Resolves existing symbolic links and fails safely for missing, dangling, or inaccessible paths.
     *
     * @return The absolute backup path beside the resolved data file.
     * @throws IOException if backup creation, permission preservation, or copying fails.
     */
    public Path backupAddressBook() throws IOException {
        Path source = filePath.toRealPath();
        if (!Files.readAttributes(source, BasicFileAttributes.class).isRegularFile()) {
            throw new IOException("The candidate data path must be a regular file.");
        }
        String timestamp = LocalDateTime.now(clock).format(BACKUP_TIMESTAMP_FORMAT);
        Path backup = Files.createTempFile(source.getParent(), "HRvest-" + timestamp + "-", "-corrupt.json");
        try {
            // Restrictions are applied last so that a write-restricted original does not block the copy.
            copyDataFile(source, backup);
            copyExistingAcl(Files.getFileAttributeView(source, AclFileAttributeView.class),
                    Files.getFileAttributeView(backup, AclFileAttributeView.class));
            copyExistingPosixPermissions(source, backup);
            return backup;
        } catch (IOException e) {
            try {
                Files.deleteIfExists(backup);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw e;
        }
    }

    /**
     * Copies candidate bytes into the existing backup file.
     */
    void copyDataFile(Path source, Path backup) throws IOException {
        try (OutputStream output = Files.newOutputStream(backup)) {
            Files.copy(source, output);
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
        writeFile(filePath, filePath,
                temporary -> JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), temporary));
    }

    /** Atomically saves a JSON value while preserving existing destination permissions and links. */
    static void writeJson(Object value, Path filePath) throws IOException {
        writeFile(filePath, filePath, temporary -> JsonUtil.saveJsonFile(value, temporary));
    }

    /** Atomically restores raw before-image bytes without parsing or modifying the original data. */
    static void writeBytes(byte[] bytes, Path filePath) throws IOException {
        writeBytes(bytes, filePath, filePath);
    }

    /** Applies source permissions before writing sensitive journal bytes to a new destination. */
    static void writeBytes(byte[] bytes, Path filePath, Path permissionsSource) throws IOException {
        writeFile(filePath, permissionsSource, temporary -> Files.write(temporary, bytes));
    }

    private static void writeFile(Path filePath, Path permissionsSource, FileWriter writer) throws IOException {
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
            Path permissions = Files.exists(destination) ? destination : permissionsSource;
            copyExistingAcl(Files.getFileAttributeView(permissions, AclFileAttributeView.class),
                    Files.getFileAttributeView(temporaryFile, AclFileAttributeView.class));
            copyExistingPosixPermissions(permissions, temporaryFile);
            writer.write(temporaryFile);
            try (FileChannel channel = FileChannel.open(temporaryFile, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            // Some providers replace attributes when closing a write channel (notably ZIP filesystems).
            copyExistingAcl(Files.getFileAttributeView(permissions, AclFileAttributeView.class),
                    Files.getFileAttributeView(temporaryFile, AclFileAttributeView.class));
            copyExistingPosixPermissions(permissions, temporaryFile);
            Files.move(temporaryFile, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    @FunctionalInterface
    private interface FileWriter {
        void write(Path path) throws IOException;
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
    static void copyExistingPosixPermissions(Path destination, Path temporaryFile) throws IOException {
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
