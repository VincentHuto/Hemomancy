# 07. Bosses (Vesper, Mycophant, Annetta, Court and site bosses) and boss presentation

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (P02, B15). B15 added the Ringmaster to the tag (D-33), made rite sacrifices and `puppet_attention_immune` read it (NQ-07-3) and flipped the GameTest. Left: one line in `AGENTS.md`. |
| T2 | Built (P02, B15). B15 moved the burrow gate and idle threshold into `VesperCombatRules` with tests. The `LORE_REFERENCE.md` line already read 60%. |
| T3 | Built (P02). |
| T4 | Built (P02). Left: a live GameTest for the Vesper victory experience. Skipped in B15: the GameTest servers load no Chamber of Will and `completeVictory` refuses an owner outside it. |
| T5 | Built (P02). `mycophantYieldLogout` is required and passes. |
| T6 | Built (B15): rematch owed flag, `shouldRearmRematch`, `mycophantRematchLine` required and passing. |
| T7 | Done (B15): stills in `build/build2/B15-shots/` with `index.txt`. The owner's look is owed. |
| T8 | Done (P02 and the 2026-11-14 docs pass). |
| CR-16 (no task) | Built (B15): Annetta's max health 51 (NQ-07-1). |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

These rulings keep the boss fights and the presentation layer as built, change four numbers, fix two Mycophant
encounter faults, route dead rewards through the victory, and approve one boss definition for the whole mod.

- Largest pieces of work: the boss definition (T1), the Mycophant win that survives a logout or death (T5), and the
  rematch that re-arms (T6). The number changes are small but each has a second copy on the client boss bar.
- Surprise 1: G-2c is narrower than the sheet says. Specimen capture, the menagerie leash and the Binding tail already
  call `ManipulationReactiveEvents.isBoss`. What is wrong is the definition itself. It is a class-name prefix
  (`common.entity.boss.`), so the Circus Ringmaster is not a boss, and two Saint adds (Seraphae Fragment, Velorum
  Devotee) are. Three hand-kept partial lists also exist (kill blood gain, rite sacrifices, Scarlet Mummer attention).
- Surprise 2: CR-16 cannot be acted on as written. "Lower health to match" gives no number, and the code has two
  things to match (50 damage and 2400 ticks). It is a new question (NQ-07-1).
- Surprise 3: the Mycophant bar notch (`MycophantFrame.notches`, 0.5) and the Crowned Refusal bar notches
  (`VesperCrownedRefusalFrame.WOUNDS`, 0.12 / 0.42 / 0.72) are private copies of the server thresholds. CR-15 and CR-19
  must change both sides.
- Surprise 4: CR-19 moves the first wound to 70%, which is exactly the literal that gates the grounded burrow
  (`0.7F`). CR-28 says keep the burrow as built, so the gate has to move with the anchor (T2).
- No boss, encounter, boss bar or cinematic file was touched by the lash, held-manipulation or HUD work after
  2026-11-11. Three Saint entity files changed after the sheet, none in a way that touches these rows. No conflicts.
- The 2026-11-13 sound audit changes no row here. It notes the Mycophant death file (9.86 s) outruns the 6 s yield,
  which T5 does not change.
