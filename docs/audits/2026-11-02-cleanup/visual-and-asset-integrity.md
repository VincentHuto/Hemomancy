# Visual uncertainties and asset integrity

Audit date: 2026-11-02. Tree: `neo-1.21.1` at `8d01a316c`. Read-only audit; nothing in `src/` or `tools/` was changed.

## Summary

The static scan covered **280 blocks** (all ten `BlockInit` registers), **850 items** (603 registered in `ItemInit`,
including 161 through helpers and 18 Court spawn eggs from the `CourtRole` loop, plus 247 auto or custom block items),
**2,215 PNG textures**, **144 `.mcmeta` files**, **1,789 authored and 45 generated models**, **67 core shader JSONs**
and **188 sound events**. Every block but one has a blockstate, every item has an item model, and all 99
`builtin/entity` items have a renderer. The scan found **20 distinct missing or dangling references**: one blockstate,
three texture paths named by 12 model references, 42 unresolved face texture variables in five models, two atlas
sources, three sound files, and five texture paths at ten Java sites plus one missing icon directory. It also found
**155 orphan textures**. Another 233 archived textures sit under `old/` or are named "- Copy", and the jar excludes
them. The orphans also include 89 orphan or stale models and 26 unused shader files. The placeholders found are:
the Pale Intercession, a live Unstained ally whose base and emissive textures are fully transparent, so its body
never renders; two live mobs that wear one flat colour; nine blocks that are plain cubes made from other blocks'
textures, or from an item sprite in the Witness Organ's case; 29 mob effects in 14 icon sets shared with another
effect or item; four WIP items with vanilla sprites; three pairs of items with identical icons; and the Venous
Strider sabatons, which reuse the Chalybeate boot model and texture. The texel-density list from 2026-10-17 is still entirely open,
and it exists only in a memory note, not in `docs/`. A client log from today confirms most of this at runtime. That
run is `run/logs/latest.log`, a client load from 14:20 to 14:39 on an NVIDIA RTX 4090 with driver 610.88, and no file
under `src/` changed after 14:20.

## Method

- Scripts (kept in the session scratchpad, not committed), all run with `python -I`:
  - `asset_audit.py` parses `BlockInit`, `ItemInit` and `CourtRole` for registry ids. It resolves blockstates, model
    parent chains, `neoforge:composite` and `neoforge:obj` children (`.obj`, `.mtl`, `map_Kd`) and texture variables,
    face by face. It checks particle, font, atlas, sound and `.mcmeta` references, and every `"textures/...png"`
    literal in Java. It also resolves the `minecraft:` and `neoforge:` namespaces against the NeoForge 21.1.251
    resource jars in `build/moddev/artifacts/`.
  - `orphans.py` classifies the textures nothing references. Texture paths that Java builds from string literals
    count as dynamic references, not orphans.
  - `model_quality.py` measures area-weighted texel density per model face, mixed resolutions, and alpha-0 texels on
    the solid layer.
  - `shader_audit.py` matches core and post shaders against `ShaderInit`, includes and uniforms.
  - `style_audit.py` samples copper and brass hues on Harbinger-side items.
- Runtime corroboration comes from `run/logs/latest.log` and `debug.log`, plus 108 other `latest.log` files under
  `build/` dated 2026-10-25 or later (30 client loads). The same resource warnings repeat in every client load.

Script counts:

| Check | Count |
| --- | --- |
| Blocks parsed / `.register(` calls in `BlockInit` | 280 / 280 |
| Blockstates: authored / generated only / missing | 268 / 11 / 1 |
| Items parsed (direct + block items) | 603 + 247 = 850 |
| Item models: authored / generated only / missing | 826 / 24 / 0 |
| `builtin/entity` items without a renderer | 0 of 99 |
| Distinct blockstate model references measured | 683 |
| Unresolved face texture variables | 42 in 5 models |
| Missing texture files named by models | 3 paths, 12 references |
| Java texture literals: resolved / missing | 437 / 10 |
| Data, book-theme and guide JSON texture references: resolved / missing | 156 / 0 |
| Particle JSON sprite references: missing | 0 (14 JSONs, 12 registered sprite-set types) |
| Font providers: missing | 0 (7 JSONs) |
| Atlas sources: missing | 2 |
| Sound files: missing / unreferenced OGGs | 3 / 57 |
| Animation strips with unused frames | 4 |
| Textures: referenced / archived (`old/`, "- Copy") / reached by built paths / orphan | 1,478 / 233 / 349 / 155 |
| Models: orphan (strict) / item models for unregistered ids | 12 / 77 |
| Core shaders: registered / never registered | 58 / 9 |
| Median texel density, block models / item element models | 16.0 / 16.0 px per block |

The texture split counts every one of the 2,215 PNGs once.

---

## 1. Asset integrity

**VS-1 · Medium · `rendering_bile_block` has no blockstate, so every client load logs 16 missing-variant warnings.**
Evidence: `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java:169-170` registers it in `LIQUIDBLOCKS`.
There is no `blockstates/rendering_bile_block.json` in `src/main` or `src/generated`. The other three liquids have one
(`white_humor_block.json`, `morphic_nectar_block.json`, `phlegethontic_ichor_block.json`).
`run/logs/latest.log:164-179` has "missing model for variant: 'hemomancy:rendering_bile_block#level=0..15'", and the
30 client logs since 10-25 hold 400 of these lines. The fluid surface uses the fluid renderer, so in practice the
missing block model only shows as missing-texture particles.
Confidence: Verified. Action: add a blockstate and a particle-only model like `white_humor_block`. Effort: S.

**VS-2 · Medium · The left-hand Hematic Microscope viewing model points at a deleted texture.**
Evidence: `assets/hemomancy/models/item/hematic_microscope_viewing_left.json:4-5` names `hemomancy:item/hematic_microscope`
for texture `0` and for `particle`. That PNG was deleted in `08eea4acd` (`git log --diff-filter=D`).
`run/logs/latest.log:180-181` reports "Missing textures in model hemomancy:item/hematic_microscope_viewing_left#standalone".
The model is drawn when the instrument is mirrored (`client/render/layer/player/HematicMicroscopeLayer.java:21,57`), so
every cube shows the magenta-and-black checker. The credit line names `tools/model_export/hematic_microscope.py`, which
is not in the repository (see AN-9 in `entity-animation-and-effects.md`).
Confidence: Verified. Action: point it at the block textures that `item/hematic_microscope.json` uses, or restore the
generator and regenerate. Effort: S.

**VS-3 · Medium · Five models leave face textures as unresolved Blockbench variables, which render as the missing texture with no log line.**
Minecraft resolves an unknown `#missing` or `#8` reference to the missing sprite silently, so the runtime log stays
clean. The faces:

