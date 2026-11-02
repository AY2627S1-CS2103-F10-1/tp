package seedu.address.model.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestCaseUtil.assertAllCases;

import java.util.List;

import org.junit.jupiter.api.Test;

class RoleTest {
    private static final String ID = "11111111-1111-1111-1111-111111111111";

    @Test
    void constructor_unpreparedOrInvalidName_rejects() {
        assertAllCases(List.of("", " ", " Engineer", "Engineer ", "Line\nBreak"), name -> {
            assertThrows(IllegalArgumentException.class, Role.MESSAGE_CONSTRAINTS, () -> new Role(ID, name));
        });
    }

    @Test
    void constructor_nullName_rejects() {
        assertThrows(NullPointerException.class, null, () -> new Role(ID, null));
    }

    @Test
    void constructor_nullIdentity_rejects() {
        assertThrows(NullPointerException.class, null, () -> new Role(null, "Engineer"));
    }

    @Test
    void constructor_invalidIdentity_rejects() {
        assertThrows(IllegalArgumentException.class, "Invalid role identity.", () -> new Role("bad-id", "Engineer"));
    }

    @Test
    void constructor_customIdentityWithReservedName_rejects() {
        assertThrows(IllegalArgumentException.class, Role.MESSAGE_RESERVED_IDENTITY, () -> new Role(ID, "NO ROLE"));
    }

    @Test
    void constructor_builtinIdentityAndName_preservesBuiltInRole() {
        assertEquals(Role.NO_ROLE, new Role(Role.NO_ROLE_ID, Role.NO_ROLE_NAME));
    }

    @Test
    void constructor_builtinIdentityWithCustomName_rejects() {
        assertThrows(IllegalArgumentException.class, Role.MESSAGE_RESERVED_IDENTITY, () ->
                new Role(Role.NO_ROLE_ID, "Engineer"));
    }

    @Test
    void constructor_builtinIdentityWithDifferentNameCase_rejects() {
        assertThrows(IllegalArgumentException.class, "The built-in role must be named " + Role.NO_ROLE_NAME + ".", () ->
                new Role(Role.NO_ROLE_ID, "no role"));
    }

    @Test
    void create_reservedName_rejects() {
        assertAllCases(List.of("No role", " NO ROLE "), name -> {
            assertThrows(IllegalArgumentException.class, Role.MESSAGE_RESERVED_IDENTITY, () -> Role.create(name));
        });
    }

    @Test
    void create_nullName_rejects() {
        assertThrows(NullPointerException.class, null, () -> Role.create(null));
    }

    @Test
    void renamed_nullName_rejects() {
        Role role = Role.create("Engineer");

        assertThrows(NullPointerException.class, null, () -> role.renamed(null));
    }

    @Test
    void create_paddedName_preservesPreparedDisplay() {
        Role role = Role.create("\u2003 Software  Engineer \u2003");

        assertEquals("Software  Engineer", role.getName());
    }

    @Test
    void renamed_paddedName_preservesPreparedDisplayAndIdentity() {
        Role role = Role.create("Engineer");

        Role renamed = role.renamed("\t Senior  Engineer \t");

        assertEquals("Senior  Engineer", renamed.getName());
        assertEquals(role.getId(), renamed.getId());
    }

    @Test
    void toLookupKey_paddedMixedCaseName_preservesInternalSpacing() {
        assertEquals("senior  engineer", Role.toLookupKey(" Senior  ENGINEER "));
    }

    @Test
    void create_embeddedControlCharacters_rejectsWithExamples() {
        List<String> names = List.of("Line\nBreak", "Software\tEngineer", "Software\nEngineer", "Software\rEngineer");

        assertAllCases(names, name -> {
            assertThrows(IllegalArgumentException.class, Role.MESSAGE_CONSTRAINTS, () -> Role.create(name));
        });
    }

    @Test
    void constructor_reservedCommandSyntax_rejects() {
        List<String> prefixes = List.of("n/", "r/", "p/", "e/", "a/", "t/", "no/", "s/", "N/", "NO/");

        assertAllCases(prefixes, prefix -> {
            assertThrows(IllegalArgumentException.class, Role.MESSAGE_RESERVED_SYNTAX, () ->
                    new Role(ID, "Senior " + prefix + "Engineer"));
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
    }
}
