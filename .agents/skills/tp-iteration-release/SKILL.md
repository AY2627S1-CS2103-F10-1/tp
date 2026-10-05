---
name: tp-iteration-release
description: Wrap up a tP iteration on GitHub and publish its product release, from reconciling the milestone through building and smoke-testing the JAR, release notes, tagging, and closing the milestone. Use when asked to prepare, check, or publish an iteration release (such as v1.3) or to close a milestone; do not use for feature work, which `tp-feature-workflow` covers.
---

# tP iteration release

From v1.3 onward, an iteration not wrapped up properly by its deadline turns the `v1._ on time` progress item red permanently. The teaching team also reads the release notes to check that (a) they are written reasonably well and (b) the features they describe show the product has reached MVP level.

Sources: [Week 9 tP page](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week9/project.html), [Appendix E: GitHub project management](https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixE-gitHub.html), and [DevOps guide: making a release](https://nus-cs2103-ay2627-s1.github.io/tp/DevOps.html#making-a-release).

## Approval

Editing milestones, moving issues, pushing tags, creating releases, and closing milestones are outward-facing team actions. Investigate and prepare everything first, then present the full plan (milestone changes, version, release notes draft, smoke-test results) and get one explicit confirmation before changing anything on GitHub. Publish the release as a draft first so the team can review it.

## Procedure

1. **Check the deadline.** Read the milestone's due date with `gh api repos/AY2627S1-CS2103-F10-1/tp/milestones`. Leave enough time for a teammate to review the draft release before the deadline.
1. **Reconcile the milestone with the product.** List its issues and PRs (`gh issue list --milestone <version> --state all` and `gh pr list --state all --search "milestone:<version>"`). Every PR merged in this iteration, and the issue it closed, must be in the milestone. Move unfinished issues and PRs to the next milestone; do not delete or close them to make the iteration look complete.
1. **Confirm that `master` is green.** The latest CI run on team `master` must pass, and `./gradlew clean check` must pass locally. Do not release from a failing build.
1. **Bump the version.** Update `VERSION` in `src/main/java/seedu/address/MainApp.java` to the iteration version through an ordinary PR (for example, `version: Update version to v1.3`), and wait for it to be merged.
1. **Build the JAR.** Run `./gradlew clean shadowJar` on an up-to-date checkout of team `master`; the output is `build/libs/addressbook.jar`. Avoid drastic changes to `build.gradle` while doing so.
1. **Smoke-test the JAR**, not just the IDE run:
    1. Copy the JAR into a new, empty folder and run `java -jar addressbook.jar` with Java 25, the version stated in `AGENTS.md`.
    1. Confirm that the app starts with sample data and creates its data and preference files beside the JAR.
    1. Run every command described in the UG at least once, including one invalid input per new command.
    1. Exit, relaunch, and confirm that data persisted.
    1. Check that the terminal output shows no stack traces or alarming messages.
    1. Ask a teammate on a different OS to repeat the start-up check if possible.
1. **Draft the release notes** (see the next section) and capture screenshots or a short screen recording of the product in action.
1. **Tag and create a draft release** on the exact commit the JAR was built from:

    ```shell
    gh release create v1.3 build/libs/addressbook.jar --repo AY2627S1-CS2103-F10-1/tp --target <commit-sha> --title "v1.3" --notes-file <notes-file> --draft
    ```

    Add the screenshots in the GitHub release editor, have a teammate review the draft, then publish it.
1. **Close the milestone** only after the release is published and every remaining item has moved on: `gh api -X PATCH repos/AY2627S1-CS2103-F10-1/tp/milestones/<number> -f state=closed`.
1. **Plan the next iteration.** Create issues for the next milestone's tasks, assign each to its owner and to the milestone, and revise the target if this iteration slipped.

## Release notes

Write fairly detailed notes for a reader who has not seen the product:

* Describe what has changed compared to AB3, not only since the previous release, so that the MVP is visible.
* Give each feature a heading, one sentence on what it does for the target user, and an example command.
* Include screenshots or a screen recording of the product in action.
* List known limitations and features that are not yet complete, honestly.
* Link to the User Guide, and state how to run the JAR (`java -jar addressbook.jar`, with the required Java version).

## Don't

* Don't release from a failing build or with failing tests skipped.
* Don't use the `javax.web` library (or a WebView) just to show the user guide as a web page.
* Don't re-tag or delete a published release; if something is wrong, fix it and publish a patch release (for example, `v1.3.1`).
* Don't upload generated files other than the JAR, such as `data/`, logs, or `preferences.json`.
