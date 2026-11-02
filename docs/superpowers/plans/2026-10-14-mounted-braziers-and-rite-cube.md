# Mounted Iron Braziers and the Rite Cube Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Iron Braziers stand, mount on walls or hang from ceilings, and a Cardinal Rite counts any lit brazier with an offering anywhere in the N×N×N space above its floor.

**Architecture:** A pure `CardinalRiteBrazierSpace` defines the cube cells and their order; the station matcher scans it. A pure `BrazierMount` holds per-mount geometry (bowl drop, shapes, daemon stops, placement rule) shared by the block, renderers and rite code. The model generator authors wall and hanging variants keyed on vanilla `attach_face`.

**Tech Stack:** NeoForge 1.21.1, Java 21, JUnit 5, NeoForge GameTests, Python 3 + Pillow model generator.

**Spec:** `docs/superpowers/specs/2026-10-14-mounted-braziers-and-rite-cube-design.md`

## Global Constraints

- Do not commit; leave all work in the working tree (user preference).
- Custom Hemomancy particles only; no new vanilla particles.
- No copper/brass/bronze on the brazier; hematic iron, venous stone, vein accents.
- `ATTACH_FACE` default `floor`; every existing `defaultBlockState()` stays a standing brazier.
- Socket frame: x right, y up, z along the matcher's `forwards`; world = `offset(focus, forwards, up, cell)`.
- Baselines: JVM 16 known failures (`newfails.sh` must list none new); GameTests known failure `blockitemexceptionskeeptheirowners`, flakes `idlemorphlingsinkstofloor`, `astaffhooksaslackthreadatitsloomsreach`, `circussuccessionrunnercompletesatdegreefour`, `bodyanswersjourneyusesrealrecipeandrestoresstate`.

## Review Focus

- A hanging brazier at the cube's top layer (y = 2·half) under a ceiling one above it counts; one at y = 2·half+1 does not.
- A wall brazier whose wall block is mined mid-rite: it breaks, spills its offering, and the rite collapses through the existing re-validation (no dupes: escrowed offerings stay escrowed).
- Old worlds: braziers saved without `face` load as standing.
- A brazier in the Focus column (e.g. hanging over the staff) never counts.
- Floor mounds appear only for standing braziers; a wall brazier on a gash cell does not bend the trench.

---

