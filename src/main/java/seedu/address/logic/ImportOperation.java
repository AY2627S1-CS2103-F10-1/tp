package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.CandidateCsvParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/** Coordinates whole-batch import and restores the in-memory snapshot if application or saving fails. */
final class ImportOperation {
    private static final Logger logger = LogsCenter.getLogger(ImportOperation.class);
    private final Model model;
    private final Storage storage;

    ImportOperation(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
    }

    CommandResult execute() throws CommandException {
        AddressBook snapshot = new AddressBook(model.getAddressBook());
        Predicate<? super Person> filter = model.getPersonFilter();
        Person expanded = model.isExpandedViewProperty().get() ? model.getFilteredPersonList().getFirst() : null;
        List<Person> imported = readCandidates();
        AddressBook combined = combine(snapshot, imported);
        try {
            model.setAddressBook(combined);
            save();
        } catch (CommandException | RuntimeException failure) {
            restore(snapshot, filter, expanded, failure);
            if (failure instanceof CommandException commandException) {
                throw commandException;
            }
            throw new CommandException("Import failed while applying candidates; the previous state was restored.",
                    failure);
        }
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        String importedSummary = imported.size() + (imported.size() == 1 ? " candidate" : " candidates");
        logger.info("Import completed: " + importedSummary + ".");
        return new CommandResult("Imported " + importedSummary + " successfully.");
    }

    private List<Person> readCandidates() throws CommandException {
        try {
            return CandidateCsvParser.parse(storage.readImport());
        } catch (IOException | ParseException e) {
            throw new CommandException("Import failed: " + e.getMessage(), e);
        }
    }

    private AddressBook combine(AddressBook snapshot, List<Person> imported) throws CommandException {
        Map<String, String> identities = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        snapshot.getPersonList().forEach(person -> identities.put(person.getEmail().value, "existing HRvest data"));
        AddressBook combined = new AddressBook(snapshot);
        for (int index = 0; index < imported.size(); index++) {
            Person person = imported.get(index);
            int record = index + 2;
            String conflict = identities.putIfAbsent(person.getEmail().value, "CSV record " + record);
            if (conflict != null) {
                throw new CommandException("Import failed: CSV record " + record
                        + ", field email: duplicate candidate conflicts with " + conflict + ".");
            }
            combined.addPerson(person);
        }
        return combined;
    }

    private void save() throws CommandException {
        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException e) {
            throw new CommandException(String.format(LogicManager.FILE_OPS_ERROR_FORMAT, e.getMessage()), e);
        }
    }

    private void restore(AddressBook snapshot, Predicate<? super Person> filter, Person expanded, Exception failure)
            throws CommandException {
        try {
            model.setAddressBook(snapshot);
            model.updateFilteredPersonList(filter == null ? Model.PREDICATE_SHOW_ALL_PERSONS : filter::test);
            if (expanded != null) {
                model.expandPerson(expanded);
            }
            if (!snapshot.getPersonList().equals(model.getAddressBook().getPersonList())
                    || model.isExpandedViewProperty().get() != (expanded != null)) {
                throw new IllegalStateException("Rollback verification failed.");
            }
        } catch (RuntimeException rollbackFailure) {
            logger.severe("Import rollback failed; state restoration could not be verified.");
            if (rollbackFailure != failure) {
                rollbackFailure.addSuppressed(failure);
            }
            throw new CommandException("Import failed and rollback failed. State could not be restored; "
                    + "stop using HRvest and check the saved data before restarting.", rollbackFailure);
        }
    }
}
