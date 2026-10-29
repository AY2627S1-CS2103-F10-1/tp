package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {


    // Leaves room for two labelled numbers, e.g. "+65 9123 4567 (HP), +65 6123 4567 (Office)".
    public static final int MAX_INPUT_LENGTH = 50;
    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers must contain at least 3 ASCII digits and only printable ASCII characters. "
                    + "Formatting, extensions and contact labels are allowed";
    public static final String MESSAGE_INPUT_CONSTRAINTS =
            "Phone numbers must begin with the number itself, such as 9123, +65, (65) or (+65), contain at least "
                    + "3 digits, and have at most 50 printable ASCII characters. Put labels and extensions after "
                    + "the number, e.g. +65 6123 4567 (Office) ext 89";
    public static final String VALIDATION_REGEX = "[\\x20-\\x7E]+";
    // A leading + or ( must be followed by a digit, so that "+Office: 911" is rejected like "Office: 911".
    private static final String INPUT_START_REGEX = "(?:\\+\\(?|\\(\\+?)?[0-9].*";
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

    /**
     * Returns true if a given string is a valid phone number for new command or CSV input.
     * New input must also start with the number, which rejects free text such as "call me on 9123", and have at
     * most {@link #MAX_INPUT_LENGTH} characters. Saved phones are checked only by {@link #isValidPhone(String)},
     * so that records accepted by earlier versions still load.
     */
    public static boolean isValidPhoneInput(String test) {
        return isValidPhone(test) && test.length() <= MAX_INPUT_LENGTH && test.matches(INPUT_START_REGEX);
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