- Counts: 19 rows. BUILD 8, KEEP 8, DONE 0, MOOT 0, CONFLICT 0, UNCLEAR 1, OWNER 1, PARKED 1.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| CR-15 | The Mycophant splits into phase 2 at 60% health, not 50%. Supersedes the older "phase 2 at 50%" ruling in `docs/BOSSES.md`. | BUILD | `common/entity/boss/endgame/MycophantCombatRules.java` `phase`: `health <= maxHealth * 0.5F`. Second copy: `client/screen/overlay/boss/frame/MycophantFrame.java` `notches()` returns `{0.5F}`. Last Brood stays at 25% (`MycophantLastBroodRules.THRESHOLD`). | T2 |
| CR-16 | Lower Annetta's health "to match" the break. No number given. | UNCLEAR | `common/entity/boss/annetta/AnnettaKnowlesEntity.java` `setAttributes` MAX_HEALTH 350, armour 8. `AnnettaEncounterRules` `BREAK_MIN_TICKS` 2400, `BREAK_MIN_DAMAGE` 50, `guardedDamage` holds her at 1 HP. `damageTaken` counts the blow after the shield and before armour, so it runs ahead of health lost. Health also drives her cooldowns (`cooldownAfter`) and the neck tick (`AnnettaLooks.tickSwell`). Two numbers could be "matched". See NQ-07-1. | - |
| CR-19 | Crowned Refusal wound anchors become 0.70 / 0.40 / 0.10. Bands become 156 / 156 / 156 of 520, with the last 52 HP never removed. | BUILD | `common/entity/boss/endgame/VesperCombatRules.java` `ANCHOR_THRESHOLDS = {0.72F, 0.42F, 0.12F}` (private). Second copy: `client/screen/overlay/boss/frame/VesperCrownedRefusalFrame.java` `WOUNDS = {0.12F, 0.42F, 0.72F}`. Two literals lean on the old values in `VesperTheCrownedRefusalEntity.java`: `0.42F` (idle ticks, line 730) and `0.7F` (burrow gate, line 689). | T2 |
| CR-20 | Kindled Champion regrowth by crown stage becomes 2 / 3 / 3 health per second. | BUILD | `common/entity/boss/court/KindledChampionRules.java` `REGROWTH = {2.0F, 3.0F, 4.5F}`, read by `regrowthPerSecond`. Bud thresholds 0.66 / 0.33 and `YIELD_AT` 0.1 stay. | T2 |
| CR-21 | The Bloom Warden out-healing a D0 to D2 player is intended. The answer is amends. | KEEP | `common/entity/boss/court/BloomWardenRules.java` `MAX_HEAL_PER_SECOND` 4.0, `BLOOM_HEAL` 0.75, `ROOTED_SHIELD` 0.25, `MAX_BLOOMS` 12. Amends path exists in `BloomWardenEntity.java` (a Giving Light offering ends the fight). Matches. | T8 |
| CR-22 | Give the Evening Star's XP and the Saints' XP through the victory. Delete the Evening Star's loot table, which only repeats the Memory of Vesper. | BUILD | `VesperTheEveningStarEntity.java` `xpReward = 180`, and `completeBloodAbsorption` calls `VesperOrdealManager.completeVictory` then `discard()`, so neither XP nor `loot_table/entities/vesper_evening_star.json` ever fires. `completeVictory` gives no XP. Saints: `SaintTrialEntity` constructor takes 80 (Hemorath) or 120 (Putriciel, Seraphae, Velorum), `die` holds at 1 HP, `endTrial` rolls loot and gives no XP. The Mycophant already does it right (`MycophantEncounterManager.completeVictory`, `giveExperiencePoints(180)`). | T4 |
| CR-27 | Title cards sit on the lower third. Confirmed. | KEEP | `client/render/cinematic/TitleCard.java` class comment: "A boss's name card on the lower third: the epithet above, the name below". Matches. | T8 |
| CR-28 | A grounded burrow stays in Vesper's rotation as built. | KEEP | `VesperTheCrownedRefusalEntity.java` line 688: burrow off cooldown, last attack not a burrow, wings grown or health at or under 70%, 35% roll. Matches. T2 must move the 70% literal so CR-19 does not change when it opens. | T2, T8 |
| CR-29 | The Mycophant's brood clutch and amber failure beat stay deferred. | PARKED | No "clutch" in `common/entity/boss/endgame/Mycophant*.java`. No amber failure beat found. Neither is built. | - |
| PT-81 | A Mycophant win is recorded by owner UUID at the killing blow and settled at the next login. A death in the yield keeps the win too. | BUILD | `MycophantEntity.die` starts the 120-tick yield (`MycophantYieldRules.length`). `tickDeath` calls `MycophantEncounterManager.completeVictory` at the end, which returns early when the owner is offline. `onDeath` cancels the death and calls `failAttempt`, which clears the boss. Probe `PlaythroughEndgameGameTests.mycophantYieldLogout` (`required = false`) asserts both players keep the win. Still lost today. | T5 |
| PT-84 | After a lost rematch the hunt comes back by itself once the retry cooldown has run. | BUILD | `MycophantEncounterManager.failAttempt` sets the cooldown (6000 ticks, `MYCOPHANT_RETRY_COOLDOWN_TICKS`) and says `message.hemomancy.mycophant.expelled` ("It will remember after five minutes in the Gardens"). `tickGardensPlayer` counts the cooldown down, then `MycophantEncounterRules.shouldAccumulate` refuses a defeated player. Only `tryStartRematch` (a lure) starts one. Probe `mycophantRematchLine` fails. | T6 |
| RV-18 | Boss presentation stays as built. | KEEP | `BossSceneDirector.SKIP_TICKS` 20. `client/render/cinematic/scene/PhaseCardScene.java` `LENGTH` 50. Epithets in `en_us.json` match the list in `docs/BOSSES.md`. Frames exist for every boss in `client/screen/overlay/boss/frame/`. "Bars hidden under openings" was not checked per boss. | T8 |
| RV-21 | Boss fight numbers stay as built. | KEEP | `VesperWoundRules` sites CARAPACE, NECK, TAIL, `DRAIN_REQUIRED` 100, `SCAB_RATE` 0.4, `SCAB_FLOOR_FRACTION` 0.5, `HIT_FALLBACK_RATIO` 0.10. `VesperEveningStarAct` Sickles at one third. `MycophantClaimRules` 8% fusion bonus and 0.2 Wing Guard. `MycophantNurseryLayout.CEILING_Y` 18. Annetta's 1 HP guard in `AnnettaEncounterRules.guardedDamage`. All match. CR-15 and CR-19 change two numbers next to this row (the split and the wound thresholds). The more specific rows win. | T8 |
| RV-82 | Broken Church in swamp and dark forest, and thrall or puppet kills credit the owner. Keep. | KEEP | `data/hemomancy/tags/worldgen/biome/has_structure/broken_church.json` lists both. `StainedPriestessEntity.creditedPlayer` exists and is used at the death. Matches. | T8 |
| RV-84 | Vesper's own caster, the mount's head and neck, summoned Vespers burrowing with particles only. Keep. | KEEP | `common/entity/boss/endgame/VesperManipulationCaster.java` exists. `VesperBurrow.java` line 139 handles a summoned Vesper's ground. The head and neck and "particles only" were not checked in the model or by eye. | T8 |
| RV-85 | Mycophant: she/her in player text, 2x render scale, 4.5x5 hitbox, brood capped at 4, Pulp Bloom screen effect. Keep. | KEEP | `en_us.json` `message.hemomancy.mycophant.*` uses she/her (D-16 allows she or they). `MycophantRenderer` scales 2.0. `EntityInit` `mycophant` sized 4.5 x 5.0. `MycophantBroodRules.BROOD_CAP` 4. The Pulp Bloom screen effect was not located by name, only the move. | T8 |
| G-2c, CQ-12 | Approved: one boss definition, and every reader uses it. | BUILD | `ManipulationReactiveEvents.isBoss` (line 548): Ender Dragon, Wither, Warden, Elder Guardian, the `c:bosses` tag, or a class name starting `com.vincenthuto.hemomancy.common.entity.boss.`. About 25 call sites already use it, including `SpecimenJarBlockItem`, `MenagerieLeash`, `BloodBindingManip` and `BloodBindingShaping`. `ThreadRipperManip.protectedBody` adds its own package check and names `CircusRingmasterEntity`. Partial lists: `BloodVolumeEvents` (kill gain, Dragon and Wither only), `HarbingerCardinalRiteEvents.processSacrifices` (Dragon and Wither only), tag `puppet_attention_immune` (12 hand-picked ids). No `hemomancy:bosses` tag exists. | T1 |
| V-6b | Give the two Mycophant spore colours on the 0 to 255 scale so they stop rendering black. | BUILD | `MycophantEncounterManager.tickClaim` lines 137 and 140: `new ParticleColor(0.48F, 0.72F, 0.34F)` and `new ParticleColor(0.92F, 0.66F, 0.24F)`. HutosLib `ParticleColor` takes 0 to 255. These are the only two such calls in `src/main/java` (the third hit, `ParticleBurstPacket.UNTINTED`, is black on purpose). | T3 |
| PR-9 | A screenshot pass over every bar frame, opening scene and phase card, the Mycophant waking and capture, and the Saint rise. | OWNER | `gradle/review-clients.gradle` has `bossPresentationReviewClient` and `mycophantReviewClient`. `BossPresentationClientReview` and `MycophantClientReview` exist under `src/gameTest`. An agent can capture the stills. The owner judges them. | T7 |

