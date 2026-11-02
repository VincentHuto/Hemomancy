# Work area 1 — Correctness implementation results

7 September 2026 · [Tasks M01–M07](01-correctness.md) · [Work-plan index](README.md)

The seven correctness changes are implemented. Automated checks cover the server behavior; live client, multiplayer and visual acceptance remain open. These are initial bounded values for the later balance pass, not a claim that the roster is now balanced.

## Changes and values

| Task | Implemented behavior | Regression evidence |
|---|---|---|
| M01 — Exsanguinate | Refund up to 600 mL only after accepted damage actually kills a qualifying blood-bearing victim. Bloodless, immune and already-dead victims cannot generate blood. | Rejected/bloodless actions leave the fixture at 2,000 mL; a valid execution raises it to 2,600; repeating against the corpse adds nothing. |
| M02 — Charge economics | Mortar requires 40 ticks; Lance requires 30. Incomplete releases stop before payment/cooldown. Funeral Bell scales its entire damage, including status bonuses, and added Wither duration. Ironhearted and Thread Ripper retain their existing full-charge rules. | Zero/one/half/just-under-full charge rejection; successful full Mortar/Lance casts produce one/three projectiles; Bell tests one/half/full charge with all five status counts. |
| M03 — Blood Rush | Shared healing is 0.5 HP per 20 effect ticks, independent of amplifier. Duration and existing speed modifiers remain intact. | Actual effect ticking heals 0.5 HP in 20 ticks, 2.5 in 100, and 6 in 250. Max-health clamping and weapon amplifier checked. |
| M04 — Channels/Ward | START obeys cooldown and rejects dead players. Duplicate START does nothing. A depleted Ward retains its zero pool until expiration; upkeep adds 2 rather than granting a fresh 6. Dimension changes interrupt channeling. | Restart/payment/pool checks, timed upkeep, plus Penumbral Drift interruption on selection, death and dimension event. |
| M05 — Endless Hour | Damage hooks preserve 1 HP, accumulate lethal overflow, and collect debt once on expiry or logout. Pending debt blocks recasting before payment. | Actual damage pipeline, multiple hits, absorption accounting, expiry tick, lethal collection, recast rejection, dimension/End-return clone events and logout settlement. |
| M06 — Osseous Bloom | Boss damage is capped at 12 HP after mastery and affinity scaling, before the victim's defenses. Ordinary targets retain current-health scaling. | Wither at 20/100/1,000 current HP with baseline and increased mastery; ordinary targets at 20/100/1,000 HP. |
| M07 — Status timing | Beacon directly heals 0.4 HP per one-second pulse. Blood Loss amplifier zero drains players; higher amplifiers use the normal zero-based tier convention. | Beacon heals 3.2 HP over its eight-second lifetime. Player amplifier matrix and actual mob effect ticks include immunity frames. |

Three casts also loaded a client-only particle provider on dedicated servers: Exsanguinate, Osseous Bloom and Endless Hour. They now construct the same glow particle data directly. Their visuals have not been redesigned in this pass.

## Charge, payment and payload

Costs below are registration values before the existing cost ledger's modifiers. Bell scales the final modified cost by the same charge fraction as its damage. Rejected incomplete fixed casts spend zero blood and start no cooldown.

| Power | One-tick release | Half charge | Full charge |
|---|---|---|---|
| Hematic Mortar | Rejected, 0 mL | Rejected, 0 mL | 40 ticks, 200 mL, one unchanged mortar shot |
| Blood Needle Lance | Rejected, 0 mL | Rejected, 0 mL | 30 ticks, 150 mL, three unchanged piercing needles |
| Funeral Bell | 1/80 charge, 12.5 mL | 40 ticks, 500 mL | 80 ticks, 1,000 mL |
| Ironhearted | Rejected, 0 mL | Rejected, 0 mL | Existing full-charge payload; 300 mL base cost |
| Thread Ripper | Rejected, 0 mL | Rejected, 0 mL | Existing full-charge payload; 35 mL base cost |

Funeral Bell's damage before affinity adjustment:

| Existing qualifying statuses | One tick | Half charge | Full charge |
|---|---:|---:|---:|
| 0 | 0.05 HP | 2 HP | 4 HP |
| 1 | 0.075 HP | 3 HP | 6 HP |
| 2 | 0.10 HP | 4 HP | 8 HP |
| 3 | 0.125 HP | 5 HP | 10 HP |
| 4 | 0.15 HP | 6 HP | 12 HP |

The four statuses are Wither, Poison, Blood Loss and Grave Debt. Added Wither lasts 2/60/120 ticks at those releases when at least one status qualifies. Existing longer or stronger Wither follows vanilla effect merging. Full-charge damage, radius and Wither strength are preserved; this does not consume the existing statuses.

## Shared effects and producers

