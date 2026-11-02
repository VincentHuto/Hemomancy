# Harbinger Voyager NPCs Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add dialogue-first active-vessel NPCs: one guaranteed Harbinger Voyager captain-scholar and a 1-in-5 Harbinger Votary Wayfarer companion spawn rule.

**Architecture:** Implement the NPCs as structure-only Harbinger dialogue entities following the existing Vicar/Alchemist/Mnemonist patterns. Add a small pure rules class for the 1-in-5 companion roll, register entity types/attributes/renderers/spawn eggs, and document an `ActiveHarbingerVoyagerNpcSpawner` helper for future active vessel `afterPlace()` integration.

**Tech Stack:** Java 21, Minecraft 1.21.1, NeoForge deferred registries, existing `DialogueTree`/payload dialogue system, JSON assets/resources, PowerShell/Gradle verification.

---

### Task 1: Spawn Rule And Tests

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/voyager/ActiveHarbingerVoyagerNpcRules.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/worldgen/structure/voyager/ActiveHarbingerVoyagerNpcRulesTest.java`

- [ ] **Step 1: Write the failing test**

Create a main-method test that proves the Voyager always spawns and the companion appears only when the roll is zero:

```java
package com.vincenthuto.hemomancy.common.worldgen.structure;

final class ActiveHarbingerVoyagerNpcRulesTest {
	public static void main(String[] args) {
		voyagerAlwaysSpawns();
		votaryUsesOneInFiveRoll();
	}

	private static void voyagerAlwaysSpawns() {
		assertTrue(ActiveHarbingerVoyagerNpcRules.shouldSpawnVoyager(),
				"active voyager vessels should always spawn a Harbinger Voyager");
	}

	private static void votaryUsesOneInFiveRoll() {
		assertTrue(ActiveHarbingerVoyagerNpcRules.shouldSpawnVotaryWayfarer(0),
				"roll 0 should spawn the Votary Wayfarer companion");
		for (int roll = 1; roll < ActiveHarbingerVoyagerNpcRules.VOTARY_WAYFARER_CHANCE; roll++) {
			assertFalse(ActiveHarbingerVoyagerNpcRules.shouldSpawnVotaryWayfarer(roll),
					"non-zero companion rolls should not spawn the Votary Wayfarer");
		}
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void assertFalse(boolean condition, String message) {
		assertTrue(!condition, message);
	}
}
```

- [ ] **Step 2: Run the failing test**

Run: `./gradlew.bat compileTestJava`

Expected: compile failure because `ActiveHarbingerVoyagerNpcRules` does not exist.

- [ ] **Step 3: Implement the pure rule**

```java
package com.vincenthuto.hemomancy.common.worldgen.structure;

public final class ActiveHarbingerVoyagerNpcRules {
	public static final int VOTARY_WAYFARER_CHANCE = 5;

	private ActiveHarbingerVoyagerNpcRules() {
	}

	public static boolean shouldSpawnVoyager() {
		return true;
	}

