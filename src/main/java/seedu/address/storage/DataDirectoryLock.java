package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Holds exclusive access to a data directory for the lifetime of an application instance. */
public final class DataDirectoryLock implements AutoCloseable {
    private final FileChannel channel;

    private DataDirectoryLock(FileChannel channel) {
        this.channel = channel;
    }

    /**
     * Acquires an operating-system lock before any shared data is read or written.
     * The persistent lock file is deliberately retained to avoid locking different files after deletion.
     *
     * @throws IOException if the directory cannot be locked or another instance holds the lock.
     */
    public static DataDirectoryLock acquire(Path directory) throws IOException {
        requireNonNull(directory);
        Files.createDirectories(directory);
        Path lockPath = directory.toRealPath().resolve(".hrvest.lock");
        FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                LinkOption.NOFOLLOW_LINKS);
        try {
            try {
                if (channel.tryLock() == null) {
                    throw alreadyRunning(directory);
                }
            } catch (OverlappingFileLockException e) {
                throw alreadyRunning(directory);
            }
            return new DataDirectoryLock(channel);
        } catch (IOException | RuntimeException e) {
            // Retain the acquisition error if releasing the channel also fails.
            try (DataDirectoryLock failedLock = new DataDirectoryLock(channel)) {
                throw e;
            }
        }
    }

    private static IOException alreadyRunning(Path directory) {
        return new IOException("Another HRvest instance is already using " + directory.toAbsolutePath().normalize()
                + ". Close that instance before restarting. Do not delete .hrvest.lock while HRvest is running.");
    }

    @Override
    public void close() throws IOException {
        // Closing the channel releases its OS lock, including after an abnormal process exit.
        channel.close();
    }
}
