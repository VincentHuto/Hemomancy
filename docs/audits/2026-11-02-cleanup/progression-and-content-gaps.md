# Audit: progression blockers and content gaps (2026-11-02)

Scope: the Harbinger ladder D0 to D8, the Unstained path, advancements, player-facing promises, station upgrades,
Cardinal Rites and Working crafts, creative-only content and soft blockers. Code and data only; nothing was built or run.

**Summary.** 20 findings: 1 Blocker, 3 High, 7 Medium, 9 Low. The Harbinger rank ladder itself holds: every rank rite,
floor, Working-craft subject and medium, and 13 of the 14 station-upgrade rites trace back to a survival source. The one
Blocker is on the Unstained side. Lethean Poppy items come only from an Acolyte trade, Unstained NPCs refuse anyone at D1
or higher, and wild poppies drop nothing when broken. So the advertised "cure at any degree" route (and the founder's
Severed Covenant) cannot be built by anyone who has been initiated. The High findings are three single-item dead ends:
the Suspended Cleansed Blood Crystal (three Unstained rites), the Suspended Blood Crystal (Forge Masterwork) and the Living
Syringe (a one-time reward with no replacement). Most Medium findings come from WIP-tab content leaking into live
progression, or live content filed as WIP. The Unstained path has no playthrough coverage (`docs/PLAYTHROUGH_TESTS.md`
line 8), and that is where the hard block was found.

**Method.** Every registered item and block was taken from `ItemInit`/`BlockInit` (checked against
`src/gameTest/resources/frozen/registry-ids.txt`: 832 of 850 items, the 18 missing are Court spawn eggs). A source graph
was built from all 656 recipes (key letters not used in their pattern were ignored), all loot tables, loot modifiers, the
56 NPC trades, every hemomancy id found in worldgen JSON and in the decompressed structure NBTs (paired with block loot
tables), and Java `new ItemStack(ItemInit.x)` grants. It was then closed under recipes until nothing changed. Each
candidate was then read by hand. Code grants were taken as sources without proving their conditions, and natural mob
spawning was not modelled, so a "source" here means a path exists, not that it is easy. Items already covered by
`docs/OPEN_QUESTIONS.md` are not reported again; see the last section.

---

## Findings

| Id | Sev | Claim | Conf. | Action | User call |
| --- | --- | --- | --- | --- | --- |
| PG-1 | Blocker | Lethean Poppy items are unobtainable at D1+, so a Harbinger cannot be baptised and a founder cannot perform the Severed Covenant | Verified | fix | Yes |
| PG-2 | High | Suspended Cleansed Blood Crystal has no survival source; Pale Consecration, Lethe Covenant and Lethean Font cannot be built | Verified | fix or cull | Yes |
| PG-3 | High | Suspended Blood Crystal has no loot table or recipe; Enduring Pattern (Forge Masterwork, D7) can never complete | Verified | fix | Minor |
| PG-4 | High | The Living Syringe is a one-time reward with no recipe, loot or reissue; losing it ends restricted sampling, including Succession donations | Verified | fix | Minor |
| PG-5 | Medium | The Mnemonist Puppet Ordeal (D5) is unreachable: its component recipe needs an Engram Block item that never drops | Verified | fix | No |
| PG-6 | Medium | Eight player-made blocks (Covenant Throne, Mycelial Lantern, Scar Station and others) have no block loot table and vanish when broken | Verified | fix | No |
| PG-7 | Medium | The Drudge system is WIP and its Electrode has no source, but the Semi-Sentient Construct Working craft is live and on the Rites & Crafts map | Verified | decide | Yes |
| PG-8 | Medium | 22 WIP-tab entries are survival-obtainable (and 6 more are made by code); 8 of them feed live recipes, rites or trades | Verified | decide | Yes |
| PG-9 | Medium | The Morphling Cradle is documented and taught as a live station but cannot be obtained | Verified | decide | Yes |
| PG-10 | Medium | The Sanguine Omen, the only rite-bound member, cannot be obtained, so rite spaces have no survival payoff | Verified | decide (D-10) | Yes |
| PG-11 | Medium | `initiate_rite` still calls itself "the third rite ... granting formal entry into the Incarnadine Fane" but grants no degree | Verified | cull or rewrite | Yes |
| PG-12 | Low | Hyphae never drop, so three Spore Sac recipes are dead; bonemealing Hyphae makes tall grass | Verified | fix | No |
| PG-13 | Low | The Material Atlas and item inquiries list 13 items with no survival source | Verified | decide | Yes |
| PG-14 | Low | Dead worldgen content: the blood tower NBTs and their chest loot, eight `lowtide_ruin` NBTs, and the Silver Bells Tower feature (empty biome tag) | Verified | cull or place | No |
| PG-15 | Low | A test weave recipe ships: 1 Sanguine Formation plus 7 enzymes makes 8 formations | Verified | cull | No |
| PG-16 | Low | Legacy registrations kept against the "mod not live" rule (Tome of the Unstained alias, 27 retired memory items); the docs still describe the Tome as crafted | Verified | cull | No |
| PG-17 | Low | The `raw_clay_flask` recipe advancement names a recipe that does not exist and uses the pre-1.21 predicate key | Verified | fix | No |
| PG-18 | Low | The founder cure route is fragile even after PG-1: kill-credit rule, no map at D5 to D6, and a config can remove the biome | Verified (code) / Suspected (impact) | decide | Yes |
| PG-19 | Low | Clarity Ascension silently needs 52 End-stone-tier blocks; no Unstained text mentions the End | Verified | expand | No |
| PG-20 | Low | An unused lang key reads "The Red Taxonomy reward has not been decided yet." | Verified | cull | No |

