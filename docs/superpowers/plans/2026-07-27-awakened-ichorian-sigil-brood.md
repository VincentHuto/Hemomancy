# Awakened Ichorian Sigil Brood Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the generic awakened sigil glyph with nine data-driven anatomical castes that continuously unfold from their grounded inscriptions while preserving all existing gameplay.

**Architecture:** Extend each `IchorianSigilDefinition` with optional grounded edge topology and a validated awakened anatomical rig. A pure common-code pose calculator maps grounded nodes through the 40-tick unfolding and caste animation, while the client renderer turns that pose into bounded procedural vessels, landmarks, membranes, an organ, and the shared crimson eye. The existing entity remains responsible for rite-scaled orbit movement and persistence; invalid or absent rigs retain the current generic renderer.

**Tech Stack:** Java 21, NeoForge 1.21.1, Minecraft/JOML vector and rendering APIs, Gson data resources, `FriendlyByteBuf` packet sync, JUnit 5, Gradle.

## Global Constraints

- Grounded sigils remain rigid surface inscriptions.
- Node order remains authoritative for drawing order, blood expenditure, correctness, and learned progress.
- Node counts and `bloodCostMl()` remain unchanged for all nine sigils.
- Existing orbit functions and rite-footprint scaling remain unchanged.
- Awakened size scales from approximately 0.8 blocks at tier one to approximately 1.6 blocks at tier five.
- Every valid rig maps every grounded node exactly once and contains exactly one `EYE` and one `ORGAN`.
- Awakening is continuous: detachment ticks 0–10, node migration ticks 6–32, quickening ticks 18–40, full caste animation after tick 40.
- Original sequential node connections remain the primary living tendons.
- Missing or malformed rigs use the existing generic floating organic sigil fallback.
- Existing JSON and saved awakened entities remain compatible.
- Use one entity per sigil, cached topology, bounded vessel subdivisions, indexed membrane triangles, and no particle-volume-dependent body rendering.
- Gameplay effects, movement bounds, costs, progression, and persistence do not change.

---

## File Structure

### Common data and pose code

- Modify `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilDefinition.java`
  - Add optional grounded connections and optional awakened anatomy while preserving the old constructor.
- Create `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilAnatomy.java`
  - Own the immutable anatomical rig records and enums.
- Create `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilAnatomyValidator.java`
  - Validate mappings, roles, topology, coordinates, and animation parameters.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilLoader.java`
  - Parse optional grounded connections and anatomical rigs, dropping invalid rigs to fallback.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncIchorianKnowledge.java`
  - Sync the two new optional definition sections.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/CardinalRiteSigilProgress.java`
  - Resolve visible grounded edges without changing ordered node progress.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java`
  - Use definition-aware visible grounded edges.
- Create `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilPose.java`
  - Own renderer-independent posed landmarks, vessels, membranes, and growth state.
- Create `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilPoseCalculator.java`
  - Calculate unfolding, tier scaling, and all nine caste deformations.
- Create `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilFacing.java`
  - Calculate stable smoothed yaw from client movement.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/entity/utility/AwakenedIchorianSigilEntity.java`
  - Track client render-facing state and provide a conservative render bound without changing save data or orbit logic.

### Client rendering

- Create `src/main/java/com/vincenthuto/hemomancy/client/render/entity/misc/AwakenedIchorianSigilGeometryRenderer.java`
  - Render procedural tubes, landmarks, membranes, eye, and organ from a calculated pose.
- Modify `src/main/java/com/vincenthuto/hemomancy/client/render/entity/misc/AwakenedIchorianSigilRenderer.java`
  - Select anatomical or generic fallback rendering and orchestrate facing/pose calculation.

### Data

- Modify all nine files under `src/main/resources/data/hemomancy/ichorian_sigil/`
  - Author anatomical rigs; revise grounded topology for Shunt, Mnemonic, Seal, Cage, and Hematic Lattice.

### Tests

- Create `src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilAnatomyValidatorTest.java`
- Create `src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilLoaderTest.java`
- Modify `src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/CardinalRiteSigilProgressTest.java`
- Modify `src/test/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/IchorianKnowledgeSyncPacketTest.java`
- Create `src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilPoseCalculatorTest.java`
- Create `src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilFacingTest.java`
- Create `src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilBroodResourceTest.java`

---

### Task 1: Add the anatomical rig model and validation

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilDefinition.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilAnatomy.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilAnatomyValidator.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilAnatomyValidatorTest.java`

**Interfaces:**
- Produces: `IchorianSigilDefinition.Connection(int from, int to)`
- Produces: `List<IchorianSigilDefinition.Connection> connections()`
- Produces: `Optional<IchorianSigilAnatomy> awakenedForm()`
- Produces: legacy nine-argument `IchorianSigilDefinition(...)` constructor defaulting new fields to empty
- Produces: `IchorianSigilAnatomy` records `Landmark`, `Vessel`, `Membrane`, and `Animation`
- Produces: `IchorianSigilAnatomyValidator.Result validate(int sourceNodeCount, IchorianSigilAnatomy candidate)`

