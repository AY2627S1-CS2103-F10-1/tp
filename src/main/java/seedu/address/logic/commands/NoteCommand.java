package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;

/**
 * Replaces the single note of a candidate in the displayed list.
 */
public class NoteCommand extends Command {

    public static final String COMMAND_WORD = "note";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Replaces the note of the candidate identified by the displayed index.\n"
            + "Parameters: INDEX (must be a positive integer) " + PREFIX_NOTE
            + "NOTE_TEXT (non-blank, at most 500 characters after trimming)\n"
            + "Example: " + COMMAND_WORD + " 2 " + PREFIX_NOTE + "Strong on system design, weak on SQL";
    public static final String MESSAGE_SUCCESS = "Updated note for %1$s: %2$s";
    public static final String MESSAGE_INVALID_CANDIDATE_INDEX = "The candidate index provided is invalid.";

    private final Index index;
    private final Note note;

    /**
     * Constructs a command to replace the note at the given displayed index.
     */
    public NoteCommand(Index index, Note note) {
        requireAllNonNull(index, note);
        this.index = index;
        this.note = note;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireAllNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(MESSAGE_INVALID_CANDIDATE_INDEX);
        }

        Person candidate = displayedPersons.get(index.getZeroBased());
        Person updatedCandidate = new Person(candidate.getName(), candidate.getPhone(), candidate.getEmail(),
                candidate.getAddress(), candidate.getTags(), candidate.getStatus(), Optional.of(note));
        model.setPerson(candidate, updatedCandidate);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_SUCCESS, candidate.getName(), note));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof NoteCommand otherCommand
                && index.equals(otherCommand.index) && note.equals(otherCommand.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, note);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).toString();
    }
}
