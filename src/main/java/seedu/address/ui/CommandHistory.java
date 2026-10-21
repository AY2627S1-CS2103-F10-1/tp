package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Keeps a session-scoped list of commands submitted through the {@code CommandBox},
 * plus a cursor the UI uses to walk through them with the arrow keys.
 *
 * <p>Design notes:
 * <ul>
 *   <li>The cursor sits at {@link #entries}{@code .size()} when the user is composing a
 *       new command. That "past-the-end" position maps to an empty text field.</li>
 *   <li>Every submitted command is recorded, including ones that failed to parse or execute,
 *       so users can press Up and fix a typo.</li>
 *   <li>Older entries are discarded once {@link #MAX_ENTRIES} is reached.</li>
 * </ul>
 */
public class CommandHistory {

    /** Upper bound on the number of commands retained; bounds memory for long sessions. */
    public static final int MAX_ENTRIES = 100;

    private final List<String> entries = new ArrayList<>();
    private int cursor = 0;

    /**
     * Records a submitted command and resets the cursor to the past-the-end position.
     * Blank commands (empty or whitespace-only) are ignored.
     *
     * @param command the raw user input that was submitted
     * @throws NullPointerException if {@code command} is null
     */
    public void add(String command) {
        requireNonNull(command);
        if (command.isBlank()) {
            return;
        }
        if (entries.size() == MAX_ENTRIES) {
            entries.remove(0);
        }
        entries.add(command);
        cursor = entries.size();
    }

    /**
     * Returns the previous command relative to the current cursor, or
     * {@link Optional#empty()} when the user is already at the oldest entry.
     * The cursor moves one step backwards on a successful call.
     */
    public Optional<String> previous() {
        if (cursor <= 0) {
            return Optional.empty();
        }
        cursor--;
        return Optional.of(entries.get(cursor));
    }

    /**
     * Returns the next command relative to the current cursor. If the cursor is at the
     * newest entry, moves to the past-the-end position and returns an empty string so
     * the UI can clear its input field.
     */
    public Optional<String> next() {
        if (cursor >= entries.size()) {
            return Optional.empty();
        }
        cursor++;
        if (cursor == entries.size()) {
            return Optional.of("");
        }
        return Optional.of(entries.get(cursor));
    }

    /**
     * Returns an unmodifiable view of the recorded entries, oldest first.
     */
    public List<String> getEntries() {
        return List.copyOf(entries);
    }

    /**
     * Returns the current cursor position. Mostly for tests.
     */
    public int getCursor() {
        return cursor;
    }
}
