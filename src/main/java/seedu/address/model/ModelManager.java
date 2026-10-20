package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;
import seedu.address.model.role.Role;
import seedu.address.model.role.RoleCatalogue;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final ReadOnlyBooleanWrapper isExpandedView = new ReadOnlyBooleanWrapper(false);

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        filteredPersons.addListener((ListChangeListener<Person>) change -> isExpandedView.set(false));
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
    }

    @Override
    public Runnable createRestorePoint() {
        AddressBook snapshot = new AddressBook(addressBook);
        Predicate<? super Person> predicate = filteredPersons.getPredicate();
        boolean expanded = isExpandedView.get();
        return () -> {
            addressBook.resetData(snapshot);
            filteredPersons.setPredicate(predicate);
            isExpandedView.set(expanded);
        };
    }

    @Override
    public void createRole(String name) {
        List<Role> roles = new ArrayList<>(getRoleCatalogue().getRoles());
        checkArgument(getRoleCatalogue().find(name).isEmpty(), "A role with this name already exists.");
        roles.add(Role.create(name));
        addressBook.setRoleCatalogue(new RoleCatalogue(roles));
    }

    @Override
    public void renameRole(String oldName, String newName) {
        Role oldRole = getRoleCatalogue().requireRole(oldName);
        Role renamed = oldRole.renamed(newName);
        List<Role> roles = getRoleCatalogue().getRoles().stream()
                .map(role -> role.equals(oldRole) ? renamed : role).toList();
        replaceRoleData(oldRole, renamed, new RoleCatalogue(roles));
    }

    @Override
    public void deleteRole(String name) {
        Role role = getRoleCatalogue().requireRole(name);
        checkArgument(!role.equals(Role.NO_ROLE), "No role cannot be renamed or deleted.");
        List<Role> remaining = getRoleCatalogue().getRoles().stream()
                .filter(entry -> !entry.equals(role)).toList();
        replaceRoleData(role, null, new RoleCatalogue(remaining));
    }

    /** Validates a complete replacement before updating live catalogue and person references. */
    private void replaceRoleData(Role oldRole, Role renamed, RoleCatalogue catalogue) {
        AddressBook replacement = new AddressBook();
        replacement.setRoleCatalogue(catalogue);
        for (Person person : addressBook.getPersonList()) {
            Set<Role> assignments = new HashSet<>(person.getRoles());
            if (assignments.remove(oldRole) && renamed != null) {
                assignments.add(renamed);
            }
            if (assignments.isEmpty()) {
                assignments.add(Role.NO_ROLE);
            }
            replacement.addPerson(person.withRoles(assignments));
        }
        addressBook.resetData(replacement);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    @Override
    public ReadOnlyBooleanProperty isExpandedViewProperty() {
        return isExpandedView.getReadOnlyProperty();
    }

    @Override
    public void expandPerson(Person person) {
        requireNonNull(person);
        checkArgument(filteredPersons.contains(person), "Person must be in the displayed list.");
        updateFilteredPersonList(person::equals);
        isExpandedView.set(true);
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        isExpandedView.set(false);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons)
                && isExpandedView.get() == otherModelManager.isExpandedView.get();
    }

}
