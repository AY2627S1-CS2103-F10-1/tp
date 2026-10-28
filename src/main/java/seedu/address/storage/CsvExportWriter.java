package seedu.address.storage;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.core.CandidateCsvFormat.HEADERS;

import java.io.IOException;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.commons.util.FileUtil;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/** Writes raw candidate data as UTF-8 CSV with a byte order mark using atomic replacement. */
public class CsvExportWriter {
    /** Lets spreadsheet programs such as Excel detect UTF-8, so that non-ASCII text is not garbled. */
    static final char BYTE_ORDER_MARK = '\uFEFF';

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /** Returns the export path beside the configured JSON data file, without resolving symbolic links. */
    public static Path resolveDestination(Path dataFile) throws IOException {
        Path dataDirectory = requireNonNull(dataFile).toAbsolutePath().normalize().getParent();
        if (dataDirectory == null) {
            throw new IOException("The export directory could not be determined from " + dataFile + ".");
        }
        return dataDirectory.resolve("export.csv");
    }

    /** Exports all candidates and returns only after closing the destination successfully. */
    public Path export(ReadOnlyAddressBook addressBook, Path destination) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(destination);
        try {
            // The data folder may not exist yet, e.g. when sample data has not been saved.
            FileUtil.createParentDirsOfFile(destination);
            checkDestination(destination);
            writeReplacement(addressBook, destination);
            return destination;
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            String reason = describeFailure(e);
            throw new IOException("Could not export to " + destination + ": " + reason, e);
        }
    }

    /** Keeps the previous export intact until the replacement has been written and closed successfully. */
    private void writeReplacement(ReadOnlyAddressBook addressBook, Path destination) throws IOException {
        Path temporary = Files.createTempFile(destination.toAbsolutePath().getParent(),
                "HRvest-export-", ".tmp");
        try {
            JsonAddressBookStorage.copyExistingAcl(
                    Files.getFileAttributeView(destination, AclFileAttributeView.class,
                            LinkOption.NOFOLLOW_LINKS),
                    Files.getFileAttributeView(temporary, AclFileAttributeView.class));
            JsonAddressBookStorage.copyExistingPosixPermissions(destination, temporary);
            try (Writer writer = openWriter(temporary)) {
                writer.write(BYTE_ORDER_MARK);
                writeCsv(addressBook, writer);
            }
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            checkDestination(destination);
            replaceFile(temporary, destination);
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException | RuntimeException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw e;
        }
    }

    /** Replaces the completed export atomically, failing safely if the filesystem cannot do so. */
    protected void replaceFile(Path temporary, Path destination) throws IOException {
        Files.move(temporary, destination,
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Returns a user-facing reason for {@code e}.
     * File system exceptions are described by their reason, as their message only repeats the path.
     */
    static String describeFailure(Exception e) {
        if (e instanceof AccessDeniedException) {
            return "The file or folder is not writable (permission denied).";
        }
        if (e instanceof NoSuchFileException) {
            return "The folder to export to does not exist.";
        }
        if (e instanceof FileSystemException fileSystemException) {
            String reason = fileSystemException.getReason();
            return reason == null ? "The file or folder could not be accessed." : reason;
        }
        return e.getMessage() == null ? "An unknown error occurred." : e.getMessage();
    }

    /** Rejects existing non-regular entries; opening also refuses symbolic links to limit replacement races. */
    private void checkDestination(Path destination) throws IOException {
        try {
            BasicFileAttributes attributes = Files.readAttributes(destination, BasicFileAttributes.class,
                    LinkOption.NOFOLLOW_LINKS);
            if (!attributes.isRegularFile()) {
                throw new IOException("The destination is not a regular file (directories and links are not allowed).");
            }
        } catch (NoSuchFileException e) {
            // CREATE handles an absent destination.
        }
    }

    /** Opens the temporary export without following links. Overridable for I/O failure tests. */
    protected Writer openWriter(Path destination) throws IOException {
        return Files.newBufferedWriter(destination, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE,
                LinkOption.NOFOLLOW_LINKS);
    }

    /** Writes seven quoted fields per record, preserving literal text and using CRLF record separators. */
    void writeCsv(ReadOnlyAddressBook addressBook, Writer writer) throws IOException {
        writeRecord(writer, HEADERS);
        for (Person person : addressBook.getPersonList()) {
            String tags = JSON_MAPPER.writeValueAsString(person.getTags().stream()
                    .map(tag -> tag.tagName).sorted().toList());
            writeRecord(writer, List.of(person.getName().fullName, person.getPhone().value,
                    person.getEmail().value, person.getAddress().value, person.getStatus().toString(), tags,
                    person.getNote().map(note -> note.value).orElse("")));
        }
    }

    private void writeRecord(Writer writer, List<String> fields) throws IOException {
        writer.write(fields.stream().map(CsvExportWriter::escapeField).collect(Collectors.joining(",")));
        writer.write("\r\n");
    }

    /** Quotes a field and doubles embedded quotes, leaving all other characters unchanged. */
    static String escapeField(String field) {
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }
}