	public static boolean shouldSpawnVotaryWayfarer(int roll) {
		return roll == 0;
	}
}
```

- [ ] **Step 4: Run the rule test**

Run: `./gradlew.bat compileJava compileTestJava`

Then run:

`java -cp "build/classes/java/main;build/classes/java/test" com.vincenthuto.hemomancy.common.worldgen.structure.voyager.ActiveHarbingerVoyagerNpcRulesTest`

Expected: exit 0.

### Task 2: NPC Entities And Dialogue Trees

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVoyagerEntity.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVotaryWayfarerEntity.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVoyagerDialogueTrees.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVotaryWayfarerDialogueTrees.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java`

- [ ] **Step 1: Implement two passive Harbinger dialogue entities**

Follow the `HarbingerAlchemistEntity` / `HarbingerMnemonistEntity` interaction pattern: `PathfinderMob`, basic look/random goals, persistent structure-style NPC behavior, item inquiry first, then clarity/purifying/degree branches.

- [ ] **Step 2: Implement dialogue tree factories**

Add concise trees with these branch methods:

```java
public static DialogueTree itemInquiry(ItemStack held, int degree, int entityId)
public static DialogueTree clarity(int entityId)
public static DialogueTree purifying(int entityId)
public static DialogueTree forDegree(int degree, int entityId)
```

Voyager lines should read as captain-scholar research observations. Votary lines should read as apprentice field-study reactions.

- [ ] **Step 3: Register entities and attributes**

Register:

```java
harbinger_voyager
harbinger_votary_wayfarer
```

Both use `MobCategory.CREATURE`, no ordinary spawn placement, and `createMobAttributes()` with modest health, movement speed, and follow range.

### Task 3: Client Renderer, Spawn Eggs, Lang, And Assets

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/npc/harbinger/HarbingerVoyagerRenderer.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/npc/harbinger/HarbingerVotaryWayfarerRenderer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java`
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`
- Add item model JSONs for both spawn eggs.
- Add placeholder-polished 64x64 entity textures.

- [ ] **Step 1: Add renderers**

Use existing Harbinger NPC model/render style when possible. If a specific model class is not needed, use the same base renderer approach as nearby Harbinger NPC renderers and point to:

```text
textures/entity/harbinger_voyager/harbinger_voyager.png
textures/entity/harbinger_votary_wayfarer/harbinger_votary_wayfarer.png
```

- [ ] **Step 2: Add spawn eggs**

Register creative/test spawn eggs:

```java
spawn_egg_harbinger_voyager
spawn_egg_harbinger_votary_wayfarer
```

- [ ] **Step 3: Add language keys**

Add names plus dialogue lines for both NPCs. Keep all displayed text translatable.

### Task 4: Item Inquiry JSON

**Files:**
- Create: `src/main/resources/data/hemomancy/dialogue_inquiry/voyager/hemomancy/salt_stained_voyager_log.json`
- Create: `src/main/resources/data/hemomancy/dialogue_inquiry/voyager/hemomancy/erythrocoral_fragment.json`
- Create: `src/main/resources/data/hemomancy/dialogue_inquiry/voyager/hemomancy/chalybeate_sclerite.json`
- Create: `src/main/resources/data/hemomancy/dialogue_inquiry/votary_wayfarer/hemomancy/salt_stained_voyager_log.json`
- Create: `src/main/resources/data/hemomancy/dialogue_inquiry/votary_wayfarer/hemomancy/erythrocoral_fragment.json`
- Create: `src/main/resources/data/hemomancy/dialogue_inquiry/votary_wayfarer/hemomancy/chalybeate_sclerite.json`

- [ ] **Step 1: Add data-driven held-item responses**

Use simple line lists. Voyager responses should be measured and scholarly; Votary responses should be curious and personal.

- [ ] **Step 2: Parse JSON resources**

Run a PowerShell `ConvertFrom-Json` check across these six new files.

Expected: all parse without output errors.

### Task 5: Active Vessel Spawn Helper And Docs

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/voyager/ActiveHarbingerVoyagerNpcSpawner.java`
- Modify: `docs/HEMOMANCY_REFERENCE.md`
- Modify: `docs/LORE_REFERENCE.md`

- [ ] **Step 1: Add reusable structure spawn helper**

Create a helper method future active vessel structures can call from `afterPlace()`:

```java
public static void spawnForActiveVessel(WorldGenLevel level, BoundingBox fullBox, BlockPos center, RandomSource random)
```

It should spawn one Voyager and roll `random.nextInt(ActiveHarbingerVoyagerNpcRules.VOTARY_WAYFARER_CHANCE)` for the Votary Wayfarer.

- [ ] **Step 2: Update docs**

Document the two NPC ids, structure-only spawn intent, dialogue-first V1 scope, and 1-in-5 companion rule.

### Task 6: Verification

**Files:**
- Verify all touched files.

- [ ] **Step 1: Run targeted tests**

Run:

```powershell
./gradlew.bat compileJava compileTestJava
java -cp "build/classes/java/main;build/classes/java/test" com.vincenthuto.hemomancy.common.worldgen.structure.voyager.ActiveHarbingerVoyagerNpcRulesTest
```

- [ ] **Step 2: Parse new JSON**

Run PowerShell `ConvertFrom-Json` against the new model/dialogue files and `en_us.json`.

- [ ] **Step 3: Run full build**

Run: `./gradlew.bat build`

- [ ] **Step 4: Forbidden pattern scan**

Run:

```powershell
rg -n "^import net\.minecraftforge|^import .*compat\.mna|^import .*compat\.curios|new\s+SimpleChannel|\bSimpleChannel\s+[A-Za-z_]|NetworkRegistry\." src/main/java/com/vincenthuto/hemomancy/common src/main/java/com/vincenthuto/hemomancy/client src/main/java/com/vincenthuto/hemomancy/Hemomancy.java
```

Expected: no matches.

- [ ] **Step 5: Report runClient gap if not performed**

If `runClient` is not run, final response must state that in-game renderer/dialogue validation remains.
