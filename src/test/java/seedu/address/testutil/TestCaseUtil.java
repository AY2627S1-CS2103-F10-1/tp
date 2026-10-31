package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.jupiter.api.function.ThrowingConsumer;

/** Runs table-driven test cases with a diagnostic for each input. */
public final class TestCaseUtil {
    private static final int MIN_PRINTABLE_ASCII = 0x20;
    private static final int MAX_PRINTABLE_ASCII = 0x7e;

    private TestCaseUtil() {
    }

    /**
     * Runs every case and groups failures by input, escaping non-ASCII and control characters.
     * Cases run sequentially; callers must isolate state when a scenario mutates it.
     */
    public static <T> void assertAllCases(Collection<T> inputs, ThrowingConsumer<T> assertion) {
        assertAll(inputs.stream().map(input -> () ->
                assertAll("Input: [" + describeInput(input) + "]", () -> assertion.accept(input))));
    }

    /** Escapes invisible input characters so failures distinguish whitespace and Unicode cases. */
    private static String describeInput(Object input) {
        return String.valueOf(input).codePoints()
                .mapToObj(codePoint -> codePoint >= MIN_PRINTABLE_ASCII && codePoint <= MAX_PRINTABLE_ASCII
                        ? Character.toString(codePoint) : String.format("\\u{%04X}", codePoint))
                .collect(Collectors.joining());
    }
}