---

### PG-1 (Blocker): Lethean Poppies are unobtainable at D1+, so the Harbinger-to-Unstained cure route is closed

- The Lethean Baptism floor is 5 `hemomancy:lethean_poppy` blocks and 4 Cleansed Stone:
  `src/main/resources/data/hemomancy/recipe/cardinal_rite/lethean_baptism.json:26-27`. The founder's Severed Covenant
  also lays poppies: `.../recipe/cardinal_rite/severed_covenant.json:14`.
- The only source of the poppy item is the Acolyte trade (4 wool for 2 poppies):
  `src/main/resources/data/hemomancy/npc_trade/acolyte/wool.json:14`. No recipe, chest loot or structure places the item
  (the church chest is `.../loot_table/chests/unstained_church.json`: candle, paper, bottle, copper, wool, Hemolytic
  Solution, Liber Immaculatus).
- Every Unstained NPC refuses all trade at D1 or higher:
  `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/trade/NpcTradeSides.java:65-70`
  (`refuses = Side.UNSTAINED && hasDegree(player, 1)`). The Harbinger degree is only reset by the cure itself
  (`common/capability/PathMutualExclusionHelper.java:117-120`), which comes after the baptism.
- Wild poppies drop nothing: `lethean_poppy` (`common/init/BlockInit.java:949`) has no
  `loot_table/blocks/lethean_poppy.json` (only `potted_lethean_poppy.json` and `lethean_poppy_wreath.json` exist).
  Right-clicking a bloomed poppy gives Lethean Dew, never the flower (`common/block/unstained/plant/LetheanPoppyBlock.java:53-56`).
  Poppies cannot be moved, so the 3x3 plus pattern cannot be built around wild ones.
- The Zealot's accept options give only Hemolytic Solution and the Liber Immaculatus
  (`common/entity/npc/dialogue/route/ZealotDialogueEvents.java:58-77`).
- The design promises this route: "Unstained Zealots can offer ordinary purification at any Harbinger degree"
  (`docs/HEMOMANCY_REFERENCE.md:1603`), and baptism starting purity is tuned per degree (D1 = 40, D5+ = 0, line 1611).
