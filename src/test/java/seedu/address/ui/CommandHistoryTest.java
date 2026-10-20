package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {

    @Test
    public void add_null_throwsNullPointerException() {
        CommandHistory h = new CommandHistory();
        assertThrows(NullPointerException.class, () -> h.add(null));
    }

    @Test
    public void add_blank_isIgnored() {
        CommandHistory h = new CommandHistory();
        h.add("");
        h.add("   ");
        assertTrue(h.getEntries().isEmpty());
        assertEquals(0, h.getCursor());
    }

    @Test
    public void add_resetsCursorToPastTheEnd() {
        CommandHistory h = new CommandHistory();
        h.add("list");
        h.previous();
        assertEquals(0, h.getCursor());
        h.add("find Alice");
        assertEquals(2, h.getCursor());
    }

    @Test
    public void previous_emptyHistory_returnsEmpty() {
        assertEquals(Optional.empty(), new CommandHistory().previous());
    }

    @Test
    public void previous_walksBackwardsOneStepAtATime() {
        CommandHistory h = new CommandHistory();
        h.add("list");
        h.add("find Alice");
        h.add("exit");

        assertEquals(Optional.of("exit"), h.previous());
        assertEquals(Optional.of("find Alice"), h.previous());
        assertEquals(Optional.of("list"), h.previous());
        assertEquals(Optional.empty(), h.previous());
    }

    @Test
    public void next_pastNewestEntry_returnsEmptyStringToClearField() {
        CommandHistory h = new CommandHistory();
        h.add("list");
        h.add("exit");
        h.previous(); // cursor at "exit"
        assertEquals(Optional.of(""), h.next());
        assertEquals(Optional.empty(), h.next());
    }

    @Test
    public void nextAndPrevious_areInverse() {
        CommandHistory h = new CommandHistory();
        h.add("list");
        h.add("find Alice");
        h.previous();
        h.previous();
        assertEquals(Optional.of("find Alice"), h.next());
        assertEquals(Optional.of("list"), h.previous());
    }

    @Test
    public void add_pastMaxEntries_dropsOldest() {
        CommandHistory h = new CommandHistory();
        for (int i = 0; i < CommandHistory.MAX_ENTRIES + 5; i++) {
            h.add("cmd" + i);
        }
        assertEquals(CommandHistory.MAX_ENTRIES, h.getEntries().size());
        assertEquals("cmd5", h.getEntries().get(0));
        assertEquals("cmd" + (CommandHistory.MAX_ENTRIES + 4), h.getEntries().get(CommandHistory.MAX_ENTRIES - 1));
    }

    @Test
    public void getEntries_returnsImmutableCopy() {
        CommandHistory h = new CommandHistory();
        h.add("list");
        var entries = h.getEntries();
        assertThrows(UnsupportedOperationException.class, () -> entries.add("mutated"));
        assertFalse(h.getEntries().contains("mutated"));
    }
}
