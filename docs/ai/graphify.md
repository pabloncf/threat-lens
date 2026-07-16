# Graphify — code knowledge graph

Source: https://github.com/Graphify-Labs/graphify (MIT). PyPI package `graphifyy`
(the CLI command is `graphify`). Installed via `uv tool install graphifyy`.

## What it does

Turns a folder of code, docs, PDFs, images, etc. into a queryable knowledge graph
instead of something you grep through file by file. Code is parsed locally with
tree-sitter (no LLM, nothing leaves the machine); only the semantic pass over
non-code files (docs, PDFs, media) uses a model. Every edge is tagged `EXTRACTED`
(explicit in source) or `INFERRED` (resolved by graphify), so you always know
what was read directly vs. guessed.

## Status on this project

Installed project-scoped (not global — this skill only applies here, unlike RTK):

- CLI: `uv tool install graphifyy` (user-level tool install, shared across projects)
- Skill: `.claude/skills/graphify/SKILL.md` (+ `references/`) — committed to this repo
- Hooks: `.claude/settings.json` — `PreToolUse` on Bash search commands and on
  `Read`/`Glob`, nudging the agent toward `graphify query` instead of manual exploration
- Output artifacts: `graphify-out/graph.html`, `graphify-out/GRAPH_REPORT.md`,
  `graphify-out/graph.json` — **gitignored**, regenerated locally, not source of truth

Verify anytime:

```bash
graphify --version
ls graphify-out/ 2>/dev/null   # empty until the graph is first built
```

## Building the graph

Run inside the AI assistant (not a plain shell command — the semantic pass over
non-code files needs the assistant's own model):

```
/graphify .
```

Produces `graphify-out/{graph.html, GRAPH_REPORT.md, graph.json}`.

## When to regenerate

- **Routine code edits** (small fixes, adding a function, normal day-to-day changes):
  run `graphify update .` — incremental, AST-only, no model cost. Safe to do often.
- **Full rebuild** (`/graphify .` again): only after large structural changes — a big
  refactor, a new module/domain area, a renamed/reorganized layout. Not on every
  small commit; the incremental update covers that.

## How the agent should use it

- For codebase questions, run `graphify query "<question>"` first when
  `graphify-out/graph.json` exists, before reading multiple files manually.
  Use `graphify path "<A>" "<B>"` for how two things connect, `graphify explain
  "<concept>"` for a focused explanation of one node.
- Read `graphify-out/GRAPH_REPORT.md` for broad architecture review, or when
  query/path/explain don't surface enough context.
- If `graphify-out/wiki/index.md` exists (built with `--wiki`), use it for broad
  navigation instead of raw source browsing.

## Useful commands

```bash
graphify query "<question>"        # scoped subgraph answering a question
graphify path "A" "B"              # shortest path between two concepts
graphify explain "<concept>"       # focused explanation of one node
graphify update .                  # incremental rebuild, AST-only, no cost
graphify <path> --watch            # auto-rebuild on file changes, no LLM needed
```

## Uninstall

```bash
graphify claude uninstall          # remove hooks + CLAUDE.md section (this project)
graphify uninstall --purge         # remove all platforms + delete graphify-out/
uv tool uninstall graphifyy        # remove the CLI itself
```
