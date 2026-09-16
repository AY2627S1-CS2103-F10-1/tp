package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

public class RemarkCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndex_addsRemark() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes coffee"));
        Person edited = new Person(original.getName(), original.getPhone(), original.getEmail(), original.getAddress(),
                original.getTags(), new Remark("Likes coffee"));
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        expected.setPerson(original, edited);
        expected.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        assertCommandSuccess(command, model, String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS,
                Messages.format(edited)), expected);
    }

    @Test
    public void execute_validIndex_emptyRemark_removesRemark() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));
        Person edited = new Person(original.getName(), original.getPhone(), original.getEmail(), original.getAddress(),
                original.getTags(), new Remark(""));
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        expected.setPerson(original, edited);
        expected.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        assertCommandSuccess(command, model, String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS,
                Messages.format(edited)), expected);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index invalid = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RemarkCommand(invalid, new Remark("x")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand first = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("x"));
        assertTrue(first.equals(first));
        assertTrue(first.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("x"))));
        assertFalse(first.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("x"))));
        assertFalse(first.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("y"))));
        assertFalse(first.equals(null));
    }
}
