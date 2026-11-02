# Work area 2 — Predictable targeting and casting

7 September 2026 · [M08–M12 tasks](02-targeting-and-casting.md) · [Work-plan index](README.md)

M08–M12 are implemented. This pass changes recipient selection, validation and reactive lifecycles. Casting art remains assigned to area 5; the existing custom particles are preserved, including repairs to server-side particle-data construction where necessary.

## Target and cover rules — M08/M09

| Targeting family | Powers | Current rule |
|---|---|---|
| Aimed entity ray | Deadly Gaze, Hematic Rebuke, Hematic Impressment | Stop at the first collision shape. Return the actual entity intersection point. An opening or the uncovered portion of a slab permits a hit; a full wall or glass does not. Behind-caster entities are excluded. Thread Ripper also benefits from the shared ray correction. |
| Aimed beam | White Verdict | Clip the beam endpoint to geometry; require a forward, visible eligible target inside the existing beam width. |
| Forward cone | Blood Eclipse, Prismatic Reproof | Preserve cone range/angle, require target visibility, and clip existing particle sample positions to geometry. |
| Nearest visible victim | Blood Aneurysm, Hemorrhage, Exsanguinate, Insatiable Hunger, Grave Debt | Preserve nearest selection and range, but exclude hidden or allied targets. These remain surrounding searches and **can select behind the caster**. Exsanguinate additionally retains its wounded/blood-bearing requirements. |
| Placed field | Eclipse Well | Clip the placement point to geometry. The field retains its existing radius, pull and status behavior. |
| Hostile area pulse or splash | Aneurysm splash, Activation Potential, Sanguine Ignition, Vitric Combustion, all four Updraft forms, Cryogenic Pulse, Osseous Bloom, Bloom of Rot | Preserve area shape and scaling; use the same eligibility before applying damage, status, ignition or knockback. |

**Deliberate cover exceptions:** radial pulses, secondary splash and an already-placed Eclipse Well remain area effects. They do not perform a separate line-of-sight test for every victim. This pass prevents placing an aimed field through a wall, rather than converting every area effect into a ray. Collision shapes block targeted effects, including glass; fluids are not ray blockers. Visibility uses eye-to-eye collision traces.

### Recipient matrix

| Candidate | Hostile effect | Allied support |
|---|---|---|
| Caster | Excluded | Suture fallback and Beacon; Circuit requires another player |
| Same-team entity/player | Excluded, even with team friendly fire enabled | Eligible when the support power accepts players |
| Caster's tameable or bound summon | Excluded | Existing player-only support scope is retained |
| Loaded teammate's owned creature | Excluded through ownership/alliance resolution | Existing player-only support scope is retained |
| Caster's impressed body | Excluded | Existing player-only support scope is retained |
| Unaffiliated ordinary animal/mob | Eligible | Excluded |
| Opposing survival player, PvP enabled | Eligible | Excluded |
| Opposing player, PvP disabled | Excluded from damage, statuses and knockback | Excluded |
| Dead entity or spectator | Excluded | Excluded |
| Creative player | Excluded from hostile targeting | May receive allied support |

`ManipulationCombatHelper` owns the shared policy. Existing callers inherit it, including guided targeting, relevant crowd-control helpers and player-origin conductive arcs. Grave Debt retains genuinely ownerless/NPC burst behavior; a player-owned burst with an unavailable owner does not guess who is allied. Third-party pets need the existing ownership interfaces or alliance behavior to participate.

## Support selection — M10

- **Living Circuit:** choose up to three living, non-spectator teammates within the existing search volume, ordered by distance and then UUID. Solo START spends nothing. Losing all recipients ends the channel before another upkeep payment. An allied strike does not apply or consume the armed Conductive Mark.
- **Lumen Suture:** choose the nearest injured teammate, with UUID as a tie-breaker. If none qualifies, heal/cleanse/shield the caster, including the existing full-health absorption use case.
- **Hematic Beacon:** heal only its caster and teammates. Hostile reveal uses the hostile recipient policy. If the owner is unavailable, the pulse does nothing; its ordinary expiration/logout cleanup still applies.

## Cast validation and payment — M11

| Power/path | Validation before payment |
|---|---|
| Pyretic Forge | Nonempty held input, available smelting recipe and nonempty result |
| Umbral Step | Hit a block; choose a collision-free, supported landing; validate darkness on **that final candidate**, including the face fallback |
| Umbral Reversal | Find a supported, collision-free dark candidate in the existing backward search |
| Rebuke / Impressment | The first aimed mob must be commandable and an eligible hostile target |
| Exsanguinate | Find a visible, wounded, blood-bearing eligible target |
| Endless Hour | Retain area 1's pending-hour rejection before payment |
| Sanguine Halo | Retain its existing already-orbiting rejection before payment |

