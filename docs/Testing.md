---
  layout: default.md
  title: "Testing guide"
  pageNav: 3
---

# Testing guide

<!-- * Table of Contents -->
<page-nav-print />

<!-- -------------------------------------------------------------------------------------------------------------------- -->

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<box type="info" seamless>

**Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</box>

### Windows symbolic-link tests

The storage regression tests create real symbolic links, including relative links and links to missing files. On Windows, enable **Developer Mode** before running the suite with JDK 25. This lets Java create the links from an ordinary PowerShell or IntelliJ session. See [Microsoft's symbolic-link prerequisites](https://learn.microsoft.com/en-us/windows/win32/api/winbase/nf-winbase-createsymboliclinkw).

If link creation fails with `A required privilege is not held by the client`, check Developer Mode and the permissions of the process running the tests. A restricted test runner may still deny link creation. Run the tests from a session that permits it; these tests must exercise real links to verify storage recovery and collision handling.

After changing Windows permissions, restart the IDE and stop the existing Gradle daemon before rerunning the suite from PowerShell:

```powershell
.\gradlew.bat --stop
.\gradlew.bat clean check coverage shadowJar
```

### Windows display scaling

The feedback layout tests run on Windows with the native JavaFX toolkit. `check` also runs them at 150% and 175% scaling through `testWindowsScale150` and `testWindowsScale175`, with separate reports and coverage data for each run. These two tasks are skipped on other operating systems.

To check a scale without changing Windows Settings, pass it to the test JVM through Gradle:

```powershell
.\gradlew.bat test --tests 'seedu.address.ui.ResultDisplayTest' '-PjavaFxUiScale=1.5'
.\gradlew.bat test --tests 'seedu.address.ui.ResultDisplayTest' '-PjavaFxUiScale=1.75'
```

Run the full suite without `javaFxUiScale` to use the machine's normal display settings.

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`
