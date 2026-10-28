package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("91")); // less than 3 numbers
        assertFalse(Phone.isValidPhone("phone")); // non-numeric

        // valid phone numbers
        assertTrue(Phone.isValidPhone("911")); // exactly 3 numbers
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("9312 1534")); // formatted number
        assertTrue(Phone.isValidPhone("124293842033123")); // long phone numbers
    }

    @Test
    public void constructor_formattedNumbers_preservesFormatting() {
        for (String value : new String[] {"+65 9123 4567", "9123-4567", "+1-202-555-0123", "0 0-7",
            "(65) 6123 4567", "1234 5678 (HP) 1111-3333 (Office)", "6123 4567 ext. 123", "Office: 911",
            "123456789012345678901234567890"}) {
            assertTrue(Phone.isValidPhone(value), value);
            assertEquals(value, new Phone(value).value);
        }
    }

    @Test
    public void isValidPhone_missingDigitsOrUnsupportedCharacters_returnsFalse() {
        for (String value : new String[] {"+", "+1-2", "1 - 2", "Office", "Office: 12", "---",
            "123\t456", "123\n456", "123\u00A0", "123\u200B", "\uFF11\uFF12\uFF13", "123\u007F"}) {
            assertFalse(Phone.isValidPhone(value), value);
        }
    }

    @Test
    public void equals() {
        Phone phone = new Phone("999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("995")));
    }
}
