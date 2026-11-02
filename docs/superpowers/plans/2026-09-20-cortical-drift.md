# Cortical Drift End Biome Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `hemomancy:cortical_drift`, a self-contained End biome of floating islands wired together by crackling nerve-fiber bridges, centered on one colossal hollow brain — the Vagrant Mind.

**Architecture:** Biome placement rides TerraBlender's `EndBiomeRegistry` (no mixin, no dimension override) with an `END`-category surface rule reskinning vanilla End island terrain. The Vagrant Mind is a procedural `Structure` built from a metaball shell. Island bridges come from a region-seeded planner (`CorticalNetworkPlan`) that an ordinary per-chunk `Feature` draws slices of. All sparking reuses the existing HutosLib lightning and tendril systems.

**Tech Stack:** NeoForge 1.21.1 (21.1.219), Java 21, TerraBlender 4.1.0.8, HutosLib, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-09-20-cortical-drift-design.md`

## Global Constraints

- NeoForge imports only (`net.neoforged.*`). Never `net.minecraftforge.*`.
- Resource locations via `Hemomancy.rloc("snake_case_id")`; registry entries via `DeferredHolder`/`DeferredRegister`.
- Singular 1.21 data paths: `data/hemomancy/worldgen/...`, `data/hemomancy/tags/block/...`. Never pluralize to `recipes/`/`loot_tables/`.
- Networking is payload-based (`CustomPacketPayload` + static `TYPE` + `STREAM_CODEC`). Never `SimpleChannel`.
- Visual effects must use the authored Hemomancy/HutosLib toolkit (glow, tendril, lightning), not vanilla particle clouds.
- Build/test commands run from project root on Windows PowerShell: `./gradlew.bat build`, `./gradlew.bat test`, `./gradlew.bat alphaCheck`, `./gradlew.bat runClient`.
- Lore guardrails: the Vagrant Mind is a dormant organ, never a deity. No God/Satan framing.
- Do not reference `compat/mna/**` or `compat/curios/**` — both are excluded from the build.

---

## Phase Overview

| Phase | Deliverable | Verifiable by |
|---|---|---|
| 1 | Block palette + tags + assets | `./gradlew.bat build`, blocks exist in-game |
| 2 | Biome, surface rules, TerraBlender End registration, config | Fly into the End, find a reskinned patch |
| 3 | The Vagrant Mind structure | `/locate structure hemomancy:vagrant_mind` |
| 4 | Island nerve bridge network | Bridges connect every island, unbroken across chunks |
| 5 | Nerve lightning, ambience, docs | Fibers crackle; docs updated; `alphaCheck` green |

Each phase leaves the mod in a working, launchable state.

---

# Phase 1 — Block palette

Ten blocks, their tags, models, textures, and lang. Nothing in later phases can be built without these, and they are independently verifiable in a creative inventory.

### Task 1: Register the bulk terrain blocks

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalDriftBlockPaletteSourceTest.java`

**Interfaces:**
- Produces: `BlockInit.neural_tissue`, `BlockInit.axonal_slate`, `BlockInit.myelin_sheath`, `BlockInit.ganglion_matter`, `BlockInit.cortical_folds` — all `DeferredHolder<Block, Block>`.

- [ ] **Step 1: Write the failing test**

This repo's worldgen and registration tests are source-contract tests (they read the source file and assert on its text) because Minecraft registry classes cannot be instantiated in a plain JVM test. Follow that pattern.

```java
package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CorticalDriftBlockPaletteSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath));
	}

	@Test
	void bulkTerrainBlocksAreRegistered() throws IOException {
		String blockInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java");
		for (String id : new String[] { "neural_tissue", "axonal_slate", "myelin_sheath",
				"ganglion_matter", "cortical_folds" }) {
			assertTrue(blockInit.contains("BASEBLOCKS.register(\"" + id + "\""),
					"BlockInit should register " + id);
		}
	}

	@Test
	void ganglionMatterEmitsLight() throws IOException {
		String blockInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java");
		int index = blockInit.indexOf("BASEBLOCKS.register(\"ganglion_matter\"");
		assertTrue(index > 0, "ganglion_matter must be registered");
		String declaration = blockInit.substring(index, index + 400);
		assertTrue(declaration.contains("lightLevel"), "ganglion_matter should emit light");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*CorticalDriftBlockPaletteSourceTest"`
Expected: FAIL — "BlockInit should register neural_tissue".

- [ ] **Step 3: Register the blocks**

Add near the `venous_stone` declarations in `BlockInit.java` (around line 263), matching the surrounding formatting exactly:

```java
	public static final DeferredHolder<Block, Block> neural_tissue = BASEBLOCKS.register("neural_tissue",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));

	public static final DeferredHolder<Block, Block> axonal_slate = BASEBLOCKS.register("axonal_slate",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));

	public static final DeferredHolder<Block, Block> myelin_sheath = BASEBLOCKS.register("myelin_sheath",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.0F, 4.0F)));

	public static final DeferredHolder<Block, Block> ganglion_matter = BASEBLOCKS.register("ganglion_matter",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.lightLevel(state -> 6)));

	public static final DeferredHolder<Block, Block> cortical_folds = BASEBLOCKS.register("cortical_folds",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "*CorticalDriftBlockPaletteSourceTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java src/test/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalDriftBlockPaletteSourceTest.java
git commit -m "feat(worldgen): register Cortical Drift bulk terrain blocks"
```

### Task 2: Register the fiber and node blocks

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java`
- Modify: `src/test/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalDriftBlockPaletteSourceTest.java`

**Interfaces:**
- Consumes: the `BASEBLOCKS` / `CROSSBLOCKS` registers from Task 1.
- Produces: `BlockInit.nerve_fiber`, `BlockInit.nerve_bundle` (both `RotatedPillarBlock`, so they can run along any axis), `BlockInit.synaptic_node`, `BlockInit.dura_membrane`, `BlockInit.dendrite_growth`.

- [ ] **Step 1: Write the failing test**

Append to `CorticalDriftBlockPaletteSourceTest`:

```java
	@Test
	void fiberBlocksAreAxisAligned() throws IOException {
		String blockInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java");
		for (String id : new String[] { "nerve_fiber", "nerve_bundle" }) {
			int index = blockInit.indexOf("BASEBLOCKS.register(\"" + id + "\"");
			assertTrue(index > 0, "BlockInit should register " + id);
			assertTrue(blockInit.substring(index, index + 300).contains("RotatedPillarBlock"),
					id + " must be a RotatedPillarBlock so fibers can run on any axis");
		}
	}

	@Test
	void synapticNodeIsBrightAndTagged() throws IOException {
		String blockInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java");
		int index = blockInit.indexOf("BASEBLOCKS.register(\"synaptic_node\"");
		assertTrue(index > 0, "synaptic_node must be registered");
		assertTrue(blockInit.substring(index, index + 400).contains("lightLevel(state -> 10)"),
				"synaptic_node should be the brightest block in the palette");

		String conductors = read("src/main/resources/data/hemomancy/tags/block/ductilis_conductors.json");
		assertTrue(conductors.contains("hemomancy:synaptic_node"),
				"synaptic_node must conduct so Ferric Ductilis chains along the bridges");
	}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*CorticalDriftBlockPaletteSourceTest"`
Expected: FAIL — "BlockInit should register nerve_fiber".

- [ ] **Step 3: Register the blocks and add the tag entry**

In `BlockInit.java`, below the Task 1 block:

```java
	public static final DeferredHolder<Block, Block> nerve_fiber = BASEBLOCKS.register("nerve_fiber",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of().strength(0.4F, 1.0F).noOcclusion()));

	public static final DeferredHolder<Block, Block> nerve_bundle = BASEBLOCKS.register("nerve_bundle",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of().strength(0.8F, 2.0F).noOcclusion()));

	public static final DeferredHolder<Block, Block> synaptic_node = BASEBLOCKS.register("synaptic_node",
			() -> new Block(BlockBehaviour.Properties.of().strength(1.2F, 4.0F).lightLevel(state -> 10)));

	public static final DeferredHolder<Block, Block> dura_membrane = BASEBLOCKS.register("dura_membrane",
			() -> new Block(BlockBehaviour.Properties.of().strength(0.6F, 1.0F).noOcclusion()));

	public static final DeferredHolder<Block, Block> dendrite_growth = CROSSBLOCKS.register("dendrite_growth",
			() -> new Block(BlockBehaviour.Properties.of().instabreak().noCollission().noOcclusion()));
```

Ensure `net.minecraft.world.level.block.RotatedPillarBlock` is imported.

Then add to the `"values"` array in `src/main/resources/data/hemomancy/tags/block/ductilis_conductors.json`, after the existing `"hemomancy:iron_brazier"` entry (add a comma to that line):

```json
    "hemomancy:synaptic_node"
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "*CorticalDriftBlockPaletteSourceTest"`
Expected: PASS, 4 tests.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java src/main/resources/data/hemomancy/tags/block/ductilis_conductors.json src/test/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalDriftBlockPaletteSourceTest.java
git commit -m "feat(worldgen): register Cortical Drift fiber and synaptic node blocks"
```

### Task 3: Textures, models, and lang

**Files:**
- Create: `src/main/resources/assets/hemomancy/textures/block/neural_tissue.png` and one per block from Tasks 1–2 (10 total; `nerve_fiber` and `nerve_bundle` each also need a `_top` variant for the pillar end cap)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/data/gen/DataGeneration.java` if the new blocks need explicit datagen entries
- Modify: `src/generated/resources/assets/hemomancy/lang/en_us.json`

**Interfaces:**
- Consumes: the block ids registered in Tasks 1–2.
- Produces: nothing code-facing; this task makes the blocks renderable.

- [ ] **Step 1: Author the textures**

16×16 PNGs matching the reference palette:

| Texture | Palette |
|---|---|
| `neural_tissue` | near-black `#0B0A11` with faint violet flecks |
| `axonal_slate` | deep blue `#1A2140`, horizontal banding |
| `myelin_sheath` | bone white `#D8D5C4`, soft mottling |
| `ganglion_matter` | yellow `#C9A227` with dark seams |
| `cortical_folds` | grey-tan `#8E8B72`, strong parallel gyri ridges |
| `nerve_fiber` / `nerve_fiber_top` | pale filament `#CFC9E8` on dark |
| `nerve_bundle` / `nerve_bundle_top` | braided multi-strand, same hues |
| `synaptic_node` | white-violet `#EDE7FF`, bright centre |
| `dura_membrane` | translucent grey-violet, alpha channel |
| `dendrite_growth` | thin branching filaments, transparent background |

- [ ] **Step 2: Run datagen**

Run: `./gradlew.bat runData`
Expected: blockstate and item-model JSON appear under `src/generated/resources/assets/hemomancy/`. `runData` generates blockstates, item models, and language only — server providers are intentionally commented out, so do not enable them.

- [ ] **Step 3: Verify the lang entries generated**

Run: `git diff src/generated/resources/assets/hemomancy/lang/en_us.json`
Expected: ten new `block.hemomancy.*` entries. Correct any that read awkwardly (for example ensure `Dura Membrane`, not `Dura membrane`).

- [ ] **Step 4: Verify in-game**

Run: `./gradlew.bat runClient`
Expected: all ten blocks appear in the creative inventory with correct textures and no missing-model checkerboard. `ganglion_matter` and `synaptic_node` visibly glow.

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/assets/hemomancy/textures/block src/generated/resources/assets/hemomancy
git commit -m "feat(assets): add Cortical Drift block textures, models, and lang"
```

---

# Phase 2 — Biome, surface rules, and End registration

At the end of this phase you can fly into the End and find a Cortical Drift patch with the right palette and fog. No structures or bridges yet.

### Task 4: Config entries

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/config/HemoCommonConfig.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/config/CorticalDriftConfigSourceTest.java`

**Interfaces:**
- Produces: `HemoCommonConfig.ENABLE_CORTICAL_DRIFT_END_REGION` (`BooleanValue`) and `HemoCommonConfig.CORTICAL_DRIFT_END_REGION_WEIGHT` (`IntValue`).

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CorticalDriftConfigSourceTest {
	@Test
	void endRegionIsConfigurableAndRestartGated() throws IOException {
		String config = Files.readString(Path.of("").toAbsolutePath()
				.resolve("src/main/java/com/vincenthuto/hemomancy/config/HemoCommonConfig.java"));
		assertTrue(config.contains("ENABLE_CORTICAL_DRIFT_END_REGION"),
				"toggle for the Cortical Drift End region must exist");
		assertTrue(config.contains("enableCorticalDriftEndRegion"),
				"toggle must use the camelCase config key");
		assertTrue(config.contains("corticalDriftEndRegionWeight"),
				"TerraBlender weight must be configurable");
		int index = config.indexOf("enableCorticalDriftEndRegion");
		assertTrue(config.lastIndexOf("worldRestart()", index) > config.indexOf("push(\"worldgen\")"),
				"biome registration cannot change on a live world, so it must be restart-gated");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*CorticalDriftConfigSourceTest"`
Expected: FAIL — "toggle for the Cortical Drift End region must exist".

- [ ] **Step 3: Add the config entries**

In `HemoCommonConfig`, add the fields beside the Phlegethontic ones:

```java
	public static BooleanValue ENABLE_CORTICAL_DRIFT_END_REGION;
	public static net.neoforged.neoforge.common.ModConfigSpec.IntValue CORTICAL_DRIFT_END_REGION_WEIGHT;
```

And inside `registerCommonConfig`, after the Phlegethontic block and before `commonBuilder.pop()`:

```java
		ENABLE_CORTICAL_DRIFT_END_REGION = commonBuilder
				.comment("Generate the Cortical Drift biome in new End chunks. Requires restart.")
				.worldRestart().define("enableCorticalDriftEndRegion", true);
		CORTICAL_DRIFT_END_REGION_WEIGHT = commonBuilder
				.comment("TerraBlender End biome weight for Cortical Drift. Requires restart.")
				.worldRestart().defineInRange("corticalDriftEndRegionWeight", 2, 1, 100);
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "*CorticalDriftConfigSourceTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/config/HemoCommonConfig.java src/test/java/com/vincenthuto/hemomancy/config/CorticalDriftConfigSourceTest.java
git commit -m "feat(config): add Cortical Drift End region toggle and weight"
```

### Task 5: Surface rules

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/biome/CorticalDriftSurfaceRuleData.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalDriftSurfaceRuleSourceTest.java`

**Interfaces:**
- Consumes: `BlockInit.myelin_sheath`, `axonal_slate`, `neural_tissue`, `ganglion_matter` (Task 1); `BiomeInit.CORTICAL_DRIFT` (Task 6 — declare the key there first if working out of order).
- Produces: `CorticalDriftSurfaceRuleData.makeRules()` returning `SurfaceRules.RuleSource`.

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CorticalDriftSurfaceRuleSourceTest {
	private static String source() throws IOException {
		return Files.readString(Path.of("").toAbsolutePath().resolve(
				"src/main/java/com/vincenthuto/hemomancy/common/worldgen/biome/CorticalDriftSurfaceRuleData.java"));
	}

	@Test
	void rulesAreScopedToTheBiome() throws IOException {
		assertTrue(source().contains("SurfaceRules.isBiome(BiomeInit.CORTICAL_DRIFT)"),
				"rules must not leak into vanilla End biomes");
	}

	@Test
	void strataRunCrustToCore() throws IOException {
		String source = source();
		assertTrue(source.contains("SurfaceRules.ON_FLOOR") && source.contains("myelin_sheath"),
				"white myelin crust on the floor");
		assertTrue(source.contains("SurfaceRules.UNDER_FLOOR") && source.contains("axonal_slate"),
				"deep blue slate directly beneath");
		assertTrue(source.contains("SurfaceRules.DEEP_UNDER_FLOOR") && source.contains("neural_tissue"),
				"black neural tissue at depth");
		assertTrue(source.contains("Noises.SURFACE") && source.contains("ganglion_matter"),
				"yellow ganglion seams blended by surface noise");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*CorticalDriftSurfaceRuleSourceTest"`
Expected: FAIL — `NoSuchFileException`.

- [ ] **Step 3: Write the rule source**

This mirrors `PhlegethonticSurfaceRuleData` exactly.

```java
package com.vincenthuto.hemomancy.common.worldgen.terrablender;

import com.vincenthuto.hemomancy.common.init.BiomeInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;

/** Reskins vanilla End island terrain inside the Cortical Drift without touching its shape. */
public final class CorticalDriftSurfaceRuleData {
	private CorticalDriftSurfaceRuleData() {}