- [ ] **Step 1: Write failing validator and compatibility tests**

Create tests that build this valid rig and assert no validation errors:

```java
private static IchorianSigilAnatomy validRig() {
	return new IchorianSigilAnatomy(
			new Vec3(0.0D, 0.0D, -1.0D),
			new IchorianSigilAnatomy.Animation(
					IchorianSigilAnatomy.Style.ARTERIAL_FORK, 1.0F, 0.7F, 0.25F),
			List.of(
					new IchorianSigilAnatomy.Landmark(0, new Vec3(0, 0, -0.6),
							IchorianSigilAnatomy.Role.EYE, 0.14F),
					new IchorianSigilAnatomy.Landmark(1, new Vec3(0, 0, 0),
							IchorianSigilAnatomy.Role.ORGAN, 0.18F),
					new IchorianSigilAnatomy.Landmark(2, new Vec3(0.5, 0.1, 0.5),
							IchorianSigilAnatomy.Role.VALVE, 0.12F)),
			List.of(new IchorianSigilAnatomy.Vessel(1, 2, 0.07F)),
			List.of(new IchorianSigilAnatomy.Membrane(0, 1, 2)));
}
```

Add individual assertions for duplicate source indices, missing source indices,
out-of-range vessel endpoints, out-of-range membrane endpoints, absent or
duplicate eyes/organs, zero/non-finite forward vectors, non-finite positions,
non-positive radii/thickness, and parameters outside `[0, 2]`.

Also instantiate the existing nine-argument definition constructor and assert
`connections().isEmpty()` and `awakenedForm().isEmpty()`.

- [ ] **Step 2: Run the validator tests and confirm they fail to compile**

Run:

```powershell
./gradlew.bat test --tests '*IchorianSigilAnatomyValidatorTest'
```

Expected: compilation failure because the anatomical types do not exist.

- [ ] **Step 3: Add immutable rig records and definition fields**

Implement:

```java
public record IchorianSigilAnatomy(
		Vec3 forward,
		Animation animation,
		List<Landmark> landmarks,
		List<Vessel> vessels,
		List<Membrane> membranes) {
	public enum Role { EYE, ORGAN, JOINT, VALVE, LIMB_TIP, HOOK, RIB, GANGLION, MEMBRANE_TIP }
	public enum Style {
		PENDULOUS_AMPULLA, NEEDLE_THREAD, CONTRACTILE_SHIELD,
		ARTERIAL_FORK, RECALL_RIBBON, FIVE_LIPPED_SHUTTER,
		WALKING_RIB_TOWER, VASCULAR_ARBOR, OPTIC_STALK_VEIL
	}
	public record Animation(Style style, float pulse, float flex, float lag) {}
	public record Landmark(int source, Vec3 position, Role role, float radius) {}
	public record Vessel(int from, int to, float thickness) {}
	public record Membrane(int a, int b, int c) {}
}
```

Copy all lists in compact constructors. Extend the definition record with:

```java
List<Connection> connections,
Optional<IchorianSigilAnatomy> awakenedForm
```

and preserve current callers with:

```java
public IchorianSigilDefinition(ResourceLocation id, Kind kind, int tier, int color,
		String name, String purpose, int stability, int capacityMl, List<Node> nodes) {
	this(id, kind, tier, color, name, purpose, stability, capacityMl,
			nodes, List.of(), Optional.empty());
}
```

- [ ] **Step 4: Implement strict, non-throwing validation**

`validate` returns the candidate in `Result.form()` only when every invariant
passes; otherwise it returns `Optional.empty()` and a copied list of concrete
error strings. Check exact source coverage using a `BitSet`, exact role counts,
all index bounds, `Double.isFinite`, `Float.isFinite`, a forward-vector length
greater than `1.0E-6`, positive sizes, and animation parameters in `[0, 2]`.

- [ ] **Step 5: Run focused tests**

Run:

