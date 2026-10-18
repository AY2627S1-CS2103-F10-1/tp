package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.role.Role;
import seedu.address.testutil.PersonBuilder;

public class RoleContainsKeywordPredicateTest {

    private static final Role BACKEND = new Role("Backend");
    private static final Role FRONTEND = new Role("Frontend");

    @Test
    public void constructor_nullRole_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RoleContainsKeywordPredicate(null));
    }

    @Test
    public void test_candidateHasRole_returnsTrue() {
        Person p = new PersonBuilder().withRoles(Set.of(BACKEND)).build();
        assertTrue(new RoleContainsKeywordPredicate(BACKEND).test(p));
    }

    @Test
    public void test_candidateMissingRole_returnsFalse() {
        Person p = new PersonBuilder().withRoles(Set.of(FRONTEND)).build();
        assertFalse(new RoleContainsKeywordPredicate(BACKEND).test(p));
    }

    @Test
    public void equals_sameRole_returnsTrue() {
        assertEquals(new RoleContainsKeywordPredicate(BACKEND), new RoleContainsKeywordPredicate(new Role("backend")));
    }

    @Test
    public void hashCode_sameRole_sameHash() {
        assertEquals(new RoleContainsKeywordPredicate(BACKEND).hashCode(),
                new RoleContainsKeywordPredicate(BACKEND).hashCode());
    }

    @Test
    public void toStringMethod_includesRole() {
        String expected = RoleContainsKeywordPredicate.class.getCanonicalName() + "{role=" + BACKEND + "}";
        assertEquals(expected, new RoleContainsKeywordPredicate(BACKEND).toString());
    }
}
