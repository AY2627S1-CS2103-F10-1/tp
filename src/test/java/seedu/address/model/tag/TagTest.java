package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        for (String invalid : new String[] {"", " ", "has space", "comma,sep", "paren(s)"}) {
            assertThrows(IllegalArgumentException.class, () -> new Tag(invalid));
        }
    }

    @Test
    public void constructor_technicalSkillNames_stored() {
        for (String name : new String[] {"C++", "C#", ".NET", "Node.js", "machine-learning", "go/rust"}) {
            assertEquals(name, new Tag(name).tagName);
        }
    }

    @Test
    public void isValidTagName_null_throws() {
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
    }

    @Test
    public void isValidTagName_valuesByPartition_returnsExpected() {
        // valid
        assertTrue(Tag.isValidTagName("backend"));
        assertTrue(Tag.isValidTagName("Java17"));
        assertTrue(Tag.isValidTagName("C++"));
        assertTrue(Tag.isValidTagName("C#"));
        assertTrue(Tag.isValidTagName(".NET"));
        assertTrue(Tag.isValidTagName("Node.js"));
        assertTrue(Tag.isValidTagName("front-end"));
        // invalid
        assertFalse(Tag.isValidTagName(""));
        assertFalse(Tag.isValidTagName(" "));
        assertFalse(Tag.isValidTagName("has space"));
        assertFalse(Tag.isValidTagName("tab\there"));
        assertFalse(Tag.isValidTagName("new\nline"));
    }

}
