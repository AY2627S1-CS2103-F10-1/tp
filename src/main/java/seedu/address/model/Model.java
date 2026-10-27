package seedu.address.model;

import java.util.Collection;
import java.util.function.Predicate;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;
import seedu.address.model.role.RoleCatalog;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns whether the displayed list is in expanded view. */
    ReadOnlyBooleanProperty isExpandedViewProperty();

    /**
     * Shows only the given displayed person in expanded view.
     *
     * @throws IllegalArgumentException if the person is not in the displayed list.
     */
    void expandPerson(Person person);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /** Captures a rollback action for a command whose subsequent save may fail. */
    default Runnable createRestorePoint() {
        AddressBook snapshot = new AddressBook(getAddressBook());
        return () -> setAddressBook(snapshot);
    }

    /** Returns the shared immutable role catalog. */
    default RoleCatalog getRoleCatalog() {
        return getAddressBook().getRoleCatalog();
    }

    /** Creates an unused role after checking name uniqueness. */
    void createRole(String name);

    /** Renames a role globally without replacing any person's assignments. */
    void renameRole(String oldName, String newName);

    /** Removes a role globally, falling back to No role only for empty assignments. */
    void deleteRole(String name);

    /** Resolves and replaces a person's assignments atomically. */
    default void replacePersonRoles(Person person, Collection<String> names) {
        setPerson(person, person.withRoles(getRoleCatalog().resolve(names)));
    }
}
