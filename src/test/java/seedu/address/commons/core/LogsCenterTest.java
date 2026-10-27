package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LogsCenterTest {
    @TempDir
    public Path directory;

    @Test
    public void initialize_logDestinationIsDirectory_retainsConsoleLogging() throws Exception {
        Files.createDirectory(directory.resolve("addressbook.log.0"));
        String classPath = Path.of(LogsCenter.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator
                + Path.of(LogsCenterTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path output = directory.resolve("console.txt");
        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        // Keep coverage collection active in the JVM that exercises static initialization.
        ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .filter(argument -> argument.startsWith("-javaagent:") && argument.contains("jacoco"))
                .forEach(command::add);
        command.addAll(List.of("-cp", classPath, LoggingProbe.class.getName()));
        Process process = new ProcessBuilder(command)
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
