---
name: writing-tests
description: How to write JUnit 5 tests in this AB3-based repo with good coverage and consistent style. Use when adding a feature, fixing a bug, or adding/reviewing tests.
---

# Writing tests

References: `docs/Testing.md`, [se-edu Java coding standard](https://se-education.org/guides/conventions/java/index.html). Tests must obey the same Java coding standard and Checkstyle rules as production code.

## Where tests go

* Mirror the production package: `src/main/java/seedu/address/logic/commands/FooCommand.java` → `src/test/java/seedu/address/logic/commands/FooCommandTest.java`.
* Class name: `<ClassUnderTest>Test`.
* Reuse helpers in `src/test/java/seedu/address/testutil/` (`PersonBuilder`, `TypicalPersons`, `TypicalIndexes`, `Assert.assertThrows`) and `CommandTestUtil` (`assertCommandSuccess`, `assertCommandFailure`). Add new builders/typical data there rather than duplicating setup.
* Test data files go under `src/test/data/<TestClassName>/`. Write temp files only to `src/test/data/sandbox/` or JUnit `@TempDir`.

## Naming

`featureUnderTest_testScenario_expectedBehavior()`, e.g.:

* `execute_validIndexUnfilteredList_success()`
* `parse_missingPrefix_throwsParseException()`
* `equals_differentName_returnsFalse()`

## What to test

For every new or changed behaviour:

1. **Happy path** — typical valid input.
1. **Boundaries** — empty, one, max, first/last index, `0`, `-1`, size + 1.
1. **Invalid input** — null, blank, malformed, wrong prefix, duplicate; assert the exact exception and message.
1. **Equivalence partitions** — at least one value from each partition.
1. `equals`/`hashCode`/`toString` for new model/command classes.
1. Parser tests (`XYZCommandParserTest`) using `CommandParserTestUtil`.
1. Storage round-trips (`JsonAdapted*Test`), including corrupt/invalid JSON.
1. A regression test that fails before a bug fix and passes after it.

## Style

* One behaviour per test; arrange–act–assert with blank lines between phases.
* Use `assertEquals(expected, actual)` in that order.
* Use `Assert.assertThrows` from `testutil` for exceptions.
* No `Thread.sleep`, network, randomness, or dependence on test order/system locale/time.
* No `System.out` in tests; no `@Disabled` without a linked issue.
* Tests don't need Javadoc, but a class-level comment is welcome for non-obvious setups.

## Coverage

* Run `./gradlew test coverage`; open `build/reports/jacoco/coverage/html/index.html`.
* New/changed code should be covered by tests (aim for all branches of new logic). Don't write assertion-free tests just for coverage.

## Workflow

1. Write or update the test (ideally first).
1. `./gradlew test --tests "<fully.qualified.TestClass>"`.
1. `./gradlew check` — all tests and Checkstyle must pass before done.
1. Never delete, weaken, or disable an existing test to make the build pass; fix the code or ask.
