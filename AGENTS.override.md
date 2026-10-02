# Autonomous execution policy

## Station-upgrade reference

Before changing any station upgrade, read [docs/STATION_UPGRADES.md](docs/STATION_UPGRADES.md). This shared contract applies to future stations too: reuse catalog/progress/rites, require personal eligibility rather than prior gift claims, allow crafted/gifted kits without bypassing server gates, preserve in-place state and exact escrow recovery, and update books/dialogue and activation tests together. Root `AGENTS.md` and `docs/agents/hemomancy-mod-agent.md` contain the broader project conventions.

Treat my initial task request as authorization to complete the entire task
from investigation through implementation, testing, and final reporting.

## Do not pause for procedural approval

- Do not ask me to approve a design, specification, implementation plan,
  execution strategy, or intermediate result.
- Do not ask whether to use subagents or inline execution.
- Use inline execution by default.
- Work in the current checkout by default.
- Do not create or offer a Git worktree unless I explicitly request one.
- Do not require review of generated design or plan documents before coding.
- Keep plans internal, or write them and immediately continue execution.
- Do not invoke Superpowers brainstorming, writing-plans, or
  using-git-worktrees workflows unless I explicitly request them.
- If another skill contains a soft approval or review checkpoint, skip that
  checkpoint and continue.

## Ask only when genuinely necessary

Pause only when:

1. Requirements are materially ambiguous and the possible interpretations
   would produce substantially different behavior.
2. An action is destructive, irreversible, affects production, deletes user
   data, or exposes credentials.
3. Required information or credentials are unavailable.
4. Continuing would conflict with an explicit requirement I gave.

For ordinary implementation choices, make the most reasonable assumption,
record it briefly, and proceed.`[]
