# Caveman — lean session mode

Source: a personal skill authored outside this repo. Installed project-scoped at
`.claude/skills/caveman/SKILL.md` (copy, not a symlink) — fully self-contained, no
dependency on where it originally came from.

## What it does

Rewrites the assistant's prose to a terse register (drops articles, filler, pleasantries,
hedging; short fragments; short synonyms) while keeping every technical detail exact:
code blocks, commands, API names, and quoted error strings are never altered. Measured
~65% output-token reduction. Six intensity levels: `lite`, `full` (default), `ultra`,
`wenyan-lite`, `wenyan-full`, `wenyan-ultra`.

**What it compresses vs. what it never touches:** Caveman shortens *narration and
prose* — explanations, transitions, hedging. It does not rewrite or shorten a diff,
a stack trace, or an error string once the decision is made to show it: those are
quoted verbatim. The one nuance to know — in `full` (the default), a long raw error
log is not dumped by default; the assistant quotes the shortest decisive line instead,
unless you ask for the complete output. If you want the full log verbatim, ask for it
explicitly (or drop to `lite`, which doesn't compress log inclusion).

## When to activate

Turn on `/caveman full` for:
- Long exploration/debugging sessions — lots of back-and-forth reading code, running
  commands, narrowing down a bug.
- Any session where you've said you want fewer tokens / more speed.

Keep normal prose (don't activate, or say "stop caveman" if it's already on) for:
- Explaining architecture or trade-offs to yourself or someone else later.
- Writing documentation, ADRs, or anything meant to be read outside the session.
- Code review — the skill's own boundary rule already excludes code/commits/PRs from
  compression.

## Auto-clarity (built into the skill, not something to configure)

The skill itself drops out of caveman register for: security warnings, irreversible-action
confirmations, multi-step sequences where fragment ambiguity risks misread, and whenever
you repeat a question. It resumes caveman after the clear part is done. This is automatic —
no need to ask for normal prose in those cases, though you still can.

## Invocation

```
/caveman              # full mode (default)
/caveman lite          # lighter compression, full sentences
/caveman ultra         # extreme compression, bare fragments
/caveman wenyan        # classical Chinese register
stop caveman           # back to normal prose
```

Mode persists for the whole session until changed or explicitly stopped — it does not
drift back to normal prose on its own.