Blood Rush uses the shared cadence for the manipulation (250 ticks: 6 HP), living-tool proc (50 ticks: 1 HP), Vesper (30 ticks: 0.5 HP), Curved Horn (200 ticks: 5 HP), potion (1,000 ticks: 25 HP potential), Covenant Throne (12,000 ticks: 300 HP potential), and the optional MnA effect component. Healing requires missing health. Reapplication follows the existing effect-duration merging behavior; amplifier still affects registered attributes, but not the healing pulse.

| Blood Loss amplifier | Player drain in 20 ticks, before Coagulation | Ordinary mob loss in 20 actual ticks | Bloodless mobs |
|---|---:|---:|---|
| 0 / tier I | 10 mL | 1 HP | Immune |
| 1 / tier II | 20 mL | 1 HP | Immune |
| 2 / tier III | 30 mL | 1 HP | Immune |

Mob damage remains 0.5 HP per attempted tick, limited by normal immunity frames. It is not 10 HP/s. Coagulation's blocked-drain ledger uses the corrected player formula too. Beacon retains its Resistance and hostile-reveal pulses; replacing its continually refreshed 30-tick Regeneration fixes the missing heal without leaving a long regeneration buff behind.

## Endless Hour lifecycle

- **Activation:** preserve the existing ten-second Absorption V and Resistance IV protection.
- **Incoming damage:** armor/resistance are resolved first. Absorption remains available for normal consumption. Only the amount that would lower health below 1 is deferred.
- **Collection:** clear the stored debt first, then subtract it from health. Armor, resistance, absorption, incoming-damage shields and damage-triggered passives do not reduce it a second time. Lethal collection invokes normal death handling; death-event rescue effects can still intervene.
- **Recast:** any pending hour blocks payment and activation, including the expiry boundary before its tick has settled the debt.
- **Logout:** collect before player data is saved. Rejoining cannot pause or erase an unpaid obligation. Existing potion durations are not stripped from the player.
- **Dimension travel:** retain the persistent debt and game-time deadline, transferring both to the replacement player when returning from the End; interrupt any ordinary continuous channel separately.
- **Death/respawn:** clear the dead player's obligation so it does not charge the next life. Canceled death events do not clear an active debt. Expired persisted debt is also handled by the normal player tick after loading.

Live relog, portal travel and HUD feedback still need client acceptance; event-level checks are not a substitute for those sessions.

## Osseous Bloom comparison

The following neutral-affinity examples show damage entering the victim's defenses. A 1.45 multiplier illustrates three Crimson Mastery levels; affinity is also applied before the boss ceiling.

| Current HP | Ordinary, ×1 | Boss, ×1 | Ordinary, ×1.45 | Boss, ×1.45 |
|---|---:|---:|---:|---:|
| 20 | 5 | 5 | 7.25 | 7.25 |
| 100 | 25 | 12 | 36.25 | 12 |
| 1,000 | 250 | 12 | 362.5 | 12 |

The cap uses the existing boss predicate: vanilla Wither/Ender Dragon and Hemomancy's boss package. Third-party bosses outside that predicate are not automatically recognized. Slowness and ordinary-target policy are unchanged; broader targeting belongs to area 2.

## Validation and remaining acceptance

Runtime regressions live in [ManipulationCorrectnessGameTests.java](../../src/gameTest/java/com/vincenthuto/hemomancy/gametest/ManipulationCorrectnessGameTests.java). They use real registered powers and server entities.

- Initial regression run reproduced the defects and three dedicated-server particle-loading failures (`build/manipulation-correctness-red.log`). The existing Vein Mason fixture also failed in early runs but passed in the final run; its intermittent behavior remains outside this pass.
- All 14 focused JVM casting, Thread Ripper and Ironhearted checks passed (`build/manipulation-correctness-focused-jvm.log`).
- The broader JVM run executed 1,775 tests with 64 failures in the current checkout, including stale file paths, layout/registration expectations and legacy discovery assertions (`build/manipulation-correctness-expanded.log`). These unrelated failures were not rewritten as part of this pass.
- Final GameTest run: **223/225 passed, including all 17 new correctness tests** (`build/manipulation-correctness-verified.log`). The two failures are existing Hematic Stake checks: `delayedInvalidationFallsBackToTheStakeItem` and `formationPlacesAndRegistersAllSixStakeDirections`. Both failed before these production changes. The Beacon refresh regression now passes in its own batch because neighboring tests otherwise clear the shared reactive-state maps while it waits for upkeep.
- No live client/combat/multiplayer or visual sign-off is claimed. M28/M32 retain that acceptance work. Area 5 still requires authored, concept-faithful effects, including visible bloody swords for Crimson Coronation.

Implementation owners: `ExsanguinateManip`, `BloodShotManip`, `BloodNeedleManip`, `FuneralBellManip`, `BloodRushEffect`, `ManipulationChannelManager`, `ManipulationReactiveEvents`, `EndlessHourManip`, `OsseousBloomManip`, and `BloodLossEffect`. Relevant English descriptions now explain full-charge requirements, healing and debt/cap behavior.
