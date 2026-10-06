---
name: pe-bug-prevention
description: Avoid the functionality bugs and feature flaws that cost marks in the CS2103/T Practical Exam (PE). Use when designing, implementing, or reviewing a user-facing command, parser, input validation, error message, duplicate detection, search, data-file handling, or the UG section that describes them; do not use for docs-only changes unrelated to product behaviour.
---

# PE bug prevention

In the PE, other teams test the product and file bugs against it; the teaching team triages them as `type.FunctionalityBug`, `type.FeatureFlaw`, or `type.DocumentationBug`. Design every user-facing behaviour so that it would survive that testing. Apply this skill together with `code-quality`, `writing-tests`, and `secure-coding`.

Sources: [Week 8 tP page](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week8/project.html) and the PE bug-triaging guidelines it extracts.

## Judge validity from the user's point of view

Overzealous input validation is the most common tP flaw. Define "valid" by what a real user might legitimately enter, not by the narrowest data type.

* **Warn rather than block** when an input deviates from the expected format but cannot hinder the software's operation. Block only inputs that would break parsing, storage, or later commands, and either block or warn on every potentially harmful input.
* Accept real-world variations. For example, a phone field should accept `1234 5678 (HP) 1111-3333 (Office)`, and a date field should accept a past interview date for record-keeping (optionally with a warning).
* Restrict characters only when there is a parsing reason, and say so in the UG. Even then, a restriction that rejects legitimate values is a flaw: rejecting `/` in names blocks legal names such as `Raj s/o Kumar`. AB3's `ArgumentTokenizer` only recognises a prefix after whitespace, so allow `/` in values and avoid prefixes that collide with common values (such as `s/`).
* Set length or size limits only when they are generous for the target user. An 8-digit phone limit is unreasonable unless every target user has such a number.

## Survive user mistakes

Deliberate sabotage (for example, a 30-digit phone number) is not a bug, but any input a user could produce by mistake must not crash the app, corrupt data, or make it unusable.

* Handle missing spaces between parameters, repeated prefixes, empty values, extra whitespace, and wrong-case keywords gracefully.
* Parse numbers and indexes without overflow; an index such as `99999999999` must produce an error message, not an exception.
* Keep very long values usable in the UI. Wrap or truncate them so that the distinguishing part stays visible; showing only the first few characters of a long name hinders the user and raises the severity.
* Keep terminal output presentable. Do not print stack traces, alarming warnings, or misleading messages during normal use.

## Write specific error messages

* State the exact reason and the expected format, for example `Index must be a positive integer, but got "0"` rather than `Invalid command`.
* Do not list several possible causes when the parser knows which one occurred.
* Distinguish a format error (`2021-13-28`: the month must be 01 to 12) from an invalid value (`2021-02-30`: February has no 30th day). A combined `Invalid date or incorrect format` message is acceptable only if separating them is not worth the effort.

## Keep commands fast to type

* Use one-shot commands, not multi-step prompts. Use a multi-step flow only as an optional aid for new users or for rare multi-step tasks such as importing a file.
* Keep keywords short, but not cryptic. Where a keyword is both long and frequently used, support a short alias as well (as Git does with `--no-verify` and `-n`).
* Make command words and prefixes case-insensitive unless case carries meaning.
* Avoid hard-to-type special characters in command formats when a plain alternative works.

## Match real-world case sensitivity and usefulness

* Follow the case sensitivity of the real-world entity: person names and search keywords are case-insensitive.
* Make search useful by default: match case-insensitively and combine keywords with OR, so `find Alice Richards` returns both `Alice Davidson` and `Alison Richards`.
* A feature that works but is less useful than it could easily be (from the end user's view) is a flaw; when the better behaviour costs little extra effort, implement it.

## Detect duplicates realistically

* Treat values that differ only in case or whitespace as potential duplicates: `John Doe`, `john doe`, and `John  Doe` are probably the same person.
* For near-matches that might be distinct people, warn and let the user decide rather than silently accepting or blocking.
* State in the UG exactly what counts as a duplicate, so users are not led to believe the app catches cases it does not.

## Keep the UG and the product consistent

* When behaviour changes, update the UG in the same PR. A UG/product mismatch is a bug either way: `FunctionalityBug`/`FeatureFlaw` if the product is wrong, `DocumentationBug` if the UG is wrong.
* Document every input restriction, limit, warning, and duplicate rule a user could run into.

## Preserve data-file editability

* Keep the data file human-editable (Constraint-Human-Editable-File).
* Support manual edits at least as well as AB3: correct edits must load, and the UG must warn that invalid edits can cause data loss. Do not promise more support in the UG than the product delivers.
* Loading an invalid file must not crash the app.

## Review checklist

Before declaring a user-facing change done, confirm each item and add tests for the cases that apply:

* [ ] Legitimate real-world values are accepted; deviations that cannot cause harm produce a warning, not a rejection.
* [ ] Any character or length restriction has a parsing justification and a generous limit, and is documented in the UG.
* [ ] Typing mistakes (missing spaces, huge numbers, empty or repeated fields) produce a specific error and leave the data intact.
* [ ] Each error message names the specific problem and the expected format.
* [ ] Command words, prefixes, names, and search keywords are case-insensitive where the real-world entity is.
* [ ] Duplicate detection ignores case and extra whitespace, and the UG states its limits.
* [ ] Long values remain readable in the UI.
* [ ] The UG describes the behaviour exactly as implemented.
