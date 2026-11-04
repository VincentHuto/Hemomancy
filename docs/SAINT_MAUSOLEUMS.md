# Saint Mausoleums

Status (2026-11-01): pass 1 of the legacy-dungeon rework is committed (`2ff58cc50` and the fix commits through `62f5b38da`); the four complexes generate, every Chapel kind runs, the ring door and Saint fights work end to end. Most of it has been seen only in stills and tests, not played. The per-Saint combat overhauls and the Canon Memory gift rework are not started.

## What it is

Four Saints rest in four buried complexes, one each: Hemorath (The First Saint), Seraphae (The Chain Saint), Putriciel (The Befouled Saint) and Velorum (Saint of the Endless Hour). Each is a jigsaw structure `hemomancy:mausoleum_<saint>` in the set `saint_mausoleums` (random spread 40/12), biomes per Saint (Hemorath badlands and savanna; Seraphae meadow, cherry grove, flower forest, sunflower plains; Putriciel swamp, mangrove, dark forest; Velorum snowy biomes). The old generic `hemomancy:mausoleum` stays in the data, disabled.

**Placement.** The entrance pavilion sits flush on the highest ground within 14 blocks of its centre and refuses a site whose ground there varies by more than 4 blocks (`MausoleumEntranceRules`), so it is never cut into a hillside. `max_distance_from_center` stays 116 so the complex folds back under itself. `MausoleumSealing` keeps ground water out of every chunk as it generates.

**Chain and layout.** Entrance, stairs, antechamber (barrels: `chests/mausoleum`), ossuary descent, crypt gallery (six bays of loculi, three barrels, a reliquary behind a hematic iron door with the `chests/mausoleum_treasure` barrel), a two-piece spiral down an open well, the threshold hall (ward line), the galleried nave (31 by 21, 16 tall, the ring door at its far end), three Chapel wings (slot 0 at the nave floor, 1 from its gallery, 2 down into a crypt) and the Saint's chamber as the lowest room, under the entrance. Walked backwards: the Saint's life, rest above and beginning below. The nave is 34 below the entrance floor, the chamber 40; 19 pieces, lowest box -39.

**Degrees.** The outer halls (entrance through the gallery and its loot) are open from about D3 to D4; there is no trial out there any more. The ward, the Chapels, the ring door and the Saint are D8.

**Consecrated ward.** Inside a complex survival players cannot break or place, and explosions and pistons move nothing; every refusal strikes the ward (`WardStrikePacket`: ripple, flash, low bell, glow motes). Past the threshold slab everything is warded: a hidden `ward_anchor` block entity in the threshold wall (`WardAnchors`), a D8 gate in `MausoleumWardRules` that sets anyone lower down a block before the line ("The ward does not know you"), a breathing glyph layer in the Saint's ward colour within 12 blocks (`MausoleumWardRenderer`, `mausoleum_ward` shader). Hallowed ground: an Apotheos loses 4 spore load a second, then takes 1 `hallowed_burn` a second (`HallowedBurnLayer` crawls the glyphs over them); Silentium and lower Harbingers are untouched. The ground is consecrated, not holy: the Saint's residue rejecting the hive.

**Chapels.** Each wing is approach, trial hall and focus room; the generator writes the trial's marks into the `chapel_focus` (`ChapelRules`, one D8 challenger at a time; leaving, dying or a wrong answer resets with a 100-tick cooldown). Four kinds:

| Kind | Trial | Set pieces |
| --- | --- | --- |
| Beholding | Four witnesses flash in order; walk back and stand at each in that order within 400 ticks | `witness_stone`, Velorum's `witness_devotee` |
| Litany | A stone by the door shows three words (Abocipher unless literate); offer those three of six candidates at the focus, in order, within a minute | `litany_stone`, six `offering_stand`s |
| Downfall | Step on the gate; three waves of three Saint mobs (constructs, vexes, bog revenants, strays) rise from their seals; reach the focus | `downfall_seal` seals |
| Repose | Stay still in the inlaid ring for 600 ticks while corner pylons pulse (2 magic damage every 60 ticks, never lethal; Silentium stillness counts double) | `pulse_pylon` |

Slots: Hemorath Beholding/Litany/Downfall; Seraphae Beholding/Downfall/Repose; Putriciel Litany/Downfall/Repose; Velorum Beholding/Litany/Repose. A completed Chapel stays lit.

