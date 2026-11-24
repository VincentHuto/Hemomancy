# Ichorian Sigil Rendering Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task (the owner runs plans natively, with one final reviewer). Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Sigil links render as shader-drawn gouges that fill, glow and grow veins when the pattern closes, and the living form hovers over its pattern instead of orbiting the rite.

**Architecture:** The server tags each synced `SigilSegment` with its sigil, link index and awakened flag. Pure rules in `common/rite/sigil` turn client-stamped ages into stage values and turn a link into a ground strip; `CardinalRiteBoundaryRenderer` draws the strips with a new `ichorian_sigil_gouge` shader and adds node tendrils through the existing tendril pass. The entity's orbit motion is replaced by a fixed hover point with a bob and a slow turn.

**Tech Stack:** Java 21, NeoForge 1.21.1, GLSL 150 core shaders, JUnit 5, the repo's review-client harness.

**Spec:** `docs/superpowers/specs/2026-11-19-ichorian-sigil-rendering-design.md`

## Global Constraints

- No commits. The owner commits; leave all work in the working tree.
- One Gradle process at a time, review clients included.
- Follow `AGENTS.md`: no top-level feature folders; nothing under `common/` names a client class; render types are built once (`RenderTypeCachingGuardrailTest`); no vanilla particles; no `Component.literal` English; animation time only from `AnimationTime`.
- No vertical beams or light shafts. Veins and tendrils stay thin and partly clear; the completion moment is bold.
- No copper, brass or bell timbre. No new sound is needed; `RITE_SIGIL_AWAKEN` stays.
- `cardinal_floor_membrane.fsh`, `FloorMesh` and `GashLoop` are not edited.
- No gameplay change: node order, blood cost, awakening rules and sockets stay as they are.
- The mod is unreleased: no migration for old saved sigil entities.
- A payload layout change bumps `PacketHandler.PROTOCOL_VERSION` (now `"57"`) and rewrites `src/test/resources/network/payload-registry.txt`.
- JVM test command: `./gradlew.bat test --tests "<fully.qualified.TestClass>"`. Before calling the work done: `./gradlew.bat test` green.

## Review Focus

1. Nodes at different heights (stepped ground): the strip must run from one node's height to the other's without NaN or reversed winding. Test in Task 3; judged by eye in Task 7's stepped-ground still.
2. Zero-length link (both ends on the same spot): emits no quads, no division by zero. Test in Task 3.
3. A player who joins or re-logs mid-rite sees links already cut and an awake sigil already settled, with no replay. Test in Task 2.
4. A rite ends and a new rite starts on the same centre: the old stamps are gone, the new pattern cuts in fresh. Test in Task 2.
5. A sigil entity loaded from a save older than this change (no hover tags) discards itself instead of hovering at the world origin. Test in Task 6.

---