```powershell
./gradlew.bat test --tests '*IchorianSigilAnatomyValidatorTest'
```

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilDefinition.java src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilAnatomy.java src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilAnatomyValidator.java src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilAnatomyValidatorTest.java
git commit -m "feat: define ichorian anatomical rigs"
```

### Task 2: Parse rigs and render explicit grounded topology

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilLoader.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/CardinalRiteSigilProgress.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java:754`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilLoaderTest.java`
- Modify: `src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/CardinalRiteSigilProgressTest.java`

**Interfaces:**
- Consumes: anatomical records and validator from Task 1.
- Produces: package-visible `IchorianSigilLoader.parseDefinition(ResourceLocation, JsonObject)`
- Produces: `CardinalRiteSigilProgress.completedConnections(IchorianSigilDefinition, int)`

- [ ] **Step 1: Write failing parser tests**

Parse one complete JSON object and assert its connection, forward, style, roles,
vessel, and membrane values. Parse the current legacy shape below and assert
both new fields remain empty:

```json
{
  "kind": "support",
  "tier": 1,
  "color": "0xE6A23C",
  "name": "Legacy",
  "purpose": "Compatibility",
  "nodes": [[-1, 0], [0, -1], [1, 0], [0, 1]]
}
```

Parse a definition with a duplicate source mapping and assert the definition
still loads but `awakenedForm()` is empty.

- [ ] **Step 2: Write failing grounded-edge tests**

Add a definition with connections `[0,1]`, `[0,2]`, `[2,3]`. Assert:

- one completed node exposes no edges;
- two completed nodes expose only `[0,1]`;
- three completed nodes expose `[0,1]` and `[0,2]`;
- four completed nodes expose all three edges;
- an empty connection list retains the existing sequential behavior.

- [ ] **Step 3: Run the focused tests and confirm failure**

Run:

```powershell
./gradlew.bat test --tests '*IchorianSigilLoaderTest' --tests '*CardinalRiteSigilProgressTest'
```

Expected: FAIL because parsing and definition-aware topology are absent.

- [ ] **Step 4: Extract definition parsing and add optional fields**

Move the existing JSON-to-definition work into
`parseDefinition(ResourceLocation, JsonObject)`. Parse:

```java
connections.add(new IchorianSigilDefinition.Connection(
		pair.get(0).getAsInt(), pair.get(1).getAsInt()));
```

Parse `awakened_form.forward`, `animation`, `nodes`, `vessels`, and `membranes`
into the Task 1 records. Run the validator and log every validation error with
the definition ID before retaining `Optional.empty()` as fallback.

- [ ] **Step 5: Make grounded edge visibility definition-aware**

Implement:

```java
public static List<Connection> completedConnections(
		IchorianSigilDefinition definition, int completedNodes)
```

For authored connections, include an edge only when both endpoint indices are
less than `min(nodeCount, completedNodes)`. For no authored connections, use
the existing sequential implementation. Ignore invalid authored edges
defensively. Change `HarbingerCardinalRiteEvents.visibleSigilSegments` to pass
the definition instead of only `sigil.nodes()`.

- [ ] **Step 6: Run focused and placement/progress regressions**

Run:

```powershell
./gradlew.bat test --tests '*IchorianSigilLoaderTest' --tests '*CardinalRiteSigilProgressTest' --tests '*CardinalRiteSigilPlacementRulesTest' --tests '*CardinalRiteSigilRulesTest'
```

Expected: PASS.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilLoader.java src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/CardinalRiteSigilProgress.java src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilLoaderTest.java src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/CardinalRiteSigilProgressTest.java
git commit -m "feat: load ichorian anatomy and grounded topology"
```

### Task 3: Synchronize full anatomical definitions

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncIchorianKnowledge.java`
- Modify: `src/test/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/IchorianKnowledgeSyncPacketTest.java`

**Interfaces:**
- Consumes: definition and anatomy types from Task 1.
- Produces: package-visible `writeDefinition(FriendlyByteBuf, IchorianSigilDefinition)`
- Produces: package-visible `readDefinition(FriendlyByteBuf)`

- [ ] **Step 1: Write a failing codec round-trip test**

Use `new FriendlyByteBuf(Unpooled.buffer())`, encode a definition containing two
ground connections, all landmark roles, two secondary vessels, and one membrane,
then decode it and assert record equality. Add a legacy definition round trip
with both optional sections empty.

- [ ] **Step 2: Run the packet test and confirm failure**

Run:

```powershell
./gradlew.bat test --tests '*IchorianKnowledgeSyncPacketTest'
```

Expected: FAIL because the packet omits the new fields.

- [ ] **Step 3: Extract definition codec helpers**

Keep packet framing unchanged, but have the definition loop call
`writeDefinition` and `readDefinition`. After nodes, encode:

```java
buffer.writeVarInt(definition.connections().size());
// each from/to as VarInt
buffer.writeBoolean(definition.awakenedForm().isPresent());
```

When present, write forward XYZ doubles; animation style enum and three floats;
landmark source, XYZ, role, radius; vessels; and membranes. Decode in the same
order, validate the decoded form, and drop it to fallback if invalid.

- [ ] **Step 4: Run packet and knowledge tests**

Run:

```powershell
./gradlew.bat test --tests '*IchorianKnowledgeSyncPacketTest' --tests '*IchorianKnowledgeTest'
```

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncIchorianKnowledge.java src/test/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/IchorianKnowledgeSyncPacketTest.java
git commit -m "feat: sync awakened sigil anatomy"
```

