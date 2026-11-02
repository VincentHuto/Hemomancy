# Progress Screen Tree Layouts Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Re-lay out every node tree in the Harbinger and Unstained progress screens so each reads in progression order (centre = start, outward = later, direction = family) and fits the viewport without overlaps.

**Architecture:** Mostly data re-authoring produced by generator scripts (Skills `setTreePosition`, `ManipulationTreeInit` coordinates, `MaterialAtlasSpec.entryAt`), plus targeted algorithm fixes in the shared recipe-map layout, trace drawing, materials offset, a new pure `UnstainedProgressLayout`, and a shared `PanZoomState.focusOn` opening view. Every layout path has a Python port in the scratchpad that produces before/after JSON checked by one shared metric script.

**Tech Stack:** Java 21 / NeoForge 1.21.1 client GUI code, JUnit 5 (`./gradlew.bat test`), TypeScript + Vitest layout editor (`tools/skill_tree_editor`, `npx vitest run`), Python 3.10 + matplotlib for generation/verification scripts.

**Spec:** `docs/superpowers/specs/2026-10-11-progress-tree-layouts-design.md`

## Global Constraints

- Lineage policy: fix links, keep degrees. Never change a required degree, skill-point cost, blood cost, recipe, or gate.
- NeoForge imports only (`net.neoforged.*`); no `net.minecraftforge.*`.
- Match surrounding code style (tabs in Java files that use tabs; existing comment density).
- Do not commit. Leave all work in the working tree (user preference).
- **Do not run Gradle** inside package agents. The coordinator runs `./gradlew.bat` serially (shared build dirs collide). Package agents verify with the scratchpad Python scripts and `npx vitest run <file>` in `tools/skill_tree_editor`.
- Scratchpad: `C:\Users\Vince\AppData\Local\Temp\claude\C--Users-Vince-Desktop-My-MC-Mods-NeoForge-1-21-1-Hemomancy\dfde20e2-28e1-4812-ab6f-f85867b36876\scratchpad` (abbreviated `$SP`). Before-state dumps: `$SP/layouts/*.json`; analysis ports: `$SP/skills_layout.py`, `$SP/LayoutSim.java`, `$SP/recipe_map_layout.py`, `$SP/materials_layout.py`, `$SP/scripts/compute_layouts.py`.
- Acceptance metrics (spec): centre distance ≥ node size + 10 px; 0 children ringed inside a parent; 0 inward links; 0 links within 0.6 × node size of a third node; 0 crossings inside a sector; no label rect over a node rect. Checked by `python $SP/check_layout.py <after.json>`.
- Opening view: `PanZoomState.focusOn(hubX, hubY, radius, vw, vh)`, zoom clamped to `[0.5, 1.0]`.

## Review Focus

1. **Hidden/locked nodes must not shift visible ones.** Players see subsets (veiled tiers, Liber-gated rites, undiscovered sigils). Layout must be computed from the full definition set; test it where a pure layout class exists (RecipeMapLayout, UnstainedProgressLayout).
2. **Small GUI viewports (448×238 at 1080p auto scale 4).** `focusOn` must never produce zoom < 0.5 or put the hub off-screen; tested in `PanZoomStateFocusTest`.
3. **The editor must still round-trip.** Re-authored Java blocks must parse in the editor (`skillParser`, `manipulationParser`, `materialAtlasParser`, `recipeMapParser`); each package runs its parser test file.
4. **Pre-clarity / pre-baptism Unstained players.** The new path layout must render sensibly for OUTSIDER, NOVITIATE, CLEANSED_UNPLEDGED, PLEDGED and post-clarity states; tested in `UnstainedProgressLayoutTest`.
5. **Deep-layer skill traces.** After adding `deep_base` parents, no deep node may draw more than one hub trace; tested by a source/logic test on `SkillTraceLayerCache` behaviour.

---