| Model | Bad refs | Visible? |
| --- | --- | --- |
| `models/block/harbinger_escritoire.json` (block and item) | 79 `#missing` (from line 34) | Yes: element 21's `up` face (the hutch top at y 26) and element 13's `down` face (the desk-top overhang, x -3..19) are exposed. Most of the others are inner faces. |
| `models/item/living_crossbow_arrow.json` (the loaded override of `living_crossbow`) | 10 `#8` (lines 844-899) | Yes: elements 25 and 26 are flat strings, and their `up` faces have area. |
| `models/item/hematic_microscope.json:24` | 1 `#missing` | Probably hidden (the top of the grip under the body). |
| `models/block/cardinal_focus.json` | 8 `#missing` | No: the down faces are coplanar with the base top, and the side faces sit inside the corner posts. |
| `models/block/fungal_podium.json:129-146` (also used by `fungal_implantation_pylon`) | 8 `#missing` | No: they are the zero-area faces of two crossed planes. |

Confidence: Verified for the references; visibility is Suspected until seen in a client. Action: assign textures or
delete the faces in the generator or Blockbench source. Effort: S.

**VS-4 · Low · Ten `builtin/entity` item models name `hemomancy:items/...` (plural) textures that do not exist.**
Evidence: `layer0` is `hemomancy:items/liber_sanguinum` in these files: `models/item/liber_sanguinum.json:3`,
`harbinger_assignment_ledger.json:3`, `living_axe.json:4`, `living_blade.json:4`, `living_sickle.json:4`,
`living_spear.json:4`, `marrow_crown.json:4` and `sanguis_lancea.json:4`. `annettas_sanguis_lancea` inherits it.
`liber_immaculatus.json:3` names `items/liber_immaculatus`. The custom renderer never draws `layer0`. However,
`liber_sanguinum`, `liber_immaculatus` and `harbinger_assignment_ledger` have no `particle` entry, so their
break and use particles use the missing sprite. The intended `textures/item/liber_sanguinum.png` exists and is
otherwise an orphan.
Confidence: Verified. Action: fix the path and add a `particle`. Effort: S.

**VS-5 · Low · Two atlas sources point at missing files.**
Evidence: `assets/minecraft/atlases/blocks.json` lists `hemomancy:gui/entity/royal_guard_shield_base` and
`hemomancy:gui/empty_banner_slot_background`. Neither exists, though `textures/entity/royal_guard_shield_base.png`
does. `run/logs/latest.log:148-149` reports "Missing sprite" for both. No Java code names either sprite.
Confidence: Verified. Action: delete the two entries. Effort: S.

**VS-6 · Low · Ten Java texture literals and one built path name missing files; none of them is drawn today.**
Evidence:
- `textures/entity/tracker.png` is returned by `getTextureLocation` in six projectile renderers:
  `client/render/entity/projectile/BloodCloudRenderer.java:23`, `BloodCloudCarrierRenderer.java:23`,
  `BloodOrbDirectedRenderer.java:23`, `BloodOrbTrackingRenderer.java:23`, `TrackingPestsRenderer.java:23` and
  `TrackingSerpentRenderer.java:23`. These are 25-line `EntityRenderer` subclasses with no `render` override, so the
  texture is never bound.
- `LeechRenderer.java:13` declares an unused `TEXTURE` constant for `entity/denizen/model_denizen.png`.
- `VascularViewScreen.java:40-41` declares `gui/vascular_view.png` and `gui/vascular_border.png`, which the screen
  never blits; it draws the vein background instead.
- `ScarBinderScreen.java:31` names `gui/epic_gui.png` in its default branch (see VS-25).
- `common/item/harbinger/bloodline/VasculariumCharmItem.java:40-41` builds `textures/gui/command_icons/<command>.png`
  for seven commands, but the directory does not exist and `getIcon()` has no caller.

Confidence: Verified. Action: delete the dead constants and the unused icon enum field. Effort: S.

**VS-7 · Low · Four animation strips carry a frame their frame list never uses.**
Evidence: `textures/block/befouling_ash_trail.png`, `block/smouldering_ash_trail.png`, `block/virid_salis_trail.png` and
`item/smouldering_ash_trail.png` have six frames, but their `.mcmeta` lists only frames 0 to 4.
`run/logs/latest.log:150-153` reports "Unused frames ... [5]". Three of these four textures are also orphans (see
Appendix B). Confidence: Verified. Action: trim the strips or delete the orphans. Effort: S.

**VS-8 · Low · `textures/block/mushroom_block_morel_inside.png` is a damaged, unreferenced PNG.**
Evidence: its `iCCP` chunk fails its CRC, and Pillow refuses to open the file. Minecraft's loader ignores CRCs, so the
file is still stitched into the block atlas. Nothing references it. Confidence: Verified. Action: delete it.
Effort: S.

**VS-9 · Low · Orphans cost atlas space and review time.**
Evidence (Appendix B to D): 155 textures that nothing references, 12 orphan models, 77 item models for ids that are not
registered, 26 unused shader files (VS-30) and four stale generated item models for itemless blocks. The four are
`src/generated/resources/assets/hemomancy/models/item/{abocipher_emitter,qliphoth_bloom,warp_chair_filler,placed_blood_stained_stone}.json`.
`HemoItemModelProvider` writes a model for every block in the base registers whether or not the block has an item.
Vanilla's `blocks` atlas stitches every PNG under `textures/block/` and `textures/item/`, referenced or not. The atlas
is 4096×4096 in this run (`latest.log`: "Created: 4096x4096x4 minecraft:textures/atlas/blocks.png-atlas"). Already
covered elsewhere: the 57 unreferenced OGGs (AN-15), the two particle JSONs with no registered type (AN-28) and the
missing `tooth_pecks` OGGs (AN-10).
Confidence: Verified for the lists. Action: one deletion batch, and skip itemless blocks in `HemoItemModelProvider`.
Effort: M.

## 2. Placeholders

**VS-10 · High · The Pale Intercession (registry `spectral_companion`) is invisible: both of its textures are fully transparent.**
Evidence: `textures/entity/pale_intercession/base.png` and `emissive.png` are 128×128 with zero non-transparent pixels,
and have been since their first commit, `a22299245`. `client/render/entity/summon/PaleIntercessionRenderer.java:13-26`
draws the model with `entityTranslucent(base)`, and `client/render/layer/mob/PaleIntercessionGlowLayer.java:19` draws
the glow with `entityTranslucentEmissive(emissive)`. Both draw nothing. Only the `lethean_drip` particles from
`PaleIntercessionEntity` show. `docs/HEMOMANCY_REFERENCE.md:4331` documents the ally as complete, and the entity
audit lists it as OK.
Confidence: Verified. Action: author the texture and glow (a generator or Blockbench paint) and capture it in a review
client. Effort: M.

**VS-11 · Medium · Two live mobs wear a single flat colour.**
Evidence: `textures/entity/hemojelly/model_hemojelly.png` is 64×32 in one colour (255,80,120), and
`textures/entity/myelin_borer/model_myelin_borer.png` is 32×32 in one colour (232,216,192). Both are fully opaque and
unchanged since `c8789c0ae` and `539e2a10b` in April. Both mobs are registered with spawn eggs. The Myelin Borer spawns
in the Cortical Drift (`HEMOMANCY_REFERENCE.md:4276`), and the Hemojelly is an ambient sea mob (`:4301`). Five more
flat textures belong to the commented-out tendency mobs (Cruor Fiend, Void Drinker, Frozen Clot, Abyssal Siphon,
Synapse Hound). Their renderers and spawn-egg item models remain, but the entity types are unregistered; that is
pending decision M-2 in `OPEN_QUESTIONS.md` §1.5.
Confidence: Verified. Action: texture the two live mobs; resolve M-2 for the rest. Effort: M.

