# Rite Purposes and Working Crafts Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give every Harbinger Cardinal Rite a purpose that decides its floor, and move machine crafting from floorless Blood Structures onto Working-floor rites whose subject is the built structure.

**Architecture:** A pure `CardinalRitePurpose` enum validates rite floors. Working crafts are ordinary `CardinalRiteRecipe`s with a new `subject` pattern and feed amount; a pure `WorkingCraftBed` describes where a subject may stand, `WorkingCraftSubjectMatcher` finds it in the world, and `WorkingCraftRites` is the rite variant (prepare/escrow, feed, complete once, recover) alongside `StationUpgradeRites`, both using a shared `RiteOfferingEscrow`. A new `SUBJECT_FEEDING` phase replaces anchors for crafts. The client receives subject blocks and fill through the existing rite sync and draws the floor swallowing the structure.

**Tech Stack:** Java 21, NeoForge 21.1.251 (Minecraft 1.21.1), HutosLib `MultiblockPattern`, JUnit 5 (`./gradlew.bat test`), NeoForge GameTests (`./gradlew.bat runGameTestServer`), GLSL 150 core shaders.

**Spec:** `docs/superpowers/specs/2026-10-14-rite-purposes-and-working-crafts-design.md`

## Global Constraints

- NeoForge imports only (`net.neoforged.*`); `Hemomancy.rloc("snake_case")`; payload networking per `PacketHandler` (no `SimpleChannel`).
- Purposes: `self` → Threshold, `world` → Dominion, `making` → Working, `bond` → Communion. Default culminations: ascension, domain, forging, binding.
- Special rites (`"special": true`): Apotheos (`self`), Bloom of the Qliphoth (`world`), Ancestral Communion (`bond`).
- Beds (wide × deep): minor 3×1, lesser 3×2, greater 3×3, grand 5×4; subject at most 3 tall, standing on the floor's upper layer (Focus Y + 1), any side, any horizontal rotation.
- Working crafts: `bloodCost` 0, `subject.feed` = old structure `bloodCost`; ceremony `simple`, no anchors/sockets/waves/helpers, faint fog, `safe_retry`, `forging`.
- Floorless Harbinger structures (exactly 8): hematic_iron_block, liber_sanguinum, living_staff, iron_brazier, dried_gourd, hematic_stake, vial_centrifuge, ghastly_alembic. Unstained structures pallid_retort and unstained_pillar untouched.
- No `offerings` on Blood Structure recipes. No copper in Working-craft subjects (copper_block → `hemomancy:chiseled_hematic_iron_block`).
- Custom Hemomancy/HutosLib visuals only for touched effects; no new vanilla particles.
- Do not commit. Leave all work in the working tree (user preference overrides the plan template's commit steps).
- Baseline JVM suite: 3,099 tests, the 16 known failures listed in `%TEMP%\hemo_baseline_failcases.txt`. Finish with no new failures.

## Review Focus

- **A subject that also matches a floorless recipe pattern.** Projecting into a Working subject outside a rite must give the Working-craft hint and must not condense a Sanguine Formation or craft anything. Pinned by `WorkingCraftGameTests.projectionIntoIdleSubjectOnlyHints` (Task 7).
- **The caster breaks a subject block mid-feed, or a hopper/piston changes it.** The rite collapses safely: offerings and staff return, the structure stays, nothing is crafted. Pinned by `WorkingCraftGameTests.brokenSubjectReturnsEscrow` (Task 11).
- **Server restart between block removal and rite removal.** No second result. Pinned by `WorkingCraftGameTests.completionIsOnceOnly` calling completion twice (Task 11).
- **A subject built so it overlaps two beds (corner) or rests one block above the floor.** No match; the Focus explains the rite cannot find its subject. Pinned by `WorkingCraftBedTest.cornerCellsBelongToNoBed` and `WorkingCraftSubjectMatcher` floor-level check (Task 2/Task 7).
- **Old Mnemonic Blueprints targeting `blood_structure/<moved>`.** They keep working, now as rite blueprints. Pinned by `LegacyBloodStructureIdsTest` and the `MnemonicBlueprintTarget` remap (Task 9).

---

### Task 1: Rite purpose grammar

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/CardinalRitePurpose.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/recipe/CardinalRiteRecipe.java` (fields `purpose`, `special` + accessors)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/recipe/serializer/CardinalRiteRecipeSerializer.java` (parse, validate, network encode/decode, MapCodec keys/encode)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteCulmination.java` (purpose-aware `resolve`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java` (`culminationStyle`)
- Modify: all 52 Harbinger JSONs under `src/main/resources/data/hemomancy/recipe/cardinal_rite/*.json` (add `purpose`, `special` where listed, rehome 17 floors)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/rite/CardinalRitePurposeTest.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/recipe/CardinalRitePurposeDataTest.java`

**Interfaces:**
- Produces: `CardinalRitePurpose { SELF, WORLD, MAKING, BOND }` with `String id()`, `String floorStyle()`, `CardinalRiteCulmination defaultCulmination()`, `static CardinalRitePurpose byId(String)` (null when blank/unknown), `static String floorStyleOf(String floorPath)` (path text before the last `_`), `static String violation(CardinalRitePurpose purpose, boolean special, String floorPath)` (null when valid).
- Produces: `CardinalRiteRecipe.getPurpose()`, `setPurpose(CardinalRitePurpose)`, `isSpecial()`, `setSpecial(boolean)`.
- Produces: `CardinalRiteCulmination.resolve(String authored, boolean rankup, CardinalRitePurpose purpose, boolean producesItem, String floorStyle)`.

- [ ] **Step 1: Write the failing unit test**

```java
package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class CardinalRitePurposeTest {
	@Test
	void eachPurposeOwnsOneFloorStyleAndCulmination() {
		assertEquals("threshold", CardinalRitePurpose.SELF.floorStyle());
		assertEquals("dominion", CardinalRitePurpose.WORLD.floorStyle());
		assertEquals("working", CardinalRitePurpose.MAKING.floorStyle());
		assertEquals("communion", CardinalRitePurpose.BOND.floorStyle());
		assertEquals(CardinalRiteCulmination.ASCENSION, CardinalRitePurpose.SELF.defaultCulmination());
		assertEquals(CardinalRiteCulmination.DOMAIN, CardinalRitePurpose.WORLD.defaultCulmination());
		assertEquals(CardinalRiteCulmination.FORGING, CardinalRitePurpose.MAKING.defaultCulmination());
		assertEquals(CardinalRiteCulmination.BINDING, CardinalRitePurpose.BOND.defaultCulmination());
	}

	@Test
	void parsesIdsAndRejectsUnknown() {
		assertEquals(CardinalRitePurpose.BOND, CardinalRitePurpose.byId(" Bond "));
		assertNull(CardinalRitePurpose.byId(""));
		assertNull(CardinalRitePurpose.byId("crafting"));
	}

	@Test
	void floorStyleIsThePathBeforeTheTier() {
		assertEquals("working", CardinalRitePurpose.floorStyleOf("working_lesser"));
		assertEquals("qliphoth_root", CardinalRitePurpose.floorStyleOf("qliphoth_root_grand"));
	}

	@Test
	void floorMustMatchPurposeUnlessSpecial() {
		assertNull(CardinalRitePurpose.violation(CardinalRitePurpose.MAKING, false, "working_greater"));
		assertNotNull(CardinalRitePurpose.violation(CardinalRitePurpose.MAKING, false, "threshold_greater"));
		assertNull(CardinalRitePurpose.violation(CardinalRitePurpose.WORLD, true, "qliphoth_grand"));
		assertNotNull(CardinalRitePurpose.violation(null, false, "working_lesser"));
	}

	@Test
	void authoredCulminationWinsThenRankThenPurpose() {
		assertEquals(CardinalRiteCulmination.GROUNDING, CardinalRiteCulmination.resolve(
				"grounding", false, CardinalRitePurpose.WORLD, false, "dominion"));
		assertEquals(CardinalRiteCulmination.ASCENSION, CardinalRiteCulmination.resolve(
				"", true, CardinalRitePurpose.SELF, false, "threshold"));
		assertEquals(CardinalRiteCulmination.BINDING, CardinalRiteCulmination.resolve(
				"", false, CardinalRitePurpose.BOND, true, "communion"));
		assertEquals(CardinalRiteCulmination.FORGING, CardinalRiteCulmination.resolve(
				"", false, null, true, null));
	}
}
```

- [ ] **Step 2: Run it to verify it fails** — `./gradlew.bat test --tests "*CardinalRitePurposeTest"`; expected: compilation failure (`CardinalRitePurpose` missing).

- [ ] **Step 3: Implement `CardinalRitePurpose`**

```java
package com.vincenthuto.hemomancy.common.rite;

import java.util.Locale;

/**
 * What a Harbinger rite is for. The purpose decides the floor style the rite is
 * performed on and how it resolves when no culmination is authored.
 */
public enum CardinalRitePurpose {
	/** Degree up, or a power or body change in the caster. */
	SELF("self", "threshold", CardinalRiteCulmination.ASCENSION),
	/** Changes the world: conjures, alters weather, makes something appear. */
	WORLD("world", "dominion", CardinalRiteCulmination.DOMAIN),
	/** Consumes components to make something new. */
	MAKING("making", "working", CardinalRiteCulmination.FORGING),
	/** Acts on ties between people: bloodlines, covenants, shared pools. */
	BOND("bond", "communion", CardinalRiteCulmination.BINDING);

	private final String id;
	private final String floorStyle;
	private final CardinalRiteCulmination defaultCulmination;

	CardinalRitePurpose(String id, String floorStyle, CardinalRiteCulmination defaultCulmination) {
		this.id = id;
		this.floorStyle = floorStyle;
		this.defaultCulmination = defaultCulmination;
	}

	public String id() { return id; }
	public String floorStyle() { return floorStyle; }
	public CardinalRiteCulmination defaultCulmination() { return defaultCulmination; }

	public static CardinalRitePurpose byId(String id) {
		if (id == null || id.isBlank()) return null;
		String wanted = id.trim().toLowerCase(Locale.ROOT);
		for (CardinalRitePurpose purpose : values()) if (purpose.id.equals(wanted)) return purpose;
		return null;
	}

	/** Floor ids are {@code <style>_<tier>}; the style is everything before the last underscore. */
	public static String floorStyleOf(String floorPath) {
		int split = floorPath.lastIndexOf('_');
		return split <= 0 ? floorPath : floorPath.substring(0, split);
	}

	/** @return why the floor does not suit the purpose, or null when it does */
	public static String violation(CardinalRitePurpose purpose, boolean special, String floorPath) {
		if (purpose == null) return "declares no purpose (self, world, making or bond)";
		if (special) return null;
		String style = floorStyleOf(floorPath);
		return purpose.floorStyle.equals(style) ? null
				: "is a " + purpose.id + " rite and must use a " + purpose.floorStyle + " floor, not " + floorPath;
	}
}
```

Add to `CardinalRiteCulmination`:

```java
	/**
	 * Resolves the authored culmination, then rank rites ascend, then the rite's
	 * purpose decides. Rites without a purpose (Unstained, legacy) keep the old
	 * item/floor-style fallback.
	 */
	public static CardinalRiteCulmination resolve(String authored, boolean rankup, CardinalRitePurpose purpose,
			boolean producesItem, String floorStyle) {
		CardinalRiteCulmination explicit = byId(authored);
		if (explicit != null) return explicit;
		if (rankup) return ASCENSION;
		if (purpose != null) return purpose.defaultCulmination();
		return resolve("", false, producesItem, floorStyle);
	}
```

`HarbingerCardinalRiteEvents.culminationStyle` calls the new overload with `recipe.getPurpose()`.

- [ ] **Step 4: Recipe fields and serializer.** In `CardinalRiteRecipe` add `protected CardinalRitePurpose purpose; protected boolean special;` with getters/setters. In `CardinalRiteRecipeSerializer.fromJsonObject`, after the floor block, when `!unstained`:

```java
		CardinalRitePurpose purpose = CardinalRitePurpose.byId(GsonHelper.getAsString(pJson, "purpose", ""));
		boolean special = GsonHelper.getAsBoolean(pJson, "special", false);
		if (!unstained) {
			String violation = CardinalRitePurpose.violation(purpose, special,
					recipe.getFloorId() == null ? "" : recipe.getFloorId().getPath());
			if (violation != null) {
				throw new JsonSyntaxException("Harbinger cardinal rite " + pRecipeId + " " + violation);
			}
		}
		recipe.setPurpose(purpose);
		recipe.setSpecial(special);
```

Network: write `purpose == null ? "" : purpose.id()` and `special` right after `rankup` in `toNetwork`, read them in `fromNetwork` in the same position. MapCodec: add `"purpose"`, `"special"` to `keys`, and encode them when present.

- [ ] **Step 5: Write the failing data test** that walks `recipe/cardinal_rite` recursively and pins the roster:

```java
package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRitePurposeDataTest {
	private static final Path RITE_ROOT = Path.of("src/main/resources/data/hemomancy/recipe/cardinal_rite");

	private static final Map<String, String> PURPOSES = Map.ofEntries(
			Map.entry("initiate_rite", "self"), Map.entry("sanguine_brotherhood", "self"),
			Map.entry("illuminatus_rite", "self"), Map.entry("sanctified_rite", "self"),
			Map.entry("archon_rite", "self"), Map.entry("apotheos_rite", "self"),
			Map.entry("vascular_mending", "self"), Map.entry("hematic_fortification", "self"),
			Map.entry("crimson_beacon", "self"), Map.entry("eternal_covenant", "self"),
			Map.entry("sanguine_attunement", "self"), Map.entry("chamber_of_will", "self"),
			Map.entry("rooted_vein", "world"), Map.entry("hungering_earth", "world"),
			Map.entry("sanguine_fervor", "world"), Map.entry("sanguine_eclipse", "world"),
			Map.entry("pallid_shadow", "world"), Map.entry("sanguine_dominion", "world"),
			Map.entry("founding_fane", "world"), Map.entry("hematic_succession", "world"),
			Map.entry("mnemonic_restoration", "world"), Map.entry("bloom_of_qliphoth", "world"),
			Map.entry("puppeteer_trial_veinwing_vulture", "world"), Map.entry("puppeteer_trial_marrow_spitter", "world"),
			Map.entry("puppeteer_trial_gorebound_hulk", "world"), Map.entry("puppeteer_trial_scarlet_mummer", "world"),
			Map.entry("puppeteer_trial_sanguine_hound", "world"), Map.entry("puppeteer_trial_cinder_bellows", "world"),
			Map.entry("puppeteer_trial_mnemonist_puppet", "world"),
			Map.entry("pallid_vessel_rite", "making"), Map.entry("crimson_vessel_rite", "making"),
			Map.entry("ashen_vessel_rite", "making"), Map.entry("horn_of_culmination_rite", "making"),
			Map.entry("exsanguination", "making"), Map.entry("first_condensation", "making"),
			Map.entry("sanguine_athanor", "making"), Map.entry("steady_separation", "making"),
			Map.entry("second_fraction", "making"), Map.entry("true_groove", "making"),
			Map.entry("enduring_pattern", "making"), Map.entry("armature_consecration", "making"),
			Map.entry("monolithic_armature", "making"), Map.entry("eightfold_script", "making"),
			Map.entry("palimpsest", "making"), Map.entry("ganglionic_weave", "making"),
			Map.entry("cortical_weave", "making"),
			Map.entry("bloodline_founding", "bond"), Map.entry("bloodline_recall", "bond"),
			Map.entry("hematic_unbinding", "bond"), Map.entry("scarlet_summons", "bond"),
			Map.entry("covenant_vigil", "bond"), Map.entry("ancestral_communion", "bond"));
	private static final Set<String> SPECIAL = Set.of("apotheos_rite", "bloom_of_qliphoth", "ancestral_communion");

	@Test
	void everyHarbingerRiteDeclaresItsPinnedPurposeOnAMatchingFloor() throws IOException {
		Set<String> seen = new HashSet<>();
		try (var paths = Files.walk(RITE_ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
				if (json.has("unstained") && json.get("unstained").getAsBoolean()) continue;
				String name = path.getFileName().toString().replace(".json", "");
				boolean working = path.getParent().getFileName().toString().equals("working");
				String purpose = json.get("purpose").getAsString();
				if (working) assertEquals("making", purpose, path.toString());
				else {
					assertEquals(PURPOSES.get(name), purpose, path + " purpose");
					seen.add(name);
				}
				boolean special = json.has("special") && json.get("special").getAsBoolean();
				assertEquals(SPECIAL.contains(name), special, path + " special");
				String floor = json.get("floor").getAsString();
				assertNull(CardinalRitePurpose.violation(CardinalRitePurpose.byId(purpose), special,
						floor.substring(floor.indexOf(':') + 1)), path.toString());
			}
		}
		assertEquals(PURPOSES.keySet(), seen);
	}
}
```

- [ ] **Step 6: Edit the JSONs** with a one-off Python script (not kept): add `"purpose"` after `"riteType"` per the `PURPOSES` map, add `"special": true` to the three special rites, and rewrite `floor` style for the 17 rehomed rites keeping the tier (`working_lesser` → `threshold_lesser`, etc.). Preserve formatting with `json.dump(indent=2)`.

- [ ] **Step 7: Run tests** — `./gradlew.bat test --tests "*CardinalRitePurpose*" --tests "*CardinalRiteLayeredRecipeDataTest" --tests "*CardinalRiteCulmination*"`; expected: PASS. Fix existing tests that assert old floors for rehomed rites.

---

### Task 2: Working-craft bed geometry (pure)

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/WorkingCraftBed.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/rite/WorkingCraftBedTest.java`

**Interfaces:**
- Produces: `record WorkingCraftBed(int width, int depth)` with `MAX_HEIGHT = 3`, `static WorkingCraftBed forTier(CardinalRiteType)`, `boolean fits(Footprint)`, `boolean contains(int lateral, int outward, int up)`, `static CardinalRiteType smallestFitting(Footprint)` (null if none), nested `record Footprint(int width, int depth, int height)` with `static Footprint of(String[][] pattern)` (non-space bounding box; pattern[z][y] rows, char index x).

- [ ] **Step 1: Failing test**

```java
package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class WorkingCraftBedTest {
	@Test
	void bedSizesFollowTheFloorTier() {
		assertEquals(new WorkingCraftBed(3, 1), WorkingCraftBed.forTier(CardinalRiteType.MINOR));
		assertEquals(new WorkingCraftBed(3, 2), WorkingCraftBed.forTier(CardinalRiteType.LESSER));
		assertEquals(new WorkingCraftBed(3, 3), WorkingCraftBed.forTier(CardinalRiteType.GREATER));
		assertEquals(new WorkingCraftBed(5, 4), WorkingCraftBed.forTier(CardinalRiteType.GRAND));
	}

	@Test
	void footprintIgnoresSpacePadding() {
		String[][] pattern = {{"   ", " P "}, {"PIP", "IBI"}, {"   ", " P "}};
		assertEquals(new WorkingCraftBed.Footprint(3, 3, 2), WorkingCraftBed.Footprint.of(pattern));
		String[][] forge = {{"H H", "ACG"}};
		assertEquals(new WorkingCraftBed.Footprint(3, 1, 2), WorkingCraftBed.Footprint.of(forge));
	}

	@Test
	void fitsInEitherRotationWithinHeight() {
		WorkingCraftBed lesser = WorkingCraftBed.forTier(CardinalRiteType.LESSER);
		assertTrue(lesser.fits(new WorkingCraftBed.Footprint(3, 2, 3)));
		assertTrue(lesser.fits(new WorkingCraftBed.Footprint(2, 3, 3)));
		assertFalse(lesser.fits(new WorkingCraftBed.Footprint(3, 3, 1)));
		assertFalse(lesser.fits(new WorkingCraftBed.Footprint(3, 2, 4)));
	}

	@Test
	void smallestFittingTier() {
		assertEquals(CardinalRiteType.MINOR, WorkingCraftBed.smallestFitting(new WorkingCraftBed.Footprint(3, 1, 3)));
		assertEquals(CardinalRiteType.GREATER, WorkingCraftBed.smallestFitting(new WorkingCraftBed.Footprint(3, 3, 3)));
		assertEquals(CardinalRiteType.GRAND, WorkingCraftBed.smallestFitting(new WorkingCraftBed.Footprint(3, 4, 3)));
		assertNull(WorkingCraftBed.smallestFitting(new WorkingCraftBed.Footprint(6, 6, 2)));
	}

	@Test
	void containsUsesLateralOutwardAndUp() {
		WorkingCraftBed greater = WorkingCraftBed.forTier(CardinalRiteType.GREATER);
		assertTrue(greater.contains(0, 1, 1));
		assertTrue(greater.contains(-1, 3, 3));
		assertFalse(greater.contains(0, 0, 1), "the Focus row is not part of the bed");
		assertFalse(greater.contains(2, 1, 1));
		assertFalse(greater.contains(0, 1, 0), "the bed starts on the floor's upper layer");
		assertFalse(greater.contains(0, 1, 4));
	}

	@Test
	void cornerCellsBelongToNoBed() {
		// A diagonal neighbour of the Focus is lateral 1 / outward 1 for two sides; it lies in both beds,
		// but a subject must lie entirely in one side's bed, which the matcher checks per side.
		WorkingCraftBed lesser = WorkingCraftBed.forTier(CardinalRiteType.LESSER);
		assertTrue(lesser.contains(1, 1, 1));
		assertFalse(lesser.contains(2, 1, 1));
	}
}
```

- [ ] **Step 2: Run** — `./gradlew.bat test --tests "*WorkingCraftBedTest"`; expected compile failure.

- [ ] **Step 3: Implement**

```java
package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;

/**
 * Where a Working craft's subject may stand: beside the Cardinal Focus on one
 * side, on the floor's upper layer, inside a bed sized by the floor tier.
 * Cells are measured from the Focus: lateral across the side, outward away from
 * the Focus, up from the floor plane.
 */
public record WorkingCraftBed(int width, int depth) {
	public static final int MAX_HEIGHT = 3;

	public static WorkingCraftBed forTier(CardinalRiteType tier) {
		return switch (tier) {
			case MINOR -> new WorkingCraftBed(3, 1);
			case LESSER -> new WorkingCraftBed(3, 2);
			case GREATER -> new WorkingCraftBed(3, 3);
			case GRAND -> new WorkingCraftBed(5, 4);
		};
	}

	public boolean fits(Footprint footprint) {
		if (footprint.height() > MAX_HEIGHT) return false;
		return footprint.width() <= width && footprint.depth() <= depth
				|| footprint.depth() <= width && footprint.width() <= depth;
	}

	public boolean contains(int lateral, int outward, int up) {
		return Math.abs(lateral) <= width / 2 && outward >= 1 && outward <= depth && up >= 1 && up <= MAX_HEIGHT;
	}

	public static CardinalRiteType smallestFitting(Footprint footprint) {
		for (CardinalRiteType tier : CardinalRiteType.values()) if (forTier(tier).fits(footprint)) return tier;
		return null;
	}

	/** The bounding box of a pattern's non-space cells. Patterns are {@code [z][y]} rows of x characters. */
	public record Footprint(int width, int depth, int height) {
		public static Footprint of(String[][] pattern) {
			int minX = Integer.MAX_VALUE, maxX = -1, minY = Integer.MAX_VALUE, maxY = -1, minZ = Integer.MAX_VALUE, maxZ = -1;
			for (int z = 0; z < pattern.length; z++) {
				for (int y = 0; y < pattern[z].length; y++) {
					String row = pattern[z][y];
					for (int x = 0; x < row.length(); x++) {
						if (row.charAt(x) == ' ') continue;
						minX = Math.min(minX, x); maxX = Math.max(maxX, x);
						minY = Math.min(minY, y); maxY = Math.max(maxY, y);
						minZ = Math.min(minZ, z); maxZ = Math.max(maxZ, z);
					}
				}
			}
			if (maxX < 0) return new Footprint(0, 0, 0);
			return new Footprint(maxX - minX + 1, maxZ - minZ + 1, maxY - minY + 1);
		}
	}
}
```

- [ ] **Step 4: Run** — expected PASS.

---

### Task 3: Recipe subject schema

**Files:**
- Modify: `common/recipe/CardinalRiteRecipe.java` (`subject` MultiblockPattern, `subjectFeedMl` int, `isWorkingCraft()`)
- Modify: `common/recipe/serializer/CardinalRiteRecipeSerializer.java` (parse `subject`, validate, network)
- Modify: `common/rite/CardinalRiteProgressionPolicy.java` only if it rejects zero-anchor ceremonies for crafts (check and allow `simple` zero-anchor Working crafts)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/recipe/WorkingCraftSubjectRulesTest.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/WorkingCraftRecipeRules.java` (pure validation used by the serializer)

**Interfaces:**
- Produces: `CardinalRiteRecipe.getSubject()` (nullable `MultiblockPattern`), `getSubjectFeedMl()`, `isWorkingCraft()` (= subject != null).
- Produces: `WorkingCraftRecipeRules.violation(CardinalRitePurpose purpose, String floorPath, String[][] subjectPattern, int feedMl, int anchorCount, int bloodCost)` → null when valid. Rules: purpose MAKING, floor style `working`, subject fits `WorkingCraftBed.forTier(tier of floorPath)`, feed > 0, no anchors, `bloodCost` 0.

- [ ] **Step 1: Failing test** for `WorkingCraftRecipeRules.violation` covering: valid lesser loom; wrong purpose; non-working floor; 3×3 on lesser; zero feed; anchors present; non-zero bloodCost.
- [ ] **Step 2: Run** — compile failure.
- [ ] **Step 3: Implement** `WorkingCraftRecipeRules` (tier from `floorPath.substring(lastIndexOf('_')+1)` via `CardinalRiteType.byName`), the recipe fields, and serializer parsing:

```java
		if (pJson.has("subject")) {
			JsonObject subject = GsonHelper.getAsJsonObject(pJson, "subject");
			MultiblockPattern pattern = CardinalRitePatternJson.parse(subject);
			int feed = GsonHelper.getAsInt(subject, "feed");
			recipe.setSubject(pattern, feed);
		}
		// after ceremony parse:
		if (recipe.isWorkingCraft()) {
			String violation = WorkingCraftRecipeRules.violation(purpose, recipe.getFloorId().getPath(),
					recipe.getSubject().getPatternArray(), recipe.getSubjectFeedMl(),
					recipe.getCeremony().anchors().size(), (int) recipe.getBloodCost());
			if (violation != null) throw new JsonSyntaxException("Working craft " + pRecipeId + " " + violation);
		}
```

Network: after the existing layered block, write `boolean hasSubject`, then `writePattern(subject)` and `writeVarInt(feed)`; read symmetrically.
- [ ] **Step 4: Run** the new test plus `*CardinalRite*` tests; expected PASS.

---

### Task 4: Working-craft data and floorless roster

**Files:**
- Create: 20 files `src/main/resources/data/hemomancy/recipe/cardinal_rite/working/<id>.json`
- Delete: the same 20 files plus `consecrated_bloodwell.json` from `recipe/blood_structure/`
- Modify: `recipe/blood_structure/vial_centrifuge.json` (remove `offerings`)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/recipe/WorkingCraftRosterDataTest.java`
- Modify tests that read moved files: `BloodStructureStationProgressionRecipeTest`, `LivingWorkshopResourceTest`, `WarpChairAssetTest`, `DendriticDistributorSynapticLoadoutSourceTest`, `BloodStructureHitBlockTest`, `UnreachableHarbingerSourceTest`, `HematicStakeBloodStructureRecipeTest` (repoint to `cardinal_rite/working/` and the `subject` object, or drop assertions that no longer apply)

**Interfaces:**
- Produces: rite IDs `hemomancy:cardinal_rite/working/<id>` for the 20 crafts in the spec roster; each JSON has `"id"`, `type: hemomancy:cardinal_rite_recipe`, `bloodCost: 0`, `riteType`, `required_degree`, `rankup: false`, `purpose: making`, `riteName` (the result's English name), `riteDescription`, `floor`, `medium` (`{"item": ...}`), `brazier_signature`, `subject {pattern, key, feed}`, `result`, and the craft ceremony:

```json
  "ceremony": {
    "profile": "simple",
    "anchors": [],
    "support_sockets": [],
    "waves": [],
    "guaranteed_waves": [],
    "fragile_offsets": [],
    "target_duration_ticks": 600,
    "focus": "living_staff",
    "required_helpers": 0,
    "helper_roles": [],
    "still_interval_ticks": 0,
    "atmosphere": { "fog": "faint", "lightning": false, "dome": false },
    "failure": "safe_retry",
    "culmination": "forging"
  }
```

- [ ] **Step 1: Failing data test** pinning: the set of 20 working craft files; per craft `required_degree`, floor tier, medium item, `subject.feed` (the spec roster table); every subject's `Footprint` fits its tier bed; no subject key names `minecraft:copper_block`; every craft has ≥1 brazier offering; `blood_structure/` contains exactly the 8 floorless Harbinger + 2 Unstained files; no `blood_structure/*.json` has `offerings`.
- [ ] **Step 2: Run** — FAIL (files missing).
- [ ] **Step 3: Generate the JSONs** with a one-off Python script driven by the spec roster table (pattern/key copied from the old file; Loom and Construct patterns replaced as in the spec; copper keys swapped; offerings: existing ones carried, new ones from the table; `riteName` from `en_us.json` `block.hemomancy.<id>`/`item.hemomancy.<id>`; `riteDescription` "Build the <name> beside the Cardinal Focus and let the Working floor take it."). Delete the old files and `consecrated_bloodwell.json`; strip `offerings` from `vial_centrifuge.json`.
- [ ] **Step 4: Run** the data test and the repointed tests; expected PASS.

---

### Task 5: Floorless cleanup (no offerings, no special cases)

**Files:**
- Delete: `common/recipe/BloodStructureOffering.java`, `common/recipe/BloodStructureOfferingPlacement.java`, `common/network/capa/harbinger/PacketBloodStructureOfferingBurst.java`, `client/data/ActiveBloodStructureOfferingBurstClientData.java`
- Modify: `common/recipe/BloodStructureRecipe.java` (drop offerings), `common/recipe/serializer/BloodStructureRecipeSerializer.java` (reject `offerings` with a clear error; drop network fields), `common/network/capa/harbinger/BloodStructureCraftingHelper.java` (remove offering match/consume, `isRiteExclusive`, `riteExclusiveMessage`; `ProjectionCraftMatch` loses offerings), `common/event/BloodStructureFeedManager.java` (no offering consumption or burst), `common/network/capa/harbinger/BloodCraftingKeyPressPacket.java` (remove rite-exclusive branches), `common/event/PendingBloodCraftManager.java` (replace `levelEvent(2001)` + `ENDERMAN_SCREAM` with `HemoParticleData.bloodCell` bursts and `SoundInit.RITE_COMPLETE` via `level.playSound`), `common/rite/harbinger/PuppeteerTrialRiteController.java` (send `CardinalRiteBeats.send(level, rite, CardinalRiteEventKind.OFFERING_DRAWN, i, "", List.of(brazierTop, centre), stack)` per offering instead of the burst packet), `common/network/PacketHandler.java` (unregister burst packet), client renderers that read the burst data, `common/network/PlaceStructurePacket.java`, `common/item/harbinger/tool/DebugShowcaseItem.java`, `client/screen/item/BloodStructureHintScreen.java`, `client/screen/skilltree/shared/CraftingTabView.java`, `compat/jei/BloodStructureRecipeCategory.java`, `common/tile/harbinger/rite/IronBrazierBlockEntity.java` (doc comment), gametest journey fixtures (`HemoJourneyFixtures`, `HarbingerJourneyAutomation`, `HarbingerJourneyFixtureGameTests` offering planner test removed; Centrifuge journey no longer places braziers)
- Test: update `BloodStructureOfferingSourceTest` into `BloodStructureFloorlessSourceTest` asserting the serializer rejects `offerings` and the helper no longer references braziers.

- [ ] **Step 1:** Rewrite the source test to assert absence of offering code and presence of the rejection message; run → FAIL.
- [ ] **Step 2:** Make the deletions/edits listed; `./gradlew.bat compileJava compileTestJava compileGameTestJava` until clean.
- [ ] **Step 3:** Run `./gradlew.bat test`; compare failures to baseline; fix any new failures caused by the removal.

---

### Task 6: Shared rite offering escrow

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/RiteOfferingEscrow.java`
- Modify: `common/rite/harbinger/StationUpgradeRites.java` (use the helper; behavior unchanged)

**Interfaces:**
- Produces:
  - `static final String ESCROWED = "ESCROWED", CONSUMED = "CONSUMED", RETURNED = "RETURNED";`
  - `static boolean escrow(ServerLevel level, ActiveCardinalRite rite, CompoundTag state)` — writes `RiteIdentity`, consumes each itinerary brazier via `IronBrazierBlockEntity.consumeForRite(false)` into `state.Offerings`, sets `EscrowState=ESCROWED`; on any failure returns the already-consumed items via `returnEscrow` and returns false.
  - `static void returnEscrow(ServerLevel level, ActiveCardinalRite rite, CompoundTag state)` — `EscrowState=RETURNED`, offerings + released staff into the overworld recovery queue keyed by `RiteIdentity`, immediate delivery when the owner is present and alive.
  - `static void markConsumed(CompoundTag state)`; `static boolean isEscrowed(CompoundTag)`; `static boolean isConsumed(CompoundTag)`.
  - `static void releaseStaffToRecovery(ServerLevel level, ActiveCardinalRite rite, UUID riteId)`.

- [ ] **Step 1:** Move the escrow loop from `StationUpgradeRites.prepare`, the RETURNED branch from `recover`, and `releaseStaff` into the helper; keep station-specific checks (identity, tier, snapshot, `wasUpgradedBy`) in `StationUpgradeRites`.
- [ ] **Step 2:** Compile; run `./gradlew.bat test --tests "*StationUpgrade*"`; expected PASS (behavior identical). The station GameTests run in Task 12.

---

### Task 7: Working-craft rite engine (server)

**Files:**
- Create: `common/rite/harbinger/WorkingCraftSubjectMatcher.java`
- Create: `common/rite/harbinger/WorkingCraftRites.java`
- Modify: `common/rite/CardinalRitePhase.java` (add `SUBJECT_FEEDING` after `STATION_PROJECTION`)
- Modify: `common/rite/ActiveCardinalRite.java` (`craft` tag + serialization key `"Craft"`, factory `workingCraft`, `feedSubject`, `subjectFeedNeeded`, `subjectFill`, `isWorkingCraft`, progress for `SUBJECT_FEEDING`)
- Modify: `common/rite/CardinalRiteStationMatcher.java` (`StationMatch` gains `WorkingCraftSubjectMatcher.SubjectMatch subject`; `find`/`resolve`/`findCaptured*` check the subject for Working crafts after braziers)
- Modify: `common/network/capa/harbinger/BloodCraftingKeyPressPacket.java` (create `ActiveCardinalRite.workingCraft` for crafts; after staff capture call `WorkingCraftRites.prepare`; craft start message)
- Modify: `common/rite/harbinger/CardinalRiteInteractionHandler.java` (feed routing in `SUBJECT_FEEDING`)
- Modify: `common/rite/harbinger/HarbingerCardinalRiteEvents.java` (tick pause/subject check, `verifyRiteStructure`, completion branch, checklist line)
- Modify: `common/rite/harbinger/CardinalRiteOrdealEngine.java` (procession skips brazier take for crafts)
- Modify: `common/rite/harbinger/CardinalRiteTermination.java` (`WorkingCraftRites.recover` for non-completed outcomes)
- Modify: `common/rite/CardinalRiteProblems.java`, `CardinalRiteRingRules.java`, `client/render/world/rite/escalation/RiteGuidance.java` (treat `SUBJECT_FEEDING` like `STATION_PROJECTION`)
- Modify: `common/event/BloodStructureFeedManager.java` (idle-subject hint that handles the projection)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/rite/WorkingCraftRiteStateTest.java` (pure `ActiveCardinalRite` state)

**Interfaces:**
- Consumes: `WorkingCraftBed`, `CardinalRiteRecipe.getSubject/getSubjectFeedMl/isWorkingCraft`, `RiteOfferingEscrow`.
- Produces:
  - `WorkingCraftSubjectMatcher.SubjectMatch(Direction side, List<BlockPos> positions, List<BlockState> states)`; `static Optional<SubjectMatch> find(Level level, BlockPos focusPos, CardinalRiteType floorTier, MultiblockPattern subject)`; `static boolean intact(Level level, List<BlockPos> positions, List<BlockState> states)`.
  - `ActiveCardinalRite.workingCraft(UUID player, BlockPos center, ResourceLocation recipeId, int riteSize, int feedMl)`; `CompoundTag craft()`; `boolean isWorkingCraft()`; `int subjectFeedNeeded()`; `boolean feedSubject(int paidMl)` (returns true when accepted; seals and enters the finale phase when full); `float subjectFill()`.
  - `WorkingCraftRites.isRite(ActiveCardinalRite)`; `prepare(ServerLevel, ActiveCardinalRite, SubjectMatch) → boolean`; `subjectIntact(ServerLevel, ActiveCardinalRite) → boolean`; `List<BlockPos> subjectPositions(ActiveCardinalRite)`; `List<BlockState> subjectStates(ServerLevel, ActiveCardinalRite)`; `complete(ServerLevel, ServerPlayer, ActiveCardinalRite, CardinalRiteRecipe) → boolean`; `recover(ServerLevel, ActiveCardinalRite)`; `hintAt(ServerPlayer, ServerLevel, BlockPos) → boolean`.

Matcher algorithm (cheap: bed cells × 4 forwards × 4 sides):

```java
	public static Optional<SubjectMatch> find(Level level, BlockPos focusPos, CardinalRiteType floorTier, MultiblockPattern subject) {
		WorkingCraftBed bed = WorkingCraftBed.forTier(floorTier);
		BlockPattern pattern = subject.getBlockPattern();
		String[][] cells = subject.getPatternArray();
		List<int[]> solid = solidCells(cells);            // {x, y, z} of non-space cells
		if (solid.isEmpty()) return Optional.empty();
		int[] first = solid.getFirst();
		for (Direction side : Direction.Plane.HORIZONTAL) {
			Direction lateralAxis = side.getClockWise();
			for (int outward = 1; outward <= bed.depth(); outward++)
				for (int lateral = -bed.width() / 2; lateral <= bed.width() / 2; lateral++)
					for (int up = 1; up <= WorkingCraftBed.MAX_HEIGHT; up++) {
						BlockPos bedCell = focusPos.relative(side, outward).relative(lateralAxis, lateral).above(up);
						for (Direction forwards : Direction.Plane.HORIZONTAL) {
							BlockPos origin = bedCell.subtract(offset(first, forwards));
							BlockPattern.BlockPatternMatch match = pattern.matches(level, origin, forwards, Direction.UP);
							if (match == null) continue;
							List<BlockPos> positions = solid.stream().map(c -> match.getBlock(c[0], c[1], c[2]).getPos()).toList();
							if (!inSideBed(bed, focusPos, side, positions)) continue;
							if (positions.stream().mapToInt(BlockPos::getY).min().orElse(0) != focusPos.getY() + 1) continue;
							return Optional.of(new SubjectMatch(side, positions,
									positions.stream().map(level::getBlockState).toList()));
						}
					}
		}
		return Optional.empty();
	}

	/** Vanilla BlockPattern placement: x along forwards×up, y down from the top row, z along forwards. */
	private static Vec3i offset(int[] cell, Direction forwards) {
		Vec3i right = forwards.getNormal().cross(Direction.UP.getNormal());
		return new Vec3i(right.getX() * cell[0] + forwards.getStepX() * cell[2],
				-cell[1], right.getZ() * cell[0] + forwards.getStepZ() * cell[2]);
	}
```

`inSideBed` converts each position to (lateral, outward, up) for that side and calls `bed.contains`.

ActiveCardinalRite additions:

```java
	private CompoundTag craft = new CompoundTag();
	/** Subject, feed and escrow state of a Working craft rite. */
	public CompoundTag craft() { return craft; }
	public boolean isWorkingCraft() { return craft.contains("FeedRequired"); }

	public static ActiveCardinalRite workingCraft(UUID playerUUID, BlockPos centerPos, ResourceLocation recipeId,
			int riteSize, int feedMl) {
		ActiveCardinalRite rite = new ActiveCardinalRite(playerUUID, centerPos, recipeId, 1, riteSize);
		rite.phase = CardinalRitePhase.SUBJECT_FEEDING;
		rite.degree = 1;
		rite.anchorBloodMl = new int[0];
		rite.instabilityRepairBloodMl = new int[0];
		rite.craft.putInt("FeedRequired", Math.max(1, feedMl));
		return rite;
	}

	public int subjectFeedNeeded() {
		if (phase != CardinalRitePhase.SUBJECT_FEEDING) return 0;
		return Math.max(0, craft.getInt("FeedRequired") - craft.getInt("Fed"));
	}

	public boolean feedSubject(int paidMl) {
		int needed = subjectFeedNeeded();
		if (paidMl <= 0 || needed <= 0) return false;
		int accepted = Math.min(needed, paidMl);
		craft.putInt("Fed", craft.getInt("Fed") + accepted);
		committedBloodMl += accepted;
		idleTicks = 0;
		if (subjectFeedNeeded() == 0) {
			altarSealed = true;
			setPhase(finalePhase());
		}
		return true;
	}

	public float subjectFill() {
		int required = craft.getInt("FeedRequired");
		return required <= 0 ? 0.0F : Math.min(1.0F, craft.getInt("Fed") / (float) required);
	}
```

Serialize `craft` as `tag.put("Craft", craft.copy())` and restore it in `deserialize` (missing key → empty tag). `getProgress`: `case SUBJECT_FEEDING -> 0.1D + 0.6D * subjectFill();`.

Interaction routing (top of the per-rite loop in `tryProject`):

```java
			if (rite.getPhase() == CardinalRitePhase.SUBJECT_FEEDING) {
				if (physicalTarget == null || !WorkingCraftRites.subjectPositions(rite).contains(physicalTarget))
					return CardinalRiteProjectionResult.handled(0.0D);
				int paid = spendBlood(player, rite, Math.min(rite.subjectFeedNeeded(),
						Math.max(1, (int) Math.floor(projectionRate))));
				if (paid > 0 && rite.feedSubject(paid)) {
					data.setDirty();
					WorkingCraftRites.onFed(serverLevel, player, rite);   // rise sound every 20 ticks; seal beat + enterFinale when full
				}
				return CardinalRiteProjectionResult.handled(paid);
			}
```

Tick integration in `onLevelTick`: treat `WorkingCraftRites.isRite(rite)` like a station rite for "wait for caster" and floor-pause; every 20 ticks check `WorkingCraftRites.subjectIntact` and on failure `failRite` (safe_retry knockback) — `Termination.end(BROKEN)` returns escrow through `WorkingCraftRites.recover`. `verifyRiteStructure` uses `layeredStructureMatch` for crafts. `completeRite` branch right after the FINALE refusal check: `if (WorkingCraftRites.isRite(rite)) return WorkingCraftRites.complete(sLevel, caster, rite, recipe);`. `CardinalRiteOrdealEngine.tickOfferingProcession` skips the brazier take when `WorkingCraftRites.isRite(rite)`.

`WorkingCraftRites.complete` order: if `RiteOfferingEscrow.isConsumed(craft)` → return true (already settled). If `!subjectIntact` → `recover`, return false. Consume the Focus medium through `CardinalRiteMediumRules.finish`. `markConsumed(craft)` and `setDirty`. Remove each subject block (`setBlock(AIR, 3)`) with a `HemoParticleData.bloodCell` burst. Drop the result `ItemEntity` at Focus + (0.5, 1.5, 0.5). Award `MachineAccessEvents.awardMachineCrafted`, `FirstBloodcraftAssignment.recordStructure`, `LivingStaffBondHelper.unlockFromBloodStructureCraft`, `SkillPointGainEvents.onRiteCompleted`, `LiberKnowledgeHelper.unlockForRite`. Play `SoundInit.RITE_SUBJECT_SINK` and `RITE_COMPLETE`, message "The Working floor takes the structure. <result> condenses on the Focus.". Return true.

`hintAt`: for each Working craft recipe, if the clicked block is part of the subject pattern built anywhere (reuse `BloodStructureCraftingHelper.findPatternAtHit(BlockPattern, ServerLevel, BlockPos)`), message once per 40 ticks: "This is the frame of a <result>. Build it beside a Cardinal Focus on a <tier> Working floor, seat <medium> in the Focus, light its braziers and plant your Living Staff." and return true so projection does not fall through.

- [ ] **Step 1: Failing pure state test** (`WorkingCraftRiteStateTest`): `workingCraft` starts in `SUBJECT_FEEDING`; `feedSubject` caps at the requirement; full feed with no itinerary goes to `CULMINATION`, with an itinerary to `OFFERING_PROCESSION`; `subjectFill`; `craft` survives `serialize(provider)`/`deserialize` (use `HolderLookup.Provider` from existing state tests' pattern; if those need registries, assert via `serialize()` legacy overload).
- [ ] **Step 2: Run** → FAIL.
- [ ] **Step 3: Implement** the files above.
- [ ] **Step 4:** `./gradlew.bat compileJava compileTestJava compileGameTestJava`; `./gradlew.bat test` → no new failures.

---

### Task 8: Client sync and the swallow visuals

**Files:**
- Modify: `client/data/ActiveRiteClientData.java` (`RiteEntry.withSubject(List<BlockPos>, int[] stateIds, float fill)` + getters)
- Modify: `common/network/capa/harbinger/PacketSyncActiveRites.java` (encode/decode subject at the end)
- Modify: `common/rite/harbinger/HarbingerCardinalRiteEvents.java` (`toClientEntry` adds subject for crafts)
- Modify: `src/main/resources/assets/hemomancy/shaders/core/world/blood_structure_warp.{json,vsh,fsh}` (uniform `FillLine`, default 100000; vertex passes camera-relative Y; fragment discards above a ragged line and brightens the meniscus)
- Modify: `common/init/ShaderInit.java` (`FillLine` in both holders' uniform lists), `client/render/HemoRenderTypes.java` (`bloodStructureWarp` overload with `fillLine`; old signature delegates with 100000)
- Create: `client/render/world/WorkingCraftSwallowRules.java` (pure timing), `client/render/world/WorkingCraftSwallowGeometry.java` (vessels on the CRUOR batch), `client/render/world/WorkingCraftSwallowRenderer.java` (shell + ghost sink/drain), `client/rite/WorkingCraftSwallowClientState.java` (last subject per centre; ending ghosts; cleared on logout)
- Modify: `client/event/ClientEvents.java` (render hook after `CardinalRiteBoundaryRenderer.render`), `client/render/world/ManipulationVisualRenderer.java` (`WorkingCraftSwallowGeometry.collect(poses, FLOWS, camera, partial)`), `client/data/CardinalRiteEventClientState.java` (forward endings to the swallow state), logout/reload clear hooks
- Modify: `common/init/SoundInit.java`, `src/main/resources/assets/hemomancy/sounds.json`, `en_us.json` (`rite.subject_rise`, `rite.subject_sink` + subtitles)
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/WorkingCraftSwallowRulesTest.java`, `src/test/java/com/vincenthuto/hemomancy/client/render/world/WorkingCraftSwallowHookSourceTest.java`

**Interfaces:**
- Produces: `WorkingCraftSwallowRules`: `SINK_TICKS = 40`, `DRAIN_TICKS = 24`, `SEAL_PULSE_TICKS = 12`; `static double fillLine(double bottomY, double height, float fill)`; `static float smoothFill(float previous, float target, float partialStep)`; `static double sinkOffset(int age)` (0 → height+0.2 over `SINK_TICKS`, smoothstep); `static float drainFill(float startFill, int age)`; `static float heartbeat(double time)` = `pow(max(0, sin(time*0.6)), 4)`.

- [ ] **Step 1: Failing rules test** (monotonic sink, drain reaches 0 at `DRAIN_TICKS`, fill line clamps to [bottom, bottom+height], heartbeat in [0,1]) and a source-contract test asserting `ClientEvents` calls `WorkingCraftSwallowRenderer.render` and `ManipulationVisualRenderer` calls `WorkingCraftSwallowGeometry.collect`, and that no vanilla `ParticleTypes` appear in the three new client files.
- [ ] **Step 2: Run** → FAIL.
- [ ] **Step 3: Implement.** Shell: exposed faces of subject positions (reuse the face emitter by extracting `BloodStructureFeedWarpRenderer`'s face code into a package-private `BlockShellFaces` helper used by both), tint `0xB0141C` (Working floor blood), alpha 0.9, `fillLine = bottom + height*fill + 0.02`. Sealed phases: fill 1 plus a one-shot pulse (`SEAL_PULSE_TICKS`) brightening alpha. Ghost sink: `BlockRenderDispatcher.renderSingleBlock` per captured state translated down by `sinkOffset(age)` (floor occludes the sunk part), shell drawn with matching fill line. Drain: shell only with `drainFill`. Vessels: for each subject column at the bounding-box corners, a 7-point path from the floor (corner pushed 0.35 outward, y = floorTop) up to the fill line with a `sin(t*5+seed)*0.06` wobble, `ribbonPath(start 0.07, end 0.035)`, colour white (material supplies hue) with `0xFFD2C4` core pass at 40% width and `0x7A1020` base cards (`card`) at each foot; width multiplied by `1 + 0.25*heartbeat`.
- [ ] **Step 4:** Validate shaders with `glslangValidator` if present (`glslangValidator -S vert/frag` on the files); compile; run tests.

---

### Task 9: Liber, JEI, hints, spawner and blueprints

**Files:**
- Create: `common/recipe/LegacyBloodStructureIds.java` (map of 20 moved IDs + `remap(ResourceLocation) → Optional<ResourceLocation>`) and its test `LegacyBloodStructureIdsTest`
- Modify: `common/item/shared/MnemonicBlueprintTarget.java` (compact constructor remaps `BLOOD_STRUCTURE` legacy targets to `CARDINAL_RITE`)
- Modify: `client/render/world/MnemonicBlueprintPattern.java` (Working craft rite: floor + subject on the bed behind the Focus)
- Create: `client/screen/skilltree/shared/CraftingMapRecipe.java` (adapter over `BloodStructureRecipe` or a Working-craft `CardinalRiteRecipe`: id, displayName, result, requiredDegree, bloodCost/feed, pattern, catalyst stack, offerings, floor label, `isWorking()`)
- Modify: `CraftingTabState`, `CraftingTabController`, `CraftingTabView` (use the adapter; include Working crafts; click opens `RiteHintScreen` for Working crafts), `RitesTabController`/`RitesTabState` (exclude Working crafts), `HarbingerRecipeMapDefinitions` (moved entries keyed `cardinal_rite/working/<id>`; `craftingPaths()` includes them), `client/screen/skilltree/util/RecipeLookup.java`, `MiniRecipeRenderer.java` (find Working crafts for results)
- Modify: `client/screen/item/RiteHintScreen.java` (subject section: "Build it on the bed beside the Focus" + preview of the subject pattern)
- Create: `compat/jei/WorkingCraftRecipeCategory.java`; Modify: `compat/jei/JEIPlugin.java`
- Modify: `common/network/PlaceStructurePacket.java` (Working crafts: place the subject on the bed opposite the floor's forwards, seat the medium in the Focus), `client/screen/item/StructureSpawnerScreen.java` (if it filters by type)

- [ ] **Step 1:** Failing `LegacyBloodStructureIdsTest` (all 20 remap, floorless ones do not, unknown passes through empty) and a `CraftingMapRecipe` source test asserting the Crafting tab no longer imports `BloodStructureRecipe` directly outside the adapter.
- [ ] **Step 2:** Implement; compile; run tests.

---

### Task 10: Progression and teaching text

**Files:**
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json` (Alchemist Votary/Initiate/Illuminatus lines; Vicar Rooted Vein floor line `hemomancy.vicar.votary.rooted_vein.floor` names Deepslate Tiles/Bricks with Chiseled Hematic Iron studs on a Dominion lesser floor; Woven Vessel ledger hint mentions the Working floor for the Loom; Working-craft hint strings)
- Modify: Liber entries that describe machine construction as Blood Structures (grep `Blood Structure` in `assets/hemomancy` book JSON and `LiberEntryDefinitions`)
- Modify: `common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java` only if lines are hard-coded

- [ ] **Step 1:** `grep -rn "Blood Structure\|blood structure\|working Lesser floor\|Working floor" src/main/resources/assets/hemomancy src/main/java --include=*.json --include=*.java` and update each teaching line to the floorless/Working split.
- [ ] **Step 2:** Run `./gradlew.bat test --tests "*Localization*" --tests "*Dialogue*" --tests "*Liber*"`; no new failures.

---

### Task 11: GameTests

**Files:**
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/WorkingCraftGameTests.java`
- Modify: GameTests that build floors for rehomed rites (grep `working_lesser`, `threshold_`, `dominion_`, `communion_` in `src/gameTest`) and journey fixtures that referenced moved structure recipes

Tests (each builds the floor from `CardinalRiteFloorRegistry`, places the Focus/medium/lit braziers/subject, uses a mock server player with active blood at the required degree):
- `somaticLoomCraftRunsEndToEnd` — activation, feed to full via `CardinalRiteInteractionHandler`/`feedSubject`, procession ticks, completion: subject blocks gone, one Somatic Loom item entity on the Focus, `ITEM_CRAFTED` stat > 0, staff returned.
- `recallReturnsEscrowAndLeavesSubject`; `brokenSubjectReturnsEscrow`; `completionIsOnceOnly` (call `WorkingCraftRites.complete` twice → one item).
- `floorDamagePauses` (remove a floor block → phase and fill unchanged after 40 ticks).
- `projectionIntoIdleSubjectOnlyHints` (no rite: projection handled, no formation item, structure unchanged).
- `floorlessStaffAndCentrifugeNeedNoBraziers`.
- `rootedVeinRunsOnDominion`.
- `everyWorkingCraftSpawnsReadyToRun` (PlaceStructurePacket path → `CardinalRiteStationMatcher.resolve` returns MATCHED for each of the 20).

- [ ] **Step 1:** Write the tests; `./gradlew.bat compileGameTestJava`.
- [ ] **Step 2:** `./gradlew.bat runGameTestServer`; fix failures; compare against the known `blockItemExceptionsKeepTheirOwners` baseline failure.

---

### Task 12: Docs, contract and final verification

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md` (§5.3 purposes/floor table and rehomed rites; §25.1 split into floorless tier + Working crafts table; §25.2 note), `docs/STATION_UPGRADES.md` (shared `RiteOfferingEscrow` note), `docs/CARDINAL_RITE_AUDIT.md` (status line), relevant `wiki/*.md` (grep "Blood Structure"), `AGENTS.md` (contract bullet: purpose grammar, floorless roster, Working-craft escrow/once-only rules)
- Memory: update `project_hemomancy.md` pointer if needed

- [ ] **Step 1:** Update docs.
- [ ] **Step 2:** `./gradlew.bat build`, `./gradlew.bat test` (compare to baseline), `./gradlew.bat runGameTestServer`, `./gradlew.bat alphaCheck`.
- [ ] **Step 3:** Dispatch one fresh reviewer over the whole diff (native execution preference) and fix confirmed findings.
