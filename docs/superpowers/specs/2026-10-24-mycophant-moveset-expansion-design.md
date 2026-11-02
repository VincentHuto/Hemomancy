# Mycophant Moveset Expansion — Design

Date: 2026-10-24. Builds on `2026-10-18-mycophant-spawn-mother-design.md` (the rebuild). That document still holds for
the body, the waking, the split, the yielding and the claim rules unless this one says otherwise.

## 1. Intent

The Mycophant is the Apotheos path's final boss, and in play she does two things on repeat: the Rushing Embrace and the
Brood Call. The cause is the move picker, not a shortage of moves. The embrace has the highest weight and covers
5–16 blocks, so a player who keeps their distance mostly gets the embrace, the siphon and the brood. The claw and pluck
moves only reach about 5–6 blocks. Every move is a single swing, and a hold lasts up to 8 seconds, so the grab fills
the fight.

The user wants a FromSoftware-style final boss, with Ebrietas (Bloodborne) as the reference: a big body that answers
wherever you stand, multi-hit strings with delayed hits, lingering ooze, a signature beam, readable big moves with long
punish windows, and a late desperation surge. They approved the proposed themes and every example move.

## 2. Goals, non-goals, anti-goals

**Goals**
- **Mother's attention.** No position is safe for long. Hugging her, kiting her and hiding behind or under her each
  have two or three answers.
- **The brood as a mechanic.** She nurses funglings into bigger strains, plants brood tumors that hatch if left alone,
  and grieves when her young die (the grief stagger already exists).
- **The Nursery fights with her.** Nectar pools from her spit stay on the floor, tumors pulse and hatch, and the
  existing nectar pressure stays the assimilation clock.
- **FromSoftware rhythm.** Multi-hit strings with a delayed final hit, chained follow-ups, clear tells, long recoveries
  on big moves, and a once-per-fight desperation set piece.
- The embrace stays her signature, but as a punish for staying far away too long, not the default.

**Non-goals**
- No change to the waking, the split, the yielding, rewards, succession, the hunt or the capture.
- No change to the model's anatomy (bones, proportions). New poses and animation only.
- No new arena geometry. The Nursery layout stays as built; new cover comes from tumors she plants.
- The "brood clutch" idea (funglings clinging to her sac as armour) is out of scope for this pass.

**Anti-goals (a failure even if it technically works)**
- Vanilla particle clouds dominating any move. Every effect uses the Hemomancy/HutosLib toolkit
  (`docs/MANIPULATION_VISUALS.md`; `HemoParticleData`, `BossTelegraphs`, the school materials, tendrils, claw slashes).
- Unreadable damage: every hit has a tell long enough to react to (at least 10 ticks), except the second hit of a
  string, which is read off the first.
- A move that can kill in one combo from full health on Normal without the player being hit by every part of it.
- Pose pops. Every new pose blends through `MycophantAnimState`, and strings blend hit to hit.
- The embrace or any one move taking more than about a third of her picks over a fight (see §8).

## 3. Constraints

- Follow `AGENTS.md` and `docs/architecture/ARCHITECTURE_STANDARDS.md`. Specifically:
  - Function-first packages: entities in `common/entity/...`, renderers in `client/render/...`, rules beside the
    entity in `common/entity/boss/endgame/`.
  - Targets through `ManipulationCombatHelper` where the guardrails require it (`CombatTargetingGuardrailTest`).
  - Text as `en_us.json` keys. `en_us.json` has uncommitted edits from another session, so add keys without
    reordering or rewriting existing lines.
  - Payload changes bump `PacketHandler.PROTOCOL_VERSION` and rewrite `src/test/resources/network/payload-registry.txt`.
  - Animation time through `MycophantAnimState` and `BossReplayClock`; no raw synced counter plus partial tick
    (`BossAnimationClockGuardrailTest`).
  - Render types built once (`RenderTypeCachingGuardrailTest`); world-space effects draw from
    `WorldEffectDispatcher` only.
- The model is generated. Edit `tools/model_export/mycophant_anim.java` (and `mycophant.py` only for bone access),
  then run `python tools/model_export/mycophant.py`. Never hand-edit `MycophantModel.java`.