- Knock-on effects for the same players: the first Pale Distillate needs a poppy (`recipe/pale_distillate.json`; the
  ghost-pipe distillation is Retort-only, `common/tile/harbinger/crafting/GhastlyAlembicBlockEntity.java:291`, so the first
  Pallid Retort is gated too); poppy wreaths (altar purity) need 4; the novitiate vow needs 4
  (`common/mission/unstained/UnstainedObservances.java:130,151-153`).
- A D0 player with active blood is unaffected: the Acolyte will trade.
- Action: fix. Options: a self-drop loot table for the poppy (bloomed only, or shears/silk touch); add 5 poppies to the
  Zealot's starter supply; or exempt players who accepted purification from the D1 refusal (the refusal is ruling Q-2,
  default D-15). Needs the user's pick.

### PG-2 (High): Suspended Cleansed Blood Crystal has no source; three Unstained rites cannot be built

- `suspended_cleansed_blood_crystal` (`common/init/BlockInit.java:1262`): no recipe, no loot table, not placed by any
  structure or feature, and no code places it (only `BlockEntityInit` references it).
- Rites that lay it in their pattern: Pale Consecration (U2, `recipe/cardinal_rite/pale_consecration.json:33`), Lethe
  Covenant (U8, `lethe_covenant.json:45`), Lethean Font (U8, the "pinnacle" rite, `lethean_font.json:51`;
  `docs/HEMOMANCY_REFERENCE.md:2520,2526,2538`).
- Player-facing promises: the atlas shows it at purity 25 (`client/screen/skilltree/shared/MaterialsData.java:1531`), the
  lang describes it as "A blood crystal purified through Unstained rites" (`assets/hemomancy/lang/en_us.json:8392`), and the
  Zealot has an inquiry for it (`data/hemomancy/dialogue_inquiry/zealot/hemomancy/suspended_cleansed_blood_crystal.json`).
- Not a block on Enlightenment: Moon-Washed Copper is repeatable (+5/+10 clarity) and the stage milestone grants
  `enlightened_seeker` and the Vestment (`common/event/UnstainedMilestoneHandler.java:136-137`).
- Clarity Ascension defines the crystal as key `C` but never uses it in its pattern (`clarity_ascension.json`), so it is not
  affected.
- Action: fix (a White Humor purification of the Suspended Blood Crystal, plus a self-drop loot table) or cull the three
  rites. Unstained rite work is parked (D-11, memory note), so this needs the user's call.

### PG-3 (High): Suspended Blood Crystal has no drop; the Forge Masterwork upgrade is unreachable

- Enduring Pattern (Resonant Forge Masterwork, D7) offers `hemomancy:suspended_blood_crystal`:
  `recipe/cardinal_rite/enduring_pattern.json:22`; `docs/STATION_UPGRADES.md:34,69`.
- The block (`common/init/BlockInit.java:1258`) has no recipe and no loot table. It is placed in
  `structure/harbinger_outpost.nbt` (and one other outpost piece), but breaking it drops nothing.
- The Artificer still issues the free `master_cam_kit` at D7, and the Liber tells the player to bring the crystal
  (`data/hemomancy/books/libersanguinium/resonant_forge/pages/upgrades.json:8`).
- Action: fix. Add a self-drop loot table and a recipe (for example, a blood crystal grown on an Alembic plus a formation),
  or swap the offering. Picking the offering is a small user call.

### PG-4 (High): The Living Syringe is irreplaceable

- Its only source is the one-time First Separation reward (`common/init/ItemInit.java:647`, granted from
  `common/mission/alchemist/FirstSeparationAssignment.java`). No recipe, loot, trade or reissue exists.
  `PendingDelivery` protects delivery, not later loss (lava, despawn, a death drop).
- 114 of 153 blood profiles set `requires_living_syringe: true` (`data/hemomancy/blood_profiles/`), including all 8
  professional Harbingers (`harbinger_vicar.json`, `harbinger_alchemist.json`, ...), all 10 Circus and 18 Court
  profiles, Chitinite and Chiton. Loose vials refuse these (`common/item/harbinger/BloodSamplingRules.java:18`).