### Task 4: Calculate continuous anatomical poses and caste motion

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilPose.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilPoseCalculator.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilPoseCalculatorTest.java`

**Interfaces:**
- Consumes: `IchorianSigilDefinition` and validated `IchorianSigilAnatomy`.
- Produces: `AwakenedIchorianSigilPoseCalculator.calculate(IchorianSigilDefinition definition, float ageTicks)`
- Produces: `AwakenedIchorianSigilPose`
- Produces: `AwakenedIchorianSigilPoseCalculator.tierScale(int tier)`
- Produces: package-visible `AwakenedIchorianSigilPoseCalculator.normalizedGroundPosition(IchorianSigilDefinition, int)`

Define the pose as:

```java
public record AwakenedIchorianSigilPose(
		List<Landmark> landmarks,
		List<Vessel> primaryVessels,
		List<Vessel> secondaryVessels,
		List<Membrane> membranes,
		float detachment,
		float migration,
		float quickening,
		float scale) {
	public record Landmark(int source, Vec3 position,
			IchorianSigilAnatomy.Role role, float radius, float activation) {}
	public record Vessel(int from, int to, float thickness, float growth) {}
	public record Membrane(int a, int b, int c, float inflation) {}
}
```

- [ ] **Step 1: Write failing transformation tests**

Assert:

- at tick 0 every posed node equals its tier-sized
  `normalizedGroundPosition(definition, source)`, preserving the authored shape;
- at tick 6 migration is still zero;
- at tick 19 node positions are finite and between ground and target;
- at tick 32 base migration reaches target positions;
- at tick 18 quickening begins at zero and at tick 40 reaches one;
- no adjacent sampled ticks move a landmark more than 0.3 local blocks;
- primary vessel count is `nodes.size() - 1`;
- secondary vessels and membranes retain authored topology;
- tier scales are exactly `0.8, 1.0, 1.2, 1.4, 1.6`;
- every posed coordinate and radius is finite.

- [ ] **Step 2: Write failing caste-signature tests**

Construct one minimal rig per `Style`, sample ticks 41, 58, and 75, round all
landmark positions to milliblocks, and assert nine distinct signatures. Add
specific behavior checks:

- Ampulla organ radius swells and suspension nodes settle vertically.
- Needle-thread hook pairs alternate laterally.
- Shield rib nodes flex without banking the whole pose.
- Arterial fork alternates valve radii.
- Recall ribbon propagates a phase along source indices.
- Shutter lip radii clamp in sequence.
- Rib tower counter-twists upper/lower nodes.
- Arbor propagates contraction from inner to outer sources.
- Optic veil has increasing lag toward trailing nodes.

- [ ] **Step 3: Run pose tests and confirm failure**

Run:

```powershell
./gradlew.bat test --tests '*AwakenedIchorianSigilPoseCalculatorTest'
```

Expected: compilation failure because the pose types do not exist.

- [ ] **Step 4: Implement stage curves and base migration**

Use clamped smoothstep:

```java
private static float stage(float age, float start, float end) {
	float linear = Mth.clamp((age - start) / (end - start), 0.0F, 1.0F);
	return linear * linear * (3.0F - 2.0F * linear);
}
```

Set detachment `[0,10]`, migration `[6,32]`, and quickening `[18,40]`.
Interpolate every grounded node to the landmark with matching `source`.
Normalize the grounded point set and anatomical target point set independently
around their respective bounding-box centers, preserving each silhouette while
making its largest axis equal to the tier target. Use
`tierScale = 0.6F + 0.2F * clamp(tier, 1, 5)` as that target extent. Interpolate
between the two normalized endpoints so tick 0 is the full rigid inscription
shape and tick 32 is the full anatomical silhouette, both at a readable
tier-consistent size.

- [ ] **Step 5: Implement nine bounded style deformations**

Use a single `switch (anatomy.animation().style())`. Each deformation must be a
deterministic sine/easing function scaled by `pulse`, `flex`, `lag`, landmark
role, source index, and `quickening`. Cap positional deformation at 0.12 local
blocks and radius deformation at ±18%. Apply deformations after base migration,
so the ground endpoint remains exact and all styles continuously grow in.

- [ ] **Step 6: Run pose and existing motion tests**

Run:

```powershell
./gradlew.bat test --tests '*AwakenedIchorianSigilPoseCalculatorTest' --tests '*AwakenedIchorianSigilMotionTest' --tests '*IchorianSigilOrganicGeometryTest'
```

Expected: PASS, proving anatomy does not alter world orbit behavior.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilPose.java src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilPoseCalculator.java src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilPoseCalculatorTest.java
git commit -m "feat: pose awakened ichorian castes"
```

