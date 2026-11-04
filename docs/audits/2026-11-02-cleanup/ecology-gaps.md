# Ecology gaps audit (2026-11-02)

Scope: the living world. Creatures, where they spawn, what they eat, drop and fear, how biomes are populated, and whether the ecology in the docs is in the data and code. Read-only audit: no source, resource or tool file was changed. Evidence was gathered with Grep/Glob sweeps and small `python -I` scripts over the JSON tree, the structure NBT templates and the Java sources. Vanilla behaviour was checked against the NeoForge 21.1.251 sources jar in `build/moddev/artifacts/`.

## Summary

`EntityInit` registers 182 entity types: 164 directly plus 18 Brined Court role types from `courtMembers(...)`. Of these, 106 are non-MISC living types, plus the 18 Court roles and two MISC-category Court followers. 54 mod entity types spawn naturally, through 17 mod biome JSONs and 42 biome modifiers. The rest come from structures (NBT or `afterPlace`), features, encounters, summons or spawn eggs. The data hangs together. Every natural spawner has a `SpawnPlacements` entry, every placed feature is placed somewhere, and every dropped item has a downstream use.

The audit raised 30 findings:

- **5 High.** Leeches never spawn, yet their Swollen Leech gates the Morphling Incubator. Wild Lethean Poppies have no loot table. The Overworld Fungal Gardens biome has only Abhorrent Thoughts and Fargones in packs of 8 and no vanilla ores. Seven creatures, including the Gardens' Abhorrent Thought and the "boss" Chthonian Queen, still carry the 7 HP / 1 attack template stats. The Hemorrhagic Plateau and Sporecrown Thicket list the Blood-Drunk Puppeteer twice, so it dominates both.
- **9 Medium.**
- **16 Low.**

Two backlog items are cited rather than repeated: M-2 (five unregistered placeholder mobs) and M-6 (the legacy mausoleum). One open question is wrong: OPEN_QUESTIONS §1.4 says squid cannot be leashed, but vanilla 1.21.1 squid can.

## Creature table

Category `benthos` is the enum-extended `hemomancy:pelagic_benthos` (cap 12). Category `pyrosomes` is `hemomancy:pyrosomes` (cap 8). "JSON" means the biome JSON spawner. "mod" means a biome modifier. Weights are `weight/min-max`.

