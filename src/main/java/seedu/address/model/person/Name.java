package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names should only contain letters, digits, spaces, apostrophes ('), periods (.), hyphens (-) "
                    + "and slashes (/), and should not be blank. Each word must start with a letter or digit, "
                    + "and each of these punctuation marks must be followed by a letter or digit, except that "
                    + "a word may end with one period. A separate @ may introduce an alias. E.g. Anne O'Brien, "
                    + "Kelly-Ann Tan, Tan Ah Kow Jr., Ravi s/o Kumar or Mohamed Ali @ Ahmad";

    // Real names use apostrophes, periods, hyphens and slashes (O'Brien, Jr., Kelly-Ann, s/o). Allowing them
    // only between letters or digits, or as one closing period, keeps punctuation-only names invalid.
    private static final String ALPHANUMERIC_RUN = "\\p{Alnum}++";
    private static final String WORD_REGEX = ALPHANUMERIC_RUN + "(?:['./-]" + ALPHANUMERIC_RUN + ")*+\\.?+";
    // Singapore and Malaysian legal names can record an alias after a separate "@", e.g. "Mohamed Ali @ Ahmad".
    private static final String ALIAS_MARKER = "(?:@ ++)?+";

    /*
     * The first character of the name must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String VALIDATION_REGEX = WORD_REGEX + "(?: ++" + ALIAS_MARKER + WORD_REGEX + ")*+ *+";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.matches(VALIDATION_REGEX);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