- Hematic Succession donations from professionals need it (`common/entity/npc/dialogue/SuccessionDialogue.java:110-116`).
  Without the syringe, the Succession route that replaces lost NPCs cannot start.
- Other one-time items already have reissue rules (Memory of Vesper:
  `common/capability/player/harbinger/degree/SilentiumAscensionRules.java:13`; Concentrated Blood). The syringe has none.
- Action: fix. Either a crafting recipe (the Vial Rack already has one, `recipe/vial_rack.json`), or an Alchemist reissue
  when none is carried.

### PG-5 (Medium): The Mnemonist Puppet Ordeal is unreachable

- The ordeal offers `mnemonist_cradle` and `memory_thread`:
  `recipe/cardinal_rite/puppeteer_trial_mnemonist_puppet.json:13,25`. It is the seventh Circus lesson
  (`common/mission/circus/CircusCurriculum.java:17`), and the map icon is the cradle
  (`client/screen/skilltree/shared/HarbingerRecipeMapDefinitions.java:120`).
- The cradle recipe needs `hemomancy:engram_block` (`recipe/mnemonist_cradle.json:6`).
- Engram blocks are only stamped or scratched into the world (`common/item/harbinger/tool/EngramStampItem.java:47`,
  `common/event/ScratchEngramHandler.java:101`). There is no `loot_table/blocks/engram_block.json`, `EngramBlock` overrides
  no drops, and the item is hidden from creative (`common/init/BlockInit.java:1389`).
- Tests pin the recipe text (`src/test/.../PuppeteerIntegrationSourceTest.java:254`); the journey fixture places engram
  blocks directly (`src/gameTest/.../HemoJourneyFixtures.java:1442`), so no test plays this step.
- Action: fix. Replace `E` with an obtainable ingredient (the Engram Stamp, or a scribed block that drops), or give engram
  blocks a self-drop.

### PG-6 (Medium): Eight player-made blocks vanish when broken

None of these has a `loot_table/blocks/<id>.json`, and `FillerBlock` breaks the master through its loot table
(`common/block/shared/FillerBlock.java:228,250`), so breaking any part destroys the block with nothing dropped:

| Block | Made by | Registered |
| --- | --- | --- |
| `covenant_throne` | Working craft D6 (`recipe/cardinal_rite/working/covenant_throne.json`); D7 Living Covenant proof | `common/init/BlockInit.java:1213` |
| `fungal_implantation_pylon` | Working craft D4 (becomes the Fungal Podium) | `BlockInit.java:1188` |
| `mycelial_lantern` | Working craft D3 (First Culture) | `BlockInit.java:1125` |
| `scar_station` | Working craft D4 `runic_chisel_station` (Vein-Mason chapter) | `BlockInit.java:1066` |
| `sanguine_vigil` | Working craft D6 `vascular_effigy` | `BlockInit.java:1234` |
| `visceral_mirror` | Working craft D4 | `BlockInit.java:1269` |
| `hematic_stake` | Blood Structure (`recipe/blood_structure/hematic_stake.json`) | `BlockInit.java:1196` |
| `witness_organ` | Crafting (`recipe/witness_organ.json`) | `BlockInit.java:1130` |

The other Working-craft results do drop themselves (`somatic_loom`, `warp_chair`, `mason_effigy`, `sanguine_monolith`,
`mnemonic_reliquary`, `resonant_forge`, ...). The crafts can be repeated, so this is costly rather than blocking.
Action: fix (self-drop tables, with `copy_state` where a stage exists).

### PG-7 (Medium): WIP Drudge content leaks through a live Working craft

- The Drudge system is "`WIP — post-alpha` ... not part of the first-release gameplay promise"
  (`docs/HEMOMANCY_REFERENCE.md:2867`). Birthing needs the Drudge Electrode (same file, 2993-3000).
