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
    public void toString_returnsValue() {
        Remark remark = new Remark("Call after 6pm");

        assertEquals("Call after 6pm", remark.toString());
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Call after 6pm");

        assertTrue(remark.equals(new Remark("Call after 6pm")));
        assertTrue(remark.equals(remark));
        assertFalse(remark.equals(null));
        assertFalse(remark.equals("Call after 6pm"));
        assertFalse(remark.equals(new Remark("Use email")));
    }

    @Test
    public void hashCode_sameValues_returnsSameHashCode() {
        Remark remark = new Remark("Call after 6pm");

        assertEquals(remark.hashCode(), new Remark("Call after 6pm").hashCode());
    }
}
