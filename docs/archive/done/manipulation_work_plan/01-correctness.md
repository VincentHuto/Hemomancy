# 1. Correct behavior and resource exploits

[Work-plan index](README.md) · [Original audit](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md)

Fix these before drawing balance conclusions. Each ticket begins by reproducing or reconfirming the original source finding.

Implemented on 7 September 2026. See [results, chosen values, regression evidence and remaining live checks](01-correctness-results.md). The acceptance criteria below are retained for traceability.

## M01 — Make Exsanguinate earn its blood refund

- [x] Implemented and covered by server regressions. See [evidence and live acceptance limits](01-correctness-results.md).

**Scope:** [Exsanguinate](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#exsanguinate)

**Depends on:** None. **Work unit:** Focused.

**Do:** Reproduce extraction against a normal wounded blood-bearing enemy, an immune/protected target, a bloodless target and an invalid target. Trace the damage result and refund callers. Make the refund conditional on the intended successful extraction; use the existing execution behavior as the baseline rather than introducing a new resource system.

**Done when:** A successful qualifying execution refunds once. Damage rejection, invalid targets and non-blood victims cannot generate blood. Record blood before/after, and add a focused regression for the rejected-damage case.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M02 — Reconcile partial-charge cost and payload

- [x] Implemented and covered by server regressions. See [evidence and live acceptance limits](01-correctness-results.md).

**Scope:** [Hematic Mortar](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-mortar), [Blood Needle Lance](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-needle-lance), [Funeral Bell](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#funeral-bell), [Ironhearted](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#ironhearted), [Thread Ripper](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#thread-ripper)

**Depends on:** None. **Work unit:** Focused.

**Do:** Measure one-tick, half and full releases for Mortar, Lance and Funeral Bell, including every status bonus. Make partial output scale with payment, or require full charge for a genuinely fixed payload. Keep Ironhearted and Thread Ripper full-charge-only; do not impose that rule on unrelated charged powers.

**Done when:** A charge/cost/output table demonstrates that a tiny payment cannot buy an unchanged full payload. Full-charge behavior remains useful. Canceled casts and incomplete Ironhearted/Thread Ripper releases spend no blood or cooldown. Test the selected scaling rule at boundary values.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M03 — Correct Blood Rush healing cadence

- [x] Implemented and covered by server regressions. See [evidence and live acceptance limits](01-correctness-results.md).

**Scope:** [Blood Rush](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-rush)

**Depends on:** None. **Work unit:** Focused.

**Do:** Inspect every BloodRushEffect producer, including living-weapon procs. Measure healing over one, five and 12.5 seconds while injured. Set a deliberate healing budget and implement its cadence in the shared effect; retain the manipulation dash and evaluate weapon-proc duration separately.

**Done when:** Document the chosen total healing and measured HP/s. Amplifier/duration behavior is explicit, full-health ticks do not create excess healing, and weapon procs use the corrected shared effect. Add one timed-effect regression. Do not change the memory recipe in this step.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M04 — Close channel restart and Ward refresh loopholes

- [x] Implemented and covered by server regressions. See [evidence and live acceptance limits](01-correctness-results.md).

**Scope:** [Sanguine Ward](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-ward)

**Depends on:** None. **Work unit:** Focused.

**Do:** Trace START, upkeep, STOP, selection changes and disconnect through the shared channel manager. Reproduce starting during universal cooldown and restarting Ward for its initial shield. Apply cooldown eligibility at the correct shared entry point and make initial/refresh shielding respect the actual lifecycle.

**Done when:** Repeated START packets and rapid stop/start cannot produce unpaid or cooldown-bypassing shield gains. Legitimate upkeep still pulses once per second. Stop/death/selection change leaves no active channel. Verify another continuous power to catch shared-path regressions; leave numeric shield balancing for M18.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M05 — Finish Endless Hour damage deferral and repayment

- [x] Implemented and covered by server regressions. See [evidence and live acceptance limits](01-correctness-results.md).

**Scope:** [Endless Hour](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#endless-hour)

**Depends on:** None. **Work unit:** Multi-file.

**Do:** Recheck callers of the existing accumulation and expiry methods. Implement the stated borrowed-time lifecycle through the appropriate damage/tick hooks: what is deferred, the survival floor, expiry repayment, and state cleanup. Specify how defenses interact with repayment so damage is not accidentally mitigated twice. Define death/logout/dimension-change behavior before wiring the hooks.

**Done when:** Demonstrate activation, multiple incoming hits, lethal incoming damage, expiry and exactly one repayment. Recast cannot erase debt. Relog/death does not duplicate or silently discard obligations under the chosen lifecycle. Automated damage-state checks pass; separate live HUD/FX acceptance remains in M28.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M06 — Bound Osseous Bloom boss scaling

- [x] Implemented and covered by server regressions. See [evidence and live acceptance limits](01-correctness-results.md).

**Scope:** [Osseous Bloom](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#osseous-bloom)

**Depends on:** None. **Work unit:** Focused.

**Do:** Measure raw and post-defense damage against ordinary, elite and boss health pools at baseline and high scaling. Choose and document a boss ceiling or diminishing rule that preserves the current-health identity without overwhelming the rest of the roster.

**Done when:** A small comparison table shows predictable scaling at several health pools and no runaway multiplication. Ordinary enemies retain useful payoff. Regression coverage tests boss and non-boss paths. Do not replace the spell with unrelated flat damage without an explicit gameplay rationale.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M07 — Verify status timing and amplifier edge cases

- [x] Implemented and covered by server regressions. See [evidence and live acceptance limits](01-correctness-results.md).

**Scope:** [Hematic Beacon](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-beacon), [Gloam Laceration](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#gloam-laceration), [Hemorrhage](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hemorrhage), [Blood Shot](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-shot), [Blood Needle](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-needle)

**Depends on:** None. **Work unit:** Focused.

**Do:** Check whether Beacon actually heals while its short Regeneration I duration is continually refreshed. Check amplifier-0 Blood Loss on players versus mobs. Fix confirmed timing/formula defects in the narrowest shared owner; first define whether amplifier zero is intended to bleed players.

**Done when:** Beacon healing is measured under continuous refresh. A status table records player/mob behavior for relevant Blood Loss amplifiers. No accidental every-tick damage multiplication is assumed; immunity frames are included in measurements.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.