- `drudge_electrode` and `drudge_submission_device` (`common/init/ItemInit.java:420,424`) have no recipe, loot or trade.
- The Semi-Sentient Construct is still a live D3 Working craft (`recipe/cardinal_rite/working/semi_sentient_construct.json`,
  result `hemomancy:semi_sentient_construct`) and sits in the Rites & Crafts "Constructs/Effigies" family
  (`client/screen/skilltree/shared/HarbingerRecipeMapDefinitions.java:74`). A player can spend a D3 rite to make a block
  that does nothing.
- Action: decide. Either withhold the SSC craft (and its map node) until Drudges ship, or add recipes for the Electrode
  and DSD.

### PG-8 (Medium): WIP-tab filing contradicts live progression

`common/init/CreativeTabInit.java:59-102` keeps these out of the main tab, but each is survival-obtainable and most are
used by live content:

| WIP entry (line) | Live source | Live use |
| --- | --- | --- |
| `unsigned_ancestral_ledger` (67) | Bloodline Founding and Recall rite results | signed ledgers join lines (D5) |
| `vascular_poultice` (69) | crafting, Vicar `chapel_shards` trade | Sanguine Vigil Working-craft offering |
| `memory_thread` (70) | crafting, two Mnemonist trades | Mnemonist Puppet Ordeal offering |
| `vitality_chalice` (75) | crafting | Exsanguinate weave input; HR §5.1 emergency restore |
| `vascular_status_gauge` (79) | crafting | Hemolymphal Pulse weave input |
| `sanguine_blob` (68) | `initiate_rite` reward (`common/rite/harbinger/CardinalRiteCompletionEffects.java:232`) | Hearty Compass recipe |
| `lethean_brew` (86) | Guardian trade, distillation | altar purity (HR §38.1) |
| `hematic_suture_needle` (66) | crafting, Artificer trade | direct blood routing ("Implemented", HR §38) |
| `gourdvine_tap` (93) | crafting | described as a D2 item in HR §1 |
| `semi_sentient_construct`, `visceral_mirror`, `sanguine_vigil` (88, 96, 94) | Working crafts | Rites & Crafts map |
| `crucible_of_nether_ichor`, `voidtouched_vessel`, the three husk effigies, `vein_spider`, `void_eye_organ`, `witness_organ`, `blood_thrall_effigy`, `draught_of_still_mercy` | recipes | none |
| the five `echo_of_*` organs, `blood_bolt` | made by the Visceral Mirror, the Living Crossbow | organ effects |

Action: decide, one row at a time. Move finished content into its `CreativeTabSection`; take recipes and trades away from
unfinished content.

### PG-9 (Medium): The Morphling Cradle is taught but cannot be obtained

- Documented as live: `docs/HEMOMANCY_REFERENCE.md:2712-2722`, `docs/MORPHLING_STRAINS.md:161-163`; strains carry a
  `cradlePrimal` flag; the Liber teaches it ("The Morphling Cradle hosts one Morphling outside the staff ...",
  `data/hemomancy/books/libersanguinium/the_hematic_order/pages/morphling_cradle_and_nectar.json`); the atlas lists it at
  D3 (`client/screen/skilltree/shared/MaterialsData.java:190`).
- `morphling_cradle` (`common/init/BlockInit.java:1079`) has a self-drop loot table, but no recipe, craft, trade or
  placement. It is on the WIP tab only (`CreativeTabInit.java:91`).
- Action: decide. Add a Working craft or recipe, or mark the Liber page and atlas entry as not yet available.

### PG-10 (Medium): The only rite-bound member is unobtainable

- `hemomancy:sanguine_omen` is the only entry in `data/hemomancy/tags/block/rite_bound/world.json:4`; it was "chosen
  because it is decorative and costs no progression" (`docs/CARDINAL_RITES.md:81,90`).
- The block (`common/init/BlockInit.java:1183`) has no recipe, loot source or placement, and is WIP-tab only
  (`CreativeTabInit.java:95`). The atlas still lists it at D4 (`MaterialsData.java:1007`).
- So rite spaces (`common/rite/zone/RiteSpace*`, HR §5.3 "Rite spaces") have no survival-visible effect at all.
- Action: decide together with D-10. At least make the Omen obtainable, or say that rite spaces are inert for now.