## Plan

### T1. One boss definition (size M)

- Rulings: G-2c, CQ-12
- Change:
  - Add entity type tag `hemomancy:bosses`. Values: `#c:bosses`, `minecraft:ender_dragon`, `minecraft:wither`,
    `minecraft:warden`, `minecraft:elder_guardian`, and the Hemomancy bodies `annetta_knowles`, `stained_priestess`,
    `latent_annetta_infection`, `vesper_crowned_refusal`, `vesper_evening_star`, `mycophant`, `naeglerophaeon`,
    `phlegethosoma`, `bloom_warden`, `kindled_champion`, `hemorath`, `putriciel`, `seraphae`, `velorum`,
    `circus_ringmaster`. The two Saint adds (`seraphae_fragment`, `velorum_devotee`) stay in for now so nothing changes
    for them (NQ-07-2).
  - One helper reads the tag and nothing else: `isBoss(LivingEntity)` in `HemoEntityPredicates`. Remove the four
    `instanceof` checks and the class-name prefix from `ManipulationReactiveEvents.isBoss` and point every caller at
    the helper. Declare the tag key beside the others in `EntityInit` (`createTag`).
  - `ThreadRipperManip.protectedBody`: drop the package check and the `CircusRingmasterEntity` test. Keep
    `CircusPerformerEntity` (performers are not bosses).
  - Fold in the partial lists as planned under NQ-07-3: `processSacrifices` passes the helper, tag
    `puppet_attention_immune` becomes `#hemomancy:bosses`, and the kill blood gain multiplier stays Dragon and Wither
    only and is named as the one exception.
  - Effect on play: the Ringmaster becomes a boss for every reader (halved Binding, damage caps, no Paralysis, no
    leash, jar or Hematic Command). Naeglerophaeon, the Latent Infection, Phlegethosoma, the Court bosses and the
    Ringmaster become immune to the Scarlet Mummer's attention pull. No Hemomancy boss can be a rite sacrifice.