**VS-12 · Medium · Nine blocks are plain cubes made from other blocks' textures, or from an item sprite.**
Evidence (`model_quality.py`, "MODELEDBLOCKS whose model is a single full cube"):
- **Witness Organ** (`BlockInit.java:1130`, `WIP` list): `models/block/witness_organ.json` puts the item sprite
  `item/mnemonic_ambergris` on all six faces of a `block/cube`. The sprite has 74 alpha-0 texels and the model has no
  `render_type`, so the cube shows the sprite's hidden RGB values on the solid layer. The reference documents it as a
  working Degree 3 block.
- **Hematic Suture Node** (`BlockInit.java:101`, main tab): a `cube_all` of `block/chiseled_hematic_iron_block`, the same
  as the decorative chiseled block. The reference calls it a "visible anchor".
- **Sanguine Vigil, Blood Pylon, Blood Trial Altar** (`WIP` list, `CreativeTabInit.java:94-99`): cubes of hematic-iron
  textures. The Blood Trial Altar also spawns live content (AN-2, PG-8).
- **Circus evidence records**: `circus_stage_record`, `circus_loft_record`, `circus_quarters_record` and
  `circus_carousel_record` (`BlockInit.java:1313` and nearby) are `cube_all` of vanilla `stripped_dark_oak_log`, which
  looks the same as a log.

The Covenant Throne's `cube_all` is not a problem, because its block uses `ENTITYBLOCK_ANIMATED` and a renderer.
Confidence: Verified. Action: model the Witness Organ and the Suture Node first (live), then the records; the WIP
cubes wait on their features. Effort: M to L.

**VS-13 · Medium · Twenty-nine mob effects share their icon with another effect or with an item.**
Evidence (byte-identical PNGs under `textures/mob_effect/`):
- The documented placeholders (`BRINED_COURT.md`, "Unfinished small pieces"): `stung` = `item/morphling_kinbud`,
  `pinned` = `item/morphling_holdfast`, `morphling_sealed` = `item/morphling_ink_cap`.
- Not documented, all live effects:
  - `arcane_resonance` = `iron_retort` = `sanguine_clarity`
  - `counterfeit_scar` = `hemolysis` = `impaired_recovery`
  - `cryoprotection` = `poison_resistance` = `venomous_resilience`
  - `grave_debt` = `hemophagy` = `insatiable_hunger`
  - `arachnid_anastomosis` = `web_mobility`
  - `blood_drunkenness` = `transfusion_saturation`
  - `burrowers_instinct` = `earthen_mining`
  - `conductive_mark` = `neural_overload`
  - `ender_resistance` = `veiled`
  - `mnemonic_screams` = `monolithic_dislocation`
  - `necrosis` = `wither_resistance`

  Several of these pairs are opposites; for example, `neural_overload` is harmful and `conductive_mark` is a mark.

The Morphling passive effects reuse their strain's item icon on purpose and are not counted.
Confidence: Verified. Action: draw distinct icons, starting with harmful/beneficial pairs. Effort: M.

**VS-14 · Low · More items share an icon with another item or use a vanilla sprite.**
Evidence:
- Byte-identical icons on distinct registered items: `item/hallowed_residuum_fragment_putriciel` =
  `..._seraphae` (two Saints' fragments look the same); `item/hemolytic_solution` = `item/hemolytic_vial`; the
  `scars/scar_anvil` and `scars/scar_blood_honed` overlays are the same.
- Vanilla sprites on WIP items: `desert_husk_effigy` and `zombie_husk_effigy` (`minecraft:item/rotten_flesh`),
  `vein_spider` (`spider_eye`) and `void_eye_organ` (`ender_eye`).
- Verbatim vanilla copies: `block/mycelium_top`, `environment/rain`, `block/attached_gourd_stem`
  (= `attached_pumpkin_stem`), and five fully transparent "overlay" textures copied from `redstone_dust_overlay`
  (`active_befouling_ash_overlay`, `active_smouldering_ash_overlay`, `befouling_ash_overlay`, `smouldering_ash_overlay`,
  `redstone_dust_overlay`).

Confidence: Verified. Action: repaint the fragments and the vial; the WIP sprites wait on their features. Effort: S to M.

**VS-15 · Low · Placeholder and authoring files ship in the jar.**
Evidence: `build.gradle:24-38` excludes `old/`, "- Copy", `.bbmodel`, three named text files and the drudge
"grey_old (n)" PNGs. `verifyPackagedClientResources` (`build.gradle:333-362`) rejects only `/old/`, `.bbmodel` and
`.cache`. These still ship:
- `textures/block/test/{1,2,3,4,blood_scorched_scab}.png`
- `textures/models/armor/{steve,test_blood_lust_layer_1,new_unstained_layer_1,silent_archon_layer_1_old,silent_archon_layer_2_old}.png`
- `textures/block/hematic_iron_block_old.png` and `textures/gui/rune_binder_upgrade_old.png`
- eight `.xcf` files (`entity/blood_gourd/gourd.xcf`, `environment/chamber_of_will_sky.xcf`,
  `gui/blood_overlay/base_degree_8.xcf`, `gui/gui_mind_runes.xcf`, `item/{asce,heart,moon}.xcf`,
  `particle/blood_avatar_hit.xcf`)
- `textures/mob_effect/vincent_huto_light_purple_hemolysis_<uuid>.webp`

Confidence: Verified. Action: delete the files, or add excludes and extend the packaged check to `.xcf`, `.webp` and
`/test/`. Effort: S.

## 3. Texel density and model scale

**VS-16 · Medium · The 2026-10-17 texel-density "still open" list lives only in a memory note, and every item on it is still open.**
Evidence: the list is in the user memory note `feedback_texel_density.md`. It is not in `docs/OPEN_QUESTIONS.md` or in
any topic doc; `docs/BOSSES.md:52,120` records only the ruling itself. The note's audit scripts, `build/density_audit.py`
and `density_audit2.py`, sit in the untracked build directory and are lost on `clean`. Measured today (Java models:
PNG size against the `LayerDefinition` size, with renderer scale 1 unless noted):

| Item (from the note) | Measured now | Status |
| --- | --- | --- |
| Velorum | `VelorumModel` 256×256, `entity/boss/velorum/velorum.png` 512×512 → 32 px/block | open |
| Iron Wall | `IronWallModel` 128×128, `model_iron_wall.png` 256×256 → 32 | open |
| Morphling Polyp | `MorphlingPolypModel` 128×128, `model_morphling_polyp.png` 256×256 → 32 | open |
| Barbed Shield | `BarbedShieldModel` 64×64, `model_barbed_shield.png` 128×128 → 32 | open |
| Covenant armour | `CovenantLeaderArmorModel` 128×64, `covenant_mantle_layer_{1,2}.png` 256×128 → 32 | open |
| Venous Strider armour | `venous_strider_layer_1.png` 256×128 (and byte-identical to the Chalybeate layers, VS-23) | open |
| Both elytras | `fungal_elytra.png` and `pontiff_elytra.png` 128×64 against the vanilla 64×32 layout → 32 | open |
| Kitbashed JSON weapons | `living_syringe` 238, `living_baghnakh` 212, `barbed_blade` 189, `living_crossbow` 154, `chitinite_mace` 92 px/block | open |
| Mnemonic Whale | `MnemonicWhaleModel` 128×128, PNG 64×64 → 8 | open |
| Hematic Microscope | item model 10.4 | open |
| Blood Crystal | stages 1/2/3 at 10.4 / 8.3 / 7.6 | open |
| Puffball | 4.0 (block and item) | open |
| Living weapon hand models, Unstained weapon meshes | not re-measured (Java, drawn at first-person scale) | unverified |

Confidence: Verified for the measured rows. Action: copy the list into `OPEN_QUESTIONS.md` §4 housekeeping and
version the audit script under `tools/model_export/`. Effort: S for the bookkeeping; M to L to fix the models.

**VS-17 · Medium · Density outliers not on that list.**
Evidence (`model_quality.py`, area-weighted over model faces; a detailed block reads about 16 to 19):
- `block/sanguine_omen` is a `cube_all` of a 256×256, 4,660-colour painted texture: 256 px/block, 16 times the norm.
  The block is on the WIP list.
- `escharian_scyphus_1..4` at 32.
- `vacant_effigy_0..5` at about 29.
- `humane_idol` at 75.6.
- `scrying_podium` at 39.2.
- `unstained_podium` at 35.7.
- `fungal_podium` and `fungal_implantation_pylon` at 31.4.
- `hydra_crown` at 34.3.
- `pulse_pylon` (item) at 34.1.
- The `saint_sarcophagus` item at 33.0; the block model is 16.5.
- The Phlebotomist's Cabinet and Field Case specimen models put 256-wide label textures on small faces (about 1,000
  px/block).

