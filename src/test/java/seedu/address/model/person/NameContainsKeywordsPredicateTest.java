package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NameContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = List.of("first");
        List<String> secondPredicateKeywordList = List.of("first", "second");

        NameContainsKeywordsPredicate firstPredicate = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        NameContainsKeywordsPredicate secondPredicate = new NameContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        NameContainsKeywordsPredicate firstPredicateCopy = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different person -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_nameContainsKeywords_returnsTrue() {
        // One keyword
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Alice"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Multiple keywords
        predicate = new NameContainsKeywordsPredicate(List.of("Alice", "Bob"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Only one matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Bob", "Carol"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Carol").build()));

        // Mixed-case keywords
        predicate = new NameContainsKeywordsPredicate(List.of("aLIce", "bOB"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));
    }

    @Test
    public void test_keywordStartsBracketedOrCommaWord_returnsTrue() {
        Person person = new PersonBuilder().withName("Tan Wei Ming (Kelvin Chen), Jr.").build();

        assertTrue(new NameContainsKeywordsPredicate(List.of("Kelvin")).test(person));
        assertTrue(new NameContainsKeywordsPredicate(List.of("chen")).test(person));
        assertTrue(new NameContainsKeywordsPredicate(List.of("Ming")).test(person));
    }

    @Test
    public void test_keywordsContainParenthesesOrCommas_returnsTrue() {
        Person aliasCandidate = new PersonBuilder().withName("Tan Wei Ming (Kelvin Chen)").build();
        Person suffixCandidate = new PersonBuilder().withName("Martin Luther King, Jr.").build();

        assertTrue(new NameContainsKeywordsPredicate(List.of("(Kelvin)")).test(aliasCandidate));
        assertTrue(new NameContainsKeywordsPredicate(List.of("(kEL)")).test(aliasCandidate));
        assertTrue(new NameContainsKeywordsPredicate(List.of("Chen)")).test(aliasCandidate));
        assertTrue(new NameContainsKeywordsPredicate(List.of("King,")).test(suffixCandidate));
        assertTrue(new NameContainsKeywordsPredicate(List.of("Nobody", "(Kelvin)")).test(aliasCandidate));
    }

    @Test
    public void test_punctuationOnlyKeywords_doNotMatchEveryCandidate() {
        Person candidate = new PersonBuilder().withName("Tan Wei Ming (Kelvin)").build();

        assertFalse(new NameContainsKeywordsPredicate(List.of("(", ")", ",", "(,)")).test(candidate));
        assertFalse(new NameContainsKeywordsPredicate(List.of("(,)", "Nobody")).test(candidate));
        assertTrue(new NameContainsKeywordsPredicate(List.of("(,)", "Kelvin")).test(candidate));
    }

    @Test
    public void test_normalizedKeywordInsideNameWord_returnsFalse() {
        Person candidate = new PersonBuilder().withName("Tan Wei Ming (Kelvin)").build();

        assertFalse(new NameContainsKeywordsPredicate(List.of("(elvin)")).test(candidate));
        assertFalse(new NameContainsKeywordsPredicate(List.of("(Kelvins)")).test(candidate));
    }

    @Test
    public void test_apostrophesAndHyphens_keepWordPrefixSemantics() {
        Person candidate = new PersonBuilder().withName("Kelly-Ann O'Brien").build();

        assertTrue(new NameContainsKeywordsPredicate(List.of("Kelly-A")).test(candidate));
        assertTrue(new NameContainsKeywordsPredicate(List.of("O'B")).test(candidate));
        assertFalse(new NameContainsKeywordsPredicate(List.of("Ann", "Brien")).test(candidate));
    }

    @Test
    public void test_keywordStartsNameWord_returnsTrue() {
        // Start of the first word
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Ale"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alex Yeoh").build()));

        // Start of a later word, different case
        predicate = new NameContainsKeywordsPredicate(List.of("yEo"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alex Yeoh").build()));

        // Single letter
        predicate = new NameContainsKeywordsPredicate(List.of("y"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alex Yeoh").build()));
    }

    @Test
    public void test_keywordInsideOrLongerThanNameWord_returnsFalse() {
        // Inside a word
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("lex", "eoh"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alex Yeoh").build()));

        // Longer than the word it starts
        predicate = new NameContainsKeywordsPredicate(List.of("Alexander"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alex Yeoh").build()));
    }

    @Test
    public void test_nameDoesNotContainKeywords_returnsFalse() {
        // Zero keywords
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").build()));

        // Non-matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Carol"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Keywords match phone, email and address, but do not match name
        predicate = new NameContainsKeywordsPredicate(List.of("12345", "alice@email.com", "Main", "Street"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").withAddress("Main Street").build()));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(keywords);

        String expected = NameContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