| Entity | Category | Spawn sources | Biomes / sites | Loot table | Verdict |
| --- | --- | --- | --- | --- | --- |
| unstained_zealot, unstained_guardian, unstained_acolyte | CREATURE | `UnstainedChurchStructure.afterPlace` (persistent) | Unstained Church: Overworld plus Nether biomes, see EC-10 | empty tables | OK; Nether-roof risk (EC-10) |
| unstained_scout | CREATURE | `ToothGeodeFeature` (persistent) | tooth geodes, underground in forests and taigas | empty table | OK |
| harbinger_hermit | CREATURE | `BloodTempleStructure.afterPlace` | Blood Temple: Overworld plus Nether biomes | none | OK; Nether-roof risk (EC-10) |
| harbinger_alchemist, _artificer, _mnemonist, _vicar | CREATURE | `HarbingerOutpostStructure.afterPlace` | Harbinger Outposts | alchemist and vicar only (fire for successors and misbegotten) | OK; inconsistent tables (EC-28) |
| harbinger_cicatrix_anchorite | CREATURE | `MasonsRespiteStructure` | Mason's Respite | none | OK |
| harbinger_voyager, harbinger_votary_wayfarer | CREATURE | `ActiveHarbingerVoyagerNpcSpawner` | Voyager vessels (reef) | none | OK |
| circus_fire_eater, _stilt_walker, _acrobat, _knife_thrower, _strongman, _beast_tamer, _threadkeeper, _understudy | CREATURE | template NBT, `PersistenceRequired:1` | pavilion plus the five teaching camps | empty tables | OK |
| circus_ringmaster | CREATURE | pavilion NBT | pavilion | none | OK |
| court_* (18 roles) | CREATURE | `CourtResidentSpawner` on resident marks; Shared Wake schools via `CourtSchools` | Brined Court Seats and outposts; pelagic open water | none | OK; known gaps cited (PT-23, no backfill) |
| bloom_warden, kindled_champion | MISC | Court trial code | Lighthouse Garden, Vent Sanctum | none | OK |
| peacock_spider | CREATURE (class `Spider`) | mod `forest`/`birch`/`flower_forest`/`meadow` 5/1-2; `CircusPerformanceController` | Overworld forests | chitinous_husk, puppeteering_thread | **Natural spawn almost never succeeds (EC-7)** |
| vampire_bat | AMBIENT | mod `#is_nether` 4/8-20 (cluster 20); Ringmaster aggregate | every Nether biome plus Phlegethontic Basin | calcified_blood_spine | Pack size and prey rule (EC-12) |
| hematic_construct | MONSTER | Blood Trial Altar, Chapel Downfall | rites and mausoleums | crude memory, scrap | OK |
| hemorath, seraphae, putriciel, velorum | MONSTER | `HarbingerSaintEncounterHooks` | Saint mausoleums | residuum plus quintessence | OK |
| seraphae_fragment | MONSTER | Seraphae fight | mausoleum | formation | OK |
| annetta_knowles, stained_priestess, latent_annetta_infection | MONSTER | `BrokenChurchStructure` plus encounter code | Broken Church, **Overworld `fungal_gardens` only** | none; code drops | Site reliability (EC-9) |
| vesper_crowned_refusal, vesper_evening_star | MONSTER | `VesperOrdealManager` | ordeal arena | Evening Star table unreachable | EC-29 |
| mycophant | MONSTER | `MycophantEncounterManager` | Gardens hunt, Fruiting Lure | mycophant_tendril | OK |
| leech | CREATURE (`Animal`) | **none**: spawn egg and sugar breeding only | none | swollen_leech | **High (EC-1)** |
| bloodlicker | MONSTER | `CardinalRiteOrdealEngine` | rite ordeals | none | OK |
| bog_revenant | MONSTER | mod swamp, mangrove 8/1-2; Bog Body break; Chapel Downfall | swamps | rotten flesh, vivianite, adipocere | OK |
| fargone | MONSTER | OW `fungal_gardens` JSON **10/8-8**; dimension meadows 8, spires 10, drifting 8, thicket 6; Blood Moon; Dynasty Castle NBT | Overworld and Gardens | proboscis, formation | Template stats (EC-5); OW pack (EC-3) |
| desiccant | MONSTER | mod desert, badlands, savanna 6/1-2 | hot dry biomes | telson | OK |
| thirster | MONSTER | isles 10/1-4; Blood Moon; Dynasty tower NBT | Gardens, Overworld at Blood Moon | flask, crude memories, formation | Template stats (EC-5) |
| abhorrent_thought | MONSTER | OW `fungal_gardens` **10/8-8**; meadows 10, isles 8, spires 12, drifting 15, plateau 6 | Overworld Fungal Gardens plus 6 dimension biomes | formation | **Template stats (EC-5)**; OW (EC-3) |
| erythromycelium_eruptus | MONSTER | thicket 15, depths 8 | Gardens | spore_sac | OK |
| blood_drunk_puppeteer | MONSTER | mod 24/1-1 (dark forest, swamps, old-growth taigas, **plus thicket and plateau**); JSON thicket 4, plateau 3; Will anchors; Carousel captive | Overworld and Gardens | Enthralling Filament (`puppeteering_thread`) | **Duplicate entries (EC-4)** |
| phlegethosoma | MONSTER | basin mod 1/1-1, 64-block exclusion | Phlegethontic Basin | spine, formation | OK |
| excoriated | MONSTER | basin JSON 4/1-2; `PhlegethonticWorldgenFeature` guardian | Basin | spine, harpoon, bone | OK |
| phlegethontic_bombardier | MONSTER | basin mod 8/1-3 | Basin | husk | OK |
| will | MONSTER | `WillAmbushDirector` (3 per player, 8 per dimension, cooldown) | anywhere | empty | OK |
| blood_thrall, veinwing_vulture, cinder_bellows, marrow_spitter, gorebound_hulk, mnemonist_puppet, ringmaster_pattern, scarlet_mummer, sanguine_hound, drudge | CREATURE | player summons and constructs | n/a | mostly none | OK |
| enthralled_doll | MONSTER | puppeteer and performer summons | n/a | bleeding_bulb, gated by `shouldDropLoot` | OK |
| chiton | benthos | rockpool JSON 26/2-4 | Rockpool Shore | chitinous_husk | OK; doc says empty table (EC-24) |
| pyrosome | pyrosomes | pelagic 18/1-2, twilight 2/1-1 | open water | empty (intended) | OK |
| pelagic_herring | WATER_AMBIENT | pelagic 45/6-10, twilight 3 | open water | raw_pelagic_herring | OK |
| ice_fish | WATER_AMBIENT | mod cold and frozen oceans, frozen river, ice spikes 12/3-5 | cold water | cleansing_hemolymph | OK |
| siphonophore | WATER_CREATURE | twilight 18/1-2, midnight 2 | Twilight Ocean | ganglion_cluster | OK; preys on herring |
| bloody_belly_comb_jelly | WATER_AMBIENT | midnight 24/1-2, carrion 2 | Midnight Ocean | cuttlefish_chromatophores | OK |
| hagfish | benthos | carrion 30/2-4, hydrothermal 2 | Carrion Depths | empty (intended) | OK |
| vampire_squid | WATER_AMBIENT | carrion 24, hydrothermal 24 | deep layers | calcified_blood_spine | OK |
| barbed_urchin | WATER_AMBIENT | reef 22/2-4; Gardens water (5 biomes, 1-2) | reef, fungal sea | calcified_blood_spine | OK (template stats, but passive) |
| chalybeate_snail | benthos | hydrothermal 32/2-4, carrion 2; vent feature clusters (persistent, one time) | Hydrothermal Depths | scrap | OK |
| blood_lantern_jelly | WATER_AMBIENT | midnight 28/2-4, reef 3, twilight 3, carrion 1 | Midnight Ocean | empty (intended) | OK; HR §26.5 stale |
| mnemonic_whale | WATER_CREATURE | pelagic 12/1-1, twilight 1, reef 1 | open water | empty (intended) | OK; HR §26.5 stale |
| brined_votary | MONSTER | `HarbingerVoyagerWreckStructure` (persistent) | Voyager wrecks (reef) | scrap, nugget; letter in code | OK |
| prism_cuttle | WATER_CREATURE | mod warm, lukewarm, deep lukewarm 14/1-3; twilight 28, pelagic 2, reef 2, midnight 1 | Twilight Ocean plus warm oceans | chromatophores, glow ink | OK |
| lump_of_thought | MONSTER | isles 5, spires 8 | Gardens | formation | Template stats, no stroll goal (EC-5) |
| morphling_polyp | MONSTER | mod `#is_overworld` 3/1-1 (ocean throttled) | Overworld incl. pelagic; not OW Fungal Gardens | morphling_polyp | OK |
| antibody | MONSTER | `GardensNetwork` suspicion summons (lifetime-bound) | Gardens | empty | OK |
| hunter_fungus | MONSTER | meadows 6, spires 5, drifting 5, depths 4, thicket 6 | 5 of 7 Gardens biomes | spore_sac | OK |
| facsimile_bat / _enderman / _blaze / _golem / _horse / _donkey / _wolf / _spider | AMBIENT / MONSTER / MONSTER / CREATURE / CREATURE / CREATURE / CREATURE / MONSTER | dimension JSON only (bat in spires, depths, thicket; enderman in drifting, spires, depths; blaze and golem in plateau; horse and donkey in meadows; wolf in meadows, isles; spider in depths, thicket) | Gardens dimension | spore_sac | OK |
| fungling | CREATURE (hostile `PathfinderMob`) | meadows 10, isles 8, drifting 8, thicket 12; Mycophant brood | Gardens | spore_sac | Category (EC-19); template stats (EC-5) |
| tooth_pecks | MONSTER | Devil's Tooth break chance; tooth geodes; Broken Church jar | old-growth taigas, geodes | bleeding_bulb, bone meal | OK |
| hemolymphopoda | AMBIENT | rockpool JSON 30/2-5; **mycelial_depths JSON 20/3-8**; mod beaches, rivers, swamps, mushroom fields, lush and dripstone caves 30/2-5 | shores, caves, dry Gardens gorge | cleansing_hemolymph | Habitat (EC-14) |
| lantern_tick | MONSTER | mod dripstone, lush, deep_dark 6/1-1 | caves | lantern_tick_helmet | OK |
| myelin_borer | MONSTER | cortical_drift JSON 8/1-2; `VagrantMindPiece` (persistent) | Cortical Drift | ganglion_cluster | OK; dead tag (EC-20) |
| mortarbound | MONSTER | mod deep_dark 2/1-1 (y<0, wall face) | Deep Dark | sculk_vein | OK |
| naeglerophaeon | MONSTER | `VagrantMindEncounterManager` | Vagrant Mind | naeglerophaeon_ganglion | OK |
| chitinite | CREATURE (`PathfinderMob`) | mod lush, dripstone, deep_dark 10/1-3; thicket 8/2-4 | caves, Gardens | chitinous_husk | Despawn cycle (EC-8) |
| fervent_chitinite | CREATURE | mod basalt_deltas 2/1-1 | Basalt Deltas only | blood_crystal, fervent_husk | Rare key drop (EC-17) |
| venom_rib_centipede | MONSTER | mod forests, jungles, swamps, dark forest, lush caves 4/1-1 | damp temperate | toxicognath, spider eye | Prey mismatch (EC-16) |
| chthonian | MONSTER (class `Spider`) | drifting 6, thicket 8; termite mounds (persistent) | Gardens, savanna mounds | chitinous_husk | **Never attacks; template stats (EC-5)** |
| chthonian_queen | MONSTER | `TermiteMoundFeature`, one per mound | savanna | husk, ferric enzyme, physogastrism | **"Boss" with 7 HP and no attack (EC-5)** |
| crimson_doe | CREATURE (`PathfinderMob`) | mod plains, forests, cherry, meadow, OW fungal_gardens 12/1-3 | Overworld | venison, bleeding bulb, formation | Despawn cycle (EC-8); doc drift (EC-15) |
| verdigris_moth | AMBIENT | mod forests, taigas, snowy 9/1-2 (night) | Overworld | empty (intended) | OK |
| choir_keeper | AMBIENT | mod End highlands, midlands, barrens, small islands 7/1-1 | End | venous_pinion | OK |
| osteophage | AMBIENT | mod soul_sand_valley 2/1-1 | Soul Sand Valley | venous_pinion | OK |
| luminal_cicada | AMBIENT | mod dark_forest, lush_caves 7/1-2 | Overworld | cuttlefish_chromatophores | OK (odd shared drop) |
| hematic_burrower | CREATURE | mod forests 18/1-2 | shallow forest caves | clay | OK |
| scarlet_serpent | CREATURE | mod desert, badlands, swamps, jungles 8/1-2 | Overworld | serpent_scale | Despawn cycle (EC-8) |
| hemojelly | WATER_CREATURE | mod warm, lukewarm, ocean, deep oceans, **plus rockpool, pelagic, reef** 4/1-2 | ocean surfaces | thread, vivacious enzyme | Outside Pelagic tables (EC-30) |
| venous_strider | CREATURE | mod mushroom fields, swamps, OW fungal_gardens, **fungal_isles** 10/1-3; plateau JSON 5/1-2 | Overworld and Gardens | spine, formation, pinion, spores | Two sources of truth (EC-15) |

