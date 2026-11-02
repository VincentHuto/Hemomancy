# Dynasty Castle Jigsaw Structure Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the single-layout `DynastyCastleFeature` with a custom jigsaw-style structure that assembles programmatically generated `.nbt` pieces into varied castles.

**Architecture:** Generate plain `.nbt` piece templates with a `DataProvider` (CircusPavilion pattern), assemble them in code as `PoolElementStructurePiece`s at authored offsets (Vigil pattern), and register a custom `Structure` + `StructureType` with `structure`/`structure_set`/biome-tag JSONs (Mausoleum pattern).

**Tech Stack:** NeoForge 1.21.1, Java 21, Minecraft worldgen structure API, JUnit 5, Gradle (`runData` for datagen, `test` for unit tests).

## Global Constraints

- Target: NeoForge 1.21.1. Mod id: `hemomancy`. Use `Hemomancy.rloc("...")` for ResourceLocations.
- Structure NBT `DataVersion` = `SharedConstants.getCurrentVersion().getDataVersion().getVersion()` (never hardcode).
- Pieces contain NO jigsaw connector blocks; assembly is code-driven.
- Block registry ids used by geometry: `hemomancy:hemorrhagic_crust`, `hemomancy:hyphae_block`, `hemomancy:conscious_mass`, `hemomancy:sanguine_glass`, `hemomancy:calcified_hyphae`, `minecraft:polished_blackstone`, `minecraft:polished_blackstone_bricks`, `minecraft:air`.
- Entity ids: `hemomancy:fargone` (variant int in tag key `"FargoneVariant"` = `FargoneVariant.id()`), `hemomancy:thirster`.
- Piece NBTs live at `src/main/resources/data/hemomancy/structure/dynasty_castle/<name>.nbt`; assembly references them as `hemomancy:dynasty_castle/<name>`.
- `docs/` is gitignored in this repo; do not attempt to `git add` files under `docs/`. Commit only source/resource files.

---

## File Structure

- Create `src/main/java/com/vincenthuto/hemomancy/common/data/gen/DynastyCastlePieces.java` — geometry: produces `List<BlockPlacement>` + `List<EntityPlacement>` per named piece. One responsibility: block/entity layout of each piece.
- Create `src/main/java/com/vincenthuto/hemomancy/common/data/gen/DynastyCastlePieceProvider.java` — `DataProvider` writing each piece to `.nbt`. One responsibility: NBT serialization.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/data/gen/DataGeneration.java` — register the new provider.
- Create `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/DynastyCastleLayouts.java` — assembly graph (keep + slots + weighted candidates).
- Create `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/DynastyCastleStructure.java` — custom `Structure` with CODEC + `findGenerationPoint`.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/init/StructureInit.java` — register the `StructureType`.
- Create `src/main/resources/data/hemomancy/worldgen/structure/dynasty_castle.json`.
- Create `src/main/resources/data/hemomancy/worldgen/structure_set/dynasty_castle.json`.
- Create `src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/dynasty_castle.json`.
- Delete `src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/DynastyCastleFeature.java`.
- Delete `src/main/resources/data/hemomancy/worldgen/configured_feature/dynasty_castle.json`, `.../placed_feature/dynasty_castle.json`, `.../neoforge/biome_modifier/add_dynasty_castle.json`, `.../tags/worldgen/biome/dynasty_castle_spawnlist.json`.
- Modify `BaseFeatureInit.java`, `ConfiguredFeatureInit.java`, `PlacedFeatureInit.java`, `BiomeInit.java` — remove `DYNASTY_CASTLE` feature wiring.
- Replace `src/test/java/com/vincenthuto/hemomancy/common/worldgen/DynastyCastleWorldgenSourceTest.java` — assert new structure wiring instead of the feature.

---

## Task 1: Piece geometry class