**Ring door.** A 5 by 5 controller multiblock (`ring_door`, 24 fillers, no item): venous slab in a hematic iron frame, three rings with lacquer nodes. One node lights per completed Chapel (found within 64 blocks, plus or minus 16 high) and its ring turns to the seam. It opens to held Blood Projection on either path: `RingDoorRules.OPEN_TICKS` 200 fed ticks paying 5 each (1000 in all, `ring_door` source), a 4-tick grace then a bleed-off at half speed, no refund. A Silentium with all three nodes opens it properly (`PROPER`): blood spreads to monolith stone and the slab sinks. An Apotheos forces it at any time (`FORCED`): hyphae crack it and it bursts, and the tomb records the skipped Chapel kinds. Right-click with an empty hand only says the rings answer blood.

**Sarcophagus and chamber.** The tomb is 2 by 4 on 23 fillers, centred on its main block, cut at z 32 into foot and head sections (`SectionedBakedModel`, shared with the door), retextured per Saint by `SarcophagusPalettes` and `RetexturedBakedModel`; the item is the tomb at half scale. The chamber is 37 by 27 by 17: processional, pillar ranks, a clear 15 by 15 boss floor, a three-step dais and an apse; `CHAMBER_SOCKETS` are filled by each theme's `chamber_sockets` hook. Consecration is three 100-blood offerings in the bowl; a vial draw with a dominant tendency matching the Saint's pair gives one peaceful Consecrated Syringe (Centrifuge makes Hallowed Residuum), otherwise the Saint wakes. Foul Paste or a Saint relic wakes it regardless.

**Set pieces.** Eleven generator-authored blocks: `frozen_devotee`, `witness_stone`, `litany_stone`, `offering_stand`, `pulse_pylon`, `downfall_seal`, `containment_anchor_block`, `rot_pyre`, `sealed_niche`, `opened_niche`, `reliquary_sconce`, plus Hemorath's `blood_gutter` and `venous_basin`. No template carries an entity. Per-Saint looks are `style` or `flame` properties on the shared block, not new blocks.

**Themes.** Every theme grades by depth, walked backwards from the chamber, and keeps the threshold corridor and the boss floor pinned open.

| Saint | Family and motif | Own halls | Ambience |
| --- | --- | --- | --- |
| Hemorath | Venous stone, hematic iron plating, bone; basins and channels, gutters wetter by depth; the Order's own house | Downfall (the Draining Pit), descending to the chamber | Heartbeat quickening with depth, drips |
| Seraphae | Quartz brick, calcite, diorite grilles; chains and healed overgrowth (GROWTH 0 to 4), swollen pillars; a containment, not a tomb | Repose (the Still Ward) | Choir hum, anchor ticks |
| Putriciel | Mud brick, mossy stone brick, packed ash, charred wood, bone keystones only; alternating burned and overgrown bands (BAND 8 to 3), pyres and spore shelves the only light | Litany (the Burned Library), Purge Corridors, the Ash Garden | Crackle, creak, far flares |
| Velorum | Rimed deepslate, packed and blue ice, dark lacquer; the frozen congregation everywhere (FROST 1 to 4), rime lanterns, icicles | Litany (the Rule), Repose (the Cell Row) | Ice creaks, the player's own steps muffled |

New block families: §24.12 (Putriciel) and §24.13 (Velorum) of the reference.

**Awakening and the fights.** The rising Saint reads its tomb's door record (`SaintEnrageRules`). Forced: +30% attack, +15% speed, and each skipped Chapel adds its deed (Downfall +50% health, Repose +20% speed, Beholding Resistance, Litany Regeneration); residuum drops as a fragment (three craft one). Proper fights as designed. All four extend `SaintTrialEntity`, which carries the shared rise, the tomb return and one reward roll (`entities/<saint>`: residuum and 1 to 2 Sanguine Quintessence). No Saint can be killed: Hemorath overloads or wears down and is exsanguinated (he reforms), Seraphae is contained at full integrity, Putriciel rests after the fifth absolution, Velorum reverts to dormancy. Seraphae wakes the chamber's four nearest whole anchor blocks and restores them (`SeraphaeAnchorRules`); Putriciel's absolution window puts the pyres out and blooms the shelves (`PutricielChamberLightingRules`). Each Saint has a first power pass and two secondary moves with committed warnings (`docs/validation/SAINT_POWERS_2026_10_27.md`, `SAINT_SECONDARY_ATTACKS_2026_10_27.md`).

**Residuum and Recollections.** A Recollection cyst (one Saint each, rarity 1/24 chunks in the Archive biomes) gives its Recollection to an Apotheos or a possessing Silentium by touch. Each Canon Memory (Crimson Tithe, Unclosing Eye, Bloom of Rot, Endless Hour) is woven at the Loom from the Saint's Hallowed Residuum and its Recollection with the tendency pair and 100 blood (§25.7.2).