## Biome table

Features are listed by placed-feature name. "+mod" marks additions from biome modifiers.

| Biome | Features | Spawns | Verdict |
| --- | --- | --- | --- |
| fungal_gardens (Overworld, TerraBlender; replaces jungle, mushroom fields, swamp, mangrove) | fungal_floor, morphic_pool, hyphae_tendril, venous_ridge, canopy (sparse), huge_fungus, infested_venous_stone_blob, mycelium_blob, bleeding_hearts, patch_hyphae, small_infected_fungus, stink_horns, +mod earthen_vein. **No ores, lakes or springs.** | monster: abhorrent_thought 10/8-8, fargone 10/8-8 (nothing else); creature: mooshroom 8/4-8 (cannot spawn), +mod crimson_doe, venous_strider; ambient: bat; water: urchin | **EC-3** |
| mycelial_meadows | fungal_floor, morphic and rendering pools, tendrils, sparse canopy, huge fungus, mycelium blob, bleeding hearts, hyphae, field fungi, stink horns, gill curtain | fungling, facsimile horse, donkey and wolf; abhorrent, fargone, hunter; urchin | OK |
| fungal_isles | floor, pools, tendrils, sparse canopy, mycelium blob, hearts, field fungi, stink horns | fungling, facsimile wolf, +mod venous_strider; abhorrent, thirster, lump; urchin | OK; precipitation `true` vs doc (EC-13) |
| sporecrown_thicket | floor, pools, tendrils, dense canopy, huge fungus, blobs, hearts, hyphae, fungi, stink horns, crimson flames, gill curtain, recollection cyst | fungling, chitinite; facsimile bat; eruptus, chthonian, fargone, **puppeteer x2**, facsimile spider, hunter; urchin | **EC-4** |
| hyphal_spires | floor, tendrils, ridge, huge fungus, blobs, hyphae, fungi, sporite cluster, recollection cyst | facsimile bat; abhorrent, fargone, lump, facsimile enderman, hunter; urchin | OK |
| drifting_mycelium | floor, pools, tendrils, canopy, huge fungus, blobs, hearts, hyphae, fungi, ghost pipes, gill curtain | fungling; abhorrent, fargone, chthonian, facsimile enderman, hunter; urchin | OK |
| mycelial_depths | spore nexus tower, floor, morphic pool, hematic iron ore, tendrils, huge fungus, conscious mass, ghost pipes, sporite, gills, cyst | **hemolymphopoda 20/3-8**, facsimile bat; eruptus, facsimile spider and enderman, hunter | EC-14 |
| hemorrhagic_plateau | spore nexus tower, floor, morphic pool, ore, ridge, infested blob, hearts, stink horns | venous strider, facsimile golem; abhorrent, **puppeteer x2**, facsimile blaze | **EC-4** |
| rockpool_shore | pelagic_seabed, pelagic_habitat, court_signs, +mod small_infected_fungus | hemolymphopoda (ambient), chiton (benthos), +mod hemojelly, polyp | No ores or underground features (EC-6) |
| erythrocoral_reef | erythrocoral_reef, court_signs, +mod field fungi | urchin, lantern jelly, tropical fish, pufferfish; squid, dolphin, whale, cuttle; +mod hemojelly, polyp | EC-6 |
| pelagic_ocean | seabed, habitat, court_signs, +mod field fungi | herring; whale 12, cuttle, squid; pyrosome; +mod hemojelly, polyp | EC-6; no drowned (EC-27) |
| twilight_ocean / midnight_ocean / carrion_depths / hydrothermal_depths | seabed, habitat, court_signs (+mod field fungi; hydrothermal +mod deep_ocean_vent) | as in the creature table | OK as water layers; underground handled by EC-6 |
| phlegethontic_basin (Nether) | basin terrain, glowstone_extra, escharian overgrowth, +mod phlegethontic_vein | excoriated; +mod bombardier, phlegethosoma, vampire_bat | No quartz, gold or debris (EC-27) |
| cortical_drift (End) | none in JSON (archipelago through the `#is_end` modifier, by design) | myelin_borer | OK (documented) |

