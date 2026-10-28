# Repository conventions

These files are the checked-in conventions for HRvest. Read the relevant file before writing code, a commit message, or a review comment. The project skills describe workflows; these documents describe the standards those workflows apply.

| File | Scope | Upstream source |
| --- | --- | --- |
| [git.md](git.md) | Commit messages, branches, and focused changes | [Git conventions](https://se-education.org/guides/conventions/git.html) |
| [java.md](java.md) | Java naming, layout, statements, and comments | [Java coding standard](https://se-education.org/guides/conventions/java/index.html) |
| [logging.md](logging.md) | Log levels and useful, safe diagnostics | [Java logging conventions](https://se-education.org/guides/conventions/java/logging.html) |
| [checkstyle.md](checkstyle.md) | Gradle and IDE checks | [Using Checkstyle](https://se-education.org/guides/tutorials/checkstyle.html) |

## Project choices

* Apply the SE-EDU basic and intermediate Java rules. Use the [Google Java style guide](https://google.github.io/styleguide/javaguide.html) for topics they do not cover.
* Retain the existing `seedu.address` package hierarchy of this AB3-based project.
* Use the existing mandatory commit category prefixes and issue-number branch names described in [git.md](git.md).
* Use Java 25 and the checked-in Gradle wrapper. Run `gradlew.bat` from PowerShell on Windows.
* Keep Checkstyle's configured rules and versions in the existing build configuration; these documents do not change them.
* Diagnose operations with `LogsCenter`, without writing candidate contents into logs.

## Maintaining this copy

The [SE-EDU guides](https://se-education.org/guides/) are the upstream authority. These documents adapt their rules to the repository and identify project choices separately. Sources were checked on 28 October 2026. When an upstream rule changes, update the relevant local file and explain that update in the change summary. Review code against the checked-in version so that contributors use the same standards.

The adapted upstream material is distributed under the [SE-EDU guides MIT license](LICENSE). Keep that attribution when copying or updating it.
