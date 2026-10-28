# Java conventions

Adapted from the [SE-EDU basic and intermediate Java rules](https://se-education.org/guides/conventions/java/intermediate.html), checked on 28 October 2026. See [LICENSE](LICENSE) for upstream attribution. Advanced-only rules are not required. Use the [Google Java style guide](https://google.github.io/styleguide/javaguide.html) for uncovered topics.

## Naming

* Package names are lowercase. Keep this project's existing `seedu.address` hierarchy; use a project or group root for a new school project rather than claiming an institutional namespace.
* Classes and enums use PascalCase nouns; methods use camelCase verbs; variables use camelCase.
* Constants use `SCREAMING_SNAKE_CASE`. Give related constants a shared prefix.
* Treat acronyms as words within names, such as `openDvdPlayer`, rather than `openDVDPlayer`.
* Use English names. Prefer longer, descriptive names for wider scopes. Short scratch names belong only in small scopes; reserve `j` and later iterator names for nested loops.
* Boolean names should read as conditions, preferably using `is`, `has`, `was`, `can`, or `should`. A boolean setter takes the form `setFound(boolean isFound)`.
* Give collections plural names.
* Tests may use `featureUnderTest_testScenario_expectedBehavior()`. Omit parts only when the remaining name accurately describes the test's scope.

## Layout

* Indent with four spaces, never tabs. Aim for lines under 110 characters; the hard limit is 120.
* Indent wrapped lines eight spaces beyond the parent line. Break after commas and before operators, including `.`, `&` in type bounds, and `|` in multi-catch declarations.
* Keep a method or constructor name attached to its opening `(`. Prefer a break outside a parenthesised expression when it improves readability.
* Use K&R braces. Put `else`, `catch`, and `finally` on the line containing the preceding closing brace.
* Use spaces around operators, after keywords and commas, and after semicolons in `for` headers. Surround the ternary `:` with spaces.
* Separate logical groups within a block with one blank line.

```java
if (hasCandidates) {
    updateSummary(candidates,
            selectedStatus);
} else {
    showEmptyList();
}
```

## Statements

* Put every class in a package. Keep import ordering consistent and do not use wildcard imports.
* Project import order: static imports; `java`/`javax`; `org`; other imports. Separate groups with blank lines and sort imports alphabetically within each group, as the Checkstyle configuration requires.
* Attach array brackets to the type: `int[] values`.
* Declare variables in the smallest useful scope and initialise them where declared when an appropriate value exists. Do not add fake initial values merely to satisfy the layout convention.
* Do not expose mutable class variables as public, except in behavior-free data classes. Constants may be public.
* Always use braces for conditionals and loops, even for a single statement. Keep the body on its own line.
* Use the conventional forms of `if`/`else`, `for`, `while`, `do`/`while`, `switch`, and `try`/`catch`/`finally`.
* In a colon-style `switch`, mark intentional fallthrough with `// Fallthrough`; do not leave a missing `break` ambiguous. Arrow-style switches are also accepted.

## Comments and Javadoc

* Write descriptive class and public-method Javadoc, except for trivial accessors, tests, and overrides that retain the documented behavior. Use `{@inheritDoc}` when appropriate.
* Start the summary with a third-person verb, such as `Returns` or `Adds`. Use English, American spelling, and complete sentences.
* Use a multi-line Javadoc block with `/**` on its own line, aligned `*` characters, and a space after each `*`. Separate tags from the description with one blank line.
* Supply all `@param` tags or omit them only when the parameters are already self-explanatory or described. Supply `@return` unless the return value is obvious or the method is `void`.
* Indent comments with their code. Explain purpose or rationale instead of repeating obvious statements.

## Verification

Review the changed code manually as well as running `gradlew.bat checkstyleMain checkstyleTest`. Checkstyle enforces only part of this standard; a passing report does not establish compliance with naming, explanation quality, or every layout rule.
