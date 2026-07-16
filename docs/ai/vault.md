# Memory vault (Obsidian)

Location: [`docs/vault/`](../vault/00-Index.md), inside this repo — versioned, not
gitignored. This is durable project memory (decisions, glossary, learnings), separate
from `graphify-out/` (regenerated code graph) and from `CLAUDE.md` (the short index of
this AI layer itself).

To browse with backlinks/graph view, open `docs/vault/` as a vault in Obsidian.app
(File → Open folder as vault). Reading/writing the markdown files directly works fine
without the app too.

## Structure

- `00-Index.md` — map note, links to everything else.
- `Decisions/` — one file per architecture decision (ADR), using `Decisions/_template.md`.
- `Learnings/` — process/preference learnings (see [docs/ai/learnings.md](learnings.md)
  for the full mechanism). `Learnings/_active-rules.md` is the only file read routinely;
  the rest is history.
- `Glossary.md` — project-specific terms, one `##` heading per term.
- `Sessions/` — optional, one file per session worth remembering later.

## When to READ the vault

- Start of a session doing non-trivial work: skim `00-Index.md` and
  `Learnings/_active-rules.md` — both short, cheap to read every time.
- Before touching an area of the codebase that has a related ADR in `Decisions/` —
  check there before proposing a conflicting approach.
- When the user asks "why did we..." or anything about historical rationale.

## When to WRITE to the vault

Only after explicit user confirmation — never decide alone that something is a
permanent decision or a confirmed learning.

- **New architecture decision** (a library, pattern, or approach was chosen and an
  alternative rejected): propose a short ADR, ask the user to confirm it's worth
  keeping, then write it to `Decisions/`.
- **Confirmed learning** (something that worked, or a mistake that got corrected):
  see [docs/ai/learnings.md](learnings.md) for the exact trigger and format.
- If unsure whether something rises to "ADR-worthy" vs. just a passing implementation
  detail, ask rather than guess.

## Note on Graphify

Graphify supports building or feeding a vault directly (`/graphify . --obsidian
--obsidian-dir docs/vault`), which would give an auto-generated map of the *code*
inside the same vault structure. Not set up here — mixing auto-generated code notes
with hand-curated decisions/learnings needs a deliberate call on where the line goes,
so this is left as a future option rather than something enabled by default.
