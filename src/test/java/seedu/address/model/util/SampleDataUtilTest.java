package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class SampleDataUtilTest {
    @Test
    public void getSamplePersons_returnsExpectedPeopleWithEmptyRemarks() {
        assertEquals(6, SampleDataUtil.getSamplePersons().length);
        assertFalse(SampleDataUtil.getSampleAddressBook().getPersonList().isEmpty());
        assertEquals("", SampleDataUtil.getSamplePersons()[0].getRemark().value);
    }
}