- Files: `src/main/resources/data/hemomancy/tags/entity_type/` (new `bosses.json`, edit
  `puppet_attention_immune.json`), `common/entity/HemoEntityPredicates.java`, `common/init/EntityInit.java`,
  `common/manipulation/ManipulationReactiveEvents.java`, `common/manipulation/ductilis/ThreadRipperManip.java`,
  `common/rite/harbinger/HarbingerCardinalRiteEvents.java`, and the callers: `common/damage/SchoolCombatEvents.java`,
  `SchoolAwareness.java`, `SchoolStates.java`, `common/entity/menagerie/MenagerieLeash.java`,
  `common/entity/projectile/BloodNeedleEntity.java`, `common/item/harbinger/morphlings/DeadMansRopeMorphlingItem.java`,
  `MarrowplumeMorphlingItem.java`, `MorphlingLock.java`,
  `common/item/harbinger/tile/functional/SpecimenJarBlockItem.java`, `common/manipulation/animus/BloodBindingManip.java`,
  `BloodBindingShaping.java`, `HematicRiposteEvents.java`, `common/manipulation/congeatio/GlacialGraspShaping.java`,
  `OsseousBloomManip.java`, `RimeboundSentenceManip.java`, `common/manipulation/ductilis/Paralysis.java`,
  `common/manipulation/HematicCommandManager.java`, `common/manipulation/mortem/ExsanguinateManip.java`.
