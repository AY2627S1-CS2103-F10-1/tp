package seedu.address.logic.parser;

import seedu.address.logic.commands.ExportCommand;

/** Parses the export command, ignoring extra arguments like other parameterless commands. */
public class ExportCommandParser implements Parser<ExportCommand> {
    @Override
    public ExportCommand parse(String args) {
        return new ExportCommand();
    }
}
