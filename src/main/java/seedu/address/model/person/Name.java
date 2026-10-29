package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names should not be blank, and each word must start with a letter or digit. Besides letters, "
                    + "digits and spaces, names can use apostrophes, periods, hyphens and slashes between letters "
                    + "(Anne O'Brien, Kelly-Ann Tan, Ravi s/o Kumar), a closing period (Tan Ah Kow Jr.), a comma "
                    + "after a word (Martin Luther King, Jr.), words in parentheses (Tan Wei Ming (Kelvin)) and "
                    + "an alias after a separate @ (Mohamed Ali @ Ahmad)";

    // Real names use apostrophes, periods, hyphens and slashes (O'Brien, Jr., Kelly-Ann, s/o). Allowing them
    // only between letters or digits, or as one closing period, keeps punctuation-only names invalid.
    private static final String ALPHANUMERIC_RUN = "\\p{Alnum}++";
    private static final String WORD_REGEX = ALPHANUMERIC_RUN + "(?:['./-]" + ALPHANUMERIC_RUN + ")*+\\.?+";
    // Official names can add a romanised or preferred name in parentheses, e.g. "Tan Wei Ming (Chen Wei Ming)".
    private static final String BRACKETED_WORDS_REGEX = "\\(" + WORD_REGEX + "(?: ++" + WORD_REGEX + ")*+\\)";
    // A comma may end a word, e.g. "King, Jr.". Singapore and Malaysian legal names can record an alias after a
    // separate "@", e.g. "Mohamed Ali @ Ahmad".
    private static final String SEPARATOR_REGEX = ",?+ ++(?:@ ++)?+";

    /*
     * The first character of the name must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String VALIDATION_REGEX = WORD_REGEX
            + "(?:" + SEPARATOR_REGEX + "(?:" + WORD_REGEX + "|" + BRACKETED_WORDS_REGEX + "))*+ *+";

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
