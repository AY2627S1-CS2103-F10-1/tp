# tP Feature Workflow Procedure

## Investigate

Treat the user's requested deliverable as one issue. Read the current Team GitHub Workflow in the team wiki and inspect the relevant repository and GitHub state needed to plan that issue, its branch, and the requested work. These read-only steps do not need approval.

1. Read the current repository `AGENTS.md` and applicable project skills. Check `git status`, the current branch, remotes, and existing local work. Do not overwrite, discard, or silently switch away from unrelated work.
1. Verify the current authenticated GitHub identity and access using the available authenticated GitHub CLI or tool. Never copy credentials from another machine or from this reference.

## Plan and announce the work

Plan the user's requested deliverable as one issue and one branch. Do not split it into multiple issues or create issues for routine Git steps such as pushing.

Settle the issue title, branch name, work scope, owner, labels, and milestone from the available evidence. Announce this concrete plan and ask for one explicit confirmation before creating or changing workflow objects. A request to implement the feature does not replace confirmation of the plan. After confirmation, carry out the approved plan without another approval gate. If the plan cannot be carried out within its approved scope, stop and explain what changed.

Infer routine metadata from current evidence:

* Assign the issue to one responsible member. If the user says to assign it to them, use the verified current GitHub login.
* Choose available `type.*` and `priority.*` labels from the task meaning and current team examples; omit labels that do not help.
* Use the clearly active iteration milestone. Do not create a milestone or change a milestone deadline for a feature request.
* Ask one concise, bundled question only when task boundaries, owner, or milestone cannot be responsibly inferred and the uncertainty materially affects the work. Do not ask for fields already established by repository or team evidence.

## Create the issue and fork branch

Create the issue in `AY2627S1-CS2103-F10-1/tp` with a descriptive title and brief, non-redundant body. Apply the inferred owner, useful labels, and active milestone. Issues are encouraged rather than mandatory for PRs generally, but this feature-start workflow uses one issue to track the requested deliverable.

Create one local branch in the fork checkout named `<issue-number>-<short-kebab-task-name>`, based on the latest team `master`. Confirm that `origin` is the user’s fork and `upstream` is the team repository. Follow the currently published team workflow if the team has moved from the early fork-based workflow to a centralized workflow. Preserve uncommitted work and existing branches; if the deliverable already has uncommitted changes on `master`, carry them onto the task branch without discarding them. If the checkout is dirty or based on an unexpected branch, find a safe way to create the new branch without disturbing unrelated work. Publish the branch to the fork after making a meaningful commit.

## Implement and prepare the PR

Continue with implementation when the user requested implementation. Otherwise, stop after issue and branch setup and report what is ready. Follow the repository’s ordinary coding, testing, documentation, and pre-commit instructions; this skill does not replace them.

When implementation is complete, use the team’s commit conventions, run the applicable repository checks, and push the task branch to the user’s fork. Open one PR against the team repository’s `master` when there is a meaningful change. Match the issue title, include `Fixes #<issue-number>` when applicable, assign the PR to the iteration milestone, and request `@developers` as reviewer. Use a draft PR if the work is not ready. Do not merge.

## Report

Report the issue, branch, and PR links created or reused; explain metadata inferred from live evidence; list checks run and any failures or incomplete steps; and ask only for inputs still needed from the user. Never claim an action succeeded unless the tool or command confirmed it.

## Current workflow sources

Recheck the [team wiki’s Team GitHub Workflow](https://github.com/AY2627S1-CS2103-F10-1/tp.wiki/blob/master/Home.md) when the workflow runs; treat it as the source of truth for this workflow.