**Models and animation.** Each Saint's rig, atlases and `.bbmodel` come from `tools/model_export/<saint>.py`; the `<saint>_anim.java` tracks pose the existing phases over `BossReplayClock`. The 2026-10-26 model pass: iron erupting through Hemorath's bones, Seraphae's wings as branching nerves, Putriciel's growing colonies in olive and bone with fire only in absolution, Velorum's plain hood and knotted cord.

## Where it lives

| Area | Path |
| --- | --- |
| Structure, placement, sealing | `common/worldgen/structure/MausoleumStructure.java`, `MausoleumEntranceRules.java`, `MausoleumSealing.java` |
| Ward, Chapels, ring door rules | `common/block/harbinger/puzzle/MausoleumWardRules.java`, `ChapelRules.java`, `RingDoorRules.java`; `common/event/MausoleumEvents.java` |
| Block entities | `common/tile/harbinger/puzzle/ChapelFocusBlockEntity.java`, `RingDoorBlockEntity.java`, `WardAnchors.java` |
| Saints | `common/entity/boss/saint/SaintTrialEntity.java`, `SaintEnrageRules.java`, `hemorath/`, `seraphae/` (`SeraphaeAnchorRules`), `putriciel/` (`PutricielChamberLightingRules`), `velorum/` |
| Recollections | `common/block/harbinger/plant/RecollectionCystBlock.java`, `RecollectionRules.java`, `common/tile/harbinger/crafting/engram/EngramRecollections.java` |
| Client | `client/render/world/MausoleumWardRenderer.java`, `client/render/layer/player/HallowedBurnLayer.java`, `client/render/tile/harbinger/puzzle/SarcophagusPalettes.java`, `client/sound/MausoleumAmbience.java` and `MausoleumAmbienceRules.java` |
| Structure generator | `tools/worldgen/mausoleum_complexes.py` + `tools/worldgen/mausoleum/{kit,checks,entrance,pieces,trials,build}.py`, `themes/<saint>.py` (version-controlled; re-run, never hand-edit an NBT) |
| Model generators | `tools/model_export/`: `saint_sarcophagus.py`, `ring_door.py`, the eleven set pieces, `blood_gutter.py`, `venous_basin.py`, `<saint>.py` |
| Review clients | `gradle/review-clients.gradle`: `mausoleumReviewClient` (`-Pmausoleum=<saint>`, `-PmausoleumNatural=true`, `-PmausoleumSeed`), `sarcophagusReviewClient`, `ringDoorReviewClient`; stops in `src/gameTest/resources/mausoleum/review-stops.json`, shots in `build/mausoleum-review/<saint>/` |
| Tests | `src/gameTest/.../gametest/MausoleumAssemblyGameTests` (four seeds by four Saints), `MausoleumWardGameTests`, `MausoleumSealingGameTests`, `SaintEncounterGameTests`, `SaintPowerGameTests`; dev-only `/hemo test mausoleum <saint> [seed]` (`MausoleumTestCommand`, gameTest source set) |
| Reference | `HEMOMANCY_REFERENCE.md` §5.6g, §5.6h, §5.8, §24.12, §24.13, §25.7.2, §25.8; `LORE_REFERENCE.md` §10; `wiki/Saints-and-Encounters.md` |

## Rulings that bind

User rulings, 2026-10-26 to 2026-10-29:

- Tendencies: Hemorath ANIMUS + FERRIC, Seraphae LUX + DUCTILIS, Putriciel MORTEM + FLAMMEUS, Velorum CONGEATIO + TENEBRIS. Every Canon Memory takes its Saint's pair.
- No Saint can be killed. Seraphae is contained; Hemorath is exsanguinated and reforms; Putriciel is purified and rests until the rot reclaims them; Velorum reverts to dormancy. Every fight restores the sarcophagus dormant and rolls one reward.
- Titles and pronouns: Hemorath The First Saint (phase card keeps "The Vessel Empties"), he; Seraphae The Chain Saint, she; Putriciel The Befouled Saint, he in life and they now; Velorum Saint of the Endless Hour, he, with no martyr framing. "Hollow Vessel" is retired from player text.
- Hemorath was an early, powerful Hemomancer and never a Harbinger or Archon; Erythravane was the first Vicar and the first Archon. The other three never belonged to the Order; each was sealed by their own people and the Order inherited the tombs.
- Doctrine tags are gone and Saints are never random; an unassigned sarcophagus is inert until a relic names it.
- Mausoleums are consecrated, not holy. Saint fights and Chapels are D8; the outer trial halls open D3 to D4.
- The armor-stand congregation is gone; figures are blocks (`frozen_devotee`).
- Putriciel's fire is the Flammeus look (crimson body, pink-white heat, black soot tips), never green or yellow in any fire texel (`PutricielFireResourceTest`); only the overgrown bands' spore glow is green. No bone-brick trims on Putriciel: trims, bases and floors are mossy stone brick, bone only on keystones, capitals and seals.
- Saint gifts are full reworks of the four Canon Memories; not done, the user's design call.
- Each Saint gets its own full combat and visual overhaul later: Hemorath proximity blood drain, a leaking absorbed meter and iron-spike bone patches; Seraphae a mechanics cleanup, wings as nervous projections, white vessel light with gold nerve flashes; Putriciel fire only in a cleansing interval and a palette with no red; Velorum a stillness meter with openings and a frozen congregation the player can wake. The 2026-10-27 power passes are a first step, not those overhauls.

