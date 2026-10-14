---
name: documentation-quality
description: Write clear, appropriately scoped project documentation, including the README, User Guide, and Developer Guide. Use when creating or changing documentation content; use markdown-conventions for formatting-only edits.
---

# Documentation quality

Apply this skill together with `markdown-conventions` for Markdown and MarkBind syntax. Use `pe-bug-prevention` when documenting user-facing behavior, input restrictions, or data-file handling.

## Purpose and placement

* Read the destination section and analogous documentation before writing. Match their purpose and level of detail rather than adding a new section for every changed class or feature.
* Use the README and site introduction to explain the product and its value to the intended user. Keep claims consistent with the documented product scope.
* Put operating instructions, command syntax, and examples in the User Guide. Put requirements and design explanations in the relevant Developer Guide sections.
* Keep glossary entries focused on definitions. Put requirements and constraints in their own sections rather than embedding them in definitions.

## Consistency and precision

* Reuse established terms for the product, actors, entities, fields, and statuses. Check related sections and documents so a wording change does not leave competing names or descriptions.
* Write clear, professional prose and user stories with a specific user goal and benefit. Replace vague or informal expressions with the intended meaning.
* Describe use cases through actor actions, system responses, and relevant extensions. Distinguish actual preconditions from ordinary steps; follow the established use-case format.
* Make non-functional requirements measurable using an agreed target and the conditions under which it applies. Identify missing targets instead of inventing numbers or making unsupported promises.
* Write accurate command syntax and representative examples that explain the supported operation. Apply `pe-bug-prevention` for agreement between the User Guide and implemented behavior.

## Developer Guide implementation detail

Before adding an implementation subsection, identify the specific design question a developer needs answered and explain why the code alone does not sufficiently answer it. Useful explanations include why a simpler approach was rejected, a non-obvious constraint that determines the design, or an interaction or invariant that is difficult to understand from individual classes.

* Compare the proposed explanation with analogous features already in the guide. A new field or command does not automatically justify its own implementation subsection.
* Explain the relevant rationale, tradeoff, or consequence. Descriptions that only restate fields, parser steps, command execution, or method calls do not qualify on their own.
* If no concrete unanswered design question can be identified, omit the implementation subsection. Calling a feature noteworthy or complex is not sufficient justification.
* This criterion applies to implementation subsections, not to required user instructions, architecture updates, or testing documentation.
