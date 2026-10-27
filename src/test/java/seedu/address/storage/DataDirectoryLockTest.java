package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DataDirectoryLockTest {
    @TempDir
    public Path directory;

    @Test
    public void acquire_sameDirectory_rejectsSecondOwner() throws Exception {
        Path data = directory.resolve("data");
        try (DataDirectoryLock first = DataDirectoryLock.acquire(data)) {
            assertThrows(IOException.class, conflictMessage(data), () ->
                     DataDirectoryLock.acquire(data.resolve("../data")));
            assertTrue(Files.isRegularFile(data.resolve(DataDirectoryLock.LOCK_FILE_NAME)));
        }
    }

    @Test
    public void close_lockedDirectory_allowsReopening() throws Exception {
        DataDirectoryLock first = DataDirectoryLock.acquire(directory);
        first.close();

        try (DataDirectoryLock reopened = DataDirectoryLock.acquire(directory)) {
            assertThrows(IOException.class, conflictMessage(directory), () ->
                     DataDirectoryLock.acquire(directory));
        }
        assertTrue(Files.exists(directory.resolve(DataDirectoryLock.LOCK_FILE_NAME)));
    }

    @Test
    public void acquire_differentDirectories_allowsIndependentInstances() throws Exception {
        try (DataDirectoryLock first = DataDirectoryLock.acquire(directory.resolve("first"));
                DataDirectoryLock second = DataDirectoryLock.acquire(directory.resolve("second"))) {
            assertTrue(Files.exists(directory.resolve("first").resolve(DataDirectoryLock.LOCK_FILE_NAME)));
            assertTrue(Files.exists(directory.resolve("second").resolve(DataDirectoryLock.LOCK_FILE_NAME)));
        }
    }

    @Test
    public void acquire_filePath_failsWithoutChangingFile() throws Exception {
        Path file = Files.writeString(directory.resolve("file"), "keep");

        assertThrows(IOException.class, () -> DataDirectoryLock.acquire(file));
        assertEquals("keep", Files.readString(file));
    }

    @Test
    public void acquire_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> DataDirectoryLock.acquire(null));
    }

    @Test
    public void acquire_lockFileIsDirectory_failsWithoutChangingDirectory() throws Exception {
        Path lock = Files.createDirectory(directory.resolve(DataDirectoryLock.LOCK_FILE_NAME));

        assertThrows(IOException.class, () -> DataDirectoryLock.acquire(directory));
        assertTrue(Files.isDirectory(lock));
    }

    @Test
    public void acquire_otherProcessHoldsLock_rejectsUntilOwnerCloses() throws Exception {
        try (DataDirectoryLock owner = DataDirectoryLock.acquire(directory)) {
            assertEquals(2, runProbe());
        }
        assertEquals(0, runProbe());
        assertEquals(0, runProbe());
    }

    @Test
    public void acquire_otherJvmOwnsDirectory_reportsConflictWithoutWaiting() throws Exception {
        Process owner = startProbe(true);
        try {
            String ready = CompletableFuture.supplyAsync(() -> {
                try {
                    return owner.inputReader().readLine();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }).get(20, TimeUnit.SECONDS);
            assertEquals("Acquired", ready);

            assertThrows(IOException.class, conflictMessage(directory), () ->
                     DataDirectoryLock.acquire(directory));

            owner.getOutputStream().close();
            assertTrue(owner.waitFor(20, TimeUnit.SECONDS));
            assertEquals(0, owner.exitValue());
        } finally {
            owner.destroyForcibly();
        }
        try (DataDirectoryLock reopened = DataDirectoryLock.acquire(directory)) {
            assertTrue(Files.isRegularFile(directory.resolve(DataDirectoryLock.LOCK_FILE_NAME)));
        }
    }

    private String conflictMessage(Path data) {
        return "Another HRvest instance is already using " + data.toAbsolutePath().normalize()
                + ". Close that instance before restarting. Do not delete "
                + DataDirectoryLock.LOCK_FILE_NAME + " while HRvest is running.";
    }

    private int runProbe() throws Exception {
        Process process = startProbe(false);
        try {
            assertTrue(process.waitFor(20, TimeUnit.SECONDS));
            int exitCode = process.exitValue();
            if (exitCode == 2) {
                assertTrue(process.inputReader().readAllAsString()
                        .contains("Another HRvest instance is already using"));
            }
            return exitCode;
        } finally {
            process.destroyForcibly();
        }
    }

    private Process startProbe(boolean hold) throws Exception {
        String classPath = Path.of(DataDirectoryLock.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI())
                + File.pathSeparator
                + Path.of(DataDirectoryLockTest.class.getProtectionDomain()
                        .getCodeSource().getLocation().toURI());
        return new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", classPath, LockProbe.class.getName(), directory.toString(), Boolean.toString(hold))
                .redirectErrorStream(true).start();
    }

    /** Attempts to acquire the directory in another JVM, independent of same-process lock detection. */
    public static class LockProbe {
        public static void main(String[] args) throws IOException {
            try (DataDirectoryLock lock = DataDirectoryLock.acquire(Path.of(args[0]))) {
                System.out.println("Acquired");
                if (Boolean.parseBoolean(args[1])) {
                    System.in.read();
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(2);
            }
        }
    }
}