### Task 5: Add stable movement-facing and safe render bounds

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilFacing.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/utility/AwakenedIchorianSigilEntity.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilFacingTest.java`

**Interfaces:**
- Produces: `AwakenedIchorianSigilFacing.update(float previousYaw, double dx, double dz, float smoothing)`
- Produces: `AwakenedIchorianSigilEntity.getRenderFacingYaw(float partialTick)`

- [ ] **Step 1: Write failing facing tests**

Assert stationary movement retains previous yaw, eastward and northward movement
produce their expected Minecraft yaw, wraparound interpolation takes the short
path between `179` and `-179`, and smoothing moves only the configured fraction
toward the target.

- [ ] **Step 2: Run the test and confirm failure**

Run:

```powershell
./gradlew.bat test --tests '*AwakenedIchorianSigilFacingTest'
```

Expected: compilation failure because the facing helper does not exist.

- [ ] **Step 3: Implement stable yaw and client tracking**

Calculate target yaw with `atan2(-dx, dz)`, skip updates when horizontal motion
is below `1.0E-6`, and use wrapped-degree interpolation. In the entity's client
interpolation tick, capture the pre-update position, update the facing from the
actual interpolated delta with smoothing `0.25F`, and retain previous/current
yaw for partial-tick interpolation.

Do not add facing fields to NBT or synchronized entity data. Add a conservative
inflated culling box or equivalent entity render-bounds override with at least
1.0 block clearance around the tier-five 1.6-block body.

- [ ] **Step 4: Run facing, motion, and entity source regressions**

Run:

```powershell
./gradlew.bat test --tests '*AwakenedIchorianSigilFacingTest' --tests '*AwakenedIchorianSigilMotionTest'
```

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilFacing.java src/main/java/com/vincenthuto/hemomancy/common/entity/utility/AwakenedIchorianSigilEntity.java src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/AwakenedIchorianSigilFacingTest.java
git commit -m "feat: orient awakened sigils to flight"
```

### Task 6: Render the anatomical pose with bounded procedural geometry

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/misc/AwakenedIchorianSigilGeometryRenderer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/misc/AwakenedIchorianSigilRenderer.java`

**Interfaces:**
- Consumes: calculated pose from Task 4 and facing from Task 5.
- Produces: `AwakenedIchorianSigilGeometryRenderer.render(AwakenedIchorianSigilPose, PoseStack, MultiBufferSource, float, int, long)`
- Retains: current generic `renderShape(...)` fallback for missing rigs.

- [ ] **Step 1: Extract the current generic renderer without changing output**

Move the current `renderShape`, `renderBand`, and `renderVesselSection` methods
behind a clearly named `renderGenericFallback(...)` path. Keep `SHAPE_SCALE`,
the seven segment vessel bound, current colors, and current node spheres
unchanged.

- [ ] **Step 2: Add pose-driven vessel rendering**

Render primary and secondary vessels as crossed organic ribbons so they remain
visible from non-planar angles. Use no more than seven subdivisions per vessel.
Scale width by posed thickness and growth; skip zero-growth vessels. Use
`IchorianSigilOrganicGeometry.sample` only as a bounded soft deformation around
the already posed 3D endpoints, extending it if necessary to handle full
three-dimensional normals.

- [ ] **Step 3: Add landmark, organ, and eye rendering**

Render landmark spheres at posed positions. Give the organ a restrained
heartbeat and a dark tissue core. Render the single eye as a dark-crimson
ellipsoid with a smaller near-black pupil; its color should be approximately
`0.45F, 0.01F, 0.02F` for the iris/core pass rather than a bright emissive red.
Use role-specific scale only to clarify joints, valves, hooks, ribs, and tips.

- [ ] **Step 4: Add indexed membrane rendering**

For each pose membrane, emit a double-sided triangle using its three posed
landmarks. Alpha and displacement grow with `inflation`; use low-opacity
black-crimson tissue so open castes remain visibly open. Do not triangulate or
allocate topology every frame beyond iterating the cached authored triangles.

- [ ] **Step 5: Orchestrate anatomical versus fallback rendering**

In `AwakenedIchorianSigilRenderer.render`:

```java
if (sigil.awakenedForm().isPresent()) {
	stack.mulPose(Axis.YP.rotationDegrees(entity.getRenderFacingYaw(partialTick)));
	AwakenedIchorianSigilPose pose =
			AwakenedIchorianSigilPoseCalculator.calculate(sigil, time);
	AwakenedIchorianSigilGeometryRenderer.render(
			pose, stack, buffers, time, sigil.color(), sigil.id().hashCode());
} else {
	renderGenericFallback(...);
}
```

Remove the generic constant spinning/90-degree flip from the anatomical path.
The new pose itself performs the unfolding; ambient particles are not used to
define any body part.

- [ ] **Step 6: Compile and run renderer-adjacent tests**

Run:

```powershell
./gradlew.bat compileJava
./gradlew.bat test --tests '*AwakenedIchorianSigil*' --tests '*IchorianSigilOrganicGeometryTest'
```

Expected: compilation succeeds and all focused tests PASS.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/com/vincenthuto/hemomancy/client/render/entity/misc/AwakenedIchorianSigilGeometryRenderer.java src/main/java/com/vincenthuto/hemomancy/client/render/entity/misc/AwakenedIchorianSigilRenderer.java src/main/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilOrganicGeometry.java
git commit -m "feat: render living ichorian anatomy"
```

### Task 7: Author the nine brood rigs and revised grounded shapes

