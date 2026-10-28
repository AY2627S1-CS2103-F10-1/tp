package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;

public class CommandResultTest {
    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());
    }

    @Test
    public void equals_exportRequest_includesFlag() {
        CommandResult request = new CommandResult("", false, false, true);
        CommandResult same = new CommandResult("", false, false, true);
        assertEquals(request, same);
        assertEquals(request.hashCode(), same.hashCode());
        assertNotEquals(new CommandResult(""), request);
        assertFalse(new CommandResult("").isExport());
        assertTrue(request.isExport());
    }

    @Test
    public void createImportRequest_flagsAndEquality_includeImport() {
        CommandResult request = CommandResult.createImportRequest();
        assertTrue(request.isImport());
        assertFalse(request.isExport());
        assertFalse(request.isExit());
        assertFalse(request.isShowHelp());
        assertFalse(new CommandResult("").isImport());
        assertEquals(request, CommandResult.createImportRequest());
        assertEquals(request.hashCode(), CommandResult.createImportRequest().hashCode());
        assertNotEquals(new CommandResult(""), request);
        assertNotEquals(CommandResult.createExportRequest(), request);
        assertTrue(new ImportCommand().execute(new ModelManager()).isImport());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + ", export=" + commandResult.isExport()
                + ", importCandidates=" + commandResult.isImport() + "}";
        assertEquals(expected, commandResult.toString());
    }
}
