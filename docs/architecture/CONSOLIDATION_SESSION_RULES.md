# Consolidation Session Rules

Every consolidation session follows this protocol. A session prompt names the step it
runs; the rest is here so every session behaves the same way.

Read first, in this order:
1. `AGENTS.md`, `AGENTS.override.md`
2. `docs/architecture/CONSOLIDATION_PROGRESS.md` (the ledger)
3. `docs/architecture/CONSOLIDATION_ROADMAP.md`: the section for your phase
4. `docs/architecture/CANONICAL_STANDARDS_PROPOSAL.md`: the sections your phase cites
5. `docs/architecture/ARCHITECTURE_AUDIT.md`: the finding IDs your phase cites, and
   their `evidence/` reports

## 1. Preflight (stop if any check fails)

1. **One session at a time.** Do not run while another session builds this repository.
   Sessions share the Gradle build folders, so concurrent builds produce false
   `NoClassDefFoundError` and file-lock failures. If you see one, report it rather than
   "fixing" code.
2. **Previous step finished.**
   - The ledger must show the previous step as `DONE`, with a commit hash that appears
     in `git log`.
   - If your own step shows `IN PROGRESS`, resume from its handoff notes instead of
     starting over.
3. **Working tree.** Run `git status --short`.
   - Leave unrelated changes alone.
   - If uncommitted changes touch files in your scope, stop and report.
4. **Baseline.** Run `./gradlew.bat test`. Compare the result to the latest ledger entry.
   - The failure set must be **identical** to the recorded one: the known baseline
     failures before step 2, and zero failures from step 2 onward.
   - If anything new fails, stop and report. Do not fix unrelated failures.
5. **Guardrails.** Every guardrail test listed in the ledger must still pass. They run as
   part of `test`, under `src/test/java/com/vincenthuto/hemomancy/architecture/`.

## 2. Execution

- **Impact map first.** Use **one** read-only subagent (Opus, high effort) to list every
  caller and every test that pins the code you will move or delete. Use at most three
  subagents at once, and never use them for edits.
- **Edit inline**, in small steps. Run `./gradlew.bat test` after each step.
- **Behaviour preserving by default.** An item marked **APPROVAL** in the roadmap or audit
  may be implemented only if the ledger's *Decisions* table marks it `APPROVED`.
  Otherwise, leave it alone and list it under *Pending approvals* in your ledger entry.
- **Frozen identifiers.** These must not change:
  - registry IDs and resource paths, including JSON folder names
  - NBT and attachment keys
  - codec field names
  - payload byte layouts
  - structure codecs
  - command syntax

  Changing any of these is a migration that needs its own approved decision.
- **Finish migrations.** Move every call site in scope and delete the superseded code.
  Do not leave a new helper beside the old copies. A temporary adapter must be removed
  within the same step.
- **Reuse before adding.** Check `common/util`, `ManipulationCombatHelper`, `BloodFlowLedger`,
  `TransientServerStateEvents`, HutosLib and the helpers from earlier steps before
  writing a new one.
- **Tests move with code.** Update or replace any source-text test that pins moved text
  in the **same** step. Prefer converting it into a behaviour or data test. Use the
  shared helpers in `src/test/java/com/vincenthuto/hemomancy/support/` once they exist.
- **Lock in what you fixed.** For each consolidation or fix, add a regression or guardrail
  test so later steps cannot silently undo it, then list it in the ledger. Guardrail
  tests go in `src/test/java/com/vincenthuto/hemomancy/architecture/`.
- **Visual and timing changes.** Use the matching review client (see the project memory
  on review clients). Capture before and after screenshots and record their paths in the
  ledger. Compilation alone does not prove visuals are preserved.
- **Docs.** When a convention becomes real, update the relevant standards section and
  the AGENTS.md lines that describe it. When behaviour changes through an approved
  item, update `docs/HEMOMANCY_REFERENCE.md` and the relevant `wiki/*.md` page.

## 3. Postflight

1. **Tests.** `./gradlew.bat test` must be green, with zero failures from step 2 onward.
2. **alphaCheck.** Run `./gradlew.bat alphaCheck`. It includes the dedicated-server
   GameTests. It must be green, or every failure must be recorded in the ledger as a
   known issue with a reason it is unrelated.
3. **Final review.** One fresh reviewer subagent (Opus, high effort) reads `git diff` and
   checks for:
   - unintended behaviour change
   - changed frozen identifiers
   - leftover duplicate code
   - client classes reachable from common code

   Fix what it confirms.
4. **Ledger entry.** Update `CONSOLIDATION_PROGRESS.md` with:
   - status
   - date
   - commit hash(es)
   - test counts before and after
   - metrics: classes and lines removed and added, call sites migrated, remaining legacy instances
   - guardrails added
   - pending approvals
   - deviations from the roadmap
   - handoff notes for the next step
5. **Commit.** Commit on the current branch with messages prefixed
   `consolidation(step-N):`. The user has asked for this in the step prompts. If the
   prompt says "do not commit", leave the changes uncommitted and set the ledger status
   to `DONE (uncommitted)`.
6. **Report.** Finish with a short report: what changed, what was verified, what was not
   verified, and what is pending for the user.

## 4. When the work does not fit in one session

Any step may span several sessions:
1. Stop at a green, committed sub-step.
2. Set the ledger status to `IN PROGRESS`, with exact handoff notes: what is done, what
   is next, and any half-migrated area.
3. Re-run the same prompt in a new session to continue.

Never end a session with the build red.
