# Blocks and materials utility audit (2026-11-02)

Scope: every registered item and block, how it is obtained and how it is used, plus families that are redundant,
half-built or orphaned. Read-only audit; no source, resource or tool file was changed.

## Summary

Hemomancy registers **850 items** (603 in `ItemInit`, 585 named plus 18 Court-role spawn eggs, and 247 block items
created by `BlockInit.onRegisterItems`) and **280 blocks** (247 with an auto or custom block item, 6 whose item lives in
`ItemInit`, 27 with no item: 7 potted plants, 4 fluids, 16 technical blocks). Creative filing: 457 entries in the
Hematic groups, 316 in the other main-tab sections, 43 on the WIP list (28 items, 15 blocks), 7 hidden by hand and 27
hidden by the retirement rules; `MISC` holds nothing live. After checking every candidate by hand:
**25 entries have no survival source at all** (6 WIP tools and dev items, 8 WIP blocks, 11 finished or hidden ids), **27 retired or
legacy memory items** are still registered, **16 blocks exist only in the world** (their item cannot be obtained; 2 of
them, `engram_block` and `hyphae`, are recipe ingredients), and **8 obtainable entries have no recipe, tag, code or structure use** (four of them plain decoration). Three
unobtainable ids gate live progression: the Unstained rites, the D7 Forge upgrade and the D5 Mnemonist Puppet ordeal.
Six redundancy findings cover 10 families or pairs, and three half-set findings cover 5 gaps. 34 unbreakable set pieces with creative-only items
are working as designed and are listed under "Not a problem".

Method: a Python pass (`python -I`) over `src/main/resources/data` and `src/generated/resources/data` (generated files
skipped where an authored twin exists), a reference scan of `src/main/java` (MnA and Curios excluded, as in
`build.gradle`), and a parse of all 226 structure NBT palettes (main and generated), with reachability checked against
template pools, Java template loaders, the Vigil layouts and the empty legacy mausoleum biome tag. Code-side sources
(dialogue gifts, boss drops, worldgen features) were found by grep and confirmed by reading each call site for every id
named below. GameTest structures and test code are not counted as uses.

---

## 1. Inventory

### MT-1 (Low) 25 structure templates are reachable from nothing, and they hold the only placements of five blocks

- **Claim:** 25 of 226 NBTs are loaded by no pool, structure or Java loader, so the blocks placed only there never reach a world.
- **Evidence:** unreferenced templates: `structure/mausoleum.nbt` and the ten `structure/mausoleum/*.nbt` (legacy structure, biome tag `tags/worldgen/biome/has_structure/mausoleum.json` is empty), `structure/harbinger_outpost.nbt` (the jigsaw outpost only uses `harbinger_outpost/*` pieces; `worldgen/structure/harbinger_outpost.json:3`), `lowtide_ruin/*` (8; the Chamber of Will draws OBJ models of the same names, `client/render/world/chamberofwill/LowtideRuinObjModels.java:11`), `tower/blood_tower_core`, `tower/blood_tower_top_1`, `broken_unstained_church`, `qliphoth_bloom`, `qliphoth_fane` (referenced only from excluded `compat/mna/faction/HarbingersFaction.java:75`). Blocks whose only placements are there: `blood_pylon`, `blood_trial_altar`, `offering_gate` (legacy trial rooms), `suspended_blood_crystal` (legacy outpost and mausoleum roots), `engram_block` (`qliphoth_fane`).
- **Confidence:** Verified.
- **Recommendation:** cull the 14 non-mausoleum dead templates; the legacy mausoleum is already M-6 in `docs/OPEN_QUESTIONS.md` (retire). Effort S. User call: only for `lowtide_ruin` (confirm the OBJ path replaced it).

The full per-id table is in the Appendix.

---

## 2. Orphans

### 2a. Gating progression

### MT-2 (High) `suspended_cleansed_blood_crystal` cannot be obtained, and eight Unstained rites need it

- **Claim:** No recipe, loot table, worldgen, NBT or code places or grants the block, yet it is the `C` key of eight Unstained rite patterns (up to nine per pattern).
- **Evidence:** `common/init/BlockInit.java:1262`; no `loot_table/blocks/suspended_cleansed_blood_crystal.json`; no code outside `BlockEntityInit.java:230`; uses in `recipe/cardinal_rite/pale_consecration.json` (D2), `clarity_ascension.json` (D5), `antiseptic_ground.json` (D6), `moon_washed_copper.json`, `pale_vigil.json` (D7), `lethean_font.json`, `lethean_judgment.json`, `lethe_covenant.json` (D8). Each pattern key names the block directly, with no tag alternative (`pale_consecration.json` key `C`; parsed by `CardinalRitePatternJson.parse` from `common/recipe/serializer/CardinalRiteRecipeSerializer.java:250`).
- **Confidence:** Verified.
- **Recommendation:** expand. Give all three suspended crystals self-drop loot tables, add a crafting recipe for `suspended_blood_crystal`, and add a `white_humor_purification` recipe `suspended_blood_crystal` to `suspended_cleansed_blood_crystal` (the purification pattern already exists for shards, glass and stone, `recipe/white_humor_purification/`). Effort S. User call: the ingredients.

### MT-3 (High) `suspended_blood_crystal` cannot be obtained, and the D7 Enduring Pattern rite takes it as an offering

- **Claim:** The block has no loot table and its only placements are in the two dead templates of MT-1, so the Resonant Forge Masterwork upgrade cannot be performed in survival.
- **Evidence:** `BlockInit.java:1258`; no `loot_table/blocks/suspended_blood_crystal.json`; offering in `recipe/cardinal_rite/enduring_pattern.json` and `docs/STATION_UPGRADES.md:69`; filed in `CreativeTabGroup.NATURAL` (`CreativeTabGroup.java:117`).
- **Confidence:** Verified.
- **Recommendation:** expand, same fix as MT-2 (loot table plus a recipe). Effort S. User call: recipe.

### MT-4 (High) `engram_block` has no item source, so the Mnemonist Cradle and the D5 Mnemonist Puppet ordeal are uncraftable

- **Claim:** The engram block is placed by scratching (`common/event/ScratchEngramHandler.java:101`) or stamping, drops nothing (no loot table) and its item is hidden; `mnemonist_cradle` needs one, and the Mnemonist Puppet Ordeal needs the cradle in a brazier.
- **Evidence:** `BlockInit.java:229`, `BlockInit.java:1388-1389` (hidden item); no `loot_table/blocks/engram_block.json`; `recipe/mnemonist_cradle.json:7`; `recipe/cardinal_rite/puppeteer_trial_mnemonist_puppet.json` (brazier signature `mnemonist_cradle`).
- **Confidence:** Verified (one other placement is the dead `qliphoth_fane` template).
- **Recommendation:** expand: swap the `E` key of `mnemonist_cradle.json` for an obtainable memory item (`mnemonic_folio`, `wax_cylinder` or `engram_stamp`), or give the engram block a self drop (it would then also need a creative entry). Effort S. User call: which.

### MT-5 (Medium) `hyphae` never drops, so three spore-sac recipes are dead

- **Claim:** The cross-model hyphae plant is generated (`worldgen/configured_feature/patch_hyphae.json`) and placed by code, but has no loot table, so its item, an ingredient of three recipes, cannot be had.
- **Evidence:** `BlockInit.java:627`; no `loot_table/blocks/hyphae.json`; `recipe/spore_sac.json`, `spore_sac_from_enzyme_primer.json`, `spore_sac_from_vivacious_spores.json`.
- **Confidence:** Verified. Spore sacs have other sources (11 mob loot tables), so this blocks nothing.
- **Recommendation:** expand: add a shears-or-silk loot table like vanilla grass, or point the three recipes at `hyphae_block`. Effort S. User call: no.

### 2b. Finished or filed content with no source

### MT-6 (Medium) `hemorath_rib`, the top-tier blood vessel, has no source, and shares a name with Hemorath's relic

- **Claim:** The RIB gourd tier (5,000 mL, passive refill) is registered, rendered and given a capability, but nothing grants it. Its English name "Rib of Hemorath" collides with `saint_relic_hemorath`, "Hemorath's Rib".
- **Evidence:** `ItemInit.java:708`; `common/capability/HemoCapabilityRegistrar.java:107`; `EnumBloodGourdTiers.RIB`; lang `item.hemomancy.hemorath_rib` and `item.hemomancy.saint_relic_hemorath`; `docs/HEMOMANCY_REFERENCE.md:1194,3142`.
- **Confidence:** Verified.
- **Recommendation:** expand: make it Hemorath's tomb or forced-fight reward (the trial loot path is `SaintTrialEntity.fragmentOf`, `common/entity/boss/saint/SaintTrialEntity.java:451`), and rename one of the two items. Effort S. User call: yes (D-1/D-2 Saint rework may own this).

### MT-7 (Medium) The Silthmere Glaive has no source

- **Claim:** A lore-sanctioned Unstained weapon with a full renderer and combat events has no recipe, loot, trade or gift.
- **Evidence:** `ItemInit.java:1105`; `common/item/unstained/tool/SilthmereGlaiveEvents.java`; only a Liber unlock (`common/capability/player/shared/knowledge/discovery/LiberDiscoveryEvents.java:90`) and a Guardian inquiry (`dialogue_inquiry/guardian/hemomancy/silthmere_glaive.json`); `docs/LORE_REFERENCE.md:520`.
- **Confidence:** Verified.
- **Recommendation:** expand: a Guardian trade card or a crafting recipe on `pale_silver_ingot` and `tears_of_silthmere`. Effort S. User call: yes.

### MT-8 (Low) `dendrite_growth` and `dura_membrane` are documented as Cortical Drift palette but placed nowhere

