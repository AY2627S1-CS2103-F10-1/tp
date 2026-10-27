package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LogsCenterTest {
    @TempDir
    public Path directory;

    @Test
    public void addFileHandler_missingParent_keepsHandlersAndReportsFailure() {
        Logger logger = LogsCenter.getLogger(LogsCenterTest.class);
        Logger base = logger.getParent();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        StreamHandler capture = new StreamHandler(output, new SimpleFormatter());
        base.addHandler(capture);
        Handler[] originalHandlers = base.getHandlers();
        try {
            LogsCenter.addFileHandler(directory.resolve("missing/log").toString());
            logger.warning("Logging after failure remains available.");
            capture.flush();

            assertArrayEquals(originalHandlers, base.getHandlers());
            assertTrue(output.toString().contains("Could not create the log file."));
            assertTrue(output.toString().contains("Logging after failure remains available."));
            assertFalse(Files.exists(directory.resolve("missing")));
        } finally {
            base.removeHandler(capture);
            capture.close();
        }
    }

    @Test
    public void initialize_logDestinationIsDirectory_retainsConsoleLogging() throws Exception {
        Files.createDirectory(directory.resolve("addressbook.log.0"));
        String classPath = Path.of(LogsCenter.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator
                + Path.of(LogsCenterTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path output = directory.resolve("console.txt");
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", classPath, LoggingProbe.class.getName())
                .directory(directory.toFile()).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertTrue(process.waitFor(20, TimeUnit.SECONDS));
            String console = Files.readString(output);
            assertEquals(0, process.exitValue(), console);
            assertTrue(console.contains("Could not create the log file."), console);
            assertTrue(console.contains("Probe logging is available."), console);
            assertFalse(console.contains("ExceptionInInitializerError"), console);
            assertTrue(Files.isDirectory(directory.resolve("addressbook.log.0")));
        } finally {
            process.destroyForcibly();
        }
    }

    /** Initializes logging in a fresh JVM before any other application class can initialize it. */
    public static class LoggingProbe {
        public static void main(String[] args) {
            LogsCenter.getLogger(LoggingProbe.class).warning("Probe logging is available.");
        }
    }
}
