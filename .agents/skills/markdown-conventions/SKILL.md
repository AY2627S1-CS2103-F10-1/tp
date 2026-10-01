---
name: markdown-conventions
description: se-edu Markdown coding standard for docs, README, AGENTS.md and skill files. Use whenever creating or editing a .md file.
---

# Markdown conventions

Source: [se-education.org Markdown coding standard](https://se-education.org/guides/conventions/markdown.html).

## Rules

* Follow [GitHub Flavored Markdown](https://github.github.com/gfm/).
* **Don't hard-wrap** lines at a fixed width; one sentence or paragraph per line so diffs stay meaningful.
* Blank line **before every list**.
* Blank line **before every code block**, and give fenced blocks a language (` ```java `, ` ```shell `).
* Space after `#` in headings: `# Heading`.
* Blank line between a heading and the content around it.
* Blockquotes: put `>` on **every** line, not just the first.
* Ordered lists: number **every** item `1.` so reordering needs no renumbering.
* Bullets: use `*`, not `-`.
* Italics: use `_underscores_`, not `*asterisks*`. Bold: `**text**`.

```markdown
## Example heading

Some text.

* First bullet
* Second bullet

1. Step one
1. Step two

> Quote line one
> Quote line two

This is _italic_ and **bold**.
```

## Repo specifics

* `docs/` is a MarkBind site. Keep YAML front matter (`layout`, `title`, `pageNav`), MarkBind components (such as `<box>` and `<puml>`), Nunjucks variables (`{{ ... }}`), and `_markbind/` layouts and shared content intact. See the [MarkBind syntax overview](https://markbind.org/userGuide/markBindSyntaxOverview.html) and [page structure guide](https://markbind.org/userGuide/tweakingThePageStructure.html) when editing these features.
* Resolve links and images relative to the source file. Use `{{ baseUrl }}` for site-root links in reusable `_markbind/` content; MarkBind converts links to `.md` pages into `.html` links when building the site. See [intra-site links](https://markbind.org/userGuide/formattingContents.html#intra-site-links).
* Put diagrams' source (PlantUML `.puml`) in `docs/diagrams/` and generated images in `docs/images/`.
* No trailing whitespace; end files with a single newline (checked by `.github/run-checks.sh`).
* Never paste secrets, real personal data, or internal URLs into docs or screenshots.
