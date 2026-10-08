package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NoteTest {

    @Test
    public void constructor_nullNote_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Note(null));
    }

    @Test
    public void constructor_blankNote_throwsIllegalArgumentException() {
        for (String text : new String[] {"", " ", "\t\r\n", "\u2003"}) {
            assertThrows(IllegalArgumentException.class, Note.MESSAGE_BLANK, () -> new Note(text));
        }
    }

    @Test
    public void constructor_overLengthLimit_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Note.MESSAGE_TOO_LONG, () -> new Note("a".repeat(501)));
    }

    @Test
    public void constructor_surroundingWhitespace_preservesInternalContents() {
        String contents = "Strong on C++,  weak on SQL; [round 2] / follow-up\tYES\nNext: \u4e2d\u6587";
        assertEquals(contents, new Note("\t \u2003" + contents + "\u2003 \r\n").value);
    }

    @Test
    public void constructor_boundaryLengths_acceptsNotes() {
        assertEquals("a", new Note("a").value);
        assertEquals("a".repeat(500), new Note("  " + "a".repeat(500) + "  ").value);
        String emoji = "\ud83d\ude00".repeat(500);
        assertEquals(emoji, new Note(emoji).value);
    }

    @Test
    public void isValidNote_validAndInvalidPartitions_returnsExpectedResult() {
        assertThrows(NullPointerException.class, () -> Note.isValidNote(null));
        assertFalse(Note.isValidNote(" \t\n"));
        assertFalse(Note.isValidNote("a".repeat(501)));
        assertTrue(Note.isValidNote(" x "));
        assertTrue(Note.isValidNote("a".repeat(500)));
        assertFalse(Note.isValidNote("\ud83d\ude00".repeat(501)));
    }

    @Test
    public void equals_sameAndDifferentContents_returnsExpectedResult() {
        Note note = new Note("Interview feedback");
        Note copy = new Note(" Interview feedback ");
        assertTrue(note.equals(note));
        assertEquals(note, copy);
        assertEquals(note.hashCode(), copy.hashCode());
        assertFalse(note.equals(null));
        assertFalse(note.equals("Interview feedback"));
        assertFalse(note.equals(new Note("Different feedback")));
        assertFalse(note.equals(new Note("interview feedback")));
        assertEquals("Interview feedback", note.toString());
    }
}