Confidence: Verified (numbers); whether each one is intended is Suspected. Action: regenerate the generator-made ones at
density 1, and repaint the Sanguine Omen at 16 px per block. Effort: M.

**VS-18 · Low · Some models mix texture resolutions, and three item sprites are not 16×16.**
Evidence:
- Mixed widths: `blood_gutter_*` (16 and 128), `venous_basin_2/3` and `blood_basin` (one 16 px face on a 128 atlas),
  `palimpsest_scriptorium` (16 and 64), `phlebotomists_{cabinet,field_case}_specimen_0..8` (16 and 256),
  `specimen_jar` (16 and 64) and the `resonant_forge` item (16 and 32).
- Off-size `item/generated` sprites: `calcified_blood_spine` 18×18, `incandescent_enzyme` 18×18,
  `hematic_condenser_kit` 15×16.

Confidence: Verified. Action: fold the stray 16 px faces into the atlases and resize the three sprites. Effort: S.

## 4. Style consistency

**VS-19 · Medium · Harbinger machines are kitbashed from vanilla textures.**
Evidence: 181 block and item model files use 45 distinct `minecraft:` textures outside the particle slot. Among them:
- The Resonant Forge uses vanilla `anvil`, `stripped_dark_oak_log` and `brown_wool` as leather
  (`models/block/resonant_forge.json`).
- The Hematic Armature, Ghastly Alembic, Mycelial Crucible and Lantern, Morphling Incubator, Palimpsest Scriptorium,
  Consecrated Bloodwell, Scar Station and heart-node use vanilla `bone_block_side` and `bone_block_top`.
- The Somatic Loom uses vanilla wool and stained glass for its eight tendency channels (`somatic_loom_cop_*.json`).
- The Phlebotomist's Cabinet uses `black_terracotta`, `glass` and `stripped_oak_log`; the Ringmaster topper uses wool
  and `gold_block`.
- `blood_gutter` uses `deepslate_tiles`.

The blood-side material rule calls for hematic iron, bone and lacquer, and for blood-wood rather than vanilla oak.
Vanilla bone is the closest fit, but the oak, anvil and wool read as vanilla furniture next to the mod's own blocks.
Confidence: Verified for the references; Suspected that it reads badly. Action: swap in the mod's blood-wood,
lacquer and bone textures, starting with the Forge and the Loom. Effort: M.

**VS-20 · Low · No copper, brass, bronze or silver tones were found on Harbinger gear; three stale copper texture references remain.**
Evidence: no texture, model or part name on the Harbinger side contains copper, brass, bronze or silver.
`style_audit.py` hue sampling flagged only the intended fungal amber (`sporite_crystal`, Mycophant) and the Sunburst
house saffron. `pale_silver_*`, `consecrated_copper_ingot`, `verdigris_*` and the copper fittings in
`tools/model_export/hemomancy_books.py` belong to Unstained items. The stale references:
`models/item/sporitic_thurible.json:4-5`, `living_torch.json:4` and `living_flail.json:4` use
`item/consecrated_copper_ingot` as `layer0`, and the thurible also uses it as `particle`. The thurible's break and use
particles therefore show copper-ingot pixels. The torch and flail use `block/venous_stone` for their particles.
Confidence: Verified. Action: point them at blood-side textures. Effort: S.

**VS-21 · Low · Item icon families made in different passes differ in outline and palette.**
Evidence (16×16 item sprites; "outline" = share of edge texels darker than 0.6 of the interior median):
- No dark outline: `ahaematic_*` 0.00, `hallowed_*` 0.00, `recollection_*` 0.00.
- Full dark outline: `chitinite_*` 1.00, `echo_*` 0.98, `barbed_*` 0.97, `morphling_*` 0.84.
- Palette sizes run from about 6 to 11 colours (`memories` overlays, `morphling`, `silent`) up to 64 to 114 colours
  (`tincture_*`, `hematic_*`, `mnemonic_*`).

Confidence: Suspected (the metric is a heuristic). Action: a sprite style pass per family, not per item. Effort: M.

**VS-22 · Low · The Unstained book theme borrows blood-side colours and fails its own contrast check.**
Evidence:
- `assets/hemomancy/book_themes/liber.json`, used by `data/hemomancy/books/liberimmaculatus/book.json:9`, has
  `unreadTitleShift` from `#B3121A` (blood red) to `#7A248F`, the same as the Harbinger `fane.json`.
- Its muted ink, `#6F8A9A` on paper `#E4E7E2`, is about 2.9:1. HutosLib warns "Book theme hemomancy:liber: muted ink
  contrast is below 3:1" (`latest.log:147`).
- `gui/book/fane/paper.png` = `gui/book/liber/paper.png` and `fane/leaf.png` = `liber/leaf.png`: the two books share
  identical paper and leaf textures.

Confidence: Verified. Action: give the Lethean theme a still-water title shift, darken `inkMuted`, and give it its own
paper. Effort: S.

**VS-23 · Low · Some armour and elytra textures are copies of others, and the Venous Strider sabatons are Chalybeate boots.**
Evidence (byte-identical):
- `models/armor/venous_strider_layer_1.png` = `chalybeate_sclerite_layer_1.png` = `chalybeate_sclerite_layer_2.png`.
  `client/render/armor/ModelBackedArmorItemRenderHelper.java:33,146-148` draws the Venous Strider sabatons with
  `ChalybeateFortressArmorModel.boots` and that texture, so they look the same as Chalybeate boots.
