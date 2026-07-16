# RTK — token-optimized shell proxy

Source: https://github.com/rtk-ai/rtk (Rust binary, Apache-2.0). Not to be confused with
`reachingforthejack/rtk` ("Rust Type Kit") — a different, unrelated project with the same name.

## What it does

Filters and compresses the output of common dev commands (git, test runners, linters,
package managers, docker, cloud CLIs, etc.) before that output reaches the model's context.
Typical savings: 60-90% of output tokens per command, with <10ms overhead.

## Status on this machine

Installed globally, already active for every Claude Code project (not just this one):

- Binary: installed via Homebrew (`brew install rtk`)
- Hook: registered globally at `~/.claude/settings.json` (PreToolUse, native binary `rtk hook claude`)
- Reference doc: `~/.claude/RTK.md`, pulled into `~/.claude/CLAUDE.md` via `@RTK.md`
- Backup of the pre-install settings.json: `~/.claude/settings.json.bak`

Verify anytime:

```bash
rtk --version
rtk gain          # must show token-savings stats, not "command not found"
rtk init --show   # full status of hook / RTK.md / settings.json
```

## How it's used in practice

The hook rewrites Bash tool calls transparently (`git status` -> `rtk git status`), so most of
the time nothing needs to be typed differently. Two things it does **not** cover:

1. Claude Code's built-in `Read` / `Grep` / `Glob` tools bypass the Bash hook entirely — they
   never get rewritten. When those tools aren't enough for the job (e.g. filtering a huge log,
   or reading with an argument the built-in tool doesn't support), drop to a shell command:
   `cat`/`head`/`tail`, `rg`/`grep`, `find` — or call `rtk read` / `rtk grep` / `rtk find` /
   `rtk smart` explicitly for the compressed version.
2. New shells / environments where the hook isn't installed (e.g. a subagent running elsewhere,
   or another machine) won't rewrite commands automatically — call `rtk <cmd>` directly there.

Useful direct commands:

```bash
rtk read file.ext -l aggressive   # signatures only, strips bodies
rtk smart file.ext                # 2-line heuristic summary of a file
rtk grep "pattern" .              # grouped search results
rtk find "*.ext" .                # compact find results
rtk gain                          # token-savings stats for this project/session
rtk discover                      # find missed savings opportunities
```

## Telemetry

Disabled by default, opt-in only (`rtk telemetry enable`), aggregate/anonymous even when on.
This setup does **not** enable it. Check current state with `rtk telemetry status`.

## Reinstall on another machine

```bash
brew install rtk                # or: curl -fsSL https://raw.githubusercontent.com/rtk-ai/rtk/refs/heads/master/install.sh | sh
rtk init -g --auto-patch        # installs hook + ~/.claude/RTK.md + @RTK.md reference, no prompts
rtk init --show                 # verify
```

## Uninstall

```bash
rtk init -g --uninstall   # removes hook, RTK.md, settings.json entry (restores backup)
brew uninstall rtk
```
