package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void valueMethods_workCorrectly() {
        Remark remark = new Remark("hello");
        assertEquals("hello", remark.value);
        assertEquals("hello", remark.toString());
        assertTrue(remark.equals(remark));
        assertTrue(remark.equals(new Remark("hello")));
        assertFalse(remark.equals(new Remark("other")));
        assertFalse(remark.equals(null));
        assertFalse(remark.equals("hello"));
        assertEquals(new Remark("hello").hashCode(), remark.hashCode());
    }
}
