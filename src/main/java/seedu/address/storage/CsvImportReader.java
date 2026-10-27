package seedu.address.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;

/** Reads a bounded copy of the fixed import file without following links. */
public class CsvImportReader {
    public static final int MAX_BYTES = 10 * 1024 * 1024;
    private static final int BUFFER_SIZE = 8192;

    /** Reads the import beside the configured data directory. */
    public byte[] read(Path dataFile) throws IOException {
        Path dataDirectory = dataFile.toAbsolutePath().normalize().getParent();
        if (dataDirectory == null || dataDirectory.getParent() == null) {
            throw new IOException("The import directory could not be determined from the configured data file.");
        }
        Path source = dataDirectory.getParent().resolve("import.csv");
        try {
            checkInput(source);
            try (FileChannel channel = FileChannel.open(source, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
                return readBytes(channel);
            }
        } catch (IOException | SecurityException | UnsupportedOperationException e) {
            String reason = e instanceof NoSuchFileException ? "File not found."
                    : e instanceof AccessDeniedException ? "Read permission denied." : e.getMessage();
            if (reason == null || reason.isBlank()) {
                reason = "The file could not be safely opened or read on this filesystem.";
            }
            throw new IOException("Could not read " + source + ": " + reason, e);
        }
    }

    private void checkInput(Path source) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(source, BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
        if (!attributes.isRegularFile()) {
            throw new IOException("Input must be a regular file; links, directories and special files are rejected.");
        }
        if (attributes.size() > MAX_BYTES) {
            throw new IOException("Input exceeds the 10 MiB file limit.");
        }
    }

    /** Reads bounded bytes; overridable to test read failures, limits, and resource cleanup. */
    protected byte[] readBytes(FileChannel channel) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        while (channel.read(buffer) != -1) {
            if (output.size() + buffer.position() > MAX_BYTES) {
                throw new IOException("Input exceeds the 10 MiB file limit.");
            }
            output.write(buffer.array(), 0, buffer.position());
            buffer.clear();
        }
        return output.toByteArray();
    }
}
