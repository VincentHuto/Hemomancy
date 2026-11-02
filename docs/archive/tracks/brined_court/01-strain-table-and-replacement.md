# Track 1: Strain table and replacing the old strains

**Status:** DONE 2026-10-30. The `MorphlingStrains` table and the eight house strains landed (items, passive effects, Latin, tendencies, Cradle flags, Last Rite ids, eight partner-organism polyp families, icons, recipes, placeholder text) and the first-attempt strains are gone from `src/`. Left: power ladders and home conditions (Track 2), attachment art, HUD sprites and polyp geometry (Track 3), final text and books (Track 4). See "Handover" at the end.

Phase A. Lands first. Spec sections: "The new strains", "Classification names", "Implementation notes".

## Goal

Delete the eight first-attempt strains and add the eight house strains as new items, with their Latin, tendencies and icons. Before adding them, replace the strain lists scattered through the code with one table, so this change and every later strain change edits one place.

**Decided 2026-10-29: no migration.** The mod isn't live. The old strains (Lumenlace, Irontooth, Deadman's Purse, Witch's Ear, Bootlace, Winter Shroud, Gravecap, Emberfang) were a first attempt and are removed outright, not renamed or converted. There are no registry aliases, no `MorphlingMigrationRules` entries for them, no `PlayerDataMigrations` entry and no save fixtures. Old Morphlings in dev worlds simply become unknown items. The spec's "replaces" and "carries forward" notes are only a guide to which old code is worth reusing in Track 2; they're not a mapping that has to be preserved.

## Why the table comes first (SETTLED 2026-10-29, M1)

There's no strain enum or data record today. A strain is its item class plus about fifteen hand-kept lists keyed by item holder or id:

| Where | What it hard-codes |
| --- | --- |
| `common/init/ItemInit.java` (≈676–691) | 8 `morphling_<id>` holders |
| `common/init/EffectInit.java` (≈210–250) | 8 passive effects `morphling_<id>` |
| `MorphlingItem.morphlingPassiveEffects()` | the same 8 effect holders |
| `MorphlingMigrationRules` `ID_MIGRATIONS`, `LAYER_FAMILY_MIGRATIONS` | legacy animal ids and polyp families mapped to strains |
| `CreativeTabSection.java:56`, `CreativeTabGroup.java:71` | creative filing |
| `client/screen/skilltree/shared/MaterialsData.java` (≈649), `MaterialAtlasSpec.java` (≈199) | atlas predicates and coordinates per strain |
| `SpecimenBestiaryDefinitions.MORPHLING_ENTRIES` | polyp layer → item → Bestiary id |
| `client/screen/overlay/MorphlingHudVisuals.java` | id → HUD texture and accent colour |
| `client/registry/ItemPropertiesRegistrar.java:134` | jar model branch for emberfang/lumenlace |
| `LastRiteHelper.sourceIdForMorphling` | Lumenlace → ink_mantle / last_light, Winter Shroud → cryptobiosis |
| `MorphlingCradleBlockEntity.isCradleSupportedPrimalStrain` | 5-item Cradle whitelist |
| `EquippedMorphlingEvents` (`applyHotheadedTick`, `scaleExhaustion`), `mixin/util/MixinHooks.java` | Emberfang-only branches |
| `client/model/morphling/MorphlingMutationRegistry.init()` | one visual mutation per strain (Track 3 owns this) |
| `MorphlingPolypLayer` enum | 12 legacy animal families with colour and catalyst |

Add `common/item/harbinger/morphlings/MorphlingStrain` (a record) and `MorphlingStrains` (the ordered table of eight). One row holds:

