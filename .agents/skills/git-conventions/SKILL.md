---
name: git-conventions
description: Team git conventions for commit messages, branch names, and pull requests (opening, reviewing, merging), based on the se-edu git guide and course Appendix E. Use whenever writing a commit message, creating a branch, or preparing, reviewing, or merging a pull request.
---

# Git conventions

Source: [se-education.org git conventions](https://se-education.org/guides/conventions/git.html).

## Commit message subject

* Every commit **must** have a well-written subject line.
* Limit to **50 characters** (hard limit **72**).
* Use the **imperative mood**: `Add README.md`, not `Added README.md` or `Adding README.md`. Test: "If applied, this commit will _\<subject\>_."
* **Capitalise** the first letter.
* **No trailing period.**
* Start every subject with a mandatory category prefix followed by a colon and space. Use `feat`, `fix`, `refactor`, `doc`, `test`, `version`, `dbg`, `hack` (avoid), `WIP`, or `chore`; for example, `feat: Add email field` or `chore: Update release date`. The text after the prefix still follows the rules above.

Team tag vocabulary (mandatory prefix, from issue #1 discussion): `feat`, `fix`, `refactor`, `doc`, `test`, `version`, `dbg`, `hack` (avoid), `WIP`, `chore`.

## Commit message body

* Separate subject and body with a **blank line**.
* **Wrap at 72 characters.**
* Use blank lines between paragraphs; bullet points are fine.
* Explain **WHAT and WHY, not HOW** — the diff shows how.
* Recommended structure:
    1. Current situation — present tense. Don't write "currently" or "originally".
    1. Why it needs to change.
    1. What is being done — imperative mood, often starting with "Let's …".
    1. Why it is done that way.
    1. Any other relevant info (links, issue refs such as `Fixes #12`).
* The body is optional. When present, follow the rules above.

### Example

```
Person attribute classes: Extract a parent class PersonAttribute

Person attribute classes (e.g. Name, Address, Age) have some common
behaviours (e.g. isValid()).

The common behaviours across person attribute classes cause code
duplication.

Extracting the common behaviour into a super class allows us to use
polymorphism when dealing with person attributes. For example,
validity checking can be done for all attributes in one loop.

Let's pull up behaviours common to all person attribute classes into
a new parent class named PersonAttribute.

Using inheritance is preferable over composition here because the
common behaviours are not composable.
```

## Branch names

* Meaningful, **kebab-case**: `refactor-ui-tests`.
* For issue work: `issueNumber-keywords-from-title`, e.g. `1234-ui-freeze-error`.

## Commit hygiene

* One logical change per commit; don't mix refactoring with behaviour changes.
* Never commit secrets, generated files (`build/`, `data/`, logs) or IDE files.
* Review `git diff --staged` before every commit.
* Don't use `--no-verify` to skip hooks.

## Pull requests

Source: [Appendix E: GitHub project management](https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixE-gitHub.html).

* Open each PR from its own branch of your fork, never from `master`. Prefer several small PRs over one large PR.
* Make the PR title the same as its issue's title, include `Fixes #<issue>`, and assign the PR to the iteration milestone. Use a draft PR for work in progress.
* Review teammates' PRs seriously: leave comments at specific lines of the diff, not only an overall comment, and approve (`LGTM`) only after reading the change.
* Don't merge a PR whose CI is failing, unless the failure is confirmed to be safe to ignore.
* Merge with a merge commit; don't rebase or squash, because that changes commit timestamps that progress tracking relies on.
* Don't delete a PR's branch after merging; the course's scripts check that branching was used.
* After a merge, sync your fork (`git pull upstream master`, then `git push origin master`) and merge `master` into your unmerged branches.

## Agent rules

* Only commit, push, or open PRs when the user asks.
* Never force-push shared branches or rewrite `master` history.
* Draft commit messages and PR descriptions for the user; don't post comments on issues or PRs on the user's behalf.
* Verify a message with: subject length `git log -1 --format=%s | awk '{print length}'`.
