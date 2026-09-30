---
name: code-quality
description: Review and improve CS2103/T implementation quality in non-trivial Java changes. Use when writing, modifying, or reviewing Java production or test code, including pre-merge reviews; do not use for docs-only, Git-only, configuration-only, or purely mechanical formatting changes.
---

# Code quality

Apply this skill together with `java-coding-standard`. Use `writing-tests` for test design and `secure-coding` when the change handles input, storage, logging, dependencies, or user data.

Review changed code in its surrounding context. Preserve the existing architecture and prefer the smallest change that makes the code easier to understand, safer to modify, or less likely to fail.

## Review criteria

### Readability and structure

* Keep methods focused. Treat a method beyond about 30 lines, deep nesting, or a hard-to-follow happy path as a prompt to extract steps or use guard clauses when that improves clarity.
* Apply the Single Level of Abstraction Principle (SLAP): statements in a method should normally operate at one level of abstraction and at the highest useful level.
* Replace complicated expressions, repeated negations, clever shortcuts, and unexplained literals with clear intermediate values, straightforward control flow, and named constants where appropriate.
* Arrange related statements in a logical order so the code reads as a coherent sequence. Avoid premature optimization unless measurements or requirements justify the complexity.
* Use names that accurately explain purpose: nouns for things, verbs for actions, plural names for collections, and boolean names that read as conditions. Avoid vague, misleading, ambiguous, or numbered names.

### Reliability and error handling

* Use exceptions for unusual runtime conditions, not ordinary control flow. Do not swallow failures in empty `catch` blocks.
* Use Java assertions for programmer assumptions and invariants, not for validating user input or performing required work. Assertion expressions must be free of side effects because assertions can be disabled.
* Add useful logging at significant operations and failure boundaries when it will help diagnose behavior. Use the repository's logging facilities and appropriate levels; do not add noisy logs or expose personal data.
* Defend important invariants at object and component boundaries with validation, non-null checks, immutable data, defensive copies, or coordinated association updates as appropriate. Choose a level of defensiveness proportional to the risk and complexity.
* Handle all meaningful branches explicitly. A final `else` or `default` must represent every remaining case or reject an impossible state clearly.

### Maintainability

* Remove noticeable copy-paste-modify duplication when one clear shared abstraction can express the common behavior, especially in test setup and assertions. Do not introduce an abstraction that is harder to understand than the duplication it replaces.
* Give each variable one purpose, do not recycle parameters as local variables, and keep variable scope as small as practical.
* Remove dead code. Keep comments minimal and useful: explain the contract or rationale (what and why), while making the implementation itself explain how.

## Applying the rubric

The CS2103/T rubric asks for some evidence of logging, exceptions, assertions, and defensive coding across the implementation. Do not require every mechanism in every file or pull request; flag an absence only when the changed feature has a concrete place where the mechanism is useful.

For reviews, report only actionable findings with a location, the concrete readability, correctness, or maintenance consequence, and a proportionate fix. Do not report subjective alternatives that offer no clear improvement, duplicate findings already enforced by Checkstyle, or demand unrelated refactoring outside the change's scope.

Sources: [CS2103/T tP grading](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html), [Code Quality](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/codeQuality.html), and [Error Handling](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/errorHandling.html).