Vanilla biomes touched by modifiers: 59 vanilla biomes plus `#c:is_swamp`, `#c:is_spooky`, `#minecraft:is_forest`, `#minecraft:is_jungle`, `#minecraft:is_taiga`, `#minecraft:is_overworld`, `#minecraft:is_end` and `#c:is_nether`. The busiest are dark_forest (9 modifiers), swamps and mangroves (8 each), old-growth taigas (7 each) and lush_caves (6).

## Findings

### EC-1 · High · Leeches never spawn, but their only drop gates the Morphling Incubator

- **Claim.** No biome JSON, modifier, structure or code path creates a Leech. The only sources are its spawn egg and sugar breeding. `swollen_leech` comes only from the Leech's loot table, yet it is a required brazier offering of the D5 Working that builds the Morphling Incubator. Every strain needs the Incubator to mature past Developing.
- **Behaviour is also a placeholder.** The HR describes a "blood-sucking leech", but the AI walks to crop blocks and is tempted by sugar.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java:469`
  - `src/main/resources/data/hemomancy/loot_table/entities/leech.json` (the only `swollen_leech` source; no chest, trade or modifier lists it)
  - `src/main/resources/data/hemomancy/recipe/cardinal_rite/working/morphling_incubator.json:18`
  - `docs/HEMOMANCY_REFERENCE.md:2675`, `:4285`
  - `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/LeechEntity.java:93-95`, `:128-134`
- **Confidence:** Verified.
- **Action:** fix. Give leeches a natural spawn (swamps, rivers or bog bodies fit the lore) or add another Swollen Leech source, and give them a feeding behaviour.
- **User's call:** yes, on habitat and behaviour.

### EC-2 · High · Wild Lethean Poppies drop nothing when broken

- **Claim.** `lethean_poppy` has no block loot table, although every other flower and fungus has one. The 2026-10 resource test checks only the potted poppy.
- **Why it matters.** The `lethean_poppies` feature scatters poppies across taiga and snowy biomes, but a player cannot pick them; right-click only yields Lethean Dew. The poppy item is needed for:
  - 5 Unstained Cardinal Rite patterns: glass_lungs, lethean_baptism, severed_covenant, silver_dawn, silver_veil
  - the Wreath
  - Pale Distillate
  - distillation
  - the 4-poppy Unstained observance
- **Only source.** The Acolyte's trade (2 poppies for 4 wool). The Zealot's line tells players to weave "whole" wild plants into a wreath.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java:949`
  - absence of `src/main/resources/data/hemomancy/loot_table/blocks/lethean_poppy.json`
  - `src/main/resources/data/hemomancy/npc_trade/acolyte/wool.json:14`
  - `src/main/resources/data/hemomancy/recipe/cardinal_rite/lethean_baptism.json:27`
  - `src/main/java/com/vincenthuto/hemomancy/common/mission/unstained/UnstainedObservances.java:130`
  - `src/test/java/com/vincenthuto/hemomancy/common/worldgen/FieldMycologyResourceTest.java:50-55`, `:71`
- **Confidence:** Verified.
- **Action:** fix. Add a self-drop table with silk-touch and explosion conditions like the other flowers, and add `lethean_poppy` to `assertSinglePlantDrop`.
- **User's call:** no, unless the trade-only route is intended.

### EC-3 · High · The Overworld Fungal Gardens has a placeholder spawn list and no vanilla ores

- **Monsters.** The only monsters are `abhorrent_thought 10/8-8` and `fargone 10/8-8`. No vanilla monster spawns, so no bones, string or gunpowder. A 1.5 x 3.25 eldritch "thought" spawns in Overworld clusters.
- **Creatures.** `mooshroom 8/4-8` cannot spawn because the surface is `erythrocytic_mycelium`, not `#mooshrooms_spawnable_on`.
- **Features.** No ore, lake, spring or underground-decoration feature. `FungalGardensOverworldRegion` replaces jungle, mushroom fields, swamp and mangrove at every parameter point, so the underground under those columns is this biome too.
- **Default exposure.** The region is on by default.
- **Tags.** The biome is also missing from `#minecraft:is_overworld` and `#c:is_overworld`. The field-fungi and polyp modifiers, and other mods, skip it.
- **Evidence:**
  - `src/main/resources/data/hemomancy/worldgen/biome/fungal_gardens.json:39-64` (spawners) and its `features` block
  - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/biome/FungalGardensOverworldRegion.java:22-37`
  - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/biome/FungalGardensSurfaceRuleData.java:14`, `:22-31`
  - `src/main/resources/data/minecraft/tags/worldgen/biome/is_overworld.json`
  - `src/main/java/com/vincenthuto/hemomancy/config/HemoCommonConfig.java:9`
- **Confidence:** Verified for the data. The underground reach is Suspected; one F3 check under a jungle-replacement column settles it.
- **Action:** fix or expand. Write a real Overworld spawn list (mod fauna plus vanilla monsters at sane pack sizes), add `BiomeDefaultFeatures`-equivalent ores, and tag the biome `is_overworld`.
- **User's call:** yes. Should Abhorrent Thoughts appear in the Overworld at all?

### EC-4 · High · Blood-Drunk Puppeteer is listed twice in two Gardens biomes

- **Claim.** The puppeteer spawn tag adds `sporecrown_thicket` and `hemorrhagic_plateau` at weight 24, on top of the generator's direct entries (4 and 3).
- **Effective shares of monster weight:**
  - Hemorrhagic Plateau: 27/39 (69%)
  - Sporecrown Thicket: 28/71 (39%)
- **Consequence.** Each puppeteer brings four Enthralled Dolls. `tools/worldgen/fungal_gardens_worldgen.py` is meant to be the single source for these biomes, but the tag bypasses it.
- **Evidence:**
  - `src/main/resources/data/hemomancy/tags/worldgen/biome/blood_drunk_puppeteer_spawnlist.json:9-10`
  - `src/main/resources/data/hemomancy/neoforge/biome_modifier/add_blood_drunk_puppeteer.json:7`
  - `src/main/resources/data/hemomancy/worldgen/biome/sporecrown_thicket.json:75`
  - `src/main/resources/data/hemomancy/worldgen/biome/hemorrhagic_plateau.json:48`
  - `tools/worldgen/fungal_gardens_worldgen.py:417`, `:438`
- **Confidence:** Verified.
- **Action:** fix. Remove the two `hemomancy:` biomes from the tag and keep the generator's weights.
- **User's call:** no.

