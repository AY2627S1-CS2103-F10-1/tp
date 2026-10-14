package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;

public class CandidateDataBackupTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void backupAddressBook_invalidBytes_preservesExactBytesAndExistingBackups() throws Exception {
        Path path = temporaryFolder.resolve("candidates.json");
        byte[] original = {(byte) 0xff, 0, 13, 10, 32};
        Files.write(path, original);
        Path previous = temporaryFolder.resolve("HRvest-existing-corrupt.json");
        Files.writeString(previous, "previous backup");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);

        Path backup = storage.backupAddressBook();

        assertArrayEquals(original, Files.readAllBytes(backup));
        assertArrayEquals(original, Files.readAllBytes(path));
        assertEquals(path.getParent().toRealPath(), backup.getParent());
        assertTrue(backup.getFileName().toString().startsWith("HRvest-"));
        assertTrue(backup.getFileName().toString().endsWith("-corrupt.json"));
        assertEquals("previous backup", Files.readString(previous));
    }

    @Test
    public void backupAddressBook_copyFailure_removesOnlyPartialBackup() throws Exception {
        Path path = temporaryFolder.resolve("candidates.json");
        Files.writeString(path, "original data");
        Path previous = temporaryFolder.resolve("HRvest-existing-corrupt.json");
        Files.writeString(previous, "previous backup");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path) {
            @Override
            void copyDataFile(Path source, Path backup) throws IOException {
                Files.writeString(backup, "partial data");
                throw new IOException("Copy failed");
            }
        };

        assertThrows(IOException.class, "Copy failed", storage::backupAddressBook);

        assertEquals("original data", Files.readString(path));
        assertEquals("previous backup", Files.readString(previous));
        try (var files = Files.list(temporaryFolder)) {
            assertEquals(2, files.count());
        }
    }

    @Test
    public void backupAddressBook_cleanupFailure_reportsCopyFailureAndRetainsOriginal() throws Exception {
        Path path = temporaryFolder.resolve("candidates.json");
        Files.writeString(path, "original data");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path) {
            @Override
            void copyDataFile(Path source, Path backup) throws IOException {
                Files.delete(backup);
                Files.createDirectory(backup);
                Files.writeString(backup.resolve("partial"), "partial data");
                throw new IOException("Copy failed");
            }
        };

        assertThrows(IOException.class, "Copy failed", () -> {
            try {
                storage.backupAddressBook();
            } catch (IOException e) {
                assertEquals(1, e.getSuppressed().length);
                throw e;
            }
        });

        assertEquals("original data", Files.readString(path));
    }

    @Test
    public void backupAddressBook_symbolicLink_preservesLinkAndBacksUpTarget() throws Exception {
        Path target = temporaryFolder.resolve("target").resolve("candidates.json");
        Files.createDirectories(target.getParent());
        Files.writeString(target, "original data");
        Path link = Files.createSymbolicLink(temporaryFolder.resolve("link.json"),
                temporaryFolder.relativize(target));

        Path backup = new JsonAddressBookStorage(link).backupAddressBook();

        assertEquals(target.getParent().toRealPath(), backup.getParent());
        assertEquals("original data", Files.readString(backup));
        assertTrue(Files.isSymbolicLink(link));
        assertEquals("original data", Files.readString(target));
    }

    @Test
    public void readAndBackupAddressBook_danglingLink_failsWithoutTreatingFileAsMissing() throws Exception {
        Path target = temporaryFolder.resolve("missing.json");
        Path link = Files.createSymbolicLink(temporaryFolder.resolve("link.json"), target.getFileName());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(link);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertThrows(IOException.class, storage::backupAddressBook);

        assertTrue(Files.isSymbolicLink(link));
        assertFalse(Files.exists(target));
        try (var files = Files.list(temporaryFolder)) {
            assertEquals(List.of(link), files.toList());
        }
    }

    @Test
    public void readAndBackupAddressBook_directory_failsWithoutCreatingBackup() throws Exception {
        Path directory = temporaryFolder.resolve("candidates.json");
        Files.createDirectory(directory);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertThrows(IOException.class, storage::backupAddressBook);

        try (var files = Files.list(temporaryFolder)) {
            assertEquals(List.of(directory), files.toList());
        }
    }

    @Test
    public void backupAddressBook_posixPermissions_preservesPermissions() throws Exception {
        try (var fileSystem = FileSystems.newFileSystem(temporaryFolder.resolve("posix.zip"),
                Map.of("create", "true", "enablePosixFileAttributes", "true"))) {
            Path path = fileSystem.getPath("/candidates.json");
            Files.writeString(path, "original data");
            var permissions = PosixFilePermissions.fromString("rw-------");
            Files.setPosixFilePermissions(path, permissions);

            Path backup = new JsonAddressBookStorage(path).backupAddressBook();

            assertEquals(permissions, Files.getPosixFilePermissions(backup));
            assertEquals("original data", Files.readString(backup));
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void readAndBackupAddressBook_inaccessibleFile_failsWithoutLosingOriginal() throws Exception {
        Path path = temporaryFolder.resolve("candidates.json");
        Files.writeString(path, "original data");
        AclFileAttributeView aclView = Files.getFileAttributeView(path, AclFileAttributeView.class);
        List<AclEntry> originalAcl = aclView.getAcl();
        AclEntry deniedRead = AclEntry.newBuilder().setType(AclEntryType.DENY)
                .setPrincipal(aclView.getOwner()).setPermissions(AclEntryPermission.READ_DATA).build();
        List<AclEntry> restrictedAcl = new ArrayList<>();
        restrictedAcl.add(deniedRead);
        restrictedAcl.addAll(originalAcl);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);

        try {
            aclView.setAcl(restrictedAcl);
            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertThrows(IOException.class, storage::backupAddressBook);
        } finally {
            aclView.setAcl(originalAcl);
        }

        assertEquals("original data", Files.readString(path));
        try (var files = Files.list(temporaryFolder)) {
            assertEquals(List.of(path), files.toList());
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void backupAddressBook_restrictedAcl_preservesAcl() throws Exception {
        Path path = temporaryFolder.resolve("candidates.json");
        Files.writeString(path, "original data");
        AclFileAttributeView aclView = Files.getFileAttributeView(path, AclFileAttributeView.class);
        AclEntry ownerAccess = AclEntry.newBuilder().setType(AclEntryType.ALLOW)
                .setPrincipal(aclView.getOwner()).setPermissions(EnumSet.allOf(AclEntryPermission.class)).build();
        aclView.setAcl(List.of(ownerAccess));

        Path backup = new JsonAddressBookStorage(path).backupAddressBook();

        assertEquals(aclView.getAcl(), Files.getFileAttributeView(backup, AclFileAttributeView.class).getAcl());
        assertEquals("original data", Files.readString(backup));
    }
}