- `id` (`"sunburst"`), item holder, passive effect holder
- preferred and secondary `EnumBloodTendency`
- binomial lang key and folk-name lang key
- house (a `CourtHouse` enum: `GIVING_LIGHT, HOLDING_STONE, SHARED_WAKE, WOVEN_CURRENT, VEILED_FOLD, STILL_BELL, LAST_MARROW, KINDLING`, shallow to deep; it starts here, and Track 5 may move it to `common/capability/player/harbinger/court/` if player state needs it)
- home condition (a `MorphlingHomeCondition` reference, filled in by Track 2)
- `cradlePrimal` flag
- the Last Rite id it can arm and at which stage
- polyp family (below)
- HUD texture name and accent colour (the client reads it; the colour is an int, so `common/` stays client-free)
- creative order

Every list above then reads the table. Add `MorphlingStrainTableGuardrailTest` under `src/test/java/.../architecture/` that fails on a hard-coded `morphling_<strain>` reference outside the table and `ItemInit`.

Since nothing has to survive, there's no need to refactor with the old names first. Build the table with the eight new rows, delete the old items, classes and resources in the same change, and get the tree compiling. Track 2 fills in behaviour.

## The eight strains (SETTLED names, ids and tendencies, 2026-10-29)

| Id | Folk name | Binomial | Preferred / secondary | Cradle at Primal | Last Rite |
| --- | --- | --- | --- | --- | --- |
| `sunburst` | Sunburst | *Heliophyta donans* | Lux / Ferric | yes | — |
| `holdfast` | Holdfast | *Hapterophyta tenax* | Ferric / Animus | yes | — |
| `mermaids_purse` | Mermaid's Purse | *Sanguibursa communis* | Animus / Ductilis | yes | — |
| `dead_mans_rope` | Dead Man's Rope | *Funiculospora nexilis* | Ductilis / Tenebris | no | — |
| `ink_cap` | Ink Cap | *Atramentaria velata* | Tenebris / Ductilis | no | `ink_mantle` at Apex |
| `hushbell` | Hushbell | *Campanospora silens* | Congeatio / Tenebris | yes | `cryptobiosis` at Primal |
| `marrowplume` | Marrowplume | *Medullophyta ossivora* | Mortem / Congeatio | yes | — |
| `kinbud` | Kinbud | *Pyrrhiza gemmans* | Flammeus / Mortem | no | — |

Item ids are `morphling_<id>`, matching the icon pack's file names.

## What gets removed

- The eight old item classes, their rule helpers (`DeadmansPurseFeedBankRules`, `EmberfangHeatRules`, `LumenlaceCamouflageRules`, `WinterShroudResilienceRules`) once Track 2 has taken what it reuses, their passive effect classes and registrations, item models, textures, incubator recipes, HUD sprites and lang keys.
- The unregistered leftovers `ChitiniteMorphlingItem`, `PestsMorphlingItem`, `TickMorphlingItem`, `UrchinMorphlingItem`, plus `morphling_cuttlefish.png` and `morphling_urchin.png`.
- `LastRiteHelper.LAST_LIGHT_ID` and its branch. `ink_mantle` and `cryptobiosis` keep their ids and move to Ink Cap and Hushbell through the table.
- `MorphlingMigrationRules` in full, if nothing outside Morphlings uses it: the legacy animal-id map and the polyp-family map exist only to carry old saves forward. Remove `SpecimenBestiaryState.recordMorphlingLayer`'s call to `migrateLayerFamily` with it.
- `MorphlingStrainReframeResourceTest`, `MorphlingLumenlaceRenameResourceTest`, `MorphlingMigrationRulesSourceTest` and `MorphlingRenameScanScopeTest` were written to pin the old names and their migration, so delete them rather than rewrite them. Keep a simple scan that the eight old names no longer appear in `src/` (docs are free to mention them as history).

## Wild polyp families (SETTLED 2026-10-29, M5)

