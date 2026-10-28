package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    public static final String MESSAGE_CONSTRAINTS = "Emails should be of the format local-part@domain "
            + "and adhere to the following constraints:\n"
            + "1. The local-part may contain letters, digits, and !#$%&'*+/=?^_`{|}~- separated by periods. "
            + "It must not start or end with a period or contain consecutive periods.\n"
            + "2. The local-part is followed by an '@' and then a domain name. The domain name is made up of domain "
            + "labels separated by periods.\n"
            + "The domain name must:\n"
            + "    - end with a domain label at least 2 characters long\n"
            + "    - have each domain label start and end with alphanumeric characters\n"
            + "    - contain only alphanumeric characters and internal hyphens, including consecutive hyphens.";
    private static final String ATOM_CHARACTERS = "a-zA-Z0-9!#$%&'*+/=?^_`{|}~-";
    private static final String LOCAL_ATOM_REGEX = "[" + ATOM_CHARACTERS + "]++";
    private static final String LOCAL_PART_REGEX = "^" + LOCAL_ATOM_REGEX + "(?:\\." + LOCAL_ATOM_REGEX + ")*+";
    // Label interiors may contain repeated hyphens; the first and last characters must be alphanumeric.
    private static final String DOMAIN_PART_REGEX = "[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?";
    private static final String DOMAIN_LAST_PART_REGEX = "(?=[^.]{2})" + DOMAIN_PART_REGEX + "$";
    private static final String DOMAIN_REGEX = "(" + DOMAIN_PART_REGEX + "\\.)*+" + DOMAIN_LAST_PART_REGEX;
    public static final String VALIDATION_REGEX = LOCAL_PART_REGEX + "@" + DOMAIN_REGEX;

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        checkArgument(isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = email;
    }

    /**
     * Returns true if a given string is a valid email.
     */
    public static boolean isValidEmail(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
