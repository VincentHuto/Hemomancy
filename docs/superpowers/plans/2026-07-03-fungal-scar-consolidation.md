# Fungal Scar Consolidation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Design spec:** [2026-07-02-fungal-scar-consolidation-design.md](../specs/2026-07-02-fungal-scar-consolidation-design.md) — read it first; this plan implements the 9 → 8 roster verbatim. Sections referenced as *spec §N*.

**Goal:** Cut four fungal scars (Respergillus, Lumina Devorans, Anastocordyceps nexus, Thanomyces resurgens), relocate Sanguiflora's fantasy to the morphling side, rework Talaromyces (drop Haste, keep hyphal vein-mining), restore Noctifly Agaric unchanged, and add four new scars — Rhizovitta (draw from fungal ground), Putrivora (digest affliction), **Oculiflora** (see the network + Will anchors), Cryostroma (rest-state conserve) — with save-data migration for every removed item.

**Why this plan runs second (after [guardrails](2026-07-03-audit-phase1-guardrails.md), before [morphlings](2026-07-03-morphling-fungal-strain-reframe.md)/[Wills](2026-07-03-rogue-hemomancer-wills.md)):** it is the smallest content change (ideal pipeline shakedown for the migration/testing patterns the bigger plans reuse); it delivers **Oculiflora**, which flips the Wills telegraph render gate from no-op to live; and it removes **Sanguiflora** cleanly before Gravecap absorbs its on-kill fantasy in the morphling plan.

