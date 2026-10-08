package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class StatusTest {
    @Test
    public void fromString_mixedCase_returnsCanonicalStatus() {
        for (Status status : Status.values()) {
            assertEquals(status, Status.fromString(status.toString().toUpperCase()));
            assertEquals(status.toString(), Status.fromString(status.toString().toLowerCase()).toString());
        }
    }

    @Test
    public void fromString_invalidValue_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Status.MESSAGE_CONSTRAINTS, () -> Status.fromString("Hired"));
        assertThrows(IllegalArgumentException.class, Status.MESSAGE_CONSTRAINTS, () -> Status.fromString(""));
    }
}
