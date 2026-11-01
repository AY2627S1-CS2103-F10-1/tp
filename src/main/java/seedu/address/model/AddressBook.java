package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.role.Role;
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

    /** Creates a role without changing candidate assignments. */
    public void createRole(String name) {
        List<Role> roles = new ArrayList<>(roleCatalog.getRoles());
        checkArgument(roleCatalog.find(name).isEmpty(), RoleCatalog.MESSAGE_DUPLICATE_NAME);
        roles.add(Role.create(name));
        setRoleCatalog(new RoleCatalog(roles));
    }

    /** Renames a role and all its assignments, preserving its identity. */
    public void renameRole(String oldName, String newName) {
        Role oldRole = roleCatalog.requireRole(oldName);
        Role renamed = oldRole.renamed(newName);
        RoleCatalog catalog = new RoleCatalog(roleCatalog.getRoles().stream()
                .map(role -> role.equals(oldRole) ? renamed : role).toList());
        replaceRoleData(oldRole, Set.of(renamed), catalog);
    }

    /** Deletes a role, assigning No role to candidates whose last assignment is removed. */
    public void deleteRole(String name) {
        Role role = roleCatalog.requireRole(name);
        checkArgument(!role.equals(Role.NO_ROLE), Role.MESSAGE_PROTECTED_ROLE);
        RoleCatalog catalog = new RoleCatalog(roleCatalog.getRoles().stream()
                .filter(entry -> !entry.equals(role)).toList());
        replaceRoleData(role, Set.of(), catalog);
    }

    /** Validates the complete replacement before updating the live catalog and candidates. */
    private void replaceRoleData(Role oldRole, Set<Role> replacements, RoleCatalog catalog) {
        AddressBook replacement = new AddressBook();
        replacement.setRoleCatalog(catalog);
        for (Person person : getPersonList()) {
            Set<Role> assignments = new HashSet<>(person.getRoles());
            if (assignments.remove(oldRole)) {
                assignments.addAll(replacements);
            }
            if (assignments.isEmpty()) {
                assignments.add(Role.NO_ROLE);
            }
            replacement.addPerson(person.withRoles(assignments));
        }
        resetData(replacement);
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
