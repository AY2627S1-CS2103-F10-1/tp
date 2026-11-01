package seedu.address.model.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestCaseUtil.assertAllCases;

import java.util.List;

import org.junit.jupiter.api.Test;

class RoleTest {
    @Test
    void constructor_unpreparedOrInvalidName_rejects() {
        String id = "11111111-1111-1111-1111-111111111111";
        assertAllCases(List.of("", " ", " Engineer", "Engineer ", "Line\nBreak"), name -> {
            assertThrows(IllegalArgumentException.class, Role.MESSAGE_CONSTRAINTS, () -> new Role(id, name));
        });
        assertThrows(NullPointerException.class, () -> new Role(id, null));
        assertThrows(NullPointerException.class, () -> new Role(null, "Engineer"));
        assertThrows(IllegalArgumentException.class,
                "The built-in No role identity and name are reserved.", () -> new Role(id, "NO ROLE"));
    }

    @Test
    void role_reservedIdentityAndName_preservesValidation() {
        assertEquals(Role.NO_ROLE, new Role(Role.NO_ROLE_ID, Role.NO_ROLE_NAME));
        assertThrows(IllegalArgumentException.class, () -> new Role(Role.NO_ROLE_ID, "Engineer"));
        assertThrows(IllegalArgumentException.class, () -> new Role(Role.NO_ROLE_ID, "no role"));
        assertThrows(IllegalArgumentException.class, () -> Role.create(" NO ROLE "));
    }

    @Test
    void createRole_embeddedControlCharacters_rejectsWithExamples() {
        assertAllCases(List.of("Software\tEngineer", "Software\nEngineer", "Software\rEngineer"), name -> {
            assertThrows(IllegalArgumentException.class, Role.MESSAGE_CONSTRAINTS, () -> Role.create(name));
        });
    }

    @Test
    void role_valueSemantics_preservesNameAndIdentity() {
        Role role = Role.create(" \tEngineer\t ");
        Role copy = new Role(role.getId(), "Engineer");
        assertEquals(role, copy);
        assertEquals(role.hashCode(), copy.hashCode());
        assertEquals("Engineer", role.toString());
        assertFalse(role.equals(role.renamed("Senior Engineer")));
        assertFalse(role.equals("Engineer"));
        RoleCatalog first = new RoleCatalog(List.of(role, Role.NO_ROLE));
        RoleCatalog second = new RoleCatalog(List.of(Role.NO_ROLE, copy));
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
