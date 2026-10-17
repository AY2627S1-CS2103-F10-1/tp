package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class CsvExportWriterTest {
    private static final String HEADER = "\"name\",\"phone\",\"email\",\"address\",\"status\",\"tags\",\"note\"\r\n";

    @TempDir
    public Path directory;

    @Test
    public void writeCsv_specialCharacters_exactOutput() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().withName("Alice").withPhone("00123").withEmail("alice@example.com")
                .withAddress("=1+1, \"街\"").withTags("java", "backend", "Java")
                .withNote("@literal\r\nnext, \"quoted\"").build());
        StringWriter output = new StringWriter();

        new CsvExportWriter().writeCsv(book, output);

        assertEquals(HEADER + "\"Alice\",\"00123\",\"alice@example.com\",\"=1+1, \"\"街\"\"\","
                + "\"Applied\",\"[\"\"Java\"\",\"\"backend\"\",\"\"java\"\"]\","
                + "\"@literal\r\nnext, \"\"quoted\"\"\"\r\n", output.toString());
    }

    @Test
    public void writeCsv_equalTagSets_deterministicAndRoundTrips() throws Exception {
        Person first = new PersonBuilder().withTags("java", "Java", "backend").build();
        Person second = new PersonBuilder().withTags("backend", "Java", "java").build();
        StringWriter firstOutput = new StringWriter();
        StringWriter secondOutput = new StringWriter();
        AddressBook book = new AddressBook();
        book.addPerson(first);
        new CsvExportWriter().writeCsv(book, firstOutput);
        book.setPersons(java.util.List.of(second));
        new CsvExportWriter().writeCsv(book, secondOutput);
        assertEquals(firstOutput.toString(), secondOutput.toString());

        String encoded = "[\"Java\",\"backend\",\"java\"]";
        String cell = CsvExportWriter.escapeField(encoded);
        assertTrue(firstOutput.toString().contains(cell));
        String decoded = cell.substring(1, cell.length() - 1).replace("\"\"", "\"");
        Set<Tag> tags = Arrays.stream(new ObjectMapper().readValue(decoded, String[].class))
                .map(Tag::new).collect(Collectors.toSet());
        assertEquals(first.getTags(), tags);
        assertEquals(second.getTags(), first.getTags());
    }

    @Test
    public void export_repeatedAndEmpty_replacesWithoutTrailingBytes() throws Exception {
        Path destination = directory.resolve("export.csv");
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().withEmail("alice@example.com").build());
        book.addPerson(new PersonBuilder().withEmail("bob@example.com").build());
        CsvExportWriter exporter = new CsvExportWriter();
        assertEquals(destination, exporter.export(book, destination));
        assertEquals(3, Files.readAllLines(destination).size());
        assertTrue(Files.readString(destination).endsWith("\"[]\",\"\"\r\n"));
        exporter.export(new AddressBook(), destination);
        assertEquals(HEADER, Files.readString(destination));
        try (var entries = Files.list(directory)) {
            assertEquals(1, entries.count());
        }
    }

    @Test
    public void resolveDestination_relativeNormalizedAndUnicode_usesDataParent() throws Exception {
        assertEquals(Path.of("export.csv").toAbsolutePath(),
                CsvExportWriter.resolveDestination(Path.of("data", "addressbook.json")));
        assertEquals(directory.resolve("街 space/export.csv"), CsvExportWriter.resolveDestination(
                directory.resolve("街 space/data/../data/addressbook.json")));
        assertThrows(IOException.class, () -> CsvExportWriter.resolveDestination(directory.getRoot()));
        assertThrows(IOException.class, () ->
                CsvExportWriter.resolveDestination(directory.getRoot().resolve("book.json")));
    }

    @Test
    public void export_directoryOrMissingParent_failsWithoutDeleting() throws Exception {
        Path destination = Files.createDirectory(directory.resolve("export.csv"));
        assertThrows(IOException.class, () -> new CsvExportWriter().export(new AddressBook(), destination));
        assertTrue(Files.isDirectory(destination));
        Path missing = directory.resolve("missing/export.csv");
        assertThrows(IOException.class, () -> new CsvExportWriter().export(new AddressBook(), missing));
        assertFalse(Files.exists(missing.getParent()));
    }

    @Test
    public void export_symbolicLinks_rejectsWithoutFollowing() throws Exception {
        assumeTrue(directory.getFileSystem().supportedFileAttributeViews().contains("posix"));
        Path target = Files.writeString(directory.resolve("target"), "original");
        Path link = Files.createSymbolicLink(directory.resolve("export.csv"), target);
        CsvExportWriter exporter = new CsvExportWriter();
        assertThrows(IOException.class, () -> exporter.export(new AddressBook(), link));
        assertEquals("original", Files.readString(target));
        Files.delete(target);
        assertThrows(IOException.class, () -> exporter.export(new AddressBook(), link));
        assertTrue(Files.isSymbolicLink(link));
        assertFalse(Files.exists(target));
        assertEquals(directory.resolve("export.csv"), CsvExportWriter.resolveDestination(
                Files.createSymbolicLink(directory.resolve("data"), directory.resolve("elsewhere"))
                        .resolve("book.json")));
    }

    @Test
    public void export_permissions_defaultsAndExistingModeRetained() throws Exception {
        assumeTrue(directory.getFileSystem().supportedFileAttributeViews().contains("posix"));
        Path ordinary = Files.createFile(directory.resolve("ordinary"));
        Path destination = directory.resolve("export.csv");
        CsvExportWriter exporter = new CsvExportWriter();
        exporter.export(new AddressBook(), destination);
        assertEquals(Files.getPosixFilePermissions(ordinary), Files.getPosixFilePermissions(destination));
        Set<PosixFilePermission> restricted = Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
        Files.setPosixFilePermissions(destination, restricted);
        exporter.export(new AddressBook(), destination);
        assertEquals(restricted, Files.getPosixFilePermissions(destination));
    }

    @Test
    public void export_openFailure_reportsPermissionReason() {
        Path destination = directory.resolve("export.csv");
        CsvExportWriter exporter = new CsvExportWriter() {
            @Override
            protected Writer openWriter(Path path) throws IOException {
                throw new AccessDeniedException(path.toString());
            }
        };
        assertThrows(IOException.class, "Could not export to " + destination
                + ": The file or folder is not writable (permission denied).", () ->
                exporter.export(new AddressBook(), destination));
        assertFalse(Files.exists(destination));
    }

    @Test
    public void export_writeOrCloseFailure_reportsPossibleDataLoss() {
        for (boolean failOnWrite : new boolean[] {true, false}) {
            Path destination = directory.resolve("export.csv");
            CsvExportWriter exporter = new CsvExportWriter() {
                @Override
                protected Writer openWriter(Path path) throws IOException {
                    Files.writeString(path, "");
                    return new Writer() {
                        @Override
                        public void write(char[] buffer, int offset, int length) throws IOException {
                            if (failOnWrite) {
                                throw new IOException("write failed");
                            }
                        }

                        @Override
                        public void flush() {}

                        @Override
                        public void close() throws IOException {
                            throw new IOException("close failed");
                        }
                    };
                }
            };
            String reason = failOnWrite ? "write failed" : "close failed";
            assertThrows(IOException.class, "Could not export to " + destination + ": " + reason
                    + " The export may be empty or incomplete; the previous export may be lost.", () ->
                    exporter.export(new AddressBook(), destination));
        }
    }
}
