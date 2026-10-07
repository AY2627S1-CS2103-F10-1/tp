package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class StatusTest {

    @Test
    public void fromString_validUppercase_returnsStatus() {
        assertEquals(Status.APPLIED, Status.fromString("APPLIED"));
        assertEquals(Status.INTERVIEWING, Status.fromString("INTERVIEWING"));
        assertEquals(Status.OFFERED, Status.fromString("OFFERED"));
        assertEquals(Status.REJECTED, Status.fromString("REJECTED"));
        assertEquals(Status.HIRED, Status.fromString("HIRED"));
    }

    @Test
    public void fromString_validLowercase_returnsStatus() {
        assertEquals(Status.APPLIED, Status.fromString("applied"));
        assertEquals(Status.HIRED, Status.fromString("hired"));
    }

    @Test
    public void fromString_validMixedCase_returnsStatus() {
        assertEquals(Status.INTERVIEWING, Status.fromString("Interviewing"));
        assertEquals(Status.OFFERED, Status.fromString("oFfErEd"));
    }

    @Test
    public void fromString_validDisplayName_returnsStatus() {
        assertEquals(Status.APPLIED, Status.fromString("Applied"));
        assertEquals(Status.INTERVIEWING, Status.fromString("Interviewing"));
    }

    @Test
    public void fromString_invalidValue_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Status.fromString("UNKNOWN"));
        assertThrows(IllegalArgumentException.class, () -> Status.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> Status.fromString("Pending"));
    }

    @Test
    public void getDisplayName_returnsExpectedCapitalisedName() {
        assertEquals("Applied", Status.APPLIED.getDisplayName());
        assertEquals("Interviewing", Status.INTERVIEWING.getDisplayName());
        assertEquals("Offered", Status.OFFERED.getDisplayName());
        assertEquals("Rejected", Status.REJECTED.getDisplayName());
        assertEquals("Hired", Status.HIRED.getDisplayName());
    }

    @Test
    public void toString_returnsDisplayName() {
        assertEquals("Applied", Status.APPLIED.toString());
        assertEquals("Hired", Status.HIRED.toString());
    }
}
