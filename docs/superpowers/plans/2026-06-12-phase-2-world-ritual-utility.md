# Phase 2 World Ritual Utility Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add direct Blood Projection growth for Bloodwood trees from vanilla dead bushes, plus Mnemonic Candles as a lit ritual utility block that improves manipulation cooldowns and blood regeneration nearby.

**Architecture:** Bloodwood growth plugs into the existing `BlockBloodInteractions` / `BlockBloodEndpoint` style by adding a projection helper that handles vanilla dead bushes before legacy projection fallback. The tree is generated immediately with existing `blood_wood_log` plus new `blood_wood_leaves`, using deterministic shape rules that can be tested without a live world. Mnemonic Candles use a standard placeable block with `LIT` state and a server tick aura that grants a short `mnemonic_candle_aura` effect; existing cooldown and blood-regeneration paths read that effect through small rule helpers.

**Tech Stack:** Java 21, NeoForge 1.21.1, Minecraft block/resource JSON, existing Hemomancy registries, plain Java sentinel tests, PowerShell-generated PNG textures.

---

### Task 1: Add Failing Rule And Resource Tests

**Files:**
- Create: `src/test/java/com/vincenthuto/hemomancy/common/block/shared/BloodwoodGrowthRulesTest.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/effect/MnemonicCandleRulesTest.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/resource/PhaseTwoWorldRitualResourceTest.java`

- [ ] **Step 1: Write tests for Bloodwood shape rules**

```java
package com.vincenthuto.hemomancy.common.block.shared;

public final class BloodwoodGrowthRulesTest {
	private BloodwoodGrowthRulesTest() {}

	public static void main(String[] args) {
		assertEquals(900.0D, BloodwoodGrowthRules.PROJECTION_BLOOD_COST, "projection cost");
		assertTrue(BloodwoodGrowthRules.requiredClearance().size() > 80, "clearance footprint should cover tree volume");
		assertTrue(BloodwoodGrowthRules.logOffsets().size() >= 18, "tree should have trunk and branches");
		assertTrue(BloodwoodGrowthRules.leafOffsets().size() >= 70, "tree should have broad dragon-tree canopy");
		assertFalse(BloodwoodGrowthRules.logOffsets().stream().anyMatch(BloodwoodGrowthRules.leafOffsets()::contains),
				"logs and leaves should not overlap");
	}

	private static void assertEquals(double expected, double actual, String label) {
		if (Math.abs(expected - actual) > 0.001D) throw new AssertionError(label + ": expected " + expected + " got " + actual);
	}

	private static void assertTrue(boolean value, String label) {
		if (!value) throw new AssertionError(label);
	}

	private static void assertFalse(boolean value, String label) {
		if (value) throw new AssertionError(label);
	}
}
```

- [ ] **Step 2: Write tests for candle multipliers**

```java
package com.vincenthuto.hemomancy.common.effect;

public final class MnemonicCandleRulesTest {
	private MnemonicCandleRulesTest() {}

	public static void main(String[] args) {
		assertEquals(80, MnemonicCandleRules.AURA_INTERVAL_TICKS, "aura interval");
		assertEquals(120, MnemonicCandleRules.AURA_DURATION_TICKS, "aura duration");
		assertEquals(0.90D, MnemonicCandleRules.manipulationCooldownMultiplier(true), "cooldown aura");
		assertEquals(1.0D, MnemonicCandleRules.manipulationCooldownMultiplier(false), "no aura cooldown");
		assertEquals(1.5D, MnemonicCandleRules.bonusBloodRegenPerTick(true), "regen aura");
		assertEquals(0.0D, MnemonicCandleRules.bonusBloodRegenPerTick(false), "no aura regen");
	}

	private static void assertEquals(double expected, double actual, String label) {
		if (Math.abs(expected - actual) > 0.001D) throw new AssertionError(label + ": expected " + expected + " got " + actual);
	}

	private static void assertEquals(int expected, int actual, String label) {
		if (expected != actual) throw new AssertionError(label + ": expected " + expected + " got " + actual);
	}
}
```

- [ ] **Step 3: Write resource tests**

```java
package com.vincenthuto.hemomancy.common.resource;

import java.nio.file.Files;
import java.nio.file.Path;

public final class PhaseTwoWorldRitualResourceTest {
	private static final Path ROOT = Path.of("src/main/resources");
	private static final Path DATA = ROOT.resolve("data/hemomancy");
	private static final Path ASSETS = ROOT.resolve("assets/hemomancy");

	private PhaseTwoWorldRitualResourceTest() {}

	public static void main(String[] args) throws Exception {
		for (String id : new String[] { "mnemonic_candle", "blood_wood_leaves" }) {
			require(ASSETS.resolve("blockstates/" + id + ".json"));
			require(ASSETS.resolve("models/block/" + id + ".json"));
			require(ASSETS.resolve("models/item/" + id + ".json"));
			require(ASSETS.resolve("textures/block/" + id + ".png"));
		}
		require(ASSETS.resolve("textures/item/mnemonic_candle.png"));
		require(ASSETS.resolve("textures/mob_effect/mnemonic_candle_aura.png"));
		require(DATA.resolve("recipe/mnemonic_candle.json"));
		require(DATA.resolve("loot_table/blocks/mnemonic_candle.json"));
		require(DATA.resolve("loot_table/blocks/blood_wood_leaves.json"));
	}

	private static void require(Path path) {
		if (!Files.exists(path)) throw new AssertionError("missing " + path);
	}
}
```

