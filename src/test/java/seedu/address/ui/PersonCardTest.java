package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PersonCardTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void expandedView_longFieldsAndMissingNote_displaysCompleteRecord() throws Exception {
        List<String> command = new ArrayList<>();
        if (System.getProperty("os.name").startsWith("Linux") && System.getenv("DISPLAY") == null) {
            command.add("xvfb-run");
            command.add("-a");
        }
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        // Preserve coverage instrumentation in the isolated JavaFX process.
        ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .filter(argument -> argument.startsWith("-javaagent:") && argument.contains("jacoco"))
                .forEach(command::add);
        command.add("-cp");
        command.add(getTestClasspath());
        command.add(ExpandUiTestApp.class.getName());
        Path output = temporaryFolder.resolve("javafx-output.txt");

        Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
        }

        assertTrue(finished, "JavaFX check timed out: " + Files.readString(output));
        assertEquals(0, process.exitValue(), Files.readString(output));
    }

    /** Returns the test classpath, including Gradle's isolated test class loader. */
    private String getTestClasspath() {
        ClassLoader loader = getClass().getClassLoader();
        if (loader instanceof URLClassLoader urlLoader) {
            return Stream.of(urlLoader.getURLs()).map(url -> {
                try {
                    return Path.of(url.toURI()).toString();
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Invalid test classpath URL", e);
                }
            }).collect(Collectors.joining(System.getProperty("path.separator")));
        }
        return System.getProperty("java.class.path");
    }
}