- Pure rules stay pure and JVM-tested (`Mycophant*Rules`).
- Owned encounters never kill: a lethal blow expels the owner through the existing failure path. Claim-style damage
  (meters, the Last Brood cocoon) uses `hemomancy:mycophant_claim` and `MycophantClaimRules.clampNonlethal`.
- Holds and pins move the player only through the client-pinned hold system built on 2026-10-24
  (`MycophantHoldClientEvents`, `MycophantClaimRules.holdSlack`). Pulls use velocity (as the siphon reel does), never
  per-tick teleports.
- Several sessions share the build directory; run Gradle one at a time.

## 4. Move selection (the root fix)

Replace the single weight table in `MycophantControllerRules.choose` with a position-aware picker. Pure rules, fully
JVM-tested.

**Where the player is**, measured from her centre against her body yaw:
- **Band** by horizontal distance: `HUG` under 3.5 blocks, `NEAR` 3.5–7, `MID` 7–13, `FAR` over 13.
- **Sector** by angle from her facing: `FRONT` up to 60°, `FLANK` 60°–120°, `REAR` over 120°.

**Each move declares** the bands and sectors it answers and a base weight for each, per phase. A move with no weight in
the player's current band and sector is ineligible.

**Variety rules**
- Never the same move twice in a row (strings and follow-ups are one move for this rule).
- A move used in the last three picks has its weight multiplied by 0.4, then 0.6, then 0.8 (most recent first).
- **Kite pressure.** She counts the ticks the quarry has spent in `MID` or `FAR` during the last 200 ticks. The
  embrace's weight starts low (8) and gains 4 per 20 ticks of kiting, capped at 40. It resets when an embrace starts.
- **Hug pressure.** The same count for `HUG` raises the weight of the Brood Settle.
- Cooldowns stay per move as today.

**Follow-ups (strings).** A move may name follow-ups with a condition (its hit landed, the player is still in a given
band or sector) and a chance. A follow-up starts after a short link gap instead of the normal decision delay. Follow-ups
ignore the anti-repeat penalty but never chain more than two moves deep.