**Files:**
- Modify: `src/main/resources/data/hemomancy/ichorian_sigil/reservoir.json`
- Modify: `src/main/resources/data/hemomancy/ichorian_sigil/suture.json`
- Modify: `src/main/resources/data/hemomancy/ichorian_sigil/bastion.json`
- Modify: `src/main/resources/data/hemomancy/ichorian_sigil/shunt.json`
- Modify: `src/main/resources/data/hemomancy/ichorian_sigil/mnemonic.json`
- Modify: `src/main/resources/data/hemomancy/ichorian_sigil/seal.json`
- Modify: `src/main/resources/data/hemomancy/ichorian_sigil/cage.json`
- Modify: `src/main/resources/data/hemomancy/ichorian_sigil/hematic_lattice.json`
- Modify: `src/main/resources/data/hemomancy/ichorian_sigil/lens.json`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilBroodResourceTest.java`

**Interfaces:**
- Consumes: loader schema and validator from Tasks 1–2.
- Produces: nine valid, complete anatomical definitions.

- [ ] **Step 1: Write the failing resource contract test**

Load all nine JSON resources through `IchorianSigilLoader.parseDefinition`.
Assert:

```java
assertEquals(expectedNodeCounts.get(id), definition.nodes().size());
assertEquals(expectedNodeCounts.get(id) * 50, definition.bloodCostMl());
assertTrue(definition.awakenedForm().isPresent());
assertEquals(expectedStyles.get(id), definition.awakenedForm().orElseThrow().animation().style());
```

Also assert exactly nine distinct styles, each tier's maximum landmark extent
after `tierScale` is at most its target size plus `0.15`, and all authored
connections reference valid nodes.

Expected node counts remain:

```java
Map.of(
		"reservoir", 4, "suture", 4, "bastion", 6,
		"shunt", 5, "mnemonic", 8, "seal", 6,
		"cage", 7, "hematic_lattice", 12, "lens", 9);
```

- [ ] **Step 2: Run the resource test and confirm failure**

Run:

```powershell
./gradlew.bat test --tests '*IchorianSigilBroodResourceTest'
```

Expected: FAIL because current resources have no awakened rigs.

- [ ] **Step 3: Author tiers one and two**

Use these required topology intentions:

- Reservoir / `PENDULOUS_AMPULLA`: keep diamond ground nodes; four targets form
  upper eye/prow, central-low organ, and two suspension tips; one small membrane
  triangle may form the bladder wall.
- Suture / `NEEDLE_THREAD`: keep crossing ground nodes; targets form a long
  forward filament with alternating left/right hooks; eye at the hardened front
  tip and organ behind the midpoint; no enclosing membrane.
- Bastion / `CONTRACTILE_SHIELD`: keep six ground nodes; targets form a vertical
  crescent with eye on the forward rim, organ behind center, rib roles on the
  broad face, and two or three membrane triangles making the shield slab.
- Shunt / `ARTERIAL_FORK`: retain five nodes but revise coordinates into a clear
  Y and add explicit ground connections from the bifurcation to both arms and
  the stem. Targets form two valve branches, a bifurcation organ, a forward eye,
  and an exposed trailing tip; no membranes.

Use these exact revised Shunt ground values:

```json
"nodes": [[0, -1.2], [-1, 0], [0, 0], [1, 0], [0, 1.2]],
"connections": [[2, 0], [2, 1], [2, 3], [2, 4]]
```

- [ ] **Step 4: Author tiers three and four**

Use these required topology intentions:

- Mnemonic / `RECALL_RIBBON`: retain eight nodes, revise ground coordinates into
  two interlocked loops, and use explicit loop connections. Living targets make
  a self-crossing neural ribbon with ganglia distributed through both loops.
- Seal / `FIVE_LIPPED_SHUTTER`: retain six nodes, revise ground coordinates into
  five radial lip tips plus a center, and connect each tip to the center.
  Living targets keep the organ at the throat center, eye on the forward lip,
  and five membrane wedges.
- Cage / `WALKING_RIB_TOWER`: retain seven nodes, revise the ground into paired
  rails and cross-rungs using explicit connections. Living targets form upper
  and lower ring landmarks with vertical ribs and an open middle.
- Hematic Lattice / `VASCULAR_ARBOR`: retain twelve nodes, revise ground
  coordinates and connections into a central branching network. Living targets
  form an exposed 3D arbor with inner valves and outer capillary tips; no
  enclosing membrane.

Use these exact revised ground graphs:

```json
// mnemonic
"nodes": [[-1.4, 0], [-0.7, -0.8], [0, -0.12], [0.7, 0.8],
          [1.4, 0], [0.7, -0.8], [0, 0.12], [-0.7, 0.8]],
"connections": [[0,1], [1,2], [2,3], [3,4], [4,5], [5,6], [6,7], [7,0]]

// seal: node 5 is the center
"nodes": [[0, -1.35], [1.28, -0.42], [0.79, 1.09],
          [-0.79, 1.09], [-1.28, -0.42], [0, 0]],
"connections": [[5,0], [5,1], [5,2], [5,3], [5,4]]

// cage
"nodes": [[-1.2, 1.2], [-1.2, 0], [-1.2, -1.2], [0, -1.45],
          [1.2, -1.2], [1.2, 0], [1.2, 1.2]],
"connections": [[0,1], [1,2], [2,3], [3,4], [4,5], [5,6],
                [0,6], [1,5], [2,4]]

