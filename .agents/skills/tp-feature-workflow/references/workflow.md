# tP Feature Workflow Procedure

## Before starting

1. Read the current repository `AGENTS.md` and applicable project skills. Check `git status`, the current branch, remotes, and existing local work. Do not overwrite, discard, or silently switch away from unrelated work.
1. Read the current Team GitHub Workflow in the team wiki and follow its current instructions.
1. Verify the current authenticated GitHub identity and access using the available authenticated GitHub CLI or tool. Never copy credentials from another machine or from this reference.
1. Inspect the team repository’s open issues, labels, milestones and due dates, comparable issues and PRs, and the user’s fork branches. Search for a matching issue before creating one. Reuse an appropriate existing open issue rather than making a duplicate.

## Plan and announce the work

Break the concrete feature into independently mergeable issues sized for one person to complete in a few hours. Keep work breadth-first so every merge leaves a working, improved product. Create issues for feature work, not routine Git steps such as pushing. Split shared work into single-owner tasks and represent their relationship with GitHub sub-issues when available.

Before workflow investigation or mutation, tell the user the planned issue titles and branch names, or explain what remains uncertain, then wait for explicit confirmation. If investigation later changes the planned task boundaries or object count, update the user and get confirmation of the revised plan before creating or changing GitHub objects.

Infer routine metadata from current evidence:

* Assign each issue to one responsible member. If the user says to assign it to them, use the verified current GitHub login.
* Choose available `type.*` and `priority.*` labels from the task meaning and current team examples; omit labels that do not help.
* Use the clearly active iteration milestone. Do not create a milestone or change a milestone deadline for a feature request.
* Ask one concise, bundled question only when task boundaries, owner, or milestone cannot be responsibly inferred and the uncertainty materially affects the work. Do not ask for fields already established by repository or team evidence.

## Create issue(s) and fork branch(es)

Create each issue in `AY2627S1-CS2103-F10-1/tp` with a descriptive title and brief, non-redundant body. Apply the inferred owner, useful labels, and active milestone. Issues are encouraged rather than mandatory for PRs generally, but this feature-start workflow uses an issue to track each planned task.

For each issue, create a separate local branch in the fork checkout named `<issue-number>-<short-kebab-task-name>`, based on the latest team `master`. Confirm that `origin` is the user’s fork and `upstream` is the team repository. Follow the currently published team workflow if the team has moved from the early fork-based workflow to a centralized workflow. Preserve uncommitted work and existing branches; if the deliverable already has uncommitted changes on `master`, carry them onto the task branch without discarding them. If the checkout is dirty or based on an unexpected branch, find a safe way to create the new branch without disturbing unrelated work. Publish the branch to the fork after making a meaningful commit.

## Implement and prepare the PR

Continue with implementation when the user requested implementation. Otherwise, stop after issue and branch setup and report what is ready. Follow the repository’s ordinary coding, testing, documentation, and pre-commit instructions; this skill does not replace them.

When implementation is complete, use the team’s commit conventions, run the applicable repository checks, and push the task branch to the user’s fork. Open a PR against the team repository’s `master` when there is a meaningful change. Match the issue title, include `Fixes #<issue-number>` when applicable, assign the PR to the iteration milestone, and request `@developers` as reviewer. Use a draft PR if the work is not ready. Keep each task on its own branch and PR. Do not merge.

## Report

Report the issue, branch, and PR links created or reused; explain metadata inferred from live evidence; list checks run and any failures or incomplete steps; and ask only for inputs still needed from the user. Never claim an action succeeded unless the tool or command confirmed it.

## Current workflow sources

Recheck the [team wiki’s Team GitHub Workflow](https://github.com/AY2627S1-CS2103-F10-1/tp.wiki/blob/master/Home.md) when the workflow runs; treat it as the source of truth for this workflow.
