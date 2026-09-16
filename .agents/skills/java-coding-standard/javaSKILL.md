---
name: java-coding-standard
description: The se-edu Java coding standard as enforced (partly) by this repo's Checkstyle config. Use whenever writing, modifying or reviewing Java code in src/main or src/test.
---

# Java coding standard

Source: [se-edu Java coding standard](https://se-education.org/guides/conventions/java/index.html). Checkstyle config: `config/checkstyle/checkstyle.xml`. Checkstyle does not catch everything below — follow all of it.

## Naming

* Packages: all lowercase, e.g. `seedu.address.model.person`.
* Classes/enums: **nouns** in `PascalCase`.
* Variables: `camelCase`; methods: **verbs** in `camelCase` (`getName()`, `computeTotalWidth()`).
* Constants: `SCREAMING_SNAKE_CASE`; group related constants with a common prefix (`COLOR_RED`, `COLOR_GREEN`).
* Abbreviations are not uppercased: `exportHtmlSource()`, not `exportHTMLSource()`.
* Booleans use `is`/`has`/`was`/`can`/`should`: `isSet`, `hasData`, `canEvaluate()`. Setter: `void setFound(boolean isFound)`.
* Collections are plural: `List<Person> persons`.
* Name length proportional to scope; `i`, `j`, `k` only for loop counters.
* English only, American spelling.
* Test methods: `featureUnderTest_testScenario_expectedBehavior()`.

## Layout

* Indent with **4 spaces**, never tabs. Wrapped lines indent **8 spaces**.
* Line length: soft limit **110**, hard limit **120**.
* K&R ("Egyptian") braces — opening brace on the same line.
* Wrap: break **after** commas, **before** operators (`+`, `&&`, `.`, `?`, `:`); keep method name attached to `(`; prefer higher-level breaks.
* Whitespace: around binary operators and `=`; after reserved words (`if (`, `for (`); after commas and `for` semicolons.
* One blank line between logical units within a block.
* `switch`: `case` indented one level inside `switch`; explicit `// Fallthrough` comment when falling through. Arrow form `case A -> ...` is fine.

```java
if (condition) {
    statements;
} else if (otherCondition) {
    statements;
} else {
    statements;
}

try {
    statements;
} catch (IOException e) {
    statements;
} finally {
    statements;
}
```

## Statements

* Every class is in a package. **No wildcard imports.**
* Import order (Checkstyle `CustomImportOrder`): static imports → `java.*`/`javax.*` → `org.*` → everything else (e.g. `seedu.*`), blank line between groups, alphabetical within groups.
* Class layout order: Javadoc → declaration → static variables (public → private) → instance variables (public → private) → constructors → methods.
* Modifier order: `public static abstract synchronized transient final native`, access modifier first.
* Array brackets on the type: `int[] a`.
* Initialise variables where declared; declare in the smallest scope.
* No public non-final fields (except simple data classes).
* Use `this.` only to disambiguate from a parameter.
* Always use braces for `if`/`else`/`for`/`while`/`do`, even single statements. Put the condition on its own line, not on the same line as the statement.
* Avoid magic numbers/strings — use named constants.
* Prefer `Optional`, immutability, and defensive checks (`requireNonNull`) as AB3 does.

## Comments and Javadoc

* Javadoc **required** on all public classes and public methods, and non-trivial private methods. Optional for trivial getters/setters, `@Override`s with unchanged contract, and test classes/methods.
* First sentence is a summary starting with a verb in third person: `Returns …`, `Adds …`.

```java
/**
 * Returns the person with the given index.
 *
 * @param index Zero-based index of the person.
 * @return The person at {@code index}.
 * @throws IndexOutOfBoundsException If the index is invalid.
 */
```

* `/**` on its own line, aligned `*`, space after `*`, blank line before tags.
* Document **all** `@param`s or none. `@return` may be omitted if obvious.
* Short form allowed: `/** Returns the name. */`.
* Comments are indented to the code they describe; explain _why_, not _what_. Remove commented-out code.

## Workflow

1. Match the style of surrounding AB3 code.
1. Run `./gradlew checkstyleMain checkstyleTest` and fix every warning.
1. Self-review against the checklist above (naming, braces, Javadoc, imports, line length).