**Variants.** A move may roll a variant when it starts (the Rake String's two-hit or delayed three-hit form). Sync the
variant to the client (a new `DATA_ATTACK_VARIANT`) and key the attack clock on attack and variant.

## 5. Changes to existing moves

| Move | Change |
|---|---|
| Rushing Embrace | Weight from kite pressure (§4). Meter fills over 5 s (1/100 per tick); four hits break free. |
| Fusion Embrace | Same weighting; meter over 4 s (1/80 per tick); five hits break free. The 8% refusal bonus stays. |
| Claw Rake | Becomes the **Rake String** (§6). |
| Pluck | Kept; also the Claw Pin's follow-up. |
| Siphon Feed | Kept; `NEAR`/`MID` front only. |
| Brood Call | Kept; lower weight now that tumors and the mortar also make brood. |
| Nectar Surge | Kept as an environmental beat. |
| Wing Guard | Gains a **riposte**: a frontal blow on the guard starts an immediate Wing Sweep as a follow-up. |
| Wing Sweep | Kept; can chain into the Pluck (as the rebuild spec already said). |
| Pulp Bloom | Kept. |

Phase 2 makes the phase-1 moves faster (shorter wind-ups and decision delays) and adds hits: the Rake String gains a
fourth swing, the Siphon Spit fires five globs, and the Tail Lash swings twice.

## 6. New moves, phase 1 (the claim)

Damage values are starting points for Normal, to be tuned in review. Ticks are wind-up / active / recovery.

| Move | Answers | Tell | What happens | Counter |
|---|---|---|---|---|
| **Rake String** | HUG/NEAR, FRONT/FLANK | The first claw lifts and draws back (cone telegraph). | Left rake, right rake, then either nothing (two-hit variant) or a *delayed* overhead claw slam (three-hit variant). Each rake is a 6-block 80° cone, 7 damage; the slam a 3-block circle at the claw tip, 12 damage. About 14 / 8 per rake / 8, with a 10–14 tick hesitation before the slam. Phase 2 adds a fourth rake. | Dodge each rake; don't roll early into the slam. |
| **Brood Settle** | HUG, any sector | She rears up on all four claws and the sac glows bright amber (18-tick wind-up). | She drops her belly: a 4.5-block circle around her centre, 14 damage and an outward shove. 40-tick recovery with her belly on the floor: the big punish window. | Get out from under her. |
| **Claw Pin** | NEAR, FRONT | A front claw rises and points. | A stab at the player's spot; on a hit they are pinned under the claw for 20 ticks (client-pinned like a hold, no meter) and take 6 damage, then the **Pluck** follows. | Sidestep the stab. |
| **Tail Lash** | HUG/NEAR, REAR (and FLANK at a lower weight) | The tail tendrils coil and the abdomen tilts. | A sweep across the rear 150° within 6 blocks, 8 damage and knockback. Phase 2 swings twice (back and forth). | Jump or leave the arc. |
| **Vent Bloom** | HUG/NEAR/MID, REAR/FLANK | The petal vents open and their interiors brighten. | A spore cloud settles behind her (radius 3, lasts 6 s): 1 damage per second inside, plus 1 nectar pressure per second in an owned fight. | Leave the cloud. |
| **Hand Sweep** | NEAR/MID, FRONT | The grasping hand swings out wide and low (wide cone telegraph). | A ground-level 120° sweep from 4 to 9 blocks, 9 damage. A player whose feet are at least 0.8 blocks off the ground when it passes is missed. On a hit, it can chain into the Rushing Embrace. | Jump it or close in under it. |
| **Siphon Spit** | MID/FAR, FRONT/FLANK | The siphon arches up and swells. | Three nectar globs lobbed at and around the player (five in phase 2). A direct hit deals 5; each glob leaves a **nectar pool** (radius 1.5, 8 s) that slows like the pressure hazard and adds pressure while stood in. | Keep moving; don't fight in the pools. |
| **Spore Mortar** | FAR (MID at a lower weight), any sector | The abdomen lifts and the vents pump. | 3–5 egg sacs lobbed high. **Landing rings appear on the floor first** (at least 14 ticks before impact). Each bursts on landing: 6 damage in 2.5 blocks plus spores. In an owned fight, one egg in each volley lands intact as a **brood tumor** (§7) instead of bursting. | Read the rings and leave them. |
| **Nursing** | Any, needs a living brood fungling within 14 blocks | She turns to the fungling and the siphon reaches for it. | She feeds it for 80 ticks. If she finishes, it becomes a **grown brood** (§7). The feeding is broken by killing the fungling, or by 8 damage to her while she feeds. | Kill the fungling or hit her hard; this is also an opening. |

## 7. New brood and arena pieces

- **Grown brood.** A brood fungling she finished nursing: about 1.8× its size, 30 health, an 8-damage bite and the
  same herding. A synced flag on `FunglingEntity`; the renderer scales it. At most two grown at once. Killing one makes
  her grieve for 50 ticks instead of 30.
- **Brood tumor.** A new block, `brood_tumor`: a fleshy amber sac two blocks tall (a lower and an upper half, so it
  hides a standing player), placed on the Nursery floor by the
  Spore Mortar. It pulses (a slow glow) and hatches a brood fungling after 20 seconds, then breaks (respecting the
  brood cap of 4). The player breaks it in one or two hits (low hardness, no drop). It blocks line of sight like any
  block, which is what makes it cover for the Gaze. At most six tumors stand at once. Tumors exist only in owned
  fights; the arena rebuild on entry clears them, and so do the yielding and failure. Track them in the boss and
  remove any left when she leaves.
- **Nectar pool, spore cloud, lattice net.** One new lingering hazard entity with kinds (as `AnnettaPoolEntity` has),
  holding its radius and life and applying its effect. The client draws each kind with the existing toolkit: the
  nectar pool as an amber blood pool (the brazier `bloodPool` look), the spore cloud as drifting spore motes, the
  lattice net as hyphae strands (the tendril drawing).

## 8. New moves, phase 2 (the joining)

| Move | Answers | Tell | What happens | Counter |
|---|---|---|---|---|
| **Hyphae Lances** | NEAR/MID/FAR, FRONT/FLANK | The wings rise and the spars point down. | Spars stab into the floor and a line of hyphae lances erupts toward the player, one every 3 ticks over about 12 blocks. Each spot cracks amber for 10 ticks before it erupts (follow `PriestessEruptionEntity`'s pattern and reuse its thorn drawing in Mycophant colours where it fits). 10 damage and a toss. A second, angled line follows in the later part of phase 2 (below 35%). | Step off the line. |
| **Lattice Net** | MID, FRONT | The wings spread forward and the lattice glows. | A net settles over a 4-block circle at the player's position after a 16-tick tell. Anyone inside when it lands is netted for 40 ticks: heavily slowed and unable to jump. The Fusion Embrace is then strongly weighted as her next move. | Leave the circle before it lands. |
| **Gaze of the Many Eyes** | MID/FAR, FRONT | The pulp's eyes open one by one and lock on (a 24-tick tell, with a rising sound). | A beam from the pulp sweeps across about 100° over 40 ticks, tracking slowly. 3 damage and 1 pressure every 5 ticks while in it. Its hit test is a block raycast from the pulp to the player's body centre (not their eyes), so a two-block tumor within about two blocks of the player, on her side, blocks it. In an unowned fight (no tumors) the only counter is outrunning the sweep. | Shelter close behind a tumor, or outrun the sweep. |
| Wing Guard riposte | FRONT | A blow lands on the guard. | An immediate Wing Sweep. | Hit the guard from the side or behind instead. |

## 9. Desperation: the Last Brood

Once per fight, the first time her health falls below 25% in phase 2 (persist the flag in her save data).

1. **Tell (30 ticks).** She drags back toward the pool, rears and roars; the collar flares and the sac swells and
   brightens. She stays hittable at half damage.
2. **The pull (up to 160 ticks).** The gestation sac opens toward the player and pulls on the whole arena: velocity
   toward the hold point each tick, strong enough that sprinting away only just holds ground. Four **binding
   tendrils** run from her to anchor points 6–9 blocks out around her, so they lie between her and the player. Each is
   a hazard entity hit-tested along its whole length; two hits cut it. Through the pull she takes half damage and her
   health cannot fall below 5% of max, so she cannot be burned past the set piece into the yielding.
3. **Cut free.** When all four are cut, the sac tears and she collapses for 80 ticks: the biggest punish window of the
   fight.
4. **Spent.** If the 160 ticks run out with tendrils still standing and the player not reeled in, the pull ends, the
   tendrils wither and she staggers for 30 ticks. Surviving is enough; cutting free is the big reward.
5. **Cocooned.** If the pull reaches the hold point first, the player is cocooned: held for 60 ticks (client-pinned),
   then released with heavy claim damage (nonlethal), a blood drain and +25 pressure. The fight continues.

The sac's opening is a pose and glow change (the shell swells, its front scales out, the glow layer brightens), not new
geometry.

## 9a. Hold kinds

The embrace's hold becomes one of three hold kinds, synced alongside the claimed id so the client pins to the right
point:

| Kind | Pin point | Meter | Can be broken | On break |
|---|---|---|---|---|
| `SAC` (Rushing and Fusion Embrace) | the sac `holdPoint` (unchanged) | yes (§5 rates) | yes: N hits, or one cast per hold (unchanged rules) | the 40-tick break stagger, pressure relief, the fusion's 8% bonus (unchanged) |
| `PIN` (Claw Pin) | under the claw tip, on the floor | no | no; it runs out after 20 ticks | — |
| `COCOON` (Last Brood) | the sac `holdPoint` | no | no; it runs out after 60 ticks | — |

`MycophantHoldClientEvents` pins to the kind's point. The `holdSlack` rules apply to every kind.

## 10. Animation and visuals

- Every new move gets authored poses in `mycophant_anim.java`, driven by the replayed attack clock and its variant.
  Strings blend hit to hit (each swing a pose mix, never a snap), and `blendFromLast` covers move-to-move changes.
- Needed poses, at least: rake left, rake right, overhead slam; settle rise and drop; claw pin; tail coil and lash
  (driving the existing tail chains); vents open (the existing `vents` value); hand sweep wind and swing; spit arch;
  mortar pump; nursing reach (aim the siphon at the fungling with the existing latch blend); lance stab; net spread;
  gaze (pulp forward, head tracking the beam); the Last Brood rear, pull and collapse.
- Tells use `BossTelegraphs` (cones, rings, lines, glows) in her palette (amber, yolk, spore, crimson, bone). Landing
  rings for the mortar, crack marks for the lances, and the net's circle must read from 15 blocks.
- The Gaze beam, the net, the pools and the binding tendrils are client-drawn with the existing toolkit, through a
  public bridge class in `client/render/world` if the material is package-private (as `OutpouringBrushes` does). No new
  private shader art.
- No vanilla `ParticleTypes` in any touched file; add a source assertion for the new files.

## 11. Review and tests

- **Pure rules (JVM):** band and sector classification; eligibility per position and phase; anti-repeat and decay;
  kite and hug pressure; follow-up conditions and the two-deep cap; variant rolls; every timeline; hit geometry (cones,
  circles, the rear arc, the hand sweep's jump clearance); mortar landing spread; lance line spacing; net and gaze
  timing; Last Brood thresholds and its once-per-fight flag; the new meter rates; grown brood caps; tumor caps and hatch
  timing.
- **A simulated-fight distribution test:** drive the picker through a few thousand decisions with a scripted player who
  spends time in every band and sector, and assert no move exceeds about a third of picks and every band has at least
  two eligible answers per phase.
- **Review client** (`MycophantClientReview`): a posed scene per new pose, live scenes that start each new move in turn
  against the villager dummy, and `hold_live`-style real-player checks for the Claw Pin, the Lattice Net, the Gaze's
  cover and the Last Brood (cut-free and cocooned paths, both nonlethal). Keep `MYCOPHANT_HOLD_JITTER` at 0.0000 for
  pins and cocoons.
- Full `./gradlew.bat test` green; update source pins that the changes move.

## 12. Docs

Update `docs/HEMOMANCY_REFERENCE.md` (the Mycophant phase bullets), the 2026-10-18 rebuild spec's cross-references,
and any wiki page that lists her moves.

## 13. Decided by the user

- The four themes and every example move in §5–§9, including the Last Brood (2026-10-24: "They all look good, let's
  start on them").
- Ebrietas and FromSoftware bosses in general as the reference.

## 14. My calls (to review)

- The band and sector thresholds, the weights, the anti-repeat factors and the kite-pressure numbers.
- Shorter holds (5 s and 4 s) with one fewer hit to break, so the grab stops filling the fight.
- Claw Rake becomes the Rake String rather than standing beside it.
- Grown brood is the existing fungling scaled up, not a new creature.
- Brood tumors are a block (so ordinary line of sight treats them as cover), placed only in owned fights.
- One hazard entity with kinds for the pools, clouds and net.
- The Last Brood triggers once, below 25% in phase 2. If the pull times out she is spent (a 30-tick stagger); during
  the pull she takes half damage and cannot fall below 5%; the binding tendrils sit 6–9 blocks out and can be hit
  anywhere along their length.
- Brood tumors are two blocks tall and the Gaze aims at the player's body centre, so standing close behind a tumor
  blocks it; the pool rim is not cover (it sits flush with the floor).
- Holds become kinds (§9a): only the embrace's sac hold has the meter and the break rules; the Claw Pin and the cocoon
  just run out.
- The brood clutch (funglings armouring her sac) is left for later.

## 15. Left to the builder

- Exact tick counts, damage, radii and weights within the spirit of §6–§9, settled in review.
- Pose values and easing for every new pose.
- Which toolkit material draws each client effect.
- How the binding-tendril anchors and the net are hit-tested.
- Sounds: reuse her existing set and fitting vanilla sounds as the current moves do.
