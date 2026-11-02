# 2. Make casting predictable

[Work-plan index](README.md) · [Original audit](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md)

Resolve target policy, failed-cast transactions and event-driven eligibility before tuning broad combat interactions.

Implemented on 7 September 2026. See [target rules, lifecycle decisions, regression evidence and live acceptance limits](02-targeting-and-casting-results.md). The original acceptance criteria remain below for traceability.

## M08 — Make aimed attacks respect intended visibility

- [x] Implemented with server regression coverage. See [results and remaining live acceptance](02-targeting-and-casting-results.md).

**Scope:** [Deadly Gaze](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#deadly-gaze), [Hematic Rebuke](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-rebuke), [Hematic Impressment](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-impressment), [White Verdict](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#white-verdict), [Blood Eclipse](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-eclipse), [Prismatic Reproof](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#prismatic-reproof), [Blood Aneurysm](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-aneurysm), [Hemorrhage](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hemorrhage), [Exsanguinate](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#exsanguinate), [Insatiable Hunger](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#insatiable-hunger), [Grave Debt](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#grave-debt), [Eclipse Well](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#eclipse-well)

**Depends on:** None. **Work unit:** Several focused patches.

**Do:** Classify these as aimed rays, cones, nearby selection or placed fields. Reuse existing eligible-target/raycast helpers where they fit. Clip aimed rays and placed effects to geometry, and require visibility where the action promises an aimed target. Preserve genuinely intended area behavior explicitly rather than applying one universal ray rule.

**Done when:** Check an open target, wall, doorway, glass/partial cover and a target behind the caster. The selected target and visible impact agree. Record any deliberate through-cover exceptions. Handle one targeting family per patch.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M09 — Unify hostile-area ally and PvP handling

- [x] Implemented with server regression coverage. See [results and remaining live acceptance](02-targeting-and-casting-results.md).

**Scope:** [Blood Aneurysm](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-aneurysm), [Activation Potential](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#activation-potential), [Prismatic Reproof](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#prismatic-reproof), [Sanguine Ignition](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-ignition), [Vitric Combustion](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#vitric-combustion), [Scalding Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#scalding-updraft), [Soaring Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#soaring-updraft), [Suspended Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#suspended-updraft), [Expulsive Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#expulsive-updraft), [Cryogenic Pulse](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#cryogenic-pulse), [Osseous Bloom](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#osseous-bloom), [Blood Eclipse](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-eclipse), [Bloom Of Rot](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#bloom-of-rot), [Hemorrhage](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hemorrhage)

**Depends on:** None. **Work unit:** Several focused patches.

**Do:** Define the existing game policy for self, teammates, owned summons, neutral entities and PvP-disabled players. Apply that policy through shared eligibility helpers to the listed hostile actions. Keep intentional self-costs, such as Bloom of Rot self-poison, distinct from accidental friendly fire.

**Done when:** A target matrix demonstrates correct results for solo, team, tame/summon and PvP-disabled cases. Area damage, status application and knockback use compatible eligibility. Do not delete deliberate self-drawbacks.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M10 — Fix beneficial recipient selection

- [x] Implemented with server regression coverage. See [results and remaining live acceptance](02-targeting-and-casting-results.md).

**Scope:** [Living Circuit](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#living-circuit), [Lumen Suture](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#lumen-suture), [Hematic Beacon](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-beacon)

**Depends on:** None. **Work unit:** Focused.

**Do:** Filter recipients using the intended alliance rules before choosing nearest/most-injured players. Define Lumen self fallback and Circuit solo behavior. Keep recipient selection deterministic enough for clear targeting feedback.

**Done when:** An injured opponent standing closer than a teammate does not steal an allied heal/buff. Self fallback works when appropriate; no-valid-recipient behavior is explicit. Team effects still reach eligible allies.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M11 — Validate failed actions before spending or awarding progress

- [x] Implemented with server regression coverage. See [results and remaining live acceptance](02-targeting-and-casting-results.md).

**Scope:** [Pyretic Forge](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#pyretic-forge), [Umbral Step](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#umbral-step), [Umbral Reversal](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#umbral-reversal), [Hematic Rebuke](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-rebuke), [Hematic Impressment](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-impressment), [Exsanguinate](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#exsanguinate), [Endless Hour](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#endless-hour), [Sanguine Halo](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-halo)

**Depends on:** M01, M04, M08, M10. **Work unit:** Multi-file.

**Do:** Trace shared payment and successful-use bookkeeping, then the listed action-specific early returns. Move predictable validation before payment and reuse the validated target/destination where practical. Validate the final safe teleport position, not only the original candidate. Check exact-cost payment semantics and keep any required reserve explicit.

**Done when:** Invalid input/target/destination or already-active state spends no blood, applies no new cooldown, and earns no mastery/strain/tendency rewards. Valid casts charge once. Test exact cost and one unit below/above. A legitimately fired attack that misses can still be a successful cast; do not equate hit detection with all use progression.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M12 — Make reactive powers enforce their stated activation rules

- [x] Implemented with server regression coverage. See [results and remaining live acceptance](02-targeting-and-casting-results.md).

**Scope:** [Sovereign Instinct](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sovereign-instinct), [Blackhearted](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blackhearted), [Phoenix Debt](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#phoenix-debt)

**Depends on:** M04. **Work unit:** Multi-file.

**Do:** Trace event handlers rather than the ordinary cast function. Reconcile alignment, active-blood/equipped state, internal cooldown, shared last-rite eligibility and blood payment. Verify Sovereign target redirection duration; introduce a bounded persistent state only if needed to satisfy its intended control effect.

**Done when:** Each passive has a documented ready/blocked/triggered/spent lifecycle. Alignment exceptions are either intentional and displayed or corrected. Phoenix triggers once, Blackhearted saturation recovers correctly, and Sovereign does not immediately undo its own action through ordinary AI.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.
