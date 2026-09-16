---
name: pre-commit-checks
description: Run and interpret the full local verification (repo text checks, Checkstyle, tests, commit message, secret scan) before committing or opening a PR. Use before any commit, when asked to verify work, or when setting up the team git hooks.
---

# Pre-commit checks

## One-time setup (per clone)

```shell
git config core.hooksPath .githooks
```

On macOS/Linux also run `chmod +x .githooks/*` if the hooks are not executable. On Windows the hooks run in Git Bash (bundled with Git for Windows).

This enables:

* `.githooks/pre-commit` — secret scan on staged changes, repo text checks, Checkstyle and tests.
* `.githooks/commit-msg` — checks the subject line follows the `git-conventions` skill.

Set `SKIP_GRADLE=1` to skip only the Gradle step for doc-only commits (e.g. `SKIP_GRADLE=1 git commit`). Do **not** use `git commit --no-verify`.

## Manual verification (what the hooks run)

1. `git diff --staged` — review every changed line; no secrets, debug prints, or unrelated changes.
1. `./.github/run-checks.sh` — trailing whitespace, EOF newline, CRLF line endings (same as CI).
1. `./gradlew checkstyleMain checkstyleTest` — zero Checkstyle violations.
1. `./gradlew test` — all tests pass; new behaviour has tests (see `writing-tests`).
1. Optional: `./gradlew check coverage` — mirrors CI exactly.
1. Commit message follows `git-conventions` (imperative, capitalised, no period, ≤ 72 chars, blank line before body).

## Interpreting failures

* Checkstyle report: `build/reports/checkstyle/main.html` and `test.html`. Fix the code; never edit `config/checkstyle/` to silence it.
* Test report: `build/reports/tests/test/index.html`.
* Line-ending errors: ensure your editor uses LF; `git config core.autocrlf input` (macOS/Linux) or `true` (Windows).
* A false-positive secret scan match (e.g. a fake token in a test): rename the value so it is obviously fake, rather than bypassing the hook.

## Agent rules

* Report the actual command output; never claim checks pass without running them.
* If checks cannot be run (e.g. no JDK 25 available), say so explicitly.
