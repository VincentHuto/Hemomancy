# Progress Screen Tree Layouts — Design

Date: 2026-10-11
Status: approved in chat (scope, lineage policy, Materials approach); pending spec review

## Goal

Reorganize every node tree in the Harbinger and Unstained progress screens so that each one
(a) reads in progression order and (b) fits the viewport cleanly: no overlapping nodes, no
labels on nodes, minimal edge crossings, balanced use of space, nothing hidden under chrome.

## Agreed decisions

- **Lineage policy: fix links, keep degrees.** Where a node is linked under a parent that unlocks
  later, or has no parent, re-parent it to a parent at the same or lower degree. No required
  degree, skill-point cost, blood cost or recipe changes.
- **Materials: radial re-layout** of both atlases, including placement of the entries that
  currently have no atlas position. The Qliphoth chain keeps its D7 gates and lineage.
- **Scope: all packages** listed below, in this session.

## The compass rule (applies to every tree)

1. **Centre = where the path starts.** Each tree has one hub at its centre.
2. **Distance outward = when it unlocks.** Nodes sit on the ring (or band) of their unlock
   gate. A child is never placed on a ring inside its parent's ring.
3. **Direction = family.** Each family/branch/tendency owns an angular sector. Sector width is
   proportional to how many nodes it must hold, not split evenly.
4. **Links point outward.** Within a sector, children are ordered under their parents so links
   fan outward without crossing where the tree shape allows.
5. **Labels never sit on nodes.** Family labels go outside the outer rim; degree labels go in a
   reserved empty gap.
6. **Opening view fits progress.** Open and Home centre on the hub and pick a zoom that fits
   everything up to the player's next gate, clamped to `[0.5, 1.0]` so icons stay visible.

Shared acceptance metrics per tree (checked by script and by tests):

- node-centre distance ≥ node size + 10 px (36 px for 26 px nodes, 38 px for 28 px nodes);
- 0 children ringed inside a parent; 0 inward links;
- 0 links passing within 0.6 × node size of a third node's centre;
- crossings: 0 within a sector; cross-sector crossings reported and minimized;
- no family/degree label rectangle intersects a node rectangle.

## Package 0 — Shared opening view

- Add `PanZoomState.focusOn(hubX, hubY, radius, viewportW, viewportH)`:
  `zoom = clamp(min(vw, vh) / (2 * (radius + 24)), 0.5, 1.0)`, pan so the hub is at the
  viewport centre. `centreOn` stays for non-radial content.
- Radial controllers (Skills, Tendencies, Rites, Crafting, Materials, Unstained Progress) call
  `focusOn` from init and `resetView`, passing the radius of the player's current gate ring + 1.
- Content size comes from real extents (outermost node or label + padding), not unused rings.
  Fixes the 35 px off-centre opening in Skills (`SkillsTabController.java:172-173`) and the
  1030² canvas in Rites/Crafting (`RecipeMapLayout.java:85-88`).

## Package 1 — Skills (74 nodes, data-only + one trace rule)

Positions are authored in `common/init/skills/*SkillBranch.java` (`setTreePosition`); the layout
editor rewrites the same blocks. Rings stay as today: surface D0–D4 at radii 72/120/170/220/270,
deep D5–D8 at 120/170/220/270 around (480, 480).

- **Ring snap:** every node is placed on its own degree ring (±4 px).
- **Sectors follow the editor's compass rule** (`javaLayoutSource.test.ts`): core north,
  covenant north-east (deep only), summons east, scars south, mycelial south-west, living staff
  west. Sector widths scale with node count; core's `dynamic_use` / `quick_caster` move north.
  Target surface allocation (screen degrees, 0 = east, clockwise): summons −35…45,
  scars 55…100, mycelial 108…148, staff 156…236, core 244…330, with the 330…−35 gap reserved
  for degree labels. Deep layer uses the same order with covenant inserted between core and
  summons. Exact angles are produced by the layout script and tuned to the metrics above.
