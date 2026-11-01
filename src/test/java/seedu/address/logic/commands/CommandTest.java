package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;

public class CommandTest {

    @Test
    public void checkDisplayedIndex_firstAndLastCandidates_doesNotThrow() {
        assertDoesNotThrow(() -> Command.checkDisplayedIndex(Index.fromOneBased(1), 1));
        assertDoesNotThrow(() -> Command.checkDisplayedIndex(Index.fromOneBased(1), 3));
        assertDoesNotThrow(() -> Command.checkDisplayedIndex(Index.fromOneBased(3), 3));
    }

    @Test
    public void checkDisplayedIndex_emptyList_throwsCommandException() {
        assertThrows(CommandException.class, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                Command.checkDisplayedIndex(Index.fromOneBased(1), 0));
    }

    @Test
    public void checkDisplayedIndex_beyondDisplayedList_throwsCommandException() {
        assertThrows(CommandException.class, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                Command.checkDisplayedIndex(Index.fromOneBased(4), 3));
        assertThrows(CommandException.class, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                Command.checkDisplayedIndex(Index.fromOneBased(Integer.MAX_VALUE), 3));
    }
}
