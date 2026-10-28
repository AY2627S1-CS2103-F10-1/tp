package seedu.address.logic.parser;

import seedu.address.logic.commands.ImportCommand;

/** Parses the fixed-path import command, ignoring extra arguments like export. */
public class ImportCommandParser implements Parser<ImportCommand> {
    @Override
    public ImportCommand parse(String args) {
        return new ImportCommand();
    }
}
