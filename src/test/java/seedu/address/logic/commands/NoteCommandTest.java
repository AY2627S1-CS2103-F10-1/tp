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
import seedu.address.model.person.Status;
import seedu.address.testutil.PersonBuilder;

public class NoteCommandTest {

    private static final String NOTE_TEXT = "Strong on system design, weak on SQL";
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NoteCommand(null, new Note(NOTE_TEXT)));
        assertThrows(NullPointerException.class, () -> new NoteCommand(INDEX_FIRST_PERSON, null));
        assertThrows(NullPointerException.class, () -> new NoteCommand(null));
    }

    @Test
    public void execute_clearNote_preservesCandidateDetails() {
        Person original = model.getFilteredPersonList().getFirst();
        Person noted = new PersonBuilder(original).withStatus(Status.INTERVIEWING).withNote(NOTE_TEXT).build();
        model.setPerson(original, noted);
        Person cleared = new PersonBuilder(original).withStatus(Status.INTERVIEWING).build();
        Model expected = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        expected.setPerson(noted, cleared);

        assertCommandSuccess(new NoteCommand(INDEX_FIRST_PERSON), model,
                String.format(NoteCommand.MESSAGE_CLEAR_SUCCESS, original.getName()), expected);
        assertFalse(model.getFilteredPersonList().getFirst().hasNote());
    }

    @Test
    public void execute_clearAbsentNote_succeedsWithoutChangingCandidate() {
        Person original = model.getFilteredPersonList().getFirst();
        Model expected = new ModelManager(model.getAddressBook(), model.getUserPrefs());

        assertCommandSuccess(new NoteCommand(INDEX_FIRST_PERSON), model,
                String.format(NoteCommand.MESSAGE_CLEAR_SUCCESS, original.getName()), expected);
    }

    @Test
    public void execute_clearOutsideDisplayedList_keepsDataAndFilterUnchanged() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandFailure(new NoteCommand(INDEX_SECOND_PERSON), model, NoteCommand.MESSAGE_INVALID_CANDIDATE_INDEX);
    }

    @Test
    public void equals_clearCommands_comparesOperationAndIndex() {
        NoteCommand clear = new NoteCommand(INDEX_FIRST_PERSON);
        NoteCommand copy = new NoteCommand(INDEX_FIRST_PERSON);

        assertEquals(clear, copy);
        assertEquals(clear.hashCode(), copy.hashCode());
        assertFalse(clear.equals(new NoteCommand(INDEX_SECOND_PERSON)));
        assertFalse(clear.equals(new NoteCommand(INDEX_FIRST_PERSON, new Note(NOTE_TEXT))));
        assertFalse(new NoteCommand(INDEX_FIRST_PERSON, new Note(NOTE_TEXT)).equals(clear));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new NoteCommand(INDEX_FIRST_PERSON, new Note(NOTE_TEXT)).execute(null));
    }

    @Test
    public void execute_firstUnfilteredIndex_updatesOnlySelectedCandidate() {
        assertNoteUpdateSuccess(INDEX_FIRST_PERSON, NOTE_TEXT);
    }

    @Test
    public void execute_lastUnfilteredIndex_updatesOnlySelectedCandidate() {
        assertNoteUpdateSuccess(Index.fromOneBased(model.getFilteredPersonList().size()), "Schedule final interview");
    }

    @Test
    public void execute_existingNote_replacesEntireNote() {
        Person original = model.getFilteredPersonList().getFirst();
        model.setPerson(original, new PersonBuilder(original).withNote("Old feedback").build());

        assertNoteUpdateSuccess(INDEX_FIRST_PERSON, NOTE_TEXT);
    }

    @Test
    public void execute_candidateWithStatus_preservesStatus() {
        Person original = model.getFilteredPersonList().getFirst();
        Person interviewingCandidate = new PersonBuilder(original).withStatus(Status.INTERVIEWING).build();
        model.setPerson(original, interviewingCandidate);

        assertNoteUpdateSuccess(INDEX_FIRST_PERSON, NOTE_TEXT);
    }

    @Test
    public void execute_identicalNote_succeedsNormally() {
        assertNoteUpdateSuccess(INDEX_FIRST_PERSON, NOTE_TEXT);
        assertNoteUpdateSuccess(INDEX_FIRST_PERSON, NOTE_TEXT);
    }

    @Test
    public void execute_filteredIndex_updatesDisplayedCandidateAndKeepsFilter() {
        Model expectedModel = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        assertNoteUpdateSuccess(INDEX_FIRST_PERSON, NOTE_TEXT, expectedModel);
        assertEquals(1, model.getFilteredPersonList().size());
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
        model.updateFilteredPersonList(candidate -> false);
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

    /**
     * Asserts that an unfiltered note update succeeds and preserves the candidate's other details.
     */
    private void assertNoteUpdateSuccess(Index index, String text) {
        Model expectedModel = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        assertNoteUpdateSuccess(index, text, expectedModel);
    }

    /**
     * Asserts that a note update succeeds with the explicitly prepared expected filter.
     */
    private void assertNoteUpdateSuccess(Index index, String text, Model expectedModel) {
        Person original = model.getFilteredPersonList().get(index.getZeroBased());
        Person updated = new PersonBuilder(original).withNote(text).build();
        expectedModel.setPerson(original, updated);
        String expectedMessage = String.format(NoteCommand.MESSAGE_SUCCESS, original.getName(), text);

        assertCommandSuccess(new NoteCommand(index, new Note(text)), model, expectedMessage, expectedModel);
    }
}