- `fungal_elytra.png` = `pontiff_elytra.png`, and `client/render/layer/player/FungalElytraLayer.java:25,45` uses both.
- `layer_1` = `layer_2` for `covenant_mantle`, `phantasmal_blood_lust` and `sheolic_blood_lust`.

Confidence: Verified (identical files); the visual effect on custom-model armour is Suspected. Action: paint the
Venous Strider and Pontiff textures. Effort: S to M.

## 5. Screens and HUD

There are 48 `Screen` subclasses under `client/screen/`. Thirty-six use `ScreenDrawUtils` or `VeinBackgroundRenderer`.
The Dialogue screen draws from its own complete nine-slice sprite set (8 themes × 8 pieces, all present). The radial
menus are shader-drawn. Every literal translation key in `client/` exists in `en_us.json` or the vanilla language
files; dynamic key prefixes are left to the i18n audit. No button has an empty handler, and there are no TODO or
placeholder markers.

**VS-24 · Medium · Mod HUD layers stay on screen with F1 (hide GUI) and in spectator mode.**
Evidence:
- NeoForge gates only vanilla layers on `hideGui`. `net/minecraft/client/gui/Gui.java:248` (NeoForge 21.1.251 sources)
  adds the vanilla groups with `() -> !minecraft.options.hideGui`, and `GuiLayerManager.add(child, shouldRender)` wraps
  only those groups. Layers added through `registerAboveAll` render unconditionally.
- Ten overlays check `hideGui` themselves, for example `IronheartedOverlay.java:37` and `CardinalRiteOverlay.java:30`.
  These do not:
  - the main blood bar, `BloodVolumeOverlay.renderHUD` (`client/screen/overlay/BloodVolumeOverlay.java:191-199`)
  - `UnstainedGaugeOverlay` (`:88-92`)
  - `HarbingerLodestoneOverlay` (`:16-21`)
  - `StillArtCooldownOverlay`
  - `ChamberVisitOverlay` (`:29`)
  - `WillPresenceOverlay` (`:26`)
  - the `hud_markers` layer's `EarthenVeinTravelClientEvents.renderTooltip` (`:127-140`)
  - `ActionBarOverlay`. `GuiLayerRegistrar.java:26` replaces `VanillaGuiLayers.OVERLAY_MESSAGE`, and
    `RegisterGuiLayersEvent.replaceLayer` → `wrapLayer` swaps out the wrapper that held vanilla's `hideGui` check, so
    `ActionBarOverlay.java:34` now draws under F1.
- Only `HematicMicroscopeOverlay`, `MorphicNectarScreenOverlay` and `WhiteHumorScreenOverlay` check `isSpectator()`.
  Vanilla hides its status bars in spectator mode, but the blood bar stays.
- The vignettes (`FungalWhisperVignetteOverlay`, `SanguineOmenOverlay`) and `BossSceneOverlay` are judgment calls.

Confidence: Verified. Action: add one `hideGui` and spectator guard at registration in `GuiLayerRegistrar` (a wrapper
lambda) rather than per overlay. Effort: S.

**VS-25 · Medium · The Scar Binder screens are dead UI, and `RadialMenuScreen` has no callers.**
Evidence: `ItemScarBinder` (`common/item/harbinger/scar/ItemScarBinder.java:30`) is never constructed or registered,
yet the following are all live:
- `ContainerInit.java:23-24` registers the `scar_binder` menu.
- `client/registry/MenuScreenRegistrar.java:41` registers `ScarBinderScreen`.
- `ScreenScarBinderViewer` and the `gui/scar_binder*.png` textures ship.

`ScarBinderScreen`'s default branch names a missing `gui/epic_gui.png` (`:31`), and it draws with a raw `Tesselator`
quad rather than the shared chrome. `client/screen/radial/RadialMenuScreen.java:24` is referenced nowhere outside its
own file.
Confidence: Verified. Action: delete them, or register the item if the Binder is meant to return (this ties into
housekeeping decision M-3). Effort: S.

**VS-26 · Low · Private copies of the shared chrome slip past `ScreenChromeGuardrailTest`.**
Evidence: the guardrail (`src/test/java/.../architecture/ScreenChromeGuardrailTest.java:29-46`) matches certain
helper names and an exact `-1/+17` slot-well layout. These copies are not caught:
- `StillwaterCondenserScreen.java:86` `drawFrame` (a two-pixel bevel) and `:79` `drawSlot` (a well at `-2/-1/0`
  offsets)
- `MiniRecipeRenderer.java:442` `drawSlot` and `drawMiniSlot`, which are `drawSlotWell` shifted by one pixel
- `EnzymaticScriptoriumScreen.java:168` `drawSlot`
- `PuppeteersSpindleScreen.java:403` `drawPanel`

Confidence: Verified. Action: route them through `ScreenDrawUtils.drawBevelBorder` and `drawSlotWell`, and widen the
guardrail's slot pattern. Effort: S.

**VS-27 · Low · Some labels are built from registry ids instead of names.**
Evidence:
- `client/screen/tile/functional/MasonsEffigyScreen.java:199,324-337` title-cases `scar_*` paths, so the scar list
  can disagree with the item names.
- `client/screen/overlay/CardinalRiteOverlay.java:139-145` falls back to a title-cased recipe path when the recipe is
  not on the client.
- `client/screen/item/RiteHintScreen.java:600-602` falls back to a floor path with the underscores replaced.

Confidence: Verified. Action: use the scar item's `getHoverName()`; the fallbacks are a matter for the i18n audit.
Effort: S.

**VS-28 · Low · Several fixed-size panels have no fit-scaling.**
Evidence: `ScryingDiagnosticsScreen` is 322×(230+16), `RiteHintScreen` and `BloodStructureHintScreen` 340×220,
`HeartNodeScreen` 300×212, `PuppeteersSpindleScreen` 280×234, `VisceralMirrorScreen` 280×200 and
`DiscoveryInscriptionScreen` 300×176; each only centres itself. At GUI scale 3 in a 1280×720 window the scaled
screen is 426×240, so Scrying Diagnostics overflows vertically. `BloodlinePoolScreen` (520×320) and
`MnemonicReliquaryScreen` (360×280) already use `fitScale`.
Confidence: Suspected (not seen in a client). Action: reuse the `fitScale` pattern. Effort: S.

**VS-29 · Low · JEI reports duplicate entries from the creative tab.**
Evidence: `latest.log:249-251`, "20 duplicate items ... in 'Hemomancy the Sanguis Noctis'", for
`living_weapon_graft`, `consecrated_syringe`, `saint_sarcophagus` and `house_marker`. These items vary only by data
component, and JEI needs subtype interpreters for them. The creative-tab frame and banners
(`client/screen/inventory/CreativeTabChrome.java`) use the shared chrome correctly.
Confidence: Verified. Action: register subtype interpreters in the JEI plugin. Effort: S.

## 6. Shaders and render

**VS-30 · Low · Twenty-six shader files are dead, and one post chain names another mod's programs.**
Evidence:
- Nine core shader JSONs are never registered in `client/render/shader/ShaderInit.java:218-284`: `aoe_gun_projectile`,
  `lodestone_texture`, `particle/particle`, `radial_menu`, `runic_energy_flow`, `screen/screen_particle` and
  `shapes/{scrolling_texture,scrolling_triangle_texture,triangle_texture}`.