	public static SurfaceRules.RuleSource makeRules() {
		SurfaceRules.RuleSource myelin = state(BlockInit.myelin_sheath.get());
		SurfaceRules.RuleSource slate = state(BlockInit.axonal_slate.get());
		SurfaceRules.RuleSource ganglion = state(BlockInit.ganglion_matter.get());
		SurfaceRules.RuleSource tissue = state(BlockInit.neural_tissue.get());

		return SurfaceRules.ifTrue(SurfaceRules.isBiome(BiomeInit.CORTICAL_DRIFT), SurfaceRules.sequence(
				SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, myelin),
				SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.sequence(
						SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -0.2, 0.2), ganglion),
						slate)),
				SurfaceRules.ifTrue(SurfaceRules.DEEP_UNDER_FLOOR, tissue)));
	}

	private static SurfaceRules.RuleSource state(net.minecraft.world.level.block.Block block) {
		return SurfaceRules.state(block.defaultBlockState());
	}
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "*CorticalDriftSurfaceRuleSourceTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/worldgen/biome/CorticalDriftSurfaceRuleData.java src/test/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalDriftSurfaceRuleSourceTest.java
git commit -m "feat(worldgen): add Cortical Drift End surface rules"
```

### Task 6: Biome definition and TerraBlender End registration

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/BiomeInit.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalDriftBiomeRegistrationSourceTest.java`