### EC-5 · High · Seven creatures keep the copy-paste template stats, and Chthonians never attack

- **Claim.** These all carry `MAX_HEALTH 7, MOVEMENT_SPEED 0.3, ATTACK_DAMAGE 1`:
  - Abhorrent Thought (endgame Gardens hazard, 1.5 x 3.25)
  - Fargone (all Dynasty Castle variants, Elder and Guard included)
  - Thirster
  - Lump of Thought
  - Fungling
  - Chthonian and Chthonian Queen (the "boss variant, exactly 1 per mound")
- **Same template.** Enthralled Doll and Barbed Urchin share it but are minions or passive.
- **Chthonian behaviour.** `AbstractChthonianEntity` overrides `registerGoals` without any target selector, so Chthonians never set a target. They never hit, and the wooden-tool wear in `doHurtTarget` never fires.
- **Idle goals.** Thirster and Lump have no stroll goal.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/AbhorrentThoughtEntity.java:38-40`
  - `.../mob/arthropod/FargoneEntity.java:40-42`
  - `.../mob/monster/ThirsterEntity.java:21-23`
  - `.../mob/monster/LumpOfThoughtEntity.java:21-23`
  - `.../mob/animal/FunglingEntity.java:131-133`
  - `.../mob/arthropod/AbstractChthonianEntity.java:27-30`, `:73-87`, `:101-106`
  - `docs/HEMOMANCY_REFERENCE.md:4266`, `:4272`
- **Confidence:** Verified.
- **Action:** fix. Give each a tiered attribute block. Give Chthonians a hurt-by and a player or wood-tool target goal, and give the Queen boss-tier stats.
- **User's call:** yes, for the numbers.

### EC-6 · Medium · Pelagic surface habitats have no ore or underground features

- **Claim.** `PelagicOverworldRegion` replaces beach, stony shore, warm and lukewarm oceans and the three deep oceans with Rockpool Shore, Erythrocoral Reef and Pelagic Ocean through `ModifiedVanillaOverworldBuilder`. Those keys then hold the underground parameter points too.
- **Below the seabed.** `PelagicContext.biome` hands the original biome back below `floor - 5`, and that original is the pelagic biome.
- **Missing features.** None of the seven pelagic JSONs lists ores, geodes, monster rooms, disks or springs. Seagrass and kelp are absent too, by design for the reef and shore.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/biome/PelagicOverworldRegion.java:17-22`
  - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/terrain/pelagic/PelagicContext.java:102-110`
  - `src/main/resources/data/hemomancy/worldgen/biome/pelagic_ocean.json:52-57`
  - same pattern in rockpool_shore, erythrocoral_reef and the four depth JSONs
- **Confidence:** Suspected. The data is verified, but no in-world ore scan was run.
- **Action:** expand. Add vanilla ocean ore and underground steps to the three surface habitats, or restore the vanilla biome below the skin.
- **User's call:** yes.

### EC-7 · Medium · Peacock Spiders almost never spawn naturally

- **Claim.** The spawn predicate wants daytime, open sky and raw brightness of at least 9 on dirt. But the class extends `Spider`, which extends `Monster`.
- **Vanilla rule.** `Monster.getWalkTargetValue` makes `PathfinderMob.checkSpawnRules` fail above raw brightness 12.
- **Result.** In clear daylight (brightness 15) every natural attempt fails. Spawns succeed only during rain, thunder or dawn. The Circus act still spawns spiders by code.
- **Category side effect.** Being a `Monster` in the CREATURE category also removes it on Peaceful.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/PeacockSpiderEntity.java:23`, `:41-46`
  - NeoForge sources `net/minecraft/world/entity/monster/Monster.java:85`, `PathfinderMob.java:29-30`, `LevelReader.java:121-129`
  - `src/main/resources/data/hemomancy/neoforge/biome_modifier/add_peacock_spider.json`
- **Confidence:** Verified by code reading; not observed in-world.
- **Action:** fix. Override `getWalkTargetValue` or `checkSpawnRules`, or stop extending `Spider`.
- **User's call:** no.

### EC-8 · Medium · Mod land animals despawn like monsters but respawn on the slow creature cycle

- **Affected creatures.** Crimson Doe, Venous Strider, Scarlet Serpent, Chitinite, Fervent Chitinite, Hematic Burrower and Fungling.
- **Despawn.** They are `PathfinderMob`s in the CREATURE category with no `removeWhenFarAway` override. They despawn beyond 128 blocks, and at random beyond 32 after 30 s idle. Vanilla `Animal` returns `false` here.
- **Respawn.** CREATURE spawns run only at chunk generation and every 400 ticks.
- **Result.** Herds generated with the chunk vanish once the player leaves, and come back only as a trickle.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/CrimsonDoeEntity.java:22`
  - `.../VenousStriderEntity.java:23`, `.../ScarletSerpentEntity.java:26`, `.../mob/arthropod/AbstractChitiniteEntity.java:32`
  - NeoForge sources `net/minecraft/world/entity/animal/Animal.java:121-123`, `Mob.java:732-764`
- **Confidence:** Verified (code); the population effect is Suspected.
- **Action:** decide. Override `removeWhenFarAway` to `false` for the "animal" ones (the vanilla norm), or keep them transient and accept sparse herds.
- **User's call:** yes.

### EC-9 · Medium · Annetta's Broken Church generates only in the optional Overworld Fungal Gardens

- **Claim.** `has_structure/broken_church` lists only `hemomancy:fungal_gardens`.
- **Dependency.** The whole Annetta arc depends on one TerraBlender region that replaces only four vanilla biomes and has a config switch:
  - the founder-only Severed Covenant cure
  - both Annetta weapons
- **Evidence:**
  - `src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/broken_church.json:4`
  - `src/main/java/com/vincenthuto/hemomancy/config/HemoCommonConfig.java:28-30`
  - `docs/HEMOMANCY_REFERENCE.md:4608`
- **Confidence:** Verified.
- **Action:** decide. Add fallback biomes (swamp, dark forest), or document the dependency and make the region non-optional.
- **User's call:** yes.

### EC-10 · Medium · Blood Temples and Unstained Churches can be placed on the Nether roof

- **Claim.** Both biome tags include all five Nether biomes. Their site checks and `project_start_to_heightmap: WORLD_SURFACE_WG` read the first occupied height, which in the Nether is the ceiling (about y 128).
- **Flatness check passes.** The roof is uniformly at the ceiling height, so the temple flatness test succeeds.
- **Consequence.** `afterPlace` would set the Hermit, and the Zealot, Guardians and Acolytes, above the bedrock roof.
- **Contrast.** The Surveyor Bivouac solves exactly this with its ceiling search; the HR notes the surface heightmap "would find the Nether roof".
- **Evidence:**
  - `src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/blood_temple.json:41-45`, `unstained_church.json:41-45`
  - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/StructurePlacementChecks.java:161-178`, `:325-338`
  - `src/main/resources/data/hemomancy/worldgen/structure/blood_temple.json` (`project_start_to_heightmap`)
  - `docs/HEMOMANCY_REFERENCE.md:4613`