`MorphlingPolypLayer` has 12 animal families (cuttlefish, urchin, …), each with a colour and a catalyst item, which the Bestiary records and incubation reads. With no migration to protect, replace them with **eight families, one per strain, named for the strain's partner organism** (sun alga, coralline alga, gut bacteria, light bacteria, black yeast, cold bacteria, bone bacteria, vent bacteria). Each family's catalyst comes from the strain's house water: for example, Hematic Algal Crust for Sunburst, a Chiton-region drop for Holdfast, a Hagfish drop for Marrowplume, a vent or Chalybeate drop for Kinbud. Track 2 picks the final catalysts with the incubator recipes. The Bestiary then records eight families, one per strain.

## Resources

- Item textures: install the eight sprites from `docs/archive/concept/output/brined-strain-icons-16x16/16x16/` as `textures/item/morphling_<id>.png`. If a sprite changes, regenerate from the pixel maps with that folder's `export-sprites.ps1`.
- Item models `models/item/morphling_<id>.json`. The jar model branch in `ItemPropertiesRegistrar` reads the table.
- Incubator recipes `data/hemomancy/recipe/incubator/morphling_<id>.json` with the new catalysts.
- `recipe/mind_spike.json` and its recipe advancement use `morphling_winter_shroud`: pick its new ingredient (Hushbell is the obvious successor, but it's a free choice now).
- `en_us.json`: new keys per strain (folk name, binomial as the greyed subtitle, description, maturity lines). Track 4 writes the text; placeholders are fine here.
- HUD overlay sprites under `textures/gui/morphling_overlay/`: placeholders now, redrawn by Track 3. Delete `old/`.

## Tests

- New: `MorphlingStrainsTest`. It checks that there are eight rows with unique ids, that every tendency is preferred exactly once, that the Cradle set matches the spec, and that Last Rite ids appear only on Ink Cap Apex and Hushbell Primal.
- New: the old-name scan above.
- Rewrite the rest of the Morphling tests that remain (`MorphlingAbilityRulesTest`, `MorphlingEnvironmentRulesTest`, `MorphlingPassiveEffectPresentationTest`, the HUD and Bestiary order tests) against the new strains as Track 2 lands their behaviour.
- Goldens: `creative-tab-order.txt` (≈277–286), `registry-ids.txt`.

## Proof

`./gradlew.bat test` and the Morphling GameTests compile and pass against the new table. Then a fresh dev world: each strain in creative, incubated from its polyp family, equipped, and shown in the HUD and the Bestiary.

## Docs

`docs/HEMOMANCY_REFERENCE.md` §16.1 types table (ids, names, tendencies, Cradle list). Track 4 does the full text rewrite.

## Decisions

All answered 2026-10-29: the table lands with the new strains (M1), the ids above (M2), the secondary tendencies follow the gradient (M4), and the wild polyp families become eight partner-organism families (M5).

## Handover (written 2026-10-30, unattended run)

### What landed

- `common/item/harbinger/morphlings/`: `MorphlingStrain` (one row: id, tendencies, `CourtHouse`, `MorphlingHomeCondition` (all `UNSET`), Cradle flag, Last Rite id and stage, `MorphlingPolypLayer` family, HUD texture and ARGB accent, creative order, lazily created item and passive-effect holders), `MorphlingStrains` (the ordered table plus `of`, `byId`, `byItemPath`, `byPolypFamily`, `is`, `isCradlePrimal`, `lastRiteFor`), `CourtHouse`, `MorphlingHomeCondition`, `HouseMorphlingItem` (tendencies, Latin and passive from the row) and the eight `<Strain>MorphlingItem` classes. Placeholder powers: every strain keeps its passive status effect up; Ink Cap carries Ink Reprieve at Apex (`hemomancy:ink_mantle`, 500 mL) and Hushbell Cryptobiosis at Primal (`hemomancy:cryptobiosis`, 650 mL), both moved from the old classes.
- Registration: `ItemInit` holds the eight `morphling_<id>` items; `EffectInit.MORPHLING_PASSIVES` registers one `MorphlingStrainEffect` per row (no per-strain effect fields).
- Every former hand-kept list now reads the table: the `MorphlingItem` passive set, `LastRiteHelper` (Last-Light Mantle retired), `MorphlingCradleBlockEntity`, `CreativeTabGroup.MORPHLINGS`, `MaterialsData` and `MaterialAtlasSpec`, `SpecimenBestiaryDefinitions`, `MorphlingHudVisuals`, the Living Staff `staff_visual` predicate (table order 1-8), `MorphlingMutationRegistry` (placeholder attachments), muscle-memory signatures, the villager trade and the Mycelial skill icons (both now Marrowplume).
- `MorphlingPolypLayer` is now eight families (sun alga, coralline alga, gut bacteria, light bacteria, black yeast, cold bacteria, bone bacteria, vent bacteria) with new spawn contexts in `MorphlingPolypLayerRules`; `MorphlingPolypLayerModel` maps each to an old appendage as a placeholder.
- Deleted: the eight old item classes, the four unregistered leftovers, `MorphlingMigrationRules` (and the Bestiary's layer migration), the old models, textures, effect icons, HUD sprites (including `old/`), recipes and lang keys, and `textures/item/old/morphling_*`. The four old rule helpers stay for Track 2 under neutral names: `MorphlingHeatRules`, `MorphlingFeedBankRules`, `MorphlingCamouflageRules`, `MorphlingColdResilienceRules` (nothing calls them yet).
- The Emberfang and Winter Shroud hooks were removed from `EquippedMorphlingEvents` (the incoming-damage handler is gone) and `MixinHooks` (powder snow now honours only Cryoprotection; exhaustion only Clarity).
- Resources: the eight 16x16 icons from `docs/archive/concept/output/brined-strain-icons-16x16/16x16/` as item textures and as passive-effect icons; item models; incubator recipes; `mind_spike` now takes a Hushbell; placeholder HUD sprites (copies of the old ones by the spec's pairing); placeholder lang for effects, atlas text, Bestiary families and Alchemist surrender options. Names and Latin are final.
- Tests: new `MorphlingStrainsTest`, `MorphlingStrainTableGuardrailTest` (strain ids only in the table and `ItemInit`; no old strain name anywhere in `src/`; indexed in `ARCHITECTURE_STANDARDS.md`) and `MorphlingStrainGameTests` (registration, incubation by family catalyst, worn passive, Bestiary yield, Last Rite and Cradle). Rewritten: `MorphlingPassiveEffectPresentationTest`, `MorphlingEnvironmentWiringSourceTest`, the HUD and Bestiary order tests, the polyp family tests. Deleted as planned: `MorphlingStrainReframeResourceTest`, `MorphlingLumenlaceRenameResourceTest`, `MorphlingMigrationRulesSourceTest`, `MorphlingRenameScanScopeTest`, `MorphlingEnvironmentGameTests`; `MorphlingStage4To8SourceTest` was cut to its strain-neutral checks.
- Goldens regenerated, not hand-merged: `frozen/registry-ids.txt`, `frozen/creative-tab-order.txt`, `dialogue/route-fingerprints.txt`. No payload changed, so `PROTOCOL_VERSION` is untouched.
- Docs: HR §16.1 rewritten (types table with Latin, tendencies, house, Cradle, Last Rite, polyp family and catalysts), plus §16.2 families, §16.3.1, the §16.4 Cradle list, §16.5 attachments and the effects table.

### GameTests removed for Track 2 to replace

They pinned old strain powers, so they went with the strains: Irontooth Primal in `CombatTargetingMatrixGameTests`; the Bootlace staff Primal charge test in `HarbingerRepairGameTests` (re-add with a real Primal: charges once, rejects a cooldown without Staff wear); Gravecap's periodic-kill husbandry and Winter Shroud's Tun Molt in `SchoolCombatGameTests`; and `MorphlingEnvironmentGameTests` (Emberfang heat, Winter Shroud cold). The Lumenlace rescue test became `inkCapReprieveKeepsLethalThresholdAndPeriodicProtection` and passes.

### What remains for Tracks 2-4

- Track 2: the power ladders, home conditions (`MorphlingStrain.homeCondition()` is `UNSET`), final catalysts, the shared mechanics, Kinbud heat through `MorphlingHeatRules`, and the GameTests above.
- Track 3: real attachments (the registry reuses old models), HUD sprites (copies of old ones), polyp appendage geometry per family.
- Track 4: descriptions, Bestiary entries and surrender options are first drafts; Liber pages, LORE_REFERENCE, the wiki and `docs/MORPHLING_STRAINS.md`.
- Proof gap: nobody walked a fresh dev world by hand. Equip, passive, Bestiary yield and incubation are proven server-side by `MorphlingStrainGameTests`; the creative tab was captured in a real client (`creativeTabReviewClient -PcreativeLabel=brined1`, `build/creative-tab-review/screenshots/creative-brined1-7-hematic-morphlings.png` shows the eight new icons). The HUD and Bestiary screens were not captured.

### Calls made unattended

- The table is the only per-strain list. `ItemInit` still declares the eight holders (the guardrail allows it), and `EffectInit` registers the passives by looping over the table, so there is no per-strain effect field. The table builds its holders lazily, so it loads in plain JUnit.
- `MorphlingStrain` is a final class, not a record, so its registry holders can be created on first use.
- First-pass catalysts (Track 2 picks the finals), each with Rotten Flesh as before: Sunburst Hematic Algal Crust and Sea Pickle; Holdfast Chitinous Husk (Chiton drop) and Kelp; Mermaid's Purse Raw Pelagic Herring and Cod; Dead Man's Rope Ganglion Cluster (Siphonophore drop) and Glow Ink Sac; Ink Cap Ink Sac and Chromatophores; Hushbell Cleansing Hemolymph (Ice Fish drop) and Packed Ice; Marrowplume Bone Worm Colony and Bone (the Hagfish has no drop); Kinbud Giant Tube Worm Colony and Magma Cream. Each family's hint item is its first catalyst.
- Wild polyp spawn contexts for the new families follow the house waters by analogy: sun alga for open ground and shallows, coralline alga for stone, cold bacteria for taiga and high stone, vent bacteria for heat and deep stone, and so on. Sun alga is the fallback.
- Muscle-memory signatures follow the spec's replacement pairing, except Coursing Legs, which moved from Emberfang to Mermaid's Purse (the speed hunter) along with its Sanguine Fists momentum; Sanguine Fists itself goes to Kinbud.
- Living Staff `staff_visual` values 1-8 follow table order; the eight staff models are unchanged.
- Placeholder art: HUD sprites copy the old strain each replaces; mutation attachments and polyp appendages reuse old models (the pairing is in HR §16.5).
- Accent colours: Sunburst gold, Holdfast stone, Mermaid's Purse cobalt, Dead Man's Rope violet-blue, Ink Cap grey-violet, Hushbell frost, Marrowplume blood red, Kinbud flame orange.
- Mind Spike's ingredient is Hushbell.
- Creative filing: the jar, polyp and strains sit only in the Hematic Morphlings group. HEAD also filed the jar and polyp under the Brined Court section, which put the jar after the Arsenal and failed `builtCreativeTabsPreservePlayerToolOrderAndSeparateDebugTools`; dropping that filing made the registration suite pass. I also dropped the dead `hemorath_rib` section filing (the item is on the WIP list), which was failing `CreativeTabSectionTest` at HEAD. Either can be moved back if the creative-tab owner wants Morphlings in the Brined Court band, but that test then needs updating.
- **Review fix (2026-10-30, user):** the Morphling jar, the polyp and the eight strain items are filed in the Brined Court section of the main creative tab again (`CreativeTabSection.withMorphlings`), where the user had them before this track. They stay listed in `CreativeTabGroup.MORPHLINGS` too; the section filing wins.