// hematic_lattice: node 0 is the branch heart
"nodes": [[0,0], [0,-0.9], [0,-2], [0.9,0], [2,0], [0,0.9],
          [0,2], [-0.9,0], [-2,0], [1.35,-1.35], [1.35,1.35],
          [-1.35,1.35]],
"connections": [[0,1], [1,2], [0,3], [3,4], [0,5], [5,6],
                [0,7], [7,8], [1,9], [3,9], [3,10], [5,10],
                [5,11], [7,11]]
```

- [ ] **Step 5: Author tier five**

Lens / `OPTIC_STALK_VEIL` keeps its eye-ring-plus-center ground form. The center
node becomes the large forward `EYE`; one inner surrounding node becomes the
`ORGAN`; the other seven surrounding nodes become membrane tips and joints.
Author a trailing fan of non-overlapping membrane triangles attached around the
stalk. Keep total scaled extent near 1.6 blocks.

Use the following exact animation parameters and rig landmarks. Each landmark is
`source: [x, y, z, ROLE, radius]`; coordinates are unscaled local blocks:

```text
reservoir  PENDULOUS_AMPULLA  pulse=1.10 flex=0.65 lag=0.45
  0:[0.00, 0.18,-0.55,EYE,0.14] 1:[-0.30,0.35,0.00,JOINT,0.11]
  2:[0.00,-0.28, 0.12,ORGAN,0.22] 3:[0.30,0.35,0.00,JOINT,0.11]
  vessels=[[1,3,0.06]] membranes=[[1,2,3]]

suture  NEEDLE_THREAD  pulse=0.85 flex=1.20 lag=0.20
  0:[0.00, 0.00,-0.72,EYE,0.12] 1:[-0.24,0.05,-0.18,HOOK,0.09]
  2:[0.06,-0.02, 0.18,ORGAN,0.14] 3:[0.25,-0.05,0.68,HOOK,0.09]
  vessels=[[0,2,0.045]] membranes=[]

bastion  CONTRACTILE_SHIELD  pulse=0.75 flex=0.70 lag=0.30
  0:[0.30, 0.05,-0.55,EYE,0.13] 1:[-0.22,0.48,-0.25,RIB,0.11]
  2:[-0.38,0.22, 0.10,RIB,0.11] 3:[-0.27,-0.05,0.28,ORGAN,0.17]
  4:[-0.12,-0.46,0.08,RIB,0.11] 5:[0.28,-0.34,-0.28,LIMB_TIP,0.10]
  vessels=[[1,4,0.06],[0,5,0.05]] membranes=[[0,1,2],[0,2,3],[0,3,5],[3,4,5]]

shunt  ARTERIAL_FORK  pulse=1.25 flex=0.90 lag=0.15
  0:[0.00,0.00,-0.70,EYE,0.13] 1:[-0.52,0.10,0.18,VALVE,0.14]
  2:[0.00,0.00,0.00,ORGAN,0.18] 3:[0.52,-0.06,0.18,VALVE,0.14]
  4:[0.00,0.06,0.66,LIMB_TIP,0.10]
  vessels=[[2,0,0.06],[2,1,0.075],[2,3,0.075],[2,4,0.06]] membranes=[]

mnemonic  RECALL_RIBBON  pulse=0.95 flex=1.10 lag=0.65
  0:[-0.58,0.02,-0.10,EYE,0.12] 1:[-0.34,-0.28,-0.24,GANGLION,0.10]
  2:[0.00,-0.12,0.02,JOINT,0.09] 3:[0.36,0.30,0.22,GANGLION,0.10]
  4:[0.62,0.02,0.08,ORGAN,0.16] 5:[0.32,-0.30,-0.22,GANGLION,0.10]
  6:[0.00,0.14,-0.02,JOINT,0.09] 7:[-0.36,0.30,0.22,GANGLION,0.10]
  vessels=[[0,7,0.05],[2,6,0.045]] membranes=[]

seal  FIVE_LIPPED_SHUTTER  pulse=0.80 flex=0.85 lag=0.35
  0:[0.00,0.02,-0.58,EYE,0.13] 1:[0.54,0.10,-0.18,LIMB_TIP,0.11]
  2:[0.34,-0.08,0.48,LIMB_TIP,0.11] 3:[-0.34,0.08,0.48,LIMB_TIP,0.11]
  4:[-0.54,-0.10,-0.18,LIMB_TIP,0.11] 5:[0.00,0.00,0.00,ORGAN,0.19]
  vessels=[[5,0,0.06],[5,1,0.06],[5,2,0.06],[5,3,0.06],[5,4,0.06]]
  membranes=[[5,0,1],[5,1,2],[5,2,3],[5,3,4],[5,4,0]]

cage  WALKING_RIB_TOWER  pulse=0.70 flex=0.95 lag=0.40
  0:[-0.42,0.52,0.00,RIB,0.10] 1:[-0.46,0.00,0.02,RIB,0.10]
  2:[-0.38,-0.48,0.00,RIB,0.10] 3:[0.00,-0.58,-0.08,ORGAN,0.17]
  4:[0.38,-0.48,0.00,RIB,0.10] 5:[0.46,0.00,0.02,RIB,0.10]
  6:[0.42,0.52,-0.08,EYE,0.13]
  vessels=[[0,6,0.055],[1,5,0.055],[2,4,0.055]] membranes=[]

