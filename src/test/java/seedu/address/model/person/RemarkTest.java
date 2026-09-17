package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_emptyString_allowed() {
        Remark empty = new Remark("");
        assertEquals("", empty.value);
    }

    @Test
    public void equals_sameValue_returnsTrue() {
        Remark a = new Remark("hello");
        Remark b = new Remark("hello");
        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void equals_differentValue_returnsFalse() {
        Remark a = new Remark("hello");
        Remark b = new Remark("world");
        assertNotEquals(a, b);
    }

    @Test
    public void equals_sameInstance_returnsTrue() {
        Remark a = new Remark("hello");
        assertTrue(a.equals(a));
    }

    @Test
    public void equals_null_returnsFalse() {
        Remark a = new Remark("hello");
        assertFalse(a.equals(null));
    }

    @Test
    public void equals_differentType_returnsFalse() {
        Remark a = new Remark("hello");
        assertFalse(a.equals("hello"));
    }

    @Test
    public void toString_returnsValue() {
        Remark a = new Remark("Likes to swim.");
        assertEquals("Likes to swim.", a.toString());
    }
}