- [ ] **Step 4: Run tests to verify RED**

Run:
`./gradlew.bat testClasses --no-daemon`

Expected: compile fails because `BloodwoodGrowthRules` and `MnemonicCandleRules` do not exist yet.

---

### Task 2: Implement Bloodwood Direct Projection Growth

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/block/shared/BloodwoodGrowthRules.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/block/shared/BloodwoodGrowthHandler.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/block/shared/BlockBloodInteractions.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java`

- [ ] **Step 1: Add deterministic tree offsets**

Create `BloodwoodGrowthRules` with:
- `PROJECTION_BLOOD_COST = 900.0D`
- `requiredClearance()`, `logOffsets()`, and `leafOffsets()` returning immutable `Set<BlockPos>`
- a jagged central trunk, 4-5 uneven upward branches, and a broad flattened crown inspired by Socotra dragon trees.

- [ ] **Step 2: Add growth handler**

Create `BloodwoodGrowthHandler.tryGrowFromProjection(ServerLevel level, BlockPos deadBushPos, ServerPlayer player, double maxAmount)`:
- require target block `Blocks.DEAD_BUSH`
- require active player blood volume
- require `maxAmount >= PROJECTION_BLOOD_COST`
- verify every clearance block is replaceable or the original dead bush
- drain exactly `PROJECTION_BLOOD_COST`
- replace dead bush area with `blood_wood_log` and `blood_wood_leaves`
- send success/failure messages
- return consumed blood amount, or `0.0D` if nothing happened.

- [ ] **Step 3: Route Blood Projection before endpoint fallback**

In `BlockBloodInteractions.tryProjectIntoLookedAtBlock`, after finding the looked-at block position but before checking `BlockBloodEndpoint`, call `BloodwoodGrowthHandler.tryGrowFromProjection(...)`.

- [ ] **Step 4: Register `blood_wood_leaves`**

Add `blood_wood_leaves` to `BlockInit` as a leaf block using `LeavesBlock`, no occlusion, correct sound, and random ticking compatible with vanilla leaf behavior.

---

### Task 3: Implement Mnemonic Candle Aura

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/block/shared/MnemonicCandleBlock.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/effect/MnemonicCandleAuraEffect.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/effect/MnemonicCandleRules.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/effect/MnemonicCandleAuraEvents.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/EffectInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/BloodVolumeEvents.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java`

- [ ] **Step 1: Add rule helper and effect**

`MnemonicCandleRules` defines aura radius, interval, duration, cooldown multiplier, and bonus blood regen. `MnemonicCandleAuraEffect` is a beneficial effect with amber-crimson color.

- [ ] **Step 2: Add block**

`MnemonicCandleBlock` uses `LIT` and `WATERLOGGED`, toggles on right-click, emits light only when lit, and uses a slightly larger-than-vanilla candle voxel shape.

- [ ] **Step 3: Add aura tick event**

Every `AURA_INTERVAL_TICKS`, each lit candle in range grants `mnemonic_candle_aura` to active Harbinger players. Use a small player-centered search radius to avoid level-wide scanning.

- [ ] **Step 4: Hook mechanics**

`BloodManipulation.startCooldown` multiplies by `MnemonicCandleRules.manipulationCooldownMultiplier(player.hasEffect(EffectInit.mnemonic_candle_aura))`.

`BloodVolumeEvents.playerTick` adds `MnemonicCandleRules.bonusBloodRegenPerTick(player.hasEffect(EffectInit.mnemonic_candle_aura))` when blood volume is active and not full.

---

### Task 4: Add Resources, Textures, Docs, And Verification

**Files:**
- Add recipes, loot tables, blockstates, models, lang, block/item/effect textures.
- Modify: `docs/HEMOMANCY_REFERENCE.md`

- [ ] **Step 1: Add JSON resources**

Add `mnemonic_candle` recipe using `mnemonic_ambergris`, `desiccated_membrane`, and vanilla candle. Add block/item models, blockstates, loot tables for `mnemonic_candle` and `blood_wood_leaves`.

- [ ] **Step 2: Generate PNG textures**

Generate 16x16 textures for:
- `textures/block/mnemonic_candle.png`
- `textures/item/mnemonic_candle.png`
- `textures/block/blood_wood_leaves.png`
- `textures/mob_effect/mnemonic_candle_aura.png`

- [ ] **Step 3: Update docs**

Document direct Blood Projection into dead bushes, Bloodwood leaves, and Mnemonic Candle aura behavior in `docs/HEMOMANCY_REFERENCE.md`.

- [ ] **Step 4: Verify**

Run:
`./gradlew.bat build --no-daemon`

Run:
`java -cp "build/classes/java/test;build/classes/java/main" com.vincenthuto.hemomancy.common.block.shared.BloodwoodGrowthRulesTest`
`java -cp "build/classes/java/test;build/classes/java/main" com.vincenthuto.hemomancy.common.effect.MnemonicCandleRulesTest`
`java -cp build/classes/java/test com.vincenthuto.hemomancy.common.resource.PhaseTwoWorldRitualResourceTest`

Expected: all commands exit 0.