Port the existing procedural geometry into per-piece block lists. Reuse the math in
`DynastyCastleFeature.java` (methods `buildFoundation`, `buildCurtainWalls`, `buildKeep`,
`buildTowers`, `buildGatehouse`, `buildSpire`, `buildGothicWindow`, `buildButtresses`,
`buildRoof`, `wallMaterial`) but emit into a `Map<Position,String blockId>` keyed on local
coordinates (origin at the piece's min corner, no negative coords) instead of calling
`level.setBlock`. Bake household entities per piece.

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/data/gen/DynastyCastlePieces.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/worldgen/DynastyCastlePiecesTest.java`

**Interfaces:**
- Produces:
  - `record BlockPlacement(int x, int y, int z, String name)`
  - `record EntityPlacement(double x, double y, double z, String entityId, int fargoneVariant)` (fargoneVariant = -1 for non-Fargone)
  - `static List<String> pieceNames()` → the piece ids: `keep_grand`, `keep_ruined`, `keep_squat`, `tower_tall`, `tower_short`, `tower_broken`, `wall_gothic_window`, `wall_straight`, `wall_breach`, `gate_intact`, `gate_ruined`.
  - `static int[] size(String piece)` → `{width,height,depth}`
  - `static List<BlockPlacement> blocks(String piece)`
  - `static List<EntityPlacement> entities(String piece)`

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincenthuto.hemomancy.common.data.gen.DynastyCastlePieces;
import org.junit.jupiter.api.Test;

class DynastyCastlePiecesTest {
	@Test
	void everyPieceHasBlocksWithinItsDeclaredSize() {
		assertEquals(11, DynastyCastlePieces.pieceNames().size());
		for (String piece : DynastyCastlePieces.pieceNames()) {
			int[] size = DynastyCastlePieces.size(piece);
			var blocks = DynastyCastlePieces.blocks(piece);
			assertFalse(blocks.isEmpty(), piece + " has no blocks");
			for (var b : blocks) {
				assertTrue(b.x() >= 0 && b.x() < size[0], piece + " x out of range");
				assertTrue(b.y() >= 0 && b.y() < size[1], piece + " y out of range");
				assertTrue(b.z() >= 0 && b.z() < size[2], piece + " z out of range");
			}
		}
	}

	@Test
	void keepGrandBakesFargoneHousehold() {
		var entities = DynastyCastlePieces.entities("keep_grand");
		assertFalse(entities.isEmpty());
		assertTrue(entities.stream().anyMatch(e -> e.entityId().equals("hemomancy:fargone")));
	}
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.vincenthuto.hemomancy.common.worldgen.DynastyCastlePiecesTest"`
Expected: FAIL — `DynastyCastlePieces` does not exist (compile error).

- [ ] **Step 3: Write the geometry class**

Create `DynastyCastlePieces.java`. Skeleton + one fully-ported piece shown; port the remaining
pieces from the named `DynastyCastleFeature` methods using the same pattern (translate world
`base.offset(x,y,z)` writes into `place(map, x+ox, y, z+oz, id)` with an origin offset `ox/oz`
chosen so all coordinates are ≥ 0). Map the `BlockState` locals to ids: `crust`→
`hemomancy:hemorrhagic_crust`, `hyphae`→`hemomancy:hyphae_block`, `mass`→
`hemomancy:conscious_mass`, `glass`→`hemomancy:sanguine_glass`, `calcified`→
`hemomancy:calcified_hyphae`, `darkStone`→`minecraft:polished_blackstone`, `darkBrick`→
`minecraft:polished_blackstone_bricks`.

```java
package com.vincenthuto.hemomancy.common.data.gen;

import com.vincenthuto.hemomancy.common.entity.mob.arthropod.FargoneVariant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DynastyCastlePieces {
	public record BlockPlacement(int x, int y, int z, String name) {}
	public record EntityPlacement(double x, double y, double z, String entityId, int fargoneVariant) {}
	private record Position(int x, int y, int z) {}

	private static final String CRUST = "hemomancy:hemorrhagic_crust";
	private static final String HYPHAE = "hemomancy:hyphae_block";
	private static final String MASS = "hemomancy:conscious_mass";
	private static final String GLASS = "hemomancy:sanguine_glass";
	private static final String CALCIFIED = "hemomancy:calcified_hyphae";
	private static final String DARK_STONE = "minecraft:polished_blackstone";
	private static final String DARK_BRICK = "minecraft:polished_blackstone_bricks";

	private DynastyCastlePieces() {}

	public static List<String> pieceNames() {
		return List.of("keep_grand", "keep_ruined", "keep_squat",
				"tower_tall", "tower_short", "tower_broken",
				"wall_gothic_window", "wall_straight", "wall_breach",
				"gate_intact", "gate_ruined");
	}

	public static int[] size(String piece) {
		return switch (piece) {
			case "keep_grand" -> new int[] {15, 42, 13};
			case "keep_ruined" -> new int[] {15, 26, 13};
			case "keep_squat" -> new int[] {11, 18, 9};
			case "tower_tall" -> new int[] {7, 32, 7};
			case "tower_short" -> new int[] {7, 20, 7};
			case "tower_broken" -> new int[] {7, 14, 7};
			case "wall_gothic_window", "wall_straight", "wall_breach" -> new int[] {12, 11, 3};
			case "gate_intact", "gate_ruined" -> new int[] {13, 12, 5};
			default -> throw new IllegalArgumentException("Unknown piece " + piece);
		};
	}

	public static List<BlockPlacement> blocks(String piece) {
		Map<Position, BlockPlacement> out = new LinkedHashMap<>();
		switch (piece) {
			case "tower_tall" -> tower(out, 32, 3, false);
			case "tower_short" -> tower(out, 20, 3, false);
			case "tower_broken" -> tower(out, 14, 3, true);
			// PORT: keep_* from buildKeep/buildRoof, wall_* from buildCurtainWalls section +
			// buildGothicWindow, gate_* from buildGatehouse. Use place(out, x, y, z, id).
			default -> throw new IllegalArgumentException("Unimplemented piece " + piece);
		}
		return List.copyOf(out.values());
	}

	// Example fully-ported piece: a square corner tower centered in a (2*radius+1) footprint.
	private static void tower(Map<Position, BlockPlacement> out, int height, int radius, boolean broken) {
		int c = radius; // local center so min coord is 0
		for (int y = 1; y <= height; y++) {
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					boolean edge = Math.abs(x) == radius || Math.abs(z) == radius;
					if (!edge && y > 1 && y < height) continue;
					if (broken && y > height - 5 && Math.floorMod(x * 7 + z * 5 + y, 5) == 0) continue;
					place(out, c + x, y, c + z, y == height ? CALCIFIED : wallMaterial(x + z + y));
				}
			}
		}
		// spire cap (ported from buildSpire)
		spire(out, c, c, height + 1, radius + 1, broken ? 4 : 9);
	}

	private static void spire(Map<Position, BlockPlacement> out, int cx, int cz, int startY, int radius, int height) {
		for (int layer = 0; layer < height; layer++) {
			int r = Math.max(0, radius - (layer * radius / Math.max(1, height - 1)));
			for (int x = -r; x <= r; x++) {
				for (int z = -r; z <= r; z++) {
					boolean spireEdge = Math.abs(x) == r || Math.abs(z) == r;
					place(out, cx + x, startY + layer, cz + z,
							spireEdge || r == 0 ? (layer % 3 == 0 ? CALCIFIED : DARK_BRICK) : CRUST);
				}
			}
		}
	}

	private static String wallMaterial(int value) {
		return Math.floorMod(value, 5) == 0 ? HYPHAE : (Math.floorMod(value, 7) == 0 ? DARK_BRICK : CRUST);
	}

	public static List<EntityPlacement> entities(String piece) {
		List<EntityPlacement> out = new ArrayList<>();
		switch (piece) {
			case "keep_grand" -> {
				addFargone(out, 7, 1, 6, FargoneVariant.ELDER);
				addFargone(out, 5, 1, 5, FargoneVariant.GUARD);
				addFargone(out, 9, 1, 7, FargoneVariant.ATTENDANT);
			}
			case "keep_ruined" -> addFargone(out, 7, 1, 6, FargoneVariant.RECLAIMED);
			case "tower_tall", "tower_short" -> addFargone(out, 3, 1, 3, FargoneVariant.SCOUT);
			case "tower_broken" -> out.add(new EntityPlacement(3.5, 1, 3.5, "hemomancy:thirster", -1));
			default -> { /* walls and gates carry no household */ }
		}
		return out;
	}

	private static void addFargone(List<EntityPlacement> out, int x, int y, int z, FargoneVariant variant) {
		out.add(new EntityPlacement(x + 0.5, y, z + 0.5, "hemomancy:fargone", variant.id()));
	}

	private static void place(Map<Position, BlockPlacement> out, int x, int y, int z, String id) {
		out.put(new Position(x, y, z), new BlockPlacement(x, y, z, id));
	}
}
```

Note: adjust each `size(...)` entry to bound its ported geometry (verify no coordinate is negative
or ≥ size; the test enforces this). Confirm `FargoneVariant.id()` returns the int used in the
`"FargoneVariant"` tag (see `FargoneEntity.addAdditionalSaveData`).

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew test --tests "com.vincenthuto.hemomancy.common.worldgen.DynastyCastlePiecesTest"`
Expected: PASS. If a coordinate-range assertion fails, widen that piece's `size(...)` or fix its
origin offset so all local coords are within `[0,size)`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/data/gen/DynastyCastlePieces.java src/test/java/com/vincenthuto/hemomancy/common/worldgen/DynastyCastlePiecesTest.java
git commit -m "feat: add dynasty castle piece geometry"
```

---

## Task 2: NBT piece provider + datagen wiring

Serialize each piece to a `.nbt` structure template, following `CircusPavilionStructureProvider`.

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/data/gen/DynastyCastlePieceProvider.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/data/gen/DataGeneration.java:24`

**Interfaces:**
- Consumes: `DynastyCastlePieces.pieceNames/size/blocks/entities` (Task 1).
- Produces: `.nbt` files under `data/hemomancy/structure/dynasty_castle/`. No Java symbols consumed by later tasks.

- [ ] **Step 1: Write the provider**

```java
package com.vincenthuto.hemomancy.common.data.gen;

import com.google.common.hash.Hashing;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.SharedConstants;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

final class DynastyCastlePieceProvider implements DataProvider {
	private final PackOutput.PathProvider path;

	DynastyCastlePieceProvider(PackOutput packOutput) {
		this.path = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "structure");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		return CompletableFuture.allOf(DynastyCastlePieces.pieceNames().stream()
				.map(piece -> writePiece(cache, piece))
				.toArray(CompletableFuture[]::new));
	}

	private CompletableFuture<?> writePiece(CachedOutput cache, String piece) {
		return CompletableFuture.runAsync(() -> {
			Path output = path.file(Hemomancy.rloc("dynasty_castle/" + piece), "nbt");
			try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
				NbtIo.writeCompressed(template(piece), bytes);
				byte[] data = bytes.toByteArray();
				cache.writeIfNeeded(output, data, Hashing.sha1().hashBytes(data));
			} catch (IOException exception) {
				throw new UncheckedIOException(exception);
			}
		});
	}

	private static CompoundTag template(String piece) {
		CompoundTag root = new CompoundTag();
		int[] size = DynastyCastlePieces.size(piece);
		root.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
		root.put("size", vector(size[0], size[1], size[2]));

		Map<String, Integer> stateIds = new LinkedHashMap<>();
		List<DynastyCastlePieces.BlockPlacement> placements = DynastyCastlePieces.blocks(piece);
		for (var b : placements) stateIds.computeIfAbsent(b.name(), ignored -> stateIds.size());

		ListTag palette = new ListTag();
		stateIds.keySet().forEach(name -> {
			CompoundTag state = new CompoundTag();
			state.putString("Name", name);
			palette.add(state);
		});
		root.put("palette", palette);

		ListTag blocks = new ListTag();
		for (var b : placements) {
			CompoundTag block = new CompoundTag();
			block.put("pos", vector(b.x(), b.y(), b.z()));
			block.putInt("state", stateIds.get(b.name()));
			blocks.add(block);
		}
		root.put("blocks", blocks);

		ListTag entities = new ListTag();
		for (var e : DynastyCastlePieces.entities(piece)) entities.add(entity(e));
		root.put("entities", entities);
		return root;
	}

	private static CompoundTag entity(DynastyCastlePieces.EntityPlacement placement) {
		CompoundTag entity = new CompoundTag();
		entity.put("pos", vector(placement.x(), placement.y(), placement.z()));
		entity.put("blockPos", vector((int) placement.x(), (int) placement.y(), (int) placement.z()));
		CompoundTag data = new CompoundTag();
		data.putString("id", placement.entityId());
		data.putBoolean("PersistenceRequired", true);
		if (placement.fargoneVariant() >= 0) data.putInt("FargoneVariant", placement.fargoneVariant());
		entity.put("nbt", data);
		return entity;
	}

	private static ListTag vector(int x, int y, int z) {
		ListTag v = new ListTag();
		v.add(IntTag.valueOf(x)); v.add(IntTag.valueOf(y)); v.add(IntTag.valueOf(z));
		return v;
	}

	private static ListTag vector(double x, double y, double z) {
		ListTag v = new ListTag();
		v.add(DoubleTag.valueOf(x)); v.add(DoubleTag.valueOf(y)); v.add(DoubleTag.valueOf(z));
		return v;
	}

	@Override
	public String getName() {
		return "Dynasty castle pieces";
	}
}
```

- [ ] **Step 2: Wire the provider into datagen**

In `DataGeneration.java`, after the existing `CircusPavilionStructureProvider` line (`:24`), add:

```java
		generator.addProvider(event.includeServer(), new DynastyCastlePieceProvider(packOutput));