- **Deep nodes face their surface prerequisite:** a deep node with a surface parent sits in that
  parent's angular direction so its hub trace points the right way.
- **Trace rule (code):** on the deep layer, a node that has a deep-layer parent draws only those
  parents; a deep root draws exactly one trace from the hub. Removes the stacked duplicate hub
  traces (`SkillTraceLayerCache.java:96-100`).
- **Degree labels** move into the reserved gap (`CoreSkillBranch.java:18-26`).
- **Lineage fixes** (no degree changes):
  - `skill_persistent_arsenal` (D3): parent `weapons_master` (D4) → `crimson_projection` (D3).
  - `skill_automatic_coagulation` (D3): parent `coagulation` (D4) → `hemostasis` (D2).
  - `skill_crimson_wake` (D4, orphan): parent → `last_wind` (D2, the low-blood skill).
  - The 18 deep skills lacking `deep_base_skill` gain it as an extra parent. `deep_base` starts
    unlocked, so this is gameplay-neutral, and the trace rule above keeps it visually neutral.
- **Tests:** fixes the three failing editor tests (directional separation, deep-root parent,
  toggleable list — the list gains the four newer toggleables). Update pinned coordinates in
  `SkillPointBranchSourceTest` and `LivingStaffSkillSourceTest`.

## Package 2 — Tendencies (77 manipulations + 25 scars)

Positions are authored in `ManipulationTreeInit.java`; `ManipulationsTabController.buildManipLayout`
translates (does not rotate) each tendency cluster so its bounding-box centre sits on the
sector axis (`-90° + 45° × ordinal`) at a shared anchor radius. The editor mirrors this.

- **Data-only re-author.** Each cluster is redrawn so it grows outward along its own sector
  axis: Humilis innermost, then Mediocritas, Summa, Magister, Perfectus. A child is always at
  least half a rank step outside every parent (same-rank children step outward).
- Clusters are shaped so their bounding-box centres land on their axes at a common radius, so
  the existing translate step places them correctly and the editor keeps working unchanged.
  Verified by porting `buildManipLayout` (realized radius within ±6 px of target).
- Even out sector gaps; resolve the `conjure_crossbow` / `axonal_transduction` overlap and the
  five links that pass through nodes.
- **Display links only:** manipulation tree parents are "Relates to" (never enforced), so
  `axonal_transduction` (Perfectus, no parent) is linked under `living_circuit` (Magister,
  Ductilis nerve line), and stale section
  comments in `ManipulationTreeInit` are rewritten. No gameplay effect.
- **Scars:** tier step 50 → 65 px and a uniform first-tier clearance so "+N" and two-line names
  fit (`TendencyScarLayout` / `ScarTreeLayout`). Update `ScarTreeLayoutTest`,
  `TendencyScarLayoutTest`, and the pinned `register(...)` strings in
  `BloodBindingManipulationSourceTest` and `HematicCommandWiringSourceTest`.

## Package 3 — Rites and Crafting maps (shared `RecipeMapLayout`)

- **Arc-length slots:** nodes in a cell are spaced by 38 px of arc (not a fixed 16°). A cell
  that cannot fit its arc splits into two staggered rows ±12 px around the ring.
- **Weighted sectors:** sector width ∝ the family's widest cell (in px of arc needed),
  normalized to 360° minus a reserved label gap.
- **Families, clockwise by earliest degree from north.** Rites: Miscellaneous is split into
  **Station Upgrades** (the 12 upgrade rites) and **Trials** (cinder_bellows, sanguine_hound,
  scarlet_mummer — which also gets its missing family registration); hematic_succession and
  mnemonic_restoration (both D5 Succession rites) join Bloodline/Fane. Crafting: Miscellaneous
  becomes **Stations** (Enzymatic Scriptorium, Resonant Forge, Scarlet Vanity, Somnolent Seat,
  Fungal Implantation Pylon); Vacant Effigy joins Constructs/Effigies and Hematic Stake joins
  Ritual Infrastructure.
