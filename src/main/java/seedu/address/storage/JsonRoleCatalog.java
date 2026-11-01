package seedu.address.storage;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.role.Role;
import seedu.address.model.role.RoleCatalog;

/** Jackson representation kept separate from the validated role model. */
class JsonRoleCatalog {
    private final List<Entry> roles;

    @JsonCreator
    JsonRoleCatalog(@JsonProperty("roles") List<Entry> roles) {
        this.roles = roles;
    }

    JsonRoleCatalog(RoleCatalog catalog) {
        roles = catalog.getRoles().stream().map(role -> new Entry(role.getId(), role.getName())).toList();
    }

    RoleCatalog toModelType() throws IllegalValueException {
        if (roles == null || roles.stream().anyMatch(Objects::isNull)) {
            throw new IllegalValueException("The role catalog must contain a non-null roles list.");
        }
        if (roles.stream().anyMatch(entry -> entry.id == null || entry.name == null)) {
            throw new IllegalValueException("Role identities and names must not be null.");
        }
        try {
            return new RoleCatalog(roles.stream()
                    .map(entry -> new Role(entry.id, Role.prepareName(entry.name))).toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

    /** Represents one persisted catalog entry. */
    static class Entry {
        private final String id;
        private final String name;

        @JsonCreator
        Entry(@JsonProperty("id") String id, @JsonProperty("name") String name) {
            this.id = id;
            this.name = name;
        }
    }
}