### Task 0: Shared opening view (`PanZoomState.focusOn`) — coordinator

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/util/PanZoomState.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/client/screen/skilltree/util/PanZoomStateFocusTest.java`
- Create: `$SP/check_layout.py`

**Interfaces:**
- Produces: `public void focusOn(int hubX, int hubY, int radius, int viewportW, int viewportH)`; `public static float fitZoom(int radius, int viewportW, int viewportH)` returning `clamp(min(vw, vh) / (2f * (radius + FOCUS_PADDING)), 0.5f, 1.0f)` with `FOCUS_PADDING = 24`. After `focusOn`, `sx(0, hubX) == viewportW / 2` and `sy(0, hubY) == viewportH / 2` (±1).

- [ ] Write `PanZoomStateFocusTest` (hub centred, zoom clamps to 0.5 on a 448×238 viewport with radius 270, zoom 1.0 for a tiny radius).
- [ ] Implement `fitZoom` + `focusOn`.
- [ ] Write `check_layout.py` (input JSON: `{"hub":[x,y],"nodeSize":n,"nodes":[{"id","x","y","ring"?,"targetRadius"?,"sector"?,"label"?}],"edges":[{"from","to"}],"labels":[{"text","x","y","w","h"}]}`; prints each metric and exits non-zero on failure).

### Task 1: Skills

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/skills/{Core,LivingStaff,Summon,Mycelial,Scar,Covenant}SkillBranch.java` (only `setTreePosition`, parent args/`addParents`, degree label positions in `CoreSkillBranch.java:18-26`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/SkillTraceLayerCache.java:96-100` (deep trace rule)
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/SkillsTabController.java:84-85,172-173,274,622-626,664` (content size from real extents; `focusOn` hub (480,480)-relative)
- Modify tests: `SkillPointBranchSourceTest.java`, `LivingStaffSkillSourceTest.java`, `tools/skill_tree_editor/src/server/javaLayoutSource.test.ts`
- Create: `$SP/gen_skills.py` (generator), `$SP/after/skills.json`

**Interfaces:**
- Consumes: `PanZoomState.focusOn` (Task 0).
- Produces: nothing used by other tasks.

- [ ] Add editor invariant tests to `javaLayoutSource.test.ts`: every non-root skill within ±4 px of its degree ring radius (surface D0–4 → 72/120/170/220/270; deep D5–8 → 120/170/220/270, centre 480,480); every parent on the same layer is on a ring ≤ the child's; pairwise centre distance ≥ 36 px per layer; update the toggleable list to the current 20. Run → fail.
- [ ] Apply lineage fixes: `persistent_arsenal` parent → `skill_crimson_projection`; `automatic_coagulation` parent → `skill_hemostasis`; `crimson_wake` parent → `skill_last_wind`; add `SkillPointInit.deep_base_skill` to `addParents` of the 18 deep skills lacking it.
- [ ] Generator: sector per branch per spec angles; within a sector place nodes ring by ring, ordering by parent angle (children fan under parents), spread so centre distance ≥ 40 px; deep nodes with a surface parent biased toward that parent's angle. Emit `setTreePosition` values and degree label positions in the reserved gap (330°…−35°). Write the values into the branch files (only inside `<skill-editor>` markers).
- [ ] Trace rule: deep layer draws a hub trace only for nodes with no deep-layer parent, once.
- [ ] Controller: content size = 2 × (270 + 60); `focusOn(480 - originX, …)` per layer with radius = ring radius of `min(playerDegree + 1, layerMax)`.
- [ ] Update pinned coordinates in the two JVM source tests.
- [ ] Verify: `npx vitest run src/server/javaLayoutSource.test.ts src/server/skillParser.test.ts`; `python $SP/check_layout.py $SP/after/skills_surface.json` and `..._deep.json`; render before/after PNGs.

### Task 2: Tendencies

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationTreeInit.java` (coordinates, display parents, comments)
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/TendencyScarLayout.java`, `ScarTreeLayout.java` (tier step 65, uniform clearance)
- Modify: `ManipulationsTabController.java:83` (`focusOn` the ring centre; radius = 175 + rank step × reachable rank)
- Modify tests: `ScarTreeLayoutTest`, `TendencyScarLayoutTest`, `BloodBindingManipulationSourceTest:48`, `HematicCommandWiringSourceTest:27`, and editor tendencies tests if they pin coordinates
- Create: `$SP/gen_tendencies.py`, `$SP/after/tendencies.json`

- [ ] Generator: per tendency, place nodes in the sector frame (axis = −90° + 45° × ordinal): rank radius r(Humilis)=0 … r(Perfectus)=4 steps of 55 px from the inner edge; child ≥ max(parent) + 27 px; tangential spread ≥ 38 px; mirror-balance extremes so the bbox centre lies on the axis. Convert to authored coordinates and verify with a port of `buildManipLayout` (`$SP/LayoutSim.java` logic) that realized radii are within ±6 px of target for every node.
- [ ] Display-parent fix: `register("axonal_transduction", x, y, "living_circuit")`.
- [ ] Scar spacing 65 px; clearance uniform; update scar tests.
- [ ] Verify: vitest `manipulationWorkspace.test.ts tendenciesWorkspace.test.ts scarTreeParser.test.ts scarTreeWorkspace.test.ts`; `check_layout.py` on manipulations and scars; plots.

### Task 3: Rites and Crafting maps + Unstained lists

**Files:**
- Modify: `shared/RecipeMapLayout.java` (arc-length slots, staggered rows, weighted sectors, content size), `shared/HarbingerRecipeMapDefinitions.java` (families, registrations, links), `shared/RecipeMapTracePlan.java:47-48` (hub stubs), `shared/RecipeMapCanvas.java:118,170-211,582-585` (labels, tooltip, focusOn), `shared/RecipeMapTooltip.java:26`, `shared/RitesTabController.java:62,94-96` (floor/sigil ring by first use), `shared/RitesTabView.java:215-321`, `shared/RitesTabState.java:77`, `shared/CraftingTabView.java:134-218`, `shared/CraftingTabState.java:20-24,67-74`
- Modify editor: `tools/skill_tree_editor/src/frontend/recipeMapGraph.ts`, related tests
- Modify tests: `RecipeMapLayoutTest`, `RecipeMapTracePlanTest`, `HarbingerRecipeMapDefinitionsTest`
- Create: `RecipeMapLayoutInvariantTest` (no overlap, hidden entries don't shift visible ones)

- [ ] Tests first (JVM, pure `RecipeMapLayout`): with 6 entries in one cell at ring 1, centre distance ≥ 38; layout of a subset equals layout of the full set for shared ids.
- [ ] Implement layout changes; families/link fixes per spec; floor/sigil first-use ring; tooltip wording; label placement; hub stubs; `focusOn`.
- [ ] Unstained lists: stage-name headers, gateway-first ordering, hide empty crafting tiers, result names, clarity-aware lock.
- [ ] Editor parity + vitest `recipeMapParser.test.ts recipeMapWorkspace.test.ts`.
- [ ] Verify with the Python port updated to the new rules + `check_layout.py` on both maps (each layer).

### Task 4: Materials atlases

**Files:**
- Modify: `shared/MaterialAtlasSpec.java` (hub, buckets, all `entryAt`, new entries), `shared/MaterialsTabView.java:74-81,235-236,265-266` (offset applied to hub/plaques), `shared/MaterialsTabController.java:114` (`focusOn`)
- Modify: `AGENTS.md` (Qliphoth bullet wording), `docs/agents/hemomancy-mod-agent.md:12`
- Modify tests: `MaterialsAtlasMetadataSourceTest.java:50,52`, editor `materialAtlas*.test.ts`
- Create: `$SP/gen_materials.py`, `$SP/after/materials_*.json`

- [ ] Correct parent links that contradict recipe JSON (list from `$SP/materials_analysis.json`).
- [ ] Add spec entries for MaterialsData entries with no atlas position (gate from their predicate's `minDegree`).
- [ ] Generator: sectors ∝ member count; bands per gate degree with 36 px row pitch; order by parent angle; Qliphoth outermost in its sector keeping chain order; write `entryAt` values; bucket centres/plaques at sector mid-angle outside rim.
- [ ] Offset fix in view.
- [ ] Same for Unstained (bands Always, P10, P15, P25, P35, P50, C10).
- [ ] Verify: vitest material suites; `check_layout.py`; plots.

### Task 5: Unstained Progress, Summons, Bestiary

**Files:**
- Create: `unstained/UnstainedProgressLayout.java`, `src/test/.../unstained/UnstainedProgressLayoutTest.java`
- Modify: `unstained/UnstainedProgressScreen.java` (use layout for draw/tooltip/hit-test; path rendering; observance pips; sidebar shift + wrap; tooltip gate)
- Modify: `harbinger/SummonsTabView.java`, `SummonsTabController.java` (panel bottom above tabs, clipped detail, bounded scroll, Ringmaster hint), `harbinger/BestiaryTabView.java`, `BestiaryTabController.java` (same + alphabetical list)
- Modify tests that pin source strings: `SummonsSidebarHitTestSourceTest`, `SummonsDetailSourceTest`, `BestiaryEntityPreviewSourceTest` (only if touched)

- [ ] `UnstainedProgressLayoutTest`: path order (Baptism, 5 purity, 3 bridge rites, 5 clarity), strictly increasing x, clarity omitted when locked, observance pip anchors under their opening stage, fit bounds.
- [ ] Implement layout + screen integration; remove duplicated position formulas.
- [ ] Summons/Bestiary panel fixes.

### Task 6: Integration — coordinator

- [ ] `./gradlew.bat test` (serial); fix failures (dispatch fixes to the owning package).
- [ ] `npx vitest run` full editor suite.
- [ ] Docs: `docs/HEMOMANCY_REFERENCE.md` §11 tab list + changed skill parents; Unstained Progress description.
- [ ] Before/after contact sheet for the user; in-game screenshots if the client can be launched.