### PG-11 (Medium): `initiate_rite` advertises a rank it no longer grants

- Name and description: "Rite of the Incarnadine Fane" / "The third rite of the Hematic Order, granting formal entry into
  the Incarnadine Fane." (`assets/hemomancy/lang/en_us.json:3076,6776`).
- The recipe is `required_degree: 2` with `rankup: false` (`recipe/cardinal_rite/initiate_rite.json`). D3 is reached by
  First Separation, an Alembic distillation, Concentrated Blood and sleep (`docs/HEMOMANCY_REFERENCE.md` §5.2 line 27).
  Its only effect is a WIP Sanguine Blob (`common/rite/harbinger/CardinalRiteCompletionEffects.java:231-236`).
- It is shown on the Rites & Crafts map with a Liber icon (`client/screen/skilltree/shared/HarbingerRecipeMapDefinitions.java:79`) and unlocks
  the ORDER_BELIEFS Liber entry (`common/capability/player/shared/knowledge/discovery/LiberEntryDefinitions.java:133`). A D2
  player reading it is told the wrong way to D3.
- Action: cull, or rename and re-describe it as what it does (and decide the Blob and Hearty Compass chain).

### PG-12 (Low): Hyphae never drop

- `hyphae` (`common/init/BlockInit.java:627`) has no `loot_table/blocks/hyphae.json` (`hyphae_block.json` is a different
  block). It generates (`worldgen/configured_feature/patch_hyphae.json`) and grows from Infested Wood
  (`common/block/harbinger/plant/InfestedWoodBlock.java:44`).
- Dead recipes: `recipe/spore_sac.json`, `spore_sac_from_enzyme_primer.json`, `spore_sac_from_vivacious_spores.json`.
  The atlas lists Hyphae at D4 (`MaterialsData.java:1292`).
- `HyphaeBlock.performBonemeal` (`common/block/harbinger/plant/HyphaeBlock.java:52`) is copied from vanilla grass and grows
  Tall Grass.
- Spore Sacs have other sources (puffball distillation, fungling drops, the Alchemist trade), so nothing is blocked.
- Action: fix (drop with shears) and correct the bonemeal.

### PG-13 (Low): The atlas and item inquiries list items with no survival source

From `client/screen/skilltree/shared/MaterialsData.java`: `hemorath_rib` (942, D2; HR §20.2 line 3142 lists it as the
best gourd tier, and its name "Rib of Hemorath" sits beside a different item, `saint_relic_hemorath` "Hemorath's Rib"),
`silthmere_glaive` (1738, purity 50; Guardian inquiry `dialogue_inquiry/guardian/hemomancy/silthmere_glaive.json`),
`suspended_vivianite` (150), `gilded_venous_stone` (256), `bog_body` (205, set piece), `humane_idol` (982),
`serpentine_idol` (1012), `blood_basin` (155), `blood_pylon` (160), `blood_trial_altar` (200) (the last three are
legacy trial-room pieces, see M-6), `altar_of_cleansing` (1521, a church set piece with no drop), and
`suspended_cleansed_blood_crystal` (1531, PG-2). Action: decide per item (add a source, or keep it out of the atlas and
inquiries until it has one).

### PG-14 (Low): Dead worldgen content

- `structure/tower/blood_tower_core.nbt` and `blood_tower_top_1.nbt` are referenced only by the dormant
  `common/data/gen/DynastyCastlePieces.java`; no template pool, structure JSON or live Java places them. The chest loot
  they name, `loot_table/chests/blood_tower.json`, is therefore unreachable.
- `structure/lowtide_ruin/*.nbt` (8 files): the string `lowtide_ruin` appears nowhere in code or data.
- `neoforge/biome_modifier/add_silver_bells_tower.json` targets `tags/worldgen/biome/silver_bells_tower_spawnlist.json`,
  which is empty.
- Action: cull or place. The legacy `mausoleum` structure is already M-6.

### PG-15 (Low): A test recipe ships

