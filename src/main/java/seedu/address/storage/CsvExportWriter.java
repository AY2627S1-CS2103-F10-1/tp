package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/** Writes raw candidate data as UTF-8 CSV, directly to the fixed export destination. */
public class CsvExportWriter {
    private static final List<String> HEADERS = List.of("name", "phone", "email", "address", "status", "tags", "note");
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /** Returns the export path beside the configured data directory, without resolving symbolic links. */
    public static Path resolveDestination(Path dataFile) throws IOException {
        Path dataDirectory = requireNonNull(dataFile).toAbsolutePath().normalize().getParent();
        if (dataDirectory == null || dataDirectory.getParent() == null) {
            throw new IOException("The export directory could not be determined from " + dataFile + ".");
        }
        return dataDirectory.getParent().resolve("export.csv");
    }

    /** Exports all candidates and returns only after closing the destination successfully. */
    public Path export(ReadOnlyAddressBook addressBook, Path destination) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(destination);
        boolean opened = false;
        try {
            checkDestination(destination);
            try (Writer writer = openWriter(destination)) {
                opened = true;
                writeCsv(addressBook, writer);
            }
            return destination;
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            String reason = e instanceof AccessDeniedException
                    ? "The file or folder is not writable (permission denied)." : e.getMessage();
            String warning = opened ? " The export may be empty or incomplete; the previous export may be lost." : "";
            throw new IOException("Could not export to " + destination + ": " + reason + warning, e);
        }
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
            // CREATE handles an absent destination; do not create missing parent directories.
        }
    }

    /** Opens directly without custom permissions or a link-following fallback. Overridable for I/O failure tests. */
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
