package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
    }

    @Test
    public void isValidName_apostrophesAndPeriodsWithinWords_returnsTrue() {
        for (String value : new String[] {"Anne O'Brien", "D'Angelo", "Ng'ang'a Tan", "Tan Ah Kow Jr.",
            "J. Tan", "J.R.R. Tolkien", "St. John Lee", "Mary  O'Neil", "O'Brien "}) {
            assertTrue(Name.isValidName(value), value);
        }
    }

    @Test
    public void isValidName_misplacedOrRepeatedPunctuation_returnsFalse() {
        for (String value : new String[] {"'", ".", "'Anne", ".Anne", "Anne O'", "Anne O' Brien", "Anne ..",
            "Jr..", "O''Brien", "A.'B", "J. . Tan", "Anne-Marie", "Ravi s/o Kumar", "Anne O’Brien"}) {
            assertFalse(Name.isValidName(value), value);
        }
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
