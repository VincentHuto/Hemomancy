# Somatic Loom Engram Weaving Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the Somatic Loom's thread fight with Engram Weaving: an {8/3} star sigil projected on the ground around the loom, threaded with the Living Staff as a no-crossing routing puzzle whose spans are paid from a revolving enzyme stock.

**Architecture:** Pure, JVM-tested rules in `common/tile/harbinger/crafting/engram/` (geometry, board, generator, rules, aim). The loom block entity owns an `EngramWeaveSession`; the Living Staff drives it server-side from the weaver's look every use tick (no new packets). The client draws the sigil as a terrain-draped membrane with its own core shader, standing engram glyphs, and the existing echo reworked to remember beat by beat.

**Tech Stack:** NeoForge 21.1.251 / MC 1.21.1, Java 21, JUnit 5, NeoForge GameTests, GLSL 150 core shaders, HutosLib particles/tendrils.

**Spec:** `docs/superpowers/specs/2026-10-15-somatic-loom-engram-weaving-design.md`

## Global Constraints

- Wheel order = `EnumBloodTendency` declaration order (Animus, Flammeus, Ductilis, Lux, Mortem, Congeatio, Ferric, Tenebris); opposites four apart.
- Tip radius by stage: 5.0 / 5.75 / 6.5 blocks. Trace reach 5.5 / 8 / 8. Listening reach: within trace reach on base, 16 on Ganglionic+.
- 1 enzyme item = 3 spans; vial cap 192 spans; spans consumed only at completion; reference cost reserved at start.
- No weave can kill at any rank (4-health blackout floor).
- Stakes: Humilis none; Mediocritas burn surplus spans (shortfall 5 mL blood/span); Summa/Magister fungal pressure (3 → scar or intrusion off the reference solution); Perfectus one-attack bite.
- No vanilla `ParticleTypes` in loom/engram code. NeoForge APIs only. Match each file's existing formatting (tabs in loom/BE classes).
- Do not commit (user preference); leave everything in the working tree.
- Shader: avoid GLSL reserved words/built-in shadowing (`packed`, `distance`, `offset`, `noise2`); `ShaderReservedWordTest` must stay green.

## Review Focus

1. A recipe whose fragments outnumber tips (8–12 units) — expect fragments on notches and a solvable board (generator test over every recipe profile).
2. Stock topped up or drained mid-weave (hopper/creative) — expect lanes never to commit beyond stock and the reference to stay affordable (rules test `extendRefusedPastStock`, GameTest `reserveSurvivesBurn`).
3. A weaver who logs out mid-trace with the staff held — expect the board frozen with lanes, active-lane cleared, resume on return (GameTest `leavingPausesAndKeepsLanes`).
4. A save from the witnessed weave with loose threads in the world — expect a fresh engram, spun enzyme refunded as spans, orphan threads unravel (GameTest `witnessedWeaveSavesMigrate`).
5. Fast staff sweeps that skip nodes between ticks — expect collinear fill along one line, never a jump across lines (rules test `sweepFillsOnlyAlongOneLine`).

---