- **Floors and sigils** are ringed by the earliest degree of any rite that uses them; unused
  ones follow their tier's earliest-used sibling. Tooltips say "First used at Degree N" instead
  of "Requires Degree N" (`RecipeMapTooltip.java:26`, `RecipeMapCanvas.java:118`).
- **Labels:** family labels at rim + 22 px; degree labels in the reserved gap.
- **Cross-layer links** are drawn as hub stubs on the deep layer instead of being dropped
  (`RecipeMapTracePlan.java:47-48`), so the Order and Vessel chains stay connected.
- **Link fixes (display only):** remove the duplicate pallid→crimson vessel link; flip
  exsanguination→rooted_vein to rooted_vein→exsanguination, and gorebound_hulk→
  marrow_spitter / veinwing_vulture to point into gorebound_hulk; initiate_rite→
  sanguine_brotherhood becomes conceptual (the D2 ceremony does not grant D3); add
  sanguine_monolith→covenant_throne.
- **Editor parity:** `tools/skill_tree_editor/src/frontend/recipeMapGraph.ts` mirrors the slot,
  sector and family rules. Update `RecipeMapLayoutTest`, `RecipeMapTracePlanTest`,
  `HarbingerRecipeMapDefinitionsTest`.

## Package 4 — Materials atlases (radial re-layout)

- **Bug fix:** apply the bounds offset to the hub and plaques as well as nodes
  (`MaterialsTabView.java:74-81, 235-236, 265-266`); traces bend around the drawn hub.
- **Harbinger atlas:** hub at the centre; the 15 buckets become sectors (width ∝ member
  count); each gate degree is a band of one or more rows (36 px row pitch); within a band,
  nodes are ordered by their parent's angle so veins stay short. Bucket centres and plaques
  move to their sector's mid-angle, plaques outside the rim. Positions are written as
  `entryAt(x, y)` by a generator script, so the editor model is unchanged.
- **Missing entries:** the `MaterialsData` entries with no spec entry (scars, living weapons,
  non-chest armor pieces, panes, `sanguine_omen`) get spec entries in their bucket with gates
  taken from their `MaterialsData` predicates.
- **Parent links that contradict recipes** are corrected to match the recipe JSON (e.g.
  `polished_venous_stone` ← `polished_venous_stone_bricks` is reversed; the world fungi no
  longer descend from a D3 mycelium). Atlas gates are unchanged.
- **Qliphoth:** stays the outermost band of its sector with Monolith → Seed → Bloom → Pome →
  Spine and Bloom → Memory; D7 gates and D6 veiled previews unchanged. Update the Seed/Bloom
  coordinate pins in `MaterialsAtlasMetadataSourceTest` and the "preserve authored coordinates"
  wording in `AGENTS.md` and `docs/agents/hemomancy-mod-agent.md` to "keep atlas coordinates
  on their radial gate bands".
- **Unstained atlas:** same radial rule; bands are Always, Purity 10/15/25/35/50, Clarity 10.
- Editor: `materialsGraph.ts` keeps working (entries stay `entryAt`); its gap-policy tests are
  updated for new ids.

## Package 5 — Unstained Progress, Unstained lists, Summons, Bestiary

**Unstained Progress** becomes a single left-to-right path that fits the landscape viewport:

```
Lethean Baptism ◇ → Corrupted → Tainted → Cleansing → Absolved → Purified
   → Closed Vein ◇ → Consecrated Copper ◇ → Clarity Ascension ◇
   → Awakened → Discerning → Vigilant → Resolute → Enlightened
```

- Stage nodes keep their 30 px diamonds; rites are smaller diamonds between them. Clarity
  stages stay hidden until clarity is unlocked (as today); the three bridge rites show as the
  veiled next step once Purified is reached.
