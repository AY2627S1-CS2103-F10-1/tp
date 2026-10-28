# Git conventions

Adapted from the [SE-EDU Git conventions](https://se-education.org/guides/conventions/git.html), checked on 28 October 2026. See [LICENSE](LICENSE) for upstream attribution.

## Commit subjects

* Write a meaningful subject for every commit.
* Aim for at most 50 characters; never exceed 72.
* Use an imperative verb, capitalise the subject text, and omit its final period.
* Project choice: begin with a mandatory category and a colon, for example `fix: Preserve candidate data`.
* Existing categories are `feat`, `fix`, `refactor`, `doc`, `test`, `version`, `dbg`, `hack`, `WIP`, and `chore`. Avoid `hack` for finished work.

The upstream guide allows an optional scope or category; this repository requires a category. If a contributor includes a commit body, separate it from the subject with a blank line, wrap it at 72 characters, and explain the problem and reason for the change. Put issue-closing references and validation detail in the PR description when the commit uses a subject only.

## Change boundaries

Keep each commit focused on one logical change. Separate standalone documentation and build-configuration changes from implementation changes. Review the staged diff before committing, and exclude generated files, logs, local data, credentials, and unrelated edits. Leave the repository hooks enabled.

## Branches and pull requests

Project choice: use meaningful kebab-case branch names. For issue work, start with the issue number, such as `123-preserve-candidate-data`.

Follow the [team workflow](https://github.com/AY2627S1-CS2103-F10-1/tp/wiki) for fork branches, review requests, milestones, and merging. A PR description should explain the final behavior and relevant verification. Keep issue tracking and release status out of product documentation.