Safe teleport positions use the player's complete collision box, loaded/build/border bounds, solid support and fluid/fire/hot-floor rejection. Umbral Step's Phantasmal Bloodlust exception still bypasses darkness/cooldown, not physical safety. The same destination/target selection routine is used by validation and execution on the synchronous server cast path; no shared mutable target cache was introduced.

Predictable failures return before the shared blood drain, cooldown and successful-use bookkeeping. A valid attack can still pay when it fires and misses. A valid Exsanguinate attempt against a target that later survives or rejects damage does not earn a refund; it is distinct from having no eligible target.

**Exact-cost payment:** a cast is affordable at `blood >= final modified cost`. The borrowed reserve supplies only the missing amount, without the former extra 1 mL. At cost−1 with no reserve, the cast fails unchanged; at exact cost it succeeds and leaves zero; at cost+1 it leaves one. Existing cost modifiers, reserve limits and subsequent low-blood consequences remain in effect.

## Reactive lifecycles — M12

| Power | Ready / blocked | Trigger and payment | Recovery |
|---|---|---|---|
| Blackhearted | Equipped, enabled, active blood, Mortem alignment ≥35, allowed blood-power path | Wither conversion remains free: prevent 65% up to the remaining saturation capacity and heal half the prevented amount | Saturation cap 12; rupture empties it and retains the existing 6-damage attempt and 1,400-tick refractory period. No conversion during that period; conversion resumes afterward. |
| Sovereign Instinct | Equipped, enabled, active blood, Animus alignment ≥60; at least four eligible blood-bearing mobs targeting the caster | On incoming attack, pay the existing modified 450 mL cost once; redirect ordinary attackers for 100 ticks. Existing boss distraction/Weakness fallback remains. | Existing 600-tick internal cooldown. The command manager suppresses normal target selection, maintains the redirected target and restores ordinary targeting at expiration or loss of caster. |
| Phoenix Debt | Equipped, enabled, active blood, Flammeus alignment ≥75; correct shared Last Rite source available | A hit lethal after accounting for absorption pays the existing modified 2,000 mL cost once, prevents the hit and sets 1 HP | Consume shared Last Rite eligibility and retain the existing 6,000-tick internal cooldown. No second payment on blocked repeat triggers. |

Phoenix no longer arms below its alignment/active-blood requirements. Sovereign and Phoenix already used the shared payment/alignment transaction; that behavior was preserved. Blackhearted's event path now enforces the previously bypassed prerequisites. Saturation math and cooldown tuning were not redesigned.

## Verification

Runtime regressions: [ManipulationTargetingGameTests.java](../../src/gameTest/java/com/vincenthuto/hemomancy/gametest/ManipulationTargetingGameTests.java).

- Initial tests reproduced through-wall ray selection, hostile selection of owned creatures, support recipient errors/client-factory loading, invalid-cast payment, exact-cost rejection, passive prerequisite bypass and immediately overwritten Sovereign control.
- Coverage includes wall/glass/opening/slab/behind-caster rays and hitbox padding beside thin glass panes; owned-wolf and PvP-disabled selection; all 14 affected hostile payloads against allied and opposing mobs; allied support and solo behavior; safe/unsafe/bright-fallback teleport validation; valid Forge output; exact payment; passive conversion, recovery, repeat-trigger prevention and timed redirection expiration.
- Existing Blackhearted/Phoenix fixtures now supply their real prerequisites. Beacon's fixture registers its owner in the level. Aneurysm's charge comparison uses an unobstructed position above neighboring test structures.
- **GameTests: 237/239 pass**, including all 14 new targeting/casting checks and all 17 work-area-1 correctness checks. The two existing failures remain `delayedinvalidationfallsbacktothestakeitem` and `formationplacesandregistersallsixstakedirections`. Log: `build/manipulation-targeting-verified.log`.
- **Focused JVM checks: 12/12 pass** across Morphling Body Idiom, Body Idiom, Hematic Command and Manipulation Casting rules. Both standalone borrowed-blood and shared-Last-Rite legacy checks also pass. JVM log: `build/manipulation-targeting-jvm.log`. The full JVM suite was not rerun in this pass.

**Still requires live acceptance:** client aim/impact appearance, doorway and unusual partial-cover play, real multiplayer teams/pets/PvP, crowded movement and teleport safety, and Sovereign combat AI with different enemy types. Runtime fixtures verify server rules; they do not establish client presentation or overall balance. No visual-completion claim is made.
