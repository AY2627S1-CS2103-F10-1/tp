# Checkstyle

Adapted from the [SE-EDU Checkstyle tutorial](https://se-education.org/guides/tutorials/checkstyle.html), checked on 28 October 2026. See [LICENSE](LICENSE) for upstream attribution.

Checkstyle analyses Java source against configured style rules. HRvest already supplies the rules in `config/checkstyle/checkstyle.xml` and targeted suppressions in `config/checkstyle/suppressions.xml`. Its Gradle build configures the tool version; do not install or depend on a separate system Gradle.

## Run the checks

From the repository root in PowerShell:

```powershell
.\gradlew.bat checkstyleMain checkstyleTest
```

The complete project verification also runs tests:

```powershell
.\gradlew.bat check
```

Inspect the reports in `build/reports/checkstyle/main.html` and `build/reports/checkstyle/test.html` when a check fails. Correct the source, then rerun the applicable tasks. Do not weaken a rule or add a suppression just to obtain a passing build.

## IDE setup

Import the project as a Gradle project and use its wrapper. If using the Checkstyle plugin in IntelliJ IDEA or VS Code, select the repository's existing configuration and a tool version compatible with the Gradle configuration. Run the wrapper tasks before handing work back even when the IDE reports no violations.

IDE formatting and Checkstyle are aids, not substitutes for reviewing [the Java conventions](java.md). Keep explanations, naming, and behavior consistent with the standard where automated checking is incomplete.
