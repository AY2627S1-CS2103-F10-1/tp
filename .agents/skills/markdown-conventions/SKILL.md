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

* `docs/` is a Jekyll site; keep front matter and existing `{% include %}`/liquid tags intact.
* Put diagrams' source (PlantUML `.puml`) in `docs/diagrams/` and generated images in `docs/images/`.
* No trailing whitespace; end files with a single newline (checked by `.github/run-checks.sh`).
* Never paste secrets, real personal data, or internal URLs into docs or screenshots.
