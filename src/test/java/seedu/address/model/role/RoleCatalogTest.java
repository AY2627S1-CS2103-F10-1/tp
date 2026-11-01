package seedu.address.model.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class RoleCatalogTest {
    @Test
    void constructor_missingBuiltInRole_rejects() {
        Role role = Role.create("Engineer");

        assertThrows(IllegalArgumentException.class, "The catalog must contain No role.", () ->
                new RoleCatalog(List.of(role)));
    }

    @Test
    void constructor_duplicateIdentity_rejects() {
        Role role = Role.create("Engineer");

        assertThrows(IllegalArgumentException.class, "Duplicate role identity.", () ->
                new RoleCatalog(List.of(Role.NO_ROLE, role, role)));
    }

    @Test
    void validateReferences_unknownRole_rejects() {
        Role role = Role.create("Engineer");
        RoleCatalog catalog = new RoleCatalog();

        assertThrows(IllegalArgumentException.class, RoleCatalog.MESSAGE_INVALID_REFERENCE, () ->
                catalog.validateReferences(Set.of(role)));
    }

    @Test
    void equalsAndHashCode_differentEntryOrder_preservesValueSemantics() {
        Role role = Role.create("Engineer");
        Role copy = new Role(role.getId(), "Engineer");
        RoleCatalog first = new RoleCatalog(List.of(role, Role.NO_ROLE));
        RoleCatalog second = new RoleCatalog(List.of(Role.NO_ROLE, copy));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