- Tests: new guardrail `BossDefinitionGuardrailTest` under `src/test/java/com/vincenthuto/hemomancy/architecture/`
  (no class-name or package boss check, no Dragon-and-Wither pair outside the listed exception, and every living type
  registered from a class under `common/entity/boss/` is in the tag or on a short named not-a-boss list). Extend
  `ManipulationBalancePass2TargetingGameTests` (lines 68 to 72) with each Hemomancy boss and the Ringmaster. Update
  `HematicRiposteSourceTest` (asserts the old call text) and `StrandGameTests` line 981. Regenerate
  `docs/architecture/evidence/source-text-impact-map.csv`.
- Docs: `docs/architecture/ARCHITECTURE_STANDARDS.md` (rule and guardrail index), `docs/BOSSES.md` and
  `docs/MANIPULATIONS_AND_CASTING.md` (G-2c to "Rulings that bind"), `docs/OPEN_QUESTIONS.md` roll-up (G-2c, CQ-12),
  `AGENTS.md` (one line naming the helper and the guardrail).
- Depends on: nothing. Chunk 04 LP-16 (the new boss Binding limit: a cap by max health and a chance to fail at high
  health) and chunk 15 D-33 (the Ringmaster on the boss kit) both read this definition and should build after it.
  `GlacialGraspShaping` was rebuilt on 2026-11-12. The edit there is a call rename only.

### T2. Fight numbers: Mycophant split, Crowned Refusal anchors, Champion regrowth (size S)

- Rulings: CR-15, CR-19, CR-20, CR-28
- Change:
  - `MycophantCombatRules.phase`: `0.5F` to `0.6F`. Bands of 720 become 288 / 252 / 144 (Last Brood, raw 288 at
    half damage) / 36. `MycophantFrame.notches()`: `{0.5F}` to `{0.6F}`, read from the rules instead of a literal.
    Fix the comment in `MycophantSplitRules` ("at half health").
  - `VesperCombatRules.ANCHOR_THRESHOLDS`: `{0.72F, 0.42F, 0.12F}` to `{0.70F, 0.40F, 0.10F}`. Expose it and have
    `VesperCrownedRefusalFrame.WOUNDS` read it. In `VesperTheCrownedRefusalEntity`: the idle-ticks literal `0.42F`
    becomes the second anchor (`0.40F`), and the burrow gate `0.7F` becomes `0.68F` so the grounded burrow still opens
    2% under the first wound, as it does today.
  - `KindledChampionRules.REGROWTH`: `{2.0F, 3.0F, 4.5F}` to `{2.0F, 3.0F, 3.0F}`.
- Files: `common/entity/boss/endgame/MycophantCombatRules.java`, `MycophantSplitRules.java`,
  `client/screen/overlay/boss/frame/MycophantFrame.java`, `common/entity/boss/endgame/VesperCombatRules.java`,
  `VesperTheCrownedRefusalEntity.java`, `client/screen/overlay/boss/frame/VesperCrownedRefusalFrame.java`,
  `common/entity/boss/court/KindledChampionRules.java`.
- Tests: `MycophantCombatRulesTest` (361 / 360 become 433 / 432). `VesperCombatRulesTest` (374.4 / 218.4 / 62.4
  become 364 / 208 / 52). `BossBarFramePhaseEndgameTest` (`phaseOf(0.72F)` becomes `0.70F`).
  `MycophantImplementationSourceTest.oldSavesResumeIdleWithThePhaseFromHealth` still holds (300 and 500 of 720).
  `CourtFollowerGameTests` line 347 still holds (3 is above 2). Add a JVM test that each frame's notches equal the
  server thresholds.
- Docs: `docs/BOSSES.md` (the Vesper and Mycophant paragraphs and "Rulings that bind", where "Phase 2 at 50%" is
  replaced), `docs/HEMOMANCY_REFERENCE.md` (line 1750 "The split at half health", line 2223 "Wounds (72%, 42%, 12%
  health)"), `docs/BRINED_COURT.md` (Champion regrowth), `wiki/Lore-and-Story.md` line 421 ("At half health").
- Depends on: nothing.

### T3. Mycophant spore colours (size S)

