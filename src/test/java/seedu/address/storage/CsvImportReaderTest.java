package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;

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
    public void read_contendedLock_rejectsWithoutWaiting() throws Exception {
        Path input = Files.writeString(directory.resolve("import.csv"), "content");
        try (FileChannel channel = FileChannel.open(input, StandardOpenOption.WRITE);
                FileLock lock = channel.lock()) {
            assertTrue(lock.isValid());
            assertThrows(IOException.class, () -> reader().read(dataFile()));
        }
        assertArrayEquals("content".getBytes(StandardCharsets.UTF_8), reader().read(dataFile()));
    }

    @Test
    public void read_detectedChange_rejectsAndReleasesLock() throws Exception {
        Path input = Files.writeString(directory.resolve("import.csv"), "content");
        CsvImportReader changingReader = new CsvImportReader() {
            @Override
            protected byte[] readBytes(FileChannel channel) throws IOException {
                byte[] result = super.readBytes(channel);
                Files.setLastModifiedTime(input, FileTime.fromMillis(1));
                return result;
            }
        };
        assertThrows(IOException.class, () -> changingReader.read(dataFile()));
        assertArrayEquals(Files.readAllBytes(input), reader().read(dataFile()));
    }

    @Test
    public void read_growthAfterPrecheck_enforcesLimitWhileReading() throws Exception {
        Path input = Files.writeString(directory.resolve("import.csv"), "content");
        CsvImportReader growingReader = new CsvImportReader() {
            @Override
            protected byte[] readBytes(FileChannel channel) throws IOException {
                Files.write(input, new byte[CsvImportReader.MAX_BYTES + 1]);
                return super.readBytes(channel);
            }
        };
        assertThrows(IOException.class, () -> growingReader.read(dataFile()));
    }

    @Test
    public void read_replacedFile_rejects() throws Exception {
        Path input = Files.writeString(directory.resolve("import.csv"), "content");
        CsvImportReader replacingReader = new CsvImportReader() {
            @Override
            protected byte[] readBytes(FileChannel channel) throws IOException {
                byte[] result = super.readBytes(channel);
                Files.delete(input);
                Files.writeString(input, "replacement");
                return result;
            }
        };
        assertThrows(IOException.class, () -> replacingReader.read(dataFile()));
    }

    @Test
    public void read_readFailure_releasesLock() throws Exception {
        Path input = Files.writeString(directory.resolve("import.csv"), "content");
        CsvImportReader failingReader = new CsvImportReader() {
            @Override
            protected byte[] readBytes(FileChannel channel) throws IOException {
                throw new IOException("Injected read failure");
            }
        };
        assertThrows(IOException.class, () -> failingReader.read(dataFile()));
        assertArrayEquals(Files.readAllBytes(input), reader().read(dataFile()));
    }

    private CsvImportReader reader() {
        return new CsvImportReader();
    }

    private Path dataFile() {
        return directory.resolve("data/HRvest.json");
    }
}