`data/hemomancy/recipe/memory_weaving/recaller_test.json`: 1 Sanguine Formation, seven enzymes and 350 mL weave into
8 Sanguine Formations. It loads like any recipe and shows in JEI. Action: cull.

### PG-16 (Low): Legacy registrations and stale doc lines

- `tome_of_the_unstained` is kept as a "Legacy registry alias retained so existing saves do not lose old stacks"
  (`common/init/ItemInit.java:1068-1073`). The project rule is no aliases while the mod is not live.
- 20 retired memory items (`common/capability/player/harbinger/manip/ManipulationRetirementRules.java`) and 7 legacy
  living-weapon memories (`CreativeTabInit.java` `isLegacyLivingWeaponMemory`) are still registered. The
  `inherited_memory` advancement still lists the 7.
- Docs still describe the Tome as a crafted item and as the `path_of_purity` trigger (`docs/HEMOMANCY_REFERENCE.md:2550,3268,4953`).
  The real trigger is the Liber Immaculatus (`advancement/hemomancy/path_of_purity.json`).
- Action: cull and fix the docs.

### PG-17 (Low): A recipe advancement points at a missing recipe

`data/hemomancy/advancement/recipe/hemomancy/raw_clay_flask.json` rewards and checks `hemomancy:raw_clay_flask`, which
does not exist (flasks come from HutosLib). It also uses `"item":` instead of the 1.21 `"items":` key. Action: delete or
repoint it.

### PG-18 (Low): The founder cure route is fragile even after PG-1

- Annetta's severance unlock goes only to a founder who lands the kill personally
  (`common/entity/boss/annetta/StainedPriestessEntity.java:817-823`, `source.getEntity() instanceof ServerPlayer`). A kill by
  a puppet, a thrall or another player gives nothing, and the founder has to find another Broken Church.
- No pointer reaches a D5 to D6 founder: the Vicar gives the Stained Church map from D7, the Zealot only after Clarity
  (`common/entity/npc/dialogue/route/VicarDialogueEvents.java:233-240`).
- The Broken Church generates only in the Overworld `fungal_gardens` biome (`tags/worldgen/biome/has_structure/broken_church`).
  With `enableOverworldFungalGardensRegion = false` (`config/HemoCommonConfig.java:28-30`), the founder cure cannot be done.
- Action: decide (kill credit by owner, a D5 map, a config note).

### PG-19 (Low): Clarity Ascension silently needs the End

The pattern takes 52 `#hemomancy:progression/end_ritual_stone` blocks (End Stone Bricks, Purpur Block, Purpur Pillar,
`tags/block/progression/end_ritual_stone.json`) and 15 glass blocks. No Unstained dialogue, Liber page or lang line
mentions the End (the Acolyte and Lady lines at `en_us.json:1187,1198,2535,2537` name only the copper and the rite).
Action: expand (one line in the Acolyte or Liber Immaculatus) or accept.

### PG-20 (Low): A placeholder lang key

`hemomancy.dialogue.event.alchemist_red_taxonomy_placeholder_reward` ("The Red Taxonomy reward has not been decided yet.",
`en_us.json:2029`) is referenced nowhere in code. Action: cull.

---

## Already in OPEN_QUESTIONS (seen again, not re-reported)

