package seedu.address.storage;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.role.Role;
import seedu.address.model.role.RoleCatalogue;

/** Jackson representation kept separate from the validated role model. */
class JsonRoleCatalogue {
    private final List<Entry> roles;

    @JsonCreator
    JsonRoleCatalogue(@JsonProperty("roles") List<Entry> roles) {
        this.roles = roles;
    }

    JsonRoleCatalogue(RoleCatalogue catalogue) {
        roles = catalogue.getRoles().stream().map(role -> new Entry(role.getId(), role.getName())).toList();
    }

    RoleCatalogue toModelType() throws IllegalValueException {
        try {
            if (roles == null || roles.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("The role catalogue must contain a non-null roles list.");
            }
            return new RoleCatalogue(roles.stream().map(entry -> new Role(entry.id, entry.name)).toList());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

    /** Represents one persisted catalogue entry. */
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
