package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.role.Role;
import seedu.address.testutil.PersonBuilder;

class CandidateMatchesFilterPredicateTest {
    private final Role developer = new Role("00000000-0000-0000-0000-000000000001", "Developer");
    private final Role designer = new Role("00000000-0000-0000-0000-000000000002", "Designer");

    @Test
    void constructor_noCriteria_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new CandidateMatchesFilterPredicate(List.of(), Set.of()));
    }

    @Test
    void test_multipleRoles_matchesAnyRoleAndIgnoresNameChanges() {
        CandidateMatchesFilterPredicate predicate = new CandidateMatchesFilterPredicate(List.of(),
                Set.of(developer, designer));
        assertTrue(predicate.test(new PersonBuilder().withRoles(designer).build()));
        assertTrue(predicate.test(new PersonBuilder()
                .withRoles(developer.renamed("Senior Developer")).build()));
        assertFalse(predicate.test(new PersonBuilder().build()));
        assertFalse(predicate.test(new PersonBuilder().withRoles(
                new Role("00000000-0000-0000-0000-000000000003", "Developer")).build()));
    }

    @Test
    void test_combinedCriteria_requiresBothGroups() {
        CandidateMatchesFilterPredicate predicate = new CandidateMatchesFilterPredicate(
                List.of(Status.INTERVIEWING, Status.OFFERED), Set.of(developer));
        assertTrue(predicate.test(new PersonBuilder()
                .withStatus(Status.OFFERED).withRoles(developer).build()));
        assertFalse(predicate.test(new PersonBuilder()
                .withStatus(Status.APPLIED).withRoles(developer).build()));
        assertFalse(predicate.test(new PersonBuilder()
                .withStatus(Status.OFFERED).withRoles(designer).build()));
    }

    @Test
    void person_noRoleWithNamedRoles_removesFallbackAndCopiesAssignments() {
        Person person = new PersonBuilder().withRoles(Role.NO_ROLE, developer, designer).build();
        assertEquals(Set.of(developer, designer), person.getRoles());
        assertEquals(person, new PersonBuilder(person).build());
        assertThrows(UnsupportedOperationException.class, () -> person.getRoles().clear());
    }
}
