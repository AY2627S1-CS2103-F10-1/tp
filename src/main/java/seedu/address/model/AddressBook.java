package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.role.RoleCatalog;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();
    private RoleCatalog roleCatalog = new RoleCatalog();

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        persons.forEach(person -> roleCatalog.validateReferences(person.getRoles()));
        this.persons.setPersons(persons);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        RoleCatalog replacement = newData.getRoleCatalog();
        newData.getPersonList().forEach(person -> replacement.validateReferences(person.getRoles()));
        UniquePersonList validated = new UniquePersonList();
        validated.setPersons(newData.getPersonList());
        roleCatalog = replacement;
        persons.setPersons(validated);
    }

    @Override
    public RoleCatalog getRoleCatalog() {
        return roleCatalog;
    }

    /** Replaces the catalog only when every current person reference remains valid. */
    public void setRoleCatalog(RoleCatalog catalog) {
        requireNonNull(catalog);
        persons.asUnmodifiableObservableList().forEach(person -> catalog.validateReferences(person.getRoles()));
        roleCatalog = catalog;
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        return findSamePerson(person).isPresent();
    }

    /**
     * Returns the stored person with the same identity as {@code person}, or empty if none exists.
     */
    public Optional<Person> findSamePerson(Person person) {
        return persons.findSamePerson(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        roleCatalog.validateReferences(p.getRoles());
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        roleCatalog.validateReferences(editedPerson.getRoles());
        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons)
                && roleCatalog.equals(otherAddressBook.roleCatalog);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, roleCatalog);
    }
}