- Rulings: V-6b
- Change: in `MycophantEncounterManager.tickClaim`, `new ParticleColor(0.48F, 0.72F, 0.34F)` becomes
  `new ParticleColor(122, 184, 87)` (green) and `new ParticleColor(0.92F, 0.66F, 0.24F)` becomes
  `new ParticleColor(235, 168, 61)` (amber).
- Files: `common/encounter/MycophantEncounterManager.java`.
- Tests: none needed. Seen in T7 (the capture scene).
- Docs: `docs/architecture/CONSOLIDATION_PROGRESS.md` row V-6b to done, `docs/OPEN_QUESTIONS.md` roll-up.
- Depends on: nothing. Same file as T5 and T6, so build them one after another.

### T4. Rewards through the victory (size S)

- Rulings: CR-22
- Change:
  - `VesperOrdealManager.completeVictory`: add `owner.giveExperiencePoints(180)` beside the advancement, on every
    completed ordeal (the Mycophant's rule). Set `xpReward = 0` in `VesperTheEveningStarEntity`.
  - Delete `loot_table/entities/vesper_evening_star.json` (moved to `removed_assets/`). The Memory of Vesper keeps
    arriving through the `PendingDelivery` claim.
  - `SaintTrialEntity`: keep the constructor's number as the trial's XP (80 Hemorath, 120 the other three) and give it
    in `endTrial` to the same recipient the loot goes to. No recipient (an abandoned trial) gives none. A forced-door
    win gives full XP. Set the vanilla `xpReward` to 0.
  - `mycophant.json` stays. It pays an unowned Mycophant, which really dies.
- Files: `common/encounter/VesperOrdealManager.java`, `common/entity/boss/endgame/VesperTheEveningStarEntity.java`,
  `common/entity/boss/saint/SaintTrialEntity.java`,
  `src/main/resources/data/hemomancy/loot_table/entities/vesper_evening_star.json`.
- Tests: `EndgameBossRewardResourceTest` (drop the `vesperLoot` read and its "documents Memory of Vesper" assertion,
  add an assertion that `completeVictory` gives the XP). `EntityLootTableCoverageResourceTest` line 56 (remove the
  `vesper_evening_star` guaranteed drop, list the type as having no table on purpose). `SaintKitGameTests`
  (`saint_kit_validation`): `endTrial` gives the recipient the XP once. `PlaythroughEndgameGameTests`: the Vesper
  victory raises the player's XP.
- Docs: `docs/HEMOMANCY_REFERENCE.md` (section 5.10 "is the final kill" wording, line 4587 "Vesper Evening Star
  drop", section 5.8 Saint rewards), `docs/BOSSES.md`, `docs/SAINT_MAUSOLEUMS.md`.
- Depends on: nothing.

### T5. A Mycophant win survives a logout or death in the yield (size M)

- Rulings: PT-81
- Change:
  - At the killing blow (`MycophantEntity.die`, owned fights only) record the owner's UUID as "won, not settled" in
    saved server data (a set on `ChamberOfWillManager`, or a small SavedData beside it). Not player persistent data.
  - Split the body of `completeVictory` into a settle step that needs only the player. It runs at the end of the
    yield when the owner is online, and clears the record.
  - `onLogin`: if the UUID is recorded, settle before anything else (clear the owned boss, mark defeated, queue the
    Tendril through `PendingDelivery`, XP, advancements, heir, leave the Chamber). The boss must not respawn through
    `tickArenaPlayer`.
  - `onDeath` and `onChangedDimension`: if the UUID is recorded, settle the win at once instead of `failAttempt`.
    The death is still cancelled, as today.
  - Assumption: a death in the yield settles at once because the player is online. Only the logout waits for login.
- Files: `common/encounter/MycophantEncounterManager.java`, `common/encounter/ChamberOfWillManager.java`,
  `common/entity/boss/endgame/MycophantEntity.java`.
- Tests: `PlaythroughEndgameGameTests.mycophantYieldLogout` passes and becomes `required = true`. Add a check that
  the relogged player has the Tendril once and is out of the Chamber. Update `PlaythroughFingerprints` if the victory
  line order changes. `MycophantImplementationSourceTest` line 66 reads `completeVictory` by text and may need the new
  method name. `EndgameBossRewardResourceTest` asserts two orderings inside `MycophantEncounterManager` and must still
  hold.
- Docs: `docs/BOSSES.md` and `docs/PLAYTHROUGH_TESTS.md` (D-81 from "Open questions" to "Rulings that bind"),
  `docs/OPEN_QUESTIONS.md` roll-up (PT-81), `docs/HEMOMANCY_REFERENCE.md` (Mycophant Hunt, the yielding).
- Depends on: T3 (same file). The AGENTS.md rule on First Mycophant Tendril delivery binds: actual insertion, no
  replacement claims inferred, dead-player ticks keep the claim.

### T6. The rematch re-arms after the cooldown (size M)

- Rulings: PT-84
- Change:
  - New server-only boolean on the `InitiatoryDegree` attachment, "rematch owed" (new NBT key, not synced, so no
    payload change). `failAttempt` sets it when the lost fight was a rematch. The settle step from T5 clears it.
  - New rule in `MycophantEncounterRules` (D8, Apotheos, defeated, owed, cooldown at 0, in the Gardens, alive, not
    creative or spectator, no encounter active). When it holds, `tickGardensPlayer` calls `beginClaim(player, true)`.
  - The cooldown already counts only while the player is in the Gardens, which is what the line says. The line
    `message.hemomancy.mycophant.expelled` stays as written.
  - Planned default: every lost rematch re-arms again until the player wins (NQ-07-4).
- Files: `common/encounter/MycophantEncounterManager.java`, `common/encounter/MycophantEncounterRules.java`,
  `common/capability/player/harbinger/degree/InitiatoryDegree.java`, `IInitiatoryDegree.java`.
- Tests: `MycophantEncounterRulesTest` (the new rule). Rewrite `PlaythroughEndgameGameTests.mycophantRematchLine`:
  it now asserts `shouldAccumulate` for a defeated player, which stays false. It should run the cooldown down in the
  Gardens state and assert the claim begins, then become `required = true`. If the flag is ever synced, bump
  `PacketHandler.PROTOCOL_VERSION` and rewrite `src/test/resources/network/payload-registry.txt`.
- Docs: `docs/BOSSES.md` and `docs/PLAYTHROUGH_TESTS.md` (D-84 to "Rulings that bind"), `docs/OPEN_QUESTIONS.md`
  roll-up (PT-84), `docs/HEMOMANCY_REFERENCE.md` (Mycophant Hunt, rematch and Fruiting Lure).
- Depends on: T5 (same file, and the settle step clears the flag).

### T7. Stills for the owner's presentation pass (size S)

- Rulings: PR-9
- Change: no code. Launch `runBossPresentationReviewClient` (every bar frame, opening and phase card, and the Saint
  rise) and `runMycophantReviewClient` (waking and capture). Skip the first-load Accessibility screen and confirm each
  launch got past it. Collect the stills in one folder with a short index. The new notches (T2) and the spore colours
  (T3) should be visible in them.
- Files: `gradle/review-clients.gradle` (rows `bossPresentationReviewClient`, `mycophantReviewClient`, read only),
  `src/gameTest/java/com/vincenthuto/hemomancy/gametest/BossPresentationClientReview.java`,
  `MycophantClientReview.java`.
- Tests: none.
- Docs: `docs/BOSSES.md` "Proof owed and known gaps" once the owner has looked.
- Depends on: T2 and T3 built. No other Gradle run in progress.

### T8. Record the keep rulings (size S)

- Rulings: CR-21, CR-27, CR-28, CR-29, RV-18, RV-21, RV-82, RV-84, RV-85
- Change: docs only. In `docs/BOSSES.md` move "Title card placement" and "Is a grounded burrow in the rotation" from
  "Open questions" to "Rulings that bind". Mark the presentation, Annetta, Vesper and Mycophant lists under "Calls made
  without the user" as approved 2026-11-13, except the two numbers T2 changes. Keep the brood clutch and the amber
  failure beat under "Open questions" as deferred. Fix the stale status line (2026-11-06). Record CR-21 in
  `docs/BRINED_COURT.md`.
- Files: `docs/BOSSES.md`, `docs/BRINED_COURT.md`, `docs/OPEN_QUESTIONS.md` roll-up.
- Tests: none.
- Docs: as above.
- Depends on: T2 (so the numbers recorded are the new ones).

## Conflicts with recent work

None.

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-07-1 | CR-16 | What number is Annetta's health lowered to? The break needs 50 damage and 2400 ticks, and she holds at 1 HP. Health also sets how frantic she is and how swollen the tick looks. | A: 51 (50 damage plus the 1 HP guard). The bar empties in roughly the first fifteen seconds of hitting, then she stands at 1 HP, fully frantic, until the two minutes are up. B: a larger number picked after a timed playtest, so the bar runs out near the two-minute mark. C: leave 350. | A, because it is the only number the code fixes. Judge the long empty-bar wait in play. If it reads as a stuck boss, move to B. |
| NQ-07-2 | G-2c | The Seraphae Fragment (12 HP) and the Velorum Devotee (30 HP) count as bosses today only because their classes sit in the boss package. Do they stay bosses? | A: stay in the tag. Nothing changes for them. B: out. They take full Binding, Paralysis, Glacial Grasp and the leash and Thread Ripper rules like any creature. | A for this build, because B changes two Saint fights nobody has replayed. Revisit in the Saint pass. |
| NQ-07-3 | G-2c | Three hand-kept lists also decide "boss". Should they follow the one definition? Kill blood gain x5 (Dragon and Wither only). Rite sacrifice exclusion (Dragon and Wither only, so a named Hemomancy boss inside a rite can be fed on). Scarlet Mummer attention immunity (12 ids, missing Naeglerophaeon, the Latent Infection, Phlegethosoma, the Court bosses and the Ringmaster). | A: sacrifice and attention follow the definition. Blood gain stays Dragon and Wither only. B: all three follow (a killed Stained Priestess would then give about 2600 mL). C: leave all three and list them as named exceptions. | A. The first two look like gaps. The blood multiplier is a balance number and should not grow by accident. |
| NQ-07-4 | PT-84 | When the re-armed rematch is lost again, does it re-arm again? | A: yes, every loss, until the player wins. A beaten Apotheos who loses a rematch is taken again after each five minutes spent in the Gardens. B: once per lure. A second loss ends it until a new lure is used. | A. It is how the first hunt already behaves, the fight never kills, and it is what the expelled line promises. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| G-2c, CQ-12 | Build the tag, the helper and the guardrail. | Build | T1 (defaults for NQ-07-2 and NQ-07-3 are planned) |
| CR-15, CR-19, CR-20 | Change the three numbers and the two bar-notch copies. | Build | T2 |
| V-6b | Two colour values. | Build | T3 |
| CR-22 | XP through the victory, delete the duplicate table. | Build | T4 |
| PT-81 | Record the win by UUID, settle at login or at once on death. | Build | T5 |
| PT-84 | Re-arm the rematch. | Build | T6, NQ-07-4 |
| CR-16 | Pick Annetta's health. The change itself is one attribute in `AnnettaKnowlesEntity.setAttributes` plus docs (size S). | Owner | NQ-07-1 |
| PR-9 | Look through the stills and rule on frames, openings, cards, the Mycophant waking and capture, and the Saint rise. | Owner | T7 |
| CR-19 | Play the Crowned Refusal once to confirm the burrow still first shows after the first wound is drained. | Owner | T2 built |
| CR-15 | Play the Mycophant end to end. The last recorded play predates the 2026-10-24 moveset, and phase 2 is now the longer half. | Owner | T2 built |
| CR-29 | Brood clutch and amber failure beat. | Parked | A later Mycophant pass |
| CR-21, CR-27, CR-28, RV-18, RV-21, RV-82, RV-84, RV-85 | Record the rulings in the topic docs. | Build | T8 |
| RV-84, RV-85, RV-18 | Three things were not verified by reading: summoned Vespers burrow "with particles only", the Pulp Bloom screen effect by name, and bars hidden under every opening. T7's stills cover the last two. | Later | T7 |
| LP-16 (chunk 04), D-33 (chunk 15) | Both read the boss definition. | Later | T1 built |