Working rules carried from pass 1 (`archive/tracks/mausoleums/12-next-pass.md` §2): lang keys only, `HemoParticleData`, 16 px texel density, generator-authored models and templates, a review client before "done"; trial set pieces unbreakable and itemless; `blood_basin` stays the puzzle block and stands only in Hemorath's four chamber alcoves; new planks families stay out of planks and wooden tags; theme growth touches wall and vault roles only.

## Calls made without the user

Made during the overnight build and the rework; none have been reviewed in play (`superpowers/OVERNIGHT_REPORT_2026-10-28.md` §5 to §9, `archive/tracks/mausoleums/12-next-pass.md` §2).

- The Chapel numbers: four Beholding flashes 25 ticks apart and a 400-tick answer; Litany's six candidates (bone, rotten flesh, spider eye, ink sac, glowstone dust, redstone) and a minute to answer; Downfall's three waves of three and the mob per Saint; Repose's 600 ticks, 2-damage pulse every 60 ticks and 3.5-block ring; the 100-tick fail cooldown.
- The ring door's cost and timing (1000 blood over 200 fed ticks, half-speed bleed-off, no refund), its 64-block Chapel search, and that a plain right-click does nothing. The earlier 3 x 300 one-click rule and the 40-tick fall were replaced by the held-projection opening in `62f5b38da`.
- Enrage values (+30% attack, +15% speed, per-Chapel deeds) and the fragment reward (three per residuum). Most Saint damage is fixed magic, so the attack bonus mostly helps melee, and Seraphae and Putriciel ignore the health and Regeneration deeds.
- Hallowed ground: 4 spore load a second then 1 `hallowed_burn` a second; only destroying explosions refused; riders dismounted before push-back; refusal sound a vanilla bell at low pitch.
- Recollections: cyst rarity 1/24 chunks in the three Archive biomes, taken by touch, and the Canon recipes needing the Recollection as a second catalyst.
- Placement: `max_distance_from_center` 116, footprint radius 80, the 14-block and 4-block entrance site check (some steep sites now refuse), the threshold 33 down.
- Seraphae's biomes swap plains for flower forest. Hematic iron plating, the bone brick family, charred and rimed families and their recipes.
- Ambience picks: Hemorath's heartbeat quickening toward the pit (70 to 45 ticks), Seraphae's choir as the beacon hum at pitch 0.6, Putriciel's `mud.step` creak, Velorum's glass and amethyst creak and the muffled-step stand-in for a low-pass filter.
- The tomb palettes for all four Saints, the pylon style simplification of 2026-10-30, Velorum's polished-deepslate and Putriciel's packed-mud door tracks.
- `/hemo test mausoleum` is dev-only (gameTest source set), not a `HemoCommand` branch.

## Open questions

For the user (`archive/tracks/mausoleums/12-next-pass.md` §3.8 and §3.5; `superpowers/OVERNIGHT_REPORT_2026-10-28.md`):

- Hematic iron plating in `ductilis_conductors` / `laboring_arms_resistant`?
- Charred planks in any planks or wooden tag? (Lacquer stays out.)
- Keep Hemorath's heartbeat quickening toward the pit, or slow it?
- The sound picks (choir, creak, ice creak) once heard.
- Should `/hemo test mausoleum` get a production command, or stay dev-only?
- Retire the disabled legacy `mausoleum` structure (its JSON, pools and the ten NBTs in `structure/mausoleum/`)?
- The Canon Memory gift rework (the full Memory to gift design).
- Chapel memory content owed: Hemorath's students sealing the vessel, Seraphae's "four touches, four growths".
- The sarcophagus has no D8 gate of its own: a relic, Foul Paste or a misaligned vial wakes a Saint at any degree; the ward and door in front of it are the gate. Keep?
- Velorum keeps internal `MARTYR` names in code (`DATA_MARTYRDOM`); the Saints are still called post-release WIP in the reference, the lore doc and the wiki while the complexes generate.
- Enrage dodges: a non-Apotheos can wake the tomb for an Apotheos; peaceful syringe draws give whole residuum after each return. Recollections are tradeable, the Loom has no degree gate, and `mausoleum_treasure` still drops `memory_crimson_tithe` directly.