**Interfaces:**
- Consumes: `CorticalDriftSurfaceRuleData.makeRules()` (Task 5), the config values (Task 4).
- Produces: `BiomeInit.CORTICAL_DRIFT` (a `ResourceKey<Biome>`).

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CorticalDriftBiomeRegistrationSourceTest {
	private static String biomeInit() throws IOException {
		return Files.readString(Path.of("").toAbsolutePath()
				.resolve("src/main/java/com/vincenthuto/hemomancy/common/init/BiomeInit.java"));
	}

	@Test
	void biomeKeyIsRegisteredAndBootstrapped() throws IOException {
		String source = biomeInit();
		assertTrue(source.contains("CORTICAL_DRIFT = register(\"cortical_drift\")"),
				"the biome key must exist");
		assertTrue(source.contains("register(context, CORTICAL_DRIFT, corticalDrift("),
				"the biome must be bootstrapped like its siblings");
	}

	@Test
	void registeredInThreeEndCategoriesButNotEdge() throws IOException {
		String source = biomeInit();
		assertTrue(source.contains("EndBiomeRegistry.registerHighlandsBiome(BiomeInit.CORTICAL_DRIFT"),
				"highlands carries the large plateau islands");
		assertTrue(source.contains("EndBiomeRegistry.registerMidlandsBiome(BiomeInit.CORTICAL_DRIFT"),
				"midlands keeps the patch from being seamed by vanilla end_midlands");
		assertTrue(source.contains("EndBiomeRegistry.registerIslandBiome(BiomeInit.CORTICAL_DRIFT"),
				"island carries the small scattered void isles");
		assertFalse(source.contains("EndBiomeRegistry.registerEdgeBiome(BiomeInit.CORTICAL_DRIFT"),
				"edge is deliberately excluded so the biome never borders the dragon arena");
	}

	@Test
	void endSurfaceRulesAreInstalled() throws IOException {
		assertTrue(biomeInit().contains(
				"SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.END"),
				"END-category surface rules must be installed alongside the OVERWORLD and NETHER ones");
	}

	@Test
	void registrationRespectsTheConfigToggle() throws IOException {
		String source = biomeInit();
		int index = source.indexOf("EndBiomeRegistry.registerHighlandsBiome");
		assertTrue(source.lastIndexOf("ENABLE_CORTICAL_DRIFT_END_REGION", index) > 0,
				"End registration must sit behind the config toggle");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*CorticalDriftBiomeRegistrationSourceTest"`
Expected: FAIL — "the biome key must exist".

- [ ] **Step 3: Add the key, the biome builder, and the registration**

Add the key beside the others near line 41:

```java
	public static final ResourceKey<Biome> CORTICAL_DRIFT = register("cortical_drift");
```

Add to `bootstrapBiomes`, beside the sibling `register(context, ...)` calls:

```java
		register(context, CORTICAL_DRIFT, corticalDrift(placedFeatureGetter, carverGetter));
```

Add the builder method, following the shape of `phlegethonticBasin`. Features stay empty for now; Phases 3–4 add them:

```java
	private static Biome corticalDrift(HolderGetter<PlacedFeature> placedFeatureGetter,
			HolderGetter<ConfiguredWorldCarver<?>> carverGetter) {
		MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
		// Reuse the existing roster rather than adding nerve-native mobs (spec §7). Abhorent
		// Thought suits a drifting nervous system; a bespoke End mob is deferred.
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.abhorent_thought.get(), 12, 1, 3));

		BiomeGenerationSettings.Builder biomeBuilder =
				new BiomeGenerationSettings.Builder(placedFeatureGetter, carverGetter);

		// The End uses no carvers, and terrain shape is vanilla — the biome's identity
		// comes from CorticalDriftSurfaceRuleData plus the Phase 3/4 features.
		return new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.5F).downfall(0.0F)
				.specialEffects(new BiomeSpecialEffects.Builder()
						.waterColor(0x2B2350).waterFogColor(0x140F2B)
						.fogColor(0x241B3D).skyColor(0x07060D)
						.grassColorOverride(0x6B6199).foliageColorOverride(0x6B6199)
						.ambientLoopSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP)
						.ambientMoodSound(
								new AmbientMoodSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0D))
						.build())
				.mobSpawnSettings(spawnBuilder.build()).generationSettings(biomeBuilder.build()).build();
	}
```

Add to `commonSetup`'s `enqueueWork` block, after the Phlegethontic registration:

```java
			if (HemoCommonConfig.ENABLE_CORTICAL_DRIFT_END_REGION.get()) {
				int endWeight = HemoCommonConfig.CORTICAL_DRIFT_END_REGION_WEIGHT.get();
				EndBiomeRegistry.registerHighlandsBiome(BiomeInit.CORTICAL_DRIFT, endWeight);
				EndBiomeRegistry.registerMidlandsBiome(BiomeInit.CORTICAL_DRIFT, endWeight);
				EndBiomeRegistry.registerIslandBiome(BiomeInit.CORTICAL_DRIFT, endWeight);
			}
			SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.END, Hemomancy.MOD_ID,
					CorticalDriftSurfaceRuleData.makeRules());
```

Add the imports `terrablender.api.EndBiomeRegistry` and
`com.vincenthuto.hemomancy.common.worldgen.biome.CorticalDriftSurfaceRuleData`.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "*CorticalDriftBiomeRegistrationSourceTest"`
Expected: PASS, 4 tests.

- [ ] **Step 5: Verify in-game**

Run: `./gradlew.bat runClient`

In a new world: `/execute in the_end run tp @s 3000 80 3000`, then fly outward with `/locatebiome`-style exploration or `/tp` jumps of ~1000 blocks. Expected: within a few thousand blocks you find islands with a white myelin crust, blue slate below, yellow seams, and violet fog. Confirm with F3 that the biome reads `hemomancy:cortical_drift`.

If nothing generates, raise `corticalDriftEndRegionWeight` temporarily to 50 in the config to confirm wiring, then set it back to 2.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/init/BiomeInit.java src/test/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalDriftBiomeRegistrationSourceTest.java
git commit -m "feat(worldgen): register the Cortical Drift End biome via TerraBlender"
```

---

# Phase 3 — The Vagrant Mind

One colossal hollow brain per biome patch, 150–250 blocks across, generated procedurally.

### Task 7: The shell geometry (pure logic, fully unit-testable)

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/vagrantmind/VagrantMindGeometry.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/worldgen/structure/vagrantmind/VagrantMindGeometryTest.java`

This is deliberately separated from any Minecraft class so it can be tested as real logic rather than a source contract — the same split `FerricDuctilisGeometry` / `FerricDuctilisGeometryTest` already uses in this repo.

**Interfaces:**
- Produces:
  - `VagrantMindGeometry.lobes(long seed)` → `List<Lobe>`, 10–16 entries
  - `record Lobe(double cx, double cy, double cz, double rx, double ry, double rz)`
  - `VagrantMindGeometry.field(List<Lobe> lobes, double x, double y, double z)` → `double`; `>= 1.0` means inside
  - `VagrantMindGeometry.isShell(List<Lobe> lobes, double x, double y, double z, double thickness)` → `boolean`
  - `VagrantMindGeometry.isFissure(double x, double z, double halfWidth)` → `boolean`

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.worldgen.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class VagrantMindGeometryTest {
	@Test
	void lobeCountStaysInRange() {
		for (long seed = 0; seed < 50; seed++) {
			int count = VagrantMindGeometry.lobes(seed).size();
			assertTrue(count >= 10 && count <= 16, "seed " + seed + " produced " + count + " lobes");
		}
	}

	@Test
	void geometryIsDeterministicForASeed() {
		assertEquals(VagrantMindGeometry.lobes(1234L), VagrantMindGeometry.lobes(1234L),
				"the same seed must rebuild the same brain, or chunks will disagree");
	}

	@Test
	void centreIsSolidAndDistantSpaceIsEmpty() {
		List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(7L);
		assertTrue(VagrantMindGeometry.field(lobes, 0, 0, 0) >= 1.0, "the core must be inside the field");
		assertFalse(VagrantMindGeometry.field(lobes, 500, 500, 500) >= 1.0, "far space must be outside");
	}

	@Test
	void shellIsHollow() {
		List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(7L);
		assertFalse(VagrantMindGeometry.isShell(lobes, 0, 0, 0, 8.0),
				"the centre is cavity, not shell — otherwise there is nothing to explore");
	}

	@Test
	void fissureSplitsTheHemispheres() {
		assertTrue(VagrantMindGeometry.isFissure(0.0, 40.0, 3.0), "x=0 is the sagittal plane");
		assertFalse(VagrantMindGeometry.isFissure(30.0, 40.0, 3.0), "far off-axis is not fissure");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*VagrantMindGeometryTest"`
Expected: FAIL — cannot resolve `VagrantMindGeometry`.

- [ ] **Step 3: Implement the geometry**

```java
package com.vincenthuto.hemomancy.common.worldgen.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pure geometry for the Vagrant Mind: a metaball union of ellipsoid lobes, hollowed to a shell.
 * Deliberately free of Minecraft types so it can be unit tested, matching FerricDuctilisGeometry.
 */
public final class VagrantMindGeometry {
	public static final double OUTER_RADIUS = 100.0;

	public record Lobe(double cx, double cy, double cz, double rx, double ry, double rz) {}

	private VagrantMindGeometry() {}

	public static List<Lobe> lobes(long seed) {
		Random random = new Random(seed);
		int count = 10 + random.nextInt(7);
		List<Lobe> lobes = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			// Lobes cluster toward the core so the union reads as one organ, not a cloud of blobs.
			double angle = random.nextDouble() * Math.PI * 2.0;
			double spread = 20.0 + random.nextDouble() * 30.0;
			lobes.add(new Lobe(
					Math.cos(angle) * spread,
					(random.nextDouble() - 0.5) * 30.0,
					Math.sin(angle) * spread,
					35.0 + random.nextDouble() * 25.0,
					25.0 + random.nextDouble() * 20.0,
					35.0 + random.nextDouble() * 25.0));
		}
		return lobes;
	}

	/** Summed inverse-square falloff. Values at or above 1.0 are inside the organ. */
	public static double field(List<Lobe> lobes, double x, double y, double z) {
		double total = 0.0;
		for (Lobe lobe : lobes) {
			double dx = (x - lobe.cx()) / lobe.rx();
			double dy = (y - lobe.cy()) / lobe.ry();
			double dz = (z - lobe.cz()) / lobe.rz();
			double squared = dx * dx + dy * dy + dz * dz;
			if (squared < 4.0) {
				total += 1.0 / (0.35 + squared);
			}
		}
		return total;
	}

	public static boolean isShell(List<Lobe> lobes, double x, double y, double z, double thickness) {
		if (field(lobes, x, y, z) < 1.0) {
			return false;
		}
		// Inside the field but close enough to the boundary that stepping out along any axis leaves it.
		return field(lobes, x + thickness, y, z) < 1.0
				|| field(lobes, x - thickness, y, z) < 1.0
				|| field(lobes, x, y + thickness, z) < 1.0
				|| field(lobes, x, y - thickness, z) < 1.0
				|| field(lobes, x, y, z + thickness) < 1.0
				|| field(lobes, x, y, z - thickness) < 1.0;
	}

	/** The sagittal fissure dividing the hemispheres, narrowing toward the front and back. */
	public static boolean isFissure(double x, double z, double halfWidth) {
		double taper = 1.0 - Math.min(1.0, Math.abs(z) / OUTER_RADIUS);
		return Math.abs(x) <= halfWidth * taper;
	}
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "*VagrantMindGeometryTest"`
Expected: PASS, 5 tests.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/vagrantmind/VagrantMindGeometry.java src/test/java/com/vincenthuto/hemomancy/common/worldgen/structure/vagrantmind/VagrantMindGeometryTest.java
git commit -m "feat(worldgen): add Vagrant Mind metaball shell geometry"
```

### Task 8: Cavity fiber web (pure logic)

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/vagrantmind/VagrantMindFiberWeb.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/worldgen/structure/vagrantmind/VagrantMindFiberWebTest.java`

**Interfaces:**
- Consumes: `VagrantMindGeometry.Lobe` and `field(...)` from Task 7.
- Produces:
  - `record Strand(double ax, double ay, double az, double bx, double by, double bz, boolean thick)`
  - `VagrantMindFiberWeb.strands(long seed, List<Lobe> lobes)` → `List<Strand>`
  - `VagrantMindFiberWeb.nodes(List<Strand> strands)` → `List<double[]>`, the branch points

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.worldgen.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class VagrantMindFiberWebTest {
	private static List<VagrantMindGeometry.Lobe> lobes() {
		return VagrantMindGeometry.lobes(42L);
	}

	@Test
	void webSpansTheCavity() {
		List<VagrantMindFiberWeb.Strand> strands = VagrantMindFiberWeb.strands(42L, lobes());
		assertTrue(strands.size() >= 15, "a cavity this large needs a dense web, got " + strands.size());
	}

	@Test
	void webIsDeterministic() {
		assertEquals(VagrantMindFiberWeb.strands(42L, lobes()), VagrantMindFiberWeb.strands(42L, lobes()));
	}

	@Test
	void everyStrandHasLength() {
		for (VagrantMindFiberWeb.Strand strand : VagrantMindFiberWeb.strands(42L, lobes())) {
			double dx = strand.bx() - strand.ax();
			double dy = strand.by() - strand.ay();
			double dz = strand.bz() - strand.az();
			assertTrue(Math.sqrt(dx * dx + dy * dy + dz * dz) > 4.0,
					"degenerate strands would place single floating fiber blocks");
		}
	}

	@Test
	void branchPointsBecomeNodes() {
		List<VagrantMindFiberWeb.Strand> strands = VagrantMindFiberWeb.strands(42L, lobes());
		assertTrue(VagrantMindFiberWeb.nodes(strands).size() >= 3,
				"junctions must be lit by synaptic nodes");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*VagrantMindFiberWebTest"`
Expected: FAIL — cannot resolve `VagrantMindFiberWeb`.

- [ ] **Step 3: Implement the web**

```java
package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.vincenthuto.hemomancy.common.worldgen.structure.vagrantmind.VagrantMindGeometry.Lobe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** The nerve web strung across the Vagrant Mind's hollow interior. */
public final class VagrantMindFiberWeb {
	private static final int ANCHOR_COUNT = 20;

	public record Strand(double ax, double ay, double az, double bx, double by, double bz, boolean thick) {}

	private VagrantMindFiberWeb() {}

	public static List<Strand> strands(long seed, List<Lobe> lobes) {
		Random random = new Random(seed ^ 0x5EEDFEED);
		List<double[]> anchors = anchors(random, lobes);
		List<Strand> strands = new ArrayList<>();
		// Each anchor reaches across to two others, producing a web with natural junctions.
		for (int i = 0; i < anchors.size(); i++) {
			for (int link = 1; link <= 2; link++) {
				double[] a = anchors.get(i);
				double[] b = anchors.get((i + link * 3 + 1) % anchors.size());
				double dx = b[0] - a[0];
				double dy = b[1] - a[1];
				double dz = b[2] - a[2];
				if (Math.sqrt(dx * dx + dy * dy + dz * dz) <= 4.0) {
					continue;
				}
				strands.add(new Strand(a[0], a[1], a[2], b[0], b[1], b[2], random.nextInt(4) == 0));
			}
		}
		return strands;
	}

	/** Endpoints shared by more than one strand — these get lit as synaptic nodes. */
	public static List<double[]> nodes(List<Strand> strands) {
		Map<String, double[]> seen = new HashMap<>();
		Map<String, Integer> counts = new HashMap<>();
		for (Strand strand : strands) {
			record(seen, counts, strand.ax(), strand.ay(), strand.az());
			record(seen, counts, strand.bx(), strand.by(), strand.bz());
		}
		List<double[]> nodes = new ArrayList<>();
		counts.forEach((key, count) -> {
			if (count > 1) {
				nodes.add(seen.get(key));
			}
		});
		return nodes;
	}

	private static void record(Map<String, double[]> seen, Map<String, Integer> counts,
			double x, double y, double z) {
		String key = Math.round(x) + ":" + Math.round(y) + ":" + Math.round(z);
		seen.putIfAbsent(key, new double[] { x, y, z });
		counts.merge(key, 1, Integer::sum);
	}

	private static List<double[]> anchors(Random random, List<Lobe> lobes) {
		List<double[]> anchors = new ArrayList<>(ANCHOR_COUNT);
		int attempts = 0;
		while (anchors.size() < ANCHOR_COUNT && attempts++ < 4000) {
			double theta = random.nextDouble() * Math.PI * 2.0;
			double phi = Math.acos(2.0 * random.nextDouble() - 1.0);
			double radius = VagrantMindGeometry.OUTER_RADIUS * 0.6;
			double x = radius * Math.sin(phi) * Math.cos(theta);
			double y = radius * Math.cos(phi) * 0.6;
			double z = radius * Math.sin(phi) * Math.sin(theta);
			// Walk inward until the point sits just inside the shell.
			for (int step = 0; step < 40 && VagrantMindGeometry.field(lobes, x, y, z) < 1.0; step++) {
				x *= 0.95;
				y *= 0.95;
				z *= 0.95;
			}
			if (VagrantMindGeometry.field(lobes, x, y, z) >= 1.0) {
				anchors.add(new double[] { x, y, z });
			}
		}
		return anchors;
	}
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "*VagrantMindFiberWebTest"`
Expected: PASS, 4 tests.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/vagrantmind/VagrantMindFiberWeb.java src/test/java/com/vincenthuto/hemomancy/common/worldgen/structure/vagrantmind/VagrantMindFiberWebTest.java
git commit -m "feat(worldgen): add Vagrant Mind cavity fiber web"
```

### Task 9: The structure and its placement data

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/vagrantmind/VagrantMindStructure.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/vagrantmind/VagrantMindPiece.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/StructureInit.java`
- Create: `src/main/resources/data/hemomancy/worldgen/structure/vagrant_mind.json`
- Create: `src/main/resources/data/hemomancy/worldgen/structure_set/vagrant_mind.json`
- Create: `src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/vagrant_mind.json`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/worldgen/structure/vagrantmind/VagrantMindPlacementSourceTest.java`

**Interfaces:**
- Consumes: `VagrantMindGeometry` (Task 7), `VagrantMindFiberWeb` (Task 8), the block palette (Phase 1).
- Produces: `StructureInit.vagrant_mind`, and `VagrantMindStructure.CODEC`.

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.worldgen.structure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class VagrantMindPlacementSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath));
	}

	@Test
	void structureTypeIsRegistered() throws IOException {
		String structureInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/StructureInit.java");
		assertTrue(structureInit.contains("register(\"vagrant_mind\""),
				"the structure type must be registered");
		assertTrue(structureInit.contains("VagrantMindStructure.CODEC"),
				"registration must bind our codec");
	}

	@Test
	void placementSuitsFloatingEndIslands() throws IOException {
		String json = read("src/main/resources/data/hemomancy/worldgen/structure/vagrant_mind.json");
		assertTrue(json.contains("\"hemomancy:vagrant_mind\""), "structure type must be ours");
		assertTrue(json.contains("\"terrain_adaptation\": \"none\""),
				"End islands float — beardifier adaptation would smear terrain into the void");
		assertFalse(json.contains("project_start_to_heightmap"),
				"there is no surface to project onto in the End");
		assertTrue(json.contains("#hemomancy:has_structure/vagrant_mind"),
				"placement must be gated on the biome tag");
	}

	@Test
	void biomeTagTargetsTheDrift() throws IOException {
		assertTrue(read("src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/vagrant_mind.json")
				.contains("hemomancy:cortical_drift"), "the Mind only belongs in its own biome");
	}

	@Test
	void structureSetIsSparse() throws IOException {
		String json = read("src/main/resources/data/hemomancy/worldgen/structure_set/vagrant_mind.json");
		assertTrue(json.contains("\"spacing\""), "spacing must be declared");
		assertTrue(json.contains("random_spread"), "use random spread placement");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*VagrantMindPlacementSourceTest"`
Expected: FAIL — `NoSuchFileException`.

- [ ] **Step 3: Write the structure class**

Copy the codec shape from `DynastyCastleStructure` but drop the heightmap projection — the Mind sits at a fixed y-band above the islands.

```java
package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * The Vagrant Mind: one colossal hollow brain per biome patch. Unlike the surface structures in
 * this package it does not project onto a heightmap — End islands float, so the Mind is placed at
 * an absolute y-band and simply hangs there.
 */
public class VagrantMindStructure extends Structure {
	public static final MapCodec<VagrantMindStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					settingsCodec(instance),
					HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight))
			.apply(instance, VagrantMindStructure::new));

	private final HeightProvider startHeight;

	public VagrantMindStructure(StructureSettings settings, HeightProvider startHeight) {
		super(settings);
		this.startHeight = startHeight;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		int y = startHeight.sample(context.random(),
				new net.minecraft.world.level.levelgen.WorldGenerationContext(
						context.chunkGenerator(), context.heightAccessor()));
		BlockPos origin = new BlockPos(chunkPos.getMiddleBlockX(), y, chunkPos.getMiddleBlockZ());
		return Optional.of(new GenerationStub(origin,
				builder -> builder.addPiece(new VagrantMindPiece(origin, context.random().nextLong()))));
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.vagrant_mind.get();
	}
}
```

- [ ] **Step 4: Write the piece**

`VagrantMindPiece` extends `net.minecraft.world.level.levelgen.structure.StructurePiece`, stores `origin` and `seed`, and in `postProcess` iterates the chunk's intersection with its bounding box:

```java
	@Override
	public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
			RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pos) {
		List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(seed);
		BlockState folds = BlockInit.cortical_folds.get().defaultBlockState();
		BlockState tissue = BlockInit.neural_tissue.get().defaultBlockState();

		for (int x = chunkBox.minX(); x <= chunkBox.maxX(); x++) {
			for (int z = chunkBox.minZ(); z <= chunkBox.maxZ(); z++) {
				for (int y = chunkBox.minY(); y <= chunkBox.maxY(); y++) {
					double lx = x - origin.getX();
					double ly = y - origin.getY();
					double lz = z - origin.getZ();
					if (VagrantMindGeometry.isFissure(lx, lz, 3.0) && ly > 0) {
						continue;
					}
					if (!VagrantMindGeometry.isShell(lobes, lx, ly, lz, 8.0)) {
						continue;
					}
					// Ridged gyri banding picks folds over plain tissue on the outer skin.
					boolean ridge = (Mth.floor(ly + Math.sin(lx * 0.18) * 3.0 + Math.cos(lz * 0.18) * 3.0) % 5) < 2;
					level.setBlock(new BlockPos(x, y, z), ridge ? folds : tissue, 2);
				}
			}
		}
		placeFiberWeb(level, chunkBox, lobes);
	}
```

`placeFiberWeb` walks `VagrantMindFiberWeb.strands(seed, lobes)`, converts each `Strand` to world coordinates, steps it with `VoxelBresenhamIterator` (from `com.vincenthuto.hemomancy.common.worldgen.feature`) applying a midpoint droop of up to 6 blocks, and places `nerve_bundle` for `thick` strands and `nerve_fiber` otherwise — skipping any position outside `chunkBox`. Then it places `synaptic_node` at every `VagrantMindFiberWeb.nodes(strands)` position inside the box.

Register in `StructureInit` beside the others:

```java
	public static final DeferredHolder<StructureType<?>, StructureType<VagrantMindStructure>> vagrant_mind = STRUCTURES
			.register("vagrant_mind", () -> explicitStructureTypeTyping(VagrantMindStructure.CODEC));
```

- [ ] **Step 5: Write the placement data**

`src/main/resources/data/hemomancy/worldgen/structure/vagrant_mind.json`:

```json
{
  "type": "hemomancy:vagrant_mind",
  "start_height": {
    "type": "minecraft:uniform",
    "min_inclusive": { "absolute": 96 },
    "max_inclusive": { "absolute": 140 }
  },
  "terrain_adaptation": "none",
  "biomes": "#hemomancy:has_structure/vagrant_mind",
  "step": "surface_structures",
  "spawn_overrides": {}
}
```

`src/main/resources/data/hemomancy/worldgen/structure_set/vagrant_mind.json`:

```json
{
  "structures": [
    { "structure": "hemomancy:vagrant_mind", "weight": 1 }
  ],
  "placement": {
    "type": "minecraft:random_spread",
    "spacing": 96,
    "separation": 48,
    "salt": 1947283
  }
}
```

`src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/vagrant_mind.json`:

```json
{
  "replace": false,
  "values": [
    "hemomancy:cortical_drift"
  ]
}
```

- [ ] **Step 6: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "*VagrantMindPlacementSourceTest"`
Expected: PASS, 4 tests.

- [ ] **Step 7: Verify in-game**

Run: `./gradlew.bat runClient`

In the End: `/locate structure hemomancy:vagrant_mind`, then teleport to it. Expected: a hollow folded brain 150–250 blocks across, split by a sagittal fissure, with fibers strung across the interior and lit nodes at junctions. Fly inside to confirm the cavity is enterable and not solid.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/VagrantMind*.java src/main/java/com/vincenthuto/hemomancy/common/init/StructureInit.java src/main/resources/data/hemomancy/worldgen/structure/vagrant_mind.json src/main/resources/data/hemomancy/worldgen/structure_set/vagrant_mind.json src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/vagrant_mind.json src/test/java/com/vincenthuto/hemomancy/common/worldgen/structure/vagrantmind/VagrantMindPlacementSourceTest.java
git commit -m "feat(worldgen): generate the Vagrant Mind structure"
```

---

# Phase 4 — Island nerve bridge network

Every island gets at least one fiber connection, with hub islands carrying several. The plan is region-seeded so cables never break at chunk boundaries.

### Task 10: The network planner (pure logic)

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalNetworkPlan.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalNetworkPlanTest.java`

**Interfaces:**
- Produces:
  - `record Island(int x, int y, int z)`
  - `record Cable(Island from, Island to, boolean bundle)`
  - `CorticalNetworkPlan.connect(List<Island> islands, long seed)` → `List<Cable>`
  - `CorticalNetworkPlan.sag(Cable cable, double t)` → `double[]` world position at `t` in `[0,1]`

- [ ] **Step 1: Write the failing test**

Connectivity is the requirement worth testing hardest — "all other dispersed islands connected" is the spec's core promise.

```java
package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincenthuto.hemomancy.common.worldgen.terrain.cortical.CorticalNetworkPlan.Cable;
import com.vincenthuto.hemomancy.common.worldgen.terrain.cortical.CorticalNetworkPlan.Island;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CorticalNetworkPlanTest {
	private static List<Island> islands(int count, long seed) {
		java.util.Random random = new java.util.Random(seed);
		List<Island> islands = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			islands.add(new Island(random.nextInt(2000) - 1000, 60 + random.nextInt(40),
					random.nextInt(2000) - 1000));
		}
		return islands;
	}

	@Test
	void everyIslandIsReachable() {
		for (long seed = 0; seed < 20; seed++) {
			List<Island> islands = islands(12, seed);
			List<Cable> cables = CorticalNetworkPlan.connect(islands, seed);

			Map<Island, List<Island>> adjacency = new HashMap<>();
			for (Cable cable : cables) {
				adjacency.computeIfAbsent(cable.from(), key -> new ArrayList<>()).add(cable.to());
				adjacency.computeIfAbsent(cable.to(), key -> new ArrayList<>()).add(cable.from());
			}

			Set<Island> visited = new HashSet<>();
			Deque<Island> queue = new ArrayDeque<>();
			queue.add(islands.get(0));
			visited.add(islands.get(0));
			while (!queue.isEmpty()) {
				for (Island next : adjacency.getOrDefault(queue.poll(), List.of())) {
					if (visited.add(next)) {
						queue.add(next);
					}
				}
			}
			assertEquals(islands.size(), visited.size(),
					"seed " + seed + " left islands unconnected — the spec requires every island wired");
		}
	}

	@Test
	void hubIslandsCarryMultipleCables() {
		List<Cable> cables = CorticalNetworkPlan.connect(islands(12, 5L), 5L);
		Map<Island, Integer> degree = new HashMap<>();
		for (Cable cable : cables) {
			degree.merge(cable.from(), 1, Integer::sum);
			degree.merge(cable.to(), 1, Integer::sum);
		}
		assertTrue(degree.values().stream().anyMatch(count -> count >= 3),
				"at least one hub should fan out, matching the reference imagery");
	}

	@Test
	void planIsDeterministic() {
		assertEquals(CorticalNetworkPlan.connect(islands(10, 3L), 3L),
				CorticalNetworkPlan.connect(islands(10, 3L), 3L),
				"chunks are generated independently, so the plan must be reproducible");
	}

	@Test
	void cablesSagInTheMiddle() {
		Island a = new Island(0, 80, 0);
		Island b = new Island(100, 80, 0);
		Cable cable = new Cable(a, b, false);
		assertTrue(CorticalNetworkPlan.sag(cable, 0.5)[1] < 80.0, "the midpoint must hang below the endpoints");
		assertEquals(80.0, CorticalNetworkPlan.sag(cable, 0.0)[1], 0.001, "endpoints are anchored");
		assertEquals(80.0, CorticalNetworkPlan.sag(cable, 1.0)[1], 0.001, "endpoints are anchored");
	}

	@Test
	void singleIslandProducesNoCables() {
		assertTrue(CorticalNetworkPlan.connect(islands(1, 0L), 0L).isEmpty(),
				"a lone island cannot be wired to anything and must not crash");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*CorticalNetworkPlanTest"`
Expected: FAIL — cannot resolve `CorticalNetworkPlan`.

- [ ] **Step 3: Implement the planner**

```java
package com.vincenthuto.hemomancy.common.worldgen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Region-seeded plan for the fiber bridges between Cortical Drift islands. Deterministic from the
 * island list plus the region seed so independently generated chunks agree on every cable, the
 * same contract PhlegethonticVeinPath relies on.
 */
public final class CorticalNetworkPlan {
	private static final int EXTRA_EDGES = 3;
	private static final double SAG_RATIO = 0.12;

	public record Island(int x, int y, int z) {}

	public record Cable(Island from, Island to, boolean bundle) {}

	private CorticalNetworkPlan() {}

	public static List<Cable> connect(List<Island> islands, long seed) {
		List<Cable> cables = new ArrayList<>();
		if (islands.size() < 2) {
			return cables;
		}
		Random random = new Random(seed ^ 0x4E37D2L);

		// Prim's algorithm: a minimum spanning tree guarantees every island is reachable.
		Set<Integer> connected = new HashSet<>();
		connected.add(0);
		while (connected.size() < islands.size()) {
			int bestFrom = -1;
			int bestTo = -1;
			double bestDistance = Double.MAX_VALUE;
			for (int from : connected) {
				for (int to = 0; to < islands.size(); to++) {
					if (connected.contains(to)) {
						continue;
					}
					double distance = distanceSquared(islands.get(from), islands.get(to));
					if (distance < bestDistance) {
						bestDistance = distance;
						bestFrom = from;
						bestTo = to;
					}
				}
			}
			connected.add(bestTo);
			cables.add(new Cable(islands.get(bestFrom), islands.get(bestTo), random.nextInt(3) == 0));
		}

		// A few extra edges turn well-placed islands into the multi-bundle hubs in the references.
		for (int i = 0; i < EXTRA_EDGES && islands.size() > 2; i++) {
			int from = random.nextInt(islands.size());
			int to = random.nextInt(islands.size());
			if (from != to) {
				cables.add(new Cable(islands.get(from), islands.get(to), random.nextInt(2) == 0));
			}
		}
		return cables;
	}

	/** Position along a cable at {@code t} in [0,1], hanging in a parabolic catenary approximation. */
	public static double[] sag(Cable cable, double t) {
		Island from = cable.from();
		Island to = cable.to();
		double dx = to.x() - from.x();
		double dz = to.z() - from.z();
		double span = Math.sqrt(dx * dx + dz * dz);
		double droop = span * SAG_RATIO * (4.0 * t * (1.0 - t));
		return new double[] {
				from.x() + dx * t,
				from.y() + (to.y() - from.y()) * t - droop,
				from.z() + dz * t };
	}

	private static double distanceSquared(Island a, Island b) {
		double dx = a.x() - b.x();
		double dy = a.y() - b.y();
		double dz = a.z() - b.z();
		return dx * dx + dy * dy + dz * dz;
	}
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "*CorticalNetworkPlanTest"`
Expected: PASS, 5 tests.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalNetworkPlan.java src/test/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalNetworkPlanTest.java
git commit -m "feat(worldgen): add Cortical Drift island network planner"
```

### Task 11: The bridge feature

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/NerveBridgeFeature.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/FeatureInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ConfiguredFeatureInit.java` and `PlacedFeatureInit.java` (match whatever names these hold in this repo)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/BiomeInit.java` — add the placed feature to `corticalDrift`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/worldgen/NerveBridgeFeatureSourceTest.java`

**Interfaces:**
- Consumes: `CorticalNetworkPlan.connect(...)` and `sag(...)` (Task 10); `BlockInit.nerve_fiber`, `nerve_bundle`, `synaptic_node`, `neural_tissue`, `axonal_slate`, `ganglion_matter` (Phase 1).
- Produces: `FeatureInit.NERVE_BRIDGE`, and a placed feature added to the Cortical Drift biome.

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class NerveBridgeFeatureSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath));
	}

	@Test
	void featureDrawsFromTheRegionPlan() throws IOException {
		String source = read(
				"src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/NerveBridgeFeature.java");
		assertTrue(source.contains("CorticalNetworkPlan.connect"),
				"cables must come from the shared region plan, not per-chunk randomness");
		assertTrue(source.contains("CorticalNetworkPlan.sag"), "cables must hang, not run straight");
		assertTrue(source.contains("regionSeed") || source.contains("REGION_SIZE"),
				"the plan must be keyed to a region so neighbouring chunks agree");
	}

	@Test
	void anchorsUseTheFullPalette() throws IOException {
		String source = read(
				"src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/NerveBridgeFeature.java");
		for (String block : new String[] { "neural_tissue", "axonal_slate", "ganglion_matter",
				"synaptic_node" }) {
			assertTrue(source.contains(block), "anchor masses must use " + block);
		}
	}

	@Test
	void featureIsWiredIntoTheBiome() throws IOException {
		String biomeInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/BiomeInit.java");
		int index = biomeInit.indexOf("private static Biome corticalDrift(");
		assertTrue(index > 0, "the Cortical Drift builder must exist");
		assertTrue(biomeInit.substring(index, Math.min(index + 2500, biomeInit.length()))
				.contains("NERVE_BRIDGE"), "the bridge feature must be added to the biome");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*NerveBridgeFeatureSourceTest"`
Expected: FAIL — `NoSuchFileException`.

- [ ] **Step 3: Implement the feature**

`NerveBridgeFeature extends Feature<NoneFeatureConfiguration>`. In `place(FeaturePlaceContext<NoneFeatureConfiguration> context)`:

1. Compute the region: `REGION_SIZE = 512`; `regionX = Math.floorDiv(origin.getX(), REGION_SIZE)`, same for Z; `regionSeed = level.getSeed() ^ (regionX * 341873128712L + regionZ * 132897987541L)`.
2. Build the island list for this region and its eight neighbours (so cables crossing a region border are drawn from both sides): sample a fixed 6×6 lattice of candidate positions per region, jittered by the region seed, and query `level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z)`. Keep positions whose height is above the void floor — those are islands.
3. `List<Cable> cables = CorticalNetworkPlan.connect(islands, regionSeed);`
4. For each cable, step `t` from 0 to 1 in increments of `1.0 / (span * 2)`, call `CorticalNetworkPlan.sag(cable, t)`, and **skip any position outside the current chunk** — this is what makes the feature safe to run per-chunk while producing continuous cables.
5. Place `nerve_bundle` for `cable.bundle()`, otherwise `nerve_fiber`, setting the `RotatedPillarBlock.AXIS` property to the dominant axis of travel so the texture runs along the cable.
6. At each endpoint inside the chunk, generate the anchor: a radius-3 blob of `neural_tissue` with `axonal_slate` on its lower half and `ganglion_matter` veins where `(x + y + z) % 5 == 0`, capped with one `synaptic_node` at the exact terminus.

Register the feature in `FeatureInit`, wrap it in a configured and placed feature following the existing `VENOUS_RIDGE` / `HYPHAE_TENDRIL` entries, and add `PlacedFeatureInit.NERVE_BRIDGE` to the Cortical Drift biome builder at `GenerationStep.Decoration.SURFACE_STRUCTURES`.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "*NerveBridgeFeatureSourceTest"`
Expected: PASS, 3 tests.

- [ ] **Step 5: Verify in-game**

Run: `./gradlew.bat runClient`

Fly a Cortical Drift patch. Expected: every island has at least one cable; some have three or more; cables sag between anchors and are **continuous across chunk borders** — fly along one at chunk boundaries and confirm no gaps or jogs. Anchors show the black/blue/yellow/white palette.

If cables break at chunk borders, the region seed is being derived from chunk position rather than region position — recheck step 1.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/NerveBridgeFeature.java src/main/java/com/vincenthuto/hemomancy/common/init src/test/java/com/vincenthuto/hemomancy/common/worldgen/NerveBridgeFeatureSourceTest.java
git commit -m "feat(worldgen): wire Cortical Drift islands with nerve bridges"
```

---

# Phase 5 — Nerve lightning, ambience, and documentation

### Task 12: Ambient arc manager

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalArcManager.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/Hemomancy.java` — register the event subscriber if the class is not `@EventBusSubscriber`-annotated
- Test: `src/test/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalArcManagerSourceTest.java`

**Interfaces:**
- Consumes: `LightningTesterSpawner.spawn(ServerLevel, Vec3 start, Vec3 end, LightningTestConfig)`, `NerveLinkPacket(TendrilAnchor start, TendrilAnchor end, int ticks)`, `TendrilAnchor.Point(Vec3)`, `BlockInit.synaptic_node`.
- Produces: `CorticalArcManager.tick(ServerTickEvent.Post)`.

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CorticalArcManagerSourceTest {
	private static String source() throws IOException {
		return Files.readString(Path.of("").toAbsolutePath()
				.resolve("src/main/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalArcManager.java"));
	}

	@Test
	void reusesTheAuthoredLightningToolkit() throws IOException {
		String source = source();
		assertTrue(source.contains("LightningTesterSpawner.spawn"),
				"AGENTS.md requires the authored lightning suite, not vanilla particles");
		assertTrue(source.contains("NerveLinkPacket"),
				"long bridges also carry the tendril filament overlay");
		assertFalse(source.contains("ParticleTypes.ELECTRIC_SPARK"),
				"vanilla particle clouds are a fallback, not the default");
	}

	@Test
	void arcsAreBudgeted() throws IOException {
		String source = source();
		assertTrue(source.contains("ARC_INTERVAL") || source.contains("% "),
				"arcs must fire on an interval, not every tick");
		assertTrue(source.contains("MAX_ARCS_PER_TICK") || source.contains("MAX_ARCS"),
				"a per-tick cap keeps a dense network from flooding the client");
		assertTrue(source.contains("isLoaded") || source.contains("hasChunk"),
				"never scan unloaded chunks");
	}

	@Test
	void onlyRunsInTheDrift() throws IOException {
		assertTrue(source().contains("CORTICAL_DRIFT"),
				"the manager must not tick in unrelated dimensions or biomes");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*CorticalArcManagerSourceTest"`
Expected: FAIL — `NoSuchFileException`.

- [ ] **Step 3: Implement the manager**

Follow `ConductionManager`'s structure. Every `ARC_INTERVAL = 20` ticks, for each `ServerPlayer` in a level whose dimension is `Level.END`:

1. Skip unless the player's current biome is `BiomeInit.CORTICAL_DRIFT`.
2. Scan a 32-block cube around the player for `BlockInit.synaptic_node`, using `level.hasChunk(...)` before touching any position and stopping once 12 nodes are collected.
3. Pair each node with its nearest other node within 48 blocks.
4. For up to `MAX_ARCS_PER_TICK = 3` pairs, call:

```java
LightningTesterSpawner.spawn(level, Vec3.atCenterOf(a), Vec3.atCenterOf(b), NERVE_ARC_CONFIG);
```

where `NERVE_ARC_CONFIG` is a shared `LightningTestConfig` built once from `LightningTestConfig.defaults()` with the violet-white outer and pale inner colours used by `DuctilisLightningEffects`, so biome ambience and manipulation casts read as the same phenomenon.

5. For pairs further than 24 blocks apart, also send:

```java
PacketHandler.sendToPlayer(new NerveLinkPacket(
        new TendrilAnchor.Point(Vec3.atCenterOf(a)),
        new TendrilAnchor.Point(Vec3.atCenterOf(b)), 40), player);
```

Match the existing send helper's exact name in `PacketHandler`.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "*CorticalArcManagerSourceTest"`
Expected: PASS, 3 tests.

- [ ] **Step 5: Verify in-game**

Run: `./gradlew.bat runClient`

Stand in a Cortical Drift patch near a bridge. Expected: arcs crackle between synaptic nodes roughly once a second, visually identical to a Ferric Ductilis cast; long bridges carry a soft glowing filament. Check the F3 graph — no frame-time spike, and no lag when standing beside a dense hub.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalArcManager.java src/test/java/com/vincenthuto/hemomancy/common/worldgen/terrain/cortical/CorticalArcManagerSourceTest.java
git commit -m "feat(worldgen): make Cortical Drift nerve fibers spark and crackle"
```

### Task 13: GameTest coverage

**Files:**
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/CorticalDriftGameTests.java`

**Interfaces:**
- Consumes: everything from Phases 1–4.

- [ ] **Step 1: Write the GameTests**

Following the existing `FerricDuctilisObserverGameTests` structure, add:

- `synapticNodeConductsDuctilis` — place a `synaptic_node`, confirm it matches the `ductilis_conductors` block tag.
- `nerveBridgeSegmentsAreAxisAligned` — place `nerve_fiber` along each axis, assert the `AXIS` property survives.
- `vagrantMindCavityIsHollow` — construct a small `VagrantMindPiece` in a test structure block area and assert the centre is air while the shell is solid.

- [ ] **Step 2: Run the GameTests**

Run: `./gradlew.bat gameTestServer`
Expected: all Cortical Drift tests pass.

- [ ] **Step 3: Commit**

```bash
git add src/gameTest/java/com/vincenthuto/hemomancy/gametest/CorticalDriftGameTests.java
git commit -m "test(worldgen): add Cortical Drift game tests"
```

### Task 14: Documentation

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md`
- Modify: `docs/LORE_REFERENCE.md`
- Create: `wiki/Cortical-Drift.md`
- Modify: `wiki/Home.md`

**Interfaces:**
- Consumes: the finished feature. `AGENTS.md` makes this a hard requirement, not a nicety.

- [ ] **Step 1: Update the mechanics reference**

In `docs/HEMOMANCY_REFERENCE.md`, add a Cortical Drift section covering: where the biome generates (End, TerraBlender highlands/midlands/island, config-gated and weight-tunable), the ten-block palette and what each is for, the Vagrant Mind and its enterable cavity, the bridge network, and the Ferric Ductilis interaction — `synaptic_node` is a conductor, so conduction manipulations chain along bridges.

- [ ] **Step 2: Update the lore reference**

In `docs/LORE_REFERENCE.md`, add the Vagrant Mind: a dormant organ, not a deity — nervous tissue the infection never reached, or something that thought too long and stopped. Give the Hematic Order reading (preserved memory to be tapped) and the Lethean reading (something that finally went quiet). Keep Harbinger vocabulary (`Hematic Order`, `Sanguine Brotherhood`, `Crimson Lodge`) and Unstained still-water language distinct. No God/Satan framing.

- [ ] **Step 3: Write the wiki page**

Create `wiki/Cortical-Drift.md` following the structure of the existing `wiki/Ferric-Ductilis.md`: overview, how to find it, blocks and what they drop, the Vagrant Mind, the bridge network, and the Ductilis synergy. Link it from `wiki/Home.md` beside the other biome and school pages.

- [ ] **Step 4: Run the full gate**

Run: `./gradlew.bat alphaCheck`
Expected: the combined JVM and dedicated GameTest alpha gate passes.

- [ ] **Step 5: Commit**

```bash
git add docs/HEMOMANCY_REFERENCE.md docs/LORE_REFERENCE.md wiki/Cortical-Drift.md wiki/Home.md
git commit -m "docs: document the Cortical Drift biome and the Vagrant Mind"
```

---

## Deferred (explicitly out of scope)

- A nerve-native End mob or boss. Vesper the Evening Star already holds endgame-boss space.
- Any change to the vanilla central End island or the dragon fight.
- The fallback `TheEndBiomeSource` mixin. Only revisit if playtesting shows the midlands seam is visually unacceptable — see spec §2.1.
