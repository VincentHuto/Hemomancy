# Work area 3 — Acquisition and prerequisites

7 September 2026 · [Tasks M13–M17](03-progression-and-acquisition.md) · [Recipe ledger](03-memory-recipe-ledger.md)

This pass fixes loaded staff-crossbow restoration and rejected Liberation rewards, verifies family progression on the server, and exposes separate learning, casting and weapon costs. It does **not** close the survival playthrough or pre-empt area 4's combat tuning.

| Task | Result | Remaining acceptance |
|---|---|---|
| M13: early acquisition | Eight crude learning paths checked; reproducible route and loot odds below; guidance improved | Walk the route in survival, recording travel, drops, alignment and reserves without granting memories |
| M14: recipe prices | All 85 ordinary recipes and five scar alternatives inventoried, with blank and lantern costs | Reprice justified outliers after M18–M25; no recipe prices changed here |
| M15: families | Eleven families / 26 forms checked at learning thresholds, shared progress and reload; multi-form loadouts and channel cadence checked | Live selection/input acceptance remains in M32 |
| M16: grafts and rewards | Eight staff forms checked for switch payment, restoration and occupied-offhand safety; crossbow ammunition and reward fallback repaired | Earn each graft and complete its brazier rite in play; death/drop recovery, paired empty-offhand claws, and Vesper's full encounter route remain live checks |
| M17: visible requirements | Base cast/upkeep/trigger/switch labels; learning degree, casting alignment, family baseline, Sickle exception and item attack costs clarified | Live tooltip fit/readability and final prices after M14 |

## Early route — source-backed checkpoints

This is a route to reproduce, **not a claimed completed survival run**. Server fixtures supply prerequisites and item inputs; they do not measure travel or grinding.

1. **Activate blood through the existing temple initiation.** Meet the temple's Hermit, accept the oath and use the prepared initiation rite. Follow the Sanguine Conduit and Vicar progression. The loom is not needed to start learning.
2. **Acquire a crude memory.** Thirsters killed by players have a 5% Blood Shot and 5% Blood Rush chance per kill. Player-killed Hematic Constructs have a 10% Deadly Gaze chance. An Outpost's dedicated memory roll has total weight 64: Shot/Rush each 5/64, each of the other six memories 3/64, and no memory 36/64. These are independent opportunity probabilities, not guaranteed drops or measured travel times.
3. **Right-click to learn for 500 mL**, with active blood and the rank's learning degree. No blank, enzymes, loom or brazier is needed. Duplicate use retains the shard and blood. The crude options are Shot, Rush, Gaze, Mending, Hemorrhage, Glacial Grasp, Sanguine Ignition and Void Shroud. Their tooltips now distinguish learning degree from casting alignment and cost. Refill before learning another shard or casting; do not treat the learning payment as the casting price.
4. **Degree I grants Blood Absorption and Blood Projection.** These remain automatic utilities, not ordinary memories to buy. Use the existing blood infrastructure and hematic-iron progression. Construct the Living Staff with two iron bars and two Venous Stone blocks in its authored vertical pattern, holding a Sanguine Formation and projecting 150 mL onto the iron bars. This structure teaches Conjure Staff through the existing progression path.
5. **Follow the Vicar/Votary and Alchemist/Initiate route.** The existing Harbinger journey checkpoints include hematic iron and staff crafting, Votary rite, centrifuge preparation, enzyme recovery, Alchemist reward, enzyme mastery and Initiate rite. They precede first culture and first woven memory. Use the Conduit/assignment requirements for the exact rite inputs; these milestones are not replaced with degree grants.
6. **At Degree III, build the Somatic Loom.** Its unchanged recipe uses one bookshelf, six iron bars and four Hematic Iron Blocks, a held Sanguine Formation and 150 projected mL. Match the authored pattern in JEI/recipe guidance. A learning rank of Degree 0/I does not imply self-built loom access at that degree.
7. **Prepare a blank and weaving inputs.** A blank costs four Sanguine Formations, four Blood-Stained Stone and one Neurotic Enzyme. Use recovered/looted enzymes or lantern production. A self-built Mycelial Lantern is also Degree III: 2,500 projected mL, Ferric Binder, two Sanguine Glass, two brown mushroom blocks, one Hematic Iron Block, four Polished Venous Stone and a copper block. The culture is retained; each enzyme takes 600 mL and two minutes. Weaving costs and catalyst slots are in the ledger.

Sources: `CrudeMemoryShardItem`, `KnownManipulationGrantHelper`, `loot_table/entities/{thirster,hematic_construct}.json`, `loot_table/chests/harbinger_outpost.json`, `recipe/blood_structure/{living_staff,somatic_loom,mycelial_lantern}.json`, `recipe/hematic_memory.json`, and the existing `gametest/journey/HemoJourneyStage` route.

The reviewed acquisition dependencies do not require an early woven memory to obtain the loom. Random loot availability and the actual journey pace still need the playthrough. The Degree-III loom gate remains unchanged.

## Recipe review — prices retained pending combat tuning

Run `python tools/manipulation_recipe_ledger.py` to rebuild and validate the [90-row ledger](03-memory-recipe-ledger.md). It lists catalysts, school enzyme counts, weaving blood, the blank enzyme, serial lantern time and scar alternatives. Non-memory weaving outputs such as the eight-Formation recipe are excluded from the memory count.

Priority comparisons remain Blood Rush, the five Avatar memories, Coronation, White Verdict, Phoenix Debt, Dowsing, Grasp/Hemorrhage, the shadow utilities and saint rewards. Area 1 changed healing and boss damage; area 4 has not yet established final relative utility. Changing premium catalysts now would make M14's stated dependency meaningless. Cross-school enzymes remain authored choices, and a capstone does not acquire a new requirement to purchase every intermediate form.

