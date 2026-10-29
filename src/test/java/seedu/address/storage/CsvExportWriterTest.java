package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.role.Role;
import seedu.address.model.role.RoleCatalog;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class CsvExportWriterTest {
    private static final String HEADER = "\"name\",\"phone\",\"email\",\"address\",\"status\","
            + "\"tags\",\"note\",\"roles\"\r\n";
    private static final String NO_ROLES = "[\"No role\"]";

    @TempDir
    public Path directory;

    @Test
    public void writeCsv_specialCharacters_exactOutput() throws Exception {
        Role designer = Role.create("Designer");
        Role developer = Role.create("backend Developer");
        AddressBook book = new AddressBook();
        book.setRoleCatalog(new RoleCatalog(List.of(Role.NO_ROLE, designer, developer)));
        book.addPerson(new PersonBuilder().withName("Alice").withPhone("00123").withEmail("alice@example.com")
                .withAddress("=1+1, \"街\"").withTags("java", "backend", "Java")
                .withNote("@literal\r\nnext, \"quoted\"").withRoles(designer, developer).build());
        StringWriter output = new StringWriter();

        new CsvExportWriter().writeCsv(book, output);

        assertEquals(HEADER + "\"Alice\",\"00123\",\"alice@example.com\",\"=1+1, \"\"街\"\"\","
                + "\"Applied\",\"[\"\"Java\"\",\"\"backend\"\",\"\"java\"\"]\","
                + "\"@literal\r\nnext, \"\"quoted\"\"\",\"[\"\"backend Developer\"\",\"\"Designer\"\"]\"\r\n",
                output.toString());
    }

    @Test
    public void writeCsv_printableAsciiAndLegacyUnicode_parsedValuesPreserved() throws Exception {
        StringBuilder printable = new StringBuilder("Block ");
        for (int codePoint = 0x20; codePoint <= 0x7E; codePoint++) {
            printable.appendCodePoint(codePoint);
        }
        String ascii = printable.toString();
        String legacy = "Zürich\u00A0街\u200B\nNext line";
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().withName("Alice").withPhone("123").withEmail("alice@example.com")
                .withAddress(ascii).withNote(ascii).withTags().build());
        book.addPerson(new PersonBuilder().withName("Bob").withPhone("456").withEmail("bob@example.com")
                .withAddress("Legacy").withNote(legacy).withTags().build());
        StringWriter output = new StringWriter();

        new CsvExportWriter().writeCsv(book, output);

        String records = output.toString().substring(HEADER.length());
        var matcher = Pattern.compile("\"((?:[^\"]|\"\")*)\"(?:,|\\r\\n)").matcher(records);
        List<String> fields = new ArrayList<>();
        int consumed = 0;
        while (matcher.find()) {
            assertEquals(consumed, matcher.start());
            fields.add(matcher.group(1).replace("\"\"", "\""));
            consumed = matcher.end();
        }
        assertEquals(records.length(), consumed);
        assertEquals(List.of("Alice", "123", "alice@example.com", ascii, "Applied", "[]", ascii, NO_ROLES,
                "Bob", "456", "bob@example.com", "Legacy", "Applied", "[]", legacy, NO_ROLES), fields);
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
        assertTrue(Files.readString(destination).endsWith("\"[]\",\"\",\"[\"\"No role\"\"]\"\r\n"));
        exporter.export(new AddressBook(), destination);
        assertEquals(CsvExportWriter.BYTE_ORDER_MARK + HEADER, Files.readString(destination));
        try (var entries = Files.list(directory)) {
            assertEquals(1, entries.count());
        }
    }

    @Test
    public void export_nonAsciiText_startsWithUtf8ByteOrderMark() throws Exception {
        Path destination = directory.resolve("export.csv");
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().withAddress("Zürich 街").withNote("réalisé café").build());

        new CsvExportWriter().export(book, destination);

        byte[] bytes = Files.readAllBytes(destination);
        assertEquals(List.of((byte) 0xEF, (byte) 0xBB, (byte) 0xBF),
                List.of(bytes[0], bytes[1], bytes[2]));
        String content = Files.readString(destination);
        assertEquals(1, content.chars().filter(c -> c == CsvExportWriter.BYTE_ORDER_MARK).count());
        assertTrue(content.startsWith(CsvExportWriter.BYTE_ORDER_MARK + HEADER));
        assertTrue(content.contains("\"Zürich 街\"") && content.contains("\"réalisé café\""));
    }

    @Test
    public void resolveDestination_relativeNormalizedAndUnicode_usesDataDirectory() throws Exception {
        assertEquals(Path.of("data", "export.csv").toAbsolutePath(),
                CsvExportWriter.resolveDestination(Path.of("data", "addressbook.json")));
        assertEquals(directory.resolve("街 space/data/export.csv"), CsvExportWriter.resolveDestination(
                directory.resolve("街 space/data/../data/addressbook.json")));
        assertThrows(IOException.class, () -> CsvExportWriter.resolveDestination(directory.getRoot()));
        assertEquals(directory.getRoot().resolve("export.csv"),
                CsvExportWriter.resolveDestination(directory.getRoot().resolve("book.json")));
    }

    @Test
    public void export_directoryDestination_failsWithoutDeleting() throws Exception {
        Path destination = Files.createDirectory(directory.resolve("export.csv"));
        assertThrows(IOException.class, () -> new CsvExportWriter().export(new AddressBook(), destination));
        assertTrue(Files.isDirectory(destination));
    }

    @Test
    public void export_missingParent_createsParentAndExports() throws Exception {
        Path missing = directory.resolve("missing/nested/export.csv");
        assertEquals(missing, new CsvExportWriter().export(new AddressBook(), missing));
        assertEquals(1, Files.readAllLines(missing).size());
    }

    @Test
    public void export_parentIsFile_failsWithoutDeleting() throws Exception {
        Path parent = Files.writeString(directory.resolve("data"), "original");
        assertThrows(IOException.class, () -> new CsvExportWriter().export(new AddressBook(),
                parent.resolve("export.csv")));
        assertEquals("original", Files.readString(parent));
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
        assertEquals(directory.resolve("data/export.csv"), CsvExportWriter.resolveDestination(
                Files.createSymbolicLink(directory.resolve("data"), directory.resolve("elsewhere"))
                        .resolve("book.json")));
    }

    @Test
    public void export_permissions_defaultsAndExistingModeRetained() throws Exception {
        assumeTrue(directory.getFileSystem().supportedFileAttributeViews().contains("posix"));
        Path ordinary = Files.createTempFile(directory, "permissions-", ".tmp");
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
    public void export_missingFolder_reportsReasonWithoutRepeatingPath() {
        Path destination = directory.resolve("export.csv");
        CsvExportWriter exporter = new CsvExportWriter() {
            @Override
            protected Writer openWriter(Path path) throws IOException {
                throw new NoSuchFileException(path.toString());
            }
        };
        assertThrows(IOException.class, "Could not export to " + destination
                + ": The folder to export to does not exist.", () ->
                exporter.export(new AddressBook(), destination));
    }

    @Test
    public void describeFailure_fileSystemException_usesReasonNotPath() {
        String path = directory.resolve("export.csv").toString();
        String lockedReason = "The process cannot access the file because it is being used by another process";
        assertEquals(lockedReason, CsvExportWriter.describeFailure(
                new FileSystemException(path, null, lockedReason)));
        assertEquals("The file or folder could not be accessed.", CsvExportWriter.describeFailure(
                new FileSystemException(path)));
    }

    @Test
    public void describeFailure_otherExceptions_usesMessageOrFallback() {
        assertEquals("write failed", CsvExportWriter.describeFailure(new IOException("write failed")));
        assertEquals("An unknown error occurred.", CsvExportWriter.describeFailure(new IOException()));
    }

    @Test
    public void export_writeOrCloseFailure_preservesPreviousExportAndCleansTemporaryFile() throws Exception {
        for (boolean failOnWrite : new boolean[] {true, false}) {
            Path destination = directory.resolve("export.csv");
            Files.writeString(destination, "previous successful export");
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
            assertThrows(IOException.class, "Could not export to " + destination + ": " + reason, () ->
                    exporter.export(new AddressBook(), destination));
            assertEquals("previous successful export", Files.readString(destination));
            try (var entries = Files.list(directory)) {
                assertEquals(List.of(destination), entries.toList());
            }
        }
    }

    @Test
    public void export_replacementFailure_preservesPreviousExportAndCleansTemporaryFile() throws Exception {
        Path destination = Files.writeString(directory.resolve("export.csv"), "previous successful export");
        CsvExportWriter exporter = new CsvExportWriter() {
            @Override
            protected void replaceFile(Path temporary, Path target) throws IOException {
                assertTrue(Files.readString(temporary).startsWith(CsvExportWriter.BYTE_ORDER_MARK + HEADER));
                throw new IOException("replacement failed");
            }
        };

        assertThrows(IOException.class, "Could not export to " + destination + ": replacement failed", () ->
                exporter.export(new AddressBook(), destination));

        assertEquals("previous successful export", Files.readString(destination));
        try (var entries = Files.list(directory)) {
            assertEquals(List.of(destination), entries.toList());
        }
    }

    @Test
    public void export_cleanupFailure_preservesOriginalFailureAndPreviousExport() throws Exception {
        Path destination = Files.writeString(directory.resolve("export.csv"), "previous successful export");
        for (Exception failure : List.of(new IOException("replacement failed"),
                new UnsupportedOperationException("atomic replacement unsupported"),
                new SecurityException("replacement denied"))) {
            CsvExportWriter exporter = new CsvExportWriter() {
                @Override
                protected void replaceFile(Path temporary, Path target) throws IOException {
                    Files.delete(temporary);
                    Files.createDirectory(temporary);
                    Files.writeString(temporary.resolve("partial"), "partial data");
                    if (failure instanceof IOException ioException) {
                        throw ioException;
                    }
                    throw (RuntimeException) failure;
                }
            };

            assertThrows(IOException.class, "Could not export to " + destination + ": " + failure.getMessage(), () -> {
                try {
                    exporter.export(new AddressBook(), destination);
                } catch (IOException e) {
                    assertSame(failure, e.getCause());
                    assertEquals(1, failure.getSuppressed().length);
                    assertTrue(failure.getSuppressed()[0] instanceof DirectoryNotEmptyException);
                    throw e;
                }
            });

            assertEquals("previous successful export", Files.readString(destination));
        }
    }

    @Test
    public void export_writeFailureWithoutPreviousExport_leavesNoOutput() throws Exception {
        Path destination = directory.resolve("export.csv");
        CsvExportWriter exporter = new CsvExportWriter() {
            @Override
            protected Writer openWriter(Path temporary) throws IOException {
                throw new IOException("write failed");
            }
        };

        assertThrows(IOException.class, () -> exporter.export(new AddressBook(), destination));

        assertFalse(Files.exists(destination));
        try (var entries = Files.list(directory)) {
            assertEquals(0, entries.count());
        }
    }
}