- **Confidence:** Suspected (code path verified, not generated).
- **Action:** fix. Drop the Nether biomes, or reuse the Bivouac ceiling logic.
- **User's call:** yes, if Nether temples are wanted.

### EC-11 · Medium · Hyphae cannot be harvested, and bonemeal turns them into vanilla tall grass

- **Claim.** `hyphae` has no loot table, but `recipe/spore_sac.json` needs the `hemomancy:hyphae` item.
- **Bonemeal.** `HyphaeBlock.performBonemeal` is a copy of vanilla tall grass and places `TALL_GRASS` (or `LARGE_FERN`) on fungal ground.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java:627`
  - `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/plant/HyphaeBlock.java:52-56`
  - `src/main/resources/data/hemomancy/recipe/spore_sac.json`
- **Confidence:** Verified.
- **Action:** fix. Add a shears or silk-touch drop table and make bonemeal spread hyphae.
- **User's call:** no.

### EC-12 · Medium · Vampire Bat colonies are oversized, and they hunt their own kind

- **Spawn.** Weight 4 with packs of 8-20 and `getMaxSpawnClusterSize() = 20`, in every Nether biome. It is the only AMBIENT entry there, so one pack can exceed the ambient cap of 15.
- **Prey rule.** `bleedingPrey()` targets any wounded `LivingEntity` within 8 blocks except itself. One injured bat is mobbed by the colony.
- **Evidence:**
  - `src/main/resources/data/hemomancy/neoforge/biome_modifier/add_vampire_bat.json:7-9`
  - `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/VampireBatEntity.java:96`, `:140-143`
- **Confidence:** Verified.
- **Action:** fix. Exclude `VampireBatEntity` from prey, and use cluster 8, packs 4-8.
- **User's call:** no.

### EC-13 · Low · Gardens ambience and weather in the data contradict the reference

- **Doc claim.** HR §28.1 says the dimension's fungal biomes use End music with cave mood "rather than Nether ambient loops or additions".
- **Data, sound.** Five of seven biomes use Nether, soul-sand or crimson loops and additions and have no music. Only meadows and isles carry `music.end`; the generator preserves whatever effects were there.
- **Data, weather.** `fungal_isles` has `has_precipitation: true`; the HR table says None.
- **Evidence:**
  - `docs/HEMOMANCY_REFERENCE.md:4454`, `:4440`
  - `src/main/resources/data/hemomancy/worldgen/biome/drifting_mycelium.json:16`, `hyphal_spires.json:16`, `mycelial_depths.json:16`, `hemorrhagic_plateau.json:10`, `sporecrown_thicket.json:18`, `fungal_isles.json:4`
  - `tools/worldgen/fungal_gardens_worldgen.py:470-477`
- **Confidence:** Verified.
- **Action:** decide which is canon, then let the generator own `effects`.
- **User's call:** yes.

### EC-14 · Low · Tidepool horseshoe crabs spawn in dry places

- **Dry gorge.** Hemolymphopoda (tide-pool fauna in PELAGIC_ECOLOGY) spawns at 20/3-8 in the dry Mycelial Depths gorge of the alien-only dimension.
- **Off-shore predicate.** Away from the shore it needs only solid ground at or below sea level + 16, so it also fills lush and dripstone caves.
- **Displacing bats.** At weight 30 it takes 75% of ambient spawns in rivers, swamps and beaches.
- **Evidence:**
  - `tools/worldgen/fungal_gardens_worldgen.py:433`
  - `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/HemolymphopodaEntity.java` (`canSpawnHere`)
  - `src/main/resources/data/hemomancy/tags/worldgen/biome/hemolymphopoda_spawnlist.json`
- **Confidence:** Verified.
- **Action:** decide. Require water nearby, or drop it from the Gardens.
- **User's call:** yes.

### EC-15 · Low · Crimson Doe and Venous Strider docs and sources drift

- **Crimson Doe.** HR says it spawns "in no-skylight Fungal Gardens biomes". No dimension biome lists it; only the Overworld `fungal_gardens` gets it, through a tag.
- **Venous Strider.** Its `fungal_isles` spawn comes from a vanilla-style tag that sits outside the generator that owns the dimension spawns.
- **Evidence:**
  - `docs/HEMOMANCY_REFERENCE.md:4298`
  - `src/main/resources/data/hemomancy/tags/worldgen/biome/crimson_doe_spawnlist.json`, `venous_strider_spawnlist.json`
  - `tools/worldgen/fungal_gardens_worldgen.py:408-412`
- **Confidence:** Verified.
- **Action:** combine. Move dimension spawns into `BIOME_SPAWNS`, then fix the HR.
- **User's call:** no.

### EC-16 · Low · Some predator-prey pairs never meet, and prey do not flee

- **No overlap.** Venom-Rib Centipede targets Fervent Chitinite, which lives only in Basalt Deltas, a biome the centipede never shares.
- **No fleeing.** Its other prey (Chitinite, Hemolymphopoda, Tooth Pecks, Verdigris Moth) have no `AvoidEntityGoal` for it, and Prism Cuttles do not flee the Mnemonic Whale. Panic-on-hurt is the only reaction.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/VenomRibCentipedeEntity.java:70`
  - `src/main/resources/data/hemomancy/tags/worldgen/biome/fervent_chitinite_nether_spawnlist.json`
- **Confidence:** Verified.
- **Action:** expand (avoid goals) or cull the dead target.
- **User's call:** no.

### EC-17 · Low · Fervent Husk, a D6 Archon-rite offering, has a thin natural source

