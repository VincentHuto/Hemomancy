# 06. Creatures, spawns, blood profiles and placeholder mob art

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (P06, rest in B13 on 2026-11-15): the Overworld Gardens is in `#minecraft:is_overworld` with vanilla ores; its stink horns and field fungi now come from the shared modifiers (no feature twice in one step); the dead mooshroom row is gone. |
| T2 | Built (P06, rest in B13): explicit Vampire Bat list (nether wastes, crimson forest, basalt deltas, the basin) and its rewritten test. Polyp and Desiccant recorded as built (NQ-06-14, NQ-06-15). P06 cleanup also done: the two `main`-method tests are JUnit, the dead centipede constant is gone. |
| T3 | Built (P06). |
| T4 | Built (P06), except the attribute drop on the four teachers. That part is closed: a Misbegotten successor fights with the attribute. |
| T5 | Built (P06). |
| T6 | Built (B13): the T6 table as written, plus the polar bear drawn with an ordinary vial (the answer's "so do polar bears"). |
| T7 | Built (B13): the four drops, player kills only, plus 0 to 1 per Looting level. |
| T8 | Built (B13): 25 mL per 160 ticks. |
| T9 | Built (B13): the T9 table; the Fervent Chitinite has its own attributes. |
| T10 | Built (B13): `GardensRosterRules` and `GardensRosterSpawnEvents`, natural and chunk-generation spawns in the dimension only. |
| T11 | Built (B13): health max(60, max health x 3 + 30 per Degree over 6), damage 8 + 2 per Degree over 6. |
| T12 | Built 2026-11-15 (B14) with NQ-06-7, NQ-06-8 and NQ-06-10: natural spawn gone, `PhlegethosomaSummoning` ichor link, per-pool count fading like chum, 500 mL call, one per player, 60-second dive with no loot, the Alchemist line and the Liber page. |
| T13 | Built (B36, 2026-11-16): `hemojelly.py` and `myelin_borer.py` generate both mobs with glow layers, review rows `hemojellyReviewClient` and `myelinBorerReviewClient`; `effect_icons.py` draws the 15 icons that copied another effect, so all 28 are their own (`effectIconReviewClient`). The owner judges the looks. |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

These rulings fix the Overworld Fungal Gardens spawns, move three spawn rules, retune a handful of creatures, fill the
blood profile gaps, and turn the Phlegethosoma from a natural spawn into a summon. Five rows are small and ready to
build now (T1 to T5). Seven rows are "yes" answers with no numbers, so each has a proposed table here and a new question.
The largest pieces are the Phlegethosoma summon (CR-7, a new design) and the placeholder mob art (VX-16).

Surprises from the code:

- `fungal_gardens_worldgen.py` does not write the Overworld `fungal_gardens` biome at all. It writes the seven dimension
  biomes only. The live list is the JSON. `BiomeInit.fungalGardens` has no caller.
- A test pins the Puppeteer's daylight spawn as intended (`BloodDrunkPuppeteerPolishTest`). CR-3 reverses it.
- The audit's "no wild Congeatio source" is wrong. It read the `hemomancy` profiles only. Thirteen vanilla creatures
  carry Congeatio today, the polar bear among them. The goat is Ductilis.
- The Circus faculty's attack attribute is used now (`CircusFacultyEntity.strikeDefense`). Only four Harbinger teachers
  still carry an unused one.
- The Will Anchor is a live entity. Only the Blood Bullet is dead. The Leech already draws a real texture.
- The Gardens mobs are shared with the Overworld biome, the savanna mounds and the Mycophant's brood, so CR-2 cannot
  simply raise base stats.

Nothing in this chunk touches the lash, HUD or manipulation work of 2026-11-12 and 2026-11-13.

Counts: 21 rows. BUILD 16, KEEP 5, DONE 0, MOOT 0, CONFLICT 0, UNCLEAR 0, OWNER 0, PARKED 0.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| CR-1 | Fix the Overworld Fungal Gardens spawns in the generator with packs of 1 to 4. Delete the other spawn list. | BUILD | `src/main/resources/data/hemomancy/worldgen/biome/fungal_gardens.json` is live: Abhorrent Thought 10 8-8, Fargone 10 8-8, bat 10 8-8, mooshroom 8 4-8, urchin 1 1-2. `BiomeInit.fungalGardens` has no caller (grep). `tools/worldgen/fungal_gardens_worldgen.py` `BIOME_SPAWNS` has no `fungal_gardens` entry, so the generator must learn the biome first. Bat 8-8 is the vanilla default, not a defect. | T1 |
| CR-2 | Raise the Gardens dimension roster to D7 to D8 strength, after CR-1. No numbers given. | BUILD | `setAttributes` in `FunglingEntity` 7/1, `FargoneEntity` 16/3, `ThirsterEntity` 20/4, `LumpOfThoughtEntity` 24/5, `HunterFungusEntity` 26/5, `ErythromyceliumEruptusEntity` 30/6, `AbhorrentThoughtEntity` 40/7. Fargone and Abhorrent Thought also spawn in the Overworld biome, Chthonians in savanna mounds, Funglings in the Mycophant's brood. | T10 |
| CR-3 | Blood-Drunk Puppeteers spawn at night (the monster light rule). | BUILD | `BloodDrunkPuppeteerEntity.canSpawnHere` calls `checkMobSpawnRules`. `BloodDrunkPuppeteerPolishTest` asserts that call and forbids `checkMonsterSpawnRules`. Modifier weight 24, pack 1-1. | T2 |
| CR-4 | The Desiccant's 250 mL sting is on purpose and stays. The Lantern Tick may drain more than 8 mL per 160 ticks. No number given. | BUILD | `DesiccantEntity.STING_BLOOD_DRAIN` 250. `LanternTickRules.BLOOD_DRAIN_PER_TICK_BITE` 8, `LATCH_DRAIN_INTERVAL_TICKS` 160. That is 1 mL a second, equal to plain regeneration, so a latched tick nets zero today. | T8 |
| CR-5 | Every template-stat mob gets its own numbers. No numbers given. | BUILD | 7 HP, 1 damage, speed 0.3 in `AbstractChitiniteEntity`, `FunglingEntity`, `EnthralledDollEntity`, `BarbedUrchinEntity`. `EntityInit` line 1228 gives `fervent_chitinite` `ChitiniteEntity.setAttributes()`. There is one Fervent Chitinite type (Nether, basalt deltas), not an Overworld and a Nether one. | T9 |
| CR-6 | The Queen is a once-per-mound spawn, not a miniboss. She stays jar-able. | KEEP | `ChthonianQueenEntity` 60 HP. `SpecimenCaptureRules.MAX_HEALTH` 60. She is in `tags/entity_type/specimen_jar_capturable.json`. `TermiteMoundFeature` places her. | none |
| CR-7 | The Phlegethosoma stops spawning naturally. A player summons it by projecting 500 mL of blood into the basin's ichor. | BUILD | Natural spawn: `neoforge/biome_modifier/add_phlegethosoma.json` weight 1, with `PhlegethosomaSpawnRules.canSpawn`. Projection already hits fluids (`SanguineProjectionTargeting.pick` with `ClipContext.Fluid.ANY`). `ChumPools.tryProject` takes only `FluidTags.WATER`, so ichor falls through to the stain today. No summon path exists. | T12 |
| CR-8 | Venom-Rib Centipede to 26 HP. Weight unchanged. | BUILD | `VenomRibCentipedeRules.MAX_HEALTH` 34. `VenomRibCentipedeRulesTest.keepsRareMiniBossScale` asserts at least 28, so the test changes too. Modifier weight 4. | T3 |
| CR-9 | Hemolymphopoda ambient weight 10. | BUILD | `neoforge/biome_modifier/add_hemolymphopoda.json` weight 30, packs 2-5. Rockpool Shore (30) and Mycelial Depths (20) list it in their own biome files. | T2 |
| CR-10 | Vampire Bats are the Nether's ambient except in warped forest and soul sand valley. Morphling Polyps spawn rarely in all biomes. Desiccants spawn at all times in deserts and dry biomes. | BUILD | Bat: `vampire_bat_spawnlist.json` is `#minecraft:is_nether` (all five vanilla Nether biomes plus the basin). `VampireBatColonyResourceTest` pins that exact tag. Polyp: weight 3, `#minecraft:is_overworld`, water throttled 1 in 40. Already rare and Overworld-wide. Desiccant: Monster category, no light check, weight 6, desert, three badlands, three savannas, only on sand, sandstone or terracotta. Already spawns at all times. | T2 |
| CR-11 | Hematic Constructs stay glass cannons. | KEEP | `HematicConstructEntity.setAttributes` 12 HP, `BASE_DAMAGE` 8. | none |
| CR-12 | Leave the End band. | KEEP | `MyelinBorerEntity` 8 HP, 2 damage. `ChoirKeeperEntity` 10 HP. | none |
| CR-13 | Accept the Excoriated at 52/6/8. The basin is a D5 commission. | KEEP | `ExcoriatedEntity.setAttributes` 52 HP, 8 damage, 6 armour. | none |
| CR-14 | Wills scale harder with Degree. They are tests sent by the hivemind. No numbers given. | BUILD | `WillCombatRules.sentStats`: health max(28, player max health x 1.2 + 8 per Degree over 6), damage 6 + 1.25 per Degree over 6. That is 28, 32, 40 HP and 6, 7.25, 8.5 damage at D6, D7, D8. Sent Wills appear from D6 only (`WillCompositionRules.compose`). `WillCombatRulesTest` pins the formula text. | T11 |
| CR-23 | Add blood profiles for the creatures that have none. Goats, polar bears and other cold animals carry Congeatio. | BUILD | 29 living types have no file (script over `EntityInit` and `blood_profiles`). 18 are real creatures, listed in T6. `data/minecraft/blood_profiles/polar_bear.json` is already Congeatio. `goat.json` is Ductilis. Vanilla wolf and slime have no file. | T6 |
| CR-24 | Give the Gardens mobs, the Whale and the Queen tempers. No tempers named. | BUILD | Empty `tempers` in `abhorrent_thought`, `lump_of_thought`, `erythromycelium_eruptus`, `hunter_fungus`, `fungling`, `chthonian_queen`, `mnemonic_whale` (all `data/hemomancy/blood_profiles/`). Also empty: `bog_revenant`, `mortarbound`, `ice_fish`, `facsimile_spider`, `facsimile_wolf`. | T6 |
| CR-25 | Antibody and Velorum Devotee keep dropping nothing. Small drops for the other four. No items named. | BUILD | `loot_table/entities/pyrosome.json`, `hagfish.json`, `blood_lantern_jelly.json` have empty pools. `bloodlicker.json` does not exist. `antibody.json` and `velorum_devotee.json` are empty and stay. | T7 |
| FX-4 | Move hard-coded mob damage into attributes. Drop the unused attack attribute on non-fighting NPCs. | BUILD | Literals: `CircusFireEaterEntity` 4.0F, `CircusStiltWalkerEntity` 3.0F, `CircusAcrobatEntity` 4.0F, `VampireBatEntity` 1.0F or 2.0F. Unused attack 4 on the Alchemist, Artificer, Cicatrix Anchorite and Mnemonist. The Vicar has a `MeleeAttackGoal` (and adds the attribute twice). `CircusFacultyEntity.strikeDefense` now calls `doHurtTarget`, so the faculty's attribute is used. | T4 |
| VX-15 | Point the Leech at a real texture. Delete the Blood Bullet and Will Anchor renderers if those entities are dead. | BUILD | `LeechRenderer.getTextureLocation` already returns `LeechEntity.getLeechTypeName()` (real files). Its `TEXTURE` constant is dead. Nothing creates a `BloodBulletEntity`. `WillAnchorEntity` is live (`WillAmbushDirector`, `WillSubcommand`) and its renderer draws lines only. `modelwretchedwill.png` sits in `removed_assets/`, removed on purpose with the Wretched Will, not moved by another job. | T5 |
| VX-16 | Placeholder art order: mobs first, then the effect icons. | BUILD | `textures/entity/hemojelly/model_hemojelly.png` (64x32) and `textures/entity/myelin_borer/model_myelin_borer.png` (32x32) each hold one colour. `textures/mob_effect` has 64 icons, 28 of them in 13 identical sets (md5). No generator for either mob in `tools/model_export`. Blocks, sabatons, Forge and Loom not rechecked. | T13 |
| RV-27 | Keep the wayside and Harbinger site rebuilds and the Brined Votary and heart-node remodels until walked. | KEEP | `tools/worldgen/wayside_sites.py`, `tools/worldgen/harbinger_sites.py`, `tools/model_export/brined_votary.py`, `tools/model_export/heart_node.py` exist. None changed after 2026-11-11. | none |

## Plan

### T1. Overworld Fungal Gardens spawns (size S)

- Rulings: CR-1
- Change: teach `fungal_gardens_worldgen.py` the Overworld biome. Add a spawn table for `fungal_gardens` and a write step
  that sets `spawners` only (the existing `write_biomes` also rewrites `features`, which the Overworld biome must keep).
  Monsters: Abhorrent Thought weight 10 packs 1 to 4, Fargone weight 10 packs 1 to 4. Leave the bat (vanilla default
  8-8), mooshroom and urchin rows. Re-run the generator. Delete `BiomeInit.fungalGardens` and its unused imports (store
  the removed method under `removed_assets/`). If NQ-06-1 is yes, also add `hemomancy:fungal_gardens` to
  `data/minecraft/tags/worldgen/biome/is_overworld.json` and the vanilla Overworld ore features.
- Files: `tools/worldgen/fungal_gardens_worldgen.py`,
  `src/main/resources/data/hemomancy/worldgen/biome/fungal_gardens.json`,
  `src/main/java/com/vincenthuto/hemomancy/common/init/BiomeInit.java`,
  `src/main/resources/data/minecraft/tags/worldgen/biome/is_overworld.json`
- Tests: add a JVM resource test beside `FieldMycologyResourceTest` that no `fungal_gardens.json` monster pack exceeds
  4. `FacsimileResourcesTest` and `FieldMycologyResourceTest` read this file and must stay green.
- Docs: `docs/HEMOMANCY_REFERENCE.md` 26.5 and 28.1. Mark C-5 in `docs/audits/2026-11-02-cleanup/README.md`.
  `docs/OPEN_QUESTIONS.md` ruling.
- Depends on: nothing. NQ-06-1 for the tag and ores.

### T2. Three spawn edits (size S)

- Rulings: CR-3, CR-9, CR-10
- Change:
  - Puppeteer: `BloodDrunkPuppeteerEntity.canSpawnHere` uses `Monster.checkMonsterSpawnRules`. Weight 24 stays. It will
    also spawn in dark caves under those biomes by day, as any monster does.
  - Hemolymphopoda: `add_hemolymphopoda.json` weight 30 to 10. Packs 2 to 5 stay. The Rockpool Shore and Mycelial
    Depths rows stay.
  - Vampire Bat: `vampire_bat_spawnlist.json` becomes `minecraft:nether_wastes`, `minecraft:crimson_forest`,
    `minecraft:basalt_deltas`, `hemomancy:phlegethontic_basin` (NQ-06-9 confirms the basin and the list form).
  - Polyp and Desiccant: no change. Record both as rulings.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/BloodDrunkPuppeteerEntity.java`,
  `src/main/resources/data/hemomancy/neoforge/biome_modifier/add_hemolymphopoda.json`,
  `src/main/resources/data/hemomancy/tags/worldgen/biome/vampire_bat_spawnlist.json`
- Tests: flip the two source assertions in
  `src/test/java/com/vincenthuto/hemomancy/common/entity/mob/monster/BloodDrunkPuppeteerPolishTest.java`. Rewrite
  `VampireBatColonyResourceTest.coloniesLiveOnlyInTheNether` to assert the four biomes and the absence of warped forest
  and soul sand valley. `MorphlingPolypResourceTest` already pins weight 3.
- Docs: `docs/HEMOMANCY_REFERENCE.md` 26.1, 26.2, 26.5. `wiki/World-Content.md`.
- Depends on: nothing

### T3. Venom-Rib Centipede 26 HP (size S)

- Rulings: CR-8
- Change: `VenomRibCentipedeRules.MAX_HEALTH` 34.0 to 26.0. Modifier weight 4 unchanged. (`NATURAL_SPAWN_WEIGHT` in the
  rules class says 3 and nothing reads it. Leave or delete, not part of the ruling.)
- Files: `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/VenomRibCentipedeRules.java`
- Tests: `VenomRibCentipedeRulesTest.keepsRareMiniBossScale` asserts at least 28. Change it to 26.
- Docs: `docs/HEMOMANCY_REFERENCE.md` 26.1
- Depends on: nothing

### T4. Mob damage into attributes (size S)

- Rulings: FX-4
- Change: add `ATTACK_DAMAGE` to the Fire Eater (4), Stilt Walker (3) and Acrobat (4) builders and read it where the
  literal is. `VampireBatEntity.setAttributes` adds `ATTACK_DAMAGE` 1. A manifested aggregate bites for twice the
  attribute. Remove `ATTACK_DAMAGE` from the Alchemist, Artificer, Cicatrix Anchorite and Mnemonist. Keep it on the
  Vicar (remove the duplicate add) and on the Circus faculty. Before removing, read the 14 call sites of
  `getAttributeValue(Attributes.ATTACK_DAMAGE)` in main. A generic caller would throw on an NPC without the attribute.
- Files: `CircusFireEaterEntity.java`, `CircusStiltWalkerEntity.java`, `CircusAcrobatEntity.java` (all under
  `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/`),
  `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/VampireBatEntity.java`,
  `HarbingerAlchemistEntity.java`, `HarbingerArtificerEntity.java`, `HarbingerCicatrixAnchoriteEntity.java`,
  `HarbingerMnemonistEntity.java`, `HarbingerVicarEntity.java` (under `.../common/entity/npc/harbinger/`)
- Tests: no test pins these literals that I found. The Circus and ecology GameTests cover the hits. Add one GameTest
  that a teacher without the attribute still loads and ticks.
- Docs: `docs/CIRCUS.md` if it quotes the damage values (not checked)
- Depends on: nothing

### T5. Missing-texture cleanup (size S)

- Rulings: VX-15
- Change: delete the dead `TEXTURE` constant in `LeechRenderer`. Delete the Blood Bullet outright: entity type, entity
  class, model, renderer, layer registration, registrar line, the empty `textures/entity/blood_bullet` folder. Move the
  removed files to `removed_assets/`. Keep the Will Anchor and its renderer. Point its `getTextureLocation` at
  `textures/entity/blank.png` (exists).
- Files: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/mob/animal/LeechRenderer.java`,
  `src/main/java/com/vincenthuto/hemomancy/client/render/entity/projectile/BloodBulletRenderer.java`,
  `src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/BloodBulletModel.java`,
  `src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java`,
  `src/main/java/com/vincenthuto/hemomancy/client/registry/EntityRendererRegistrar.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java`,
  `src/main/java/com/vincenthuto/hemomancy/client/render/entity/mob/will/WillAnchorRenderer.java`
- Tests: remove `minecraft:entity_type hemomancy:blood_bullet` from `src/gameTest/resources/frozen/registry-ids.txt`.
- Docs: none found
- Depends on: nothing

### T6. Blood profiles (size M)

- Rulings: CR-23, CR-24
- Change: one profile file per creature below. Proposed tempers, to be confirmed by NQ-06-11 and NQ-06-12.
  - Cold animals: goat Congeatio with `cold_native` (replacing Ductilis), Ice Fish Congeatio, a new vanilla wolf file
    Animus and Congeatio. Polar bear, stray, snow golem, cod, salmon, squid already carry Congeatio.
  - No file today (18 creatures): Leech Animus. Fargone Animus, fungal. Thirster Mortem, fungal. Blood-Drunk Puppeteer
    Ductilis. Barbed Urchin Ferric. Peacock Spider Lux. Vampire Bat Tenebris. Hemolymphopoda Congeatio. Lantern Tick
    Lux. Myelin Borer Ductilis. Chthonian Ferric. Choir Keeper Tenebris. Bloodlicker Animus. Tooth Pecks Mortem.
    Enthralled Doll, Morphling Polyp, Antibody and Verdigris Moth get an explicit file with no temper.
  - Empty today (CR-24): Abhorrent Thought Ductilis and Tenebris. Lump of Thought Ductilis. Eruptus Mortem. Hunter
    Fungus Animus. Fungling Animus. Chthonian Queen Ferric and Animus. Mnemonic Whale Ductilis. Bog Revenant Mortem.
    Mortarbound Ferric. Facsimile Wolf Animus. All keep `requires_living_syringe` as it is.
  - The other 11 unfiled types are not creatures and stay without a file: Blood Cloud, Blood Thrall, Ferric Rampart,
    Intruding Memory, Iron Pillar, Iron Spike, Phantasmal Echo, Seraphae Fragment, Pale Intercession, Veinwing Vulture,
    Will.
- Files: `src/main/resources/data/hemomancy/blood_profiles/`, `src/main/resources/data/minecraft/blood_profiles/`
- Tests: `BloodProfileMigrationTest.preservesEveryShippedMembership` pins the goat as Ductilis and the Fungling as empty
  in `src/test/resources/blood_profiles/legacy_memberships.json`. Edit those two entries. `IceFishResourcesTest`
  asserts the Ice Fish has no temper. `restricted_entities.json` changes only if a syringe flag changes.
  `BloodProfileGameTests` for one new draw.
- Docs: `wiki/Blood-Profiles.md`. `docs/HEMOMANCY_REFERENCE.md` line 853 says 153 profiles. There are 185 files today
  (106 `hemomancy`, 79 `minecraft`).
- Depends on: NQ-06-11, NQ-06-12

### T7. Small drops (size S)

- Rulings: CR-25
- Change (proposed, NQ-06-13): Pyrosome 0 to 1 Chromatophores. Hagfish 0 to 2 slime balls. Blood Lantern Jelly 0 to 1
  Enthralling Filament. Bloodlicker 0 to 1 Sanguine Formation in a new `bloodlicker.json`. Antibody and Velorum
  Devotee stay empty.
- Files: `src/main/resources/data/hemomancy/loot_table/entities/pyrosome.json`, `hagfish.json`,
  `blood_lantern_jelly.json`, new `bloodlicker.json`
- Tests: `MobDropConsolidationTest` and `EntityLootTableCoverageResourceTest` (read both before editing, I did not read
  the second in full).
- Docs: `docs/HEMOMANCY_REFERENCE.md` 26.6. `docs/PELAGIC_ECOLOGY.md`.
- Depends on: NQ-06-13

### T8. Lantern Tick drain (size S)

- Rulings: CR-4
- Change (proposed, NQ-06-4): `LanternTickRules.BLOOD_DRAIN_PER_TICK_BITE` 8 to 25. Interval stays 160 ticks. That is
  about 3 mL a second, so a tick left on outruns plain regeneration. Ten bites equal one Desiccant sting. The Desiccant
  is untouched.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/effect/LanternTickRules.java`
- Tests: `src/test/java/com/vincenthuto/hemomancy/common/effect/LanternTickRulesTest.java`
- Docs: `docs/HEMOMANCY_REFERENCE.md` 26.1
- Depends on: NQ-06-4

### T9. Own numbers for the template mobs (size S)

- Rulings: CR-5
- Change (proposed, NQ-06-5). HP, armour, damage, speed:
  - Chitinite: 10, 4, 1, 0.25.
  - Fervent Chitinite: 18, 8, 3, 0.28. Needs its own `setAttributes` and its own line in `EntityInit`.
  - Fungling: 10, 0, 2, 0.32. The grown brood stays 30 HP and 8 bite (`MycophantBroodRules`). The wild Gardens value
    comes from T10.
  - Enthralled Doll: 5, 0, 2, 0.36.
  - Barbed Urchin: 10, 3, 1 (the puff still adds its state), 0.3. Read its contact damage from the attribute.
- Files: `AbstractChitiniteEntity.java`, `FerventChitiniteEntity.java` (under `.../common/entity/mob/arthropod/`),
  `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/FunglingEntity.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/EnthralledDollEntity.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/BarbedUrchinEntity.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java`
- Tests: ecology GameTests. The Mycophant fight tests if they count brood health (not checked).
- Docs: `docs/HEMOMANCY_REFERENCE.md` 26.1, 26.2
- Depends on: NQ-06-5

### T10. Gardens dimension roster (size M)

- Rulings: CR-2
- Change (proposed, NQ-06-2 and NQ-06-3): one rules table applied when a mob spawns naturally in the Gardens dimension,
  so the Overworld biome, the savanna mounds and the Mycophant's brood keep today's numbers. HP, armour, damage:
  Fungling 16, 0, 4. Fargone 36, 0, 7. Thirster 44, 0, 8. Lump of Thought 52, 0, 9. Hunter Fungus 56, 4, 10. Eruptus
  64, 4, 11. Abhorrent Thought 90, 8, 13. Chthonian 30, 6, 6. At those values a Hematic Iron Sword needs 2 to 10 hits
  and an Abhorrent Thought kills a fully armoured player in about 6.
- Files: the seven entity classes named in the CR-2 row, plus a new rules class beside them. Whether the Eruptus lunge
  reads the attribute was not checked.
- Tests: a JVM rules test for the table. One GameTest that a Gardens spawn carries the raised health and an Overworld
  spawn does not.
- Docs: `docs/HEMOMANCY_REFERENCE.md` 26.1. `docs/ENDGAME_PATHS.md` if it quotes Gardens threat.
- Depends on: T1, T9, NQ-06-2, NQ-06-3

### T11. Sent Wills scale harder (size S)

- Rulings: CR-14
- Change (proposed, NQ-06-6): in `WillCombatRules.sentStats`, health becomes player max health x 3 + 30 per Degree over
  6, damage becomes 8 + 2 per Degree over 6. For a 20 HP player that is 60, 90, 120 HP and 8, 10, 12 damage at D6, D7,
  D8, against 28, 32, 40 and 6, 7.25, 8.5 today. Speed unchanged. Broken Wills unchanged. A D8 sent Will then takes 7
  Hematic Iron Sword hits to reach its 40% absorb line, against 3 today.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillCombatRules.java`
- Tests: `WillCombatRulesTest.sentStatsScaleFromTargetSnapshot` pins the formula text. Update it.
- Docs: `docs/HEMOMANCY_REFERENCE.md` Will section. `docs/LORE_REFERENCE.md` line 527 already says Sent Wills assess
  practitioners, no change.
- Depends on: NQ-06-6. The Will school kits name manipulations rebuilt on 2026-11-12 (Hemorrhage, Prismatic Reproof,
  Glacial Grasp, Hematic Flare, Blood Eclipse, Gloam Laceration). The manipulations chunk owns whether those entity
  casts still read well. Not checked here.

### T12. Phlegethosoma summon (size L)

- Rulings: CR-7
- Change:
  - Remove the natural spawn: delete `add_phlegethosoma.json` and the `canSpawn` placement registration in
    `EntityInit`. Keep `PhlegethosomaSpawnRules.validPool` (the summon and a GameTest use it).
  - Add a link to the Projection chain in `BloodProjectionItem.performProjection`, in the block branch ahead of
    `ChumPools.tryProject`: a struck fluid in `PhlegethonticTags.ICHOR` goes to a new summon service in
    `common/entity/boss/phlegethosoma/`. It checks the Nether, the basin biome, a valid 7x7 pool, not Peaceful, and no
    living Phlegethosoma within 64 blocks. It pays through `BloodAccounting.pay` with its own named `Source`.
  - Count the blood per pool up to 500 mL. Proposed (NQ-06-7): the count fades like chum (`ChumRules.fade`, half in 45
    seconds), so the pour is one sustained act but a short break does not reset it. At the chum rate of 2 mL a tick the
    pour takes about 13 seconds.
  - At 500 mL spawn one Phlegethosoma at the pour point, home set there, persistence required, targeting the summoner,
    with its existing 60-tick emergence scene.
  - Proposed (NQ-06-8): if it loses every target for 60 seconds it dives and is removed with no loot. No cooldown
    beyond the one-at-a-time check.
  - Visuals from the mod's own toolkit (the chum bloom and the fluke's surface ripples). No vanilla particles.
  - Player text as `en_us.json` keys.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodProjectionItem.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/phlegethosoma/PhlegethosomaSpawnRules.java`,
  `PhlegethosomaEntity.java` (same folder), `src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java`,
  `src/main/resources/data/hemomancy/neoforge/biome_modifier/add_phlegethosoma.json`,
  `src/main/resources/assets/hemomancy/lang/en_us.json`
- Tests: `ShapingGuardrailTest.eachVerbChainAsksTheDispatcherAtItsPinnedPlace` pins the chain order, so add the new
  link to its list. New JVM rules test for the count and fade. New GameTests in `PhlegethosomaGameTests`: 500 mL
  summons one, a second pour is refused while one lives, a narrow pool is refused, a broken pour fades. Remove
  `neoforge:biome_modifier hemomancy:add_phlegethosoma` from `src/gameTest/resources/frozen/registry-ids.txt`.
  `BloodAccountingGuardrailTest` and `VanillaParticleRatchetTest` must stay green. No payload is needed, so no protocol
  bump.
- Docs: `docs/HEMOMANCY_REFERENCE.md` line 6744 (natural spawning). `wiki/Phlegethontic-Nether.md` line 53 says it
  spawns naturally and needs no blood magic. `docs/BOSSES.md`. `docs/TESTING.md` Phlegethosoma section. A teaching
  line once NQ-06-10 is answered.
- Depends on: NQ-06-7, NQ-06-8, NQ-06-10

### T13. Placeholder mob art, then effect icons (size L)

- Rulings: VX-16
- Change: author the Hemojelly and the Myelin Borer through new `tools/model_export` generators (model, texture at 16
  pixels a block, animation), each ending with a review client launch. Then give the 28 shared mob-effect icons their
  own art. The blocks, sabatons, Forge and Loom wait for a later pass.
- Files: `src/main/resources/assets/hemomancy/textures/entity/hemojelly/model_hemojelly.png`,
  `src/main/resources/assets/hemomancy/textures/entity/myelin_borer/model_myelin_borer.png`,
  `src/main/resources/assets/hemomancy/textures/mob_effect/`, new generators under `tools/model_export/`
- Tests: the packaged asset checks in `alphaCheck`. A review client row in `gradle/review-clients.gradle` per mob.
- Docs: mark C-11 in `docs/audits/2026-11-02-cleanup/README.md`
- Depends on: nothing

## Conflicts with recent work

None.

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-06-1 | CR-1 | The recommendation names only pack sizes. Does the fix also cover the other C-5 defects: the biome missing from `#minecraft:is_overworld` and having no vanilla ores? | A: yes, both. B: spawns only. | A. The biome replaces jungles and swamps, so a player mining there should find ores, and the tag is what lets Overworld-wide spawns such as the Polyp reach it. |
| NQ-06-2 | CR-2 | The Gardens mobs also spawn elsewhere (Fargone and Abhorrent Thought in the Overworld biome a D2 assignment visits, Chthonians in savanna mounds, Funglings in the Mycophant's brood). Where do the D7 to D8 numbers apply? | A: only on natural spawns in the Gardens dimension. B: everywhere, and take the Abhorrent Thought out of the Overworld biome. C: everywhere, no other change. | A. It keeps the D2 visit and the Mycophant fight as tuned. |
| NQ-06-3 | CR-2 | No numbers were given for the Gardens roster. Is the T10 table right (Fungling 16/4 up to Abhorrent Thought 90 HP, 8 armour, 13 damage)? | A: the T10 table. B: your own numbers. | A as a first value, tuned in play. |
| NQ-06-4 | CR-4 | How much should the Lantern Tick drain? Today 8 mL per 160 ticks equals plain regeneration. | A: 25 mL per 160 ticks. B: 8 mL per 40 ticks. C: leave at 8. | A. One number changes and the bite stays a slow leak next to the Desiccant's burst. |
| NQ-06-5 | CR-5 | No numbers were given for the template mobs. Is the T9 table right? | A: the T9 table. B: your own numbers. | A as a first value. |
| NQ-06-6 | CR-14 | How hard should Wills scale, and which Wills? Sent Wills exist from D6 only. Broken Wills are capped at tier 2 (30 HP, 5 damage). | A: sent Wills only, 60/90/120 HP and 8/10/12 damage at D6/D7/D8. B: A, and broken Wills follow the ambush tier up to 54 HP and 7 damage. C: your own curve. | A. Your answer and the lore both name the sent Will as the hivemind's test. |
| NQ-06-7 | CR-7 | Must the 500 mL arrive in one go? | A: counted per pool, fading like chum, so one sustained pour of about 13 seconds. B: one unbroken hold, any pause resets. C: no fade, it adds up over any time. | A. It reuses the chum numbers and forgives a flinch. |
| NQ-06-8 | CR-7 | What happens when the summoned Phlegethosoma is left alone, and is there a cooldown? | A: it dives and is gone after 60 seconds with no target, no loot, no cooldown beyond one at a time within 64 blocks. B: it stays until killed. C: A plus a per-player cooldown. | A. A boss left in a pool forever blocks the next summon. |
| NQ-06-9 | CR-10 | Vampire Bat biomes. Does the Phlegethontic Basin count, and may the list be explicit? An explicit list drops other mods' Nether biomes. | A: explicit list of nether wastes, crimson forest, basalt deltas and the basin. B: the same without the basin. C: every Nether biome minus the two, through a NeoForge exclusion set (not verified by reading, nothing in the repo uses one). | A. |
| NQ-06-10 | CR-7 | Who may summon, and how does the player learn it? Today the wiki says the fight needs no blood magic. A summon needs awake blood and Projection. No dialogue or book names the creature. | A: any player with awake blood, taught by one Alchemist line on the D5 basin commission and a Liber page. B: gated at D5. C: no teaching, a discovery. | A. |
| NQ-06-11 | CR-23 | Which cold animals carry Congeatio beyond the polar bear, and does the goat keep Ductilis? | A: goat Congeatio only, Ice Fish Congeatio, wolf Animus and Congeatio. B: goat carries both. C: your own list. | A. A goat then gives a clean Congeatio draw to an ordinary vial. |
| NQ-06-12 | CR-23, CR-24 | No tempers were named for the 18 unfiled creatures and the 12 empty ones. Is the T6 list right? Should the Enthralled Doll, Morphling Polyp, Antibody and Verdigris Moth stay without a temper? | A: the T6 list. B: your own edits to it. | A as a first value. The Peacock Spider and Lantern Tick as Lux give two early wild Lux sources. |
| NQ-06-13 | CR-25 | Which small drops? | A: Pyrosome 0 to 1 Chromatophores, Hagfish 0 to 2 slime balls, Blood Lantern Jelly 0 to 1 Enthralling Filament, Bloodlicker 0 to 1 Sanguine Formation. B: your own. | A. All four items exist. Note a Bloodlicker drop can be farmed from rite waves. |
| NQ-06-14 | CR-10 | "Morphling polyps should spawn rarely in all biomes": every Overworld biome as built, or the Nether, End and Gardens too? | A: Overworld only, as built (weight 3). B: every dimension. | A. The polyp picks its strain from Overworld biome kinds and has none for the Nether or End. |
| NQ-06-15 | CR-10 | "Desiccants spawn ambiently": is today's rule what you mean (Monster category, any light, on sand, sandstone or terracotta in desert, badlands and savanna)? | A: yes, as built. B: move it to the ambient category so it does not use the monster cap. | A. Savanna grass never passes the surface check, so in practice it is desert and badlands already. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| CR-1 | Generator learns the Overworld biome, packs 1 to 4, dead list deleted. | Build | T1 |
| CR-2 | Gardens roster numbers and where they apply. | Owner | NQ-06-2, NQ-06-3, then T10 |
| CR-3 | Night-only Puppeteers. Then judge in play whether weight 24 still makes them findable. | Build | T2 |
| CR-4 | Tick drain value. | Owner | NQ-06-4, then T8 |
| CR-5 | Template mob numbers. | Owner | NQ-06-5, then T9 |
| CR-7 | Summon rules: arrival, lifetime, gate and teaching. | Owner | NQ-06-7, NQ-06-8, NQ-06-10, then T12 |
| CR-8 | 26 HP and the test floor. | Build | T3 |
| CR-9 | Weight 10. | Build | T2 |
| CR-10 | Bat biome list. Polyp and Desiccant readings. | Owner | NQ-06-9, NQ-06-14, NQ-06-15, then T2 |
| CR-14 | Will curve. | Owner | NQ-06-6, then T11 |
| CR-23 | Cold animal list and the temper table. | Owner | NQ-06-11, NQ-06-12, then T6 |
| CR-24 | Tempers for the empty profiles. | Owner | NQ-06-12, then T6 |
| CR-25 | Drop items. | Owner | NQ-06-13, then T7 |
| FX-4 | Attributes for four classes, four NPCs lose the unused one. | Build | T4 |
| VX-15 | Blood Bullet deleted, Will Anchor kept, Leech constant removed. | Build | T5 |
| VX-16 | Hemojelly and Myelin Borer art, then 28 effect icons. Blocks, sabatons, Forge and Loom after. | Build | T13 |
| RV-27 | Walk the wayside sites, the Harbinger sites, the Brined Votary and the heart node. | Owner | nothing |
| CR-6, CR-11, CR-12, CR-13 | Record the four keeps in `docs/OPEN_QUESTIONS.md`. | Later | the roll-up pass |
