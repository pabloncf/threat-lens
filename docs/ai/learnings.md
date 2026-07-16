# Auto-learning

Lightweight, cheap-to-read mechanism for carrying process/preference knowledge across
sessions — repeated mistakes that got corrected, patterns that worked, preferences that
were confirmed in practice — without you having to re-explain them every time.

**This is not architecture memory.** Decisions about libraries, patterns, or structure
go in `docs/vault/Decisions/` as ADRs (see [docs/ai/vault.md](vault.md)). Learnings are
about *how to work*, not *what was built*.

## Format

Individual entry: `docs/vault/Learnings/YYYY-MM-DD-topic.md`, a few lines:

```markdown
# YYYY-MM-DD — <topic>

**What happened:** <the situation, in one or two sentences>
**Learned:** <what the correction or confirmation revealed>
**Rule:** <the practical, reusable rule going forward>
```

Consolidated: `docs/vault/Learnings/_active-rules.md` holds only the rules that are
**currently active** — a short bullet list, each one linking back to the dated entry
that produced it. This is the only file read at the start of a session (per
[docs/ai/vault.md](vault.md)); the dated files are history, not routinely re-read.

```markdown
# Active rules

- <rule, one line> — from [[2026-07-15-topic]]
- <rule, one line> — from [[2026-07-10-other-topic]]
```

## Trigger — when to write one

Only two triggers, and always with confirmation before writing — never automatic or silent:

1. **You say so explicitly** — "aprenda isso", "lembra disso", "learn this", or similar.
2. **End of a session where something broke and got corrected** — propose the entry
   (what happened / learned / rule) and ask before writing it.

If neither happened, don't write one — not every session produces a learning, and
manufacturing one to have something to write defeats the point.

## Retiring a rule

When a rule in `_active-rules.md` gets superseded or turns out wrong, remove it from
the active list (don't just leave it to accumulate) and note in the original dated
file that it was superseded and why. The dated file stays as history; only the active
list needs to stay short.
