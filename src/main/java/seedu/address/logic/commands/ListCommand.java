package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.model.Model;

/**
 * Lists all persons in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_SUCCESS = "Listed all %1$d candidate(s).";
    public static final String MESSAGE_NO_CANDIDATES = "There are no candidates yet. Use add to add one.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        int count = model.getFilteredPersonList().size();
        if (count == 0) {
            return new CommandResult(MESSAGE_NO_CANDIDATES);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, count));
    }
}