hematic_lattice  VASCULAR_ARBOR  pulse=1.20 flex=0.80 lag=0.55
  0:[0.00,0.00,0.00,ORGAN,0.18] 1:[0.00,0.05,-0.34,VALVE,0.10]
  2:[0.00,0.12,-0.72,EYE,0.13] 3:[0.34,0.06,0.00,VALVE,0.10]
  4:[0.72,0.18,0.00,LIMB_TIP,0.09] 5:[0.00,-0.03,0.34,VALVE,0.10]
  6:[0.00,-0.12,0.72,LIMB_TIP,0.09] 7:[-0.34,0.04,0.00,VALVE,0.10]
  8:[-0.72,0.16,0.00,LIMB_TIP,0.09] 9:[0.48,-0.12,-0.42,LIMB_TIP,0.08]
  10:[0.46,0.22,0.44,LIMB_TIP,0.08] 11:[-0.48,-0.16,0.42,LIMB_TIP,0.08]
  vessels use the exact grounded connection list with thickness 0.045; membranes=[]

lens  OPTIC_STALK_VEIL  pulse=0.90 flex=0.75 lag=1.15
  0:[-0.54,0.20,0.26,MEMBRANE_TIP,0.09] 1:[-0.38,-0.18,0.34,MEMBRANE_TIP,0.09]
  2:[0.00,-0.30,0.38,ORGAN,0.15] 3:[0.38,-0.18,0.34,MEMBRANE_TIP,0.09]
  4:[0.54,0.20,0.26,MEMBRANE_TIP,0.09] 5:[0.38,0.42,0.18,MEMBRANE_TIP,0.09]
  6:[0.00,0.52,0.14,JOINT,0.10] 7:[-0.38,0.42,0.18,MEMBRANE_TIP,0.09]
  8:[0.00,0.00,-0.62,EYE,0.22]
  vessels=[[8,0,0.05],[8,2,0.07],[8,4,0.05],[8,6,0.05]]
  membranes=[[8,0,1],[8,1,2],[8,2,3],[8,3,4],[8,4,5],[8,5,6],[8,6,7],[8,7,0]]
```

- [ ] **Step 6: Run resource, pose, progress, and packet tests**

Run:

```powershell
./gradlew.bat test --tests '*IchorianSigilBroodResourceTest' --tests '*AwakenedIchorianSigilPoseCalculatorTest' --tests '*CardinalRiteSigilProgressTest' --tests '*IchorianKnowledgeSyncPacketTest'
```

Expected: PASS.

- [ ] **Step 7: Commit**

```powershell
git add src/main/resources/data/hemomancy/ichorian_sigil src/test/java/com/vincenthuto/hemomancy/common/rite/sigil/IchorianSigilBroodResourceTest.java
git commit -m "data: author ichorian brood castes"
```

### Task 8: Verify compatibility, performance bounds, and the complete feature

**Files:**
- Modify only files from Tasks 1–7 if verification exposes a defect.

**Interfaces:**
- Consumes: the complete feature.
- Produces: verified implementation with no new gameplay behavior.

- [ ] **Step 1: Run formatting and focused source checks**

Run:

```powershell
git diff --check
rg -n "TBD|TODO|FIXME|placeholder" src/main/java/com/vincenthuto/hemomancy/common/rite/sigil src/main/java/com/vincenthuto/hemomancy/client/render/entity/misc/AwakenedIchorianSigilGeometryRenderer.java src/main/resources/data/hemomancy/ichorian_sigil
```

Expected: no whitespace errors and no unfinished markers introduced by this
feature.

- [ ] **Step 2: Run all focused Ichorian and Cardinal Rite tests**

Run:

```powershell
./gradlew.bat test --tests '*Ichorian*' --tests '*CardinalRite*'
```

Expected: PASS.

- [ ] **Step 3: Run the complete JVM suite**

Run:

```powershell
./gradlew.bat test
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Perform in-game visual verification**

Use the repeatable sample Cardinal Rites and inspect all nine castes during
grounding, ticks 0–40 of unfolding, idle, acceleration, turning, and stationary
moments. Verify every item in the design specification's in-game checklist:
distinct silhouettes, shared eye/material language, trackable original nodes,
continuous paths, readable dark-crimson eye, open anatomy where authored,
rite-scaled flight containment, and stable frame rate with the largest normal
set active.

- [ ] **Step 5: Inspect final scope**

Run:

```powershell
git status --short
git diff --stat c470142c9..HEAD
git log --oneline -8
```

Expected: only planned source, resource, test, and documentation files belong to
this feature; pre-existing unrelated worktree changes remain untouched.

- [ ] **Step 6: Commit any verification fixes**

If verification required changes, stage only the affected planned files and
commit:

```powershell
git commit -m "fix: polish awakened ichorian brood"
```

If no fixes were needed, do not create an empty commit.