## Family progression

The existing cumulative thresholds remain **10, 35, 85 and 185 credits**. Each mastery stage supplies the existing 5% cost/cooldown benefit, capped at 20%. Forms remain separate memories and explicit selections.

For every family/form pair, the runtime test checks rejection one credit below its threshold, acceptance at the threshold, shared progress identity, availability after attachment serialization/reload, and a saved loadout containing both baseline and form. Earlier forms are removed between checks, verifying that later forms do not need intermediate purchases. Learning at zero alignment is permitted; casting still enforces alignment separately.

Quick/charged successful casts retain their use credit. The channel check exercises Sanguine Mending: START and each one-second pulse repair 50 durability, but mastery receives one credit only after five seconds of sustained operation. Area 2's invalid-action checks remain the guard against credit for rejected casts.

## Staff and exceptional rewards

| Form | Earned activity route | Graft catalyst |
|---|---|---|
| Blade | Living-Arsenal kill while at least 75% full blood | Hematic Iron Powder |
| Axe | Existing axe-aligned manipulation activity | Chalybeate Sclerite |
| Spear | Living-Arsenal kill of a glowing victim, or spear-aligned activity | Calcified Blood Spine |
| Claws | Living-Arsenal kill while invisible or in light ≤7 | Chitinous Husk |
| Crossbow | Trigger a conductive arc with staff access | Puppeteering Thread |
| Torch | Living-Arsenal kill of a burning victim, or torch-aligned activity | Fervent Spores |
| Flail | Living-Arsenal kill of a slowed/frozen victim, or flail-aligned activity | Frigid Spores |

Each ordinary graft also costs a blank, Sanguine Formation and its recipe's physical enzyme. `LivingWeaponGraftRecipeUnlockEvents` awards the earned advancement; crafting alone does not replace it. Hold the Living Staff and channel absorption into the lit brazier. Existing grant-before-consumption checks preserve rejected offerings.

**Switching:** 250 mL base, reduced by Weapons Master in 50 mL steps to 50 mL. Restoration is free. A 249 mL fixture cannot perform a base-cost switch; 250 mL succeeds and leaves zero. All eight forms preserve the original named staff and an occupied non-weapon offhand during the tested failure/restore path.

**Loaded crossbow fix:** `LivingStaffWeaponFormHelper` stores the custom charged flag and projectile list in the restored staff. Re-forming the crossbow retrieves that load, including after a save/reload and an intervening weapon form. The load is removed from its embedded staff snapshot. `LivingCrossbowItem` clears the fired volley before a low-blood restoration can save it, preventing previously fired ammunition from returning. This does not preserve arbitrary temporary buffs on every weapon form.

**Living Sickle:** the existing route remains Degree VII, Silent Archon, Vesper defeated, a Living Staff bond and absorption of Memory of Vesper. Its registry rank still maps to Degree VI; UI guidance now calls out the stricter acquisition route. `LivingWeaponGraftRite` already rolls back awakening if the Sickle grant fails and only consumes the offering after success. This pass does not claim an end-to-end Vesper playtest.

**Thread Ripper:** Liberation's already-once-only completion gate now handles the grant result. Eligible players learn it directly; already-known players receive no duplicate. A rejected grant gives the actual memory item, dropping it if inventory is full, with brazier guidance. A synthetic low-degree fixture proves fallback behavior; it does not establish that an under-ranked Liberation finale is reachable in survival.

## Player-facing cost changes

The Harbinger panel and memory tooltips use the same base-cost labels. Continuous powers and Avatar forms display **mL/s**; reactive passives display a trigger cost; staff forms display the separate switch price. Ordinary casts display their base price. Runtime modifiers still apply where the existing ledger applies them.

Tooltips also show separate weapon costs: LivingTool melee's 50% chance of damage-stat ×25 mL, Blade/Axe's additional feral damage-stat ×75 mL, Torch breath's 60 mL/s, and Crossbow blood bolts' 50% chance of 50 mL per projectile. These costs are not included in the staff switch price. Alignment failures now state required and current alignment.

## Verification and remaining work

Runtime checks live in [ManipulationAcquisitionGameTests.java](../../src/gameTest/java/com/vincenthuto/hemomancy/gametest/ManipulationAcquisitionGameTests.java). The initial loaded-crossbow regression failed before the restoration fix.

- **6/6 new GameTests pass.** The full run is **242/245**, including all area-1 and area-2 checks. The two previously recorded stake failures remain: `delayedinvalidationfallsbacktothestakeitem` and `formationplacesandregistersallsixstakedirections`. This run also reports `formationacceptsautopickedupoutput`: its fixture refuses a preexisting stone block. These are outside the changed acquisition paths; the full gate remains failing. Log: `build/manipulation-acquisition-final.log`.
- **10/10 focused JUnit tests pass:** family registry and known-family availability.
- **Four standalone legacy checks pass:** rank gates, saved loadout source contracts, graft recipe resources and staff switch rules. These were invoked directly because they use `main`, not JUnit annotations.
- **90 memory recipe resources validated**, including result models, enzyme counts and unchanged lantern economics. Scoped `git diff --check` passes. Compilation succeeds; no full JVM-suite pass is claimed.

Live survival acquisition, graft activities/brazier channels, death recovery, client tooltip layout and multiplayer inventory behavior remain unverified. M14 remains dependent on M18–M25, and M17 needs a final cost reconciliation afterward. The custom casting-effects work remains in area 5, including authored bloody swords for Crimson Coronation.
