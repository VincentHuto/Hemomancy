# 3. Make acquisition and prerequisites coherent

[Work-plan index](README.md) · [Original audit](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md)

Start route verification early. Final recipe prices and player-facing numbers follow the combat tuning they depend on.

The first implementation pass is recorded in [area 3 results](03-acquisition-results.md), with the [90-recipe ledger](03-memory-recipe-ledger.md). M15's server checks are complete. M13/M16 retain live-route acceptance; M14 awaits M18–M25 combat tuning, and M17 needs final prices and live UI acceptance. Unchecked tasks are not claims that no work has been done.

## M13 — Make the early memory-acquisition route playable

- [ ] Complete this task and record evidence.

**Scope:** [Blood Shot](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-shot), [Blood Rush](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-rush), [Deadly Gaze](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#deadly-gaze), [Sanguine Mending](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-mending), [Hemorrhage](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hemorrhage), [Glacial Grasp](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#glacial-grasp), [Sanguine Ignition](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-ignition), [Void Shroud](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#void-shroud), [Conjure Staff](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-staff), [Blood Absorption](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-absorption), [Blood Projection](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-projection)

**Depends on:** M03, M11. **Work unit:** Progression playtest plus targeted fix.

**Do:** Walk Degree 0 through first access to the Degree-III loom without granting the desired memory. Record crude drops, ingredients, structures, required alignment and blood reserves. Identify any real dependency deadlock. Repair the smallest missing bridge or explanation; do not lower the loom degree merely because the audit noticed it.

**Done when:** A reproducible player route reaches an early usable loadout and eventually the loom. The required loot/recipe/infrastructure is available at each checkpoint. Manual completion and any fixture-assisted checks are labeled separately.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M14 — Reprice the memory recipes after gameplay is stable

- [ ] Complete this task and record evidence.

**Scope:** [Blood Rush](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-rush), [Summon Avatar](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar), [Summon Avatar Arms](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-arms), [Summon Avatar Armor](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-armor), [Summon Avatar Legs](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-legs), [Summon Avatar Complete](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-complete), [Crimson Coronation](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#crimson-coronation), [White Verdict](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#white-verdict), [Phoenix Debt](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#phoenix-debt), [Vascular Dowsing](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#vascular-dowsing), [Glacial Grasp](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#glacial-grasp), [Hemorrhage](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hemorrhage), [Black Veil Covenant](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#black-veil-covenant), [Penumbral Drift](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#penumbral-drift), [Crimson Tithe](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#crimson-tithe), [Unclosing Eye](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#unclosing-eye), [Bloom Of Rot](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#bloom-of-rot), [Endless Hour](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#endless-hour)

**Depends on:** M13, M18–M25. **Work unit:** Several recipe batches.

**Do:** For all 85 ordinary woven recipes, rebuild a sheet of blank+catalyst+enzyme+blood costs and alternate routes. Compare premium catalysts against the now-measured utility, focusing first on these outliers. Examine cross-school enzyme choices as authored choices, not automatically errors. Adjust only recipes with a justified mismatch; include scar alternatives in the comparison.

**Done when:** Every changed recipe has a before/after cost and utility rationale. Lantern cost, blank enzyme, drift risk and catalyst route remain visible. Learning a family capstone does not silently require purchasing all intermediate forms. Recipe parsing/resource checks pass.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M15 — Verify family learning and mastery progression

- [x] Server learning, shared progress, reload/loadout and channel-cadence checks complete; [evidence and live limitations](03-acquisition-results.md#family-progression).

**Scope:** [Blood Binding](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-binding), [Lingering Blood Binding](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#lingering-blood-binding), [Chain Blood Binding](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#chain-blood-binding), [Blood Lattice](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-lattice), [Blood Needle](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-needle), [Blood Needle Fan](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-needle-fan), [Blood Needle Lance](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-needle-lance), [Blood Shot](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-shot), [Guided Blood Shot](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#guided-blood-shot), [Hematic Mortar](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-mortar), [Sanguine Halo](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-halo), [Blood Cloud](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-cloud), [Expansive Blood Cloud](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#expansive-blood-cloud), [Pursuing Blood Cloud](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#pursuing-blood-cloud), [Sanguine Tempest](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-tempest), [Scalding Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#scalding-updraft), [Soaring Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#soaring-updraft), [Suspended Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#suspended-updraft), [Expulsive Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#expulsive-updraft), [Lignum Mortis](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#lignum-mortis), [Canopy Mortis](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#canopy-mortis), [Worked Lignum](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#worked-lignum), [Summon Avatar](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar), [Summon Avatar Arms](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-arms), [Summon Avatar Armor](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-armor), [Summon Avatar Legs](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-legs), [Summon Avatar Complete](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-complete), [Hematic Rebuke](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-rebuke), [Hematic Impressment](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-impressment), [Umbral Step](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#umbral-step), [Umbral Reversal](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#umbral-reversal), [Synaptic Jolt](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#synaptic-jolt), [Activation Potential](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#activation-potential), [Synaptic Storm](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#synaptic-storm), [Glacial Grasp](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#glacial-grasp), [Cryogenic Pulse](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#cryogenic-pulse), [Rimebound Sentence](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#rimebound-sentence)

**Depends on:** M02, M04, M11. **Work unit:** Family-focused verification.

**Do:** Check all eleven families against cumulative thresholds, shared mastery, learning gates, cast gates, selection and saved loadouts. Confirm quick/charged versus sustained-channel credit cadence. Keep universal efficiency benefits and explicitly selectable forms; retain useful baseline forms.

**Done when:** At each threshold, the correct forms become learnable/selectable and share progress after reload. No failed-action farming from M11 remains. Learning, equipping and using multiple forms does not duplicate or lose progress.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M16 — Verify staff grafts and exceptional reward delivery

- [ ] Complete this task and record evidence.

**Scope:** [Conjure Staff](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-staff), [Conjure Blade](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-blade), [Conjure Axe](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-axe), [Conjure Spear](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-spear), [Conjure Claws](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-claws), [Conjure Crossbow](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-crossbow), [Conjure Torch](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-torch), [Conjure Flail](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-flail), [Conjure Sickle](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-sickle), [Blood Absorption](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-absorption), [Blood Projection](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-projection), [Thread Ripper](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#thread-ripper)

**Depends on:** M11, M13. **Work unit:** Several form/reward checks.

**Do:** Walk earned graft activities, crafting, brazier absorption, morphing and restoration. Verify actual switch/attack costs, item data, paired-claw inventory safety and loaded crossbow persistence. Check Degree-VII Sickle eligibility against registry/UI wording. Check Thread Ripper direct-grant success and failure handling at Liberation without assuming an under-ranked finale is reachable.

**Done when:** Each staff form has a reproducible acquisition/use/restore route. No stored item data or items are lost on low blood, death or reload. Both special rewards are delivered once when eligible and cannot be silently consumed without the intended result.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M17 — Expose the actual costs and prerequisites to players

- [ ] Complete this task and record evidence.

**Scope:** [Conjure Staff](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-staff), [Conjure Blade](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-blade), [Conjure Axe](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-axe), [Conjure Spear](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-spear), [Conjure Claws](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-claws), [Conjure Crossbow](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-crossbow), [Conjure Torch](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-torch), [Conjure Flail](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-flail), [Conjure Sickle](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-sickle), [Blood Absorption](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-absorption), [Blood Projection](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-projection), [Summon Avatar](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar), [Sovereign Instinct](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sovereign-instinct), [Phoenix Debt](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#phoenix-debt), [Blackhearted](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blackhearted)

**Depends on:** M12, M14, M15, M16. **Work unit:** UI/data copy pass.

**Do:** Update the relevant existing tooltips/details panels and acquisition documentation to distinguish one-time cast, per-second upkeep, reactive payment, staff switch cost and item attack cost. Distinguish learning degree, casting alignment, family mastery and exceptional reward gates.

**Done when:** Displayed values agree with the runtime ledger or are explicitly labeled base values. Registry-zero staff forms are not presented as free switches. A player can identify why a learned power is currently blocked without reading source.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.