- **Natural source.** Fervent Chitinite spawns only in Basalt Deltas at weight 2 vs vanilla strider 60, on the 400-tick creature cycle, and needs brightness of 7 or less.
- **Other source.** The Hemopothecary villager's level-2 trade.
- **Evidence:**
  - `src/main/resources/data/hemomancy/neoforge/biome_modifier/add_fervent_chitinite_cave.json:7-9`
  - `src/main/resources/data/hemomancy/recipe/cardinal_rite/archon_rite.json:60`
  - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/village/VillageEvents.java:52`
- **Confidence:** Verified.
- **Action:** decide. Raise the weight or widen the biomes, or document the trade.
- **User's call:** yes.

### EC-18 · Low · Hemopothecary villagers sell a house Morphling strain at novice level

- **Claim.** The novice trade sells the Marrowplume strain item for 1 emerald.
- **Conflict.** That bypasses the polyp-to-incubation ecology and the Last Marrow house, whose hereditary strain it is.
- **Evidence:** `src/main/java/com/vincenthuto/hemomancy/common/worldgen/village/VillageEvents.java:48`
- **Confidence:** Verified.
- **Action:** decide (cull or keep).
- **User's call:** yes.

### EC-19 · Low · Fungling is a hostile mob in the friendly CREATURE category

- **Claim.** Fungling is a CREATURE-category `PathfinderMob` with a player target goal.
- **Consequences:**
  - It spawns and attacks on Peaceful.
  - It is not removed there.
  - It slips past `Monster`-keyed systems such as the Fane barrier and Virid Salis.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java:765`
  - `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/FunglingEntity.java:47`, `:315-321`
- **Confidence:** Verified.
- **Action:** fix (MONSTER plus `shouldDespawnInPeaceful`) or decide.
- **User's call:** no.

### EC-20 · Low · Dead spawn tags and a disabled worldgen chain

- **Tags with no reader:**
  - `dessicant_spawnlist` (typo duplicate)
  - `myelin_borer_spawnlist` (the Borer spawns only from the Cortical Drift JSON)
  - five dormant-mob tags; see architecture decision M-2, not repeated here
- **Disabled chain.** `silver_bells_tower_spawnlist` was emptied in `80444dc12`, but its modifier, placed feature and configured feature still load.
- **Evidence:**
  - `src/main/resources/data/hemomancy/tags/worldgen/biome/dessicant_spawnlist.json`, `myelin_borer_spawnlist.json`, `silver_bells_tower_spawnlist.json`
  - `src/main/resources/data/hemomancy/neoforge/biome_modifier/add_silver_bells_tower.json`
- **Confidence:** Verified.
- **Action:** cull, or decide on the tower.
- **User's call:** yes, for the tower only.

### EC-21 · Low · Infected Cap carries dead grass-spread code

- **Claim.** `InfectedCapBlock` extends `SnowyDirtBlock` and copies grass spread and decay logic, which would rot covered cap blocks to dirt.
- **Dead code.** Neither `infected_cap` nor `fruiting_infected_cap` sets `randomTicks()`, so the code never runs. The blocks also carry a pointless `snowy` state.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/plant/InfectedCapBlock.java:17`, `:22`, `:45-72`
  - `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java:978-982`
- **Confidence:** Verified.
- **Action:** cull.
- **User's call:** no.

### EC-22 · Low · Erythrocytic Mycelium cannot spread in its own dimension

- **Claim.** It spreads only when raw brightness above is at least 9, the vanilla mycelium rule.
- **Consequence.** The Gardens dimension has no skylight and ambient light 0.08, so its surface never spreads there, only under torches.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/plant/ErythrocyticMyceliumBlock.java:55`
  - `src/main/resources/data/hemomancy/dimension_type/fungal_gardens.json`
- **Confidence:** Verified.
- **Action:** decide (drop the light rule, or accept it).
- **User's call:** yes.

### EC-23 · Low · `BiomeInit.bootstrapBiomes` is dead code that disagrees with the shipped JSON