## Proof owed and known gaps

Seen only in stills, by tests, or not at all (`12-next-pass.md` §3.2 to §3.4). Suggested: one played session per Saint before new art.

- Never watched in motion: the Apotheos burn layer, `SCENE_SAINT_RISE` from the processional, every trial completed solo per Saint, set pieces mid-trial, Hemorath's runoff through the door, Seraphae's anchors waking and restoring, Putriciel's purge ring and pyre/shelf switch, Velorum's frost pulse. All ambience is unheard.
- Natural worldgen: one natural run (Velorum, seed 20261028), none since the entrance check widened to 14 blocks. Run natural mode for all four and note how often steep sites refuse.
- Lighting: the ward overlay dims every inner stop; Hemorath's litany hall is very dark; the tomb reads small from the chamber door at 30 blocks (consider widening the portal); Seraphae's grilles go black under ambient occlusion; Velorum's still circle reads squared in the 7-wide Cell Row.
- Art: stubby pavilion newels; Velorum's roof only partly rimed; tomb coals and bowl blood red for every Saint (Velorum needs his own); Hemorath's carved witness variant and ration trays not made; the door's hand stream starts from a fixed offset, not the rendered hand; prostrate devotees' upper cell has no outline. The raised descent vault (`297a6177b`) still needs a look at `descent_top`.
- Not built: Velorum's congregation thaw (needs a devotee mob) and his safe cells in the Cell Row; Hemorath's swelling drawing on the four wells; the Beholding steles carved with his students.
- Chapel robustness (deferred minors): reload mid-trial loses timer and cooldown; Downfall wave UUIDs against unloaded entities; Peaceful auto-completes Downfall and it can be farmed by stepping out; `hallowed_burn` lacks `bypasses_armor`; the failure cooldown is silent; the STRAY Downfall branch is unreachable (Velorum has no Downfall). Recollection cysts are fragile (0.6 strength, piston DESTROY) and can be griefed.
- Tests: Downfall and Repose are covered by rules tests only (they need a live player); `MausoleumEvents` has no automated test; the 56 `EMPTY_TEMPLATE` journey fixtures share the grid exposure that broke `centrifugeJourneySpawnsCompleteReadyStructure`. Track 10 (Putriciel) never had a final fresh reviewer pass over its Java. Flaky: `livingfalseomencannottimeoutintosuccess`, `strongerpreyneedsmultiplebitesbeforereward`.
- Uncommitted in the tree: only two generator docstrings pointing at the archived track folder.

## History

- 2026-10-10: sarcophagus redesign, generator-authored tomb and renderer (`validation/SAINT_SARCOPHAGUS_REDESIGN_2026_10_10.md`).
- 2026-10-26: Saint reconciliation (tendencies, unkillable, titles, doctrine removed, `SeraphaeEntity` under `SaintTrialEntity`) and the model pass (`superpowers/specs/2026-10-26-saint-reconciliation-design.md`, `validation/SAINT_MODEL_RECONCILIATION_2026_10_26.md`).
- 2026-10-27: first power pass and the secondary attacks (`validation/SAINT_POWERS_2026_10_27.md`, `validation/SAINT_SECONDARY_ATTACKS_2026_10_27.md`).
- 2026-10-28: overnight Sections 5 to 9 built the first complexes, Chapels, ring door, hallowed ground, awakening and Recollections (`superpowers/specs/2026-10-28-mausoleum-complexes-design.md`, `superpowers/OVERNIGHT_REPORT_2026-10-28.md`); then the eleven-track legacy-dungeon rework (`archive/tracks/mausoleums/00-README.md`, tracks `01` to `11`).
- 2026-10-29: pass-1 summary and cleanup (`archive/tracks/mausoleums/12-next-pass.md`); committed as `2ff58cc50`, then `e0e2f9fde` (GameTest fixes), `7dd420716` (Seraphae re-wakes anchors), `297a6177b` (junction lift, taller descent vault).
- 2026-10-30 to 10-31: `62f5b38da` (held-projection ring door opening, `MausoleumSealing`, pylon simplification), later commits touched docs only.
