package seedu.address.model.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

class RoleCatalogTest {
    @Test
    void catalog_invalidReferencesAndEntries_rejects() {
        Role role = Role.create("Engineer");
        assertThrows(IllegalArgumentException.class, () -> new RoleCatalog(List.of(role)));
        assertThrows(IllegalArgumentException.class, () -> new RoleCatalog(List.of(Role.NO_ROLE, role, role)));
        assertThrows(IllegalArgumentException.class, () -> new Role("bad-id", "Engineer"));
        assertThrows(IllegalArgumentException.class, () -> Role.create("No role"));
        assertThrows(IllegalArgumentException.class, () -> Role.create("Line\nBreak"));
        assertThrows(IllegalArgumentException.class, () -> new RoleCatalog().validateReferences(Set.of(role)));
        Person person = new PersonBuilder().build();
        assertEquals(Set.of(Role.NO_ROLE), person.getRoles());
        assertThrows(IllegalArgumentException.class, () -> person.withRoles(Set.of()));
        assertThrows(UnsupportedOperationException.class, () -> person.getRoles().clear());
    }
}