### Task 1: Engram geometry

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/engram/EngramSigilGeometry.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/engram/EngramSigilGeometryTest.java`

**Interfaces — Produces:**
- `NODES = 25, TIPS = 8, VESSEL = 24`; `isTip(n)`, `isNotch(n)` (8–15), `isInner(n)` (16–23).
- `double[] unit(int node)` → `{x, z}` in tip-radius units, tip 0 at (0, -1) (front), clockwise seen from above (+x right of front... angle a_k = -π/2 + k·π/4 in the x/z plane with z forward-negative).
- `int[] neighbours(int node)`; `boolean adjacent(a, b)`; `int edgeIndex(a, b)` (0–55, -1 if none); `int[][] edges()`.
- `int face(int innerNode)` → 0–7; `int spokeInner(int face)`.
- `List<int[]> lines()` — each star line's 6 nodes in order, each ring arc pair, each spoke pair; `int[] collinearRun(int from, int to)` → nodes strictly between `from` and `to` along one shared straight star line, or null.

- [ ] Step 1: Write tests: 25 nodes; 56 edges; tip degree 4, notch degree 4, inner degree 5, vessel degree 8; notch radius ≈0.5412 and inner ≈0.4142; inner nodes on tip angles; every star line has exactly 6 nodes; `collinearRun(tip0, inner)` returns the notch between; no two edges cross except at shared nodes (segment intersection check).
- [ ] Step 2: Run `./gradlew.bat test --tests "*EngramSigilGeometryTest"` → FAIL (class missing).
- [ ] Step 3: Implement: compute tips; intersect the 8 lines pairwise; classify crossings by radius (>0.47 notch, else inner) and order each ring by angle starting at the front; build line node lists sorted by parameter; edges from consecutive line nodes, ring arcs, spokes.
- [ ] Step 4: Re-run → PASS.

### Task 2: Board model and rules

**Files:**
- Create: `.../engram/EngramBoard.java`, `.../engram/EngramWeaveRules.java`
- Test: `.../engram/EngramWeaveRulesTest.java`

**Interfaces — Produces:**
- `EngramBoard`: `NodeKind {OPEN, SCAR, MEMBRANE, PRISM}`; per node `kind`, `tint` (tendency ordinal, -1); `List<Fragment>`; `Fragment(int node, int colour /*-1 grey*/, int trueColour, boolean intrusion, int decoy /*-1*/, boolean premarked)`; `List<Lane>` one per fragment (`IntList path` starting at the fragment node; `boolean landed`); `int rotation` (face index of Animus); `boolean keyed`; `int pressure`; `int[] reference` (spans per tendency); `int[] reserved`; `CompoundTag save()` / `static EngramBoard load(CompoundTag)`; `int laneColour(int fragment)` (grey resolved through prisms passed); `int occupant(int node)` → fragment index or -1 (first lane through it).
- `EngramWeaveRules`:
  - `SPANS_PER_ENZYME = 3`, `SPAN_CAPACITY = 192`, `PRESSURE_LIMIT = 3`, `BURN_BLOOD_PER_SPAN = 5`.
  - `double tipRadius(int stage)`, `double traceReach(int stage)`, `double listenReach(int stage)`, `double snapRadius(int stage)`.
  - `enum Step {OK, NOT_ADJACENT, OCCUPIED, SCARRED, MEMBRANE, WRONG_PRISM, WRONG_FACE, FACE_TAKEN, NO_STOCK, LANDED}`.
  - `Step canExtend(EngramBoard b, int fragment, int to, int[] stock)`; `boolean extend(...)` mutates.
  - `int committed(EngramBoard b, int tendency)`; `int laneSpans(EngramBoard b, int fragment)` (own segment only).
  - `boolean connected(EngramBoard b, int fragment)` (reaches Vessel directly or by plying chain).
  - `boolean complete(EngramBoard b, int[] recipeCounts)`.
  - `void cut(EngramBoard b, int fragment, int node)`; `void clear(EngramBoard b, int fragment)` (also clears lanes plied onto it from that node onwards → they lose connection but keep their own path).
  - `Stake stakeFor(EnumManipulationRank rank)` → `NONE, BURN, PRESSURE, BITE`.
  - `boolean faceAllowed(EngramBoard b, int face, int colour)`; `int faceTendency(EngramBoard b, int face)`.

- [ ] Step 1: Tests: extend along an edge OK; non-adjacent refused; another colour's node OCCUPIED; same colour → plies and the lane ends; scar refused; membrane refuses other colours; keyed face refuses wrong colour; any face below keyed; face taken by another colour refused; stock: extending past `stock - committed` → NO_STOCK (`extendRefusedPastStock`); completion counts only true fragments and needs exact per-colour count; grey lane takes prism colour and refuses a wrong prism; cut/clear refund (committed drops); `laneSpans` of a plied lane counts only its own segment; sweepFillsOnlyAlongOneLine via `EngramSigilGeometry.collinearRun`; save/load round-trip equals.
- [ ] Step 2: Run → FAIL. Step 3: Implement. Step 4: Run → PASS.

### Task 3: Seeded generator

**Files:**
- Create: `.../engram/EngramBoardGenerator.java`
- Test: `.../engram/EngramBoardGeneratorTest.java` (reads `src/main/resources/data/hemomancy/recipe/memory_weaving/*.json` for enzyme profiles)

**Interfaces — Produces:** `static EngramBoard generate(long seed, int[] counts /*per tendency*/, EnumManipulationRank rank, int decoyChoices /*>=0*/)`; result has `reference` filled, scars/membranes/prisms/intrusions per rank table, a stored solution `int[][] solution()` (per fragment path) used by tests and pressure; `static long seed(long worldSeed, String recipeId, UUID weaver)`.

- [ ] Step 1: Tests: for every recipe profile × every rank × seeds 0..15: board generates; applying the stored solution with `EngramWeaveRules.extend` from empty lanes completes the board with stock = reference; reference per tendency ≤ 192; deterministic for same seed; intrusion counts per rank; Humilis has no intrusions/membranes/keys; Summa+ keyed; Magister+ has a grey fragment when any colour has ≥2 units; >8 fragments use notches.
- [ ] Step 2–4: FAIL → implement (placement, BFS tree routing with plying into own tree, retries, relaxation) → PASS.

### Task 4: Pressure and aim maths

**Files:**
- Modify: `.../engram/EngramWeaveRules.java` (add `applyPressure(EngramBoard, Random)`)
- Create: `.../engram/EngramAim.java`
- Test: `.../engram/EngramAimTest.java`, extend `EngramWeaveRulesTest`

**Interfaces — Produces:** `EngramAim.nearestNode(double sx, double sz, double tipRadius, double snap)` → node or -1 (Vessel when inside inner radius·0.6); `EngramAim.planeHit(eye, look, centre, planeY, reach)` → `double[]{x,z}` sigil-space blocks or null; `EngramAim.toSigil(worldX, worldZ, centreX, centreZ, forwardX, forwardZ)` and back (`toWorld`).
- [ ] Tests: snapping at each node returns it; midway between returns -1 when beyond snap; plane hit behind the eye is null; pressure never touches a reference-solution node or an occupied node; rotation/forward transform round-trips for four facings.

### Task 5: Session in the loom block entity

**Files:**
- Create: `.../engram/EngramWeaveSession.java`
- Modify: `common/tile/harbinger/crafting/SomaticLoomBlockEntity.java`, `SomaticWeaveRules.java` (drop fray/strain/hook members except floor/rank/radius/climax/blackout; `lethal` → always false)
- Create: `common/tile/harbinger/crafting/engram/EngramRecollections.java` (SavedData)

**Interfaces — Produces (BE):** `getEngram()` → `EngramBoard` or null; `getSpans(tendency)`; `engramStep(Player, int node, boolean sneaking, boolean press)` → `EngramWeaveRules.Step`; `engramActive()` fragment index; `sigilCentre()`, `sigilForward()`, `sigilRadius()`; `int committedSpans(t)`; `ENZYME_CAPACITY` stays the item count (64), `SPAN_CAPACITY` in rules.
- [ ] Payment: generate board with weaver seed; refuse if any colour's stock < reference (message names the colour and shortfall); reserve.
- [ ] Completion: when `complete` → consume `laneSpans` per colour → climax → existing `tickCompletion`; record recollection.
- [ ] Stakes on connecting an intrusion (snap lane; burn/pressure/bite).
- [ ] Pause/resume/adopt unchanged; blackout pauses.
- [ ] NBT: `weaveSaveVersion` 2, `engram` compound, spans; migration from v1 (`storedEnzymes` ×3; `weaveThreads`/`ritualOrbs` → fresh board, spun ×3 back to stock; legacy loose thread entities unravel since the loom no longer owns them).

### Task 6: Living Staff tracing

**Files:**
- Create: `common/tile/harbinger/crafting/engram/EngramTracing.java`
- Modify: `common/item/harbinger/tool/living/LivingStaffItem.java` (`use`, `onUseTick`, `releaseUsing`), `common/entity/weave/FrayedThreadEntity.java` (intrusion mode), `ThreadAttacks.java` (power from rank), `WeaveDamageEvents.java` (floor at every rank)

**Interfaces — Produces:** `EngramTracing.tracedLoom(Player)` → `SomaticLoomBlockEntity` or null (weaver of a weaving loom within reach whose aim lands on the sigil); `begin(Player)`, `tick(Player)`, `end(Player)`; `aimNode(Player, loom, reach)` → node or -1. A static registry `ActiveEngramLooms.track(level, pos)`, `.near(level, pos, radius)` maintained from the BE tick on both sides.
- [ ] `FrayedThreadEntity.spawnIntrusion(level, loom, tendency, at)`: winds up 20 ticks, attacks once via `ThreadAttacks.perform`, unravels; never hookable.

### Task 7: GameTests

**Files:**
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/SomaticLoomWeavingGameTests.java` (rewrite thread tests), `LoomWeaveTestSupport.java` (`weaveAll` traces the stored solution through `engramStep`)
- Run: `./gradlew.bat runSomaticLoomGameTestServer`
- [ ] Tests: paying unfurls an engram with reference reserved; solving completes and consumes exactly lane spans; cutting refunds; staff use with a set look extends a lane; leaving pauses and keeps lanes; Mediocritas burn; Summa pressure; Perfectus bite floored + blackout; bloodline pre-marking; stock-below-reference refused; witnessed-weave/orb save migration; existing stage/rite/practice/theme tests kept.

