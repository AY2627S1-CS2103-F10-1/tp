package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class NoteCommandTest {

    private static final String NOTE_TEXT = "Strong on system design, weak on SQL";
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NoteCommand(null, new Note(NOTE_TEXT)));
        assertThrows(NullPointerException.class, () -> new NoteCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new NoteCommand(INDEX_FIRST_PERSON, new Note(NOTE_TEXT)).execute(null));
    }

    @Test
    public void execute_firstAndLastUnfilteredIndexes_updatesOnlySelectedCandidate() {
        assertNoteUpdateSuccess(INDEX_FIRST_PERSON, NOTE_TEXT);
        assertNoteUpdateSuccess(Index.fromOneBased(model.getFilteredPersonList().size()), "Schedule final interview");
    }

    @Test
    public void execute_existingNote_replacesEntireNote() {
        Person original = model.getFilteredPersonList().getFirst();
        model.setPerson(original, new PersonBuilder(original).withNote("Old feedback").build());

        assertNoteUpdateSuccess(INDEX_FIRST_PERSON, NOTE_TEXT);
    }

    @Test
    public void execute_identicalNote_succeedsNormally() {
        assertNoteUpdateSuccess(INDEX_FIRST_PERSON, NOTE_TEXT);
        assertNoteUpdateSuccess(INDEX_FIRST_PERSON, NOTE_TEXT);
    }

    @Test
    public void execute_filteredIndex_updatesDisplayedCandidateAndResetsFilter() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        assertNoteUpdateSuccess(INDEX_FIRST_PERSON, NOTE_TEXT);
        assertEquals(getTypicalAddressBook().getPersonList().size(), model.getFilteredPersonList().size());
        assertTrue(model.getAddressBook().getPersonList().get(1).hasNote());
        assertFalse(model.getAddressBook().getPersonList().getFirst().hasNote());
    }

    @Test
    public void execute_outOfRangeUnfilteredIndex_keepsDataUnchanged() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new NoteCommand(invalidIndex, new Note(NOTE_TEXT)), model,
                NoteCommand.MESSAGE_INVALID_CANDIDATE_INDEX);
    }

    @Test
    public void execute_outOfRangeFilteredIndex_keepsDataAndFilterUnchanged() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertCommandFailure(new NoteCommand(INDEX_SECOND_PERSON, new Note(NOTE_TEXT)), model,
                NoteCommand.MESSAGE_INVALID_CANDIDATE_INDEX);
    }

    @Test
    public void execute_emptyDisplayedList_keepsDataUnchanged() {
        model.updateFilteredPersonList(person -> false);
        assertCommandFailure(new NoteCommand(INDEX_FIRST_PERSON, new Note(NOTE_TEXT)), model,
                NoteCommand.MESSAGE_INVALID_CANDIDATE_INDEX);
    }

    @Test
    public void equals_sameAndDifferentArguments_returnsExpectedResult() {
        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, new Note(NOTE_TEXT));
        NoteCommand copy = new NoteCommand(INDEX_FIRST_PERSON, new Note(NOTE_TEXT));
        assertTrue(command.equals(command));
        assertEquals(command, copy);
        assertEquals(command.hashCode(), copy.hashCode());
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ListCommand()));
        assertFalse(command.equals(new NoteCommand(INDEX_SECOND_PERSON, new Note(NOTE_TEXT))));
        assertFalse(command.equals(new NoteCommand(INDEX_FIRST_PERSON, new Note("Other feedback"))));
    }

    @Test
    public void toString_noteContents_doesNotExposeFeedback() {
        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, new Note(NOTE_TEXT));
        assertEquals(NoteCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON + "}", command.toString());
    }

    private void assertNoteUpdateSuccess(Index index, String text) {
        Person original = model.getFilteredPersonList().get(index.getZeroBased());
        Person updated = new PersonBuilder(original).withNote(text).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        expectedModel.setPerson(original, updated);
        String expectedMessage = String.format(NoteCommand.MESSAGE_SUCCESS, original.getName(), text);

        assertCommandSuccess(new NoteCommand(index, new Note(text)), model, expectedMessage, expectedModel);
    }
}