- Observances appear as small pips under the stage at which they become available, in the order
  they are earned; the sidebar list uses the same order.
- All positions come from a new pure `UnstainedProgressLayout` used by drawing, tooltips and
  hit-testing (replacing four duplicated formulas), with JVM tests.
- Opening the milestones sidebar shifts the fit area right instead of covering the path;
  sidebar text wraps. Stage tooltips use the same visibility condition as the nodes, so
  novitiate-route players can hover their stages.

**Unstained Rites / Crafting lists:** tier headers use Unstained stage names
(`RecipeDegreeGates.unstainedStageLabel`); the gateway rite sorts first, then by name; empty
crafting tiers are hidden; crafting rows show the result item's name; the rites lock check
includes the clarity requirement.

**Summons / Bestiary:** panels end above the bottom-right tabs; detail text is clipped with a
bounded scroll and a "more" hint; Ringmaster Pattern's unlock hint names the Circus Succession
finale; the Bestiary list is ordered alphabetically by display name (entries carry no habitat
data to group by).

## Docs

Update `docs/HEMOMANCY_REFERENCE.md` §11 (tab list; changed skill parents) and the Unstained
Progress description, plus `AGENTS.md` / `docs/agents/hemomancy-mod-agent.md` for the Materials
wording. Large pre-existing doc drift found during analysis (skill table missing 40 nodes,
Unstained doc mismatches) is reported, not rewritten, in this pass.

## Verification

- Layout scripts (ports of each layout path) produce before/after plots and the metric report;
  every tree must meet the acceptance metrics above.
- New invariant tests: editor vitest suites for Skills, Tendencies, Materials and recipe maps
  (ring snap, outward links, minimum spacing); JVM tests for `RecipeMapLayout`,
  `TendencyScarLayout`, `UnstainedProgressLayout`, `PanZoomState.focusOn`.
- `./gradlew.bat test` and `npx vitest run` in `tools/skill_tree_editor` pass (the pre-existing
  rite-icon count failure is fixed or reported separately).
- In-game screenshots of each tab at GUI scale 3 if the client can be launched; otherwise this
  is listed as not verified.

## Revision after in-game review (2026-10-12)

The first build met every geometric metric but read worse in-game. Short links squiggled (trace
handles and sway had fixed minimums), Skills and Tendencies felt cluttered, and the radial
Materials atlas scattered veins across the map. The user chose:

- **Traces:** handle length and sway now scale with link length (no fixed minimums), in the
  Skill, Manipulation and Material trace caches. Links over ~106 px are unchanged.
- **Skills:** original hand-authored positions restored. Kept: the three lineage fixes, the 18
  `deep_base` parents, one hub trace per deep root, and the focus-on-open view. Nudged:
  `guarded_feeding` (232,602)→(196,604) and the Degree 5/6 labels off nodes. The editor's
  pre-existing directional test still fails (core `dynamic_use`/`quick_caster` sit south).
- **Tendencies:** original authored layout restored. Fixes: Mortem's cluster mirrored so
  Funeral Bell sits at the rim; `axonal_transduction` moved beside its new display parent
  `living_circuit`; six nudges for too-close pairs and links under nodes (`hematic_riposte`,
  `ironhearted`, `sanguine_ward`, `cruor_javelin`, `searing_lash`, `radiant_wake`). Scar tier
  step 65 and scar labels from zoom 0.95 kept.
- **Materials:** original atlas restored with its parent links and buckets; plaque Y values
  compensated for the offset fix; overlap nudges; the 59 missing entries placed in neat rows
  beside their families.
- **Rites, Crafting, Unstained Progress, Summons, Bestiary:** kept as built (deep recipe-map
  rings 170/220/270/320; vow-route BYPASSED states; layer switches centre at zoom 1.0).

## Out of scope

Gate/degree changes, new mechanics, the Bestiary/Summons visibility degree, and rewriting the
drifted reference tables beyond the changed entries.