### Task 8: Sigil shader and render types

**Files:**
- Create: `src/main/resources/assets/hemomancy/shaders/core/world/engram_sigil.{json,vsh,fsh}`
- Modify: `common/init/ShaderInit.java`, `client/render/HemoRenderTypes.java` (`engramSigil(layer, ...)`)
- [ ] Layers by uniform `Layer`: 0 membrane (skin, carved star/ring/spokes, nodes, keyed faces, pressure rot, spread hem, drapes), 1 lanes (ichor vessels), 2 node caps (fragment sockets glow, scars, valves, prisms).
- [ ] `glslangValidator -S frag/vert` clean; `ShaderReservedWordTest` green.

### Task 9: Sigil renderer, fragments, aim client

**Files:**
- Create: `client/render/tile/harbinger/crafting/engram/EngramSigilMesh.java`, `EngramSigilRenderer.java`, `EngramFragmentRenderer.java`, `client/render/tile/harbinger/crafting/engram/EngramAimClient.java`
- Modify: `SomaticLoomRenderer.java` (bounds, call sigil/fragments, retire feeler thread logic → listen droop), `SomaticLoomEcho.java` (beat-driven, limbs by landed share, listen replay, decoy replay), `SomaticLoomEffectRules.java` (vial fill in spans)
- [ ] Terrain-draped mesh cached per loom (rebuild on footprint change), unfurl spread, lane ribbons on ground heights, glyph cards (engram characters), tendrils, HitGlow cursor, preview, listen trigger → echo.

### Task 10: JEI, feedback text, teaching, docs

**Files:** `compat/jei/MemoryWeavingRecipeCategory.java`, loom messages in the BE, Mnemonist dialogue lines, Liber pages (`manipulations/pages/`), `assets/hemomancy/lang/en_us.json`, `docs/HEMOMANCY_REFERENCE.md` (§25.7, loom rows), `docs/STATION_UPGRADES.md` (Somatic Loom stages), `docs/LORE_REFERENCE.md` (memory weaving paragraph), `wiki/Harbinger-Path.md`, source tests (`MemoryWeavingRedesignSourceTest`).

### Task 11: Verification

- [ ] `./gradlew.bat test` (JVM) green.
- [ ] `./gradlew.bat runSomaticLoomGameTestServer` green; `runStationUpgradeGameTestServer` green.
- [ ] Shader compile on the real GPU via a review client launch; loom review screenshots inspected and iterated.
- [ ] One fresh reviewer over the whole change.
