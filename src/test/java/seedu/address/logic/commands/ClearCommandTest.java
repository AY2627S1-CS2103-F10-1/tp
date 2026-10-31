package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TestCaseUtil.assertAllCases;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_emptyAddressBook_reportsNothingToClear() {
        assertAllCases(List.of(false, true), isConfirmed -> {
            Model model = new ModelManager();
            Model expectedModel = new ModelManager();

            assertCommandSuccess(new ClearCommand(isConfirmed), model, ClearCommand.MESSAGE_NO_CANDIDATES,
                    expectedModel);
        });
    }

    @Test
    public void execute_nonEmptyAddressBookUnconfirmed_asksForConfirmationWithoutClearing() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        String expectedMessage = String.format(ClearCommand.MESSAGE_CONFIRM,
                getTypicalAddressBook().getPersonList().size());

        assertCommandSuccess(new ClearCommand(false), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_nonEmptyAddressBookConfirmed_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());

        assertCommandSuccess(new ClearCommand(true), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_filteredListUnconfirmed_countsHiddenCandidates() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);
        String expectedMessage = String.format(ClearCommand.MESSAGE_CONFIRM,
                getTypicalAddressBook().getPersonList().size());

        assertCommandSuccess(new ClearCommand(false), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredListConfirmed_clearsHiddenCandidatesToo() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        new ClearCommand(true).execute(model);

        assertEquals(0, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void equals_sameAndDifferentConfirmation_returnsExpectedResult() {
        ClearCommand confirmed = new ClearCommand(true);

        assertEquals(confirmed, confirmed);
        assertEquals(confirmed, new ClearCommand(true));
        assertEquals(confirmed.hashCode(), new ClearCommand(true).hashCode());
        assertNotEquals(confirmed, new ClearCommand(false));
        assertFalse(confirmed.equals(null));
        assertFalse(confirmed.equals(new ListCommand()));
    }

    @Test
    public void toString_confirmedCommand_includesConfirmation() {
        assertEquals(ClearCommand.class.getCanonicalName() + "{isConfirmed=true}", new ClearCommand(true).toString());
    }
}
