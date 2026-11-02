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
import java.util.Locale;

/** Reads a bounded copy of the fixed import file without following links. */
public class CsvImportReader {
    public static final int MAX_BYTES = 10 * 1024 * 1024;
    private static final int BUFFER_SIZE = 8192;
    private static final String MESSAGE_SIZE_LIMIT = String.format(Locale.ROOT,
            "Input exceeds the %d MiB file limit.", MAX_BYTES / (1024 * 1024));

    /** Reads the import beside the configured JSON data file. */
    public byte[] read(Path dataFile) throws IOException {
        Path dataDirectory = dataFile.toAbsolutePath().normalize().getParent();
        if (dataDirectory == null) {
            throw new IOException("The import directory could not be determined from the configured data file.");
        }
        Path source = dataDirectory.resolve("import.csv");
        try {
            checkInput(source);
            try (FileChannel channel = FileChannel.open(source, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
                return readBytes(channel);
            }
        } catch (IOException | SecurityException | UnsupportedOperationException e) {
            throw new IOException(String.format("Could not read %s: %s", source, describeFailure(e)), e);
        }
    }

    /** Returns an import-specific failure reason, including a fallback for absent messages. */
    private static String describeFailure(Exception e) {
        if (e instanceof NoSuchFileException) {
            return "File not found.";
        }
        if (e instanceof AccessDeniedException) {
            return "Read permission denied.";
        }
        String reason = e.getMessage();
        return reason == null || reason.isBlank()
                ? "The file could not be safely opened or read on this filesystem." : reason;
    }

    private void checkInput(Path source) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(source, BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
        if (!attributes.isRegularFile()) {
            throw new IOException("Input must be a regular file; links, directories and special files are rejected.");
        }
        if (attributes.size() > MAX_BYTES) {
            throw new IOException(MESSAGE_SIZE_LIMIT);
        }
    }

    /** Reads bounded bytes; overridable to test read failures, limits, and resource cleanup. */
    protected byte[] readBytes(FileChannel channel) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        while (channel.read(buffer) != -1) {
            if (output.size() + buffer.position() > MAX_BYTES) {
                throw new IOException(MESSAGE_SIZE_LIMIT);
            }
            output.write(buffer.array(), 0, buffer.position());
            buffer.clear();
        }
        return output.toByteArray();
    }
}
