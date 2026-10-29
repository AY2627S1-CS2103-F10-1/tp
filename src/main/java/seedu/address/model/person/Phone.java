package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {


    public static final int MAX_LENGTH = 50;
    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers must start with a digit, + or (, contain at least 3 digits, and have at most 50 "
                    + "printable ASCII characters. Put labels and extensions after the number, "
                    + "e.g. +65 6123 4567 (Office) ext 89";
    // Starting with the number rejects free text such as "call me on 9123". The length leaves room for two
    // labelled numbers, e.g. "+65 9123 4567 (HP), +65 6123 4567 (Office)".
    public static final String VALIDATION_REGEX = "[0-9+(][\\x20-\\x7E]{0," + (MAX_LENGTH - 1) + "}";
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone;
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        return test.matches(VALIDATION_REGEX) && test.chars().filter(Character::isDigit).count() >= 3;
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
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