- No `PostChain` is created anywhere, so all seven `shaders/post/*.json` and the nine `shaders/program/*.json` are
  unused.
- `post/bloom.json` names `fufo:vertical_bloom` and `fufo:horizontal_bloom`, and `program/horizontal_bloom.json`
  names the vertex program `sobel` and `fufo:horizontal_bloom`; these come from another mod and would fail to load.
- `program/horizonal_bloom.fsh` is misspelled.
- `shaders/include/color_sorting.glsl` is never imported.

Confidence: Verified. Action: delete them (these are lodestone and MnA leftovers). Effort: S.

**VS-31 · Low · Some uniform declarations do not match between JSON and GLSL.**
Evidence:
- `shaders/core/entity/vesper_wound_draw.json` declares `FogColor`, which the program does not use. Every client load
  logs "could not find uniform named FogColor" (`latest.log:202`).
- `world/manipulation_glass.fsh:7` and `world/manipulation_skin.fsh:3` declare `uniform float HemoTime`, which is
  neither in their JSON nor used.
- `rendertype_entity_translucent_emissive could not find sampler named Sampler2` (`latest.log:201`) is vanilla's
  own shader.

Confidence: Verified. Action: remove the stray declarations. Effort: S.

Not repeated here: freeze behaviour per animation is pending decision A-4, and the Ferric 128 seed tile is V-3b (both
in `OPEN_QUESTIONS.md` §1.5). The 36 wall-clock calls in `client/` sit under A-4.

## 7. Blockstate and model behaviour

Nothing new beyond VS-1 and VS-12. The static check of variant completeness (every combination of observed property
values) found no gaps in 268 authored and 11 generated blockstates. The runtime log, which reports any state without a
model, names only `rendering_bile_block`. No block item is parented to another block's model. The models with alpha-0
texels and no `render_type` sample only opaque texels; see "Not a problem". The Witness Organ (VS-12) is the one
real case of a transparent sprite on the solid layer.

## 8. Known weak visuals: what has never been watched

**VS-32 · Medium · Twelve areas built in October have never been watched in a client by the user. Five of them are already described as wrong in their own docs.**

Ranked by how likely each is to look wrong, with this audit's static evidence where it adds something:

| Rank | Area (source) | Why it is likely wrong |
| --- | --- | --- |
| 1 | Brined Court HUD, Bestiary dossier, Liber pages, tooltips; effect icons (`BRINED_COURT.md` proof owed) | Never captured in a client. Three effect icons are confirmed placeholder copies (VS-13), and school state is not synced, so the client shows only the raised sail. |
| 2 | Cardinal rite map and floors (`CARDINAL_RITES.md`) | The doc says the Qliphoth membrane motif ignores floor rotation and the Making/Bond and Floorless/World tints look alike. |
| 3 | Saint mausoleum lighting and art (`SAINT_MAUSOLEUMS.md`, `archive/tracks/mausoleums/12-next-pass.md` §3.3-3.4) | The doc says Hemorath's litany hall is very dark, Seraphae's grilles go black under ambient occlusion, every Saint has red coals, prostrate devotees have no outline, and `descent_top` is unchecked. |
| 4 | Circus rebuilt Bellows, Juggler, Hulk, faculty and camps (`CIRCUS.md`) | Built from previews only. The evidence records are vanilla-log cubes (VS-12), the Ringmaster is stuck in one pose (AN-1), and the finale has no presentation (AN-29). |
| 5 | Fane ground (`OPEN_QUESTIONS.md` §2) | Distant domes are still separate bubbles per stake, by decision. |
| 6 | Boss presentation frames, title cards, Mycophant waking and capture (`BOSSES.md`) | No frame or card has been reviewed, and the title-card placement is an open question. |
| 7 | Heart-node screen, spread, set pieces and possession scenes (`ENDGAME_PATHS.md`) | Added after the 18-check review. Today's client load compiled every registered shader on NVIDIA, which retires the "prove it compiles" item for this GPU only, not for AMD or Intel; the visuals themselves are unwatched. |
| 8 | Casting impacts, basics and infusion veins (`MANIPULATIONS_AND_CASTING.md`) | Seen only through harness runs; the "A?" rows in the visual catalogue are pending. |
| 9 | Wayside and Harbinger sites, Brined Votary and heart-node remodels (`OPEN_QUESTIONS.md` §2) | Rebuilt 10-31 to the structure quality bar but not walked. |
| 10 | Court NPC synced poses, Woven Current strands and school in multiplayer (`BRINED_COURT.md`) | Only the strand sync was checked with two clients. |
| 11 | The NPC trade "Today" ribbon and the Scout's hub (`NPC_DIALOGUE_AND_TRADE.md`) | GameTest only. |
| 12 | Pale Intercession, Hemojelly and Myelin Borer (this audit) | Not on any proof-owed list, but statically certain to look wrong (VS-10, VS-11). |

Confidence: Verified that the docs list these; the ranking is a judgment. Action: one played session per row, as
`OPEN_QUESTIONS.md` §4 step 1 already proposes. Add rows 1 to 4 and 12 to the first session. Effort: L.

---

## Not a problem

- **Block and item models.** Every block except `rendering_bile_block` has a blockstate, and every blockstate model
  resolves through its parents to existing textures (`minecraft:` and `neoforge:` parents checked against the
  21.1.251 jars). All 850 items have an item model. All 99 `builtin/entity` items get a renderer from
  `ItemClientExtensions` (by id or class) or from `ClientExtensionsRegistrar` (books, arm banners). Item renderers and
  armour models register only from `client/item/` and `client/registry/`.
- **Item models for block items.** None is parented to a different block's model.
- **Particles and fonts.** All 12 registered sprite-set particle types have a JSON, and every sprite they name exists.
  All seven font JSONs resolve, including bitmap files, the `reference` include and the bundled OFL licence.
- **Other JSON texture references.** All 156 texture references in data, book-theme and guide JSON resolve. Every
  dialogue-theme sprite exists for all eight themes, and the category cards and icons exist for all four categories.
- **Dynamic texture paths.** Spot-checked folders resolve: `entity/pelagic/`, `entity/facsimile/`, `gui/blood_overlay/`,
  `gui/unstained_overlay/`, `gui/morphling_overlay/`, `gui/microscope/`, `gui/scriptorium/`, `gui/resonant_forge/`,
  `entity/book/`, `effect/manipulation/` and the Court and Circus portraits. The only missing one is
  `gui/command_icons/` (VS-6).
- **Mob-effect icons.** Every registered mob effect has an icon. The Morphling passive icons equal their item icons
  by design.
- **Shaders.** All 58 registered core shaders compiled in today's client load: no compile errors, and the session ran
  for 18 minutes. Every `#moj_import <hemomancy:noise/...>` resolves to `shaders/include/noise/`. Every custom
  uniform a registered shader declares is set from Java.
