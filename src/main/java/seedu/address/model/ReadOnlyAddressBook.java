package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.model.role.RoleCatalog;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

    /** Returns the catalog; legacy implementations contain only the built-in role. */
    default RoleCatalog getRoleCatalog() {
        return new RoleCatalog();
    }

}
