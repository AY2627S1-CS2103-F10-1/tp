package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestCaseUtil.assertAllCases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.opentest4j.MultipleFailuresError;

public class TestCaseUtilTest {
    @Test
    public void assertAllCases_assertionFailure_runsRemainingCases() {
        List<String> visited = new ArrayList<>();

        assertThrows(MultipleFailuresError.class, () -> assertAllCases(List.of("first", "second"), input -> {
            visited.add(input);
            assertEquals("second", input);
        }));

        assertEquals(List.of("first", "second"), visited);
    }

    @Test
    public void assertAllCases_checkedException_runsRemainingCases() {
        List<String> visited = new ArrayList<>();

        assertThrows(MultipleFailuresError.class, () -> assertAllCases(List.of("first", "second"), input -> {
            visited.add(input);
            throw new IOException("Injected failure");
        }));

        assertEquals(List.of("first", "second"), visited);
    }
}