- **Render guardrails.** Only `WorldEffectDispatcher` subscribes to `RenderLevelStageEvent`, and there are no
  `RenderGuiEvent` handlers. Every `RenderType.create` is a static constant, a lazily cached field or a
  `SeededRenderTypes` factory. `MorphlingModelAttachment` uses vanilla's memoised `entityCutout` and similar
  factories.
- **Alpha-0 texels on the solid layer.** `lethean_poppy_wreath`, `cardinal_focus`, `bog_body` and `dictation_table`
  have such texels in their textures, but every face samples only opaque texels. `sanguine_condenser` uses vanilla's
  cauldron model.
- **Fluid render layers.** The Morphic Nectar has no translucent layer, but its textures are fully opaque. The other
  three fluids are registered translucent (`ClientSetupRegistrar.java:29-33`, `PhlegethonticClientEvents.java:35-36`).
- **Cardinal Focus and Fungal Podium `#missing` faces.** They are hidden or zero-area (VS-3 lists them for clean-up
  only).
- **Copper and silver accents.** None found on Harbinger gear; see VS-20.
- **Lethean Brew icon.** Its reddish colour is the terracotta clay jar the reference describes, not blood.
- **Fully transparent glow masks.** The seven pelagic `_glow` textures are fully transparent: harmless empty glow
  passes, not missing art.
- **Saint sarcophagus, Clairaudiograph, Mnemonic Reliquary, Vial Centrifuge.** Block models measure 16 to 18.6 px per
  block.

---

## Appendix A. Missing and dangling references (full list)

| # | Kind | Reference | Named at |
| --- | --- | --- | --- |
| 1 | blockstate | `hemomancy:blockstates/rendering_bile_block.json` (16 variants) | `BlockInit.java:169-170` |
| 2 | texture | `hemomancy:item/hematic_microscope` (2 refs) | `models/item/hematic_microscope_viewing_left.json:4-5` |
| 3 | texture | `hemomancy:items/liber_sanguinum` (9 refs) | `models/item/{liber_sanguinum,harbinger_assignment_ledger,living_axe,living_blade,living_sickle,living_spear,marrow_crown,sanguis_lancea}.json`, inherited by `annettas_sanguis_lancea` |
| 4 | texture | `hemomancy:items/liber_immaculatus` (1 ref) | `models/item/liber_immaculatus.json:3` |
| 5 | face variable | `#missing` ×79 | `models/block/harbinger_escritoire.json` |
| 6 | face variable | `#8` ×10 | `models/item/living_crossbow_arrow.json:844-899` |
| 7 | face variable | `#missing` ×8 | `models/block/cardinal_focus.json:34-73` |
| 8 | face variable | `#missing` ×8 | `models/block/fungal_podium.json:129-146` |
| 9 | face variable | `#missing` ×1 | `models/item/hematic_microscope.json:24` |
| 10 | atlas source | `hemomancy:gui/entity/royal_guard_shield_base` | `assets/minecraft/atlases/blocks.json` |
| 11 | atlas source | `hemomancy:gui/empty_banner_slot_background` | `assets/minecraft/atlases/blocks.json` |
| 12 | sound | `hemomancy:entity/tooth_pecks/{ambient,hurt,death}.ogg` (3 files) | `sounds.json` (AN-10) |
| 13 | Java texture | `textures/entity/tracker.png` (6 sites) | `client/render/entity/projectile/*Renderer.java:23` |
| 14 | Java texture | `textures/entity/denizen/model_denizen.png` | `LeechRenderer.java:13` |
| 15 | Java texture | `textures/gui/vascular_view.png` | `VascularViewScreen.java:40` |
| 16 | Java texture | `textures/gui/vascular_border.png` | `VascularViewScreen.java:41` |
| 17 | Java texture | `textures/gui/epic_gui.png` | `ScarBinderScreen.java:31` |
| 18 | Java directory | `textures/gui/command_icons/{follow,interact,move,stay,diagnostics,eat,attack}.png` | `VasculariumCharmItem.java:40-41` |
| 19 | animation | unused frame 5 in 4 strips | `block/{befouling,smouldering}_ash_trail`, `block/virid_salis_trail`, `item/smouldering_ash_trail` |
| 20 | damaged file | `textures/block/mushroom_block_morel_inside.png` (bad `iCCP` CRC) | orphan |

## Appendix B. Orphan textures (155)

Nothing references these: no model, particle, font, atlas, data or theme JSON, Java literal or built path.

- `textures/block/` (32): bag_texture, befouling_ash_trail, black_terracotta, effigy_palette, eye1, eye3, eye4,
  framed_brain, framed_brain_2, grass_block_snow, hyphae_end, lethean_poppy_wreath_flowers, lethean_poppy_wreath_leaves,
  magnetizer_front, magnetizer_side, magnetizer_top, morel_stem, mushroom_block_morel_inside, mycelium_side,
  pale_silver_bell_side, purple_eye_blink, quartz_block_side, quartz_block_top, redstone_dust_dot, redstone_dust_line0,
  redstone_dust_line1, redstone_dust_overlay, smouldering_ash_trail, trans_blood_fill_tiled, transfuser_sides,
  transfuser_top, virid_salis_trail
- `textures/block/engrams/` (10): engram_0 … engram_9. `EngramTextureCache` loads only the 26 letters.
- `textures/block/mna/` (1): hollow_mana_trapazahedron
- `textures/block/test/` (5): 1, 2, 3, 4, blood_scorched_scab
- `textures/entity/` (12): glow_page, liber_inclinatio, liber_inclinatio_hidden, model_cleansing_altar,
  model_dictation_table, model_ghastly_alembic, model_living_blade_hip, model_pallid_retort, model_shiny_chitinite,
  model_somatic_loom, naeglerophaeon, royal_guard_shield_base
- `textures/entity/abhorrent_thought/` (1): model_nightmare_thought
- `textures/entity/banner/` (2) and `textures/entity/shield/` (2): hemomancy_heart, hemomancy_veins. The live copies are
  under `assets/minecraft/textures/entity/{banner,shield}/`.
- `textures/entity/blood_bullet/` (1): model_blood_bullet
- `textures/entity/blood_shot/` (1): model_blood_shot
- `textures/entity/chitinite/` (1): model_shiny_chitinite
- `textures/entity/choir_keeper/` (1): choir_keeper
- `textures/entity/crimson_serpent/` (1): model_crimson_serpent
- `textures/entity/drudge/` (5): model_drudge_blue, _brown, _green, _purple, _yellow. `DrudgeRenderer` uses only grey
  and red.
- `textures/entity/floating_eye/` (1): model_floating_eye
- `textures/entity/villager/profession/` (1) and `textures/entity/zombie_villager/profession/` (1): hemopothecary
- `textures/environment/` (5): clouds, end_sky, rain, snow, sun
- `textures/gui/` (16): blank_inv, choose_vein, harbingers_resource_bars, inventory, memory_border, morphling_jar_gui,
  pallid_retort_gui, pallid_retort_gui_overlay, primal_morphic_overlay, rune_binder_gui, rune_pattern,
  unstained_clarity_fill_tiled, unstained_fill_tiled, unstained_gauge, vein_selection, vein_teleport
