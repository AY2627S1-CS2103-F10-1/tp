package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names should only contain letters, digits, spaces, apostrophes ('), periods (.) and slashes (/), "
                    + "and should not be blank. Each word must start with a letter or digit. "
                    + "An apostrophe, period or slash must be followed by a letter or digit, "
                    + "except that a word may end with one period, e.g. Anne O'Brien, Tan Ah Kow Jr. "
                    + "or Ravi s/o Kumar";

    // Real names use apostrophes, periods and slashes (O'Brien, Jr., s/o). Allowing them only between
    // letters or digits, or as one closing period, keeps punctuation-only and repeated-punctuation names invalid.
    private static final String ALPHANUMERIC_RUN = "\\p{Alnum}++";
    private static final String WORD_REGEX = ALPHANUMERIC_RUN + "(?:['./]" + ALPHANUMERIC_RUN + ")*+\\.?+";

    /*
     * The first character of the name must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String VALIDATION_REGEX = WORD_REGEX + "(?: ++" + WORD_REGEX + ")*+ *+";

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
