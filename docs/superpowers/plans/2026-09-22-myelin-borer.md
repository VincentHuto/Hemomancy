# Myelin Borer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish the orphaned Myelin Borer into the Cortical Drift's fiber crawler — a segmented mob that skitters along the nerve-bridge network, mending it when calm and devouring it when provoked.

**Architecture:** The entity class already exists but is never registered, so Phase 1 wires it into the game unchanged. Phase 2 adds tag-driven climbing with the maths in a pure, unit-testable rules class. Phase 3 adds the two-mood AI as two `MoveToBlockGoal` subclasses following the existing `ChewWoodGoal`. Phase 4 adds the drop, the craft, spawning, GameTests and docs.

**Tech Stack:** NeoForge 1.21.1 (21.1.219), Java 21, JUnit 5, NeoForge GameTest.

**Spec:** `docs/superpowers/specs/2026-09-22-myelin-borer-design.md`

## Global Constraints

- NeoForge imports only (`net.neoforged.*`). Never `net.minecraftforge.*`.
- Resource locations via `Hemomancy.rloc("snake_case_id")`; registries via `DeferredHolder`/`DeferredRegister`.
- Singular 1.21 data paths: `data/hemomancy/loot_table/entities/`, `data/hemomancy/tags/block/`, `data/hemomancy/recipe/`. Never pluralize.
- **Registration is JSON-authoritative**: `src/main/resources` overrides `src/generated`. Java bootstrap methods such as `BiomeInit.bootstrapBiomes` are DEAD CODE in this repo, so any biome change must edit the JSON under `src/main/resources`.
- Java files use TABS.
- Borers must NEVER target `synaptic_node` — hubs and anchors must survive so the network stays recoverable.
- Lore: the Borer is maintenance tissue, never a deity or vermin. No God/Satan framing.
- Do not touch `VagrantMindPlacementRules` — its clearance radius is a separate open decision.
- Build commands: `./gradlew.bat test`, `./gradlew.bat compileJava`, `./gradlew.bat build -x test`, `./gradlew.bat runGameTestServer`, `./gradlew.bat runData`.
- **Known pre-existing failures, not yours:** `PhlegethonticBombardierResourceTest` (missing `data/hemomancy/tags/entity_type/fervent.json`), `MorphlingLumenlaceRenameResourceTest` (a non-UTF8 byte in `docs/phlegethontic-nether-worldgen/orcadian-horseman/README.md`), and ~21 GameTest failures in journey/economy/bestiary fixtures. Never "fix" these and never create that tags file.

---

## File Structure

| File | Responsibility |
|---|---|
| `common/init/EntityInit.java` | entity type + attribute registration (modify) |
| `common/init/ItemInit.java` | spawn egg + `ganglion_cluster` (modify) |
| `client/event/ClientEvents.java` | renderer binding (modify) |
| `client/event/LayerEvents.java` | model layer binding (modify) |
| `common/entity/mob/arthropod/MyelinBorerEntity.java` | entity: climbing, agitation, goals, spawn rule (modify) |
| `common/entity/mob/arthropod/MyelinBorerMovementRules.java` | pure climb/agitation maths (create) |
| `common/entity/mob/arthropod/FiberGapRules.java` | pure gap detection (create) |
| `common/entity/mob/arthropod/MendFiberGoal.java` | calm goal: fill gaps (create) |
| `common/entity/mob/arthropod/BoreFiberGoal.java` | agitated goal: sever fibers (create) |
| `common/entity/mob/arthropod/MyelinBorerAgitationEvents.java` | `BlockEvent.BreakEvent` handler (create) |
| `data/hemomancy/tags/block/cortical_crawlable.json` | what the Borer grips (create) |
| `data/hemomancy/loot_table/entities/myelin_borer.json` | drops (create) |
| `data/hemomancy/recipe/ganglion_cluster_to_synaptic_node.json` | the craft (create) |
| `data/hemomancy/worldgen/biome/cortical_drift.json` | spawn entry (modify) |

---

# Phase 1 — Make the mob exist

### Task 1: Register the entity, attributes, renderer, layer and spawn egg

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/entity/MyelinBorerRegistrationSourceTest.java`

**Interfaces:**
- Produces: `EntityInit.myelin_borer` (`DeferredHolder<EntityType<?>, EntityType<MyelinBorerEntity>>`), `ItemInit.spawn_egg_myelin_borer`.
- Consumes: the existing `MyelinBorerEntity`, `MyelinBorerModel.LAYER_LOCATION`, `MyelinBorerRenderer`.

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MyelinBorerRegistrationSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath));
	}

	@Test
	void entityTypeIsRegistered() throws IOException {
		String entityInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java");
		assertTrue(entityInit.contains("ENTITY_TYPES.register(\"myelin_borer\""),
				"the Myelin Borer class exists but was never registered, so it cannot spawn at all");
		assertTrue(entityInit.contains("MyelinBorerEntity::new"),
				"the entity type must build from the entity class");
		assertTrue(entityInit.contains("MyelinBorerEntity.setAttributes().build()"),
				"without attributes the game crashes the moment one spawns");
	}

	@Test
	void clientBindingsExist() throws IOException {
		assertTrue(read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java")
				.contains("EntityInit.myelin_borer.get(), MyelinBorerRenderer::new"),
				"an unregistered renderer makes the mob invisible");
		assertTrue(read("src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java")
				.contains("MyelinBorerModel.LAYER_LOCATION"),
				"an unregistered layer definition crashes the renderer");
	}

	@Test
	void spawnEggExists() throws IOException {
		assertTrue(read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java")
				.contains("spawn_egg_myelin_borer"),
				"the lang file already names this spawn egg, so the item must exist");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*MyelinBorerRegistrationSourceTest"`
