# Morphling Fungal-Strain Reframe Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Design spec:** [2026-07-02-morphling-fungal-strain-reframe-design.md](../specs/2026-07-02-morphling-fungal-strain-reframe-design.md) — read it first; this plan implements it verbatim and does not re-argue decisions.

**Goal:** Consolidate the 12 animal-mimic morphlings into 8 original fungal strains (one per blood tendency), migrate the cut species' best abilities into the survivors, remove all armor-lane (contact-reactive) abilities, introduce the folk-name/binomial naming presentation, retarget the incubator/polyp/cradle/bestiary pipelines, and migrate existing save data losslessly.

**Architecture:** The `MorphlingItem` base class, `IEquippedMorphling` capability, jar/staff/cradle stations, and the client mutation layer all survive unchanged in *shape* — this is an identity-and-ability reshuffle, not a systems rewrite. Species behavior stays in per-strain `MorphlingItem` subclasses. A new `MorphlingMigrationRules` helper owns every old→new stack conversion so migration is testable in isolation. Ability re-roles follow the ownership laws in [POWER_SYSTEMS_AUDIT.md](../../POWER_SYSTEMS_AUDIT.md) §4: no on-hit/contact triggers (armor's lane), ecology/state/resource/movement verbs only.

**Tech Stack:** NeoForge 1.21.1, Java 21, Hemomancy DeferredRegister patterns (`ItemInit.BASEITEMS`), stack custom data (`EnzymePower` float, `Primalized`/`WildBound` markers — see `MorphlingItem` lines ~35–120), `IncubatorRecipe` JSONs, `MorphlingMutationRegistry` client layer, focused resource/source tests.

---

## Current-state anchors (verified 2026-07-03)

| Concern | Anchor |
|---|---|
| Base + species items | `common/item/harbinger/morphlings/MorphlingItem.java` + 12 `*MorphlingItem` subclasses, `IMorphling`, `PrimalMorphlingRules`, `WildMorphlingRules` |
| Registrations | `common/init/ItemInit.java` lines ~537–561 (`morphling_fungal` … `morphling_mole`), `morphling_jar` at ~587, polyp spawn egg at ~1007 |
| Stack data keys | `"EnzymePower"` (float), `MorphlingItem.PRIMALIZED_KEY`, `MorphlingItem.WILD_BOUND_KEY`, `MATURITY_THRESHOLDS`, `PrimalMorphlingRules.APEX_LEVEL` |
| Equipped state | `common/capability/player/harbinger/morphling/EquippedMorphling(.java/Events)`, `SyncEquippedMorphlingPacket` |
| Client mutation | `client/morphling/MorphlingMutationRegistry.java`, `MorphlingVisualMutation`, `MorphlingModelAttachment`, `client/render/layer/player/MorphlingMutationLayer.java`, models under `client/model/entity/summon/`, bbmodels under `assets/hemomancy/models/entity/bbmodel/morphling/` |
| Incubation | `data/hemomancy/recipe/incubator/morphling_*.json` (12 files), `MorphlingIncubatorBlockEntity` |
| Wild pipeline | `ItemMorphlingPolyp` (+ `MorphlingLayers` custom data), `WildMorphlingRules`, `SpecimenJarBlockEntity`, `common/capability/player/harbinger/bestiary/SpecimenBestiaryProgress.java` |
| Cradle | `MorphlingCradleBlockEntity` (cradle-suitable set currently: Fungal, Leeches, Chitinite, Pests, Urchin — **three of five are being cut**, so this set must be retargeted) |
| Config | `HemoServerConfig` `morphling` section: `morphlingPassiveDrainEnabled` (true), `morphlingDrainRate` (0.5), `morphlingDrainInterval` (60) — currently unused because `getBloodCost()` returns 0 |

## The identity map (from the spec, restated for implementers)

| Old id | New id | Folk name | *(binomial tooltip)* | Tendency | Fate |
|---|---|---|---|---|---|
| `morphling_leeches` | `morphling_deadmans_purse` | Deadman's Purse | *Sanguibursa vorax* | Animus | rename + absorb Tick |
| `morphling_fungal` | `morphling_gravecap` | Gravecap | *Necrophyta saprovex* | Mortem | rename + absorb Pests |
| `morphling_bat` | `morphling_witchs_ear` | Witch's Ear | *Tympanospora susurra* | Ductilis | rename |
| `morphling_cuttlefish` | `morphling_foxfire` | Foxfire | *Ignisfatuus lucens* | Lux | rename + re-role flash |
| `morphling_spider` | `morphling_bootlace` | Bootlace | *Rhizomorpha tenebra* | Tenebris | rename + absorb Pests area-denial |
| `morphling_mole` | `morphling_irontooth` | Irontooth | *Ferrophyta lithovora* | Ferric | rename + absorb Urchin harvest |
| `morphling_serpent` | `morphling_emberfang` | Emberfang | *Pyrrhiza digestans* | Flammeus | rename |
| `morphling_centipede` | `morphling_winter_shroud` | Winter Shroud | *Sporangia dormiens* | Congeatio | reskin + absorb Chitinite plating + Urchin pulse |
| `morphling_chitinite` | — | — | — | — | **cut** → migrate stacks to `morphling_winter_shroud` |
| `morphling_urchin` | — | — | — | — | **cut** → migrate stacks to `morphling_winter_shroud` |
| `morphling_pests` | — | — | — | — | **cut** → migrate stacks to `morphling_gravecap` |
| `morphling_tick` | — | — | — | — | **cut** → migrate stacks to `morphling_deadmans_purse` |

---

## File Structure

- Create `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/MorphlingMigrationRules.java`: old→new id map, cut→survivor map, data-preserving stack rewrite, polyp `MorphlingLayers` remap, bestiary id remap. Pure/static, unit-testable.
- Create 8 strain classes in `common/item/harbinger/morphlings/` (`DeadmansPurseMorphlingItem`, `GravecapMorphlingItem`, `WitchsEarMorphlingItem`, `FoxfireMorphlingItem`, `BootlaceMorphlingItem`, `IrontoothMorphlingItem`, `EmberfangMorphlingItem`, `WinterShroudMorphlingItem`) — initially by renaming/refactoring the 8 surviving classes, then deleting the 4 cut classes once their absorbed abilities are moved.
- Create `src/main/java/com/vincenthuto/hemomancy/common/event/MorphlingMigrationEvents.java`: login + container/inventory sweep applying `MorphlingMigrationRules` to player inventories, jars, cradles, and item entities.
- Modify `common/init/ItemInit.java`: replace the 12 registrations with 8 new ids (keep `morphling_polyp`, `morphling_jar`, spawn egg).
- Modify `MorphlingItem.java`: binomial tooltip line, hunger-state fields (Task 8), harvested-material hooks.
- Modify `WildMorphlingRules.java`, `ItemMorphlingPolyp.java`: layer-family → strain retarget.
- Modify `MorphlingCradleBlockEntity.java`: cradle-suitable set + Primal area behavior retarget.
- Modify `client/morphling/MorphlingMutationRegistry.java` + attachment model classes: 8 strain silhouettes.
- Replace `data/hemomancy/recipe/incubator/` 12 JSONs with 8; update lang, item models/textures, JEI category contents (data-driven — verify only).
- Modify `docs/MORPHLING_REFERENCE.md`, `docs/HEMOMANCY_REFERENCE.md` §16 at the end.
- Tests: `src/test/java/.../MorphlingMigrationRulesTest.java`, `MorphlingStrainResourceTest.java` (+ update any existing morphling resource tests).

---

### Task 1: Migration rules helper (write this FIRST — everything else leans on it)

**Files:**
- Create: `common/item/harbinger/morphlings/MorphlingMigrationRules.java`

- [ ] **Step 1: Encode the identity map.** Static `Map<ResourceLocation, ResourceLocation> RENAMES` (8 entries) and `Map<ResourceLocation, ResourceLocation> CUT_TO_SURVIVOR` (4 entries per the table above). Expose `Optional<ResourceLocation> migrateId(ResourceLocation oldId)`.
- [ ] **Step 2: Data-preserving stack rewrite.** `ItemStack migrateStack(ItemStack old)`: if the item id matches a map entry, build a new stack of the target item with count 1 and **copy all custom data verbatim** (`EnzymePower`, `Primalized`, `WildBound`, feed history, cooldowns, nectar marker). For cut→survivor conversions, clamp nothing — a Primal Tick becomes a Primal Deadman's Purse; generosity beats item loss.
- [ ] **Step 3: Layer-family remap.** `String migrateLayerFamily(String old)` for polyp `MorphlingLayers` values and `SpecimenBestiaryProgress` recorded family strings (same rename map, string-keyed).
- [ ] **Step 4: Compile gate.** Run `./gradlew compileJava` (helper has no callers yet — only helper-internal errors matter).

### Task 2: Re-register the 8 strains

**Files:**
- Modify: `common/init/ItemInit.java`
- Rename (IDE refactor): the 8 surviving `*MorphlingItem` classes to their strain names; do **not** delete the 4 cut classes yet (their ability code is harvested in Tasks 4–6).

- [ ] **Step 1:** Rename surviving classes (`LeechesMorphlingItem`→`DeadmansPurseMorphlingItem`, `FungalMorphlingItem`→`GravecapMorphlingItem`, `BatMorphlingItem`→`WitchsEarMorphlingItem`, `CuttlefishMorphlingItem`→`FoxfireMorphlingItem`, `SpiderMorphlingItem`→`BootlaceMorphlingItem`, `MoleMorphlingItem`→`IrontoothMorphlingItem`, `SerpentMorphlingItem`→`EmberfangMorphlingItem`, `CentipedeMorphlingItem`→`WinterShroudMorphlingItem`). Update each strain's preferred/secondary tendency to the spec's single-tendency identity (primary = its column; keep one thematic secondary each for the 75%-power enzyme rule).
- [ ] **Step 2:** In `ItemInit`, replace the 12 `morphling_*` registrations (lines ~537–561) with the 8 new ids. Keep `morphling_polyp`, `morphling_jar`, and the polyp spawn egg untouched. Delete the 4 cut registrations.
- [ ] **Step 3:** Item model JSONs + textures: add 8 `assets/hemomancy/models/item/morphling_<strain>.json` (reuse/retint the surviving species' textures as placeholders — Gravecap keeps the fungal texture as-is; final art is a separate pass) and remove the 4 cut model JSONs. Add lang entries: `"item.hemomancy.morphling_deadmans_purse": "Deadman's Purse"` etc.
- [ ] **Step 4: Compile gate.** `./gradlew compileJava`, then `./gradlew build` — expect failures only in files referencing old ids (creative tab, recipes tests); fix references as they surface. Do not proceed with red references.

### Task 3: Naming presentation (folk primary + binomial subtitle)

**Files:**
- Modify: `MorphlingItem.java`, lang file, `SpecimenBestiaryProgress`-backed Bestiary screen class

- [ ] **Step 1:** Add `protected abstract String binomialKey();` (or a constructor field) per strain; in `appendHoverText`, insert one greyed, italic line under the name: `Component.translatable(binomialKey()).withStyle(GRAY, ITALIC)`. Lang: `"morphling.hemomancy.deadmans_purse.binomial": "Sanguibursa vorax"` (×8).
- [ ] **Step 2:** Bestiary tab: render the binomial as a "Classification" line on the selected-specimen panel (locate the Bestiary tab screen wired to `SpecimenBestiaryProgress`; add one text row).
- [ ] **Step 3:** Verify in `./gradlew runClient`: tooltip shows folk name + italic binomial; Bestiary shows classification.

### Task 4: Ability re-roles — cut the armor-lane triggers

Each step names exact old behavior → new behavior. All removed triggers are **on-hit/attacker-contact** (armor's lane per the audit).

**Files:** the 8 strain classes; `common/init/EffectInit.java` if an effect rename is needed.

- [ ] **Step 1 — Foxfire:** remove Mature "Chromatophore Flash" (on-hit attacker blind). Add **Chromatic Camouflage**: while stationary ≥2s in water or light ≤7, apply fading invisibility (re-check each tick; break on move/attack). Keep Sepia Wake (sprint), Ink Mantle (Apex), Last-Light (Primal).
- [ ] **Step 2 — Deadman's Purse:** remove Developing flat lifesteal (Blood Lust armor's bonus). Replace with **feed-banking**: melee hits/kills deposit into the shared `BorrowedBloodReserve` attachment from the [guardrails plan](2026-07-03-audit-phase1-guardrails.md) Task 3, drained before player blood when Primal Hemophage fires; if the guardrails plan has not landed yet, use a stack-local `BorrowedBlood` float (cap ~500) with a `// TODO(guardrails Task 3)` reroute marker. Add Tick's **Blood Fever** as the Mature slot (Speed near wounded entities). Primal Hemophage Covenant absorbs Tick's spread-on-kill flavor.
- [ ] **Step 3 — Winter Shroud (reskin of Centipede):** passive = **Cryptobiotic Hide** (Resistance amplifier scaling while stationary/low HP, replacing Centipede's poison-immunity+speed). Developing = **Tun Plating** (Chitinite's ablative Absorption regen, harvested from `ChitiniteMorphlingItem` before deletion). Mature = **Anhydrobiosis** (Centipede's cleanse, reflavored). Apex = **Tun Molt** (Centipede's Hundredfold-Molt escape + Urchin's Tidal-Anchor knockback pulse on emergence, harvested from `UrchinMorphlingItem`). Primal = **Cryptobiosis** stasis (Centipede Primal rework; register it with `LastRiteRules` as `hemomancy:cryptobiosis` per the [guardrails plan](2026-07-03-audit-phase1-guardrails.md) Task 2, else `// TODO(guardrails Task 2)`).
- [ ] **Step 4 — Gravecap:** Apex Cordyceps Burst gains Pests' **Infest** (kills spawn a small hostile-targeting fungling swarm — harvest the spawn code from `PestsMorphlingItem`). Confirm the relocated fungal-scar fantasy: Vein Orchard-style resource bloom rolls fold into Cordyceps loot rolls (see fungal-scar spec §4 Sanguiflora row).
- [ ] **Step 5 — Bootlace:** Mature = **Web Nest** (place short-lived slowing web terrain — Pests' area-denial re-expressed as silk; new block or reuse temporary cobweb from Silk Tether). Web Cocoon moves from on-hit trigger to a **proximity trap** check (hostile within 2 blocks while sneaking) to exit the contact lane.
- [ ] **Step 6 — Irontooth:** add Urchin's harvest as a cooldown drop: heavy hits taken underground (or mining milestones) shed 1 `chalybeate_sclerite`-family material (pick an existing item; do not add a new one).
- [ ] **Step 7 — Witch's Ear / Emberfang:** no trigger surgery (already ecology/state) — verify only; rename effect display strings where they referenced the old animal.
- [ ] **Step 8:** Delete `ChitiniteMorphlingItem`, `UrchinMorphlingItem`, `PestsMorphlingItem`, `TickMorphlingItem`. Compile gate: `./gradlew compileJava && ./gradlew test`.

### Task 5: Incubator + wild pipeline retarget

**Files:**
- Replace: `data/hemomancy/recipe/incubator/*.json` (12 → 8)
- Modify: `WildMorphlingRules.java`, `ItemMorphlingPolyp.java`

- [ ] **Step 1:** Write 8 incubator recipe JSONs keyed to the new ids and their (new) preferred/secondary tendencies; delete the 4 cut JSONs. Keep costs/timings from the nearest predecessor.
- [ ] **Step 2:** Retarget `WildMorphlingRules` layer-family → strain-item mapping through `MorphlingMigrationRules.migrateLayerFamily` so *existing* jarred polyps with old family strings still convert (a captured pre-migration polyp must remain redeemable).
- [ ] **Step 3:** Update polyp appendage-layer visuals only if layer ids changed; else leave (families are strings, visuals keyed to family).
- [ ] **Step 4:** `./gradlew build` + JEI spot-check in `runClient`: Incubator category shows 8 recipes.

### Task 6: Cradle + jar + staff retarget

**Files:**
- Modify: `MorphlingCradleBlockEntity.java`; verify `ItemMorphlingJar`, Living Staff topper mapping, `MorphlingIncubatorBlockEntity` output slots (id-agnostic — verify only)

- [ ] **Step 1:** Cradle-suitable Primal set was {Fungal, Leeches, Chitinite, Pests, Urchin}; retarget to **{Gravecap, Deadman's Purse, Winter Shroud, Bootlace, Irontooth}** and remap each Primal area behavior to its new owner (Chitinite's ritual fortification → Winter Shroud; Pests' patrol swarm → Bootlace web-patrol or Gravecap funglings — pick Gravecap to match Infest; Urchin's ward → Winter Shroud).
- [ ] **Step 2:** Living Staff topper model map: 8 entries (delete 4). Jar rendering is stack-driven — verify only.
- [ ] **Step 3:** Compile + `runClient` smoke: cradle accepts/buffs with a Gravecap; staff topper changes per strain.

### Task 7: Client mutation layer — 8 silhouettes

**Files:**
- Modify: `client/morphling/MorphlingMutationRegistry.java`, attachment model classes under `client/model/entity/summon/`

- [ ] **Step 1:** Re-key the registry to the 8 new items. Asset triage per spec §9: **reuse as-is** (fungal head → Gravecap; leech arms → Deadman's Purse arms *interim*), **retint/rename** (spider body → Bootlace cords interim; centipede body → Winter Shroud shell interim; mole arms → Irontooth claws; bat head → Witch's Ear interim; cuttlefish head → Foxfire; serpent legs → Emberfang interim), and mark each interim with a follow-up art note in the reference doc.
- [ ] **Step 2:** Regenerate bbmodel examples: `node tools/model_export/java_model_to_bbmodel.mjs --set=morphling --check` and fix drift.
- [ ] **Step 3:** `runClient`: equip each strain at Developing+ and confirm attachment renders, maturity growth scaling works, and multiplayer sync path (`SyncEquippedMorphlingPacket`) still keys correctly (ids changed — confirm the packet carries the stack, not a hardcoded id list).

### Task 8: Hunger + husbandry (spec §11 — ship with the reskin, feature-flagged)

**Files:**
- Modify: `MorphlingItem.java`, `EquippedMorphlingEvents.java`, `HemoServerConfig`

- [ ] **Step 1:** Add `hungerEnabled` (default **false** for the first alpha build — flip after balance pass), `fedDurationTicks`, `starvingDrainRate` to the config `morphling` section alongside the existing drain keys.
- [ ] **Step 2:** Stack data `LastFedGameTime` (long). States: **Fed** (within `fedDurationTicks`), **Hungry** (past it — passive amplifier −1, min 0), **Starving** (past 3×— apply the existing `morphlingDrainRate` drain + Morphic Strain pulse). Feeding = right-click with matching enzyme, or blooded kills while equipped. **Equip sets state to Hungry** (swap friction per audit §4.3).
- [ ] **Step 3:** Husbandry counters: per-strain `ExperienceProgress` int on the stack; increment on the strain's signature verb (Emberfang: poison ticks dealt; Irontooth: blocks mined below y=0; Gravecap: nearby hostile deaths; etc. — one cheap hook per strain). Stage-up now requires `EnzymePower ≥ threshold` **AND** `ExperienceProgress ≥ stage quota` (quota configurable; 0 = disabled, preserving current behavior when the flag is off).
- [ ] **Step 4:** HUD/tooltip: hunger state line on the tooltip + a small icon tint on `MorphlingMutationLayer` (desaturate when Hungry). Compile + focused test on the state math.

### Task 9: Save-data migration events

**Files:**
- Create: `common/event/MorphlingMigrationEvents.java`

- [ ] **Step 1:** On `PlayerLoggedInEvent` + on container open: sweep player inventory, equipped-morphling capability stack, jar contents (`ItemMorphlingJar` inventory), and swap via `MorphlingMigrationRules.migrateStack`. Also sweep `MorphlingCradleBlockEntity` hosted stack on level load/first tick.
- [ ] **Step 2:** `SpecimenBestiaryProgress`: remap recorded layer-family strings on capability deserialize (one-shot version flag so it runs once).
- [ ] **Step 3:** Register in the mod event wiring next to the existing login backfills (pattern: Degree-1 utility-manipulation backfill).
- [ ] **Step 4:** Test-world verification: load a pre-change world save (or spawn old-id stacks via a temporary debug command), confirm conversion of: inventory stack, jar-contained stack, cradle-hosted stack, Primal Tick → Primal Deadman's Purse with `Primalized` intact.

### Task 10: Tests, docs, and closeout

- [ ] **Step 1:** `MorphlingMigrationRulesTest`: every old id maps; custom data survives; cut→survivor targets are registered items; layer-family strings round-trip.
- [ ] **Step 2:** `MorphlingStrainResourceTest`: 8 item registrations, 8 incubator JSONs, 8 lang names + 8 binomial keys, 8 item models, no orphaned `morphling_tick|pests|urchin|chitinite` resources anywhere under `assets/` or `data/` (regression grep).
- [ ] **Step 3:** Update existing morphling-touching tests (registry field loaders, JEI/resource tests) for new ids.
- [ ] **Step 4:** Run `./gradlew build` (full, includes `test`/`check`) — green gate.
- [ ] **Step 5:** Docs: rewrite `docs/MORPHLING_REFERENCE.md` for the 8 strains (names, kits, hunger states); update `HEMOMANCY_REFERENCE.md` §16 and the audit's morphling rows; note interim-art follow-ups.
- [ ] **Step 6:** `runClient` end-to-end: capture wild polyp → bestiary record/surrender → wild-bound (Developing cap) → incubate → Apex → (creative Apotheos) nectar Primalize → cradle host — the full pipeline on at least two strains (one renamed, one merged-into).

## Dependency & sequencing notes

- Tasks 1→2→(3,4 parallel)→(5,6,7 parallel)→8→9→10. Task 8 is feature-flagged and can slip to a follow-up PR without blocking the reskin.
- The borrowed-blood reserve (Task 4 Step 2) and Last-Rite group (Task 4 Step 3) are delivered by the [guardrails mini-plan](2026-07-03-audit-phase1-guardrails.md) — build it first (full family order in [DEFERRED_IDEAS.md](../../DEFERRED_IDEAS.md) §C); the local-TODO fallbacks remain legal if ordering slips.
- Coordinate with the [fungal-scar consolidation spec](../specs/2026-07-02-fungal-scar-consolidation-design.md) (no implementation plan yet) at the single shared point: Gravecap absorbing Sanguiflora's on-kill resource fantasy. The [Wills plan](2026-07-03-rogue-hemomancer-wills.md) is independent of this one.
