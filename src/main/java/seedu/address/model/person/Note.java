package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a candidate's single note. Instances are immutable and contain non-blank text.
 */
public class Note {

    public static final int MAX_LENGTH = 500;
    public static final String MESSAGE_BLANK = "Note cannot be blank.";
    public static final String MESSAGE_TOO_LONG = "Note cannot exceed 500 characters.";

    public final String value;

    /**
     * Constructs a note, validating and storing the supplied text without modification.
     *
     * @param note Non-blank text containing at most 500 Unicode code points.
     */
    public Note(String note) {
        requireNonNull(note);
        checkArgument(!note.isBlank(), MESSAGE_BLANK);
        checkArgument(isValidNote(note), MESSAGE_TOO_LONG);
        value = note;
    }

    /**
     * Returns whether the supplied text is non-blank and contains at most 500 Unicode code points.
     */
    public static boolean isValidNote(String note) {
        requireNonNull(note);
        return !note.isBlank() && note.codePointCount(0, note.length()) <= MAX_LENGTH;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Note otherNote && value.equals(otherNote.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