**Architecture:** Fungal scars stay `ItemFungalScar` subclasses in the dedicated `SCARS` fungal slot, defined via `ScarInit`, cultivated in the Mycelial Crucible, with live behavior in the equipment event handler. New scars follow that exact shape. A `FungalScarMigrationRules` helper (mirroring the morphling plan's migration-first discipline) owns cut→survivor stack conversion. Oculiflora's render pass is client-only, gated on the local player's synced scar state, and reuses existing outline/highlight tech.

**Tech Stack:** NeoForge 1.21.1, `ItemInit`/`ScarInit` registration, `fungal_scar_cultivation` recipe JSONs (`data/hemomancy/recipe/fungal_scar/`), `HarbingerEquipmentEntityEventHandler` tick/event hooks, `QliphothBloomClientData` (already-synced bloom positions), `*SourceTest`/`*ResourceTest` conventions.

---

## Current-state anchors (verified 2026-07-03)

| Concern | Anchor |
|---|---|
| The 9 scar items | `common/item/harbinger/scar/fungal/*Item.java` (e.g. `ThanomycesResurgensItem`), registered in `ItemInit` lines ~803–822, definitions in `common/init/ScarInit.java` |
| Base class + slot | `ItemFungalScar` → `SCARS` capability `ScarType.FUNGAL` slot 0 (single equip) |
| Cultivation | `common/tile/crafting/MycelialCrucibleBlockEntity.java`, 9 JSONs in `data/hemomancy/recipe/fungal_scar/`, `immature_fungal_scar` intermediate (ItemInit ~831) |
| Live effect hooks | `common/capability/player/harbinger/equipment/HarbingerEquipmentEntityEventHandler.java` (e.g. `onGlideTick()` for Noctifly), `common/capability/player/harbinger/scar/fungal/VeinMinerHelper.java` (Talaromyces) |
| Bloom positions on client | `client/data/QliphothBloomClientData.java` (synced `BloomEntry` list — Oculiflora reuses this, no new sync needed) |
| Nectar-pool highlight precedent | Mole morphling Primal (Deep Tremor Sense) highlight path — donor code for Oculiflora's POI pass |
| Income routing (soft dep) | `CirculationIncomeHelper` from the [guardrails plan](2026-07-03-audit-phase1-guardrails.md); if absent, add blood directly + `TODO(guardrails)` |
| Wills telegraph gate (downstream) | [Wills plan](2026-07-03-rogue-hemomancer-wills.md) Task 6 Step 4 — `WillAnchorRenderer` checks for this plan's Oculiflora id |

## The migration map (same-tendency survivor rule)

| Removed item | Converts to | Rationale |
|---|---|---|
| `respergillus` (Animus) | `noctifly_agaric` | Animus survivor |
| `lumina_devorans` (Tenebris) | `oculiflora_reticularis` | Tenebris slot successor (night-sight → network-sight) |
| `thanomyces_resurgens` (Congeatio) | `cryostroma_perdurans` | Congeatio slot successor (death-save → conserve) |
| `anastocordyceps_nexus` (Lux) | `oculiflora_reticularis` | No Lux survivor; nearest role (connection/perception) — judgment call, flagged in PR |
| `sanguiflora_cadens` (Mortem) | `putrivora_resolvens` | Mortem slot successor; the on-kill fantasy itself moves to Gravecap (morphling plan Task 4 Step 4) |

---

## File Structure

- Create `common/capability/player/harbinger/scar/fungal/FungalScarMigrationRules.java` — removed-id → survivor map + stack/slot rewrite. Pure, tested first.
- Create 4 item classes in `common/item/harbinger/scar/fungal/`: `RhizovittaCommunisItem`, `PutrivoraResolvensItem`, `OculifloraReticularisItem`, `CryostromaPerduransItem`.
- Create `common/capability/player/harbinger/scar/fungal/RootedStateHelper.java` (Rhizovitta terrain test + state), `AfflictionDigestHelper.java` (Putrivora math), `ConserveStateHelper.java` (Cryostroma stillness ramp).
- Create `client/render/scar/OculifloraRevealRenderer.java` — the network-sight pass.
- Create `common/event/FungalScarMigrationEvents.java` — login/container/scar-slot sweep.
- Modify `ItemInit` (−5 registrations, +4), `ScarInit` (definitions), `HarbingerEquipmentEntityEventHandler` (new tick hooks; strip Talaromyces Haste), `ClientEvents` (renderer registration).
- Data: delete 5 recipe JSONs, add 4 (`rhizovitta_communis.json`, `putrivora_resolvens.json`, `oculiflora_reticularis.json`, `cryostroma_perdurans.json`); item models/textures (retint existing scar art as placeholders); lang; tags: `data/hemomancy/tags/block/fungal_network_ground.json` (terrain set shared by Rhizovitta + Oculiflora).
- Tests: `FungalScarMigrationRulesSourceTest`, `FungalScarRosterResourceTest`, helper math tests.
- Docs at closeout: `HEMOMANCY_REFERENCE.md` §13.2/§13.4, `docs/fungalscar.md`, audit rows, spec status line.

---

### Task 1: Migration rules first

**Files:** create `FungalScarMigrationRules.java` + `FungalScarMigrationRulesSourceTest`.

- [ ] **Step 1:** Encode the migration map above as `Map<ResourceLocation, ResourceLocation>`; `ItemStack migrate(ItemStack)` returns the survivor item (fresh stack — fungal scars carry no meaningful custom data worth preserving; verify that claim by inspecting `ItemFungalScar` and note the finding).
- [ ] **Step 2:** Test: all 5 removed ids map; targets are ids this plan registers; unknown ids pass through untouched. Gate: `./gradlew compileJava` (no callers yet).

### Task 2: The cuts + Talaromyces rework

**Files:** modify `ItemInit`, `ScarInit`, `HarbingerEquipmentEntityEventHandler`; delete 4 item classes + `SanguifloraeCadensItem`; delete 5 recipe JSONs, models, lang lines.

- [ ] **Step 1:** Remove registrations/definitions/classes/resources for the five removed scars. Grep-sweep `assets/` + `data/` + java for each id — zero orphan references (the pattern `FungalScarRosterResourceTest` will enforce in Task 8).
- [ ] **Step 2:** Talaromyces rework: locate its Haste grant (equipment handler or item tick) and delete it; `VeinMinerHelper` shift-mining stays untouched. Update its tooltip/lang to "hyphal-sense mining" wording (spec §4).
- [ ] **Step 3:** Noctifly: verify untouched (`onGlideTick` path green); it simply survives.
- [ ] **Step 4:** Gate: `./gradlew compileJava && ./gradlew build` — expect and fix reference fallout (creative tab, JEI lists, any recipe referencing removed items).

### Task 3: Rhizovitta communis — "draw from the network" (Animus)

**Files:** create item + `RootedStateHelper`; modify `ScarInit`, `HarbingerEquipmentEntityEventHandler`; add `fungal_network_ground` block tag.

- [ ] **Step 1 — Terrain test.** Block tag `fungal_network_ground` (erythrocytic mycelium/dirt, venous stone family, hyphae, conscious mass, infested stone). `RootedStateHelper.isRooted(ServerPlayer)`: standing on/within 2 blocks of tagged ground, **or** inside a bloom radius (`QliphothBloomSavedData` server-side), **or** in the Fungal Dimension. Cache per player per 20 ticks.
- [ ] **Step 2 — Effects.** While Rooted with Rhizovitta equipped: (a) manipulation blood-cost refund of `rootedRefundFraction` (default 15%) — hook the post-cast point in `BloodManipulation.performAction` the same way Sporitic Resonance discounts are applied, as a refund not a pre-discount so it stacks additively-not-multiplicatively with resonance; (b) ground refill `rootedRegenPerSecond` (default 4) routed through `CirculationIncomeHelper` (channel SCAR) if present.
- [ ] **Step 3 — Feedback.** Subtle root-tendril particles at the feet while Rooted (reuse a HutosLib tendril preset, low frequency); tooltip states the terrain dependency plainly.
- [ ] **Step 4 — Gate.** Math test for refund/regen flags; `runClient`: refund visibly applies on mycelium, dies on grass.

### Task 4: Putrivora resolvens — "digest affliction" (Mortem)

**Files:** create item + `AfflictionDigestHelper`; modify `ScarInit`, equipment handler.

- [ ] **Step 1 — Digestible set.** Effect tag/`Set<Holder<MobEffect>>`: Poison, Wither, Hunger, Blood Loss (mod effect), plus config list. **Not** digestible: Blood Drunkenness, Mnemonic Screams, Hematic/Morphic Strain (anti-abuse backlashes must not become food — spec §5.2 spirit; call this out in the class javadoc).
- [ ] **Step 2 — Digestion tick.** While equipped, each second per digestible active effect: reduce remaining duration by an *extra* `digestSpeedMultiplier − 1` seconds (default ×3 total decay) and grant `digestFeedPerEffectSecond` blood (default 2, income-helper routed) + a small heal (0.5 HP/s cap). Damage from the effect still applies while it lasts — digestion accelerates and compensates, never negates (the audit's armor-lane boundary).
- [ ] **Step 3 — Gate.** Pure math test (duration step-down, per-effect cap, non-digestible exclusion). `runClient`: stand in poison — effect drains fast, blood ticks up, you still take the early hits.

### Task 5: Cryostroma perdurans — "conserve" (Congeatio)

**Files:** create item + `ConserveStateHelper`; modify `ScarInit`, equipment handler.

- [ ] **Step 1 — Stillness detection.** Conserving = movement delta below threshold for ≥ `conserveEntryTicks` (default 60) **and** no manipulation cast within the window. Any movement/cast clears state and ramp.
- [ ] **Step 2 — Ramp.** Multiplier climbs from 1× to `conserveMaxMultiplier` (default 4×) over `conserveRampTicks` (default 600). Applies to: blood regen (`conserveRegenPerSecond` base 3, income-routed) and vein-section healing via `IVascularSystem` (accelerate the existing well-fed heal path — find it in the vascular tick and scale, don't fork it).
- [ ] **Step 3 — Explicitly not a death-save.** No lethal-damage hook of any kind; do **not** register with `LastRiteRules`. Frost-dormancy particle shell fades in as the ramp climbs (visual tell for "fully conserved").
- [ ] **Step 4 — Gate.** Ramp math test (entry/reset/climb); `runClient`: sit still, watch blood + a clotted vein section recover fast; step once, ramp resets.

### Task 6: Oculiflora reticularis — "see the network" (Tenebris)

**Files:** create item + `OculifloraRevealRenderer`; modify `ScarInit`, `ClientEvents`; no new packets.

- [ ] **Step 1 — Reveal sets (client-side, gated on own synced SCARS state).** Three passes, all within `oculifloraRadius` (default 48): **(a) entities** — anything in the `hemomancy_mob` tag family, blood-active players, and entities carrying Invisibility that are hemomancy-flagged (the Void Shroud/Umbral counter) → thin outline in void-purple (reuse the outline approach from Prismatic/Crimson Sight but as a *client render pass*, not the server Glowing effect — this must not leak to other players); **(b) bloom/network POIs** — Qliphoth blooms from `QliphothBloomClientData` (already synced) rendered as a through-terrain pulse column; **(c) nectar pools** — reuse the Mole-primal highlight path, permanent while equipped. Saint chambers/spore veins are **deferred** until those have client-known positions (note in [DEFERRED_IDEAS](../../DEFERRED_IDEAS.md)).
- [ ] **Step 2 — The Will anchor gate.** Expose `public static boolean networkSightActive(Player)` on the item/helper. The [Wills plan](2026-07-03-rogue-hemomancer-wills.md)'s `WillAnchorRenderer` calls this — if the Wills system lands first, its lookup no-ops until this step ships; if this lands first, the hook simply has no caller yet. Either order is safe; whichever PR lands second flips the telegraph live and must say so.
- [ ] **Step 3 — Performance guard.** Entity pass piggybacks on already-tracked entities (no extra server queries); POI pass caps at 16 rendered markers by distance; everything skipped when the scar isn't equipped. No config-off needed beyond unequipping, but add `render_layers.renderOculifloraReveal` client toggle to match the render-layer toggle convention.
- [ ] **Step 4 — "Sight → tap" is NOT in this task.** Pending decision D-05 in [DEFERRED_IDEAS.md](../../DEFERRED_IDEAS.md); the spec leans yes, but it ships as a follow-up so the reveal pass lands clean.
- [ ] **Step 5 — Gate.** `runClient`: equip → cloaked hemomancy mob outlined through a wall, bloom column visible from 40 blocks, second player (no scar) sees none of it; FPS unaffected in a spore-heavy area (spot-check F3).

### Task 7: Cultivation recipes + migration events

**Files:** 4 new recipe JSONs; create `FungalScarMigrationEvents`.

- [ ] **Step 1:** Recipes per the `fungal_scar_cultivation` schema — advanced-band tuning (2,400 blood / 2,400 ticks / 3,000 enzyme power) with tendencies ANIMUS / MORTEM / TENEBRIS / CONGEATIO respectively; Talaromyces + Noctifly JSONs untouched.
- [ ] **Step 2:** Migration sweep on login + container open + the SCARS fungal slot itself (equipped removed scar converts in place, stays equipped as its successor). Same event-wiring spot as the morphling plan's migration events — if both plans land, merge into one sweep handler and note it.
- [ ] **Step 3:** Gate: spawn each removed scar via command in a test world → converts per the map, including while equipped.

### Task 8: Tests, docs, closeout

- [ ] **Step 1:** `FungalScarRosterResourceTest`: exactly 8 registrations; 8 cultivation recipes; lang/model/texture per id; regression grep — zero references to the five removed ids anywhere under `src/`.
- [ ] **Step 2:** Helper math tests green; full `./gradlew build`.
- [ ] **Step 3:** Docs: `HEMOMANCY_REFERENCE.md` §13.2 roster table + §13.4 recipes; rewrite `docs/fungalscar.md` to the 8-scar roster; tick the audit's death-prevention row (Thanomyces gone); flip the spec's status line to `partial — implemented per plan`.
- [ ] **Step 4:** Smoke script (record in PR): equip each of the 8; Rhizovitta on/off terrain; Putrivora in a poison pit; Cryostroma ramp + reset; Oculiflora two-player reveal check; crucible-grow one new scar end-to-end; migration of an equipped Thanomyces.

## Sequencing notes

- Task 1 → 2 → (3/4/5 parallel) → 6 → 7 → 8. Tasks 3–5 are independent of each other.
- **Soft dependency:** guardrails' `CirculationIncomeHelper` (Tasks 3/4/5 income routing) — route through it if present, else direct + `TODO(guardrails)`.
- **Downstream unlock:** Task 6 Step 2 is the moment the Wills telegraph becomes fully live; coordinate the two PRs' descriptions.
