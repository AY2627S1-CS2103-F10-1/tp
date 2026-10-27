package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CsvImportReaderTest {
    @TempDir
    public Path directory;

    @Test
    public void read_fixedLocation_returnsBytesWithoutChangingInput() throws Exception {
        Path input = directory.resolve("import.csv");
        byte[] bytes = new byte[] {1, 2, 3};
        Files.write(input, bytes);

        assertArrayEquals(bytes, reader().read(dataFile()));
        assertArrayEquals(bytes, Files.readAllBytes(input));
        assertArrayEquals(bytes, reader().read(dataFile()));
    }

    @Test
    public void read_nestedDataDirectory_usesJsonAndExportDirectoryWithoutFallback() throws Exception {
        Path data = Files.createDirectories(directory.resolve("app/data"));
        Path configuredFile = data.resolve("HRvest.json");
        Files.writeString(directory.resolve("app/import.csv"), "wrong location");
        String expected = "Could not read " + data.resolve("import.csv") + ": File not found.";
        assertThrows(IOException.class, expected, () -> reader().read(configuredFile));

        byte[] bytes = "correct location".getBytes(StandardCharsets.UTF_8);
        Path source = CsvExportWriter.resolveDestination(configuredFile).resolveSibling("import.csv");
        Files.write(source, bytes);
        assertArrayEquals(bytes, reader().read(configuredFile));
    }

    @Test
    public void read_missingInput_reportsExpectedPathAndCause() {
        String expected = "Could not read " + directory.resolve("import.csv") + ": File not found.";
        assertThrows(IOException.class, expected, () -> reader().read(dataFile()));
    }

    @Test
    public void read_directoryOrSymbolicLink_rejects() throws Exception {
        Path input = directory.resolve("import.csv");
        Files.createDirectory(input);
        assertThrows(IOException.class, () -> reader().read(dataFile()));
        Files.delete(input);
        Path target = Files.writeString(directory.resolve("target.csv"), "content");
        Files.createSymbolicLink(input, target);
        assertThrows(IOException.class, () -> reader().read(dataFile()));
        assertTrue(Files.isSymbolicLink(input));
    }

    @Test
    public void read_fileLimit_acceptsBoundaryRejectsLargerFile() throws Exception {
        Path input = directory.resolve("import.csv");
        byte[] bytes = new byte[CsvImportReader.MAX_BYTES];
        Files.write(input, bytes);
        assertArrayEquals(bytes, reader().read(dataFile()));
        Files.write(input, new byte[] {0}, StandardOpenOption.APPEND);
        assertThrows(IOException.class, () -> reader().read(dataFile()));
    }

    @Test
    public void read_success_closesChannel() throws Exception {
        Files.writeString(directory.resolve("import.csv"), "content");
        TrackingReader trackingReader = new TrackingReader();

        assertArrayEquals("content".getBytes(StandardCharsets.UTF_8), trackingReader.read(dataFile()));
        assertFalse(trackingReader.channel.isOpen());
    }

    @Test
    public void read_laterEditsOrDeletion_doNotChangeBufferedCopy() throws Exception {
        Path input = Files.writeString(directory.resolve("import.csv"), "original content");
        byte[] buffered = reader().read(dataFile());

        Files.writeString(input, "replacement content");
        assertArrayEquals("original content".getBytes(StandardCharsets.UTF_8), buffered);
        Files.delete(input);
        assertArrayEquals("original content".getBytes(StandardCharsets.UTF_8), buffered);
    }

    @Test
    public void read_growthAfterPrecheck_enforcesLimitWhileReading() throws Exception {
        Path input = Files.writeString(directory.resolve("import.csv"), "content");
        TrackingReader growingReader = new TrackingReader() {
            @Override
            protected byte[] readBytes(FileChannel channel) throws IOException {
                Files.write(input, new byte[CsvImportReader.MAX_BYTES + 1]);
                return super.readBytes(channel);
            }
        };
        assertThrows(IOException.class, "Could not read " + input + ": Input exceeds the 10 MiB file limit.", () ->
                growingReader.read(dataFile()));
        assertFalse(growingReader.channel.isOpen());
    }

    @Test
    public void read_readOrPermissionFailure_reportsCauseAndClosesChannel() throws Exception {
        Path input = Files.writeString(directory.resolve("import.csv"), "content");
        List<IOException> failures = List.of(new IOException("Injected read failure"),
                new AccessDeniedException("input"));
        for (IOException failure : failures) {
            TrackingReader failingReader = new TrackingReader();
            failingReader.failure = failure;
            String reason = failure instanceof AccessDeniedException ? "Read permission denied." : failure.getMessage();

            assertThrows(IOException.class, "Could not read " + input + ": " + reason, () ->
                    failingReader.read(dataFile()));
            assertFalse(failingReader.channel.isOpen());
            assertArrayEquals("content".getBytes(StandardCharsets.UTF_8), Files.readAllBytes(input));
        }
    }

    private CsvImportReader reader() {
        return new CsvImportReader();
    }

    private Path dataFile() {
        return directory.resolve("HRvest.json");
    }

    private static class TrackingReader extends CsvImportReader {
        private FileChannel channel;
        private IOException failure;

        @Override
        protected byte[] readBytes(FileChannel channel) throws IOException {
            this.channel = channel;
            if (failure != null) {
                throw failure;
            }
            return super.readBytes(channel);
        }
    }
}