- **Claim.** The bootstrap gives `fungal_isles` a weight-30 ghast and gives `fungal_gardens` funglings, chitinites, poppies, ghost pipes, sarcodes and rafflesia. None of that ships.
- **Known.** Tests already note the method is dead.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/init/BiomeInit.java:57-68`, `:87-98`, `:138-197`
  - `src/test/java/com/vincenthuto/hemomancy/common/entity/MyelinBorerSpawnSourceTest.java:26`
- **Confidence:** Verified.
- **Action:** cull (with the PelagicBiomes bootstrap).
- **User's call:** no.

### EC-24 · Low · Reference spawn, loot and category tables are stale

- **§26.5 spawn placements:**
  - says "registered in `commonSetup`" (it is `onRegisterSpawnPlacements`)
  - gives Hemolymphopoda as `ON_GROUND` (code: `NO_RESTRICTIONS`)
  - says Lantern Jelly and Whale spawn "only through the Erythrocoral Reef" and the Cuttle "warm/lukewarm only"
  - omits about 30 registrations
- **§26.6 loot.** Says 44 entity loot tables; there are 86.
- **§28.2 and §28.3 vents.** Still put `deep_ocean_vent` in deep vanilla oceans; it is Hydrothermal-only.
- **§26.2 categories.** Lists Hemojelly as Ambient, and Prism Cuttle and Chalybeate Snail as Water Ambient; the code has WATER_CREATURE, WATER_CREATURE and pelagic_benthos.
- **Bivouac row.** Says `fungal_gardens` is used by "no dimension"; it is an enabled Overworld biome.
- **PELAGIC_ECOLOGY.** Says six new animals have empty tables; Chiton, Herring, Siphonophore and Comb Jelly drop items.
- **Evidence:**
  - `docs/HEMOMANCY_REFERENCE.md:4340-4359`, `:4364`, `:4501`, `:4511`, `:4296-4301`, `:4613`
  - `docs/PELAGIC_ECOLOGY.md:18`
- **Confidence:** Verified.
- **Action:** fix the docs.
- **User's call:** no.

### EC-25 · Low · OPEN_QUESTIONS §1.4's squid-leash item is wrong

- **Open-question claim.** "Nothing in the mod makes squid leashable".
- **Vanilla behaviour.** 1.21.1 `Squid.canBeLeashed()` returns `true`, and `MnemonicWhaleGameTests.huntsLeashedSquid` asserts and uses it.
- **Result.** The whale, Liber and Voyager lines are accurate.
- **Evidence:**
  - `docs/OPEN_QUESTIONS.md:77`
  - NeoForge sources `net/minecraft/world/entity/animal/Squid.java:78-80`
  - `src/gameTest/java/com/vincenthuto/hemomancy/gametest/MnemonicWhaleGameTests.java:121-130`
- **Confidence:** Verified.
- **Action:** fix the docs (close the question).
- **User's call:** no.

### EC-26 · Low · Crimson Lodge Annex places with no template

- **Claim.** The structure set (spacing 40) and its biome tag are live, but `structure/crimson_lodge_annex.nbt` does not exist.
- **Docs.** The wiki says annexes "do not generate yet".
- **Evidence:**
  - `src/main/resources/data/hemomancy/worldgen/structure_set/crimson_lodge_annex.json`
  - `src/main/resources/data/hemomancy/worldgen/template_pool/crimson_lodge_annex/start_pool.json`
  - absence of the NBT under `src/main/resources/data/hemomancy/structure/`
  - `wiki/World-Content.md:36`
- **Confidence:** Suspected. A missing jigsaw template likely yields an empty start or log noise, plus phantom `/locate` hits.
- **Action:** cull the set until the template exists.
- **User's call:** no.

### EC-27 · Low · Some mod biomes lack vanilla hostile and ore content

- **Pelagic.** No pelagic biome spawns any natural monster: no drowned, so no tridents, nautilus shells or copper from them.
- **Phlegethontic Basin.** No quartz, gold or ancient-debris features.
- **Pavilion.** Separately, the 65 x 65 Crimson Troupe pavilion has 8 light blocks and no structure declares `spawn_overrides`, so vanilla monsters can spawn inside it at night.
- **Evidence:**
  - pelagic biome JSON `spawners`
  - `src/main/resources/data/hemomancy/worldgen/biome/phlegethontic_basin.json`
  - template light count: `crimson_troupe_pavilion.nbt` has 8 emitters (6 lanterns, 2 hematic lanterns)
  - no `spawn_overrides` in `src/main/resources/data/hemomancy/worldgen/structure/*.json`
- **Confidence:** Verified for the data; the pavilion is Suspected.
- **Action:** decide.
- **User's call:** yes.

### EC-28 · Low · NPC loot tables are inconsistent

- **Claim.** Of the four Outpost professionals, only the Alchemist (hemolytic solution) and Vicar (formation) have death tables. These fire only for successors and misbegotten.
- **Gaps.** The Artificer, Mnemonist and Anchorite have none.
- **Evidence:**
  - `src/main/resources/data/hemomancy/loot_table/entities/harbinger_alchemist.json`, `harbinger_vicar.json`
  - `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerAlchemistEntity.java:71-73`
- **Confidence:** Verified.
- **Action:** decide (add matching tables or remove both).
- **User's call:** no.

### EC-29 · Low · The Vesper Evening Star loot table is unreachable

- **Claim.** At 0 HP the fight enters an absorption state; victory then `discard()`s the boss and pays out through `PendingDelivery`.
- **Result.** `entities/vesper_evening_star.json`, which drops `memory_of_vesper`, never runs.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/endgame/VesperTheEveningStarEntity.java:333-336`, `:616-624`
  - `src/main/resources/data/hemomancy/loot_table/entities/vesper_evening_star.json`
- **Confidence:** Verified.
- **Action:** cull. Alternatively, keep the table as a deliberate fallback and say so.
- **User's call:** no.

### EC-30 · Low · Hemojelly is added to Pelagic biomes outside the Pelagic population tables

- **Claim.** `hemojelly_spawnlist` adds Rockpool Shore, Pelagic Ocean and Erythrocoral Reef at WATER_CREATURE 4/1-2, so it competes with whales, cuttles and siphonophores.
- **Conflict.** PELAGIC_ECOLOGY says `PelagicPopulation` and the biome JSON "describe the same spawn packs", and does not mention the Hemojelly.
- **Evidence:**
  - `src/main/resources/data/hemomancy/tags/worldgen/biome/hemojelly_spawnlist.json`
  - `docs/PELAGIC_ECOLOGY.md` ("Population and world generation")
- **Confidence:** Verified.
- **Action:** combine (move into the Pelagic tables or drop the three biomes).
- **User's call:** no.

## Not a problem

- **Spawn placements.** Every one of the 54 naturally spawning mod types has a `SpawnPlacements` registration (`EntityInit.java:1036-1201`). The two enum-extended categories are defined with sensible caps (`META-INF/enumextensions.json`), and biome JSON uses the same keys.
- **Feature wiring:**
  - Every placed feature under `worldgen/placed_feature` is placed by a biome JSON or modifier.
  - Every feature a biome references exists; `glowstone_extra` is vanilla.
  - Only `tree/selector/vanilla/vanilla_mushrooms` is a configured feature with no placement.
- **Template inhabitants.** Circus pavilion, camp and Dynasty Castle entities all carry `PersistenceRequired:1`.
- **Code-placed inhabitants.** These all set persistence:
  - outpost, temple, respite, churches, Voyager vessel and wreck
  - termite mound, tooth geode, vent snails
  - Vagrant Mind borers, Phlegethontic guardian, Court residents
- **Mausoleum templates** intentionally carry no entities; Downfall waves spawn from `spawn_mark` code (SAINT_MAUSOLEUMS.md).
- **Facsimile spawn rules.** The monster facsimiles follow the implicit Monster light rule (raw brightness 11 or less in the 0.08-ambient Gardens), which matches the docs. The animal facsimiles override `checkSpawnRules`, so they spawn in the dark off grass.
- **Bounded code spawners:**
  - antibody summons dissolve
  - Will ambushes are capped at 3 per player and 8 per dimension, with a cooldown
  - Blood Moon spawns are capped at 6 nearby
  - Phlegethosoma has a 64-block exclusion
  - ocean polyps are throttled 1 in 40 with 48-block spacing
  - Court schools are one per region, ever
  - vampire-bat aggregates discard when their owner leaves
- **Feeding and defence behaviours exist:**
  - whale hunting squid, glow squid and cuttles (all share its biomes)
  - siphonophore and hemojelly catching small `AbstractFish`
  - osteophage hunting skeletons
  - hagfish rotten-flesh feeding
  - chiton grazing and clamping, snail retraction, lantern-tick latching, tooth-peck feeding
- **Drop uses.** Every item a mod creature drops has a recipe, rite, trade, curiosity-tag or gameplay use. The empty tables (hagfish, pyrosome, lantern jelly, whale, moth, circus and unstained NPCs) are documented choices.
- **Renewables.** Bog Body vivianite regrows by random tick. Gourd seeds come from the grass loot modifier. The vent feature's snail clusters are one-time by design.
- **TerraBlender surface rules.** The trailing "grass and dirt" rule in `FungalGardensSurfaceRuleData` applies only to `hemomancy:` biomes; TerraBlender's `NamespacedRule` keys on the biome namespace. It never repaints vanilla biomes. The reef branch there is a deliberate fallback for `enablePelagicWorldgen=false`.
- **Pelagic biomes host no natural monsters,** so the Vent Sanctum and Bell dry halls are spawn-safe.
- **Spawn eggs.** Every natural or encounter mob has a spawn egg except bosses, summons and constructs.
