package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class EmailTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Email(null));
    }

    @Test
    public void constructor_invalidEmail_throwsIllegalArgumentException() {
        String invalidEmail = "";
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @Test
    public void isValidEmail() {
        // null email
        assertThrows(NullPointerException.class, () -> Email.isValidEmail(null));

        // blank email
        assertFalse(Email.isValidEmail("")); // empty string
        assertFalse(Email.isValidEmail(" ")); // spaces only

        // missing parts
        assertFalse(Email.isValidEmail("@example.com")); // missing local part
        assertFalse(Email.isValidEmail("peterjackexample.com")); // missing '@' symbol
        assertFalse(Email.isValidEmail("peterjack@")); // missing domain name

        // invalid parts
        assertFalse(Email.isValidEmail("peterjack@-")); // invalid domain name
        assertFalse(Email.isValidEmail("peterjack@exam_ple.com")); // underscore in domain name
        assertFalse(Email.isValidEmail("peter jack@example.com")); // spaces in local part
        assertFalse(Email.isValidEmail("peterjack@exam ple.com")); // spaces in domain name
        assertFalse(Email.isValidEmail(" peterjack@example.com")); // leading space
        assertFalse(Email.isValidEmail("peterjack@example.com ")); // trailing space
        assertFalse(Email.isValidEmail("peterjack@@example.com")); // double '@' symbol
        assertFalse(Email.isValidEmail("peter@jack@example.com")); // '@' symbol in local part
        assertTrue(Email.isValidEmail("-peterjack@example.com")); // an atom may start with punctuation
        assertTrue(Email.isValidEmail("peterjack-@example.com")); // an atom may end with punctuation
        assertFalse(Email.isValidEmail("peter..jack@example.com")); // local part has two consecutive periods
        assertFalse(Email.isValidEmail("peterjack@example@com")); // '@' symbol in domain name
        assertFalse(Email.isValidEmail("peterjack@.example.com")); // domain name starts with a period
        assertFalse(Email.isValidEmail("peterjack@example.com.")); // domain name ends with a period
        assertFalse(Email.isValidEmail("peterjack@-example.com")); // domain name starts with a hyphen
        assertFalse(Email.isValidEmail("peterjack@example.com-")); // domain name ends with a hyphen
        assertFalse(Email.isValidEmail("peterjack@example.c")); // top level domain has less than two chars

        // valid email
        assertTrue(Email.isValidEmail("PeterJack_1190@example.com")); // underscore in local part
        assertTrue(Email.isValidEmail("PeterJack.1190@example.com")); // period in local part
        assertTrue(Email.isValidEmail("PeterJack+1190@example.com")); // '+' symbol in local part
        assertTrue(Email.isValidEmail("PeterJack-1190@example.com")); // hyphen in local part
        assertTrue(Email.isValidEmail("a@bc")); // minimal
        assertTrue(Email.isValidEmail("test@localhost")); // alphabets only
        assertTrue(Email.isValidEmail("123@145")); // numeric local part and domain name
        assertTrue(Email.isValidEmail("a1+be.d@example1.com")); // mixture of alphanumeric and special characters
        assertTrue(Email.isValidEmail("peter_jack@very-very-very-long-example.com")); // long domain name
        assertTrue(Email.isValidEmail("if.you.dream.it_you.can.do.it@example.com")); // long local part
        assertTrue(Email.isValidEmail("e1234567@u.nus.edu")); // more than one period in domain
    }

    @Test
    public void isValidEmail_longSegmentedValues_doesNotOverflowStack() {
        assertTrue(Email.isValidEmail("a.".repeat(10_000) + "a@example.com"));
        assertTrue(Email.isValidEmail("a@" + "a.".repeat(10_000) + "com"));
        assertTrue(Email.isValidEmail("a@" + "ab-".repeat(10_000) + "ab"));
        assertTrue(Email.isValidEmail("a@" + "a-".repeat(10_000) + "a"));
        assertFalse(Email.isValidEmail("a@" + "a-".repeat(10_000)));
        assertFalse(Email.isValidEmail("a@" + "a-".repeat(10_000) + "!"));
    }

    @Test
    public void isValidEmail_shortCombinations_acceptsPreviouslyValidEmails() {
        String alphanumeric = "[^\\W_]+";
        String domainPart = alphanumeric + "(-" + alphanumeric + ")*";
        Pattern previous = Pattern.compile("^" + alphanumeric + "([+_.-]" + alphanumeric + ")*@"
                + "(" + domainPart + "\\.)*(" + domainPart + "){2,}$");
        String alphabet = "ab-.";
        for (int value = 0; value < 4096; value++) {
            StringBuilder domain = new StringBuilder();
            int remaining = value;
            for (int position = 0; position < 6; position++) {
                domain.append(alphabet.charAt(remaining % alphabet.length()));
                remaining /= alphabet.length();
            }
            for (String local : List.of("a", "a.b", "a+b", "a_b", "a-b", "a..b", "-a")) {
                String email = local + "@" + domain;
                if (previous.matcher(email).matches()) {
                    assertTrue(Email.isValidEmail(email), email);
                }
            }
        }
    }

    @Test
    public void isValidEmail_dotAtomsAndInternalHyphens_acceptsValidAddresses() {
        for (String email : List.of("anne.o'brien@example.com", "alice!team@example.com",
                "!#$%&'*+/=?^_`{|}~-@example.com", "a+_-b@example.com", "a@xn--bcher-kva.de",
                "a@recruit--team.example.com", "a@a-b", "a@a--b")) {
            assertTrue(Email.isValidEmail(email), email);
        }
        for (String email : List.of(".alice@example.com", "alice.@example.com", "alice..team@example.com",
                "alice@a..com", "alice@a.-bc", "alice@a.bc-", "alice@--ab.com", "alice@a b.com",
                "alice(office)@example.com", "\"alice\"@example.com")) {
            assertFalse(Email.isValidEmail(email), email);
        }
    }

    @Test
    public void equals() {
        Email email = new Email("valid@email");

        // same values -> returns true
        assertTrue(email.equals(new Email("valid@email")));

        // same object -> returns true
        assertTrue(email.equals(email));

        // null -> returns false
        assertFalse(email.equals(null));

        // different types -> returns false
        assertFalse(email.equals(5.0f));

        // different values -> returns false
        assertFalse(email.equals(new Email("other.valid@email")));
    }
}
