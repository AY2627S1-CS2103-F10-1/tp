package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "Address book has been cleared!";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        AddressBook empty = new AddressBook();
        empty.setRoleCatalog(model.getRoleCatalog());
        model.setAddressBook(empty);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