- **Claim:** Both blocks have zero Java, worldgen or NBT references; the reference and the wiki describe them as placed.
- **Evidence:** `BlockInit.java:323,326`; `docs/HEMOMANCY_REFERENCE.md` Cortical Drift palette table ("stretched over cavity mouths", "surface decoration"); `wiki/Cortical-Drift.md:27-28`; `CorticalArchipelagoFeature` and `VagrantMindPiece` use the other eight palette blocks only.
- **Confidence:** Verified.
- **Recommendation:** expand (scatter `dendrite_growth` on island tops in `CorticalArchipelagoFeature`, stretch `dura_membrane` over the Vagrant Mind's cavity mouths) or cull and fix both docs. Effort S either way. User call: yes.

### MT-9 (Low) `gilded_venous_stone` has no source

- **Claim:** Filed under Building, it is never crafted, generated or placed; code only reads it as an alternate Alembic leak base.
- **Evidence:** `BlockInit.java:329`; `common/tile/harbinger/crafting/GhastlyAlembicBlockEntity.java:983`; `CreativeTabGroup.java:109`.
- **Confidence:** Verified.
- **Recommendation:** cull (block, item, blockstate, models, texture, loot table, the Alembic check, the creative entry) or add a recipe. Effort S. User call: yes (keep only if a gilt accent fits the blood palette rule).

### MT-10 (Low) `suspended_vivianite` has no source and no use

- **Claim:** A display block with a block entity and renderer, never placed or crafted, read by nothing.
- **Evidence:** `BlockInit.java:1254`; only `BlockEntityInit.java:224`; atlas text "grown from bog-body blood mineral deposits" with no such growth.
- **Confidence:** Verified.
- **Recommendation:** combine with MT-2/MT-3's fix (same loot table and a recipe from `vivianite_cluster`) or cull. See MT-16 for the class merge. Effort S. User call: yes.

### MT-11 (Medium) 27 retired or legacy memory items are still registered

- **Claim:** 20 memory items for retired manipulations and 7 pre-graft living-weapon memories are registered with models, lang and 7 inquiry files, hidden only by `ManipulationRetirementRules` and `CreativeTabInit.isLegacyLivingWeaponMemory`. The mod is not live, so there are no saves to protect.
- **Evidence:** `common/capability/player/harbinger/manip/ManipulationRetirementRules.java:28-48`; `ItemInit.java:464-471,486-495`; `CreativeTabInit.java:183-191`; `dialogue_inquiry/*/hemomancy/memory_living_*.json` (7); `advancement/hemomancy/inherited_memory.json` (lists the 7 legacy ids as alternatives).
- **Confidence:** Verified.
- **Recommendation:** cull: the 27 `ItemInit` entries, their `models/item/*.json` (27) and textures, lang keys, the 7 inquiry files, the 7 entries in `inherited_memory.json`, the item-id set in `ManipulationRetirementRules` and `isLegacyLivingWeaponMemory`. The retired manipulations themselves are outside this audit. Effort M. User call: confirm.

### MT-12 (Low) Two hidden legacy items: `tome_of_the_unstained` and `synaptic_step`

- **Claim:** `tome_of_the_unstained` is a plain `Item` with no source and no use; `synaptic_step` has no source and survives only as a one-ingredient salvage recipe to `naeglerophaeon_ganglion`.
- **Evidence:** `ItemInit.java:1072`, `ItemInit.java:94`; `CreativeTabInit.java:176,181`; `recipe/synaptic_step_salvage.json`.
- **Confidence:** Verified.
- **Recommendation:** cull both: `ItemInit` entries, `models/item/tome_of_the_unstained.json`, `textures/item/tome_of_the_unstained.png`, `textures/item/old/tome_of_the_unstained.png`, `models/item/synaptic_step.json`, `textures/item/synaptic_step.png`, `recipe/synaptic_step_salvage.json`, lang keys, the two `CreativeTabInit` exclusions and the pin in `src/test/.../HemomancyCreativeTabsSourceTest.java:117`. Effort S. User call: no.

### MT-13 (Low) `neutralizing_gasket` is crafted and then used by nothing

- **Claim:** An Unstained component (hemolytic plating, solution and hematic scrap) that no recipe, rite or code consumes.
- **Evidence:** `ItemInit.java:1044`; `recipe/neutralizing_gasket.json`; only the atlas (`client/screen/skilltree/shared/MaterialsData.java:1647`) and a Zealot inquiry.
- **Confidence:** Verified.
- **Recommendation:** expand: make it an ingredient of `stillwater_condenser` or `pallid_retort` (the "anti-blood component" its description promises), or cull. Effort S. User call: yes.

### MT-14 (Low) `frozen_cruor` is filed as a building block but its item cannot be obtained

- **Claim:** The block is temporary ice placed by Glacial casts and Drudge actions (`TemporaryIceManager`) and has no loot table; its item sits in the Building group.
- **Evidence:** `BlockInit.java:249`; `common/manipulation/DrudgeActions.java:435`, `EntityCasts.java:398`; `CreativeTabGroup` BUILDING list.
- **Confidence:** Verified.
- **Recommendation:** keep the block; move it to `ITEMLESS_BLOCKS` (or hidden) so the tab stops advertising it. Effort S. User call: no.

### 2c. Structure-only versus nothing at all

- **Decorative or functional, placed in structures or by code only (working as designed):** 34 unbreakable set pieces
  (Court fixtures and tissues, mausoleum niches, sconces, pyres, devotees, foci, the Hydra crown) and 16 world-only
  blocks with no drop (altars, inscriptions, the four Circus records, `bog_body`, `crimson_flames`, `fungal_podium`,
  `consecrated_bloodwell`, plus `engram_block`, `hyphae`, `frozen_cruor` above). See "Not a problem".
- **Nothing at all:** the 25 no-source ids: 6 WIP tools and dev items (`chamber_of_will_teleporter`, `debug_showcase`,
  `drudge_electrode`, `drudge_submission_device`, `structure_scanner`, `structure_spawner`), 8 WIP blocks (`blood_pylon`,
  `blood_trial_altar`, `offering_gate`, `humane_idol`, `serpentine_idol`, `morphling_cradle`, `non_euclidean_hallway`,
  `sanguine_omen`) and the 11 in MT-2, MT-3, MT-6 to MT-10, MT-12 and MT-15.

---

## 3. Redundant families

### MT-15 (Low) Dictation table exists three times

- **Claim:** `hemomancy:dictation_table` is a legacy copy whose only recipe turns it into `hutoslib:dictation_table`; `harbinger_escritoire` is the Harbinger reskin of the HutosLib block.
- **Evidence:** `BlockInit.java:1057`; `recipe/dictation_table.json`; `common/block/harbinger/decoration/HarbingerEscritoireBlock.java:23` (extends HutosLib `DictationTableBlock`); `BlockEntityInit.java:267-272`; consolidation ledger F-22 (`docs/architecture/CONSOLIDATION_PROGRESS.md:92`).
- **Confidence:** Verified.
- **Recommendation:** combine into `harbinger_escritoire` (and the HutosLib table). Cull `hemomancy:dictation_table`: block, item, BE type, `blockstates/dictation_table.json`, `models/block|item/dictation_table.json`, `textures/block|entity/model_dictation_table.png`, the two `.bbmodel` files, `loot_table/blocks/dictation_table.json`, `recipe/dictation_table.json`, and the F-22 no-op in `CreativeTabInit.java:172-175`. Effort S. User call: resolves F-22.

### MT-16 (Low) The three suspended crystal blocks are one block three times

- **Claim:** `SuspendedBloodCrystalBlock`, `SuspendedVivianiteBlock` and `SuspendedCleansedBloodCrystalBlock` share 52-53 of 55 lines; none is obtainable today.
- **Evidence:** `docs/architecture/evidence/06-content-oop.md` P11; `BlockInit.java:1254-1264`.
- **Confidence:** Verified (from the evidence file and the registrations).
- **Recommendation:** combine the classes into one `SuspendedCrystalBlock` and keep the ids the rites need (`suspended_blood_crystal`, `suspended_cleansed_blood_crystal`); cull `suspended_vivianite` unless MT-10 gives it a job. Effort M. User call: with MT-10.

### MT-17 (Low) Confusable names across unrelated items

- **Claim:** Four name clashes that will read as duplicates in JEI and the atlas.
- **Evidence:** "Rib of Hemorath" (`hemorath_rib`) vs "Hemorath's Rib" (`saint_relic_hemorath`); item `pale_silver_bell` (rite output, judgment relic) vs block `pale_silver_bells` (placed only by `common/worldgen/feature/SilverBellsTowerFeature.java:78`, used by nothing); `erythrocytic_mycelium` vs `mycelium_erythrocytic_dirt` (same class and identical properties, `BlockInit.java:964-970`, different tops: dark red vs pale grey); "effigy" names six unrelated things (`mason_effigy`, `vacant_effigy`, the Working craft "vascular_effigy" that builds `sanguine_vigil`, three WIP husk effigies, `blood_thrall_effigy`).
- **Confidence:** Verified.
- **Recommendation:** rename lang (e.g. `mycelium_erythrocytic_dirt` to "Pale Mycelial Dirt"); make `pale_silver_bells` drop `pale_silver_bell`, or rename it; see MT-6 for the rib. Effort S. User call: yes (names).

### MT-18 (Low) Sixteen tinctures for eight memories

- **Claim:** Each muscle-memory tincture exists as a flask and a jug that differ only in dose count and returned container.
- **Evidence:** `ItemInit.java:178-203` (`tincture` / `tinctureJug` helpers with `FLASK_DOSES` / `JUG_DOSES`); 16 distillation recipes; filed together in `CreativeTabGroup.ALCHEMY`.
- **Confidence:** Verified.
- **Recommendation:** keep unless the item count matters; the merge would be one item per memory with a doses data component and two distillation recipes. Effort M. User call: yes.

### MT-19 (Low) Venous stone: six members never placed, cracked bricks barely differ, six duplicate textures

- **Claim:** The 17-block family is fully used for its base, polished and brick blocks, but `venous_stone_slab/stairs/wall` and `polished_venous_stone_slab/stairs/wall` appear in no structure; `cracked_polished_venous_stone_bricks` differs from the bricks by 4.3/255 mean pixel difference (cracks do not read); six PNGs are byte-for-byte pixel copies of the base textures.
- **Evidence:** NBT counts in the Appendix (bricks 21,136 blocks in 46 templates, polished 7,373 in 50); duplicate PNGs `textures/block/polished_venous_stone_slab.png`, `_stairs.png` (= `polished_venous_stone.png`), `polished_venous_stone_brick.png`, `_brick_slab.png`, `_brick_stairs.png` (= `polished_venous_stone_bricks.png`), `cracked_polished_venous_stone_brick.png` (= `..._bricks.png`), each still referenced by a model.
- **Confidence:** Verified.
- **Recommendation:** keep the family; repoint the slab and stair models at the base textures and delete the six copies; redraw the cracked texture. Effort S. User call: no.

### MT-20 (Low) Storage blocks are one-way sinks, two have no use

- **Claim:** `chitinite_block` (4 husks), `sclerite_block` (4 sclerites) and `mnemonic_ambergris_block` have no reverse recipe; `chitinite_block` and `organic_prism` are used by nothing and placed nowhere.
- **Evidence:** `recipe/chitinite_block.json`, `sclerite_block.json`, `mnemonic_ambergris_block.json`, `organic_prism.json`; `BlockInit.java:466,470,474`.
- **Confidence:** Verified.
- **Recommendation:** expand: add the three reverse recipes; cull `organic_prism` (or give it a job). Effort S. User call: `organic_prism` only.

---

## 4. Half-sets

### MT-21 (Medium) Blood wood has no slab, stairs, fence or door, and no wood tags

- **Claim:** The only Harbinger wood is log, planks and leaves; its planks fill 1,709 blocks in 11 structures, but players can build only full blocks with it, and without `minecraft:planks` / `minecraft:logs` membership it cannot make sticks, a crafting table or a chest.
- **Evidence:** `BlockInit.java:617-625`; `data/minecraft/tags/item/` has no `planks`, `logs` or `leaves` file, and the only blood wood block in a vanilla tag is `blood_wood_leaves` in `data/minecraft/tags/block/leaves.json`; NBT counts in the Appendix.
- **Confidence:** Verified.
- **Recommendation:** expand: slab and stairs at least (fence, door, trapdoor optional), and the planks and logs tags. M-2 covers charred planks only and keeps lacquer out; blood wood is a separate call. Effort M. User call: yes.

### MT-22 (Low) `bog_iron` looks like an ore and does nothing

- **Claim:** Generated and placed (22 blocks in one template), drops itself, no smelting or recipe use.
- **Evidence:** `BlockInit.java:291`; `loot_table/blocks/bog_iron.json`; `worldgen/configured_feature/bog_iron_clump.json`.
- **Confidence:** Verified.
- **Recommendation:** expand: a smelting or blasting recipe to `minecraft:iron_nugget` or `hemomancy:hematic_iron_scrap`; or cull. Effort S. User call: yes.

### MT-23 (Low) Small gaps in otherwise used sets

- **Claim:** `hemolytic_plating_block` has no recipe from `hemolytic_plating` (only `BrokenChurchStructure.java:92` places it); the venous family has a stonecutter recipe only for `polished_venous_stone_wall`, while bone, rimed deepslate and hematic plating have full stonecutter coverage; the charred wood set is thin (the log appears only in the Mason's Respite, 9 blocks; the bookshelf and slab only in Putriciel's litany hall, 10 and 5 blocks; planks and stairs are never placed).
- **Evidence:** `recipe/` stonecutting list (12 recipes); NBT counts in the Appendix; M-2 in `docs/OPEN_QUESTIONS.md`.
- **Confidence:** Verified.
- **Recommendation:** expand: add plating-to-block and block-to-plating; add stonecutting for the venous slab, stair and wall members; keep charred as is pending M-2. Effort S. User call: no.

---

## 5. Stations and machines

Recipe and operation counts per station (data recipes counted from `data/hemomancy/recipe/`; code operations named where
the station has no recipe type):

| Station | What it serves | Count | Upgrade kits |
| --- | --- | --- | --- |
| Cardinal Focus | `cardinal_rite_recipe` | 93 (56 Harbinger rites, 17 Unstained rites, 20 Working crafts) | |
| Somatic Loom | `memory_weaving` | 103 | 2 |
| Hematic Armature | `armature_upgrade` | 48 | 2 |
| Ghastly Alembic | distillation (non-pallid) + `advanced_brewing` | 34 + 3 | 2 |
| Scar Station | `scar_recipe` (+24 shapeless scar templates at a table) | 25 | |
| Blood Structure (hematic iron block) | `blood_structure_recipe` | 11 | |
| Pallid Retort | distillation (`"pallid"`) | 9 | |
| Rendering bile pool | `rendering` | 9 | |
| Morphling Incubator | `incubator` | 8 | |
| Mycelial Crucible | `fungal_scar_cultivation` | 8 | |
| Mycelial Lantern | `enzyme_fruiting` | 8 | |
| White humor (Pale Humor Flask) | `white_humor_purification` | 6 | |
| Crafting-table specials | `blood_gourd_fill` / `scale_grip_binding` | 4 / 1 | |
| Morphic nectar pool | `morphic_nectar` | 2 | |
| Blood Projection on a block | `blood_infusion` | 2 | |
| Enzymatic Scriptorium | inscribe, denature (42 `enchantment_affinity` entries) | code | 2 |
| Resonant Forge | grind, apply | code | 2 |
| Vial Centrifuge | spin (104 `blood_profiles`) | code | 2 |
| Sanguine Condenser | condense | code | 2 |
| Clairaudiograph | carve calls (158 `clairaudiograph_sounds`) | data | |
| Code-only machines | Specimen Jar, Phlebotomist's Cabinet and Field Case, Scrying Podium, Mnemonic Reliquary, Scarlet Vanity, Dendritic Distributor, Mason and Vacant Effigy, Harbinger Escritoire, Hematic Suture Node, Humoral Barometer, Mnemonic Candle, Ossuary Clock, Antecedent Vessel, Warp Chair, Covenant Throne, Sanguine Monolith, Fungal Implantation Pylon, Earthen Vein, Hematic Stake, Heart Node, Consecrated Bloodwell; Unstained Altar of Cleansing, Unstained Podium, Stillwater Condenser, Verdigris Lattice | | |
| WIP machines | Gourdvine Tap, Crucible of Nether Ichor, Voidtouched Vessel, Witness Organ, Visceral Mirror, Semi-Sentient Construct, Morphling Cradle, Sanguine Vigil, Blood Pylon, Blood Trial Altar, Offering Gate, Humane and Serpentine Idol, Non-Euclidean Hallway | | |

All 14 upgrade kits have a shaped recipe and a teacher gift.

### MT-24 (Medium) Copper is a recipe ingredient of three Harbinger items

- **Claim:** The blood-side Hematic Microscope and Humoral Barometer (and the WIP Gourdvine Tap) take `minecraft:copper_ingot`, against the rule that copper is Unstained/antibiotic.
- **Evidence:** `recipe/hematic_microscope.json`, `recipe/humoral_barometer.json` (four copper ingots), `recipe/gourdvine_tap.json`; every other copper recipe is Unstained (`pallid_retort`, `stillwater_condenser`, `verdigris_lattice`, Unstained armour, Lethean chalice).
- **Confidence:** Verified (recipes); the models were not inspected.
- **Recommendation:** swap copper for `hematic_iron_scrap` or `minecraft:iron_nugget`, and check the three models for copper accents. Effort S. User call: whether the rule covers ingredients as well as looks.

### MT-25 (Low) Thin stations and two machines that are plain blocks

- **Claim:** The morphic nectar pool serves two recipes (one is D-4's tonic, the other a fifth route to `hematic_iron_powder`), blood infusion serves two; the WIP `crucible_of_nether_ichor` and `voidtouched_vessel` are plain `Block` with live recipes that eat progression reagents and do nothing.
- **Evidence:** `recipe/morphic_nectar/`, `recipe/blood_infusion/`; `BlockInit.java:1301,1307`; `recipe/crucible_of_nether_ichor.json` (`#hemomancy:progression/nether_ichor_reagents`), `recipe/voidtouched_vessel.json` (`#hemomancy:progression/void_vessels`).
- **Confidence:** Verified.
- **Recommendation:** keep the two thin stations (D-4 owns the tonic); cull the two plain WIP blocks with their recipes, loot tables, models, textures and the two tags if nothing else reads them. Effort S. User call: the two WIP blocks.

---

## 6. Tags

### MT-26 (Medium) The Charm of Vascularium searches a tag that does not exist

- **Claim:** The charm's locate uses `hemomancy:structures/blood_temple`, which has no tag file, so it always answers "Could Not Find A Temple" while its tooltip says it "leads you to a place of solace".
- **Evidence:** `common/item/harbinger/bloodline/VasculariumCharmItem.java:87-89`; `data/hemomancy/tags/worldgen/structure/` (13 files, no `structures/blood_temple.json`); lang `tooltip.hemomancy.vascularium_charm.leads_you_to_a_place_of`.
- **Confidence:** Verified.
- **Recommendation:** expand: add `tags/worldgen/structure/blood_temple.json` listing `hemomancy:blood_temple` and point the code at it (the other locators use `*_targets` tags). Effort S. User call: no.

### MT-27 (Medium) Mod gear is in none of the vanilla enchantable tags

- **Claim:** No mod sword, tool or armour piece is in `minecraft:swords`, `minecraft:head_armor` and the rest, and no item overrides `supportsEnchantment` / `isPrimaryItemFor`, so the vanilla table, anvil books, the Scriptorium (`input.isPrimaryItemFor`) and the Forge (`target.supportsEnchantment`) should all refuse vanilla enchantments on mod gear.
- **Evidence:** no `data/minecraft/tags/item/enchantable/` or armour/weapon tag files; `common/menu/tile/crafting/EnzymaticScriptoriumMenu.java:188`; `common/enchanting/ResonantForgeTransfer.java:76`; tool tiers still set enchantment values (`common/item/harbinger/tool/EnumModToolTiers.java`).
- **Confidence:** Suspected (code path verified, not checked in game).
- **Recommendation:** expand: add the mod armour and weapons to the vanilla slot and weapon tags, or confirm that mod gear is meant to stay unenchantable and drop the enchantment values. Effort S. User call: yes.

### MT-28 (Low) Twelve `progression/*` tags are read by nothing

- **Claim:** Leftovers of an older progression layer.
- **Evidence:** block tags `progression/deep_ritual_stone`, `dragon_proof_blocks`, `fane_core_blocks`, `qliphoth_soil`, `ritual_stone`, `void_ritual_stone`; item tags `progression/dragon_proofs`, `early_blood_reagents`, `end_city_relics`, `judgment_relics`, `lethean_reagents`, `monolith_fragments`; none is referenced by a recipe, loot table, advancement or Java string. The other `progression/*` tags are read by recipes.
- **Confidence:** Verified.
- **Recommendation:** cull the 12 files. Effort S. User call: no.

### MT-29 (Low) Biome spawnlists for mobs that do not exist, plus a typo twin

- **Claim:** Seven biome tags are read by no biome modifier or code.
- **Evidence:** `tags/worldgen/biome/abyssal_siphon_spawnlist.json`, `cruor_fiend_spawnlist.json`, `frozen_clot_spawnlist.json`, `synapse_hound_spawnlist.json`, `void_drinker_spawnlist.json` (the five unregistered mobs of ledger M-2, `docs/architecture/CONSOLIDATION_PROGRESS.md:75`), `myelin_borer_spawnlist.json` (the Borer spawns through the Drift biome, not a modifier) and `dessicant_spawnlist.json` (misspelt twin of the live `desiccant_spawnlist.json`).
- **Confidence:** Verified.
- **Recommendation:** cull with ledger M-2; cull `dessicant_spawnlist.json` now. Effort S. User call: M-2 only.

### MT-30 (Low) No interop tags

- **Claim:** Nothing is in `c:ores`, `c:ingots`, `c:storage_blocks`, `c:glass_blocks` or `minecraft:doors`/`trapdoors`; the gourd foods are missing from `c:foods`.
- **Evidence:** `data/c/tags/item/` holds only `foods/`; `data/minecraft/tags/` has no door or trapdoor files although `hematic_iron_door` and `hematic_iron_trapdoor` exist.
- **Confidence:** Verified.
- **Recommendation:** expand only if modpack interop matters; M-1 (plating) and M-2 (charred planks) stay as decided. Effort S. User call: yes.

Empty `rite_bound/*` tags are D-10; not repeated here.

---

## 7. Food, potions, brewing, effects

### MT-31 (Medium) 18 of 19 potion types can never be brewed

- **Claim:** Only `potion_of_mnemonic_whispers` has a brewing mix; the other 18 registered potions have no mix, loot or trade.
- **Evidence:** `common/init/EffectInit.java:333-343` (nine `addMix` calls: eight to vanilla potions, one to `potion_of_mnemonic_whispers`); no other `addMix` in the codebase; the 18: `potion_of_blood_binding`, `blood_loss`, `blood_rush`, `hemolysis`, `monolithic_dislocation` and 13 old Morphling-effect potions (`sanguine_fertility` through `burrowers_instinct`).
- **Confidence:** Verified.
- **Recommendation:** cull the 18 potion registrations, or add mixes for the few that are meant to be brewed. Effort S. User call: yes.

### MT-32 (Low) Five effects are applied by nothing

- **Claim:** `chitinous_bulwark`, `verminous_aura`, `hemorrhagic_venom`, `spined_barricade` and `chummed_waters` have no reference outside `EffectInit`; five more (`serpentine_guile`, `luminous_dissipation`, `venomous_resilience`, `echoic_perception`, `burrowers_instinct`) are reachable only through their unbrewable potions.
- **Evidence:** reference scan of `src/main/java` and `src/main/resources/data`.
- **Confidence:** Verified.
- **Recommendation:** cull with MT-31 (effects, effect classes, lang, icons). Effort S. User call: with MT-31.

Food and the nine live brewing mixes have sourced ingredients; see "Not a problem".

---

## 8. Spawn eggs and WIP

### MT-33 (Medium) Ten WIP-listed items are already part of the live game

- **Claim:** These sit in the WIP tab but are crafted, sold, demanded by finished recipes or shown as advancement icons.
- **Evidence:**
  - `memory_thread` (`ItemInit.java:325`): crafted, sold by the Mnemonist (`npc_trade/mnemonist/faded_memories.json`, `living_specimen.json`), wanted by the Vicar (`npc_trade/vicar/thread_blessing.json`), offering of the D5 Mnemonist Puppet Ordeal.
  - `unsigned_ancestral_ledger` (`ItemInit.java:112`): output of `cardinal_rite/bloodline_founding.json` and `bloodline_recall.json` (D5), icon of `advancement/hemomancy/blood_is_bound.json` and `degree_6_sanctified.json`.
  - `vascular_poultice` (`ItemInit.java:300`): crafted, sold (`npc_trade/vicar/chapel_shards.json`), offering of the `working/vascular_effigy.json` craft, icon of `vein_mason_d5_ready.json`.
  - `vitality_chalice` (`ItemInit.java:294`): Loom catalyst of `memory_weaving/memory_exsanguinate.json`, icon of `eternal_covenant_sealed.json`.
  - `vascular_status_gauge` (`ItemInit.java:414`): Loom catalyst of `memory_weaving/memory_hemolymphal_pulse.json`.
  - `sanguine_blob` (`ItemInit.java:175`): given by a rite (`common/rite/harbinger/CardinalRiteCompletionEffects.java:232`), ingredient of the finished `hearty_compass`, icon of `degree_2_votary.json`.
  - `hematic_suture_needle` (`ItemInit.java:208`): crafted, sold (`npc_trade/artificer/suture_work.json`), and the only tool that links the finished Hematic Suture Node (`common/item/harbinger/tool/HematicSutureNeedleItem.java:71,80`).
  - `lethean_brew` and `draught_of_still_mercy` (`ItemInit.java:1060,1054`): distillation outputs; the brew is sold by the Guardian (`npc_trade/guardian/iron.json`) and feeds another distillation.
  - `blood_bolt` (`ItemInit.java:668`): Living Crossbow ammunition, in `minecraft:arrows`.
- **Confidence:** Verified.
- **Recommendation:** promote these ten to their sections (`CreativeTabInit.java:59-102`), or pull the live references for any that really are unfinished. Effort S. User call: per item.

### MT-34 (Medium) Three Working crafts build WIP machines

- **Claim:** 3 of the 20 live Working crafts output WIP blocks, and the Visceral Mirror then produces five WIP organ echoes that nothing uses.
- **Evidence:** `recipe/cardinal_rite/working/semi_sentient_construct.json`, `visceral_mirror.json`, `vascular_effigy.json` (result `sanguine_vigil`); `common/tile/harbinger/functional/VisceralMirrorBlockEntity.java:291-295` (`echo_of_*`).
- **Confidence:** Verified.
- **Recommendation:** decide each machine: finish and promote it, or remove its craft from the roster (update `WorkingCraftRosterDataTest`) until it is finished. Effort S to remove, L to finish. User call: yes.

### MT-35 (Low) The WIP Peacock Spider spawns naturally

- **Claim:** Its egg is on the WIP list but a live biome modifier spawns it in four overworld biomes.
- **Evidence:** `CreativeTabInit.java` WIP list (`spawn_egg_peacock_spider`); `neoforge/biome_modifier/add_peacock_spider.json`; `tags/worldgen/biome/peacock_spider_spawnlist.json` (forest, birch forest, flower forest, meadow).
- **Confidence:** Verified.
- **Recommendation:** promote the egg, or remove the biome modifier until the mob is finished. Effort S. User call: yes.

### MT-36 (Low) WIP craftables with no function are craftable in survival

- **Claim:** WIP filing only hides creative entries; the recipes stay live in crafting and JEI.
- **Evidence:** live recipes for `zombie_husk_effigy`, `desert_husk_effigy`, `spider_husk_effigy`, `void_eye_organ`, `vein_spider`, `gourdvine_tap`, `witness_organ`, `crucible_of_nether_ichor`, `voidtouched_vessel` (`recipe/<id>.json`).
- **Confidence:** Verified.
- **Recommendation:** keep the WIP list as is, but move these recipes out of `data/` (or behind a condition) until the items work. Effort S. User call: yes.

### MT-37 (Low) Finished tabs list ten unobtainable entries; two drops are filed twice

- **Claim:** The main tab advertises ids no survival player can get, and `ganglion_cluster` and `naeglerophaeon_ganglion` are listed in both `CreativeTabGroup.DROPS` and `CreativeTabSection.END` (the section wins, so the group entries are dead).
- **Evidence:** unobtainable in finished tabs: `suspended_blood_crystal`, `suspended_vivianite` (Natural), `suspended_cleansed_blood_crystal`, `silthmere_glaive` (Unstained), `gilded_venous_stone`, `frozen_cruor` (Building), `dendrite_growth`, `dura_membrane` (End), `hemorath_rib` (Saints), plus `dictation_table` (Stations; F-22). Double filing: `CreativeTabGroup.java:94-97` and `CreativeTabSection.java:112-114`. The ledger's "42 items, 17 blocks" WIP count (`CONSOLIDATION_PROGRESS.md:743`) is now 28 items and 15 blocks.
- **Confidence:** Verified.
- **Recommendation:** resolved by MT-2 to MT-10, MT-14 and MT-15; drop the two dead `DROPS` entries. Effort S. User call: no.

The 107 spawn eggs (89 named plus 18 Court roles) are creative-only by nature; apart from MT-35, each egg's mob has a
natural spawn, a structure or an encounter.

---

## Not a problem

- **No dangling item or block ids.** Every `hemomancy:` item or block id in recipes, loot tables, trades, advancements and worldgen resolves to a registered entry, and every `loot_table/blocks/*.json` names a registered block.
- **UTF-8 BOMs.** 59 recipe JSONs start with a BOM; Gson's reader skips it, so they load.
- **Spores are not a redundant enzyme line.** The Mycelial Lantern checks the culture without consuming it (`MycelialLanternBlockEntity` culture slot), so spores are reusable enzyme cultures, not a 1:1 loop.
- **Saint item ladders.** Fragments (forced-fight drops via `SaintTrialEntity.fragmentOf`) craft 3:1 into residuum; relics, recollections (from `RecollectionCystBlock`) and canon memories each have distinct roles. C-6 (recollection trading, Loom gate) stays open as filed.
- **Masked Blood Lust helms** are lineage-gated Armature upgrades (`recipe/armature_upgrade/blood_lust_*_mask.json`), not duplicates.
- **Blood vessel tiers.** `blood_rock` (250 mL, venous-stone loot modifier), `bloody_flask` (2,500), `bloody_jug` (5,000) and the gourds are a deliberate ladder.
- **Hidden active ash items** are obtainable (the active trails drop them) and feed the two cylinder recipes.
- **Blood wood regrows** from dead bushes under Blood Projection (`common/block/harbinger/plant/BloodwoodGrowthHandler.java`), with persistent leaves.
- **Set pieces.** The 34 unbreakable Court and mausoleum blocks have creative-only items "placeable for building and testing" (`CreativeTabSection.java`), by design; the 16 world-only blocks are functional in place.
- **Family looks.** The eight cube-textured Cortical Drift blocks differ from each other by at least 59/255 mean pixel difference; bone, rimed deepslate, charred, lacquer and healed quartz blocks are visually distinct. No look-based merge is needed there.
- **Upgrade kits.** All 14 kits are craftable and teacher-gifted.
- **Food.** Every food item has a source; herring and venison are in `c:foods`. The nine live brewing mixes and the three `advanced_brewing` recipes use sourced ingredients.
- **Already decided or tracked elsewhere:** M-1 (plating stays out of conductor tags), M-2 (charred planks stay out of wood tags), M-6 (legacy mausoleum), D-4 (morphic tonic), D-10 (empty rite-bound tags), F-22 (dictation table), ledger M-2 (dead mobs).

---

## Appendix: full inventory

Columns: **kind** (`item`, `block+item`, `block` = no item); **tab** (`H:` Hematic group, `S:` section, `WIP`, `hidden`,
`hidden(retired)`, `hidden(legacy)`, `none`); **obtained**: recipe outputs by station (`craft`, `rite`, `loom`,
`alembic`, `armature`, `scar`, `cut`, `smelt`, `smoke`, `bstruct`, `render`, `incub`, `crucible`, `lantern`, `humor`,
`gourdfill`, `brew`, `nectar`, `infuse`, `grip`; `xN` = count), `chest`/`mob`/`lootmod`/`trade`/`adv` loot sources,
`drop:` another block's loot, `wg` data worldgen, `nbtN` placed in N reachable templates, `nbt-deadN` only in
unreachable ones, `nbt-item` inside block-entity NBT, `code` granted or placed by Java (heuristic, hand-checked for every
flagged id), `NOLOOT` breakable block without a loot table, `unbreakable`; **used**: recipe inputs by station, `tagN`
mod-relevant tag memberships, `want` trade demand, `adv`/`book`/`inq` advancement, Liber, dialogue inquiry, `decorN`
blocks placed in reachable templates, `javaN` gameplay code references, `class` custom item or block class, `food`;
**flag**: `NO-SOURCE`, `NO-USE`, `world-only`, `set-piece`, `retired`, `WIP`, `egg`.

| id | kind | tab | obtained | used | flag |
| --- | --- | --- | --- | --- | --- |
| `abocipher_emitter` | block | none | nbt2 code | java2 class |  |
| `absolution_dagger` | item | S:UNSTAINED | code | inq java5 class |  |
| `active_befouling_ash` | item | hidden | code | craftx2 java3 |  |
| `active_smouldering_ash` | item | hidden | code | java3 |  |
| `aculeate_vitriol` | item | H:ALCHEMY | craft code | armaturex4 inq java1 |  |
| `adipocere` | item | H:MATERIALS | alembic chest mob code | craft+rite java2 |  |
| `adipocere_crust` | block+item | S:SAINTS | craft nbt1 code | decor1 java1 class |  |
| `ahaematic_colloid` | item | H:BLOOD_TOOLS | code | java4 |  |
| `altar_of_cleansing` | block+item | S:UNSTAINED | nbt2 NOLOOT | inq decor2 java1 class | world-only |
| `ambergris_cylinder` | item | H:BLOOD_TOOLS | craft nbt-item code | inq java7 |  |
| `annettas_absolution_dagger` | item | S:UNSTAINED | code | inq java3 class |  |
| `annettas_sanguis_lancea` | item | H:ARSENAL | code | java2 class |  |
| `antecedent_vessel` | block+item | H:STATIONS | nbt14 NOLOOT | decor14 java1 | world-only |
| `antiphonomyces_resonans` | item | H:SCARS | crucible | class |  |
| `assumed_limb_fitting` | item | H:ARSENAL | code | java3 class |  |
| `attached_gourd_stem` | block | none | code | java1 class |  |
| `axonal_slate` | block+item | S:END | code | craft+rite java3 |  |
| `barbed_blade` | item | H:ARSENAL | craft | tag1 inq java1 class |  |
| `barbed_boots` | item | H:ARMOR | armature | armature tag1 java3 class |  |
| `barbed_chestplate` | item | H:ARMOR | armature | armature tag1 java3 class |  |
| `barbed_fitting` | item | H:ARSENAL | code | java3 class |  |
| `barbed_helm` | item | H:ARMOR | armature | armature tag1 java4 class |  |
| `barbed_leggings` | item | H:ARMOR | armature | armature tag1 java3 class |  |
| `barbed_shield` | item | H:ARSENAL | craft | class |  |
| `befouling_ash_trail` | block+item | H:NATURAL | smelt chest nbt1 code | bstructx2+craftx2+ritex3 adv decor12 java14 class |  |
| `bell_sleeper` | block+item | S:BRINED_COURT | nbt1 unbreakable | decor4 class | set-piece |
| `blank_mask` | block+item | S:CIRCUS | craft nbt2 | decor10 class |  |
| `bleeding_bulb` | item | H:MATERIALS | alembicx3+craft chest mob trade drop:bleeding_heart code | craftx14+crucible+loomx5 tag1 java4 |  |
| `bleeding_heart` | block+item | H:NATURAL | drop:potted_bleeding_heart wg | craftx2 tag2 java3 class |  |
| `blood_absorption` | item | H:BLOOD_TOOLS | code | java15 class |  |
| `blood_basin` | block+item | S:SAINTS | nbt1 NOLOOT | inq decor4 java1 class | world-only |
| `blood_bolt` | item | WIP | code | tag2 java9 class | WIP |
| `blood_chum` | item | S:BRINED_COURT | craft | rite java1 class |  |
| `blood_crystal` | block+item | H:NATURAL | mob nbt-dead2 code | bstruct+craft+ritex2 tag2 lootcond java6 class |  |
| `blood_crystal_shard` | item | H:MATERIALS | chest drop:blood_crystal,blood_crystal code | craftx9+humor+loom+ritex3 tag1 want inq java4 |  |
| `blood_echo_inscription` | block+item | H:STATIONS | nbt8 NOLOOT | decor12 java4 class | world-only |
| `blood_gourd_black` | item | H:BLOOD_TOOLS | gourdfill+rite | gourdfill+rite adv inq java1 class |  |
| `blood_gourd_red` | item | H:BLOOD_TOOLS | gourdfill+rite | gourdfill+rite adv inq java2 class |  |
| `blood_gourd_white` | item | H:BLOOD_TOOLS | gourdfill+rite | gourdfill+rite adv inq java2 class |  |
| `blood_gutter` | block+item | S:SAINTS | craft nbt22 | decor358 class |  |
| `blood_lust_boots` | item | H:ARMOR | armaturex3 | armaturex4 java2 class |  |
| `blood_lust_chest` | item | H:ARMOR | armaturex3 | armaturex4 java2 class |  |
| `blood_lust_helm` | item | H:ARMOR | armaturex3 | armaturex8 java2 class |  |
| `blood_lust_helm_grinning` | item | H:ARMOR | armature | armaturex4 java2 class |  |
| `blood_lust_helm_lodestone` | item | H:ARMOR | armature | armaturex4 java2 class |  |
| `blood_lust_helm_tengu` | item | H:ARMOR | armature | armaturex4 java2 class |  |
| `blood_lust_helm_velorum` | item | H:ARMOR | armature | armaturex4 java2 class |  |
| `blood_lust_legs` | item | H:ARMOR | armaturex3 | armaturex4 java2 class |  |
| `blood_projection` | item | H:BLOOD_TOOLS | code | java8 class |  |
| `blood_pylon` | block+item | WIP | nbt-dead2 NOLOOT | inq java1 class | NO-SOURCE |
| `blood_rock` | item | H:MATERIALS | chest lootmod | rite tag1 java1 class |  |
| `blood_scorched_scab` | block+item | S:NETHER | code | tag2 java2 |  |
| `blood_stained_stone` | item | H:MATERIALS | craft nbt1 code | alembic+craftx2+ritex3 book java3 class |  |
| `blood_thrall_effigy` | item | WIP | loom | class | WIP |
| `blood_trial_altar` | block+item | WIP | nbt-dead2 NOLOOT | inq java1 class | NO-SOURCE |
| `blood_wood_leaves` | block+item | H:BUILDING | code | tag1 java1 |  |
| `blood_wood_log` | block+item | H:BUILDING | nbt12 code | craft adv decor718 java1 |  |
| `blood_wood_planks` | block+item | H:BUILDING | craft adv nbt11 code | craftx2+rite adv decor1709 java3 |  |
| `bloody_flask` | item | H:BLOOD_TOOLS | chest mob code | alembic tag1 inq java4 class |  |
| `bloody_jug` | item | H:BLOOD_TOOLS | lootmod | craft+nectar tag1 java1 class |  |
| `bloody_vial` | item | H:BLOOD_TOOLS | alembicx2+craft chest code | craftx5+ritex2 tag1 want book inq java18 class |  |
| `bog_body` | block+item | H:NATURAL | wg nbt3 code NOLOOT | decor38 java1 class | world-only |
| `bog_iron` | block+item | H:NATURAL | wg nbt1 | decor22 java1 |  |
| `bone_brick_slab` | block+item | S:SAINTS | craft+cut nbt2 | craft tag2 lootcond decor42 |  |
| `bone_brick_stairs` | block+item | S:SAINTS | craft+cut nbt4 | tag2 decor115 |  |
| `bone_brick_wall` | block+item | S:SAINTS | craft+cut nbt4 | tag2 decor68 |  |
| `bone_bricks` | block+item | S:SAINTS | craft trade nbt19 | craftx3+cutx4+smelt want decor1140 |  |
| `bone_outgrowth` | block+item | S:SAINTS | nbt6 | decor30 class |  |
| `bone_worm_colony` | block+item | S:BRINED_COURT | nbt1 | incub lootcond decor9 java4 class |  |
| `book_of_observances` | item | S:UNSTAINED | code | book java3 class |  |
| `bound_mnemonic_remnant` | item | H:MEMORIES | code | rite java2 class |  |
| `brine_sealed_letter` | item | S:BRINED_COURT | code | inq java3 class |  |
| `brood_tumor` | block | none | code | java3 class |  |
| `calcified_blood_spine` | item | H:DROPS | mob | craftx3+rite tag1 java1 |  |
| `calcified_erythrocoral` | block+item | S:BRINED_COURT | trade nbt3 code | want decor12 java2 |  |
| `calcified_hyphae` | block+item | S:GARDENS | wg nbt17 code | tag2 decor3764 java8 |  |
| `cap_bridge` | block | none | code | java1 class |  |
| `cardinal_focus` | block+item | H:STATIONS | craft nbt1 code | decor1 java16 class |  |
| `centrifugal_governor_kit` | item | H:STATIONS | craft | rite book inq |  |
| `chalybeate_sclerite` | item | S:BRINED_COURT | chest code | alembic+craftx5+rite inq java1 |  |
| `chamber_of_will_teleporter` | item | WIP | - | - | NO-SOURCE |
| `chapel_focus` | block+item | S:SAINTS | nbt12 unbreakable | decor12 java1 class | set-piece |
| `charm_of_vascularium` | item | H:BLOOD_TOOLS | craft code | adv book java6 class |  |
| `charred_bookshelf` | block+item | S:SAINTS | nbt1 | tag1 decor10 |  |
| `charred_log` | block+item | S:SAINTS | nbt1 | craft tag1 decor9 |  |
| `charred_planks` | block+item | S:SAINTS | craft | craftx2 tag1 |  |
| `charred_planks_slab` | block+item | S:SAINTS | craft nbt1 | tag3 lootcond decor5 |  |
| `charred_planks_stairs` | block+item | S:SAINTS | craft | tag3 |  |
| `chiseled_bone_bricks` | block+item | S:SAINTS | craft+cut nbt61 | decor989 |  |
| `chiseled_hematic_iron_block` | block+item | H:BUILDING | craft adv nbt5 | craftx2+ritex5 tag3 adv decor8 java1 |  |
| `chiseled_polished_venous_stone` | block+item | H:BUILDING | craft adv nbt14 | craft adv decor136 |  |
| `chitinite_arm_banner` | item | H:ARSENAL | craft | class |  |
| `chitinite_block` | block+item | H:BUILDING | craft | - | NO-USE |
| `chitinite_boots` | item | H:ARMOR | armature | armature java3 class |  |
| `chitinite_chestplate` | item | H:ARMOR | armature | armature java3 class |  |
| `chitinite_fitting` | item | H:ARSENAL | code | craft java3 class |  |
| `chitinite_helm` | item | H:ARMOR | armature | armature java3 class |  |
| `chitinite_leggings` | item | H:ARMOR | armature | armature java3 class |  |
| `chitinite_mace` | item | H:ARSENAL | craft | class |  |
| `chitinite_shield` | item | H:ARSENAL | craft | class |  |
| `chitinous_husk` | item | H:DROPS | chest mob | alembic+craftx12+incub+loom want java4 |  |
| `chromatic_sublimate` | item | H:ALCHEMY | craft code | armaturex4+craftx2+ritex2 inq java1 |  |
| `cinder_bellows_frame` | item | S:CIRCUS | craft | rite |  |
| `circus_carousel_record` | block+item | S:CIRCUS | nbt1 NOLOOT | decor1 class | world-only |
| `circus_curtain` | block+item | S:CIRCUS | craft trade nbt2 | tag1 want decor1320 |  |
| `circus_loft_record` | block+item | S:CIRCUS | nbt1 NOLOOT | decor1 class | world-only |
| `circus_quarters_record` | block+item | S:CIRCUS | nbt2 NOLOOT | decor2 class | world-only |
| `circus_stage_record` | block+item | S:CIRCUS | nbt1 NOLOOT | decor1 class | world-only |
| `circus_waybill` | item | S:CIRCUS | code | java3 class |  |
| `clairaudiograph` | block+item | H:STATIONS | craft nbt1 | book inq decor1 java4 |  |
| `cleansed_blood_crystal_shard` | item | S:UNSTAINED | craft+humor+ritex2 code | craft tag2 inq java6 |  |
| `cleansed_sanguine_glass` | block+item | S:UNSTAINED | humor | java2 |  |
| `cleansed_sanguine_pane` | block+item | S:UNSTAINED | humor | - | NO-USE |
| `cleansed_stone` | block+item | S:UNSTAINED | craft+humorx2 code | bstruct+ritex9 tag1 java5 |  |
| `cleansing_hemolymph` | item | H:BLOOD_TOOLS | mob code | craftx2+incub inq java4 class |  |
| `clotting_lattice` | item | H:STATIONS | craft | rite book |  |
| `conscious_mass` | block+item | S:GARDENS | craft adv wg nbt4 code | craftx2+ritex5 tag4 adv decor239 java8 |  |
| `consecrated_bloodwell` | block+item | H:STATIONS | code NOLOOT | inq java13 class | world-only |
| `consecrated_copper_ingot` | item | S:UNSTAINED | alembic+ritex2 code | alembic+craftx8+humor tag1 book inq java11 |  |
| `consecrated_syringe` | item | S:SAINTS | code | inq java2 class |  |
| `constrictor_cord` | item | H:MATERIALS | craft | java1 class |  |
| `containment_anchor_block` | block+item | S:SAINTS | nbt11 code unbreakable | decor64 java3 class | set-piece |
| `cooked_pelagic_herring` | item | S:BRINED_COURT | smelt+smoke trade | tag2 want food |  |
| `cooked_venison` | item | H:FOOD | smelt+smoke | tag1 food |  |
| `cortical_crown` | item | H:STATIONS | craft code | rite book java1 |  |
| `cortical_folds` | block+item | S:END | code | java1 |  |
| `covenant_mantle` | item | H:ARMOR | craft code | java2 class |  |
| `covenant_throne` | block+item | H:STATIONS | rite NOLOOT | inq java3 class |  |
| `covenant_waybill` | item | H:GUIDES | code | java1 class |  |
| `cracked_bone_bricks` | block+item | S:SAINTS | smelt nbt8 | decor160 |  |
| `cracked_polished_venous_stone_bricks` | block+item | H:BUILDING | smelt nbt33 | decor1624 |  |
| `crimson_flames` | block+item | H:NATURAL | wg nbt2 code NOLOOT | decor11 java5 class | world-only |
| `crimson_lacquer` | item | H:ALCHEMY | craft trade code | armaturex12+craftx4 tag1 inq java2 |  |
| `crimson_vestment_fitting` | item | H:ARSENAL | code | java3 class |  |
| `crucible_of_nether_ichor` | block+item | WIP | craft | - | NO-USE WIP |
| `crude_memory_blood_lamp` | item | hidden(retired) | - | class | retired |
| `crude_memory_blood_rush` | item | H:MEMORIES | chest mob code | inq java1 class |  |
| `crude_memory_blood_shot` | item | H:MEMORIES | chest mob code | inq java1 class |  |
| `crude_memory_crimson_harvest` | item | hidden(retired) | - | class | retired |
| `crude_memory_deadly_gaze` | item | H:MEMORIES | chest mob code | inq java1 class |  |
| `crude_memory_glacial_grasp` | item | H:MEMORIES | chest | inq class |  |
| `crude_memory_hematic_flare` | item | H:MEMORIES | chest | inq class |  |
| `crude_memory_hemorrhage` | item | H:MEMORIES | chest | inq class |  |
| `crude_memory_sanguine_ignition` | item | H:MEMORIES | chest | inq class |  |
| `crude_memory_sanguine_mending` | item | H:MEMORIES | chest | inq class |  |
| `crude_memory_void_shroud` | item | H:MEMORIES | chest | inq class |  |
| `cryostroma_perdurans` | item | H:SCARS | crucible | java2 class |  |
| `cured_clay_jug` | item | H:ALCHEMY | craft | alembicx8 java4 |  |
| `current_strand` | block+item | S:BRINED_COURT | nbt2 unbreakable | tag1 decor178 class | set-piece |
| `current_tissue` | block+item | S:BRINED_COURT | nbt2 unbreakable | decor250 | set-piece |
| `curved_horn` | item | H:BLOOD_TOOLS | gourdfill+rite | craft+gourdfill java2 class |  |
| `cuttlefish_chromatophores` | item | S:BRINED_COURT | mob | craftx2+incub+rite tag1 |  |
| `dark_lacquer_planks` | block+item | S:SAINTS | craft nbt10 | craftx2 decor836 |  |
| `dark_lacquer_planks_slab` | block+item | S:SAINTS | craft nbt9 | tag2 lootcond decor90 |  |
| `dark_lacquer_planks_stairs` | block+item | S:SAINTS | craft nbt12 | tag2 decor1131 |  |
| `debug_showcase` | item | WIP | - | class | NO-SOURCE |
| `dendrite_growth` | block+item | S:END | - | - | NO-SOURCE |
| `dendritic_distributor` | block+item | H:STATIONS | rite code | inq java3 class |  |
| `desert_husk_effigy` | item | WIP | craft | class | WIP |
| `devils_tooth` | block+item | H:NATURAL | wg code | alembic java4 class |  |
| `dictation_table` | block+item | H:STATIONS | NOLOOT | craft java1 | NO-SOURCE |
| `dormant_ganglion` | block | none | code | java3 |  |
| `draught_of_still_mercy` | item | WIP | alembic | inq java2 | WIP |
| `draught_of_still_waters` | item | S:UNSTAINED | alembic trade | inq class |  |
| `dried_gourd` | item | H:MATERIALS | bstruct code | ritex2 java3 class |  |
| `dried_leech` | item | H:DROPS | smelt | alembic |  |
| `drudge_electrode` | item | WIP | - | class | NO-SOURCE |
| `drudge_submission_device` | item | WIP | - | class | NO-SOURCE |
| `druse_heart` | item | H:STATIONS | craft | rite book |  |
| `dura_membrane` | block+item | S:END | - | - | NO-SOURCE |
| `earthen_vein` | block+item | H:STATIONS | wg code | inq java4 class |  |
| `echo_of_heart` | item | WIP | code | java1 class | WIP |
| `echo_of_kidneys` | item | WIP | code | java1 class | WIP |
| `echo_of_liver` | item | WIP | code | java1 class | WIP |
| `echo_of_lungs` | item | WIP | code | java1 class | WIP |
| `echo_of_spleen` | item | WIP | code | java1 class | WIP |
| `edacious_blood_lust_boots` | item | H:ARMOR | armature | armature java4 class |  |
| `edacious_blood_lust_chest` | item | H:ARMOR | armature | armature java4 class |  |
| `edacious_blood_lust_helm` | item | H:ARMOR | armature | armature java5 class |  |
| `edacious_blood_lust_legs` | item | H:ARMOR | armature | armature java4 class |  |
| `engram_block` | block+item | hidden | nbt-dead1 code NOLOOT | craft java4 | world-only |
| `engram_stamp` | item | H:GUIDES | craft | java1 class |  |
| `enzymatic_scriptorium` | block+item | H:STATIONS | rite | ritex2 book java4 class |  |
| `enzyme_primer` | item | H:ALCHEMY | craft trade code | craftx11+ritex2 tag1 java1 |  |
| `erythrocoral_block` | block+item | S:BRINED_COURT | nbt4 code | craft decor77 java1 |  |
| `erythrocoral_fan` | block+item | S:BRINED_COURT | nbt-item code | java1 class |  |
| `erythrocoral_fragment` | item | S:BRINED_COURT | chest trade drop:erythrocoral_block,erythrocoral_fan | alembic+craft want inq |  |
| `erythrocoral_tendril` | block+item | S:BRINED_COURT | code | java1 class |  |
| `erythrocytic_dirt` | block+item | S:GARDENS | wg code | tag2 java11 |  |
| `erythrocytic_mycelium` | block+item | S:GARDENS | wg code | rite tag2 book java22 class |  |
| `escharian_overgrowth` | block | none | wg | java3 class |  |
| `escharian_overgrowth_rim` | block | none | code | java4 class |  |
| `escharian_scyphus` | block+item | S:NETHER | code | lootcond java2 class |  |
| `faded_memory` | item | H:MEMORIES | code | tag1 want java2 |  |
| `fargone_proboscis` | item | H:DROPS | mob code | armaturex4 tag1 java1 |  |
| `ferric_binder` | item | H:ALCHEMY | craftx2 trade | bstruct+craftx7+ritex6 tag1 want |  |
| `ferric_enzyme` | item | H:ALCHEMY | lantern+render mob | alembicx2+brew+craftx4 tag2 adv book inq java3 class |  |
| `ferric_spores` | item | H:SCARS | craftx3 | lantern java1 |  |
| `fervent_enzyme` | item | H:ALCHEMY | lantern+render code | alembicx2+brew+craftx4 tag1 adv book inq java4 class |  |
| `fervent_husk` | item | H:DROPS | mob code | armaturex4+loomx2+rite java3 class |  |
| `fervent_spores` | item | H:SCARS | craftx2 | craft+lantern java1 |  |
| `filler_block` | block | none | nbt5 code | java29 class |  |
| `fold_veil` | block+item | S:BRINED_COURT | nbt1 unbreakable | decor16 class | set-piece |
| `foul_paste` | item | H:ALCHEMY | alembicx4+craftx2 code | craftx3+loomx5+smelt adv inq java5 class |  |
| `fractionating_rotor_kit` | item | H:STATIONS | craft | rite book inq |  |
| `frigid_enzyme` | item | H:ALCHEMY | lantern+render | alembicx2+craftx4 tag2 want adv book inq java3 class |  |
| `frigid_spores` | item | H:SCARS | craftx2 | craft+crucible+lantern java1 |  |
| `frozen_cruor` | block+item | H:BUILDING | code NOLOOT | java15 | world-only |
| `frozen_devotee` | block+item | S:SAINTS | nbt12 unbreakable | decor206 class | set-piece |
| `frozen_drip` | block+item | S:SAINTS | nbt12 | decor97 class |  |
| `fruiting_body_cache` | block | none | code | tag2 java3 class |  |
| `fruiting_infected_cap` | block+item | S:GARDENS | nbt1 code | tag2 decor126 java2 class |  |
| `fruiting_lure` | item | S:GARDENS | craft | java2 |  |
| `fungal_gill` | block+item | S:GARDENS | wg | class |  |
| `fungal_implantation_pylon` | block+item | H:STATIONS | rite NOLOOT | inq java1 class |  |
| `fungal_podium` | block+item | H:STATIONS | nbt-dead1 code NOLOOT | inq java4 class | world-only |
| `fungal_spine` | item | S:GARDENS | chest code | tag1 adv java7 class |  |
| `ganglion_cluster` | item | S:END | mob | craft+incub java1 |  |
| `ganglion_matter` | block+item | S:END | code | java4 |  |
| `ganglionic_harness` | item | H:STATIONS | craft code | rite book java1 |  |
| `ghastly_alembic` | block+item | H:STATIONS | bstruct nbt1 code | ritex2 tag1 adv book inq decor1 java9 class |  |
| `ghost_pipe` | block+item | S:UNSTAINED | drop:potted_ghost_pipe wg | alembicx3 book java7 class |  |
| `giant_tube_worm_colony` | block+item | S:BRINED_COURT | code | incub lootcond java5 class |  |
| `gilded_venous_stone` | block+item | H:BUILDING | - | java1 | NO-SOURCE |
| `gore_wound_harpoon` | item | S:NETHER | mob code | tag2 java7 class |  |
| `gorebound_yoke` | item | S:CIRCUS | craft | rite |  |
| `gourd` | block+item | H:STATIONS | code | bstructx2+craftx2 adv class |  |
| `gourd_seeds` | item | H:FOOD | craft chest lootmod adv drop:attached_gourd_stem nbt-item | bstruct+craft tag1 adv java1 |  |
| `gourd_slice` | item | H:FOOD | craft | craftx3 food |  |
| `gourd_stew` | item | H:FOOD | craft | food |  |
| `gourdvine_tap` | block+item | WIP | craft | java1 class | WIP |
| `grinning_mask` | item | H:ARMOR | craft | armature inq |  |
| `hallowed_residuum_fragment_hemorath` | item | S:SAINTS | code | craftx3 |  |
| `hallowed_residuum_fragment_putriciel` | item | S:SAINTS | code | craftx3 |  |
| `hallowed_residuum_fragment_seraphae` | item | S:SAINTS | code | craftx3 |  |
| `hallowed_residuum_fragment_velorum` | item | S:SAINTS | code | craftx3 |  |
| `hallowed_residuum_hemorath` | item | S:SAINTS | craft mob code | loom tag1 inq java2 class |  |
| `hallowed_residuum_putriciel` | item | S:SAINTS | craft mob code | loom tag1 inq java2 class |  |
| `hallowed_residuum_seraphae` | item | S:SAINTS | craft mob code | loom tag1 inq java2 class |  |
| `hallowed_residuum_velorum` | item | S:SAINTS | craft mob code | craft+loom tag1 inq java2 class |  |
| `hanging_icicle` | block+item | S:SAINTS | nbt24 | decor604 class |  |
| `harbinger_assignment_ledger` | item | H:GUIDES | code | java4 class |  |
| `harbinger_escritoire` | block+item | H:STATIONS | craft nbt2 | decor2 java1 class |  |
| `healed_calcite` | block+item | S:SAINTS | nbt12 | decor165 |  |
| `healed_quartz_bricks` | block+item | S:SAINTS | nbt21 | decor136 |  |
| `heart_node` | block+item | S:GARDENS | craft | tag2 java5 class |  |
| `heart_pattern` | item | H:GUIDES | code | java1 class |  |
| `hearty_compass` | item | H:BLOOD_TOOLS | craft | craft class |  |
| `hematic_algal_crust` | block+item | S:BRINED_COURT | code | incub java4 class |  |
| `hematic_armature` | block+item | H:STATIONS | craft code | ritex2 inq java6 class |  |
| `hematic_condenser_kit` | item | H:STATIONS | craft code | rite book java1 |  |
| `hematic_iron_bars` | block+item | H:BUILDING | craft nbt3 | bstructx2 tag2 decor25 java1 class |  |
| `hematic_iron_block` | block+item | H:BUILDING | bstruct+craft adv drop:mycelial_synapse nbt4 code | bstructx7+craftx6+cut+ritex22 tag4 adv decor6 java8 |  |
| `hematic_iron_boots` | item | H:ARMOR | armature | armaturex3 tag1 adv java2 class |  |
| `hematic_iron_chain` | block+item | H:BUILDING | craft nbt20 | tag2 decor261 |  |
| `hematic_iron_chestplate` | item | H:ARMOR | armature | armaturex3 tag1 adv java2 class |  |
| `hematic_iron_door` | block+item | H:BUILDING | craft nbt4 | tag2 lootcond decor8 |  |
| `hematic_iron_helm` | item | H:ARMOR | armature | armaturex3 tag1 adv java2 class |  |
| `hematic_iron_knapper` | item | H:ARSENAL | code | tag1 java1 class |  |
| `hematic_iron_leggings` | item | H:ARMOR | armature | armaturex3 tag1 adv java2 class |  |
| `hematic_iron_ore` | block+item | H:NATURAL | wg code | tag1 java1 |  |
| `hematic_iron_pillar` | block+item | H:BUILDING | craft adv nbt9 | bstruct+ritex14 tag2 adv decor445 |  |
| `hematic_iron_plating` | block+item | H:BUILDING | craft+cut nbt29 | craftx3+cutx3 decor2927 |  |
| `hematic_iron_plating_slab` | block+item | H:BUILDING | craft+cut nbt4 | tag2 lootcond decor106 |  |
| `hematic_iron_plating_stairs` | block+item | H:BUILDING | craft+cut nbt29 | tag2 decor1781 |  |
| `hematic_iron_plating_wall` | block+item | H:BUILDING | craft+cut nbt12 | tag2 decor137 |  |
| `hematic_iron_powder` | item | H:MATERIALS | alembicx3+nectar chest trade code | craftx10+crucible+ritex3 tag1 want java2 |  |
| `hematic_iron_scrap` | item | H:MATERIALS | craft chest mob adv drop:hematic_iron_ore code | alembic+armaturex4+craftx43+nectar+render+rite tag2 want adv inq java12 |  |
| `hematic_iron_sword` | item | H:ARSENAL | craft adv | tag1 adv class |  |
| `hematic_iron_trapdoor` | block+item | H:BUILDING | craft | tag2 |  |
| `hematic_lantern` | block+item | H:BUILDING | craft nbt28 | decor138 |  |
| `hematic_memory` | item | H:MEMORIES | craft code | craftx9+ritex3 book inq java8 class |  |
| `hematic_microscope` | item | H:BLOOD_TOOLS | craft | book inq java2 class |  |
| `hematic_stake` | block+item | H:STATIONS | bstruct code NOLOOT | java8 class |  |
| `hematic_suture_needle` | item | WIP | craft trade code | java2 class | WIP |
| `hematic_suture_node` | block+item | H:STATIONS | craft | java1 class |  |
| `hemolymphopoda_headpiece` | item | H:ARMOR | craft | adv class |  |
| `hemolytic_plating` | item | S:UNSTAINED | craft code | craftx6+rite book inq java4 |  |
| `hemolytic_plating_block` | block+item | S:UNSTAINED | code | java1 |  |
| `hemolytic_solution` | item | S:UNSTAINED | alembic chest mob code | alembic+craftx4+rite adv book inq java9 |  |
| `hemolytic_vial` | item | S:UNSTAINED | rite code | inq java2 class |  |
| `hemorath_rib` | item | S:SAINTS | - | java1 class | NO-SOURCE |
| `hemorrhagic_crust` | block+item | S:GARDENS | wg nbt15 code | tag1 decor7354 java5 |  |
| `holdfast_root_mass` | block+item | S:BRINED_COURT | nbt1 unbreakable | decor7 class | set-piece |
| `house_marker` | block+item | S:BRINED_COURT | nbt7 code unbreakable | decor7 java1 class | set-piece |
| `humane_idol` | block+item | WIP | - | java1 class | NO-SOURCE |
| `humoral_barometer` | block+item | H:STATIONS | craft nbt1 | decor1 class |  |
| `hydra_crown` | block+item | S:BRINED_COURT | code unbreakable | java3 class | set-piece |
| `hyphae` | block+item | S:GARDENS | wg code NOLOOT | craftx3 java2 class | world-only |
| `hyphae_block` | block+item | S:GARDENS | wg nbt22 code | craft+rite tag3 decor508 java8 |  |
| `hyphal_substrate` | item | H:SCARS | craft | craftx12+rite |  |
| `immature_fungal_scar` | item | H:SCARS | cruciblex8 | class |  |
| `incandescent_enzyme` | item | H:ALCHEMY | lantern+render trade | alembicx2+craftx4 tag1 adv book inq java3 class |  |
| `incandescent_spores` | item | H:SCARS | craftx2 | lantern |  |
| `infected_cap` | block+item | S:GARDENS | wg nbt15 code | tag2 decor523 java6 class |  |
| `infected_fungus` | block+item | S:GARDENS | drop:potted_infected_fungus wg code | alembic+craftx2 tag1 java9 class |  |
| `infected_stem` | block+item | S:GARDENS | wg nbt1 code | decor60 java27 |  |
| `infested_venous_stone` | block+item | H:BUILDING | craft adv wg code | humor+rite tag4 adv java11 |  |
| `infested_wood` | block+item | H:BUILDING | craft code | craft+rite java6 class |  |
| `iron_brazier` | block+item | H:STATIONS | bstruct nbt4 code | tag1 inq decor5 java6 class |  |
| `kindled_tissue` | block+item | S:BRINED_COURT | nbt2 unbreakable | decor1591 java1 | set-piece |
| `lantern_tick_helmet` | item | H:ARMOR | mob | java1 class |  |
| `laughing_mask` | block+item | S:CIRCUS | craft trade | want class |  |
| `lethean_brew` | item | WIP | alembic trade | alembic inq java1 class | WIP |
| `lethean_chalice` | item | S:UNSTAINED | craft+rite | java2 class |  |
| `lethean_dew` | item | S:UNSTAINED | alembic trade code | alembicx2+craftx2 book inq java7 class |  |
| `lethean_poppy` | block+item | S:UNSTAINED | trade drop:potted_lethean_poppy wg NOLOOT | alembic+craftx5+ritex5 inq java6 class |  |
| `lethean_poppy_wreath` | item | S:UNSTAINED | craft+ritex2 code | tag1 book inq java4 class |  |
| `liber_immaculatus` | item | S:UNSTAINED | craft chest code | adv inq java10 class |  |
| `liber_sanguinum` | item | H:GUIDES | bstruct | adv book java5 class |  |
| `listening_scar` | item | H:BLOOD_TOOLS | code | java4 |  |
| `litany_stone` | block+item | S:SAINTS | nbt3 unbreakable | decor6 class | set-piece |
| `living_axe` | item | H:ARSENAL | code | tag1 java7 class |  |
| `living_baghnakh` | item | H:ARSENAL | code | tag1 java7 class |  |
| `living_blade` | item | H:ARSENAL | code | tag1 adv java8 class |  |
| `living_crossbow` | item | H:ARSENAL | code | tag1 java8 class |  |
| `living_flail` | item | H:ARSENAL | code | tag1 java9 class |  |
| `living_sickle` | item | hidden | code | tag1 java10 class |  |
| `living_spear` | item | H:ARSENAL | code | tag1 java7 class |  |
| `living_staff` | item | H:ARSENAL | bstruct code | tag1 adv inq java27 class |  |
| `living_syringe` | item | H:BLOOD_TOOLS | code | tag1 inq java3 class |  |
| `living_torch` | item | H:ARSENAL | code | tag1 java8 class |  |
| `living_weapon_graft` | item | hidden | craftx7 code | adv inq java3 class |  |
| `lodestone_faceplate` | item | H:ARMOR | craft | armature+craft inq |  |
| `marimo_heart` | block+item | S:BRINED_COURT | code unbreakable | java4 class | set-piece |
| `marimo_moss` | block+item | S:BRINED_COURT | code unbreakable | java3 class | set-piece |
| `marionette_crossbar` | item | S:CIRCUS | craft code | ritex7 book java6 class |  |
| `marrow_crown` | item | H:ARMOR | chest | class |  |
| `marrow_plume` | block+item | S:BRINED_COURT | nbt1 unbreakable | decor13 class | set-piece |
| `marrow_spitter_carriage` | item | S:CIRCUS | craft | rite |  |
| `mason_effigy` | block+item | H:STATIONS | rite nbt1 | decor1 java2 class |  |
| `masons_respite_map` | item | H:GUIDES | code | java1 class |  |
| `master_cam_kit` | item | H:STATIONS | craft | rite book |  |
| `memory_absolute_stillness` | item | H:MEMORIES | loom | class |  |
| `memory_activation_potential` | item | H:MEMORIES | loom chest | adv class |  |
| `memory_axonal_transduction` | item | H:MEMORIES | loom | inq class |  |
| `memory_black_veil_covenant` | item | H:MEMORIES | loom | inq class |  |
| `memory_blackhearted` | item | H:MEMORIES | loom | class |  |
| `memory_blood_absorption` | item | hidden(retired) | - | class | retired |
| `memory_blood_aneurysm` | item | H:MEMORIES | loom chest | adv inq class |  |
| `memory_blood_binding` | item | H:MEMORIES | loom | inq class |  |
| `memory_blood_cloud` | item | H:MEMORIES | loom chest | adv inq class |  |
| `memory_blood_eclipse` | item | H:MEMORIES | loomx2 chest | inq class |  |
| `memory_blood_eclipse_mantle` | item | hidden(retired) | - | class | retired |
| `memory_blood_lamp` | item | hidden(retired) | - | class | retired |
| `memory_blood_lattice` | item | H:MEMORIES | loom | class |  |
| `memory_blood_needle` | item | H:MEMORIES | loom chest | adv inq class |  |
| `memory_blood_needle_fan` | item | H:MEMORIES | loom | class |  |
| `memory_blood_needle_lance` | item | H:MEMORIES | loom | class |  |
| `memory_blood_projection` | item | hidden(retired) | - | class | retired |
| `memory_blood_rush` | item | H:MEMORIES | loomx2 chest | adv inq class |  |
| `memory_blood_shot` | item | H:MEMORIES | loom chest | adv inq java1 class |  |
| `memory_bloom_of_rot` | item | S:SAINTS | loom | inq class |  |
| `memory_canopy_mortis` | item | H:MEMORIES | loom | class |  |
| `memory_carrion_communion` | item | H:MEMORIES | loom | class |  |
| `memory_cauterizing_rebuke` | item | H:MEMORIES | loom | inq class |  |
| `memory_chain_blood_binding` | item | H:MEMORIES | loom | class |  |
| `memory_conductive_mark` | item | H:MEMORIES | loom | inq class |  |
| `memory_conjure_living_staff` | item | hidden(retired) | - | class | retired |
| `memory_crimson_coronation` | item | H:MEMORIES | loom | class |  |
| `memory_crimson_flame_conjuration` | item | H:MEMORIES | loom | class |  |
| `memory_crimson_harvest` | item | hidden(retired) | - | class | retired |
| `memory_crimson_sight` | item | hidden(retired) | - | class | retired |
| `memory_crimson_tithe` | item | S:SAINTS | loom chest | inq class |  |
| `memory_cruor_javelin` | item | H:MEMORIES | loom chest | inq class |  |
| `memory_cryogenic_pulse` | item | H:MEMORIES | loom | inq class |  |
| `memory_deadly_gaze` | item | H:MEMORIES | loom chest | adv inq class |  |
| `memory_eclipse_well` | item | H:MEMORIES | loom | class |  |
| `memory_endless_hour` | item | S:SAINTS | loom | inq class |  |
| `memory_expansive_blood_cloud` | item | H:MEMORIES | loom | class |  |
| `memory_expulsive_updraft` | item | H:MEMORIES | loom | class |  |
| `memory_exsanguinate` | item | H:MEMORIES | loom chest | inq class |  |
| `memory_ferric_rampart` | item | H:MEMORIES | loom | class |  |
| `memory_ferric_resonance` | item | hidden(retired) | - | class | retired |
| `memory_ferric_spikes` | item | H:MEMORIES | loom | class |  |
| `memory_ferric_transmutation` | item | hidden(retired) | - | class | retired |
| `memory_fruiting_contagion` | item | H:MEMORIES | loom | inq class |  |
| `memory_funeral_bell` | item | H:MEMORIES | loom | class |  |
| `memory_furnace_veins` | item | H:MEMORIES | loom | class |  |
| `memory_glacial_bastion` | item | hidden(retired) | - | class | retired |
| `memory_glacial_circulation` | item | hidden(retired) | - | class | retired |
| `memory_glacial_grasp` | item | H:MEMORIES | loom | inq class |  |
| `memory_glacial_rampart` | item | H:MEMORIES | loom | inq class |  |
| `memory_gloam_laceration` | item | H:MEMORIES | loom | inq class |  |
| `memory_grave_debt` | item | H:MEMORIES | loom | inq class |  |
| `memory_guided_blood_shot` | item | H:MEMORIES | loom | class |  |
| `memory_hematic_ballast` | item | hidden(retired) | - | class | retired |
| `memory_hematic_beacon` | item | H:MEMORIES | loom | inq class |  |
| `memory_hematic_flare` | item | H:MEMORIES | loom code | inq java2 class |  |
| `memory_hematic_impressment` | item | H:MEMORIES | loom | class |  |
| `memory_hematic_mortar` | item | H:MEMORIES | loom | class |  |
| `memory_hematic_rebuke` | item | H:MEMORIES | loom | class |  |
| `memory_hematic_riposte` | item | H:MEMORIES | loom | inq class |  |
| `memory_hemolymphal_pulse` | item | H:MEMORIES | loom | inq class |  |
| `memory_hemorrhage` | item | H:MEMORIES | loomx2 chest | inq class |  |
| `memory_hemosynthesis` | item | hidden(retired) | - | class | retired |
| `memory_insatiable_hunger` | item | H:MEMORIES | loom | inq class |  |
| `memory_iron_choir` | item | H:MEMORIES | loom | class |  |
| `memory_iron_retort` | item | H:MEMORIES | loom | inq class |  |
| `memory_ironhearted` | item | H:MEMORIES | loom | class |  |
| `memory_lignum_mortis` | item | H:MEMORIES | loom | class |  |
| `memory_lingering_blood_binding` | item | H:MEMORIES | loom | class |  |
| `memory_living_axe` | item | hidden(legacy) | - | adv inq class | retired |
| `memory_living_blade` | item | hidden(legacy) | - | adv inq class | retired |
| `memory_living_circuit` | item | H:MEMORIES | loom | class |  |
| `memory_living_claws` | item | hidden(legacy) | - | adv inq class | retired |
| `memory_living_crossbow` | item | hidden(legacy) | - | adv inq class | retired |
| `memory_living_flail` | item | hidden(legacy) | - | adv inq class | retired |
| `memory_living_spear` | item | hidden(legacy) | - | adv inq class | retired |
| `memory_living_torch` | item | hidden(legacy) | - | adv inq class | retired |
| `memory_lodestone_collapse` | item | H:MEMORIES | loom | inq class |  |
| `memory_lumen_suture` | item | H:MEMORIES | loom chest | inq class |  |
| `memory_of_vesper` | item | S:GARDENS | mob code | tag1 inq java5 class |  |
| `memory_osseous_bloom` | item | H:MEMORIES | loom | inq class |  |
| `memory_penumbral_drift` | item | H:MEMORIES | loom | class |  |
| `memory_phoenix_debt` | item | H:MEMORIES | loom | class |  |
| `memory_prismatic_reproof` | item | H:MEMORIES | loom chest | inq class |  |
| `memory_pursuing_blood_cloud` | item | H:MEMORIES | loom | class |  |
| `memory_pyretic_forge` | item | H:MEMORIES | loom | inq class |  |
| `memory_radiant_wake` | item | H:MEMORIES | loom | inq class |  |
| `memory_rimebound_sentence` | item | H:MEMORIES | loom | class |  |
| `memory_sanguine_excavation` | item | hidden(retired) | - | class | retired |
| `memory_sanguine_halo` | item | H:MEMORIES | loom | class |  |
| `memory_sanguine_ignition` | item | H:MEMORIES | loomx2 | inq class |  |
| `memory_sanguine_magnetism` | item | H:MEMORIES | loom | inq class |  |
| `memory_sanguine_marionette` | item | H:MEMORIES | loom | class |  |
| `memory_sanguine_mending` | item | H:MEMORIES | loom chest | inq class |  |
| `memory_sanguine_tempest` | item | H:MEMORIES | loom | class |  |
| `memory_sanguine_ward` | item | H:MEMORIES | loom | adv inq class |  |
| `memory_scalding_updraft` | item | H:MEMORIES | loom | inq class |  |
| `memory_searing_lash` | item | H:MEMORIES | loom chest | inq class |  |
| `memory_soaring_updraft` | item | H:MEMORIES | loom | class |  |
| `memory_sovereign_instinct` | item | H:MEMORIES | loom | class |  |
| `memory_summon_avatar` | item | H:MEMORIES | loom | inq class |  |
| `memory_summon_avatar_armor` | item | H:MEMORIES | loom | class |  |
| `memory_summon_avatar_arms` | item | H:MEMORIES | loom | class |  |
| `memory_summon_avatar_complete` | item | H:MEMORIES | loom | class |  |
| `memory_summon_avatar_legs` | item | H:MEMORIES | loom | class |  |
| `memory_summon_thrall` | item | hidden(retired) | - | class | retired |
| `memory_suspended_updraft` | item | H:MEMORIES | loom | class |  |
| `memory_synaptic_jolt` | item | H:MEMORIES | loom | inq class |  |
| `memory_synaptic_storm` | item | H:MEMORIES | loom | class |  |
| `memory_thread` | item | WIP | craft trade | rite tag1 want class | WIP |
| `memory_thread_ripper` | item | H:MEMORIES | code | java2 class |  |
| `memory_umbral_rend` | item | H:MEMORIES | loom | inq class |  |
| `memory_umbral_reversal` | item | H:MEMORIES | loom | inq class |  |
| `memory_umbral_step` | item | H:MEMORIES | loomx2 | inq class |  |
| `memory_unclosing_eye` | item | S:SAINTS | loom | inq class |  |
| `memory_vascular_dowsing` | item | H:MEMORIES | loom | inq class |  |
| `memory_venous_travel` | item | hidden(retired) | - | class | retired |
| `memory_vigil_of_glass` | item | hidden(retired) | - | class | retired |
| `memory_vital_effusion` | item | H:MEMORIES | loom | inq class |  |
| `memory_vital_reservoir` | item | hidden(retired) | - | class | retired |
| `memory_vitric_combustion` | item | H:MEMORIES | loom | inq class |  |
| `memory_void_shroud` | item | H:MEMORIES | loom chest | inq class |  |
| `memory_white_verdict` | item | H:MEMORIES | loom | class |  |
| `memory_worked_lignum` | item | H:MEMORIES | loom | class |  |
| `messenger_buoy` | block+item | S:BRINED_COURT | code unbreakable | java1 class | set-piece |
| `mind_spike` | item | H:BLOOD_TOOLS | craft+rite adv | adv class |  |
| `mnemonic_ambergris` | item | S:BRINED_COURT | code | armaturex4+craftx11+ritex4 book inq java8 |  |
| `mnemonic_ambergris_block` | block+item | S:BRINED_COURT | craft | craft+rite |  |
| `mnemonic_blueprint` | item | H:GUIDES | craftx2 code | craft inq java4 class |  |
| `mnemonic_candle` | block+item | H:STATIONS | craft nbt9 | decor29 java1 class |  |
| `mnemonic_folio` | item | H:GUIDES | craft | rite java1 class |  |
| `mnemonic_reliquary` | block+item | H:STATIONS | rite nbt1 | inq decor1 java4 |  |
| `mnemonist_cradle` | item | S:CIRCUS | craft | rite |  |
| `monolith_fragment` | item | S:GARDENS | code | craftx4+rite tag1 inq java1 class |  |
| `monolith_imbued_cloth` | item | S:GARDENS | craft code | armaturex4+craft+rite inq java2 class |  |
| `monolithic_cornerstone` | item | H:STATIONS | craft code | rite inq java2 class |  |
| `monolithic_frame_fitting` | item | H:ARSENAL | code | java3 class |  |
| `morphic_nectar_block` | block | none | code | java3 |  |
| `morphic_tonic` | item | S:GARDENS | nectar | class |  |
| `morphling_cradle` | block+item | WIP | - | book java1 class | NO-SOURCE |
| `morphling_dead_mans_rope` | item | S:BRINED_COURT | incub | inq class |  |
| `morphling_holdfast` | item | S:BRINED_COURT | incub | inq class |  |
| `morphling_hushbell` | item | S:BRINED_COURT | incub | craft adv inq class |  |
| `morphling_incubator` | block+item | H:STATIONS | rite code | inq java5 class |  |
| `morphling_ink_cap` | item | S:BRINED_COURT | incub | inq class |  |
| `morphling_jar` | item | H:MORPHLINGS | craft | adv book java1 class |  |
| `morphling_kinbud` | item | S:BRINED_COURT | incub | inq class |  |
| `morphling_marrowplume` | item | S:BRINED_COURT | incub | inq class |  |
| `morphling_mermaids_purse` | item | S:BRINED_COURT | incub | inq class |  |
| `morphling_polyp` | item | H:MORPHLINGS | mob code | rite tag1 java5 class |  |
| `morphling_sunburst` | item | S:BRINED_COURT | incub | book inq class |  |
| `mortal_display` | block+item | H:STATIONS | nbt1 | inq decor1 java2 class |  |
| `mourning_mask` | block+item | S:CIRCUS | craft trade nbt1 | decor1 class |  |
| `mycelial_crucible` | block+item | H:STATIONS | craft code | java5 class |  |
| `mycelial_lantern` | block+item | H:STATIONS | rite chest nbt3 code NOLOOT | decor4 java4 class |  |
| `mycelial_synapse` | block+item | S:GARDENS | bstruct code NOLOOT | java4 class |  |
| `mycelium_erythrocytic_dirt` | block+item | S:GARDENS | wg code | tag1 java3 class |  |
| `mycophant_tendril` | item | S:GARDENS | mob code | tag1 java2 class |  |
| `myelin_sheath` | block+item | S:END | code | craftx2+rite java2 |  |
| `naeglerophaeon_ganglion` | item | S:END | craft mob | loom inq |  |
| `nerve_bundle` | block+item | S:END | code | craft+rite tag1 java5 |  |
| `nerve_fiber` | block+item | S:END | code | craft+rite tag1 java6 |  |
| `nest_cup` | block+item | S:BRINED_COURT | nbt1 unbreakable | decor13 class | set-piece |
| `neural_tissue` | block+item | S:END | code | java4 |  |
| `neurotic_enzyme` | item | H:ALCHEMY | lantern+render | alembicx2+brew+craftx6+ritex4 tag1 adv book inq java3 class |  |
| `neurotic_spores` | item | H:SCARS | craftx2 | crucible+lantern+rite |  |
| `neutralizing_gasket` | item | S:UNSTAINED | craft | inq |  |
| `noctifly_agaric` | item | H:SCARS | crucible | class |  |
| `non_euclidean_hallway` | block+item | WIP | NOLOOT | java3 class | NO-SOURCE |
| `oculiflora_reticularis` | item | H:SCARS | crucible | java1 class |  |
| `offering_gate` | block+item | WIP | nbt-dead2 NOLOOT | inq java1 class | NO-SOURCE |
| `offering_stand` | block+item | S:SAINTS | nbt3 unbreakable | decor18 java1 class | set-piece |
| `opened_niche` | block+item | S:SAINTS | nbt16 unbreakable | decor93 class | set-piece |
| `orb_of_perspective` | item | S:GARDENS | craft | inq java1 class |  |
| `organic_prism` | block+item | H:BUILDING | craft | - | NO-USE |
| `ossuary_clock` | block+item | S:SAINTS | craft | class |  |
| `packed_ash` | block+item | S:SAINTS | nbt28 | tag1 decor1349 |  |
| `pale_distillate` | item | S:UNSTAINED | alembic+craft code | alembic+bstruct+craftx6 tag1 inq java4 |  |
| `pale_humor_flask` | item | S:UNSTAINED | chest code | alembicx2+craft book inq java4 class |  |
| `pale_silver_bars` | block+item | S:UNSTAINED | craft | - | NO-USE |
| `pale_silver_bell` | item | S:UNSTAINED | craft+rite | tag1 inq java2 class |  |
| `pale_silver_bells` | block+item | S:UNSTAINED | code | inq java1 class |  |
| `pale_silver_block` | block+item | S:UNSTAINED | craft | craft+ritex3 |  |
| `pale_silver_ingot` | item | S:UNSTAINED | alembic+craftx2+humor+ritex3 code | craftx11 tag1 book inq java9 |  |
| `palimpsest_burin` | item | H:STATIONS | craft | rite book |  |
| `pallid_icon` | item | S:UNSTAINED | craft+ritex2 code | tag1 adv book inq java5 |  |
| `pallid_infusion` | item | S:UNSTAINED | alembic | inq class |  |
| `pallid_lantern` | block+item | S:UNSTAINED | craft | rite inq |  |
| `pallid_retort` | block+item | S:UNSTAINED | bstruct code | inq java3 class |  |
| `pallid_silver_chain` | block+item | S:UNSTAINED | craft | - | NO-USE |
| `petrified_follower` | block+item | S:BRINED_COURT | nbt1 code unbreakable | decor6 java3 class | set-piece |
| `phantasmal_blood_lust_boots` | item | H:ARMOR | armature | armature java4 class |  |
| `phantasmal_blood_lust_chest` | item | H:ARMOR | armature | armature java4 class |  |
| `phantasmal_blood_lust_helm` | item | H:ARMOR | armature | armature java5 class |  |
| `phantasmal_blood_lust_legs` | item | H:ARMOR | armature | armature java4 class |  |
| `phlebotomists_cabinet` | block+item | H:STATIONS | craft nbt1 | craft book inq decor1 java4 |  |
| `phlebotomists_field_case` | block+item | H:STATIONS | craft nbt1 code | book inq decor2 java4 |  |
| `phlegethontic_ichor_block` | block | none | code | java2 class |  |
| `pointed_blood_scorched_scab` | block+item | S:NETHER | code | java2 class |  |
| `polished_venous_stone` | block+item | H:BUILDING | craft trade adv nbt50 nbt-item code | craftx6+cut+ritex4 tag1 want adv decor7373 java2 |  |
| `polished_venous_stone_brick_slab` | block+item | H:BUILDING | craft adv nbt10 | adv decor127 |  |
| `polished_venous_stone_brick_stairs` | block+item | H:BUILDING | craft adv nbt5 | adv decor69 |  |
| `polished_venous_stone_brick_wall` | block+item | H:BUILDING | craft nbt9 | tag2 decor118 |  |
| `polished_venous_stone_bricks` | block+item | H:BUILDING | craft adv nbt46 | craftx5+smelt adv decor21136 |  |
| `polished_venous_stone_slab` | block+item | H:BUILDING | craft adv | craft adv |  |
| `polished_venous_stone_stairs` | block+item | H:BUILDING | craft adv nbt-dead2 | adv |  |
| `polished_venous_stone_wall` | block+item | H:BUILDING | craft+cut | tag2 |  |
| `potted_bleeding_heart` | block | none | nbt5 | - |  |
| `potted_ghost_pipe` | block | none | - | - |  |
| `potted_infected_fungus` | block | none | - | - |  |
| `potted_lethean_poppy` | block | none | - | - |  |
| `potted_puffball_fungus` | block | none | - | - |  |
| `potted_sarcodes` | block | none | - | - |  |
| `potted_stinkhorn_fungus` | block | none | - | - |  |
| `precision_governor_kit` | item | H:STATIONS | craft | rite |  |
| `prismatic_boots` | item | H:ARMOR | armature | armature java3 class |  |
| `prismatic_chestplate` | item | H:ARMOR | armature | armature java3 class |  |
| `prismatic_fitting` | item | H:ARSENAL | code | java3 class |  |
| `prismatic_helm` | item | H:ARMOR | armature | armature java3 class |  |
| `prismatic_leggings` | item | H:ARMOR | armature | armature java3 class |  |
| `puffball_fungus` | block+item | S:GARDENS | drop:potted_puffball_fungus wg | alembic+craftx2 tag1 java1 class |  |
| `pulse_pylon` | block+item | S:SAINTS | nbt3 unbreakable | decor36 java2 class | set-piece |
| `puppeteering_thread` | item | S:CIRCUS | chest mob trade code | alembic+craftx25+loom+ritex3 tag1 book inq java11 |  |
| `puppeteers_spindle` | block+item | S:CIRCUS | craft nbt7 | decor8 java1 class |  |
| `puppeteers_wool` | block+item | S:CIRCUS | craft nbt2 | craft decor1224 |  |
| `putrivora_resolvens` | item | H:SCARS | crucible | java1 class |  |
| `qliphoth_bloom` | block | none | nbt-dead1 code | java8 class |  |
| `qliphoth_pome` | item | S:GARDENS | code | inq java4 class food |  |
| `qliphoth_seed` | item | S:GARDENS | code | rite inq java3 class |  |
| `queens_physogastrism` | item | H:DROPS | mob | craft |  |
| `radiant_healed_quartz` | block+item | S:SAINTS | nbt6 | decor20 |  |
| `rafflesia` | block+item | H:NATURAL | wg code | alembic java2 class |  |
| `raw_pelagic_herring` | item | S:BRINED_COURT | mob trade | incub+smelt+smoke tag2 java1 food |  |
| `raw_venison` | item | H:FOOD | mob | smelt+smoke tag1 food |  |
| `recollection_cyst` | block | none | wg | class |  |
| `recollection_hemorath` | item | S:SAINTS | code | loom tag1 java1 |  |
| `recollection_putriciel` | item | S:SAINTS | code | loom tag1 java1 |  |
| `recollection_seraphae` | item | S:SAINTS | code | loom tag1 java1 |  |
| `recollection_velorum` | item | S:SAINTS | code | loom tag1 java1 |  |
| `recycled_enzyme` | item | H:ALCHEMY | alembic+render chest nbt-item code | tag1 want adv inq java5 class |  |
| `relay_chart` | item | S:BRINED_COURT | code | inq java2 class |  |
| `relay_marker` | block+item | S:BRINED_COURT | code unbreakable | java1 class | set-piece |
| `reliquary_sconce` | block+item | S:SAINTS | nbt58 unbreakable | decor219 class | set-piece |
| `rendering_bile_block` | block | none | code | java3 |  |
| `resonant_forge` | block+item | H:STATIONS | rite nbt1 | ritex2 book decor1 java2 class |  |
| `rhizovitta_communis` | item | H:SCARS | crucible | java2 class |  |
| `rime_lantern` | block+item | S:SAINTS | craft nbt24 | decor176 |  |
| `rimed_deepslate_brick_slab` | block+item | S:SAINTS | craft+cut nbt1 | tag2 lootcond decor4 |  |
| `rimed_deepslate_brick_stairs` | block+item | S:SAINTS | craft+cut nbt13 | tag2 decor405 |  |
| `rimed_deepslate_brick_wall` | block+item | S:SAINTS | craft+cut nbt4 | tag2 decor83 |  |
| `rimed_deepslate_bricks` | block+item | S:SAINTS | craft nbt13 | craftx3+cutx3 decor99 |  |
| `ring_door` | block | none | nbt4 | java1 class |  |
| `ringmaster_topper` | item | S:CIRCUS | code | tag1 java1 class |  |
| `rite_fragment_inscription` | block+item | H:STATIONS | nbt6 NOLOOT | decor6 java5 class | world-only |
| `rope_pod` | block+item | S:BRINED_COURT | nbt2 unbreakable | decor17 class | set-piece |
| `rot_pyre` | block+item | S:SAINTS | nbt12 unbreakable | tag1 decor28 java2 class | set-piece |
| `rubricators_quill` | item | H:STATIONS | craft | rite book |  |
| `ruinous_enzyme` | item | H:ALCHEMY | lantern+render | alembicx3+craftx3 tag1 adv book inq java3 class |  |
| `ruinous_spores` | item | H:SCARS | craftx2 | crucible+lantern |  |
| `runic_motif_paper` | item | H:SCARS | craft code | craftx25+ritex2 inq java6 |  |
| `saint_relic_hemorath` | item | S:SAINTS | craft | tag2 book inq class |  |
| `saint_relic_putriciel` | item | S:SAINTS | craft | tag2 book inq class |  |
| `saint_relic_seraphae` | item | S:SAINTS | craft | tag2 book inq class |  |
| `saint_relic_velorum` | item | S:SAINTS | craft | tag2 book inq class |  |
| `saint_sarcophagus` | block+item | S:SAINTS | nbt4 | inq decor4 java2 class |  |
| `salt_stained_voyager_log` | item | S:BRINED_COURT | chest | tag1 want inq |  |
| `sanguine_athanor_kit` | item | H:STATIONS | craft code | rite book java1 |  |
| `sanguine_blob` | item | WIP | code | craft java1 class | WIP |
| `sanguine_condenser` | block+item | H:STATIONS | code | ritex2 inq lootcond java3 class |  |
| `sanguine_conduit` | item | H:BLOOD_TOOLS | craft nbt-dead1 code | craft java4 class |  |
| `sanguine_formation` | item | H:MATERIALS | alembic+craft+loom chest mob code | alembicx16+bstructx5+craftx30+gourdfillx4+loom+ritex16 book inq java24 class |  |
| `sanguine_glass` | block+item | H:BUILDING | craft+infuse nbt14 | bstruct+craftx4+humor+ritex9 decor291 java2 |  |
| `sanguine_hound_bridle` | item | S:CIRCUS | craft | rite |  |
| `sanguine_monolith` | block+item | H:STATIONS | rite | rite tag1 inq java2 class |  |
| `sanguine_omen` | block+item | WIP | - | tag1 class | NO-SOURCE |
| `sanguine_pane` | block+item | H:BUILDING | craft nbt5 | humor decor135 |  |
| `sanguine_quintessence` | item | H:MATERIALS | ritex2 chest mob code | craftx6+ritex14 java5 |  |
| `sanguine_salve` | item | H:ALCHEMY | craft chest trade code | craftx2+loom tag1 inq java1 class |  |
| `sanguine_vigil` | block+item | WIP | rite NOLOOT | inq java2 class | WIP |
| `sanguis_lancea` | item | H:ARSENAL | code | java2 class |  |
| `saprovitta_vestigium` | item | H:SCARS | crucible | class |  |
| `sarcodes` | block+item | H:NATURAL | drop:potted_sarcodes wg | alembic java2 class |  |
| `scale_grip` | item | H:MATERIALS | craft | java3 |  |
| `scar_anvil` | item | H:SCARS | craft+scar | java1 class |  |
| `scar_blank` | item | H:SCARS | chest code | scarx25 tag1 inq java5 |  |
| `scar_blight` | item | H:SCARS | craft+scar | inq java1 class |  |
| `scar_blood_honed` | item | H:SCARS | scar | class |  |
| `scar_chimera` | item | H:SCARS | craft+scar | java1 class |  |
| `scar_corona` | item | H:SCARS | craft+scar | java1 class |  |
| `scar_crucible` | item | H:SCARS | craft+scar | java1 class |  |
| `scar_descendence` | item | H:SCARS | craft+scar | java1 class |  |
| `scar_eye` | item | H:SCARS | craft+scar | java1 class |  |
| `scar_feral` | item | H:SCARS | craft+scar | inq java1 class |  |
| `scar_flux` | item | H:SCARS | craft+scar | java1 class |  |
| `scar_glacier` | item | H:SCARS | craft+scar | java1 class |  |
| `scar_halo` | item | H:SCARS | craft+scar | inq java1 class |  |
| `scar_heart` | item | H:SCARS | craft+scar code | loom book inq java3 class |  |
| `scar_marrow` | item | H:SCARS | craft+scar chest | java1 class |  |
| `scar_moon` | item | H:SCARS | craft+scar | loom java1 class |  |
| `scar_oblivion` | item | H:SCARS | craft+scar | java1 class |  |
| `scar_pattern` | item | H:SCARS | craftx24 chest code | adv inq java1 class |  |
| `scar_phoenix` | item | H:SCARS | craft+scar | loom java1 class |  |
| `scar_pyre` | item | H:SCARS | craft+scar | inq java1 class |  |
| `scar_rime` | item | H:SCARS | craft+scar | inq java1 class |  |
| `scar_shade` | item | H:SCARS | craft+scar | loom inq java1 class |  |
| `scar_sol` | item | H:SCARS | craft+scar | java1 class |  |
| `scar_station` | block+item | H:STATIONS | rite nbt1 code NOLOOT | inq decor1 java6 class |  |
| `scar_thorn` | item | H:SCARS | craft+scar | loom inq java1 class |  |
| `scar_transcendence` | item | H:SCARS | craft+scar code | java2 class |  |
| `scar_veil` | item | H:SCARS | craft+scar | java1 class |  |
| `scar_wither` | item | H:SCARS | craft+scar | java1 class |  |
| `scarlet_gorget` | item | S:CIRCUS | craft | rite |  |
| `scarlet_vanity` | block+item | H:STATIONS | rite | book java2 class |  |
| `sclerite_block` | block+item | H:BUILDING | craft | craft |  |
| `sclerotic_oleum` | item | H:ALCHEMY | craft code | armaturex4 inq java1 |  |
| `sclerotium_shell` | block+item | S:BRINED_COURT | nbt1 unbreakable | decor237 | set-piece |
| `scout_field_notes` | item | H:GUIDES | code | book java1 class |  |
| `scrying_dish` | item | H:BLOOD_TOOLS | craft code | craft+loom java3 |  |
| `scrying_podium` | block+item | H:STATIONS | nbt5 code | inq decor5 java6 class |  |
| `sealed_niche` | block+item | S:SAINTS | nbt15 unbreakable | decor262 class | set-piece |
| `self_reflection_mirror` | item | S:UNSTAINED | craft | inq class |  |
| `semi_sentient_construct` | block+item | WIP | rite | java2 class | WIP |
| `serpent_scale` | item | H:DROPS | mob | craftx4 tag1 java1 |  |
| `serpentine_idol` | block+item | WIP | - | java1 class | NO-SOURCE |
| `sheolic_blood_lust_boots` | item | H:ARMOR | armature | armature java4 class |  |
| `sheolic_blood_lust_chest` | item | H:ARMOR | armature | armature java4 class |  |
| `sheolic_blood_lust_helm` | item | H:ARMOR | armature | armature java5 class |  |
| `sheolic_blood_lust_legs` | item | H:ARMOR | armature | armature java4 class |  |
| `silent_archon_boots` | item | S:GARDENS | armature | java4 class |  |
| `silent_archon_chestplate` | item | S:GARDENS | armature | java4 class |  |
| `silent_archon_helm` | item | S:GARDENS | armature | java5 class |  |
| `silent_archon_leggings` | item | S:GARDENS | armature | java4 class |  |
| `silthmere_glaive` | item | S:UNSTAINED | - | inq java1 class | NO-SOURCE |
| `silver_chalice` | item | S:UNSTAINED | craft code | craftx2 book inq java3 |  |
| `smouldering_ash_trail` | block+item | H:NATURAL | craft chest code | bstruct+crucible+loom java9 class |  |
| `sodden_mud_bricks` | block+item | S:SAINTS | nbt2 | tag1 decor165 |  |
| `somatic_loom` | block+item | H:STATIONS | rite nbt1 code | ritex2 inq decor1 java8 class |  |
| `spawn_egg_abhorrent_thought` | item | S:GARDENS | - | - | egg |
| `spawn_egg_antibody` | item | S:GARDENS | - | - | egg |
| `spawn_egg_barbed_urchin` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_blood_drunk_puppeteer` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_blood_lantern_jelly` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_bloody_belly_comb_jelly` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_bog_revenant` | item | H:CREATURES | - | - | egg |
| `spawn_egg_brined_votary` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_chalybeate_snail` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_chitinite` | item | H:CREATURES | - | - | egg |
| `spawn_egg_chiton` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_choir_keeper` | item | S:END | - | - | egg |
| `spawn_egg_chthonian` | item | H:CREATURES | - | - | egg |
| `spawn_egg_chthonian_queen` | item | H:CREATURES | - | - | egg |
| `spawn_egg_circus_acrobat` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_circus_beast_tamer` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_circus_fire_eater` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_circus_knife_thrower` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_circus_ringmaster` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_circus_stilt_walker` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_circus_strongman` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_circus_threadkeeper` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_circus_understudy` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_court_arbor` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_bellwarden` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_braced_elder` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_edge_swimmer` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_gardener` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_head_tender` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_host` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_kindled_elder` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_lamp_colony` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_lightkeeper` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_linesman` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_marrow_tender` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_nursery_tender` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_offering_bearer` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_rooted_elder` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_sleeper` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_tidewarden` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_court_veiled_resident` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_crimson_doe` | item | H:CREATURES | - | - | egg |
| `spawn_egg_desiccant` | item | H:CREATURES | - | - | egg |
| `spawn_egg_enthralled_doll` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_eruptor` | item | S:GARDENS | - | - | egg |
| `spawn_egg_excoriated` | item | S:NETHER | - | - | egg |
| `spawn_egg_facsimile_bat` | item | S:GARDENS | - | - | egg |
| `spawn_egg_facsimile_blaze` | item | S:GARDENS | - | - | egg |
| `spawn_egg_facsimile_donkey` | item | S:GARDENS | - | - | egg |
| `spawn_egg_facsimile_enderman` | item | S:GARDENS | - | - | egg |
| `spawn_egg_facsimile_golem` | item | S:GARDENS | - | - | egg |
| `spawn_egg_facsimile_horse` | item | S:GARDENS | - | - | egg |
| `spawn_egg_facsimile_spider` | item | S:GARDENS | - | - | egg |
| `spawn_egg_facsimile_wolf` | item | S:GARDENS | - | - | egg |
| `spawn_egg_fargone` | item | H:CREATURES | - | - | egg |
| `spawn_egg_fervent_chitinite` | item | H:CREATURES | - | - | egg |
| `spawn_egg_fungling` | item | S:GARDENS | - | - | egg |
| `spawn_egg_gorebound_hulk` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_hagfish` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_harbinger_alchemist` | item | H:CREATURES | - | - | egg |
| `spawn_egg_harbinger_artificer` | item | H:CREATURES | - | - | egg |
| `spawn_egg_harbinger_cicatrix_anchorite` | item | H:CREATURES | - | - | egg |
| `spawn_egg_harbinger_hermit` | item | H:CREATURES | - | - | egg |
| `spawn_egg_harbinger_mnemonist` | item | H:CREATURES | - | - | egg |
| `spawn_egg_harbinger_vicar` | item | H:CREATURES | - | - | egg |
| `spawn_egg_harbinger_votary_wayfarer` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_harbinger_voyager` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_hematic_burrower` | item | H:CREATURES | - | - | egg |
| `spawn_egg_hemojelly` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_hemolymphopoda` | item | H:CREATURES | - | - | egg |
| `spawn_egg_hunter_fungus` | item | S:GARDENS | - | - | egg |
| `spawn_egg_ice_fish` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_lantern_tick` | item | H:CREATURES | - | - | egg |
| `spawn_egg_leech` | item | H:CREATURES | - | - | egg |
| `spawn_egg_luminal_cicada` | item | H:CREATURES | - | - | egg |
| `spawn_egg_lump_of_thought` | item | S:GARDENS | - | - | egg |
| `spawn_egg_marrow_spitter` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_mnemonic_whale` | item | S:BRINED_COURT | - | inq | egg |
| `spawn_egg_mnemonist_puppet` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_morphling_polyp` | item | H:CREATURES | - | - | egg |
| `spawn_egg_mortarbound` | item | H:CREATURES | - | - | egg |
| `spawn_egg_myelin_borer` | item | S:END | - | - | egg |
| `spawn_egg_naeglerophaeon` | item | S:END | - | - | egg |
| `spawn_egg_osteophage` | item | S:NETHER | - | - | egg |
| `spawn_egg_peacock_spider` | item | WIP | - | - | egg |
| `spawn_egg_pelagic_herring` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_phlegethontic_bombardier` | item | S:NETHER | - | - | egg |
| `spawn_egg_phlegethosoma` | item | S:NETHER | - | - | egg |
| `spawn_egg_prism_cuttle` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_pyrosome` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_sanguine_hound` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_scarlet_mummer` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_scarlet_serpent` | item | H:CREATURES | - | - | egg |
| `spawn_egg_siphonophore` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_thirster` | item | H:CREATURES | - | - | egg |
| `spawn_egg_tooth_pecks` | item | H:CREATURES | - | - | egg |
| `spawn_egg_unstained_acolyte` | item | S:UNSTAINED | - | - | egg |
| `spawn_egg_unstained_guardian` | item | S:UNSTAINED | - | - | egg |
| `spawn_egg_unstained_scout` | item | S:UNSTAINED | - | - | egg |
| `spawn_egg_unstained_zealot` | item | S:UNSTAINED | - | - | egg |
| `spawn_egg_vampire_bat` | item | S:NETHER | - | - | egg |
| `spawn_egg_vampire_squid` | item | S:BRINED_COURT | - | - | egg |
| `spawn_egg_veinwing_vulture` | item | S:CIRCUS | - | - | egg |
| `spawn_egg_venom_rib_centipede` | item | H:CREATURES | - | - | egg |
| `spawn_egg_venous_strider` | item | H:CREATURES | - | - | egg |
| `spawn_egg_verdigris_moth` | item | S:UNSTAINED | - | - | egg |
| `spawn_egg_will` | item | H:CREATURES | - | - | egg |
| `spawn_mark` | block+item | S:SAINTS | nbt3 unbreakable | decor18 java1 class | set-piece |
| `specimen_jar` | block+item | H:STATIONS | craft chest nbt13 nbt-item code | want decor36 java8 class |  |
| `spider_husk_effigy` | item | WIP | craft | class | WIP |
| `spore_sac` | item | H:DROPS | alembicx3+craftx3 chest mob code | alembic+craftx26+loomx4+render+ritex2 tag1 want inq java4 |  |
| `spore_shelf` | block+item | S:SAINTS | nbt21 | tag1 decor141 java1 class |  |
| `sporite_crystal` | block+item | S:GARDENS | drop:mycelial_synapse wg code | bstruct tag1 java10 |  |
| `sporitic_thurible` | item | H:BLOOD_TOOLS | rite code | inq java1 class |  |
| `stained_church_map` | item | H:GUIDES | code | java1 class |  |
| `stillwater_condenser` | block+item | S:UNSTAINED | craft | book java2 class |  |
| `stinkhorn_fungus` | block+item | H:NATURAL | drop:potted_stinkhorn_fungus wg code | alembic+craft java5 class |  |
| `structure_scanner` | item | WIP | - | class | NO-SOURCE |
| `structure_spawner` | item | WIP | - | class | NO-SOURCE |
| `sunburst_bloom` | block+item | S:BRINED_COURT | code unbreakable | tag1 java1 class | set-piece |
| `sunburst_crust` | block+item | S:BRINED_COURT | nbt1 unbreakable | tag1 decor631 | set-piece |
| `sunburst_frond` | block+item | S:BRINED_COURT | nbt1 unbreakable | tag1 decor96 class | set-piece |
| `sunburst_lamp` | block+item | S:BRINED_COURT | nbt1 unbreakable | tag1 decor3 class | set-piece |
| `suspended_blood_crystal` | block+item | H:NATURAL | nbt-dead2 NOLOOT | rite tag2 java1 class | NO-SOURCE |
| `suspended_cleansed_blood_crystal` | block+item | S:UNSTAINED | NOLOOT | ritex8 inq java1 class | NO-SOURCE |
| `suspended_vivianite` | block+item | H:NATURAL | NOLOOT | java1 class | NO-SOURCE |
| `swollen_leech` | item | H:DROPS | mob code | alembic+rite+smelt java1 |  |
| `synaptic_node` | block+item | S:END | craft code | rite tag2 java14 |  |
| `synaptic_step` | item | hidden | - | craft class | NO-SOURCE |
| `talaromyces_minus` | item | H:SCARS | crucible | java2 class |  |
| `tears_of_silthmere` | item | S:UNSTAINED | craft+ritex2 code | alembic tag1 adv book inq java5 |  |
| `telson` | item | H:DROPS | mob | craft tag1 |  |
| `tendon_line` | item | S:CIRCUS | craft | craft+ritex3 class |  |
| `tengu_mask` | item | H:ARMOR | craft | armature inq |  |
| `terrestrial_speculum` | item | H:BLOOD_TOOLS | craft | java3 class |  |
| `tidepool_anemone` | block+item | S:BRINED_COURT | code | java2 class |  |
| `tincture_carrion_metabolism` | item | H:ALCHEMY | alembic | class |  |
| `tincture_carrion_metabolism_jug` | item | H:ALCHEMY | alembic | class |  |
| `tincture_coursing_legs` | item | H:ALCHEMY | alembic | class |  |
| `tincture_coursing_legs_jug` | item | H:ALCHEMY | alembic | class |  |
| `tincture_enduring_viscera` | item | H:ALCHEMY | alembic | class |  |
| `tincture_enduring_viscera_jug` | item | H:ALCHEMY | alembic | class |  |
| `tincture_hushed_gait` | item | H:ALCHEMY | alembic | class |  |
| `tincture_hushed_gait_jug` | item | H:ALCHEMY | alembic | class |  |
| `tincture_laboring_arms` | item | H:ALCHEMY | alembic | class |  |
| `tincture_laboring_arms_jug` | item | H:ALCHEMY | alembic | class |  |
| `tincture_predatory_eyes` | item | H:ALCHEMY | alembic | class |  |
| `tincture_predatory_eyes_jug` | item | H:ALCHEMY | alembic | class |  |
| `tincture_sanguine_fists` | item | H:ALCHEMY | alembic | book class |  |
| `tincture_sanguine_fists_jug` | item | H:ALCHEMY | alembic | class |  |
| `tincture_second_pulse` | item | H:ALCHEMY | alembic | class |  |
| `tincture_second_pulse_jug` | item | H:ALCHEMY | alembic | class |  |
| `tome_of_the_unstained` | item | hidden | - | - | NO-SOURCE |
| `toxicognath` | item | H:DROPS | mob | craft tag1 |  |
| `troupe_rigging` | block+item | S:CIRCUS | craft trade nbt1 | decor1 class |  |
| `umbral_enzyme` | item | H:ALCHEMY | lantern+render | alembicx2+craftx4 tag1 adv book inq java3 class |  |
| `umbral_spores` | item | H:SCARS | craftx2 | craft+crucible+lantern java1 |  |
| `unsigned_ancestral_ledger` | item | WIP | ritex2 | java1 class | WIP |
| `unstained_boots` | item | S:UNSTAINED | craft | inq class |  |
| `unstained_chestplate` | item | S:UNSTAINED | craft | inq class |  |
| `unstained_helm` | item | S:UNSTAINED | craft | inq class |  |
| `unstained_leggings` | item | S:UNSTAINED | craft | inq class |  |
| `unstained_podium` | block+item | S:UNSTAINED | bstruct nbt1 code | rite inq decor1 java4 class |  |
| `unstained_shield` | item | S:UNSTAINED | craft code | inq java1 class |  |
| `unstained_warhammer` | item | S:UNSTAINED | craft | inq class |  |
| `vacant_effigy` | block+item | H:STATIONS | rite code | ritex2 lootcond java6 class |  |
| `vascular_poultice` | item | WIP | craft trade | rite tag1 inq class | WIP |
| `vascular_status_gauge` | item | WIP | craft | loom inq class | WIP |
| `vein_spider` | item | WIP | craft | java1 class | WIP |
| `veins_pattern` | item | H:GUIDES | craft | class |  |
| `veinwing_harness` | item | S:CIRCUS | craft | rite |  |
| `velorum_mask` | item | S:SAINTS | craft | armature inq |  |
| `venous_basin` | block+item | S:SAINTS | craft nbt11 | decor42 class |  |
| `venous_pinion` | item | H:DROPS | mob | craftx2 tag1 java1 |  |
| `venous_stone` | block+item | H:BUILDING | infuse lootmod wg nbt35 code | bstructx6+craftx6+humor+ritex5 tag6 adv decor1778 java23 |  |
| `venous_stone_slab` | block+item | H:BUILDING | craft adv nbt-dead2 | adv |  |
| `venous_stone_stairs` | block+item | H:BUILDING | craft adv | adv |  |
| `venous_stone_wall` | block+item | H:BUILDING | craft | tag2 |  |
| `venous_strider_sabatons` | item | H:ARMOR | craft | java2 class |  |
| `vent_offering_basin` | block+item | S:BRINED_COURT | nbt1 code unbreakable | decor4 java2 class | set-piece |
| `verdigris_lattice` | block+item | S:UNSTAINED | craft | java1 class |  |
| `vestment_of_the_final_molt` | item | S:UNSTAINED | code | java1 class |  |
| `vial_centrifuge` | block+item | H:STATIONS | bstruct nbt1 | ritex2 book inq decor1 java5 class |  |
| `vial_rack` | item | H:BLOOD_TOOLS | craft code | inq java2 class |  |
| `vicars_consecration_kit` | item | H:STATIONS | craft code | rite inq java2 |  |
| `virid_salis_trail` | block+item | S:UNSTAINED | rite nbt2 code | ritex3 book decor104 java4 class |  |
| `visceral_mirror` | block+item | WIP | rite NOLOOT | java2 class | WIP |
| `vitality_chalice` | item | WIP | craft code | loom java1 | WIP |
| `vivacious_enzyme` | item | H:ALCHEMY | lantern+render mob code | alembicx2+craftx5+rite tag2 adv book inq java4 class |  |
| `vivacious_spores` | item | H:SCARS | craftx3 mob | alembic+craft+crucible+lantern java1 |  |
| `vivianite_cluster` | item | H:MATERIALS | chest mob code | craftx2+loomx4 java4 |  |
| `vivianite_glass` | block+item | H:BUILDING | craft | craftx2 tag1 |  |
| `vivianite_pane` | block+item | H:BUILDING | craft | - | NO-USE |
| `vivianite_scalpel` | item | H:ARSENAL | craft chest | craft book java5 class |  |
| `void_eye_organ` | item | WIP | craft | class | WIP |
| `voidtouched_vessel` | block+item | WIP | craft | - | NO-USE WIP |
| `ward_anchor` | block | none | nbt4 | java2 class |  |
| `warden_bloom` | block | none | code | java3 class |  |
| `warp_chair` | block+item | H:STATIONS | rite nbt1 code | decor1 java5 class |  |
| `warp_chair_filler` | block | none | nbt1 code | java6 class |  |
| `wax_cylinder` | item | H:BLOOD_TOOLS | craft | craft+ritex2 book java5 |  |
| `white_humor_block` | block | none | code | java2 |  |
| `witness_devotee` | block | none | nbt1 | java1 class |  |
| `witness_organ` | block+item | WIP | craft NOLOOT | java2 class | WIP |
| `witness_stone` | block+item | S:SAINTS | nbt2 unbreakable | decor8 java1 class | set-piece |
| `worn_vow_fitting` | item | H:ARSENAL | code | java3 class |  |
| `zombie_husk_effigy` | item | WIP | craft | class | WIP |