```

- [ ] **Step 3: Run datagen to emit the pieces**

Run: `./gradlew runData`
Expected: BUILD SUCCESSFUL; new files appear under
`src/main/resources/data/hemomancy/structure/dynasty_castle/*.nbt` (one per piece name).

- [ ] **Step 4: Verify the files exist**

Run: `ls src/main/resources/data/hemomancy/structure/dynasty_castle/`
Expected: 11 `.nbt` files (`keep_grand.nbt`, `tower_tall.nbt`, … `gate_ruined.nbt`).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/data/gen/DynastyCastlePieceProvider.java src/main/java/com/vincenthuto/hemomancy/common/data/gen/DataGeneration.java src/main/resources/data/hemomancy/structure/dynasty_castle
git commit -m "feat: generate dynasty castle piece nbt templates"
```

---

## Task 3: Layout manifest (code assembly graph)

Define which pieces compose a castle and where, with per-slot random variants.

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/DynastyCastleLayouts.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/worldgen/DynastyCastleLayoutsTest.java`

**Interfaces:**
- Produces:
  - `record Slot(BlockPos offset, Rotation rotation, List<String> candidates)` — `candidates` may include the sentinel `"empty"` (skip this slot).
  - `record Layout(String keep, List<Slot> slots)`
  - `static List<Layout> all()` — at least 2 layouts with different keep + slot sets.
  - `static Layout pick(RandomSource random)` — uniformly choose a layout.

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincenthuto.hemomancy.common.data.gen.DynastyCastlePieces;
import com.vincenthuto.hemomancy.common.worldgen.structure.DynastyCastleLayouts;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class DynastyCastleLayoutsTest {
	@Test
	void everyReferencedPieceExists() {
		Set<String> valid = new HashSet<>(DynastyCastlePieces.pieceNames());
		valid.add("empty");
		assertFalse(DynastyCastleLayouts.all().isEmpty());
		for (var layout : DynastyCastleLayouts.all()) {
			assertTrue(valid.contains(layout.keep()), "unknown keep " + layout.keep());
			for (var slot : layout.slots())
				for (var candidate : slot.candidates())
					assertTrue(valid.contains(candidate), "unknown piece " + candidate);
		}
	}

	@Test
	void pickIsDeterministicForASeed() {
		var a = DynastyCastleLayouts.pick(RandomSource.create(42L));
		var b = DynastyCastleLayouts.pick(RandomSource.create(42L));
		assertTrue(a.keep().equals(b.keep()));
	}
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.vincenthuto.hemomancy.common.worldgen.DynastyCastleLayoutsTest"`
Expected: FAIL — `DynastyCastleLayouts` does not exist.

- [ ] **Step 3: Write the layouts class**

Offsets assume each keep piece is centered on the courtyard; tune them in Task 6. Corner-tower
offsets place a 7-wide tower so its inner edge meets the keep; wall slots sit between keep and
tower. Rotations orient wall/gate window faces outward.

```java
package com.vincenthuto.hemomancy.common.worldgen.structure;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;

public final class DynastyCastleLayouts {
	public record Slot(BlockPos offset, Rotation rotation, List<String> candidates) {}
	public record Layout(String keep, List<Slot> slots) {}

	private DynastyCastleLayouts() {}

	private static final List<String> TOWERS = List.of("tower_tall", "tower_short", "tower_broken", "empty");
	private static final List<String> WALLS = List.of("wall_gothic_window", "wall_straight", "wall_breach");

	private static final Layout GRAND = new Layout("keep_grand", List.of(
			new Slot(new BlockPos(-13, 0, -13), Rotation.NONE, TOWERS),
			new Slot(new BlockPos(13, 0, -13), Rotation.CLOCKWISE_90, TOWERS),
			new Slot(new BlockPos(-13, 0, 13), Rotation.COUNTERCLOCKWISE_90, TOWERS),
			new Slot(new BlockPos(13, 0, 13), Rotation.CLOCKWISE_180, TOWERS),
			new Slot(new BlockPos(-6, 0, -15), Rotation.NONE, WALLS),
			new Slot(new BlockPos(-6, 0, 15), Rotation.CLOCKWISE_180, WALLS),
			new Slot(new BlockPos(-2, 0, -18), Rotation.NONE, List.of("gate_intact", "gate_ruined"))));

	private static final Layout RUINED = new Layout("keep_ruined", List.of(
			new Slot(new BlockPos(-13, 0, -13), Rotation.NONE, TOWERS),
			new Slot(new BlockPos(13, 0, 13), Rotation.CLOCKWISE_180, TOWERS),
			new Slot(new BlockPos(-6, 0, -15), Rotation.NONE, WALLS),
			new Slot(new BlockPos(-2, 0, -18), Rotation.NONE, List.of("gate_ruined", "empty"))));

	private static final Layout SQUAT = new Layout("keep_squat", List.of(
			new Slot(new BlockPos(-13, 0, -13), Rotation.NONE, List.of("tower_broken", "tower_short", "empty")),
			new Slot(new BlockPos(-6, 0, -15), Rotation.NONE, WALLS)));

	private static final List<Layout> ALL = List.of(GRAND, RUINED, SQUAT);

	public static List<Layout> all() {
		return ALL;
	}

	public static Layout pick(RandomSource random) {
		return ALL.get(random.nextInt(ALL.size()));
	}
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "com.vincenthuto.hemomancy.common.worldgen.DynastyCastleLayoutsTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/DynastyCastleLayouts.java src/test/java/com/vincenthuto/hemomancy/common/worldgen/DynastyCastleLayoutsTest.java
git commit -m "feat: add dynasty castle layout manifest"
```

---

## Task 4: Custom Structure class + registration

Assemble the layout into `PoolElementStructurePiece`s, following `VigilPlacement.candidate` for
piece construction and `MausoleumStructure` for CODEC + `findGenerationPoint` shape.

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/DynastyCastleStructure.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/StructureInit.java` (add registration line + import already covered by `structure.*`)

**Interfaces:**
- Consumes: `DynastyCastleLayouts` (Task 3); piece NBTs under `hemomancy:dynasty_castle/<name>` (Task 2).
- Produces: `DynastyCastleStructure.CODEC` (`MapCodec<DynastyCastleStructure>`); `StructureInit.dynasty_castle` holder.

- [ ] **Step 1: Write the structure class**

```java
package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class DynastyCastleStructure extends Structure {
	public static final MapCodec<DynastyCastleStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					settingsCodec(instance),
					HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
					Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
							.forGetter(s -> s.projectStartToHeightmap))
			.apply(instance, DynastyCastleStructure::new));

	private final HeightProvider startHeight;
	private final Optional<Heightmap.Types> projectStartToHeightmap;

	public DynastyCastleStructure(StructureSettings settings, HeightProvider startHeight,
			Optional<Heightmap.Types> projectStartToHeightmap) {
		super(settings);
		this.startHeight = startHeight;
		this.projectStartToHeightmap = projectStartToHeightmap;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		RandomSource random = context.random();
		int startY = this.startHeight.sample(random,
				new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
		ChunkPos chunkPos = context.chunkPos();
		BlockPos origin = new BlockPos(chunkPos.getMiddleBlockX(), startY, chunkPos.getMiddleBlockZ());
		StructureTemplateManager templates = context.structureTemplateManager();
		DynastyCastleLayouts.Layout layout = DynastyCastleLayouts.pick(random);

		List<PoolElementStructurePiece> pieces = new ArrayList<>();
		pieces.add(piece(templates, layout.keep(), origin, net.minecraft.world.level.block.Rotation.NONE));
		for (DynastyCastleLayouts.Slot slot : layout.slots()) {
			String choice = slot.candidates().get(random.nextInt(slot.candidates().size()));
			if (choice.equals("empty")) continue;
			pieces.add(piece(templates, choice, origin.offset(slot.offset().rotate(net.minecraft.world.level.block.Rotation.NONE)),
					slot.rotation()));
		}

		return Optional.of(new GenerationStub(origin, builder -> pieces.forEach(builder::addPiece)));
	}

	private static PoolElementStructurePiece piece(StructureTemplateManager templates, String name,
			BlockPos position, net.minecraft.world.level.block.Rotation rotation) {
		StructurePoolElement element = StructurePoolElement
				.single("hemomancy:dynasty_castle/" + name)
				.apply(StructureTemplatePool.Projection.RIGID);
		BoundingBox bounds = element.getBoundingBox(templates, position, rotation);
		return new PoolElementStructurePiece(templates, element, position, 0, rotation, bounds,
				LiquidSettings.APPLY_WATERLOGGING);
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.dynasty_castle.get();
	}
}
```

Note: `StructurePoolElement.single(String)` requires the `Function<..., StructurePoolElement>`
form used in `VigilPlacement.candidate` — if the compiler flags the string overload, mirror
`VigilPlacement`'s exact call (`StructurePoolElement.single("hemomancy:antecedent/"+part.name())`)
which resolves to the same builder. `Hemomancy` import stays even if unused-removed by your IDE.

- [ ] **Step 2: Register the StructureType**

In `StructureInit.java`, after the `crimson_lodge_annex` registration, add:

```java
	public static final DeferredHolder<StructureType<?>, StructureType<DynastyCastleStructure>> dynasty_castle = STRUCTURES
			.register("dynasty_castle", () -> explicitStructureTypeTyping(DynastyCastleStructure.CODEC));
```

(`import com.vincenthuto.hemomancy.common.worldgen.structure.*;` already present.)

- [ ] **Step 3: Compile**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/DynastyCastleStructure.java src/main/java/com/vincenthuto/hemomancy/common/init/StructureInit.java
git commit -m "feat: add dynasty castle structure type and assembly"
```

---

## Task 5: Worldgen JSON (structure, set, biome tag)

**Files:**
- Create: `src/main/resources/data/hemomancy/worldgen/structure/dynasty_castle.json`
- Create: `src/main/resources/data/hemomancy/worldgen/structure_set/dynasty_castle.json`
- Create: `src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/dynasty_castle.json`

- [ ] **Step 1: Write the structure JSON**

`dynasty_castle.json` (structure):

```json
{
  "type": "hemomancy:dynasty_castle",
  "start_height": {
    "type": "minecraft:constant",
    "value": { "absolute": 0 }
  },
  "project_start_to_heightmap": "WORLD_SURFACE_WG",
  "terrain_adaptation": "beard_thin",
  "biomes": "#hemomancy:has_structure/dynasty_castle",
  "step": "surface_structures",
  "spawn_overrides": {}
}
```

- [ ] **Step 2: Write the structure_set JSON**

`structure_set/dynasty_castle.json`:

```json
{
  "structures": [
    { "structure": "hemomancy:dynasty_castle", "weight": 1 }
  ],
  "placement": {
    "type": "minecraft:random_spread",
    "spacing": 24,
    "separation": 8,
    "salt": 471203985
  }
}
```

- [ ] **Step 3: Write the biome tag**

`tags/worldgen/biome/has_structure/dynasty_castle.json`:

```json
{
  "replace": false,
  "values": [
    "hemomancy:fungal_gardens",
    "hemomancy:drifting_mycelium",
    "hemomancy:hyphal_spires",
    "hemomancy:fungal_isles",
    "hemomancy:sporecrown_thicket",
    "hemomancy:mycelial_depths",
    "hemomancy:hemorrhagic_plateau"
  ]
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/data/hemomancy/worldgen/structure/dynasty_castle.json src/main/resources/data/hemomancy/worldgen/structure_set/dynasty_castle.json src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/dynasty_castle.json
git commit -m "feat: add dynasty castle structure worldgen json"
```

---

## Task 6: Remove the old feature and update tests

**Files:**
- Delete: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/DynastyCastleFeature.java`
- Delete: `src/main/resources/data/hemomancy/worldgen/configured_feature/dynasty_castle.json`
- Delete: `src/main/resources/data/hemomancy/worldgen/placed_feature/dynasty_castle.json`
- Delete: `src/main/resources/data/hemomancy/neoforge/biome_modifier/add_dynasty_castle.json`
- Delete: `src/main/resources/data/hemomancy/tags/worldgen/biome/dynasty_castle_spawnlist.json`
- Modify: `BaseFeatureInit.java:77-78`, `ConfiguredFeatureInit.java:81` & `:253`, `PlacedFeatureInit.java:65`, `BiomeInit.java:175`
- Replace: `src/test/java/com/vincenthuto/hemomancy/common/worldgen/DynastyCastleWorldgenSourceTest.java`

- [ ] **Step 1: Replace the source test**

Overwrite `DynastyCastleWorldgenSourceTest.java`:

```java
package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DynastyCastleWorldgenSourceTest {
	private static final Path STRUCTURE = Path.of("src/main/resources/data/hemomancy/worldgen/structure/dynasty_castle.json");
	private static final Path SET = Path.of("src/main/resources/data/hemomancy/worldgen/structure_set/dynasty_castle.json");
	private static final Path BIOME_TAG = Path.of("src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/dynasty_castle.json");
	private static final Path OLD_FEATURE = Path.of("src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/DynastyCastleFeature.java");
	private static final Path STRUCTURE_INIT = Path.of("src/main/java/com/vincenthuto/hemomancy/common/init/StructureInit.java");

	@Test
	void structureIsRegisteredAndSpaced() throws Exception {
		assertTrue(Files.readString(STRUCTURE).contains("hemomancy:dynasty_castle"));
		assertTrue(Files.readString(STRUCTURE).contains("beard_thin"));
		String set = Files.readString(SET);
		assertTrue(set.contains("random_spread"));
		assertTrue(set.contains("\"separation\""));
		assertTrue(Files.readString(BIOME_TAG).contains("hemomancy:fungal_gardens"));
		assertTrue(Files.readString(STRUCTURE_INIT).contains("dynasty_castle"));
	}

	@Test
	void oldFeatureIsGone() {
		assertFalse(Files.exists(OLD_FEATURE));
	}
}
```

- [ ] **Step 2: Remove the feature registration lines**

- `BaseFeatureInit.java`: delete the `DYNASTY_CASTLE` field (the `register("dynasty_castle", ...)` block).
- `ConfiguredFeatureInit.java`: delete the `DYNASTY_CASTLE` key field and its `register(context, DYNASTY_CASTLE, ...)` line.
- `PlacedFeatureInit.java`: delete the `DYNASTY_CASTLE` key field. Grep the file for other `DYNASTY_CASTLE` uses (e.g. a `PLACED` list registration) and remove them too.
- `BiomeInit.java:175`: delete the `addFeature(biomeBuilder, ..., PlacedFeatureInit.DYNASTY_CASTLE);` line.

- [ ] **Step 3: Delete the feature file and its JSON**

```bash
git rm src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/DynastyCastleFeature.java \
  src/main/resources/data/hemomancy/worldgen/configured_feature/dynasty_castle.json \
  src/main/resources/data/hemomancy/worldgen/placed_feature/dynasty_castle.json \
  src/main/resources/data/hemomancy/neoforge/biome_modifier/add_dynasty_castle.json \
  src/main/resources/data/hemomancy/tags/worldgen/biome/dynasty_castle_spawnlist.json
```

- [ ] **Step 4: Compile and run the full worldgen test class**

Run: `./gradlew compileJava test --tests "com.vincenthuto.hemomancy.common.worldgen.*"`
Expected: BUILD SUCCESSFUL; all three worldgen test classes pass. If compile fails on a leftover
`DYNASTY_CASTLE` reference, grep `grep -rn "DYNASTY_CASTLE" src/main/java` and remove each.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: remove dynasty castle feature in favor of structure"
```

---

## Task 7: In-game verification and offset tuning

Manual — worldgen assembly can't be unit-tested end to end.

- [ ] **Step 1: Launch the client**

Run: `./gradlew runClient`

- [ ] **Step 2: Load each piece in a structure block**

In a creative superflat world, place a structure block in Load mode, enter
`hemomancy:dynasty_castle/keep_grand`, and confirm it loads without a "failed to load" chat error.
Repeat for `tower_tall` and `gate_intact` (spot check). Confirms NBT validity.

- [ ] **Step 3: Place the full structure**

Run in chat: `/place structure hemomancy:dynasty_castle`
Expected: a castle assembles — keep with corner towers, walls, and a gatehouse. Note any piece that
floats or clips into the keep.

- [ ] **Step 4: Tune offsets if needed**

If pieces are misaligned, adjust the `BlockPos offset` / `Rotation` values in
`DynastyCastleLayouts` (Task 3) and, for vertical float, the piece `size`/origin in
`DynastyCastlePieces`. Re-run `./gradlew runData` only if piece geometry changed, then re-test.
Place several times to confirm layouts and tower/wall variants differ between placements.

- [ ] **Step 5: Verify natural generation**

Create a new world, travel into the fungal_gardens dimension, and run
`/locate structure hemomancy:dynasty_castle`. Expected: it finds one within spacing range, with no
overlaps. Commit any offset tuning:

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/DynastyCastleLayouts.java src/main/java/com/vincenthuto/hemomancy/common/data/gen/DynastyCastlePieces.java
git commit -m "fix: tune dynasty castle piece alignment"
```

---

## Self-Review

**Spec coverage:**
- Spec §1 generator → Task 2. §2 piece set → Task 1 (`pieceNames`). §3 layout manifest → Task 3.
  §4 structure/set/biomes → Tasks 4–5. §5 household spawns → Task 1 (`entities`) + Task 2 (baked
  NBT). §6 cleanup → Task 6. Terrain adaptation / spacing removal of old code → Task 5 JSON + Task 6.
- All spec sections mapped to a task.

**Placeholder scan:** The only intentional "port the rest" instruction is Task 1 Step 3, which
names the exact source methods and the coordinate-translation rule and enforces correctness via the
Task 1 test — not an open TODO. Geometry for one representative piece (`tower`) is shown complete.

**Type consistency:** `BlockPlacement`/`EntityPlacement`/`pieceNames`/`size`/`blocks`/`entities`
signatures match between Task 1 (definition), Task 2 (consumption), and the tests.
`DynastyCastleLayouts.Slot/Layout/all/pick` match between Task 3 and Task 4. `StructureInit.dynasty_castle`
defined in Task 4 Step 2 and asserted in Task 6.
