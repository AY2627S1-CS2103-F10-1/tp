package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class StatusContainsKeywordPredicateTest {

    @Test
    public void equals() {
        StatusContainsKeywordPredicate appliedPredicate = new StatusContainsKeywordPredicate(Status.APPLIED);
        StatusContainsKeywordPredicate hiredPredicate = new StatusContainsKeywordPredicate(Status.HIRED);

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
        assertFalse(appliedPredicate.equals(hiredPredicate));
    }

    @Test
    public void test_matchingStatus_returnsTrue() {
        StatusContainsKeywordPredicate predicate = new StatusContainsKeywordPredicate(Status.APPLIED);
        assertTrue(predicate.test(new PersonBuilder().withStatus(Status.APPLIED).build()));
    }

    @Test
    public void test_differentStatus_returnsFalse() {
        StatusContainsKeywordPredicate predicate = new StatusContainsKeywordPredicate(Status.APPLIED);
        assertFalse(predicate.test(new PersonBuilder().withStatus(Status.HIRED).build()));
    }

    @Test
    public void getStatus_returnsStoredStatus() {
        StatusContainsKeywordPredicate predicate = new StatusContainsKeywordPredicate(Status.OFFERED);
        assertEquals(Status.OFFERED, predicate.getStatus());
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
        String expected = StatusContainsKeywordPredicate.class.getCanonicalName() + "{status=" + Status.APPLIED + "}";
        assertEquals(expected, predicate.toString());
    }
}
