package seedu.address.model.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RoleTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Role(null));
    }

    @Test
    public void constructor_invalidRoleName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Role(""));
        assertThrows(IllegalArgumentException.class, () -> new Role(" "));
        assertThrows(IllegalArgumentException.class, () -> new Role("!bad"));
    }

    @Test
    public void isValidRoleName_variousInputs() {
        assertTrue(Role.isValidRoleName("Software Engineer"));
        assertTrue(Role.isValidRoleName("DevOps-Lead"));
        assertTrue(Role.isValidRoleName("Backend/Infra"));
        assertFalse(Role.isValidRoleName(""));
        assertFalse(Role.isValidRoleName(" Engineer"));
        assertFalse(Role.isValidRoleName("@admin"));
    }

    @Test
    public void equals_caseInsensitive() {
        Role a = new Role("Software Engineer");
        Role b = new Role("software engineer");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void equals_differentRoles_returnsFalse() {
        assertNotEquals(new Role("Frontend"), new Role("Backend"));
    }

    @Test
    public void toString_wrapsInBrackets() {
        assertEquals("[Software Engineer]", new Role("Software Engineer").toString());
    }
}
