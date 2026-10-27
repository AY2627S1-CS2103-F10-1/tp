package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.role.Role;
import seedu.address.model.role.RoleCatalog;

class JsonRoleCatalogTest {
    private static final String ID = "11111111-1111-1111-1111-111111111111";

    @Test
    void toModelType_paddedStoredNames_preparesDisplayAndPreservesLookup() throws Exception {
        JsonRoleCatalog stored = new JsonRoleCatalog(List.of(
                new JsonRoleCatalog.Entry(Role.NO_ROLE_ID, " No role "),
                new JsonRoleCatalog.Entry(ID, "\u2003 Software  Engineer \t")));
        RoleCatalog catalog = stored.toModelType();
        assertEquals("Software  Engineer", catalog.requireRole(" SOFTWARE  ENGINEER ").getName());
        assertEquals(ID, catalog.requireRole("Software  Engineer").getId());
        assertEquals(Role.NO_ROLE, catalog.requireRole("no role"));
        assertEquals(catalog, new JsonRoleCatalog(catalog).toModelType());
    }

    @Test
    void toModelType_invalidStoredEntry_translatesToCheckedError() {
        for (String name : Arrays.asList(null, " ", "Line\nBreak", "Senior N/Engineer", "NO ROLE")) {
            JsonRoleCatalog stored = new JsonRoleCatalog(List.of(
                    new JsonRoleCatalog.Entry(Role.NO_ROLE_ID, Role.NO_ROLE_NAME),
                    new JsonRoleCatalog.Entry(ID, name)));
            assertThrows(IllegalValueException.class, stored::toModelType);
        }
        JsonRoleCatalog invalidId = new JsonRoleCatalog(List.of(new JsonRoleCatalog.Entry("bad-id", "Engineer")));
        assertThrows(IllegalValueException.class, "Invalid role identity.", invalidId::toModelType);
    }
}