### Task 1: Sync the sigil key, link index and awakened flag

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/ActiveRiteData.java` (record `SigilSegment`, about line 443)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteSync.java` (`visibleSigilSegments`, `toClientSigilSegment`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncActiveRites.java` (write about line 77, read about line 172)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java` (`PROTOCOL_VERSION` to `"58"`)
- Modify: `src/test/resources/network/payload-registry.txt`
- Test: the existing payload round-trip test for `PacketSyncActiveRites` if one exists; otherwise add `src/test/java/com/vincenthuto/hemomancy/common/network/SigilSegmentCodecTest.java`

**Interfaces:**
- Produces:
  ```java
  public record SigilSegment(
          double startX, double startY, double startZ,
          double endX, double endY, double endZ,
          int color, String sigilKey, int linkIndex, int linkCount, boolean awakened) {}
  ```
  `sigilKey` is `SigilPlacement.progressKey()`. `linkIndex` is the connection's position in `CardinalRiteSigilProgress.completedConnections(sigil, completed)`. `linkCount` is the number of connections the finished sigil has (`completedConnections(sigil, sigil.nodes().size()).size()`). `awakened` is `rite.isSigilAwakened(placement.progressKey())`.

- [ ] **Step 1: Write the failing test.** Encode a rite entry holding one segment `(1,64.1,2, 3,65.1,2, 0xAA1122, "hemomancy:reservoir@0", 2, 5, true)` through the packet's stream codec and assert every field of the decoded segment equals the original.
- [ ] **Step 2: Run it; expect a compile failure on the new record components.**
- [ ] **Step 3: Extend the record, the server builder and the packet's write and read (string as UTF, the two ints as var ints, the flag as a boolean, after `color`).** Fix the other constructor call sites the compiler reports.
- [ ] **Step 4: Bump `PROTOCOL_VERSION` to `"58"` and rewrite `payload-registry.txt` the way its test's failure message instructs.**
- [ ] **Step 5: Run the codec test and the payload registry test; expect PASS.**

---

### Task 2: Stage rules and client stamps

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilGougeStages.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/data/SigilGougeClientState.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/data/ActiveRiteClientData.java` (call the state's `observe` where a rite sync is applied, and its `clear` where rite client data is cleared)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilGougeStagesTest.java`, `src/test/java/com/vincenthuto/hemomancy/client/data/SigilGougeClientStateTest.java`

**Interfaces:**
- Consumes: `ActiveRiteData.SigilSegment` from Task 1.
- Produces:
  ```java
  public final class IchorianSigilGougeStages {
      public static final float CUT_IN_TICKS = 20.0F;
      public static final float COMPLETION_TICKS = 40.0F;   // fill and first glow run
      public static final float VEIN_CREEP_TICKS = 60.0F;   // starts with completion
      public static final float RERUN_PERIOD_TICKS = 160.0F; // the dimmer front that re-runs while awake
      public static final float SETTLED = 1.0e6F;            // an age meaning "long done"

      /** 0..1: how far along its length the link has opened. */
      public static float cutIn(float linkAgeTicks);
      /** 0..1: blood level in the cut; 0 until the sigil is awake. */
      public static float fill(float awakeAgeTicks);
      /** 0..1 glow at a point of the pattern. patternAlong is (linkIndex + along) / linkCount.
          First run: a bright front crossing 0..1 over COMPLETION_TICKS. After it: 0.35 + 0.25 * beat,
          plus a 0.4 front re-running every RERUN_PERIOD_TICKS. Negative awakeAge gives 0. */
      public static float glow(float awakeAgeTicks, float patternAlong, float beat);
      /** 0..1: how far the ground veins have crept from the lip. */
      public static float veinReach(float awakeAgeTicks);
      /** 0..1: how far the node tendrils have risen; starts when the glow front has passed (COMPLETION_TICKS). */
      public static float tendrilRise(float awakeAgeTicks);
  }
  ```
  ```java
  public final class SigilGougeClientState {
      /** Called once per applied rite sync. firstSightOfRite is true when this client had no entry for the rite before. */
      public static void observe(BlockPos riteCenter, List<ActiveRiteData.SigilSegment> segments,
              long gameTime, boolean firstSightOfRite);
      /** Ticks since the link was first seen, or SETTLED if it arrived with the rite's first sight. */
      public static float linkAge(BlockPos riteCenter, String sigilKey, int linkIndex, float gameTime);
      /** Ticks since the sigil was first seen awake; negative if it is not awake; SETTLED if first seen awake with the rite. */
      public static float awakeAge(BlockPos riteCenter, String sigilKey, float gameTime);
      public static void forget(BlockPos riteCenter);
      public static void clear();
  }
  ```
  All eases go through `HemoEasing`.

- [ ] **Step 1: Write the failing stage tests.**
  - `cutIn(0) == 0`, `cutIn(CUT_IN_TICKS) == 1`, `cutIn(SETTLED) == 1`, monotone between.
  - `fill(-1) == 0`, `fill(COMPLETION_TICKS) == 1`.
  - `glow(-1, 0.5F, 1) == 0`.
  - During the first run the front moves: `glow(10, 0.25F, 0) > glow(10, 0.9F, 0)` and `glow(35, 0.9F, 0) > glow(35, 0.1F, 0)`.
  - Settled: `glow(SETTLED, x, 0)` is within `[0.35, 0.75]` for x in {0, 0.5, 1}, and `glow(SETTLED, 0.5F, 1) > glow(SETTLED, 0.5F, 0)` at a time off the re-run front.
  - `veinReach(0) == 0`, `veinReach(VEIN_CREEP_TICKS) == 1`; `tendrilRise(COMPLETION_TICKS) == 0`, `tendrilRise(SETTLED) == 1`.
- [ ] **Step 2: Write the failing client-state tests.**
  - First sight of a rite with two links and `awakened == true`: `linkAge` and `awakeAge` both return `SETTLED` (Review Focus 3).
  - A later `observe` adding link 2 at game time 100: `linkAge(..., 2, 110) == 10`; links 0 and 1 unchanged.
  - A later `observe` flipping `awakened` at 200: `awakeAge(..., 215) == 15`; before the flip it is negative.
  - `observe` with an empty list drops that rite's stamps; `forget` then a fresh first-sight `observe` at the same centre with `firstSightOfRite == false` and one link at 300 gives `linkAge(..., 0, 305) == 5` (Review Focus 4).
- [ ] **Step 3: Run both; expect FAIL (classes missing).**
- [ ] **Step 4: Implement both classes.** Stamps live in a map keyed by rite centre, then by sigil key; `observe` removes keys absent from the new list.
- [ ] **Step 5: Wire `observe`, `forget` and `clear` into `ActiveRiteClientData`** (first sight is "no entry for this centre before this sync"; `forget` when a rite's entry is removed; `clear` with the data's own clear).
- [ ] **Step 6: Run both tests; expect PASS.**

---

### Task 3: Gouge strip geometry

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilGougeGeometry.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilGougeGeometryTest.java`

**Interfaces:**
- Produces:
  ```java
  public final class IchorianSigilGougeGeometry {
      public static final float CUT_HALF_WIDTH = 0.16F;   // the cut itself
      public static final float STRIP_HALF_WIDTH = 0.90F; // cut + lip + vein skirt
      public static final float STEPS_PER_BLOCK = 2.0F;
      /** Synced segment heights are surface + 0.10; the strip sits at surface + 0.012. */
      public static final float SURFACE_DROP = 0.088F;

      /** Cross-sections from start to end, at least two; empty when the link is shorter than 0.05 blocks. */
      public static List<Cross> strip(double startX, double startY, double startZ,
              double endX, double endY, double endZ);

      /** One cross-section. along is 0..1 down the link; alongBlocks is the distance from the start. */
      public record Cross(double leftX, double leftZ, double rightX, double rightZ, double y,
              float along, float alongBlocks) {}
  }
  ```
  Left and right are `STRIP_HALF_WIDTH` either side of the centre line, perpendicular to it in the XZ plane; `y` is linear between the two ends, each lowered by `SURFACE_DROP`.

- [ ] **Step 1: Write the failing tests.**
  - A 4-block link along +X at y 64.1: 9 cross-sections; first `along == 0`, last `along == 1` and `alongBlocks == 4`; every section's left and right are 1.8 apart and straddle the centre line; `y == 64.012` throughout.
  - Stepped link (start y 64.1, end y 65.1): first `y == 64.012`, last `y == 65.012`, strictly rising; no NaN (Review Focus 1).
  - Left stays on the same side for a link along +X and one along -Z (cross product sign of direction × (left − centre) is constant), so winding does not flip with direction.
  - A link with identical ends, and one 0.01 long, both return an empty list (Review Focus 2).
- [ ] **Step 2: Run; expect FAIL.**
- [ ] **Step 3: Implement `strip`.** Steps = `max(1, ceil(length * STEPS_PER_BLOCK))`.
- [ ] **Step 4: Run; expect PASS.**

---

### Task 4: The gouge shader and its draw

**Files:**
- Create: `src/main/resources/assets/hemomancy/shaders/core/world/ichorian_sigil_gouge.json`, `.vsh`, `.fsh`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/shader/ShaderInit.java` (holder `ICHORIAN_SIGIL_GOUGE`, registered like `FANE_GROUND`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java` (`ichorianSigilGouge()`; follow `faneGround` for how the time uniform is fed and for the build-once rule)
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/CardinalRiteBoundaryRenderer.java` (replace `drawSigilSegments` and `drawOrganicSigilSection`; add the gouge buffer pass in `render`)
- Test: add the shader's noise imports to whatever list `ShaderNoiseIncludeGuardrailTest` checks if it needs one; `RenderTypeCachingGuardrailTest` must stay green.

**Interfaces:**
- Consumes: `IchorianSigilGougeGeometry.strip`, `IchorianSigilGougeStages.*`, `SigilGougeClientState.linkAge/awakeAge`, `IchorianSigilRenderPalette`.
- Produces: `HemoRenderTypes.ichorianSigilGouge()` returning the one render type; a private `drawSigilGouges(PoseStack, VertexConsumer, RiteFrame, float time, Vec3 cam)` in the boundary renderer.

**Vertex contract** (`DefaultVertexFormat.NEW_ENTITY`, quads, translucent blend, no depth write, depth test on, no cull):

| Attribute | Carries |
|---|---|
| Position | camera-relative strip corner |
| Color rgb | the sigil's authored ichor colour (`IchorianSigilRenderPalette.authoredIchor(color, false)`) |
| Color a | `frame.fade` |
| UV0 | x: across, −1 at the left edge to +1 at the right; y: `alongBlocks` |
| UV1 (overlay) | x: `cutIn × 1000`, y: `fill × 1000` |
| UV2 (lightmap) | x: `glow × 1000`, y: `veinReach × 1000` |
| Normal | x: link seed in −1..1 (hash of `sigilKey` and `linkIndex`); y: `along` mapped to −1..1; z: 0 |

`glow` is computed per cross-section on the CPU with `patternAlong = (linkIndex + along) / linkCount` and `frame.heartbeat` as the beat, so the shader needs only `HemoTime` besides the standard matrices and fog.

**What the fragment shader draws**, across the strip with `a = abs(across) × STRIP_HALF_WIDTH` in blocks:

- `a < CUT_HALF_WIDTH` (jittered by noise along the length so the edge is torn, not ruled): the cut. Discard where `along > cutIn` (with a short ragged tip). Inside: a faked floor found by shifting the sample with the view direction as `gash()` does in the floor shader (read it for the method; write this one fresh); near-black walls shading to the floor; a thin wet sheen while dry. With `fill > 0` a blood surface rises from the floor to just under the lip, in the vertex colour, with slow flow noise along the length. `glow` adds emissive brightness to the blood and a narrow spill of light on the walls and lip; it never leaves the cut's neighbourhood.
- Lip, `CUT_HALF_WIDTH` to about `0.26`: a slightly raised, darker, bruised edge, alpha fading outward; crumbs of torn ground by noise.
- Skirt, lip to `STRIP_HALF_WIDTH`: transparent until `veinReach > 0`. Then thin branching veins rooted at the lip, reaching `veinReach × (STRIP_HALF_WIDTH − 0.26)` outward, writhing by slow `HemoTime` noise, thinning and fading to zero before the strip edge and near both link ends. Vein alpha stays under 0.7 and most of the skirt stays clear.
- Shared noise comes from `shaders/include/noise/*.glsl` via `#moj_import`, one per include, in dependency order.

- [ ] **Step 1: Add the shader triple, the `ShaderInit` holder and the render type.**
- [ ] **Step 2: Replace the ribbon draw.** In `render`, after the `RITE_BOUNDARY_GLOW` batch and before `RITE_BOUNDARY_CORE`, take the gouge buffer, call `drawSigilGouges` for every frame, end the batch. Remove `drawSigilSegments`, `drawOrganicSigilSection`, `SIGIL_VESSEL_SEGMENTS` and the call at the end of `drawRite`. The node landmarks (`drawSanguineBlobs`) stay and now sit over the link ends.
- [ ] **Step 3: `./gradlew.bat test --tests "*RenderTypeCachingGuardrailTest" --tests "*ShaderNoiseIncludeGuardrailTest" --tests "*MaterialQuadGuardrailTest"`; expect PASS.**
- [ ] **Step 4: Leave GPU proof to Task 7's review client** (offline validators miss driver errors).

---

### Task 5: Node tendrils

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/world/IchorianSigilNodeTendrilGeometry.java`
- Modify: `CardinalRiteBoundaryRenderer.java` (draw them in the existing `HLRenderTypeInit.TENDRIL_GLOW` / `TENDRIL_CORE` passes beside `drawStaffTendrils`, through `SanguineTendrilRibbonRenderer`)
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/IchorianSigilNodeTendrilGeometryTest.java`

**Interfaces:**
- Consumes: `SanguineTendrilRibbonRenderer.Strand` / `.Joint` (as `CardinalRiteStaffTendrilGeometry` implements them); `IchorianSigilGougeStages.tendrilRise`; `AwakenedIchorianSigilMotion.HOVER_HEIGHT` (Task 6; use the literal `2.2F` behind a local constant if Task 6 is not yet in, then switch).
- Produces:
  ```java
  public static final int TENDRILS_PER_NODE = 2;
  public static final int MAX_NODES = 12;
  public static final float REACH = 0.6F;          // share of the way to the hover point
  public static final float ROOT_HALF_WIDTH = 0.035F;

  /** Unique link endpoints of one awakened sigil, at most MAX_NODES, picked evenly when there are more. */
  public static List<Vec3> nodes(List<ActiveRiteData.SigilSegment> sigilSegments);
  /** The point the tendrils lean toward: the nodes' centroid, HOVER_HEIGHT above their mean height. */
  public static Vec3 hoverPoint(List<Vec3> nodes);
  public static List<Strand> strands(List<Vec3> nodes, Vec3 hoverPoint, long seed, float time, float rise);
  ```
  Each strand rises from its node, bows outward a little, leans to the hover point, sways by time, tapers to zero width, fades toward the tip (tip opacity 0), and ends at `REACH × rise` of the way.

- [ ] **Step 1: Write the failing tests.**
  - Four segments forming a square give 4 nodes; 30 distinct endpoints give 12.
  - `strands(nodes, hover, seed, t, 1)` has `nodes.size() × 2` strands; every joint's distance to the hover point is at least 40% of its root's distance (they stop short); width and opacity fall from root to tip and the tip opacity is 0; root half-width is `ROOT_HALF_WIDTH`.
  - `rise == 0` gives no strands.
  - Same inputs give identical output; a different `time` moves the tip by more than 0 and less than 0.3 blocks.
- [ ] **Step 2: Run; expect FAIL.**
- [ ] **Step 3: Implement the geometry and the draw.** Group the frame's segments by `sigilKey`; draw only for groups with `awakened`, with `rise` from `SigilGougeClientState.awakeAge` and alpha multiplied by `frame.fade`.
- [ ] **Step 4: Run; expect PASS.**

---

### Task 6: The living form hovers

**Files:**
- Modify: `common/rite/sigil/AwakenedIchorianSigilMotion.java`, `AwakenedIchorianSigilBodyAnimation.java`, `AwakenedIchorianSigilPoseCalculator.java`, `AwakenedIchorianSigilFacing.java`
- Modify: `common/entity/utility/AwakenedIchorianSigilEntity.java`
- Modify: `common/rite/harbinger/CardinalRiteInteractionHandler.java` (`spawnAwakenedSigil`, about line 400)
- Modify: `client/render/entity/misc/AwakenedIchorianSigilRenderer.java`
- Modify: `common/rite/CardinalRiteFootprintRules.java` (remove `awakenedSigilOrbitRadius` and its two constants if nothing else reads them)
- Test: update `AwakenedIchorianSigilMotionTest`, `AwakenedIchorianSigilFacingTest`, `AwakenedIchorianSigilBodyAnimationTest`, `AwakenedIchorianSigilPoseCalculatorTest`, `CardinalRiteFootprintRulesTest`; check `AnimationTimeGuardrailTest`'s entry for the entity still holds.

**Interfaces:**
- Produces, in `AwakenedIchorianSigilMotion`:
  ```java
  public static final int PEEL_TICKS = 30;            // unchanged
  public static final float HOVER_HEIGHT = 2.2F;      // above the pattern's mean node surface
  public static final float BOB_AMPLITUDE = 0.08F;
  public static final float BOB_PERIOD_TICKS = 80.0F;

  /** The fixed point the form holds: the sigil's node centroid in the world, HOVER_HEIGHT above surfaceY. */
  public static Position hoverPoint(IchorianSigilDefinition sigil, double placementX, double surfaceY, double placementZ);
  /** Peel from the origin to the hover point, then hover there with a vertical bob only. */
  public static Position position(double originX, double originY, double originZ,
          double hoverX, double hoverY, double hoverZ, int ageTicks);
  /** Degrees. A slow turn in place; the rate is the sigil's own. */
  public static float turnYaw(ResourceLocation sigilId, float phaseDegrees, float ageTicks);
  public static Position smoothStep(Position current, Position target, int remainingSteps); // unchanged
  ```
  Turn rates in degrees per tick: `shunt` 1.2, `suture` 0.9, `lens` 0.7, `mnemonic` 0.6, `hematic_lattice` 0.5, `cage` 0.45, `bastion` 0.35, `reservoir` 0.3, `seal` 0.12, anything else 0.5.
- `AwakenedIchorianSigilBodyAnimation.pose(ResourceLocation sigilId, float ageTicks)` and `AwakenedIchorianSigilPoseCalculator.calculate(IchorianSigilDefinition sigil, float time)`: the movement-speed parameter goes. Where speed scaled a motion, use a per-sigil idle level in its place (the same ordering as the turn rates: shunt liveliest, seal stillest) so breathing and limb drift never stop.
- `AwakenedIchorianSigilFacing` keeps `authoredForwardCorrection` and `renderYaw`; the two `update` overloads and `Orientation` go if nothing else uses them.
- Entity: synced data becomes `SIGIL_ID`, `RITE_CASTER`, `FULLY_PEELED`, `HOVER` (`Vector3f`), `TURN_PHASE` (float degrees). `initialize(UUID caster, ResourceLocation sigilId, double originX, double originY, double originZ, Position hover, float turnPhase)`. NBT keys `HoverX/Y/Z` and `TurnPhase` replace `RiteCenter`, `OrbitRadius`, `StartingAngle`. Client render state shrinks to the turn yaw (`getRenderFacingYaw(partialTick)` from `turnYaw` on the entity's age); pitch, bank and movement speed and their getters go. The 20-tick orbit radius refresh and the travel-facing block go. The rite-alive check and `discard()` stay.
- `spawnAwakenedSigil` computes the hover point with `hoverPoint(sigil, centreX + placement.x() + 0.5, surface.getY(), centreZ + placement.z() + 0.5)` and passes `turnPhase` from the hash it used for the starting angle.

- [ ] **Step 1: Rewrite the motion tests to fail first.**
  - `position(..., 0)` is the origin; `position(..., PEEL_TICKS)` is within `BOB_AMPLITUDE` of the hover point vertically and exact horizontally.
  - For ages 30 to 2000, x and z equal the hover point's exactly and `|y − hoverY| <= BOB_AMPLITUDE` (it does not travel).
  - Consecutive ticks never differ by more than 0.02 in y after the peel.
  - `hoverPoint` for a sigil whose nodes are (−2,0), (2,0), (0,2) at placement (10, 64, 20) is `(10, 66.2, 20.667)` to 0.001.
  - `turnYaw("shunt", 0, 100) == 120`, `turnYaw("seal", 0, 100) == 12`, `turnYaw("shunt", 40, 0) == 40`; unknown id uses 0.5.
  - Keep the existing `smoothStep` test unchanged.
- [ ] **Step 2: Update the body-animation and pose tests** to the new signatures: for every known sigil id the pose at two different ages differs (it is never frozen at rest), and shunt's offset range over 200 ticks exceeds seal's.
- [ ] **Step 3: Add the old-save test** (Review Focus 5): a pure static `AwakenedIchorianSigilEntity.hasHover(CompoundTag)` returns false for a tag with the old keys only and true with `HoverX/Y/Z`; `readAdditionalSaveData` marks the entity to discard on its first server tick when it is false.
- [ ] **Step 4: Run the five test classes; expect FAIL or compile errors.**
- [ ] **Step 5: Implement.** Delete the nine orbit functions, `offsetFor`, `polar`, `bounded`, `Offset`.
- [ ] **Step 6: Remove `awakenedSigilOrbitRadius`, its constants and its three test cases if no caller remains** (`Grep` first).
- [ ] **Step 7: Switch Task 5's hover height to `AwakenedIchorianSigilMotion.HOVER_HEIGHT`.**
- [ ] **Step 8: Run the five test classes plus `*AnimationTimeGuardrailTest` and `*CommonClientIsolationTest`; expect PASS.**

---

### Task 7: Review client, tuning and docs

**Files:**
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/SigilGougeClientReview.java` (model it on `FaneGroundClientReview.java`)
- Modify: `gradle/review-clients.gradle`: row `sigilGougeReviewClient: [dir: 'build/sigil-gouge-client-review', user: 'SigilGouge', size: review, props: ['hemomancy.sigilGougeReview': 'true']]`
- Modify: `docs/CARDINAL_RITES.md`, `docs/HEMOMANCY_REFERENCE.md` (the rite sections that describe sigils and the awakened brood), `docs/OPEN_QUESTIONS.md`, `docs/TESTING.md` (list the review client), `docs/architecture/evidence/source-text-impact-map.csv` (regenerate with `python gradle/source-text-impact-map.py` if any test's read set changed)

**Scene and stills** (the client logs `SIGIL_GOUGE_REVIEW captured N` and stops):

| Still | Setup |
|---|---|
| `open_flat` | a running rite on flat ground, one sigil with three of its links cut, one mid cut-in |
| `completing` | the same sigil about 20 ticks after its last node, glow front mid-pattern |
| `awake_flat` | settled: filled gouges, veins out, node tendrils up, form hovering |
| `awake_low_angle` | the same from eye height at a grazing angle (the faked depth must hold) |
| `awake_stepped` | a sigil whose nodes sit on ground stepped by one block |
| `awake_night` | `awake_flat` at night, for glow strength |
| `two_sigils` | two awake sigils in one rite, to check they do not turn in step |

- [ ] **Step 1: Add the row and the review class.** Stage states by feeding sigil progress through the rite's own server paths so the real sync and beats run.
- [ ] **Step 2: Launch `./gradlew.bat sigilGougeReviewClient`.** When a new `"kind":"startup"` line appears in `build/sigil-gouge-client-review/operator/journal.jsonl`, drop `{"op":"create","name":"SigilGouge","seed":"1"}` into `operator/inbox/`. Confirm it got past the first-load Accessibility screen. Expected: the log shows no shader compile or link error and `SIGIL_GOUGE_REVIEW captured 7`.
- [ ] **Step 3: Read every still and tune.** Check against the spec: cuts read as depth, not paint; lips torn; glow stays in the gouges; veins thin and mostly clear; no thick strands; tendrils stop short of the form; the form is centred over its pattern; the stepped strip does not sink into or float off the step by more than is plain to the eye. If the stepped case fails, sample the terrain per cross-section in the renderer (client level height at each step) and re-shoot. Re-run until it holds.
- [ ] **Step 4: Update the docs.** Record the built behaviour in `CARDINAL_RITES.md` and the reference; add one play-and-tune row to `OPEN_QUESTIONS.md` listing the owner-tunable numbers (strip widths, timings, glow levels, hover height, turn rates, tendril count) and the unreviewed calls from the spec's "Calls made without the owner".
- [ ] **Step 5: `./gradlew.bat test`; expect green** apart from failures already present on HEAD (note any by name).
