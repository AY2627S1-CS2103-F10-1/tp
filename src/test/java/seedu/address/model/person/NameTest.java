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
    public void isValidName_punctuationWithinWords_returnsTrue() {
        for (String value : new String[] {"Anne O'Brien", "D'Angelo", "Ng'ang'a Tan", "Tan Ah Kow Jr.",
            "J. Tan", "J.R.R. Tolkien", "St. John Lee", "Mary  O'Neil", "O'Brien ", "Kelly-Ann Tan",
            "Kim Min-jun", "Jean-Luc O'Neil-Smith", "Ahmad bin Ali"}) {
            assertTrue(Name.isValidName(value), value);
        }
    }

    @Test
    public void isValidName_lineageAndGenderMarkers_returnsTrue() {
        for (String value : new String[] {"Ravi s/o Kumar", "Priya d/o Raju", "Arul a/l Muthu", "Siti a/p Ahmad",
            "Lim Ah Seng l/p", "RAVI S/O KUMAR"}) {
            assertTrue(Name.isValidName(value), value);
        }
    }

    @Test
    public void isValidName_aliasAfterSeparateAt_returnsTrue() {
        assertTrue(Name.isValidName("Mohamed Ali @ Ahmad"));
        assertTrue(Name.isValidName("Tan Ah Kow @ Tan Kow Ming"));
    }

    @Test
    public void isValidName_officialNameConventions_returnsTrue() {
        for (String value : new String[] {"JOEY CHAN HSIAO AN (CHAN XIAO AN) @ JOEY CHAN (CHEN XIAO AN) MRS CHAN",
            "Tan Wei Ming (Kelvin)", "Martin Luther King, Jr.", "Lim, Mei Ling", "Tan (Kelvin), Jr.",
            "Ludwig van der Waals", "Maria de la Cruz", "Siti Nurhaliza binte Tarudin", "Sukarno", "A",
            "Henry VIII", "Mary O'Neil-Smith Jr."}) {
            assertTrue(Name.isValidName(value), value);
        }
    }

    @Test
    public void isValidName_malformedCommasOrParentheses_returnsFalse() {
        for (String value : new String[] {"Tan,", "Tan , Wei", "Tan,Wei", ", Tan", "Tan ,", "(Kelvin)",
            "(Kelvin) Tan", "Tan ()", "Tan (Kelvin", "Tan Kelvin)", "Tan ((Kelvin))", "Tan (Kelvin) )",
            "Tan ( Kelvin )", "Tan,, Wei"}) {
            assertFalse(Name.isValidName(value), value);
        }
    }

    @Test
    public void isValidName_misplacedOrRepeatedPunctuation_returnsFalse() {
        for (String value : new String[] {"'", ".", "'Anne", ".Anne", "Anne O'", "Anne O' Brien", "Anne ..",
            "Jr..", "O''Brien", "A.'B", "J. . Tan", "Anne O’Brien", "/", "/Ravi", "Ravi/", "Ravi s//o Kumar",
            "Ravi s/ o", "-", "Kelly-", "-Ann", "Kelly--Ann", "Kelly - Ann", "@", "@ Ahmad", "Ali @",
            "Ali @@ Ahmad", "Ali@Ahmad", "Ali @ @ Ahmad"}) {
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
