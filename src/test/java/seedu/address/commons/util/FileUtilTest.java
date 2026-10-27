package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {
    @TempDir
    Path temporaryFolder;

    @Test
    public void readFromFile_leadingByteOrderMark_returnsTextWithoutMark() throws Exception {
        Path file = temporaryFolder.resolve("bom.json");
        Files.write(file, new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '{', '}'});

        assertEquals("{}", FileUtil.readFromFile(file));
    }

    @Test
    public void readFromFile_byteOrderMarkInsideText_keepsMark() throws Exception {
        Path file = temporaryFolder.resolve("text.txt");
        FileUtil.writeToFile(file, "a\uFEFFb");

        assertEquals("a\uFEFFb", FileUtil.readFromFile(file));
    }
}
