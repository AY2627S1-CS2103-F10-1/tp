package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @TestFactory
    public Stream<DynamicTest> constructor_technicalSkills_preservesNames() {
        return Stream.of("C++", "C#", ".NET", "Node.js", "machine learning", "C/C++", "data  science",
                "quoted \"skill\"", "tools\\sdk", "comma,value")
                .map(name -> dynamicTest(name, () -> {
                    assertTrue(Tag.isValidTagName(name));
                    assertEquals(name, new Tag(name).tagName);
                }));
    }

    @TestFactory
    public Stream<DynamicTest> constructor_blankUntrimmedOrUnsupportedCharacters_rejects() {
        return Stream.of("", "   ", " C++", "C++ ", "line\nbreak", "tab\tname", "\u0000", "\u007f", "caf\u00e9")
                .map(name -> dynamicTest("Invalid tag: " + name.replace("\n", "\\n").replace("\t", "\\t"), () -> {
                    assertFalse(Tag.isValidTagName(name));
                    assertThrows(IllegalArgumentException.class, Tag.MESSAGE_CONSTRAINTS, () -> new Tag(name));
                }));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
    }

}