### Task 1: The rite cube

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/floor/CardinalRiteBrazierSpace.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/floor/CardinalRiteFloorDefinition.java` (add `half()`, `brazierSpace()`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteStationMatcher.java:178` (scan `brazierSpace()`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/succession/SuccessionRites.java:51-53` (comment)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/rite/floor/CardinalRiteBrazierSpaceTest.java`
- Test: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/StructureSpawnerGameTests.java` (cube GameTests)

**Interfaces:**
- Produces: `CardinalRiteBrazierSpace.cells(int half) : List<BlockPos>`; `CardinalRiteFloorDefinition.half() : int`; `CardinalRiteFloorDefinition.brazierSpace() : List<BlockPos>`.

- [ ] **Step 1: Write the failing JVM test**

```java
final class CardinalRiteBrazierSpaceTest {
	@Test
	void theSpaceIsTheCubeAboveTheFloorLessTheFocusColumnAndAnchorSores() {
		for (int half = 1; half <= 4; half++) {
			int size = 2 * half + 1;
			List<BlockPos> cells = CardinalRiteBrazierSpace.cells(half);
			Set<BlockPos> anchors = anchorCells();
			int anchorsOnFloor = 0;
			for (int x = -half; x <= half; x++) for (int z = -half; z <= half; z++)
				if ((x != 0 || z != 0) && anchors.contains(new BlockPos(x, 1, z))) anchorsOnFloor++;
			assertEquals((size * size - 1) * (size - 1) - anchorsOnFloor, cells.size(), "half " + half);
			assertEquals(cells.size(), new HashSet<>(cells).size(), "no cell twice");
			for (BlockPos cell : cells) {
				assertTrue(Math.abs(cell.getX()) <= half && Math.abs(cell.getZ()) <= half, "inside the floor: " + cell);
				assertTrue(cell.getY() >= 1 && cell.getY() <= 2 * half, "inside the cube: " + cell);
				assertFalse(cell.getX() == 0 && cell.getZ() == 0, "the Focus column stays clear: " + cell);
				if (cell.getY() == 1) assertFalse(anchors.contains(cell), "anchor sore: " + cell);
			}
		}
	}

	@Test
	void cellsRunClockwiseFromTheFocusesLeftThenNearestThenLowest() {
		List<BlockPos> cells = CardinalRiteBrazierSpace.cells(2);
		assertEquals(new BlockPos(-1, 1, 0), cells.getFirst(), "nearest, lowest cell at the Focus's left");
		for (int i = 1; i < cells.size(); i++) {
			BlockPos a = cells.get(i - 1), b = cells.get(i);
			double aa = CardinalRiteGashRules.around(a.getX(), a.getZ()), ab = CardinalRiteGashRules.around(b.getX(), b.getZ());
			assertTrue(aa < ab || aa == ab && (Math.hypot(a.getX(), a.getZ()) < Math.hypot(b.getX(), b.getZ())
					|| Math.hypot(a.getX(), a.getZ()) == Math.hypot(b.getX(), b.getZ()) && a.getY() < b.getY()), a + " then " + b);
		}
	}
}
```

- [ ] **Step 2: Run** `./gradlew.bat test --tests "*CardinalRiteBrazierSpaceTest*"` — Expected: compile FAIL, `CardinalRiteBrazierSpace` missing.

- [ ] **Step 3: Implement** `CardinalRiteBrazierSpace.cells(half)` (cached per half), sharing the anchor cell set with `CardinalRiteGashRules` (make its `anchorCells()` package-visible). Add `half()` = `Math.max(focus.getX(), focus.getZ())` and `brazierSpace()` to the floor definition. Switch the matcher loop to `floor.brazierSpace()`.

- [ ] **Step 4: Run** the JVM test — Expected: PASS.

- [ ] **Step 5: GameTests** in `StructureSpawnerGameTests` (written before Task 2 lands only use standing braziers; the wall/hanging cube test is added in Task 2):
  `braziersCountAnywhereInTheCubeAboveTheFloor` — spawn `working_lesser` with the Somatic Loom subject, set the two Loom offerings on lit standing braziers at cube cells off the gash (e.g. (−1,1,1) and (1,3,−1) on a stone pillar), expect `MATCHED`; `aBrazierAboveTheCubeIsIgnored` — same with one offering at y = 2·half+1, expect not `MATCHED`.
  Run `./gradlew.bat runGameTestServer` — Expected: new tests pass; failures only the known baseline.

### Task 2: Mounts on the block

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/rite/BrazierMount.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/rite/BrazierBlock.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/block/harbinger/rite/BrazierMountTest.java`
- Test: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/BrazierMountGameTests.java`

**Interfaces:**
- Produces: `BrazierMount.drop(AttachFace) : double`; `BrazierMount.bowl(BlockState, BlockPos, double height) : Vec3`; `BrazierMount.daemonStop(BlockState, BlockPos, Vec3 focus) : Vec3`; `BrazierMount.shape(AttachFace, Direction) : VoxelShape`; `BrazierMount.placement(Direction clicked) : AttachFace` (null-safe); `BrazierBlock.FACE = BlockStateProperties.ATTACH_FACE`.

- [ ] **Step 1: Failing JVM test** `BrazierMountTest`: drops are 0, −0.25, −0.375; `placement(UP)=FLOOR`, `placement(DOWN)=CEILING`, horizontal → `WALL`; every shape lies within [0,1]³ and wall shapes touch the wall side (max z = 1 for facing north) and ceiling shapes touch y = 1; `daemonStop` for a standing brazier is centre + 1.2 up, for a north-facing wall brazier lies north of the block, for a hanging brazier lies below the bowl and closer to the Focus.
- [ ] **Step 2: Run** — Expected: compile FAIL.
- [ ] **Step 3: Implement** `BrazierMount`; add `FACE` to `BrazierBlock` (default FLOOR), `getStateForPlacement` over `context.getNearestLookingDirections()` choosing the first mount that `canSurvive`, `canSurvive` per mount, `updateShape` returning air when support is lost, `getShape` from `BrazierMount.shape`, particles and the extinguish puff via `BrazierMount.bowl`.
- [ ] **Step 4: Run** — Expected: PASS.
- [ ] **Step 5: GameTests** `BrazierMountGameTests`: `placingOnAWallFaceMountsTheBrazier` (use `BlockItem.place` with a `BlockPlaceContext` on a wall's side face → `FACE=WALL`, `FACING` = clicked face); `aWallBrazierFallsWithItsWallAndSpillsItsOffering`; `aHangingBrazierFallsWithItsCeiling`; and in `StructureSpawnerGameTests`, `wallAndHangingBraziersCountInTheCube` (Loom offerings on a wall brazier on a pillar side and a hanging brazier under a block). Run GameTests — Expected: pass.

### Task 3: Models

**Files:**
- Modify: `tools/model_export/iron_brazier.py`
- Generated: `assets/hemomancy/models/block/iron_brazier_{wall,hanging}{,_lit,_sanguine}.json`, `blockstates/iron_brazier.json`, bbmodel
- Test: `src/test/java/com/vincenthuto/hemomancy/common/block/IronBrazierMountModelResourceTest.java`

- [ ] **Step 1: Failing resource test**: the blockstate has a part for every `face` × `facing` and the base model; every referenced model file exists; wall and hanging bodies have no `dais`/`stem` elements and have `wall_plate`/`ceiling_plate` elements.
- [ ] **Step 2: Run** — Expected: FAIL (no wall/hanging models).
- [ ] **Step 3: Implement**: `shifted(model, dy)` helper; `wall_body()` (plate, bracket arm, brace + shifted bowl/talons/spout), `hanging_body()` (ceiling plate, hook ring, four chains + shifted bowl…), shifted `lit`/`sanguine` per mount (sanguine drops the stem vein and dais band for wall/hanging); blockstate keyed on `face`, `facing`, `ritual_phase`. Run `python tools/model_export/iron_brazier.py --preview` and look at the preview.
- [ ] **Step 4: Run** the resource test plus `ClientModelTextureCoverageTest` — Expected: PASS (the latter keeps its baseline status).

### Task 4: Bowl positions everywhere

**Files:**
- Modify: `IronBrazierRenderer`, `BrazierFlames` (add `double lift`), `CardinalRiteOrdealEngine` (brazierTop, daemon stops), `PuppeteerTrialRiteController:141`, `BrazierItemAbsorptionRite:55,65`, `BrazierSpecialOfferingEffects:76,88,101`, `SuccessionBrazierLabels` (cube order labels)
- Test: update source contracts that pin the old expressions; `IronBrazierOfferingSourceTest`.

- [ ] **Step 1: Failing source test** `BrazierMountCallSitesSourceTest`: none of the listed files contains `Vec3.atCenterOf(offering.pos()).add(0.0D` or `atCenterOf(pos).add(0.0D, 0.` for brazier positions; each references `BrazierMount`.
- [ ] **Step 2: Run** — Expected: FAIL.
- [ ] **Step 3: Implement** the replacements; the renderer translates the offering by the drop and passes the drop to `BrazierFlames`; daemon stops use `BrazierMount.daemonStop(state, pos, rite centre)`; labels rank offered braziers in a nearby Focus's cube by `CardinalRiteBrazierSpace` order.
- [ ] **Step 4: Run** JVM suite — Expected: 16 baseline failures, none new.

### Task 5: Floor mounds for standing braziers

**Files:**
- Modify: `FloorMesh.java` (`of`/`build`/`footprint`: seat cells = the space's y = 1 layer; moat only for gash cells; only `FACE=FLOOR` braziers seat), `CardinalRiteFloorGeometry.java` (add `standingCells`)
- Test: `GashLoopTest` unchanged; add `FloorMeshSeatSourceTest` pinning that seats require `BrazierBlock.FACE` floor.

- [ ] Steps: failing source test → implement → pass.

### Task 6: Text, docs, visual review, suites

- [ ] Hint labels (`RiteHintScreen:504`, `RitesTabView:426`): "Braziers count anywhere in the N×N×N space above the floor".
- [ ] Succession lang/recipe text: "Clockwise round the Focus from its left, nearest and lowest first, offer…".
- [ ] `docs/HEMOMANCY_REFERENCE.md` socket/membrane paragraphs, `wiki/Harbinger-Path.md`.
- [ ] Add `brazierReviewClient` run (game dir `build/brazier-client-review`, `hemomancy.gameplayDriver`, `hemomancy.brazierReview`); extend `BrazierClientReview` with a wall and a ceiling and lit wall/hanging braziers; run it and inspect screenshots.
- [ ] Full JVM suite, GameTests, Scriptorium suite.