- `textures/gui/sprites/dialogue/blood/` (1): frame_wood
- `textures/item/` (16): anastocordyceps_nexus, harbinger_assignment_ledger, hematic_iron_ingot, humoral_barometer,
  letting_bracelet_left, letting_bracelet_right, liber_inclinatio, liber_inclinatio_hidden, liber_sanguinum (see VS-4),
  ossuary_clock, particle_item, rune_blank, sculk_vein, stabilized_sanguine_formation, wax_cylinder_mounted,
  wax_cylinder_mounted_recorded
- `textures/item/mna/` (10): ferric_enzyme, fervent_enzyme, frigid_enzyme, incandescent_enzyme,
  memory_ferric_transmutation, neurotic_enzyme, recycled_enzyme, ruinous_enzyme, umbral_enzyme, vivacious_enzyme
- `textures/item/scars/` (3): scar_ichor, scar_patch, scar_pattern
- `textures/misc/` (6): laser, laser2, laser_glow, vine, wisp_large, wisp_small
- `textures/mna/` (3): blank, dodo, resource_bars
- `textures/mob_effect/` (1): mana_to_blood
- `textures/models/morphling/` (1): attachment_debug
- `textures/particle/` (12): blood_cell_rotations, blood_cell_rotations75, blood_skull, chain, complex_lightning_bolt,
  particle_glow, particle_glow_new, particle_kunai, particle_metal, particle_mote, sparkle_2, sparkle_3
- `textures/particle/abocipher/` (3): green, tan, white

Archived and kept out of the jar (233, not counted above): `item/old/` 159 (19 of them in a folder with a space in
its name, `item/old/sanguine formation/`), `gui/old/` 22, `block/old/` 20, `entity/drudge/model_drudge_grey_old (1..6)`
6, plus 6 "- Copy" files (4 in `models/armor/`, and one each in `gui/` and `environment/`).

Reached only through built paths (349): not orphans, but they cannot be checked statically. Each built path is
listed in `orphans.txt` under `dynamic_prefix` and `dynamic_suffix`.

## Appendix C. Orphan and stale models

- Strict orphans (12): `block/budding_bog_body`, `block/earthen_vein_stented_named`, `block/erythrocytic_dirt_snow`,
  `block/ghastly_alembic_athanor_growth`, `block/ghastly_alembic_condenser_growth`, `block/rune_mod_station`,
  `block/sanguine_pane_noside`, `block/sanguine_pane_noside_alt`, `block/sanguine_pane_side_alt`,
  `item/scar_mod_station`, `item/stabilized_sanguine_formation`, `item/verdigris_censer`.
- Item models for ids that are not registered (77). Their override chains, such as `blood_gourd_*_open`, go with them:
  - abyssal_ichor, active_befouling_ash_trail, active_smouldering_ash_trail, arm_banner, befouled_vinteum_dust
  - blood_gourd_black_open, blood_gourd_red_open, blood_gourd_white_open, blood_shot_occulus, blood_tendency_gauge
  - bloodline_pool_monitor, broken_mana_trapazahedron, cured_clay_flask, curved_horn_open, desiccated_membrane
  - dicentra_sap, earthen_vein_open, earthly_transfuser, field_notes, foul_vinteum_ingot, frozen_clot
  - harbinger_grimore, hematic_field_ink, hemorath_rib_open, house_marker_giving_light, iron_knapper
  - living_grasp, living_infused_thread
  - living_staff_planted and its variants: assumed_limb, barbed_fitting, chitinite_fitting, chitinite,
    crimson_vestment, fungal, leech, monolithic_frame, pests, prismatic_fitting, serpent, worn_vow
  - living_stent, living_syringe_stent, living_thread_boots, living_thread_hood, living_thread_leggings
  - living_thread_robes, living_will, mana_infushed_memory_blank, mana_memory_sanguine_transfusion, mark_of_blood
  - molten_scab, mote_blood, obsidian_knapper, pale_field_ink, pale_silver_pickaxe, raw_clay_flask
  - roasted_gourd_seeds, runic_chisel_station, saint_enzyme, scar_binder, scar_binder_upgraded, scar_mod_station
  - serpent_scale_block, shred_of_animus
  - spawn_egg_abyssal_siphon, spawn_egg_cruor_fiend, spawn_egg_drudge, spawn_egg_frozen_clot,
    spawn_egg_synapse_hound, spawn_egg_void_drinker, spawn_egg_wretched_will
  - spell_texture, stabilized_sanguine_formation, venous_recaller, verdigris_censer, void_ichor

  Check before deleting: the `living_staff_*_planted` models are loaded by name through
  `CardinalRitePlantedStaffModels.uniqueModelNames()` (`ModelRegistrar.java:76-77`); keep any that list returns.
  `house_marker_giving_light` and the `*_open` models may be override targets.
- Authored blockstates for unregistered blocks (2): `blockstates/grass.json`, `blockstates/serpent_scale_block.json`.
- Stale generated item models (4): `abocipher_emitter`, `qliphoth_bloom`, `warp_chair_filler`,
  `placed_blood_stained_stone`.

## Appendix D. Unused shader, sound and particle files

- Core shaders never registered (9): `aoe_gun_projectile`, `lodestone_texture`, `particle/particle`, `radial_menu`,
  `runic_energy_flow`, `screen/screen_particle`, `shapes/scrolling_texture`, `shapes/scrolling_triangle_texture`,
  `shapes/triangle_texture`. Their `.vsh` and `.fsh` files go with them.
- Post chains, none used (7): `bloom`, `dimension_portal`, `energy_pylon`, `gravitational_lens`, `lens`,
  `orbital_explosion`, `wave`. Programs (9): `energy_pylon`, `gravitational_lens`, `horizontal_bloom`, `lens`,
  `vertical_bloom`, `wave`, and the `dimension_portal/` and `orbital_explosion/` sub-programs.
- Include never imported (1): `shaders/include/color_sorting.glsl`.
- Particle JSONs with no registered type (2, AN-28): `blue_sparkle_velocity`, `trail_orbit`.
- OGGs referenced by no sound event (57, AN-15):
  - `entity/beast_from_beyond/*` 6
  - `entity/colin/*` 6, of which the 3 `OLD*` files are excluded from the jar
  - `entity/dark_young/*` 7
  - `entity/denizen/*` 3
  - `entity/dread_bot/*` 3
  - `entity/hastur/*` 7
  - `entity/samples/*` 11
  - `entity/seraphim/*` 6
  - `entity/tentacle/*` 4
  - `item/bow_blade/{close,open}` 2
  - `item/star_slug/{storm,strike}` 2

## Appendix E. Cross-references to the other 2026-11-02 audits

Items in `entity-animation-and-effects.md` that touch asset integrity and are not repeated above:

- AN-2: `hematic_construct` uses the vanilla zombie model and texture.
- AN-9: thirteen committed models come from generators that are not in the repository; it covers VS-2's microscope.
- AN-10: the missing Tooth Pecks OGGs.
- AN-11 and AN-12: vanilla dust and other vanilla particles in manipulations and focal systems.
- AN-15: the unreferenced OGGs.
- AN-28: the orphan particle JSONs.
- AN-29: the Ringmaster finale has no boss presentation.

`progression-and-content-gaps.md` PG-8 covers the WIP filing of the Blood Trial Altar and other live blocks (VS-12).
