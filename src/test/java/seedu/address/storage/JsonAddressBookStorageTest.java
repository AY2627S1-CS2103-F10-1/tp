package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }

    @Test
    public void saveAddressBook_existingFile_replacesDataAndRemovesTemporaryFile() throws Exception {
        Path destination = testFolder.resolve("nested").resolve("candidates.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(destination);
        AddressBook addressBook = getTypicalAddressBook();
        storage.saveAddressBook(addressBook);
        addressBook.addPerson(HOON);

        storage.saveAddressBook(addressBook);

        assertEquals(addressBook, storage.readAddressBook().orElseThrow());
        try (var files = Files.list(destination.getParent())) {
            assertEquals(List.of(destination), files.toList());
        }
    }

    @Test
    public void saveAddressBook_serializationFailure_keepsDataAndRemovesBrandedTemporaryFile() throws Exception {
        Path destination = testFolder.resolve("candidates.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(destination);
        storage.saveAddressBook(getTypicalAddressBook());
        String existingData = Files.readString(destination);
        AddressBook failingAddressBook = new AddressBook() {
            @Override
            public ObservableList<Person> getPersonList() {
                try (var files = Files.list(testFolder)) {
                    List<Path> temporaryFiles = files.filter(path -> !path.equals(destination)).toList();
                    assertEquals(1, temporaryFiles.size());
                    String fileName = temporaryFiles.getFirst().getFileName().toString();
                    assertTrue(fileName.startsWith("HRvest-"));
                    assertTrue(fileName.endsWith(".tmp"));
                } catch (IOException e) {
                    throw new AssertionError(e);
                }
                throw new IllegalStateException("Cannot serialize candidates");
            }
        };

        assertThrows(IllegalStateException.class, "Cannot serialize candidates", () ->
                storage.saveAddressBook(failingAddressBook));

        assertEquals(existingData, Files.readString(destination));
        try (var files = Files.list(testFolder)) {
            assertEquals(List.of(destination), files.toList());
        }
    }

    @Test
    public void saveAddressBook_withoutPermissionViews_replacesDataAndRemovesTemporaryFile() throws Exception {
        try (var fileSystem = FileSystems.newFileSystem(testFolder.resolve("defaults.zip"),
                Map.of("create", "true", "enablePosixFileAttributes", "false"))) {
            assertFalse(fileSystem.supportedFileAttributeViews().contains("acl"));
            assertFalse(fileSystem.supportedFileAttributeViews().contains("posix"));
            Path destination = fileSystem.getPath("/candidates.json");
            JsonAddressBookStorage storage = new JsonAddressBookStorage(destination);
            AddressBook addressBook = getTypicalAddressBook();
            storage.saveAddressBook(addressBook);
            addressBook.addPerson(HOON);

            storage.saveAddressBook(addressBook);

            assertEquals(addressBook, storage.readAddressBook().orElseThrow());
            try (var files = Files.list(destination.getParent())) {
                assertEquals(List.of(destination), files.toList());
            }
        }
    }

    @Test
    public void saveAddressBook_existingPosixPermissions_preservesPermissions() throws Exception {
        if (Files.getFileAttributeView(testFolder, PosixFileAttributeView.class) != null) {
            assertPosixPermissionsPreserved(testFolder);
            return;
        }
        // Exercise POSIX attributes on Windows using the JDK's POSIX-enabled ZIP filesystem.
        try (var fileSystem = FileSystems.newFileSystem(testFolder.resolve("posix.zip"),
                Map.of("create", "true", "enablePosixFileAttributes", "true"))) {
            assertPosixPermissionsPreserved(fileSystem.getPath("/"));
        }
    }

    /**
     * Asserts that saving preserves POSIX permissions and removes the temporary file.
     */
    private void assertPosixPermissionsPreserved(Path folder) throws Exception {
        Path destination = folder.resolve("candidates.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(destination);
        AddressBook addressBook = getTypicalAddressBook();
        storage.saveAddressBook(addressBook);
        var permissions = PosixFilePermissions.fromString("rw-r--r--");
        Files.setPosixFilePermissions(destination, permissions);
        addressBook.addPerson(HOON);

        storage.saveAddressBook(addressBook);

        assertEquals(permissions, Files.getPosixFilePermissions(destination));
        assertEquals(addressBook, storage.readAddressBook().orElseThrow());
        try (var files = Files.list(folder)) {
            assertEquals(List.of(destination), files.toList());
        }
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    public void saveAddressBook_readOnlyPosixFile_keepsExistingDataAndRemovesTemporaryFile() throws Exception {
        Path destination = testFolder.resolve("candidates.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(destination);
        AddressBook addressBook = getTypicalAddressBook();
        storage.saveAddressBook(addressBook);
        String existingData = Files.readString(destination);
        var permissions = PosixFilePermissions.fromString("r--r--r--");
        Files.setPosixFilePermissions(destination, permissions);
        assumeFalse(Files.isWritable(destination), "The process must be denied write access to the read-only file.");
        addressBook.addPerson(HOON);

        assertThrows(IOException.class, () -> storage.saveAddressBook(addressBook));

        assertEquals(existingData, Files.readString(destination));
        assertEquals(permissions, Files.getPosixFilePermissions(destination));
        try (var files = Files.list(testFolder)) {
            assertEquals(List.of(destination), files.toList());
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void saveAddressBook_existingRestrictedAcl_preservesAcl() throws Exception {
        Path destination = testFolder.resolve("candidates.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(destination);
        AddressBook addressBook = getTypicalAddressBook();
        storage.saveAddressBook(addressBook);
        AclFileAttributeView aclView = Files.getFileAttributeView(destination, AclFileAttributeView.class);
        AclEntry ownerAccess = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(aclView.getOwner())
                .setPermissions(EnumSet.allOf(AclEntryPermission.class))
                .build();
        List<AclEntry> restrictiveAcl = List.of(ownerAccess);
        aclView.setAcl(restrictiveAcl);
        assertEquals(restrictiveAcl, aclView.getAcl());
        addressBook.addPerson(HOON);

        storage.saveAddressBook(addressBook);

        assertEquals(restrictiveAcl, aclView.getAcl());
        assertEquals(addressBook, storage.readAddressBook().orElseThrow());
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void saveAddressBook_readOnlyAcl_keepsDataAndPermissionsAndRemovesTemporaryFile() throws Exception {
        Path destination = testFolder.resolve("candidates.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(destination);
        AddressBook addressBook = getTypicalAddressBook();
        storage.saveAddressBook(addressBook);
        String existingData = Files.readString(destination);
        AclFileAttributeView aclView = Files.getFileAttributeView(destination, AclFileAttributeView.class);
        List<AclEntry> originalAcl = aclView.getAcl();
        var permissions = EnumSet.allOf(AclEntryPermission.class);
        permissions.remove(AclEntryPermission.WRITE_DATA);
        permissions.remove(AclEntryPermission.APPEND_DATA);
        AclEntry readOnlyAccess = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(aclView.getOwner())
                .setPermissions(permissions)
                .build();
        List<AclEntry> readOnlyAcl = List.of(readOnlyAccess);
        aclView.setAcl(readOnlyAcl);
        try {
            assertFalse(Files.isWritable(destination));
            addressBook.addPerson(HOON);

            assertThrows(AccessDeniedException.class, () -> storage.saveAddressBook(addressBook));

            assertEquals(existingData, Files.readString(destination));
            assertEquals(readOnlyAcl, aclView.getAcl());
            try (var files = Files.list(testFolder)) {
                assertEquals(List.of(destination), files.toList());
            }
        } finally {
            aclView.setAcl(originalAcl);
        }
    }

    @Test
    public void saveAddressBook_destinationIsDirectory_keepsExistingDataAndRemovesTemporaryFile() throws Exception {
        Path destination = Files.createDirectory(testFolder.resolve("candidates.json"));
        Path existing = destination.resolve("existing.json");
        Files.writeString(existing, "Existing data");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(destination);

        assertThrows(IOException.class, () -> storage.saveAddressBook(getTypicalAddressBook()));

        assertEquals("Existing data", Files.readString(existing));
        try (var files = Files.list(testFolder)) {
            assertEquals(List.of(destination), files.toList());
        }
    }

    @Test
    public void saveAddressBook_relativeSymbolicLinkChain_updatesTargetAndKeepsLinks() throws Exception {
        Path target = testFolder.resolve("data").resolve("candidates.json");
        JsonAddressBookStorage targetStorage = new JsonAddressBookStorage(target);
        AddressBook addressBook = getTypicalAddressBook();
        targetStorage.saveAddressBook(addressBook);
        Path relativeTarget = testFolder.relativize(target);
        Path intermediateLink = Files.createSymbolicLink(testFolder.resolve("relative-link.json"), relativeTarget);
        Path destination = Files.createSymbolicLink(testFolder.resolve("candidates.json"),
                intermediateLink.getFileName());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(destination);

        addressBook.addPerson(HOON);
        storage.saveAddressBook(addressBook);

        assertTrue(Files.isSymbolicLink(destination));
        assertEquals(intermediateLink.getFileName(), Files.readSymbolicLink(destination));
        assertEquals(relativeTarget, Files.readSymbolicLink(intermediateLink));
        assertEquals(addressBook, targetStorage.readAddressBook().orElseThrow());
        assertEquals(addressBook, storage.readAddressBook().orElseThrow());
        try (var files = Files.list(target.getParent())) {
            assertEquals(List.of(target), files.toList());
        }
    }

    @Test
    public void saveAddressBook_danglingSymbolicLink_throwsIoExceptionAndKeepsLink() throws Exception {
        Path target = testFolder.resolve("missing").resolve("candidates.json");
        Path relativeTarget = testFolder.relativize(target);
        Path destination = Files.createSymbolicLink(testFolder.resolve("candidates.json"), relativeTarget);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(destination);

        assertThrows(IOException.class, () -> storage.saveAddressBook(getTypicalAddressBook()));

        assertEquals(relativeTarget, Files.readSymbolicLink(destination));
        assertFalse(Files.exists(target.getParent()));
        try (var files = Files.list(testFolder)) {
            assertEquals(List.of(destination), files.toList());
        }
    }

    @Test
    public void saveAddressBook_destinationIsFilesystemRoot_throwsIoException() {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.getRoot());
        assertThrows(IOException.class, "The address book file path must refer to a file, not a filesystem root.", () ->
                storage.saveAddressBook(getTypicalAddressBook()));
    }
}
