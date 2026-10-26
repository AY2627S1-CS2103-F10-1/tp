package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;

public class ExportCommandTest {
    @Test
    public void execute_validModel_requestsExport() {
        assertTrue(new ExportCommand().execute(new ModelManager()).isExport());
    }

    @Test
    public void hashCode_equalCommands_returnsSameHashCode() {
        assertEquals(new ExportCommand().hashCode(), new ExportCommand().hashCode());
    }

    @Test
    public void toString_exportCommand_returnsCommandWord() {
        assertEquals("export", new ExportCommand().toString());
    }
}
