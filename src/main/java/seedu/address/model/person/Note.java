package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a candidate's single note. Instances are immutable and contain trimmed, non-blank text.
 */
public class Note {

    public static final int MAX_LENGTH = 500;
    public static final String MESSAGE_BLANK = "Note cannot be blank.";
    public static final String MESSAGE_TOO_LONG = "Note cannot exceed 500 characters.";

    public final String value;

    /**
     * Constructs a note, trimming leading and trailing whitespace while preserving its contents.
     *
     * @param note Text containing between 1 and 500 characters after trimming.
     */
    public Note(String note) {
        requireNonNull(note);
        String trimmedNote = note.strip();
        checkArgument(!trimmedNote.isBlank(), MESSAGE_BLANK);
        checkArgument(trimmedNote.codePointCount(0, trimmedNote.length()) <= MAX_LENGTH, MESSAGE_TOO_LONG);
        value = trimmedNote;
    }

    /**
     * Returns whether the given text is non-blank and within the length limit after trimming.
     */
    public static boolean isValidNote(String note) {
        requireNonNull(note);
        String trimmedNote = note.strip();
        return !trimmedNote.isBlank()
                && trimmedNote.codePointCount(0, trimmedNote.length()) <= MAX_LENGTH;
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