Expected: FAIL — "the Myelin Borer class exists but was never registered".

- [ ] **Step 3: Register the entity type and attributes**

In `EntityInit.java`, beside the `chitinite` entry (around line 538), matching its formatting:

```java
    public static final DeferredHolder<EntityType<?>, EntityType<MyelinBorerEntity>> myelin_borer = ENTITY_TYPES.register("myelin_borer",
            () -> EntityType.Builder.of(MyelinBorerEntity::new, MobCategory.MONSTER).sized(0.9F, 0.4F)
                    .build(Hemomancy.rloc("myelin_borer").toString()));
```

Add to `onAttributeCreate` (around line 861), beside the other `event.put(...)` lines:

```java
        event.put(EntityInit.myelin_borer.get(), MyelinBorerEntity.setAttributes().build());
```

Import `com.vincenthuto.hemomancy.common.entity.mob.arthropod.MyelinBorerEntity` if it is not already imported.

- [ ] **Step 4: Register the renderer, layer and spawn egg**

In `ClientEvents.renderEntities` (around line 844), beside `chitinite`:

```java
            event.registerEntityRenderer(EntityInit.myelin_borer.get(), MyelinBorerRenderer::new);
```

In `LayerEvents.registerModelLayers` (around line 64):

```java
		event.registerLayerDefinition(MyelinBorerModel.LAYER_LOCATION, MyelinBorerModel::createBodyLayer);
```

In `ItemInit.java`, beside the other spawn eggs (around line 1090). The colours are the biome's myelin white and ganglion yellow:

```java
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_myelin_borer = SPAWNEGGS.register("spawn_egg_myelin_borer",
            () -> new DeferredSpawnEggItem(EntityInit.myelin_borer, 0xD8D5C4, 0xC9A227, new Item.Properties()));
```

Add imports for `MyelinBorerRenderer` and `MyelinBorerModel` in the two client files.

- [ ] **Step 5: Run the test and the compiler**

Run: `./gradlew.bat test --tests "*MyelinBorerRegistrationSourceTest"`
Expected: PASS, 3 tests.

Run: `./gradlew.bat compileJava`
Expected: BUILD SUCCESSFUL. This is the real gate — a source-contract test passes even on code that does not compile.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java src/test/java/com/vincenthuto/hemomancy/common/entity/MyelinBorerRegistrationSourceTest.java
git commit -m "feat(entity): register the Myelin Borer"
```

---

# Phase 2 — Climbing the network

### Task 2: Pure movement and agitation rules

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerMovementRules.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/entity/MyelinBorerMovementRulesTest.java`

This class is deliberately Minecraft-free (only `java.lang`) so it is genuinely unit testable, exactly as `ChalybeateSnailMovementRules` and `VagrantMindGeometry` are. **Do not import any Minecraft class into it.**

**Interfaces:**
- Produces:
  - `MyelinBorerMovementRules.CLIMB_SPEED` → `double` = 0.12
  - `MyelinBorerMovementRules.CLIMB_STEP_HEIGHT` → `float` = 1.0F
  - `MyelinBorerMovementRules.AGITATION_TICKS` → `int` = 600
  - `climbAdjustedY(double deltaY, boolean gripping)` → `double`
  - `decayAgitation(int agitation)` → `int`
  - `isAgitated(int agitation)` → `boolean`
  - `raiseAgitation(int current)` → `int`

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincenthuto.hemomancy.common.entity.mob.arthropod.MyelinBorerMovementRules;
import org.junit.jupiter.api.Test;

class MyelinBorerMovementRulesTest {
	@Test
	void grippingTurnsFallingIntoClimbing() {
		assertEquals(MyelinBorerMovementRules.CLIMB_SPEED,
				MyelinBorerMovementRules.climbAdjustedY(-0.6D, true), 1.0e-9,
				"a gripping Borer must crawl up the cable instead of falling off it");
	}

	@Test
	void notGrippingFallsNormally() {
		assertEquals(-0.6D, MyelinBorerMovementRules.climbAdjustedY(-0.6D, false), 1.0e-9,
				"off the network the Borer is subject to gravity like anything else");
	}

	@Test
	void climbingDoesNotCancelDeliberateUpwardMotion() {
		assertEquals(0.4D, MyelinBorerMovementRules.climbAdjustedY(0.4D, true), 1.0e-9,
				"it is already moving up, so clamping would slow it down");
	}

	@Test
	void agitationDecaysToCalm() {
		int agitation = MyelinBorerMovementRules.AGITATION_TICKS;
		assertTrue(MyelinBorerMovementRules.isAgitated(agitation));
		for (int i = 0; i < MyelinBorerMovementRules.AGITATION_TICKS; i++) {
			agitation = MyelinBorerMovementRules.decayAgitation(agitation);
		}
		assertEquals(0, agitation, "agitation must reach exactly zero, not go negative");
		assertFalse(MyelinBorerMovementRules.isAgitated(agitation),
				"a calm Borer mends instead of boring");
	}

