package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class StatusContainsKeywordPredicateTest {

    @Test
    public void constructor_nullStatus_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StatusContainsKeywordPredicate((Status) null));
    }

    @Test
    public void equals() {
        StatusContainsKeywordPredicate appliedPredicate = new StatusContainsKeywordPredicate(Status.APPLIED);
        StatusContainsKeywordPredicate acceptedPredicate = new StatusContainsKeywordPredicate(Status.ACCEPTED);

        // same object -> returns true
        assertEquals(appliedPredicate, appliedPredicate);

        // same values -> returns true
        StatusContainsKeywordPredicate appliedPredicateCopy = new StatusContainsKeywordPredicate(Status.APPLIED);
        assertEquals(appliedPredicate, appliedPredicateCopy);

        // different types -> returns false
        assertFalse(appliedPredicate.equals(1));

        // null -> returns false
        assertFalse(appliedPredicate.equals(null));

        // different status -> returns false
        assertFalse(appliedPredicate.equals(acceptedPredicate));
    }

    @Test
    public void test_matchingStatus_returnsTrue() {
        StatusContainsKeywordPredicate predicate = new StatusContainsKeywordPredicate(Status.APPLIED);
        assertTrue(predicate.test(new PersonBuilder().withStatus(Status.APPLIED).build()));
    }

    @Test
    public void test_differentStatus_returnsFalse() {
        StatusContainsKeywordPredicate predicate = new StatusContainsKeywordPredicate(Status.APPLIED);
        assertFalse(predicate.test(new PersonBuilder().withStatus(Status.ACCEPTED).build()));
    }

    @Test
    public void constructor_nullStatuses_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StatusContainsKeywordPredicate((List<Status>) null));
    }

    @Test
    public void constructor_noStatuses_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StatusContainsKeywordPredicate(List.of()));
    }

    @Test
    public void test_anyOfSeveralStatuses_returnsTrue() {
        StatusContainsKeywordPredicate predicate =
                new StatusContainsKeywordPredicate(List.of(Status.OFFERED, Status.INTERVIEWING));
        assertTrue(predicate.test(new PersonBuilder().withStatus(Status.INTERVIEWING).build()));
        assertTrue(predicate.test(new PersonBuilder().withStatus(Status.OFFERED).build()));
        assertFalse(predicate.test(new PersonBuilder().withStatus(Status.APPLIED).build()));
    }

    @Test
    public void getStatuses_duplicateUnorderedStatuses_returnsUniqueStatusesInPipelineOrder() {
        StatusContainsKeywordPredicate predicate =
                new StatusContainsKeywordPredicate(List.of(Status.OFFERED, Status.APPLIED, Status.OFFERED));
        assertEquals(List.of(Status.APPLIED, Status.OFFERED), List.copyOf(predicate.getStatuses()));
    }

    @Test
    public void equals_sameStatusesInDifferentOrder_returnsTrue() {
        StatusContainsKeywordPredicate predicate =
                new StatusContainsKeywordPredicate(List.of(Status.OFFERED, Status.APPLIED));
        assertEquals(new StatusContainsKeywordPredicate(List.of(Status.APPLIED, Status.OFFERED)), predicate);
        assertEquals(new StatusContainsKeywordPredicate(Status.APPLIED),
                new StatusContainsKeywordPredicate(List.of(Status.APPLIED)));
        assertFalse(predicate.equals(new StatusContainsKeywordPredicate(Status.APPLIED)));
    }

    @Test
    public void hashCode_sameStatus_sameHash() {
        StatusContainsKeywordPredicate p1 = new StatusContainsKeywordPredicate(Status.APPLIED);
        StatusContainsKeywordPredicate p2 = new StatusContainsKeywordPredicate(Status.APPLIED);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void toStringMethod() {
        StatusContainsKeywordPredicate predicate = new StatusContainsKeywordPredicate(Status.APPLIED);
        String expected = StatusContainsKeywordPredicate.class.getCanonicalName()
                + "{statuses=[" + Status.APPLIED + "]}";
        assertEquals(expected, predicate.toString());
    }
}
