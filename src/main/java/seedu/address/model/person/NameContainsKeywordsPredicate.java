package seedu.address.model.person;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a word in a {@code Person}'s {@code Name} starts with any of the keywords given, ignoring case.
 */
public class NameContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    public NameContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(Person person) {
        String name = normalizeSearchWords(person.getName().fullName);
        return keywords.stream()
                .map(NameContainsKeywordsPredicate::normalizeSearchWords)
                .flatMap(keyword -> Arrays.stream(keyword.split("\\s+")))
                .filter(keyword -> !keyword.isEmpty())
                .anyMatch(keyword -> StringUtil.containsWordStartingWithIgnoreCase(name, keyword));
    }

    /** Treats parentheses and commas as word separators in both names and search keywords. */
    private static String normalizeSearchWords(String text) {
        return text.replaceAll("[(),]", " ");
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NameContainsKeywordsPredicate otherNameContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherNameContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