	@Test
	void agitationNeverGoesNegative() {
		assertEquals(0, MyelinBorerMovementRules.decayAgitation(0));
	}

	@Test
	void provocationResetsTheFullTimer() {
		assertEquals(MyelinBorerMovementRules.AGITATION_TICKS,
				MyelinBorerMovementRules.raiseAgitation(5),
				"a fresh provocation restarts the full 30 seconds rather than stacking on top");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*MyelinBorerMovementRulesTest"`
Expected: FAIL — cannot resolve `MyelinBorerMovementRules`.

- [ ] **Step 3: Implement the rules**

```java
package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

/**
 * Pure climb and mood maths for the Myelin Borer. Free of Minecraft types so it can be unit tested,
 * matching ChalybeateSnailMovementRules.
 */
public final class MyelinBorerMovementRules {
	/** Upward crawl speed while gripping a nerve cable. */
	public static final double CLIMB_SPEED = 0.12D;
	/** Lets a gripping Borer step over bundle knots and anchor blobs. */
	public static final float CLIMB_STEP_HEIGHT = 1.0F;
	/** Thirty seconds at twenty ticks per second. */
	public static final int AGITATION_TICKS = 600;

	private MyelinBorerMovementRules() {
	}

	/** While gripping, downward motion becomes an upward crawl; upward motion is left alone. */
	public static double climbAdjustedY(double deltaY, boolean gripping) {
		if (!gripping) {
			return deltaY;
		}
		return deltaY < 0.0D ? CLIMB_SPEED : deltaY;
	}

	public static int decayAgitation(int agitation) {
		return agitation <= 0 ? 0 : agitation - 1;
	}

	public static boolean isAgitated(int agitation) {
		return agitation > 0;
	}

	/** A fresh provocation restarts the full timer rather than stacking. */
	public static int raiseAgitation(int current) {
		return AGITATION_TICKS;
	}
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "*MyelinBorerMovementRulesTest"`
Expected: PASS, 6 tests.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerMovementRules.java src/test/java/com/vincenthuto/hemomancy/common/entity/MyelinBorerMovementRulesTest.java
git commit -m "feat(entity): add Myelin Borer movement and agitation rules"
```

### Task 3: The crawlable tag and entity climbing

**Files:**
- Create: `src/main/resources/data/hemomancy/tags/block/cortical_crawlable.json`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerEntity.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/entity/MyelinBorerClimbingSourceTest.java`

**Interfaces:**
- Consumes: `MyelinBorerMovementRules` from Task 2.
- Produces on `MyelinBorerEntity`: `public static final TagKey<Block> CRAWLABLE`, `public boolean isGripping()`, `public int getAgitation()`, `public void setAgitation(int)`, `public void provoke()`, `public boolean isAgitated()`.

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MyelinBorerClimbingSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath));
	}

	@Test
	void tagListsTheNetworkBlocks() throws IOException {
		String tag = read("src/main/resources/data/hemomancy/tags/block/cortical_crawlable.json");
		for (String id : new String[] { "hemomancy:nerve_fiber", "hemomancy:nerve_bundle",
				"hemomancy:synaptic_node" }) {
			assertTrue(tag.contains(id), "the Borer rides the whole network, including " + id);
		}
	}

	@Test
	void entityClimbsTheTagAndTracksAgitation() throws IOException {
		String entity = read(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerEntity.java");
		assertTrue(entity.contains("cortical_crawlable"),
				"climbing must be driven by the tag, not a hardcoded block list");
		assertTrue(entity.contains("public boolean onClimbable()"),
				"the Borer grips cables instead of walking");
		assertTrue(entity.contains("MyelinBorerMovementRules.climbAdjustedY"),
				"climb maths belongs in the pure rules class so it stays testable");
		assertTrue(entity.contains("MyelinBorerMovementRules.decayAgitation"),
				"agitation must decay each tick so provoked Borers calm down again");
		assertTrue(entity.contains("defineSynchedData"),
				"agitation must be synced so the client can react to the mood");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*MyelinBorerClimbingSourceTest"`
Expected: FAIL — `NoSuchFileException` for the tag file.

- [ ] **Step 3: Create the tag**

`src/main/resources/data/hemomancy/tags/block/cortical_crawlable.json`:

```json
{
  "replace": false,
  "values": [
    "hemomancy:nerve_fiber",
    "hemomancy:nerve_bundle",
    "hemomancy:synaptic_node"
  ]
}
```

- [ ] **Step 4: Add climbing and agitation to the entity**

Add to `MyelinBorerEntity`. Look at `ChalybeateSnailEntity` first for how this repo declares a climbable tag and overrides movement, and match that style:

```java
	public static final TagKey<Block> CRAWLABLE = BlockTags.create(Hemomancy.rloc("cortical_crawlable"));

	private static final EntityDataAccessor<Integer> AGITATION =
			SynchedEntityData.defineId(MyelinBorerEntity.class, EntityDataSerializers.INT);

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(AGITATION, 0);
	}

	public int getAgitation() {
		return this.entityData.get(AGITATION);
	}

	public void setAgitation(int ticks) {
		this.entityData.set(AGITATION, Math.max(0, ticks));
	}

	/** A fresh provocation restarts the full timer. */
	public void provoke() {
		setAgitation(MyelinBorerMovementRules.raiseAgitation(getAgitation()));
	}

	public boolean isAgitated() {
		return MyelinBorerMovementRules.isAgitated(getAgitation());
	}

	/** True while touching any nerve-network block. */
	public boolean isGripping() {
		BlockPos pos = this.blockPosition();
		if (this.level().getBlockState(pos).is(CRAWLABLE)) {
			return true;
		}
		for (Direction direction : Direction.values()) {
			if (this.level().getBlockState(pos.relative(direction)).is(CRAWLABLE)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onClimbable() {
		return isGripping();
	}

	@Override
	public float maxUpStep() {
		return isGripping() ? MyelinBorerMovementRules.CLIMB_STEP_HEIGHT : super.maxUpStep();
	}

	@Override
	public void travel(Vec3 travelVector) {
		super.travel(travelVector);
		if (this.horizontalCollision && isGripping()) {
			Vec3 delta = this.getDeltaMovement();
			this.setDeltaMovement(delta.x,
					MyelinBorerMovementRules.climbAdjustedY(delta.y, true), delta.z);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide) {
			setAgitation(MyelinBorerMovementRules.decayAgitation(getAgitation()));
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("Agitation", getAgitation());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setAgitation(tag.getInt("Agitation"));
	}
```

Then gate hunting on the mood: change the existing `NearestAttackableTargetGoal` in `registerGoals()` so it only applies while agitated, by passing a target predicate that also requires `this.isAgitated()`. A calm Borer must never attack.

- [ ] **Step 5: Run the test and the compiler**

Run: `./gradlew.bat test --tests "*MyelinBorerClimbingSourceTest"`
Expected: PASS, 2 tests.

Run: `./gradlew.bat compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/data/hemomancy/tags/block/cortical_crawlable.json src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerEntity.java src/test/java/com/vincenthuto/hemomancy/common/entity/MyelinBorerClimbingSourceTest.java
git commit -m "feat(entity): let the Myelin Borer grip and climb the nerve network"
```

---

# Phase 3 — The two moods

### Task 4: MendFiberGoal — closing gaps

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/FiberGapRules.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MendFiberGoal.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerEntity.java` (register the goal)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/entity/FiberGapRulesTest.java`

`FiberGapRules` is Minecraft-free so gap detection is genuinely unit testable; the goal is thin glue over it.

**Interfaces:**
- Produces:
  - `FiberGapRules.WEST/EAST/DOWN/UP/NORTH/SOUTH` → `int` indices 0..5
  - `FiberGapRules.isGap(boolean[] crawlableNeighbours)` → `boolean`
  - `FiberGapRules.gapAxis(boolean[] crawlableNeighbours)` → `int` (0 = X, 1 = Y, 2 = Z, -1 = none)
  - `MendFiberGoal(MyelinBorerEntity borer, double speed, int searchRange)`

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincenthuto.hemomancy.common.entity.mob.arthropod.FiberGapRules;
import org.junit.jupiter.api.Test;

class FiberGapRulesTest {
	/** order: west, east, down, up, north, south */
	private static boolean[] neighbours(boolean west, boolean east, boolean down, boolean up,
			boolean north, boolean south) {
		return new boolean[] { west, east, down, up, north, south };
	}

	@Test
	void airFlankedOnTheXAxisIsAGap() {
		boolean[] n = neighbours(true, true, false, false, false, false);
		assertTrue(FiberGapRules.isGap(n), "cable continues on both sides, so this hole is a severed run");
		assertEquals(0, FiberGapRules.gapAxis(n), "the replacement fiber must run along X");
	}

	@Test
	void airFlankedOnTheZAxisIsAGap() {
		boolean[] n = neighbours(false, false, false, false, true, true);
		assertTrue(FiberGapRules.isGap(n));
		assertEquals(2, FiberGapRules.gapAxis(n));
	}

	@Test
	void aDeadEndIsNotAGap() {
		boolean[] n = neighbours(true, false, false, false, false, false);
		assertFalse(FiberGapRules.isGap(n),
				"cable on one side only means open void beyond; mending here would extrude fiber into nothing");
		assertEquals(-1, FiberGapRules.gapAxis(n));
	}

	@Test
	void emptySpaceIsNotAGap() {
		assertFalse(FiberGapRules.isGap(neighbours(false, false, false, false, false, false)));
	}

	@Test
	void aCornerIsNotAGap() {
		assertFalse(FiberGapRules.isGap(neighbours(true, false, false, false, true, false)),
				"two different axes is a corner, not a severed straight run");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*FiberGapRulesTest"`
Expected: FAIL — cannot resolve `FiberGapRules`.

- [ ] **Step 3: Implement the gap rules**

```java
package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

/**
 * Decides whether an empty position is a severed cable run the Borer should mend. Minecraft-free so it
 * is unit testable; the caller samples the world and passes the six neighbour flags in.
 */
public final class FiberGapRules {
	public static final int WEST = 0;
	public static final int EAST = 1;
	public static final int DOWN = 2;
	public static final int UP = 3;
	public static final int NORTH = 4;
	public static final int SOUTH = 5;

	private FiberGapRules() {
	}

	/** A gap is air with network blocks on exactly one opposing pair, so the run is genuinely severed. */
	public static boolean isGap(boolean[] crawlableNeighbours) {
		return gapAxis(crawlableNeighbours) >= 0;
	}

	/** 0 = X, 1 = Y, 2 = Z, -1 = not a gap. */
	public static int gapAxis(boolean[] n) {
		boolean x = n[WEST] && n[EAST];
		boolean y = n[DOWN] && n[UP];
		boolean z = n[NORTH] && n[SOUTH];
		int pairs = (x ? 1 : 0) + (y ? 1 : 0) + (z ? 1 : 0);
		if (pairs != 1) {
			return -1;
		}
		return x ? 0 : (y ? 1 : 2);
	}
}
```

- [ ] **Step 4: Write the mending goal**

Create `MendFiberGoal extends MoveToBlockGoal`. Read `ChewWoodGoal` in the same package first — it is the established shape for "walk to a block, work on it for N ticks, change it" — and mirror its structure, including how it resets `nextStartTick`.

Required behaviour:

- `canUse()`: `return !this.borer.isAgitated() && super.canUse();` — a calm Borer mends.
- A private helper that fills the neighbour flags from the world, in the exact index order `FiberGapRules` expects:

```java
	private boolean[] neighbourFlags(LevelReader level, BlockPos pos) {
		return new boolean[] {
				level.getBlockState(pos.west()).is(MyelinBorerEntity.CRAWLABLE),
				level.getBlockState(pos.east()).is(MyelinBorerEntity.CRAWLABLE),
				level.getBlockState(pos.below()).is(MyelinBorerEntity.CRAWLABLE),
				level.getBlockState(pos.above()).is(MyelinBorerEntity.CRAWLABLE),
				level.getBlockState(pos.north()).is(MyelinBorerEntity.CRAWLABLE),
				level.getBlockState(pos.south()).is(MyelinBorerEntity.CRAWLABLE) };
	}
```

- `isValidTarget(LevelReader level, BlockPos pos)`: `return level.getBlockState(pos).isAir() && FiberGapRules.isGap(neighbourFlags(level, pos));`
- `tick()`: call `super.tick()`; while `isReachedTarget()` increment a `mendProgress` field, and at **40** ticks place the fiber and reset the counter:

```java
			Direction.Axis axis = switch (FiberGapRules.gapAxis(neighbourFlags(level, this.blockPos))) {
				case 0 -> Direction.Axis.X;
				case 1 -> Direction.Axis.Y;
				default -> Direction.Axis.Z;
			};
			level.setBlock(this.blockPos, BlockInit.nerve_fiber.get().defaultBlockState()
					.setValue(RotatedPillarBlock.AXIS, axis), 3);
			this.mendProgress = 0;
```

Register it in `MyelinBorerEntity.registerGoals()` at priority 3:

```java
		this.goalSelector.addGoal(3, new MendFiberGoal(this, 1.0D, 12));
```

- [ ] **Step 5: Run the test and the compiler**

Run: `./gradlew.bat test --tests "*FiberGapRulesTest"`
Expected: PASS, 5 tests.

Run: `./gradlew.bat compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/FiberGapRules.java src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MendFiberGoal.java src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerEntity.java src/test/java/com/vincenthuto/hemomancy/common/entity/FiberGapRulesTest.java
git commit -m "feat(entity): let calm Myelin Borers mend severed nerve cables"
```

### Task 5: BoreFiberGoal and provocation

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/BoreFiberGoal.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerAgitationEvents.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerEntity.java` (register the goal, provoke on hurt)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/entity/MyelinBorerBoringSourceTest.java`

**Interfaces:**
- Consumes: `MyelinBorerEntity.CRAWLABLE`, `provoke()`, `isAgitated()` from Task 3.
- Produces: `BoreFiberGoal(MyelinBorerEntity borer, double speed, int searchRange)`.

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MyelinBorerBoringSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath));
	}

	@Test
	void boringNeverEatsSynapticNodes() throws IOException {
		String goal = read(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/BoreFiberGoal.java");
		assertTrue(goal.contains("synaptic_node"),
				"the goal must explicitly exclude synaptic_node so hubs and anchors survive");
		assertTrue(goal.contains("nerve_fiber") && goal.contains("nerve_bundle"),
				"only cable segments are edible");
		assertTrue(goal.contains("isAgitated"), "only provoked Borers bore");
	}

	@Test
	void boringOnlyTargetsConnectedCable() throws IOException {
		assertTrue(read("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/BoreFiberGoal.java")
				.contains("CRAWLABLE"),
				"a target must have network neighbours, so Borers sever live runs rather than stray blocks");
	}

	@Test
	void breakingTheNetworkProvokesNearbyBorers() throws IOException {
		String events = read(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerAgitationEvents.java");
		assertTrue(events.contains("BlockEvent.BreakEvent"),
				"breaking a cable is the player's provocation");
		assertTrue(events.contains("CRAWLABLE"),
				"only network blocks provoke; ordinary blocks must not");
		assertTrue(events.contains("provoke()"), "nearby Borers must actually be provoked");
	}

	@Test
	void beingHurtProvokes() throws IOException {
		String entity = read(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerEntity.java");
		int hurt = entity.indexOf("public boolean hurt(");
		assertTrue(hurt > 0, "the Borer must override hurt so striking one angers it");
		assertTrue(entity.substring(hurt, Math.min(hurt + 400, entity.length())).contains("provoke()"),
				"a struck Borer must turn on the player rather than keep mending");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*MyelinBorerBoringSourceTest"`
Expected: FAIL — `NoSuchFileException` for `BoreFiberGoal.java`.

- [ ] **Step 3: Write the boring goal**

Create `BoreFiberGoal extends MoveToBlockGoal`, mirroring `MendFiberGoal` from Task 4:

- `canUse()`: `return this.borer.isAgitated() && super.canUse();`
- `isValidTarget(LevelReader level, BlockPos pos)`: the block must be `BlockInit.nerve_fiber.get()` or `BlockInit.nerve_bundle.get()` — **never** `BlockInit.synaptic_node.get()` — and at least one of the six neighbours must be in `MyelinBorerEntity.CRAWLABLE`, so it only severs live runs:

```java
	@Override
	protected boolean isValidTarget(LevelReader level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!state.is(BlockInit.nerve_fiber.get()) && !state.is(BlockInit.nerve_bundle.get())) {
			return false;
		}
		for (Direction direction : Direction.values()) {
			if (level.getBlockState(pos.relative(direction)).is(MyelinBorerEntity.CRAWLABLE)) {
				return true;
			}
		}
		return false;
	}
```

- `tick()`: call `super.tick()`; while `isReachedTarget()` increment a `boreProgress` field, and at **60** ticks:

```java
			level.destroyBlock(this.blockPos, false);
			this.boreProgress = 0;
```

Register it at priority 2, above mending, in `registerGoals()`:

```java
		this.goalSelector.addGoal(2, new BoreFiberGoal(this, 1.0D, 12));
```

Also override `hurt` on the entity so a struck Borer turns:

```java
	@Override
	public boolean hurt(DamageSource source, float amount) {
		boolean hurt = super.hurt(source, amount);
		if (hurt && !this.level().isClientSide) {
			provoke();
		}
		return hurt;
	}
```

- [ ] **Step 4: Write the provocation handler**

```java
package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Cutting the nerve network rouses the Borers that maintain it. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MyelinBorerAgitationEvents {
	private static final double PROVOKE_RADIUS = 16.0D;

	private MyelinBorerAgitationEvents() {
	}

	@SubscribeEvent
	public static void onBreak(BlockEvent.BreakEvent event) {
		if (!(event.getLevel() instanceof ServerLevel level)
				|| !event.getState().is(MyelinBorerEntity.CRAWLABLE)) {
			return;
		}
		AABB area = new AABB(event.getPos()).inflate(PROVOKE_RADIUS);
		for (MyelinBorerEntity borer : level.getEntitiesOfClass(MyelinBorerEntity.class, area)) {
			borer.provoke();
		}
	}
}
```

- [ ] **Step 5: Run the test and the compiler**

Run: `./gradlew.bat test --tests "*MyelinBorerBoringSourceTest"`
Expected: PASS, 4 tests.

Run: `./gradlew.bat compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/BoreFiberGoal.java src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerAgitationEvents.java src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerEntity.java src/test/java/com/vincenthuto/hemomancy/common/entity/MyelinBorerBoringSourceTest.java
git commit -m "feat(entity): provoked Myelin Borers sever the nerve network"
```

---

# Phase 4 — Payload, spawning, tests and docs

### Task 6: Ganglion Cluster drop and the synaptic node craft

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java`
- Create: `src/main/resources/data/hemomancy/loot_table/entities/myelin_borer.json`
- Create: `src/main/resources/data/hemomancy/recipe/ganglion_cluster_to_synaptic_node.json`
- Create: `src/main/resources/assets/hemomancy/textures/item/ganglion_cluster.png`
- Modify: `src/generated/resources/assets/hemomancy/lang/en_us.json` (through `runData`)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/entity/MyelinBorerLootSourceTest.java`

**Interfaces:**
- Produces: `ItemInit.ganglion_cluster` (`DeferredHolder<Item, Item>`).

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MyelinBorerLootSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void itemIsRegistered() throws IOException {
		assertTrue(Files.readString(
				ROOT.resolve("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java"))
				.contains("ganglion_cluster"), "the Borer's drop must exist as an item");
	}

	@Test
	void lootTableDropsTheCluster() throws IOException {
		Path loot = ROOT.resolve("src/main/resources/data/hemomancy/loot_table/entities/myelin_borer.json");
		assertTrue(Files.exists(loot), "without a loot table the Borer drops nothing at all");
		JsonObject json;
		try (var reader = Files.newBufferedReader(loot)) {
			json = JsonParser.parseReader(reader).getAsJsonObject();
		}
		assertTrue(json.toString().contains("hemomancy:ganglion_cluster"), "it must drop the cluster");
		assertTrue("minecraft:entity".equals(json.get("type").getAsString()),
				"it is an entity loot table");
	}

	@Test
	void clustersCraftIntoSynapticNodes() throws IOException {
		Path recipe = ROOT.resolve(
				"src/main/resources/data/hemomancy/recipe/ganglion_cluster_to_synaptic_node.json");
		assertTrue(Files.exists(recipe),
				"the payoff is letting players extend the conduction network themselves");
		String json = Files.readString(recipe);
		assertTrue(json.contains("hemomancy:ganglion_cluster") && json.contains("hemomancy:synaptic_node"),
				"the recipe must turn clusters into placeable nodes");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*MyelinBorerLootSourceTest"`
Expected: FAIL — "the Borer's drop must exist as an item".

- [ ] **Step 3: Register the item**

In `ItemInit.java`, beside the other `BASEITEMS` entries:

```java
    public static final DeferredHolder<Item, Item> ganglion_cluster = BASEITEMS.register("ganglion_cluster",
            () -> new Item(new Item.Properties()));
```

- [ ] **Step 4: Add the loot table and the recipe**

`src/main/resources/data/hemomancy/loot_table/entities/myelin_borer.json`, following `abhorent_thought.json`'s shape:

```json
{
  "type": "minecraft:entity",
  "pools": [
    {
      "rolls": 1,
      "bonus_rolls": 0,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "hemomancy:ganglion_cluster",
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": { "type": "minecraft:uniform", "min": 1.0, "max": 2.0 },
              "add": false
            }
          ]
        }
      ]
    }
  ]
}
```

`src/main/resources/data/hemomancy/recipe/ganglion_cluster_to_synaptic_node.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " g ",
    "gmg",
    " g "
  ],
  "key": {
    "g": { "item": "hemomancy:ganglion_cluster" },
    "m": { "item": "hemomancy:myelin_sheath" }
  },
  "result": {
    "id": "hemomancy:synaptic_node",
    "count": 1
  }
}
```

- [ ] **Step 5: Author the texture and run datagen**

Create a 16×16 `ganglion_cluster.png`: a knot of yellow `#C9A227` strands over dark tissue, matching the `ganglion_matter` block so the drop reads as a piece of it.

Run: `./gradlew.bat runData`
Expected: an item model and an `item.hemomancy.ganglion_cluster` lang entry appear under `src/generated/resources`. Fix the lang entry to read `Ganglion Cluster` if the generated casing is wrong.

- [ ] **Step 6: Run the tests and the datapack validation**

Run: `./gradlew.bat test --tests "*MyelinBorerLootSourceTest"`
Expected: PASS, 3 tests.

Run: `./gradlew.bat build -x test`
Expected: BUILD SUCCESSFUL. This validates the recipe and loot JSON.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java src/main/resources/data/hemomancy/loot_table/entities/myelin_borer.json src/main/resources/data/hemomancy/recipe/ganglion_cluster_to_synaptic_node.json src/main/resources/assets/hemomancy/textures/item/ganglion_cluster.png src/generated/resources/assets/hemomancy src/test/java/com/vincenthuto/hemomancy/common/entity/MyelinBorerLootSourceTest.java
git commit -m "feat(entity): add Ganglion Cluster drop and synaptic node craft"
```

### Task 7: Spawning in the Cortical Drift

**Files:**
- Modify: `src/main/resources/data/hemomancy/worldgen/biome/cortical_drift.json`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/BiomeInit.java` (the dead `corticalDrift` builder, for consistency)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerEntity.java` (`canSpawnHere`)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/entity/MyelinBorerSpawnSourceTest.java`

**Interfaces:**
- Consumes: `MyelinBorerEntity.CRAWLABLE` from Task 3, `EntityInit.myelin_borer` from Task 1.

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

class MyelinBorerSpawnSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void borersSpawnInTheDrift() throws IOException {
		Path biome = ROOT.resolve("src/main/resources/data/hemomancy/worldgen/biome/cortical_drift.json");
		JsonObject json;
		try (var reader = Files.newBufferedReader(biome)) {
			json = JsonParser.parseReader(reader).getAsJsonObject();
		}
		var monsters = json.getAsJsonObject("spawners").getAsJsonArray("monster");
		assertTrue(StreamSupport.stream(monsters.spliterator(), false)
				.anyMatch(e -> "hemomancy:myelin_borer"
						.equals(e.getAsJsonObject().get("type").getAsString())),
				"bootstrapBiomes is dead code in this repo, so the JSON is what actually spawns the Borer");
	}

	@Test
	void spawnsOnlyNearTheNetwork() throws IOException {
		String entity = Files.readString(ROOT.resolve(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerEntity.java"));
		int hook = entity.indexOf("canSpawnHere");
		assertTrue(hook > 0, "the spawn hook must exist");
		assertTrue(entity.substring(hook).contains("CRAWLABLE"),
				"Borers ride the bridges; spawning them adrift in the void would drop them into nothing");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*MyelinBorerSpawnSourceTest"`
Expected: FAIL — no `hemomancy:myelin_borer` entry in the biome.

- [ ] **Step 3: Add the spawn entry**

In `src/main/resources/data/hemomancy/worldgen/biome/cortical_drift.json`, add to `spawners.monster` beside the Abhorent Thought entry (which is weight 12, so Borers are the less common of the two):

```json
        {
          "type": "hemomancy:myelin_borer",
          "weight": 8,
          "minCount": 1,
          "maxCount": 2
        }
```

Mirror it in `BiomeInit.corticalDrift(...)` for consistency, even though that builder never runs:

```java
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.myelin_borer.get(), 8, 1, 2));
```

- [ ] **Step 4: Gate the spawn on the network**

Replace the body of the existing `canSpawnHere` in `MyelinBorerEntity` so a Borer only spawns with cable nearby:

```java
	public static boolean canSpawnHere(EntityType<? extends Monster> type, ServerLevelAccessor level,
			MobSpawnType reason, BlockPos pos, RandomSource random) {
		for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-4, -4, -4), pos.offset(4, 4, 4))) {
			if (level.getBlockState(nearby).is(CRAWLABLE)) {
				return true;
			}
		}
		return false;
	}
```

- [ ] **Step 5: Run the test and the datapack validation**

Run: `./gradlew.bat test --tests "*MyelinBorerSpawnSourceTest"`
Expected: PASS, 2 tests.

Run: `./gradlew.bat build -x test`
Expected: BUILD SUCCESSFUL. This validates the edited biome JSON.

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/data/hemomancy/worldgen/biome/cortical_drift.json src/main/java/com/vincenthuto/hemomancy/common/init/BiomeInit.java src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerEntity.java src/test/java/com/vincenthuto/hemomancy/common/entity/MyelinBorerSpawnSourceTest.java
git commit -m "feat(worldgen): spawn Myelin Borers on the Cortical Drift network"
```

### Task 8: GameTests for the two moods

**Files:**
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/MyelinBorerGameTests.java`

These prove what a JVM test cannot: real blocks, a real entity, real goal ticking.

- [ ] **Step 1: Write the GameTests**

Read `CorticalDriftGameTests` in the same package first and follow its conventions: `@GameTestHolder(Hemomancy.MOD_ID)`, `@PrefixGameTestTemplate(false)`, and `EMPTY_TEMPLATE = "bastion/mobs/empty"`. Write three tests, each with a generous `timeoutTicks` (the work takes 40–60 ticks plus pathing):

1. **`calmBorerMendsAGap`** — lay a row of `nerve_fiber` along X with a single air gap in the middle, spawn a `MyelinBorerEntity` within a block or two of it, call `setAgitation(0)`, then `h.succeedWhen(() -> h.assertBlockPresent(BlockInit.nerve_fiber.get(), gapPos))`.
2. **`agitatedBorerSeversCable`** — the same row with no gap, spawn a Borer, call `provoke()`, then succeed once any position in the row has become air.
3. **`borersNeverEatSynapticNodes`** — place a `synaptic_node` flanked by `nerve_fiber`, spawn a provoked Borer beside it, and let it run for a fixed duration, then assert the node survived. Use the delayed-assert form rather than `succeedWhen`, because here the test passes by something NOT happening:

```java
		h.runAfterDelay(200L, () -> {
			h.assertBlockPresent(BlockInit.synaptic_node.get(), nodePos);
			h.succeed();
		});
```

This guards the rule that keeps the network recoverable: hubs and anchors must survive so cut cables can always be rebuilt.

- [ ] **Step 2: Run them**

Run: `./gradlew.bat runGameTestServer`
Expected: the three Myelin Borer tests pass. The ~21 pre-existing journey/economy/bestiary failures are unrelated — do not touch them.

If `calmBorerMendsAGap` times out, the Borer probably cannot path to the block: `MoveToBlockGoal` needs a reachable target, so keep the row within a couple of blocks of the spawn position rather than across the test region.

- [ ] **Step 3: Commit**

```bash
git add src/gameTest/java/com/vincenthuto/hemomancy/gametest/MyelinBorerGameTests.java
git commit -m "test(entity): add Myelin Borer mend and bore game tests"
```

### Task 9: Documentation

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md`
- Modify: `docs/LORE_REFERENCE.md`
- Modify: `wiki/Cortical-Drift.md`

- [ ] **Step 1: Update the mechanics reference**

Add a Myelin Borer subsection to the Cortical Drift section covering: the two moods and what flips them (any provocation sets 30 seconds), the `cortical_crawlable` tag, the 40-tick mend and 60-tick bore, the rule that `synaptic_node` is never eaten, the `ganglion_cluster` drop and its craft into `synaptic_node`, and the spawn weight of 8 (1–2 per pack). Match the surrounding sections' depth and formatting.

- [ ] **Step 2: Update the lore reference**

Add the Borer per spec §7: the network's own maintenance tissue, something the Vagrant Mind grew or that grew on it, still working long after the Mind stopped. Give the Harbinger reading (proof the organ still has reflexes) and the Lethean reading (cutting the cables wakes what had finally gone quiet). Read the neighbouring entries first and match their voice. No deity framing.

- [ ] **Step 3: Update the wiki page**

Add a player-facing section to `wiki/Cortical-Drift.md`: what the Borer is, that it is harmless until you damage the network, what it drops, and that clusters craft into synaptic nodes so players can extend the conduction network themselves.

- [ ] **Step 4: Verify nothing regressed**

Run: `./gradlew.bat test`
Expected: every Myelin Borer and Cortical Drift test passes; only the known pre-existing failures remain.

- [ ] **Step 5: Commit**

```bash
git add docs/HEMOMANCY_REFERENCE.md docs/LORE_REFERENCE.md wiki/Cortical-Drift.md
git commit -m "docs: document the Myelin Borer"
```

---

## Out of scope

- A second, opposed species — one mob with two moods was chosen deliberately.
- Any change to `VagrantMindPlacementRules` — a separate open decision.
- Breeding, taming, or a passive mender-only variant.