PT-4 (Mason's Respite map never reissued; the Respite is the rarest required structure, spacing 72 and separation 24),
PT-10 (killed Anchorite), PT-21 (Circus pointer), PT-30 (lost scar blank; note that `scar_blank` also rolls in
outpost and mausoleum chests), PT-39 (Quintessence source unnamed), PT-74 (Monolith shatter roll), PT-84 (Mycophant
rematch), PT-90 (members never reach D7), M-6 (legacy mausoleum and its trial blocks), C-6 (Recollections tradeable, Loom
ungated), D-10 (rite-bound members), D-11 (Unstained ritual system), F-22 (Dictation Table).

## Not a problem (checked, holds)

- **Rank rites.** `sanguine_brotherhood`, `illuminatus_rite`, `sanctified_rite`, `archon_rite` and `apotheos_rite`: every
  offering, structure block, medium and floor traces to a survival source. Sanguine Quintessence is renewable through the
  D3 Exsanguination rite (`recipe/cardinal_rite/exsanguination.json`) as well as the mausoleum chests and the Saints.
- **Chapter proofs** (`common/mission/shared/HarbingerChapterProgression.java`, `HarbingerChapterMilestone.java`): each
  points at a reachable advancement or flag.
- **Rite helpers.** Rites that need helpers (Archon 1, Bloom 1, Covenant Vigil 1, Eternal Covenant 1, Ancestral
  Communion 3) accept bloodline NPC members (`common/rite/harbinger/CardinalRiteAllyService.java:203-206`), so a solo player
  can meet them.
- **Floors.** All 19 floors in `data/hemomancy/cardinal_rite_floor/` (the four styles at four tiers, plus the Hush,
  Qliphoth and Ancestral grand floors) use only obtainable blocks.
- **Station upgrades.** All 14 kits are craftable. Every companion offering except the Suspended Blood Crystal (PG-3) has
  a source: Chromatic Sublimate, Monolith Imbued Cloth and Hematic Memory have recipes; Monolith Fragment comes from the
  Monolith; Axonal Slate, Myelin Sheath and Nerve Bundle drop from Cortical Drift and Vagrant Mind blocks.
- **Working crafts.** All 20 subjects and media are obtainable, including the Ferric Binder (Artificer trade), Hyphal
  Substrate, Morphling Polyp, Spore Sac and Runic Motif Paper.
- **Advancements.** All 194 files: parents, icons and criteria ids resolve. Every advancement id named in Java exists.
  The 34 `impossible` advancements without a literal id are granted through built ids (`ArtificerAssignments.id`,
  `VeinMasonAssignments.id`, `ClinicalBloodKnowledge` `"clinical_" + key`) or by data (`discovery_inscription` files,
  whose targets all exist). `inherited_memory` is any-of, so its legacy entries do not block it.
- **NPC trades.** Every `want` item is obtainable (Faded Memory drops from dissolved Wills,
  `common/entity/mob/monster/will/WillEntity.java:602,614`) and every `give` id is registered.
- **Books.** The Liber pages reference items only as icons, and all 135 resolve.
- **D7 to D8 one-time items.** The Qliphoth Seed is renewable by building another Monolith
  (`common/block/harbinger/functional/SanguineMonolithBlock.java:243`). The Memory of Vesper is reissued
  (`SilentiumAscensionRules.shouldReissueMemory`). The Fungal Spine and Mycophant Tendril use `PendingDelivery`.
- **Saint materials.** Recollections come from Gardens cysts, which a possessing Silentium can bring home (HR line 1053).
  Hallowed Residuum fragments drop from Saint trials (`common/entity/boss/saint/SaintTrialEntity.java:454-456`).
- **Morphling strains.** Kinbud and Sunburst incubator inputs (Giant Tube Worm Colony, Hematic Algal Crust) are placed by
  `PelagicHabitatFeature` and drop themselves.
- **Mnemonic Ambergris** stays obtainable with Pelagic worldgen off: the reef region is still registered
  (`common/init/BiomeInit.java:438-443`) and whales spawn there.
- **Unstained rites without active blood.** They start from the blood-crafting key
  (`common/network/capa/harbinger/BloodCraftingKeyPressPacket.java`, `CardinalRiteActivationRules.java:41`).
- **Enlightenment** is reachable without the blocked rites (PG-2).
- **Converted and grown blocks.** The Fungal Podium comes from converting the Pylon
  (`common/block/harbinger/functional/FungalImplantationPylonBlock.java:150`). The Gourd grows from Gourd Seeds (grass loot
  modifier).
- **Structure placement.** The Blood Temple, Harbinger Outpost and Unstained Church (spacing 32, separation 4, wide
  biome lists) are common. The Unstained Church places the Podium and the Altar of Cleansing.
