# Validation campaigns and acceptance

[Execution index and dependency register](README.md) · [Live findings](../HARBINGER_LIVE_GAMEPLAY_FINDINGS.md) · [Evidence journal](../HARBINGER_GAMEPLAY_CAMPAIGN.md)

Status: recommended work, not implemented by this document. Priority and evidence type are explicit per task. **Requires** blocks dependent implementation. **Acceptance prerequisites** block a meaningful final gameplay verdict, not independent coding or local unit checks. Both are linked in the central register.

A compile or rule test cannot close a live gameplay failure. These tasks are acceptance campaigns after the relevant repair groups; they also explicitly cover the original campaign limitations.

## Tasks

<a id="val-01"></a>

### VAL-01: Replay the repaired solo journey from a fresh character

**Priority:** Release gate. **Evidence:** Required integration acceptance.

**Observed basis:** The original campaign earned D1-D8 through real rites but its unmodified route stopped at D1 and required later bypasses.

**Requires:**

- [RUN-03](01-runtime-and-controls.md#run-03) — Baseline controls and native selections must be usable.
- [DIA-01](02-dialogue-and-rewards.md#dia-01) — Opening quest rewards must work.
- [DIA-03](02-dialogue-and-rewards.md#dia-03) — Late returns must retain their lessons.
- [MAT-01](03-materials-and-machines.md#mat-01) — Starter loop needs its proposed final configuration.
- [MAT-02](03-materials-and-machines.md#mat-02) — Sampling must conserve resources.
- [MAT-03](03-materials-and-machines.md#mat-03) — Flasks and machine output must be usable.
- [MAT-04](03-materials-and-machines.md#mat-04) — Rare sources must be naturally obtainable.
- [MEM-02](05-weaving-scars-and-teachings.md#mem-02) — The memory loop must survive reload.
- [MEM-03](05-weaving-scars-and-teachings.md#mem-03) — First scar teaching must work.
- [RIT-01](04-crafting-rites-and-armor.md#rit-01) — D6 construction must be legal.
- [RIT-02](04-crafting-rites-and-armor.md#rit-02) — Rite readiness must be reliable.
- [RIT-04](04-crafting-rites-and-armor.md#rit-04) — Armor preparation must be usable.
- [FAN-01](07-fanes-recruitment-and-persistence.md#fan-01) — Fane craft access must work.
- [END-03](08-qliphoth-and-endings.md#end-03) — Tree lifecycle and choice presentation must work.

**Acceptance prerequisites:** None.

**Unblocks:** [VAL-02](09-validation-and-execution.md#val-02) (implementation)

**Recommended code work:** Use GameplayCampaignDriver for recorded normal actions and read-only evidence. Keep a clean production-data world separate from old failure saves and comparison fixtures. Record exact build, world seed, options, difficulty, scenario, prerequisite origin and all commands. Improve action completion/freshness checks before long scripts so a stopped client or open GUI cannot create apparent results. Do not grant degree, milestone, reward, pome lifecycle, creature bond or an intended test input.

**Recommended gameplay behavior:** Play the route a returning new player would follow: Hermit, first income, rewards, extraction, memory, scar, Fane, D6 infrastructure, armor, D7 tree and D8. Revisit old lessons deliberately. A repaired journey should not require source coordinates or external instructions for its next step.

**Acceptance:** At least one complete core route without progression/recipe/reward bypasses. Check chapter handoffs, natural source access, actual deaths/recovery and one reload per major system. Measure active time separately from travel, passive waits and operator pauses. If a new blocker appears, preserve it and mark downstream coverage assisted or blocked, not passed.

<a id="val-02"></a>

### VAL-02: Run separate natural balance and advanced-branch campaigns

**Priority:** Release gate for balance claims. **Evidence:** Required for balance conclusions.

**Observed basis:** Natural Vesper victory, acquisition economics, advanced Anchorite/Artificer flow and matched companion builds were incomplete in the initial report.

**Requires:**

- [VAL-01](09-validation-and-execution.md#val-01) — Measure pacing only after the core route is playable.
- [COM-04](06-morphlings-and-puppets.md#com-04) — Companion baselines must be controlled.
- [RIT-03](04-crafting-rites-and-armor.md#rit-03) — Rite objectives must have explicit success contracts.
- [MEM-05](05-weaving-scars-and-teachings.md#mem-05) — The advanced routing lesson must be tested.
- [END-04](08-qliphoth-and-endings.md#end-04) — Vesper needs its own natural combat evidence.

**Acceptance prerequisites:** None.

**Unblocks:** No task directly depends on this; retain its own acceptance requirement.

**Recommended code work:** Keep control variables fixed per comparison and preserve raw results. Change one tuning variable at a time after baseline failures are understood. Track resource income/expenditure, combat outcomes, retries, downtime and build prerequisites; include sample size and range. Do not turn the old assisted times into target averages. Maintain separate functional and balance verdicts.

**Recommended gameplay behavior:** Check three practical styles: active solo caster/melee, solo with a prepared companion, and ritual defense with intended NPC support. Compare why each wins or fails, not only completion time.

**Acceptance:** Natural Vesper attempts, complete D5/D6 Anchorite chain, advanced armor handoffs, matched companions and controlled support comparisons. Report unresolved cases honestly; a natural route can be functional yet still unpleasant or overpriced.

<a id="val-03"></a>

### VAL-03: Replay the shared dedicated-server progression

**Priority:** Release gate. **Evidence:** Required multiplayer acceptance.

**Observed basis:** Two clients verified some membership, station access and restart behavior, but particle failures interrupted broader progression.

**Requires:**

- [RUN-01](01-runtime-and-controls.md#run-01) — Server-side particle failures must be fixed first.
- [RUN-02](01-runtime-and-controls.md#run-02) — Awakened client rendering must be safe.
- [DIA-02](02-dialogue-and-rewards.md#dia-02) — Both entity and block reward interactions must be authoritative.
- [FAN-02](07-fanes-recruitment-and-persistence.md#fan-02) — Second-player onboarding and permissions must work.
- [FAN-03](07-fanes-recruitment-and-persistence.md#fan-03) — Helpers must remain usable.
- [FAN-04](07-fanes-recruitment-and-persistence.md#fan-04) — Death/Chamber behavior must be classified and repaired where needed.
- [MAT-02](03-materials-and-machines.md#mat-02) — Shared container conservation must hold.
- [MAT-03](03-materials-and-machines.md#mat-03) — Shared flask transfers must be valid.
- [MEM-02](05-weaving-scars-and-teachings.md#mem-02) — Weaves must survive server lifecycle.
- [COM-02](06-morphlings-and-puppets.md#com-02) — Companion state and activation must work.
- [END-01](08-qliphoth-and-endings.md#end-01) — Both completed paths need valid permissions.
- [END-02](08-qliphoth-and-endings.md#end-02) — Tree/player consumption state must synchronize.
- [RIT-02](04-crafting-rites-and-armor.md#rit-02) — Shared rite status must be accurate.

**Acceptance prerequisites:** None.

**Unblocks:** No task directly depends on this; retain its own acceptance requirement.

**Recommended code work:** Use a real dedicated server and two distinct clients on identical builds, keeping test-classloading failures separate from game failures. Start from fresh player state plus preserved crash copies. Validate server authority under simultaneous interaction, stale packets, full inventories, chunk unload and restart. Never rebuild shared HutosLib jars while clients are using them; stop owned runtimes first.

**Recommended gameplay behavior:** One founder and one late recruit should both have a comprehensible route, correct personal access and useful shared infrastructure. Both must see the relevant rite and ability feedback.

**Acceptance:** Replay all three particle reproductions, two-player initiation/membership, personal craft then shared machine, NPC roles, simultaneous reward acceptance, companion inventory changes, a rite, death/recovery and save/restart. Extend through both ending permission states. No disconnect, duplicated rewards/containers or lost earned progress.

<a id="val-04"></a>

### VAL-04: Run the separate Unstained Observances campaign

**Priority:** Follow-up. **Evidence:** Coverage expansion; not an established fourteen-quest failure.

**Observed basis:** The book UI opened with 0/14 and a missing icon. The full purification/Clarity/Observances route was not played; those assignments belong to Unstained, not Monolith progression.

**Requires:** None.

**Acceptance prerequisites:**

- [RUN-01](01-runtime-and-controls.md#run-01) — Dedicated offering/rite coverage needs particle safety.
- [DIA-01](02-dialogue-and-rewards.md#dia-01) — NPC task routing must work.
- [DIA-04](02-dialogue-and-rewards.md#dia-04) — Book/portrait/readability fixes must be visible.
- [END-01](08-qliphoth-and-endings.md#end-01) — New branch permissions must preserve Unstained exclusion.

**Unblocks:** No task directly depends on this; retain its own acceptance requirement.

**Recommended code work:** Inventory the actual fourteen observances and their existing NPC owners, prerequisites, counters and rewards before writing tests. Preserve mutual exclusion and reset rules; do not create Harbinger Monolith tasks to compensate for a different path. Cover the separate novitiate and cure entries where present. Only implement defects reproduced during that route.

**Recommended gameplay behavior:** Give each observed locked assignment a correct reason and source of the prerequisite. Explain the path distinction without erasing authored doctrine or promising simultaneous incompatible progression.

**Acceptance:** Actual entry/purification, all fourteen observances, lore/offerings, rewards, Clarity and relevant pruning; reconnect/death and repeat-claim checks. No purity/quest grants in the scored route. This task is required before claiming Observances work end to end, not before an unrelated centrifuge patch can ship.

## Completion record

When executed, record changed files, focused checks, live reproduction result, exact assists still required, save compatibility outcome, and downstream task IDs now unblocked. Keep tests of preserved broken saves separate from fresh-route acceptance. Update the relevant reference/wiki guidance only after behavior is implemented; this proposal does not redefine current mechanics.
