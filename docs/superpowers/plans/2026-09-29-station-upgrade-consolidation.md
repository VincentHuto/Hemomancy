# Station Upgrade Consolidation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Put the Ghastly Alembic, Resonant Forge, Hematic Armature and Enzymatic Scriptorium on one shared upgrade path: one claim-progress attachment, one rite handler, one tier-persistence rule, and one offering rule.

**Architecture:** A pure-data `StationUpgradeCatalog` describes all 8 upgrade tiers: station, tier, degree, upgrade item, rite, required machine use, circuits and blood per circuit. Every station block stores its tier in a shared `stage` blockstate property, which loot keeps through `copy_state`. Every station block entity implements `UpgradeableStation`. A single `StationUpgradeRites` class, which replaces `AlembicUpgradeRites`, `ResonantForgeUpgradeRites`, `ArmatureUpgradeRites` and `ScriptoriumRites`, prepares, escrows, completes and recovers every upgrade rite. A single `StationUpgradeProgress` player attachment records machine use and claims. It replaces `AdvancedBrewingProgress`, `ResonantForgeProgress` and the two Armature persistent-data flags, and it drives claim dialogue for the Alchemist, Artificer, Mnemonist and Sanguine Monolith.

**Tech Stack:** NeoForge 21.1.219 / Minecraft 1.21.1, Java 21, JUnit 5 (`./gradlew.bat test`), NeoForge GameTests (`./gradlew.bat runGameTestServer`, focused `scriptoriumGameTestServer`, `advancedBrewingGameTestServer`).

**Spec:** No separate spec file. These are the decisions agreed with the user in conversation on 2026-09-29, reproduced verbatim-in-substance here. The executor reads this section as the spec:

1. One shared progress model for all four stations. Every claim requires: machine use, a degree gate, and active blood (`IBloodVolume.isActive()`). **No** purifying/Clarity checks, which are Unstained-only concepts.
2. One rite handler (`StationUpgradeRites`). The Scriptorium follows the same process as the others: escrowed offerings, blood circuits, safe recovery. The 8-orb inscription phase, the `bloodlicker_siphon` ordeal and the enzyme-loading requirement are removed.
3. The Vicar's Consecration Kit and Monolithic Cornerstone belong to the Armature only. The Scriptorium gets two new items: **Rubricator's Quill** (`rubricators_quill`, D5) and **Palimpsest Burin** (`palimpsest_burin`, D7).
4. An upgraded station keeps its tier when broken and re-placed, using the Scriptorium's `copy_state` approach. Upgrade items are **not** refunded on break.
5. Cancelled or failed rites never lose offerings, for every station.
6. D7 rites are all `greater`. No rite requires sigils. Every rite takes exactly 6 brazier offerings: the upgrade item plus 5 tier-appropriate items, with vanilla items mixed in.
7. The Alembic stays one tier early (D3 then D5); the others are D5 then D7.
8. The first claim of each upgrade item gives the item once and unlocks its crafting recipe for repeat crafts. All 8 upgrade items have recipes.
9. The Mnemonist hands out the Scriptorium items through new dialogue.
10. The D7 Scriptorium rite is renamed **Rite of the Palimpsest** (id `cardinal_rite/palimpsest`).
11. Reference docs, books and wiki are updated to describe the standardized system.

## Global Constraints

- NeoForge imports only (`net.neoforged.*`), `DeferredHolder`, `Hemomancy.rloc("snake_case")`. No `net.minecraftforge.*`.
- Player state through `HemoAttachmentTypes` + `HemoCapabilityAccess` only.
- Resource paths stay singular: `data/hemomancy/recipe/`, `data/hemomancy/loot_table/`.
- Blockstate property for every station tier: `stage`, IntegerProperty 0..2 (0 = base, 1 = first upgrade, 2 = second upgrade).
- Exactly `6` brazier offerings per station-upgrade rite (`StationUpgradeCatalog.OFFERINGS_PER_RITE`). Each brazier holds exactly one item, and `"count"` in `brazier_signature` means *number of braziers*.
- Circuits: tier 1 = 2 circuits, tier 2 = 3 circuits. Blood per circuit: Forge 250 mL, all others 50 mL (unchanged).
- Anchors: tier-1 rites 4, tier-2 rites 8. `"failure": "safe_retry"`, `"waves": []`, `"support_sockets": []`, fog `faint`.
- Every station-upgrade rite JSON declares `"id": "hemomancy:cardinal_rite/<name>"`. Without it the 1.21 codec validates the recipe as `cardinal_rite/unknown`, and the 6-offering exemption does not apply.
- Tone: fungal blood-memory, Mnemonist memory/still vocabulary; no God/Satan framing.
- **Do not commit.** The user commits manually (saved preference). Each task ends with a verification checkpoint instead of `git commit`.

## Review Focus

These are the most likely ways the change hurts real players when no test covers them. Each line has a test in the owning task.

1. **An existing world with an already-upgraded Alembic, Forge or Armature** must keep its tier after updating: legacy block-entity NBT tier goes into the blockstate. *Test: Task 3 `legacyBlockEntityTierMovesIntoBlockstate`.*
2. **A player who claimed kits under the old system** must not get a second free kit, and must have the repeat-craft recipe. *Test: Task 7 `legacyClaimsMigrateWithoutSecondKit`.*
3. **A rite in progress while the game updates** (old `ALEMBIC_PROJECTION` / `SCRIPTORIAL_INSCRIPTION` phase, old `AlembicUpgrade` NBT key, old `monolithic_script` id) must resume or safely unlock the station and return the staff. *Test: Task 5 `legacySavedRitesResumeOrUnlock`.*
4. **Rite data load with 6 offerings at D3/D5.** A missing `"id"` or a missing policy exemption makes the recipe fail to load. *Test: Task 6 `everyStationRiteDeclaresItsIdAndSixOfferings` plus the existing ceiling test.*
5. **A station broken while its upgrade rite is running** must not duplicate anything: offerings come back once and the tier is unchanged. *Test: Task 5 `brokenSubjectReturnsEscrowOnce`.*
6. **A claim made with a full inventory** stays pending and is delivered later. *Test: Task 7 `fullInventoryClaimIsDeliveredLater`.*

---

## File Structure

**Create**
- `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/UpgradeStation.java`: enum of the four stations (serialized name, block id, seat distance).
- `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/StationUpgradeTier.java`: record describing one upgrade tier.
- `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/StationUpgradeCatalog.java`: the 8 tiers, usage keys and lookups.
- `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/StationUpgradeRules.java`: pure claim-gate and tier-step rules.
- `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/StationTierProperty.java`: shared `stage` property and block-entity helpers.
- `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/UpgradeableStation.java`: interface implemented by the 4 block entities.
- `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/station/StationUpgradeProgress.java`: player attachment for usage, claims and pending deliveries.
- `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/station/StationUpgradeMigration.java`: one-time legacy migration.
- `src/main/java/com/vincenthuto/hemomancy/common/event/StationUpgradeEvents.java`: login migration and periodic delivery.
- `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/StationUpgradeRites.java`: the unified rite handler.
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/StationUpgradeDialogue.java`: generic claim dialogue.
- `src/main/resources/data/hemomancy/recipe/{hematic_condenser_kit,sanguine_athanor_kit,precision_governor_kit,master_cam_kit,monolithic_cornerstone,rubricators_quill,palimpsest_burin}.json`
- `src/main/resources/data/hemomancy/recipe/cardinal_rite/palimpsest.json`
- `src/main/resources/assets/hemomancy/models/item/{rubricators_quill,palimpsest_burin}.json` + placeholder textures.
- Tests: `src/test/java/com/vincenthuto/hemomancy/common/station/{StationUpgradeCatalogTest,StationUpgradeRulesTest,StationUpgradeRecipeResourceTest,StationTierPersistenceResourceTest,StationUpgradeRiteResourceTest,StationUpgradeProgressTest}.java`, `src/gameTest/java/com/vincenthuto/hemomancy/gametest/StationUpgradeGameTests.java`.

**Delete**
- `common/rite/harbinger/{AlembicUpgradeRites,ResonantForgeUpgradeRites,ArmatureUpgradeRites,ScriptoriumRites}.java`
- `data/hemomancy/recipe/cardinal_rite/monolithic_script.json`
- `src/test/java/com/vincenthuto/hemomancy/common/block/harbinger/HematicArmatureBreakDropSourceTest.java`

**Modify (main)**: the four station blocks, their block entities and loot tables, the Forge and Armature blockstates, `ItemInit`, `en_us.json`, `ActiveCardinalRite`, `CardinalRitePhase`, `CardinalRiteProgressionPolicy`, `CardinalRiteRecipeSerializer`, `HarbingerCardinalRiteEvents`, `CardinalRiteInteractionHandler`, `CardinalRiteOrdealEngine`, `BloodCraftingKeyPressPacket`, `GhastlyAlembicRenderer`, `CardinalRiteBoundaryRenderer`, `HemoAttachmentTypes`, `HemoCapabilityAccess`, `AdvancedBrewingProgress`, `ResonantForgeProgress`, `ResonantForgeMenu`, `EnzymaticScriptoriumMenu`, `AdvancedBrewingDialogue`, `ResonantForgeDialogue`, `DialogueEventHandler`, `DialogueOptionPacket`, `DialogueRewardClaims`, `ArtificerProgressSnapshot`, `HarbingerArtificerEntity`, `HarbingerMnemonistEntity`, `HarbingerVicarEntity`, `SanguineMonolithDialogueTrees`, the 5 `distilled()` callers, `MaterialsData`, `MaterialAtlasSpec`.

---

### Task 1: Station catalog and claim rules (pure logic)

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/UpgradeStation.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/StationUpgradeTier.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/StationUpgradeCatalog.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/StationUpgradeRules.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/StationUpgradeCatalogTest.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/StationUpgradeRulesTest.java`

**Interfaces:**
- Produces: `UpgradeStation {ALEMBIC, RESONANT_FORGE, ARMATURE, SCRIPTORIUM}` with `serializedName()`, `blockId()` (ResourceLocation), `seatDistance()`, `static UpgradeStation byName(String)` (null when unknown).
- Produces: `record StationUpgradeTier(UpgradeStation station, int tier, int requiredDegree, String upgradeItemPath, String ritePath, List<String> requiredUsage, int circuits, int bloodPerCircuit, String dialogueKey)` with `ResourceLocation upgradeItem()`, `ResourceLocation rite()`, `ResourceLocation craftingRecipe()`, `String riteType()`, `String floor()`, `int anchors()`.
- Produces: `StationUpgradeCatalog.OFFERINGS_PER_RITE = 6`, usage constants `DISTILL, REFINE, COMPOUND, GRIND, APPLY, GRIND_SELECTED, ARMOR_UPGRADE, ARMOR_UPGRADE_CONSECRATED, ENCHANT, ENCHANT_TARGETED`, `List<StationUpgradeTier> all()`, `Optional<StationUpgradeTier> forRite(ResourceLocation)`, `StationUpgradeTier get(UpgradeStation, int tier)`, `boolean isUpgradeRitePath(String path)`.
- Produces: `StationUpgradeRules.ClaimState {READY, CLAIMED, INACTIVE_BLOOD, DEGREE_TOO_LOW, PREVIOUS_TIER_UNCLAIMED, USAGE_INCOMPLETE}`, `static ClaimState claimState(int degree, boolean activeBlood, boolean alreadyClaimed, boolean previousTierClaimed, boolean usageComplete, int requiredDegree)`, `static boolean isNextTier(int currentTier, int targetTier)`.

- [ ] **Step 1: Write the failing tests**

`src/test/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/StationUpgradeCatalogTest.java`:

```java
package com.vincenthuto.hemomancy.common.station;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StationUpgradeCatalogTest {
    @Test
    void everyStationHasExactlyTwoSequentialTiers() {
        for (UpgradeStation station : UpgradeStation.values()) {
            assertEquals(1, StationUpgradeCatalog.get(station, 1).tier());
            assertEquals(2, StationUpgradeCatalog.get(station, 2).tier());
            assertTrue(StationUpgradeCatalog.get(station, 2).requiredDegree()
                    > StationUpgradeCatalog.get(station, 1).requiredDegree(), station.name());
        }
        assertEquals(8, StationUpgradeCatalog.all().size());
    }

    @Test
    void alembicStaysOneTierEarlyAndOthersUseFiveAndSeven() {
        assertEquals(3, StationUpgradeCatalog.get(UpgradeStation.ALEMBIC, 1).requiredDegree());
        assertEquals(5, StationUpgradeCatalog.get(UpgradeStation.ALEMBIC, 2).requiredDegree());
        for (UpgradeStation station : new UpgradeStation[]{UpgradeStation.RESONANT_FORGE,
                UpgradeStation.ARMATURE, UpgradeStation.SCRIPTORIUM}) {
            assertEquals(5, StationUpgradeCatalog.get(station, 1).requiredDegree(), station.name());
            assertEquals(7, StationUpgradeCatalog.get(station, 2).requiredDegree(), station.name());
        }
    }

    @Test
    void upgradeItemsAreUniquePerTier() {
        Set<ResourceLocation> items = new HashSet<>();
        for (StationUpgradeTier tier : StationUpgradeCatalog.all())
            assertTrue(items.add(tier.upgradeItem()), "shared upgrade item " + tier.upgradeItem());
        assertEquals(ResourceLocation.parse("hemomancy:rubricators_quill"),
                StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 1).upgradeItem());
        assertEquals(ResourceLocation.parse("hemomancy:palimpsest_burin"),
                StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 2).upgradeItem());
    }

    @Test
    void ritesResolveBackToTheirTier() {
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            assertEquals(tier, StationUpgradeCatalog.forRite(tier.rite()).orElseThrow());
            assertTrue(StationUpgradeCatalog.isUpgradeRitePath(tier.ritePath()));
        }
        assertEquals("cardinal_rite/palimpsest", StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 2).ritePath());
        assertTrue(StationUpgradeCatalog.forRite(ResourceLocation.parse("hemomancy:cardinal_rite/archon_rite")).isEmpty());
        assertTrue(StationUpgradeCatalog.forRite(null).isEmpty());
    }

    @Test
    void ceremonyShapeIsStandardAcrossStations() {
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            assertEquals(tier.tier() == 1 ? 2 : 3, tier.circuits(), tier.ritePath());
            assertEquals(tier.tier() == 1 ? 4 : 8, tier.anchors(), tier.ritePath());
            boolean lesser = tier.station() == UpgradeStation.ALEMBIC && tier.tier() == 1;
            assertEquals(lesser ? "lesser" : "greater", tier.riteType(), tier.ritePath());
            assertEquals(lesser ? "hemomancy:working_lesser" : "hemomancy:working_greater", tier.floor());
            assertEquals(tier.station() == UpgradeStation.RESONANT_FORGE ? 250 : 50, tier.bloodPerCircuit());
            assertFalse(tier.requiredUsage().isEmpty(), tier.ritePath());
        }
        assertEquals(6, StationUpgradeCatalog.OFFERINGS_PER_RITE);
    }

    @Test
    void stationsRoundTripByName() {
        for (UpgradeStation station : UpgradeStation.values())
            assertEquals(station, UpgradeStation.byName(station.serializedName()));
        assertNull(UpgradeStation.byName("brewing_stand"));
    }
}
```

`src/test/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/StationUpgradeRulesTest.java`:

```java
package com.vincenthuto.hemomancy.common.station;

import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeRules.ClaimState.*;
import static org.junit.jupiter.api.Assertions.*;

class StationUpgradeRulesTest {
    @Test
    void claimGatesApplyInPlayerFacingOrder() {
        assertEquals(CLAIMED, StationUpgradeRules.claimState(7, false, true, false, false, 5));
        assertEquals(INACTIVE_BLOOD, StationUpgradeRules.claimState(7, false, false, true, true, 5));
        assertEquals(DEGREE_TOO_LOW, StationUpgradeRules.claimState(4, true, false, true, true, 5));
        assertEquals(PREVIOUS_TIER_UNCLAIMED, StationUpgradeRules.claimState(7, true, false, false, true, 7));
        assertEquals(USAGE_INCOMPLETE, StationUpgradeRules.claimState(7, true, false, true, false, 7));
        assertEquals(READY, StationUpgradeRules.claimState(5, true, false, true, true, 5));
    }

    @Test
    void tiersOnlyAdvanceOneStepToAtMostTwo() {
        assertTrue(StationUpgradeRules.isNextTier(0, 1));
        assertTrue(StationUpgradeRules.isNextTier(1, 2));
        assertFalse(StationUpgradeRules.isNextTier(0, 2));
        assertFalse(StationUpgradeRules.isNextTier(2, 3));
        assertFalse(StationUpgradeRules.isNextTier(1, 1));
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.station.*"`
Expected: FAIL. Compilation errors: `UpgradeStation`, `StationUpgradeCatalog`, `StationUpgradeRules` not found.

- [ ] **Step 3: Implement**

`UpgradeStation.java`:

```java
package com.vincenthuto.hemomancy.common.station;

import net.minecraft.resources.ResourceLocation;

public enum UpgradeStation {
    ALEMBIC("alembic", "ghastly_alembic", 1),
    RESONANT_FORGE("resonant_forge", "resonant_forge", 2),
    ARMATURE("armature", "hematic_armature", 2),
    SCRIPTORIUM("scriptorium", "enzymatic_scriptorium", 2);

    private final String serializedName;
    private final String blockPath;
    private final int seatDistance;

    UpgradeStation(String serializedName, String blockPath, int seatDistance) {
        this.serializedName = serializedName;
        this.blockPath = blockPath;
        this.seatDistance = seatDistance;
    }

    public String serializedName() { return serializedName; }
    public ResourceLocation blockId() { return ResourceLocation.fromNamespaceAndPath("hemomancy", blockPath); }
    /** Blocks behind the Cardinal Focus (opposite the rite's forward direction) where the station must sit. */
    public int seatDistance() { return seatDistance; }

    public static UpgradeStation byName(String name) {
        for (UpgradeStation station : values()) if (station.serializedName.equals(name)) return station;
        return null;
    }
}
```

`StationUpgradeTier.java`:

```java
package com.vincenthuto.hemomancy.common.station;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record StationUpgradeTier(UpgradeStation station, int tier, int requiredDegree, String upgradeItemPath,
        String ritePath, List<String> requiredUsage, int circuits, int bloodPerCircuit, String dialogueKey) {
    public StationUpgradeTier {
        requiredUsage = List.copyOf(requiredUsage);
    }

    public ResourceLocation upgradeItem() { return ResourceLocation.fromNamespaceAndPath("hemomancy", upgradeItemPath); }
    public ResourceLocation rite() { return ResourceLocation.fromNamespaceAndPath("hemomancy", ritePath); }
    /** Repeat-craft recipes share the upgrade item's id. */
    public ResourceLocation craftingRecipe() { return upgradeItem(); }
    public boolean lesser() { return station == UpgradeStation.ALEMBIC && tier == 1; }
    public String riteType() { return lesser() ? "lesser" : "greater"; }
    public String floor() { return lesser() ? "hemomancy:working_lesser" : "hemomancy:working_greater"; }
    public int anchors() { return tier == 1 ? 4 : 8; }
}
```

`StationUpgradeCatalog.java`:

```java
package com.vincenthuto.hemomancy.common.station;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public final class StationUpgradeCatalog {
    public static final int OFFERINGS_PER_RITE = 6;

    public static final String DISTILL = "distill";
    public static final String REFINE = "refine";
    public static final String COMPOUND = "compound";
    public static final String GRIND = "grind";
    public static final String APPLY = "apply";
    public static final String GRIND_SELECTED = "grind_selected";
    public static final String ARMOR_UPGRADE = "armor_upgrade";
    public static final String ARMOR_UPGRADE_CONSECRATED = "armor_upgrade_consecrated";
    public static final String ENCHANT = "enchant";
    public static final String ENCHANT_TARGETED = "enchant_targeted";

    private static final List<StationUpgradeTier> TIERS = List.of(
            new StationUpgradeTier(UpgradeStation.ALEMBIC, 1, 3, "hematic_condenser_kit",
                    "cardinal_rite/first_condensation", List.of(DISTILL), 2, 50,
                    "hemomancy.alchemist.brewing.condenser"),
            new StationUpgradeTier(UpgradeStation.ALEMBIC, 2, 5, "sanguine_athanor_kit",
                    "cardinal_rite/sanguine_athanor", List.of(REFINE, COMPOUND), 3, 50,
                    "hemomancy.alchemist.brewing.athanor"),
            new StationUpgradeTier(UpgradeStation.RESONANT_FORGE, 1, 5, "precision_governor_kit",
                    "cardinal_rite/true_groove", List.of(GRIND, APPLY), 2, 250,
                    "hemomancy.artificer.resonant_forge.precision"),
            new StationUpgradeTier(UpgradeStation.RESONANT_FORGE, 2, 7, "master_cam_kit",
                    "cardinal_rite/enduring_pattern", List.of(GRIND_SELECTED), 3, 250,
                    "hemomancy.artificer.resonant_forge.master"),
            new StationUpgradeTier(UpgradeStation.ARMATURE, 1, 5, "vicars_consecration_kit",
                    "cardinal_rite/armature_consecration", List.of(ARMOR_UPGRADE), 2, 50,
                    "hemomancy.artificer.armature.consecration"),
            new StationUpgradeTier(UpgradeStation.ARMATURE, 2, 7, "monolithic_cornerstone",
                    "cardinal_rite/monolithic_armature", List.of(ARMOR_UPGRADE_CONSECRATED), 3, 50,
                    "hemomancy.monolith.cornerstone"),
            new StationUpgradeTier(UpgradeStation.SCRIPTORIUM, 1, 5, "rubricators_quill",
                    "cardinal_rite/eightfold_script", List.of(ENCHANT), 2, 50,
                    "hemomancy.mnemonist.scriptorium.rubricator"),
            new StationUpgradeTier(UpgradeStation.SCRIPTORIUM, 2, 7, "palimpsest_burin",
                    "cardinal_rite/palimpsest", List.of(ENCHANT_TARGETED), 3, 50,
                    "hemomancy.mnemonist.scriptorium.palimpsest"));

    private StationUpgradeCatalog() {}

    public static List<StationUpgradeTier> all() { return TIERS; }

    public static Optional<StationUpgradeTier> forRite(ResourceLocation riteId) {
        if (riteId == null || !riteId.getNamespace().equals("hemomancy")) return Optional.empty();
        return TIERS.stream().filter(tier -> tier.ritePath().equals(riteId.getPath())).findFirst();
    }

    public static StationUpgradeTier get(UpgradeStation station, int tier) {
        return TIERS.stream().filter(entry -> entry.station() == station && entry.tier() == tier).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(station + " has no upgrade tier " + tier));
    }

    public static boolean isUpgradeRitePath(String path) {
        return TIERS.stream().anyMatch(tier -> tier.ritePath().equals(path));
    }
}
```

`StationUpgradeRules.java`:

```java
package com.vincenthuto.hemomancy.common.station;

public final class StationUpgradeRules {
    public enum ClaimState { READY, CLAIMED, INACTIVE_BLOOD, DEGREE_TOO_LOW, PREVIOUS_TIER_UNCLAIMED, USAGE_INCOMPLETE }

    private StationUpgradeRules() {}

    public static ClaimState claimState(int degree, boolean activeBlood, boolean alreadyClaimed,
            boolean previousTierClaimed, boolean usageComplete, int requiredDegree) {
        if (alreadyClaimed) return ClaimState.CLAIMED;
        if (!activeBlood) return ClaimState.INACTIVE_BLOOD;
        if (degree < requiredDegree) return ClaimState.DEGREE_TOO_LOW;
        if (!previousTierClaimed) return ClaimState.PREVIOUS_TIER_UNCLAIMED;
        if (!usageComplete) return ClaimState.USAGE_INCOMPLETE;
        return ClaimState.READY;
    }

    public static boolean isNextTier(int currentTier, int targetTier) {
        return targetTier == currentTier + 1 && targetTier <= 2;
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.station.*"`
Expected: PASS (8 tests).

- [ ] **Step 5: Checkpoint.** Run `./gradlew.bat compileJava`, expect BUILD SUCCESSFUL. Do not commit.

---

### Task 2: Scriptorium upgrade items and the 8 repeat-craft recipes

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java:948-964`
- Create: `src/main/resources/assets/hemomancy/models/item/rubricators_quill.json`, `palimpsest_burin.json`
- Create: `src/main/resources/assets/hemomancy/textures/item/rubricators_quill.png`, `palimpsest_burin.png` (placeholders)
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json` (item names, next to `item.hemomancy.master_cam_kit` at line ~5022)
- Create: 7 recipe JSONs in `src/main/resources/data/hemomancy/recipe/`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/StationUpgradeRecipeResourceTest.java`

**Interfaces:**
- Consumes: `StationUpgradeCatalog.all()`, `StationUpgradeTier.upgradeItemPath()` (Task 1).
- Produces: `ItemInit.rubricators_quill`, `ItemInit.palimpsest_burin` (`DeferredHolder<Item, Item>`), and recipe ids equal to each upgrade item id.

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.station;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StationUpgradeRecipeResourceTest {
    private static final Path RECIPES = Path.of("src/main/resources/data/hemomancy/recipe");
    private static final Path LANG = Path.of("src/main/resources/assets/hemomancy/lang/en_us.json");
    private static final Path ITEM_MODELS = Path.of("src/main/resources/assets/hemomancy/models/item");

    @Test
    void everyUpgradeItemHasAShapedRepeatCraftRecipe() throws IOException {
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            Path file = RECIPES.resolve(tier.upgradeItemPath() + ".json");
            assertTrue(Files.exists(file), "missing recipe " + file);
            JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            assertEquals("minecraft:crafting_shaped", json.get("type").getAsString(), file.toString());
            assertEquals(tier.upgradeItem().toString(),
                    json.getAsJsonObject("result").get("id").getAsString(), file.toString());
        }
    }

    @Test
    void scriptoriumItemsHaveNamesAndModels() throws IOException {
        JsonObject lang = JsonParser.parseString(Files.readString(LANG)).getAsJsonObject();
        assertEquals("Rubricator's Quill", lang.get("item.hemomancy.rubricators_quill").getAsString());
        assertEquals("Palimpsest Burin", lang.get("item.hemomancy.palimpsest_burin").getAsString());
        assertTrue(Files.exists(ITEM_MODELS.resolve("rubricators_quill.json")));
        assertTrue(Files.exists(ITEM_MODELS.resolve("palimpsest_burin.json")));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*StationUpgradeRecipeResourceTest"`
Expected: FAIL with `missing recipe ...hematic_condenser_kit.json`.

- [ ] **Step 3: Register the items.** In `ItemInit.java`, replace the TODO comment at line 964 with:

```java
    //Enzymatic Scriptorium upgrade items
    public static final DeferredHolder<Item, Item> rubricators_quill = BASEITEMS.register("rubricators_quill",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> palimpsest_burin = BASEITEMS.register("palimpsest_burin",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
```

- [ ] **Step 4: Add models, placeholder textures and names.**

`models/item/rubricators_quill.json`:
```json
{
  "parent": "minecraft:item/generated",
  "textures": {"layer0": "hemomancy:item/rubricators_quill"}
}
```
`models/item/palimpsest_burin.json`:
```json
{
  "parent": "minecraft:item/generated",
  "textures": {"layer0": "hemomancy:item/palimpsest_burin"}
}
```
Placeholder textures (final art is a follow-up for the user):
```powershell
Copy-Item src/main/resources/assets/hemomancy/textures/item/engram_stamp.png src/main/resources/assets/hemomancy/textures/item/rubricators_quill.png
Copy-Item src/main/resources/assets/hemomancy/textures/item/vivianite_scalpel.png src/main/resources/assets/hemomancy/textures/item/palimpsest_burin.png
```
In `en_us.json`, directly after `"item.hemomancy.master_cam_kit": "Master Cam Kit",` add:
```json
  "item.hemomancy.rubricators_quill": "Rubricator's Quill",
  "item.hemomancy.palimpsest_burin": "Palimpsest Burin",
```

- [ ] **Step 5: Add the 7 missing recipes.** `vicars_consecration_kit.json` already exists; leave it as is.

`hematic_condenser_kit.json`:
```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [" G ", "BFB", " C "],
  "key": {
    "G": { "item": "hemomancy:sanguine_glass" },
    "B": { "item": "minecraft:glass_bottle" },
    "F": { "item": "hemomancy:ferric_binder" },
    "C": { "item": "hemomancy:blood_crystal_shard" }
  },
  "result": { "id": "hemomancy:hematic_condenser_kit" }
}
```
`sanguine_athanor_kit.json`:
```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [" Q ", "HBH", " T "],
  "key": {
    "Q": { "item": "hemomancy:sanguine_quintessence" },
    "H": { "item": "hemomancy:hemolytic_solution" },
    "B": { "item": "minecraft:brewing_stand" },
    "T": { "item": "minecraft:ghast_tear" }
  },
  "result": { "id": "hemomancy:sanguine_athanor_kit" }
}
```
`precision_governor_kit.json`:
```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [" A ", "WGW", " I "],
  "key": {
    "A": { "item": "minecraft:amethyst_shard" },
    "W": { "item": "hemomancy:wax_cylinder" },
    "G": { "item": "minecraft:grindstone" },
    "I": { "item": "hemomancy:hematic_iron_block" }
  },
  "result": { "id": "hemomancy:precision_governor_kit" }
}
```
`master_cam_kit.json`:
```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [" N ", "MAM", " S "],
  "key": {
    "N": { "item": "minecraft:netherite_scrap" },
    "M": { "item": "hemomancy:mnemonic_ambergris_block" },
    "A": { "item": "minecraft:anvil" },
    "S": { "item": "hemomancy:chalybeate_sclerite" }
  },
  "result": { "id": "hemomancy:master_cam_kit" }
}
```
`monolithic_cornerstone.json`:
```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": ["FCF", "QSQ", "FCF"],
  "key": {
    "F": { "item": "hemomancy:monolith_fragment" },
    "C": { "item": "hemomancy:monolith_imbued_cloth" },
    "Q": { "item": "hemomancy:sanguine_quintessence" },
    "S": { "item": "hemomancy:sclerite_block" }
  },
  "result": { "id": "hemomancy:monolithic_cornerstone" }
}
```
`rubricators_quill.json`:
```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [" I ", "CFC", " P "],
  "key": {
    "I": { "item": "minecraft:ink_sac" },
    "C": { "item": "hemomancy:cuttlefish_chromatophores" },
    "F": { "item": "minecraft:feather" },
    "P": { "item": "hemomancy:runic_motif_paper" }
  },
  "result": { "id": "hemomancy:rubricators_quill" }
}
```
`palimpsest_burin.json`:
```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [" E ", "MSM", " A "],
  "key": {
    "E": { "item": "minecraft:echo_shard" },
    "M": { "item": "hemomancy:myelin_sheath" },
    "S": { "item": "hemomancy:vivianite_scalpel" },
    "A": { "item": "hemomancy:axonal_slate" }
  },
  "result": { "id": "hemomancy:palimpsest_burin" }
}
```

- [ ] **Step 6: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "*StationUpgradeRecipeResourceTest"`
Expected: PASS.

- [ ] **Step 7: Checkpoint.** Run `./gradlew.bat runData` and confirm `src/generated/resources/assets/hemomancy/lang/en_us.json` does not override the two new names (main resources win because `processResources` excludes duplicates). Run `./gradlew.bat compileJava`. Do not commit.

---

### Task 3: Tier lives in the blockstate and survives breaking

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/StationTierProperty.java`
- Modify: `common/block/harbinger/crafting/EnzymaticScriptoriumBlock.java:31-39` (reuse the shared property)
- Modify: `common/block/harbinger/crafting/GhastlyAlembicBlock.java` (state definition, default state, `onRemove` lines 205-208)
- Modify: `common/block/harbinger/crafting/ResonantForgeBlock.java` (state definition, default state)
- Modify: `common/block/harbinger/crafting/HematicArmatureBlock.java` (state definition, default state, `onRemove` lines 280-287)
- Modify: `common/tile/harbinger/crafting/GhastlyAlembicBlockEntity.java` (fields 109, 167-193, 269, 284, 747-751, 795, 831, 854)
- Modify: `common/tile/harbinger/crafting/ResonantForgeBlockEntity.java` (field 47, all `tier` reads, 346-349, 399, 414)
- Modify: `common/tile/harbinger/crafting/HematicArmatureBlockEntity.java` (field 86, 192, 418, 439-443, 619-621, 640, 657, 674-676)
- Modify: `src/main/resources/assets/hemomancy/blockstates/resonant_forge.json`, `hematic_armature.json`
- Modify: `src/main/resources/data/hemomancy/loot_table/blocks/ghastly_alembic.json`, `resonant_forge.json`, `hematic_armature.json`
- Delete: `src/test/java/com/vincenthuto/hemomancy/common/block/harbinger/HematicArmatureBreakDropSourceTest.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/StationTierPersistenceResourceTest.java`
- Test: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/StationUpgradeGameTests.java` (create)
- Modify tests: remove `AdvancedBrewingGameTests.breakingAthanorDropsEachInstalledKitOnce` and `ArtificerProgressionGameTests.brokenUpgradedArmatureReturnsItsPaidUpgradeItems` (both replaced here).

**Interfaces:**
- Produces: `StationTierProperty.STAGE` (`IntegerProperty "stage" 0..2`), `static int stage(BlockState)`, `static boolean raiseTo(BlockEntity be, int targetStage)`, `static void applyLegacyStage(BlockEntity be, int legacyStage)`.
- Produces: each of the three block entities gains `public void applyLegacyStage()`, called at the top of its `serverTick`.
- Keeps (until Task 5): `GhastlyAlembicBlockEntity.completeUpgrade(AlembicTier, UUID)`, `ResonantForgeBlockEntity.completeUpgrade(ResonantForgeTier, UUID)`, `HematicArmatureBlockEntity.completeUpgrade(ArmatureTier, UUID)`, reimplemented with `raiseTo`.

- [ ] **Step 1: Write the failing resource test**

```java
package com.vincenthuto.hemomancy.common.station;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StationTierPersistenceResourceTest {
    private static final Path LOOT = Path.of("src/main/resources/data/hemomancy/loot_table/blocks");
    private static final Path STATES = Path.of("src/main/resources/assets/hemomancy/blockstates");
    private static final Path BLOCKS = Path.of("src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting");

    @Test
    void everyUpgradeableStationCopiesItsStageIntoTheDrop() throws IOException {
        for (UpgradeStation station : UpgradeStation.values()) {
            String loot = Files.readString(LOOT.resolve(station.blockId().getPath() + ".json"));
            assertTrue(loot.contains("minecraft:copy_state") && loot.contains("\"stage\""),
                    station.blockId() + " loot must copy its stage");
        }
    }

    @Test
    void variantBlockstatesNoLongerEnumerateEveryPropertyCombination() throws IOException {
        assertTrue(Files.readString(STATES.resolve("resonant_forge.json")).contains("\"multipart\""));
        assertTrue(Files.readString(STATES.resolve("hematic_armature.json")).contains("\"multipart\""));
    }

    @Test
    void breakingAStationNoLongerRefundsItsUpgradeItems() throws IOException {
        String alembic = Files.readString(BLOCKS.resolve("GhastlyAlembicBlock.java"));
        assertFalse(alembic.contains("hematic_condenser_kit"), "Alembic still refunds its condenser kit");
        assertFalse(alembic.contains("sanguine_athanor_kit"), "Alembic still refunds its athanor kit");
        String armature = Files.readString(BLOCKS.resolve("HematicArmatureBlock.java"));
        assertFalse(armature.contains("new ItemStack(ItemInit.vicars_consecration_kit.get())"));
        assertFalse(armature.contains("new ItemStack(ItemInit.monolithic_cornerstone.get())"));
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew.bat test --tests "*StationTierPersistenceResourceTest"`
Expected: FAIL on `ghastly_alembic loot must copy its stage`.

- [ ] **Step 3: Create the shared property helper**

```java
package com.vincenthuto.hemomancy.common.station;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Upgrade tier shared by every upgradeable station: 0 = base, 1 = first upgrade, 2 = second upgrade. */
public final class StationTierProperty {
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 2);

    private StationTierProperty() {}

    public static int stage(BlockState state) {
        return state.hasProperty(STAGE) ? state.getValue(STAGE) : 0;
    }

    public static boolean raiseTo(BlockEntity station, int targetStage) {
        Level level = station.getLevel();
        BlockState state = station.getBlockState();
        if (level == null || !state.hasProperty(STAGE)
                || !StationUpgradeRules.isNextTier(state.getValue(STAGE), targetStage)) return false;
        level.setBlock(station.getBlockPos(), state.setValue(STAGE, targetStage), 3);
        return true;
    }

    /** Moves a tier saved by pre-consolidation builds (block-entity NBT) into the blockstate. */
    public static void applyLegacyStage(BlockEntity station, int legacyStage) {
        Level level = station.getLevel();
        BlockState state = station.getBlockState();
        if (level == null || level.isClientSide || legacyStage <= 0 || !state.hasProperty(STAGE)
                || state.getValue(STAGE) >= legacyStage) return;
        level.setBlock(station.getBlockPos(), state.setValue(STAGE, Math.min(2, legacyStage)), 3);
    }
}
```

In `EnzymaticScriptoriumBlock.java`, replace lines 31-32 with:
```java
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty STAGE =
            com.vincenthuto.hemomancy.common.block.harbinger.crafting.StationTierProperty.STAGE;
```

- [ ] **Step 4: Add `STAGE` to the three other blocks.** In `GhastlyAlembicBlock`, `ResonantForgeBlock` and `HematicArmatureBlock`:
  - add `StationTierProperty.STAGE` to the `builder.add(...)` call in `createBlockStateDefinition`;
  - append `.setValue(StationTierProperty.STAGE, 0)` to the `registerDefaultState(...)` chain in the constructor;
  - import `com.vincenthuto.hemomancy.common.block.harbinger.crafting.StationTierProperty`.

  `getStateForPlacement` needs no change. `BlockItem` applies the dropped item's `BLOCK_STATE` component after placement, as the Scriptorium already relies on.

- [ ] **Step 5: Remove break refunds.** In `GhastlyAlembicBlock.onRemove`, delete:
```java
					if (alembic.tier().ordinal() >= 1)
						Block.popResource(level, pos, new ItemStack(ItemInit.hematic_condenser_kit.get()));
					if (alembic.tier().ordinal() >= 2)
						Block.popResource(level, pos, new ItemStack(ItemInit.sanguine_athanor_kit.get()));
```
In `HematicArmatureBlock.onRemove`, delete the two `if (!level.isClientSide && armature.getArmatureTier() ...) { Containers.dropItemStack(... kit / cornerstone ...) }` blocks (lines 280-287). Keep `Containers.dropContents` and `updateNeighbourForOutputSignal`.

- [ ] **Step 6: Blockstates and loot.** Replace `blockstates/resonant_forge.json` with:
```json
{
  "multipart": [
    { "when": { "facing": "north" }, "apply": { "model": "hemomancy:block/resonant_forge" } },
    { "when": { "facing": "east" },  "apply": { "model": "hemomancy:block/resonant_forge", "y": 90 } },
    { "when": { "facing": "south" }, "apply": { "model": "hemomancy:block/resonant_forge", "y": 180 } },
    { "when": { "facing": "west" },  "apply": { "model": "hemomancy:block/resonant_forge", "y": 270 } }
  ]
}
```
Replace `blockstates/hematic_armature.json` with:
```json
{
  "multipart": [
    { "when": { "facing": "north" }, "apply": { "model": "hemomancy:block/hematic_armature", "y": 180 } },
    { "when": { "facing": "south" }, "apply": { "model": "hemomancy:block/hematic_armature" } },
    { "when": { "facing": "west" },  "apply": { "model": "hemomancy:block/hematic_armature", "y": 90 } },
    { "when": { "facing": "east" },  "apply": { "model": "hemomancy:block/hematic_armature", "y": 270 } }
  ]
}
```
`ghastly_alembic.json` is already multipart; no change needed.

Replace each of the three loot tables, substituting `<block>` with `ghastly_alembic`, `resonant_forge` or `hematic_armature`:
```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "hemomancy:<block>",
          "functions": [
            { "function": "minecraft:copy_state", "block": "hemomancy:<block>", "properties": ["stage"] }
          ]
        }
      ],
      "conditions": [{ "condition": "minecraft:survives_explosion" }]
    }
  ]
}
```

- [ ] **Step 7: Block entities read the tier from the blockstate.**

`GhastlyAlembicBlockEntity`:
- Delete the field `private AlembicTier tier = AlembicTier.BASE;` (line 109) and add `private int legacyStage;`.
- Replace `tier()` (line 167) and `setTier` (179-188) with:
```java
	public AlembicTier tier() {
		return AlembicTier.fromSaved(StationTierProperty.stage(getBlockState()));
	}

	public void applyLegacyStage() {
		StationTierProperty.applyLegacyStage(this, legacyStage);
		legacyStage = 0;
	}
```
- Replace the body of `completeUpgrade(AlembicTier target, UUID riteId)` (line 190) with:
```java
		if (target == null || riteId == null || !StationTierProperty.raiseTo(this, target.ordinal())) return false;
		lastUpgradeRite = riteId;
		setRiteLocked(false);
		setChanged();
		return true;
```
  If `setRiteLocked` does not exist under that name, use the existing rite-lock setter found with `grep -n "RiteLocked" GhastlyAlembicBlockEntity.java`.
- In `serverTick` (line 269) add `te.applyLegacyStage();` as the first statement, and replace the remaining `te.tier` field reads (e.g. line 284 `BrewingResolver.resolve(level, te.tier, ...)`) with `te.tier()`. Replace every other bare `tier` field read in the class with `tier()`.
- In both load paths (lines 747-751 and 854) replace the `this.tier = AlembicTier.fromSaved(tag.getInt("AlembicTier"));` assignments (and the invalid-tier warning block) with `this.legacyStage = tag.contains("AlembicTier") ? AlembicTier.fromSaved(tag.getInt("AlembicTier")).ordinal() : 0;`.
- Delete both `tag.putInt("AlembicTier", tier.ordinal());` writes (lines 795 and 831).

`ResonantForgeBlockEntity`:
- Replace `private ResonantForgeTier tier = ResonantForgeTier.BASE;` with `private int legacyStage;`.
- Replace `public ResonantForgeTier tier() { return tier; }` with:
```java
    public ResonantForgeTier tier() { return ResonantForgeTier.values()[StationTierProperty.stage(getBlockState())]; }
    public void applyLegacyStage() { StationTierProperty.applyLegacyStage(this, legacyStage); legacyStage = 0; }
```
- Replace every other `tier` field read (`ResonantForgeRules.canUseMaster(tier)`, `canSelectIndividual(tier)`, ContainerData `case 6 -> tier.ordinal()`) with `tier()`.
- `completeUpgrade` (346-349) becomes:
```java
    public boolean completeUpgrade(ResonantForgeTier target, UUID riteId) {
        if (target == null || riteId == null || operation != Operation.NONE
                || !StationTierProperty.raiseTo(this, target.ordinal())) return false;
        lastUpgradeRite = riteId; riteLocked = false; sync(); return true;
    }
```
- Load (399): `legacyStage = tag.contains("Tier") ? Math.clamp(tag.getInt("Tier"), 0, 2) : 0;`. Save (414): remove `tag.putInt("Tier", tier.ordinal());`.
- `serverTick` (104): first statement `forge.applyLegacyStage();`.

`HematicArmatureBlockEntity`:
- Replace the `armatureTier` field (line 86) with `private int legacyStage;`.
- `getArmatureTier()` (418) becomes:
```java
	public ArmatureUpgradeRules.ArmatureTier getArmatureTier() {
		return ArmatureUpgradeRules.ArmatureTier.byId(StationTierProperty.stage(getBlockState()));
	}

	public void applyLegacyStage() {
		StationTierProperty.applyLegacyStage(this, legacyStage);
		legacyStage = 0;
	}
```
- Line 192 `findMatchingRecipe(..., armatureTier)` becomes `getArmatureTier()`.
- `completeUpgrade` (439-443): keep the existing guard clauses, but replace the `target.id() != armatureTier.id() + 1` check and the `armatureTier = target;` assignment with `!StationTierProperty.raiseTo(this, target.id())`.
- Both loads (619-621, 674-676): `legacyStage = tag.contains(TAG_ARMATURE_TIER) ? ArmatureUpgradeRules.ArmatureTier.byName(tag.getString(TAG_ARMATURE_TIER)).id() : 0;`. Remove both `tag.putString(TAG_ARMATURE_TIER, ...)` writes (640, 657).
- `serverTick` (96): first statement `armature.applyLegacyStage();`.

- [ ] **Step 8: Write the GameTests.** Create `StationUpgradeGameTests.java`:

```java
package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.StationTierProperty;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.HematicArmatureBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ResonantForgeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StationUpgradeGameTests {
    private StationUpgradeGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade")
    public static void upgradedStationsDropTheirStage(GameTestHelper helper) {
        for (Block block : List.of(BlockInit.ghastly_alembic.get(), BlockInit.resonant_forge.get(),
                BlockInit.hematic_armature.get(), BlockInit.enzymatic_scriptorium.get())) {
            BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
            BlockState state = withFacing(block.defaultBlockState()).setValue(StationTierProperty.STAGE, 2);
            helper.getLevel().setBlockAndUpdate(pos, state);
            List<ItemStack> drops = Block.getDrops(state, helper.getLevel(), pos, helper.getLevel().getBlockEntity(pos));
            var properties = drops.getFirst().get(DataComponents.BLOCK_STATE);
            helper.assertTrue(properties != null
                    && properties.apply(block.defaultBlockState()).getValue(StationTierProperty.STAGE) == 2,
                    block + " dropped without its stage");
            helper.getLevel().removeBlock(pos, false);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade")
    public static void legacyBlockEntityTierMovesIntoBlockstate(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        BlockPos alembicPos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(alembicPos, BlockInit.ghastly_alembic.get().defaultBlockState());
        var alembic = (GhastlyAlembicBlockEntity) helper.getLevel().getBlockEntity(alembicPos);
        CompoundTag alembicTag = alembic.saveWithoutMetadata(registries);
        alembicTag.putInt("AlembicTier", 2);
        alembic.loadWithComponents(alembicTag, registries);
        alembic.applyLegacyStage();
        helper.assertTrue(StationTierProperty.stage(helper.getLevel().getBlockState(alembicPos)) == 2,
                "Legacy Alembic tier was lost");

        BlockPos forgePos = helper.absolutePos(new BlockPos(5, 2, 5));
        helper.getLevel().setBlockAndUpdate(forgePos, BlockInit.resonant_forge.get().defaultBlockState());
        var forge = (ResonantForgeBlockEntity) helper.getLevel().getBlockEntity(forgePos);
        CompoundTag forgeTag = forge.saveWithoutMetadata(registries);
        forgeTag.putInt("Tier", 1);
        forge.loadWithComponents(forgeTag, registries);
        forge.applyLegacyStage();
        helper.assertTrue(StationTierProperty.stage(helper.getLevel().getBlockState(forgePos)) == 1,
                "Legacy Forge tier was lost");

        BlockPos armaturePos = helper.absolutePos(new BlockPos(1, 2, 6));
        helper.getLevel().setBlockAndUpdate(armaturePos, BlockInit.hematic_armature.get().defaultBlockState());
        var armature = (HematicArmatureBlockEntity) helper.getLevel().getBlockEntity(armaturePos);
        CompoundTag armatureTag = armature.saveWithoutMetadata(registries);
        armatureTag.putString("ArmatureTier", "monolithic");
        armature.loadWithComponents(armatureTag, registries);
        armature.applyLegacyStage();
        helper.assertTrue(StationTierProperty.stage(helper.getLevel().getBlockState(armaturePos)) == 2,
                "Legacy Armature tier was lost");
        helper.succeed();
    }

    static BlockState withFacing(BlockState state) {
        return state.hasProperty(HorizontalDirectionalBlock.FACING)
                ? state.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH) : state;
    }
}
```

Delete `HematicArmatureBreakDropSourceTest.java`. Delete `AdvancedBrewingGameTests.breakingAthanorDropsEachInstalledKitOnce` and `ArtificerProgressionGameTests.brokenUpgradedArmatureReturnsItsPaidUpgradeItems`, then remove any imports left unused.

- [ ] **Step 9: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "*StationTierPersistenceResourceTest"` and expect PASS.
Run: `./gradlew.bat runGameTestServer` and expect `upgradedStationsDropTheirStage` and `legacyBlockEntityTierMovesIntoBlockstate` to pass, with no new failures.
Run: `./gradlew.bat advancedBrewingGameTestServer scriptoriumGameTestServer` and expect no new failures. The upgrade-rite tests still pass because `completeUpgrade` keeps its signature.

- [ ] **Step 10: Checkpoint.** Launch `./gradlew.bat runClient` and place each of the four stations to confirm none renders the missing-model texture. Do not commit.

---

### Task 4: `UpgradeableStation` on all four block entities

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/UpgradeableStation.java`
- Modify: the four block entities under `common/tile/harbinger/crafting/`
- Modify: `common/rite/harbinger/ScriptoriumRites.java` (its enzyme calls go away; the whole file is deleted in Task 5)
- Test: `StationUpgradeGameTests.java` (add a test)

**Interfaces:**
- Consumes: `StationTierProperty.stage/raiseTo` (Task 3), `UpgradeStation` (Task 1).
- Produces:
```java
public interface UpgradeableStation {
    UpgradeStation upgradeStation();
    BlockState getBlockState();          // satisfied by BlockEntity
    BlockPos getBlockPos();              // satisfied by BlockEntity
    Level getLevel();                    // satisfied by BlockEntity
    UUID machineIdentity();
    boolean isRiteLocked();
    void setRiteLocked(boolean locked);
    boolean readyForUpgradeRite(ServerLevel level, Direction riteForward);
    CompoundTag upgradeSnapshot(HolderLookup.Provider registries);
    boolean wasUpgradedBy(UUID riteId);
    void markUpgradedBy(UUID riteId);
    default int upgradeTier() { ... }
    default boolean completeUpgrade(int targetTier, UUID riteId) { ... }
    default void onUpgraded(ServerPlayer owner) {}
}
```

- [ ] **Step 1: Write the failing GameTest** (add to `StationUpgradeGameTests`):

```java
    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade")
    public static void everyStationUpgradesOneStepThroughTheSharedInterface(GameTestHelper helper) {
        for (var station : com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.values()) {
            BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
            Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(station.blockId());
            helper.getLevel().setBlockAndUpdate(pos, withFacing(block.defaultBlockState()));
            var subject = (com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeableStation) helper.getLevel().getBlockEntity(pos);
            var rite = java.util.UUID.randomUUID();
            helper.assertTrue(subject.upgradeStation() == station, station + " reported the wrong station");
            helper.assertTrue(!subject.completeUpgrade(2, rite), station + " skipped a tier");
            helper.assertTrue(subject.completeUpgrade(1, rite) && subject.upgradeTier() == 1
                    && subject.wasUpgradedBy(rite) && !subject.isRiteLocked(), station + " did not upgrade");
            var identity = subject.machineIdentity();
            var reloaded = helper.getLevel().getBlockEntity(pos).saveWithoutMetadata(helper.getLevel().registryAccess());
            helper.assertTrue(reloaded.hasUUID("MachineIdentity") && reloaded.getUUID("MachineIdentity").equals(identity),
                    station + " does not persist its identity");
            helper.getLevel().removeBlock(pos, false);
        }
        helper.succeed();
    }
```

- [ ] **Step 2: Run to verify it fails.** Run `./gradlew.bat compileGameTestJava`. Expected: FAIL, `UpgradeableStation` does not exist.

- [ ] **Step 3: Create the interface**

```java
package com.vincenthuto.hemomancy.common.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/** A crafting station that climbs the shared two-step upgrade ladder through a Cardinal Rite. */
public interface UpgradeableStation {
    UpgradeStation upgradeStation();
    BlockState getBlockState();
    BlockPos getBlockPos();
    Level getLevel();
    UUID machineIdentity();
    boolean isRiteLocked();
    void setRiteLocked(boolean locked);
    /** Idle, unlocked, and (where the station cares) facing the rite and structurally complete. */
    boolean readyForUpgradeRite(ServerLevel level, Direction riteForward);
    /** Everything the rite must find unchanged at completion; excludes lock and upgrade bookkeeping. */
    CompoundTag upgradeSnapshot(HolderLookup.Provider registries);
    boolean wasUpgradedBy(UUID riteId);
    void markUpgradedBy(UUID riteId);

    default int upgradeTier() {
        return StationTierProperty.stage(getBlockState());
    }

    default boolean completeUpgrade(int targetTier, UUID riteId) {
        if (riteId == null || !(this instanceof BlockEntity self) || !StationTierProperty.raiseTo(self, targetTier))
            return false;
        markUpgradedBy(riteId);
        setRiteLocked(false);
        self.setChanged();
        return true;
    }

    /** Hook for station-specific progression side effects after a rite raises the tier. */
    default void onUpgraded(ServerPlayer owner) {}
}
```

- [ ] **Step 4: Implement on each block entity.** Add `implements com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeableStation` to each class declaration, then add these members.

`GhastlyAlembicBlockEntity`:
```java
	@Override public UpgradeStation upgradeStation() { return UpgradeStation.ALEMBIC; }
	@Override public boolean readyForUpgradeRite(ServerLevel level, Direction riteForward) {
		return !isProcessing() && !isRiteLocked();
	}
	@Override public CompoundTag upgradeSnapshot(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		ContainerHelper.saveAllItems(tag, items, registries);
		tag.putDouble("BloodLevel", getBloodVolume());
		return tag;
	}
	@Override public void markUpgradedBy(UUID riteId) { lastUpgradeRite = riteId; setChanged(); }
```
The existing `completeUpgrade(AlembicTier, UUID)` becomes `return target != null && completeUpgrade(target.ordinal(), riteId);`.

`ResonantForgeBlockEntity`:
```java
    @Override public UpgradeStation upgradeStation() { return UpgradeStation.RESONANT_FORGE; }
    @Override public boolean readyForUpgradeRite(ServerLevel level, Direction riteForward) {
        return idle() && getBlockState().getValue(ResonantForgeBlock.FACING) == riteForward
                && ResonantForgeBlock.hasCompleteStructure(level, worldPosition, getBlockState());
    }
    @Override public void markUpgradedBy(UUID riteId) { lastUpgradeRite = riteId; sync(); }
```
Its typed `completeUpgrade` becomes `return target != null && operation == Operation.NONE && completeUpgrade(target.ordinal(), riteId);`. `upgradeSnapshot`, `machineIdentity`, `isRiteLocked`, `setRiteLocked` and `wasUpgradedBy` already exist; add `@Override`.

`HematicArmatureBlockEntity`:
```java
	@Override public UpgradeStation upgradeStation() { return UpgradeStation.ARMATURE; }
	@Override public boolean readyForUpgradeRite(ServerLevel level, Direction riteForward) {
		return idleForRite() && getBlockState().getValue(HematicArmatureBlock.FACING) == riteForward;
	}
	@Override public void markUpgradedBy(UUID riteId) { lastUpgradeRite = riteId; setChanged(); }
	@Override public void onUpgraded(ServerPlayer owner) {
		ArtificerAssignments.onArmatureTierApplied(owner, getArmatureTier());
	}
```
Match the field name of the last-upgrade UUID (`grep -n "lastUpgradeRite\|TAG_LAST_UPGRADE_RITE" HematicArmatureBlockEntity.java`). The typed `completeUpgrade` becomes `return target != null && completeUpgrade(target.id(), riteId);`.

`EnzymaticScriptoriumBlockEntity`: add identity and bookkeeping, and remove the enzyme rite methods.
```java
    private java.util.UUID machineIdentity = java.util.UUID.randomUUID();
    private java.util.UUID lastUpgradeRite;

    @Override public UpgradeStation upgradeStation() { return UpgradeStation.SCRIPTORIUM; }
    @Override public java.util.UUID machineIdentity() { return machineIdentity; }
    @Override public boolean readyForUpgradeRite(ServerLevel level, Direction riteForward) { return !riteLocked; }
    @Override public CompoundTag upgradeSnapshot(HolderLookup.Provider registries) {
        CompoundTag tag = saveWithoutMetadata(registries);
        tag.remove("RiteLocked");
        tag.remove("LastUpgradeRite");
        tag.remove("Selected");
        tag.remove("neoforge:attachments");
        return tag;
    }
    @Override public boolean wasUpgradedBy(java.util.UUID riteId) { return riteId != null && riteId.equals(lastUpgradeRite); }
    @Override public void markUpgradedBy(java.util.UUID riteId) { lastUpgradeRite = riteId; sync(); }
```
In `loadAdditional` add:
```java
        if (tag.hasUUID("MachineIdentity")) machineIdentity = tag.getUUID("MachineIdentity");
        lastUpgradeRite = tag.hasUUID("LastUpgradeRite") ? tag.getUUID("LastUpgradeRite") : null;
```
In `saveAdditional` add:
```java
        tag.putUUID("MachineIdentity", machineIdentity);
        if (lastUpgradeRite != null) tag.putUUID("LastUpgradeRite", lastUpgradeRite);
```
Delete `hasRiteEnzymes(int)` and `consumeRiteEnzymes(int)` (lines 81-91). In `ScriptoriumRites.java`, which is deleted in Task 5, remove `&& station.hasRiteEnzymes(each)` and the `old.consumeRiteEnzymes(...)` line, and replace them with `old.setRiteLocked(false)` so the build compiles in between.

- [ ] **Step 5: Run the tests to verify they pass.** Run `./gradlew.bat runGameTestServer` and expect `everyStationUpgradesOneStepThroughTheSharedInterface` PASS. Run `./gradlew.bat scriptoriumGameTestServer` and expect every failure to be limited to `eightfoldUpgradeConsumesExactlyOneOfEach` and `monolithicSubjectRejectsMissingEnzymeAndNeedsTwoCircuits`, which assert the removed enzyme behavior. Delete both now, since Task 5 replaces them.

- [ ] **Step 6: Checkpoint.** `./gradlew.bat build`. Do not commit.

---

### Task 5: One rite handler (`StationUpgradeRites`) and the rite state rename

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/StationUpgradeRites.java`
- Delete: `AlembicUpgradeRites.java`, `ResonantForgeUpgradeRites.java`, `ArmatureUpgradeRites.java`, `ScriptoriumRites.java`
- Modify: `common/rite/CardinalRitePhase.java`
- Modify: `common/rite/ActiveCardinalRite.java` (fields 49-50, 65-66, 280-340, 848-851, 896-897, 926, 1007, 1015, 1032-1034, 1058)
- Modify: `common/rite/harbinger/HarbingerCardinalRiteEvents.java` (183-190, 221, 257, 282, 339, 355-358, 368, 377, 387-388, 413, 628, 704, 964-981, 1112-1124, 1267-1271, 1348)
- Modify: `common/rite/harbinger/CardinalRiteInteractionHandler.java:66-101`
- Modify: `common/rite/harbinger/CardinalRiteOrdealEngine.java:99, 212`
- Modify: `common/network/capa/harbinger/BloodCraftingKeyPressPacket.java:526, 574-589`
- Modify: `client/render/tile/harbinger/crafting/GhastlyAlembicRenderer.java:64-74`
- Modify: `client/render/world/CardinalRiteBoundaryRenderer.java:1099-1110`
- Modify tests: `CardinalRiteStructureFailureSourceTest.java:22`, `CardinalRiteProgressionResourceTest.java:150-161`; delete superseded upgrade GameTests (listed in Step 8).
- Test: `StationUpgradeGameTests.java`

**Interfaces:**
- Consumes: `StationUpgradeCatalog.forRite/get/OFFERINGS_PER_RITE`, `StationUpgradeTier` (Task 1), `UpgradeableStation` (Task 4).
- Produces: `StationUpgradeRites.isRite(ResourceLocation)`, `circuits(ResourceLocation)`, `bloodPerCircuit(ResourceLocation)`, `seat(ActiveCardinalRite)`, `target(ActiveCardinalRite,int)`, `targetSurface(ServerLevel,ActiveCardinalRite,int)`, `station(ServerLevel,ActiveCardinalRite)` → `UpgradeableStation` or null, `prepare`, `subjectPresent`, `focusPresent`, `complete`, `recover`, `cleanup` (all `(ServerLevel, ActiveCardinalRite)`).
- Produces: `CardinalRitePhase.STATION_PROJECTION`, `ActiveCardinalRite.upgrade()` (CompoundTag), `upgradeBloodNeeded()`, `fillUpgradeCircuit(int circuit, int paid)`.

- [ ] **Step 1: Write the failing GameTests** (add to `StationUpgradeGameTests`, plus the imports they need):

```java
    // ---- rite helpers ----
    static com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite upgradeRite(GameTestHelper helper,
            com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeTier tier) {
        BlockPos center = helper.absolutePos(new BlockPos(5, 3, 5));
        var rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(java.util.UUID.randomUUID(),
                center, tier.rite(), 2400, tier.requiredDegree(), tier.requiredDegree(), false, 0, tier.anchors());
        rite.setMatchedFloor(net.minecraft.resources.ResourceLocation.parse(tier.floor()), Direction.NORTH, Direction.UP);
        helper.getLevel().setBlockAndUpdate(center, BlockInit.cardinal_focus.get().defaultBlockState());
        BlockPos seat = com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.seat(rite);
        Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(tier.station().blockId());
        BlockState state = withFacing(block.defaultBlockState()).setValue(StationTierProperty.STAGE, tier.tier() - 1);
        helper.getLevel().setBlockAndUpdate(seat, state);
        block.setPlacedBy(helper.getLevel(), seat, state, null, new ItemStack(block));
        java.util.List<com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.RiteOffering> offerings = new java.util.ArrayList<>();
        for (int i = 0; i < com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog.OFFERINGS_PER_RITE; i++) {
            BlockPos brazierPos = center.offset(i - 3, 0, -3);
            helper.getLevel().setBlockAndUpdate(brazierPos, BlockInit.iron_brazier.get().defaultBlockState());
            ItemStack offering = i == 0
                    ? new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(tier.upgradeItem()))
                    : new ItemStack(net.minecraft.world.item.Items.STICK);
            ((com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity)
                    helper.getLevel().getBlockEntity(brazierPos)).insertOffering(null, offering.copy());
            offerings.add(new com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.RiteOffering(brazierPos, offering, true));
        }
        rite.captureOfferingItinerary(offerings);
        return rite;
    }

    static void assertUpgradeCompletes(GameTestHelper helper, com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation station, int tierNumber) {
        var tier = com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog.get(station, tierNumber);
        var rite = upgradeRite(helper, tier);
        var level = helper.getLevel();
        helper.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.prepare(level, rite),
                tier.ritePath() + " preparation failed");
        var subject = com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.station(level, rite);
        helper.assertTrue(subject.isRiteLocked(), tier.ritePath() + " did not lock");
        for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, 50);
        helper.assertTrue(rite.enterInscription() && rite.sealAltar(false)
                && rite.getPhase() == com.vincenthuto.hemomancy.common.rite.CardinalRitePhase.STATION_PROJECTION,
                tier.ritePath() + " did not enter projection");
        helper.assertTrue(!rite.fillUpgradeCircuit(1, tier.bloodPerCircuit()), "accepted an out-of-order circuit");
        for (int circuit = 0; circuit < tier.circuits(); circuit++)
            helper.assertTrue(rite.fillUpgradeCircuit(circuit, tier.bloodPerCircuit()),
                    tier.ritePath() + " rejected circuit " + circuit);
        rite.markComplete();
        helper.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.complete(level, rite)
                && subject.upgradeTier() == tierNumber && !subject.isRiteLocked()
                && rite.upgrade().getString("EscrowState").equals("CONSUMED"), tier.ritePath() + " did not complete");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_alembic_1")
    public static void alembicCondenserUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.ALEMBIC, 1); }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_alembic_2")
    public static void alembicAthanorUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.ALEMBIC, 2); }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_forge_1")
    public static void forgePrecisionUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.RESONANT_FORGE, 1); }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_forge_2")
    public static void forgeMasterworkUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.RESONANT_FORGE, 2); }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_armature_1")
    public static void armatureConsecrationUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.ARMATURE, 1); }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_armature_2")
    public static void armatureMonolithicUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.ARMATURE, 2); }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_scriptorium_1")
    public static void scriptoriumEightfoldUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.SCRIPTORIUM, 1); }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_scriptorium_2")
    public static void scriptoriumPalimpsestUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.SCRIPTORIUM, 2); }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_recover")
    public static void everyStationRecoversEscrowOnceAndUnlocks(GameTestHelper helper) {
        for (var station : com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.values()) {
            var rite = upgradeRite(helper, com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog.get(station, 1));
            var level = helper.getLevel();
            helper.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.prepare(level, rite), station + " prepare");
            var subject = com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.station(level, rite);
            com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.recover(level, rite);
            helper.assertTrue(!subject.isRiteLocked() && subject.upgradeTier() == 0
                    && rite.upgrade().getString("EscrowState").equals("RETURNED"), station + " recovery");
            helper.assertTrue(rite.upgrade().getList("Offerings", net.minecraft.nbt.Tag.TAG_COMPOUND).size()
                    == com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog.OFFERINGS_PER_RITE, station + " escrow size");
            com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.recover(level, rite);
            helper.assertTrue(rite.upgrade().getString("EscrowState").equals("RETURNED"), station + " recovered twice");
            level.removeBlock(subject.getBlockPos(), false);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_recover")
    public static void brokenSubjectReturnsEscrowOnce(GameTestHelper helper) {
        var tier = com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog.get(
                com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.SCRIPTORIUM, 1);
        var rite = upgradeRite(helper, tier);
        var level = helper.getLevel();
        helper.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.prepare(level, rite), "prepare");
        level.removeBlock(com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.seat(rite), false);
        helper.assertTrue(!com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.subjectPresent(level, rite),
                "missing subject still counted as present");
        com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.recover(level, rite);
        helper.assertTrue(rite.upgrade().getString("EscrowState").equals("RETURNED"), "escrow not returned");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_recover")
    public static void legacySavedRitesResumeOrUnlock(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        var level = helper.getLevel();
        // 1) old Alembic-era key and phase name resume as STATION_PROJECTION
        var forgeRite = upgradeRite(helper, com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog.get(
                com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.RESONANT_FORGE, 1));
        helper.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.prepare(level, forgeRite), "prepare");
        CompoundTag saved = forgeRite.serialize(registries);
        saved.putString("Phase", "ALEMBIC_PROJECTION");
        saved.put("AlembicUpgrade", saved.getCompound("StationUpgrade"));
        saved.remove("StationUpgrade");
        var resumed = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.deserialize(saved, registries);
        helper.assertTrue(resumed.getPhase() == com.vincenthuto.hemomancy.common.rite.CardinalRitePhase.STATION_PROJECTION
                && resumed.upgrade().getString("EscrowState").equals("ESCROWED"), "legacy projection did not resume");
        com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.recover(level, resumed);
        level.removeBlock(com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.seat(forgeRite), false);

        // 2) pre-consolidation Scriptorium rite (no escrow, old id, removed phase) collapses and unlocks
        var scriptRite = upgradeRite(helper, com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog.get(
                com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.SCRIPTORIUM, 2));
        var subject = com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.station(level, scriptRite);
        subject.setRiteLocked(true);
        CompoundTag old = scriptRite.serialize(registries);
        old.putString("RecipeId", "hemomancy:cardinal_rite/monolithic_script");
        old.putString("Phase", "SCRIPTORIAL_INSCRIPTION");
        old.remove("StationUpgrade");
        var legacy = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.deserialize(old, registries);
        helper.assertTrue(legacy.getRecipeId().getPath().equals("cardinal_rite/palimpsest")
                && legacy.getPhase() == com.vincenthuto.hemomancy.common.rite.CardinalRitePhase.COLLAPSED,
                "legacy Scriptorium rite was not collapsed onto the new id");
        com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.recover(level, legacy);
        helper.assertTrue(!subject.isRiteLocked(), "legacy Scriptorium rite left the station locked");
        helper.succeed();
    }
```
From Task 5 onward, `StationUpgradeGameTests` also needs the imports `net.minecraft.core.registries.BuiltInRegistries`, `net.minecraft.server.level.ServerPlayer`, `net.minecraft.world.item.Items`, `com.vincenthuto.hemomancy.common.init.ItemInit`, `com.vincenthuto.hemomancy.common.capability.*`, and `com.vincenthuto.hemomancy.common.station.*`. Add them as each task's tests first use them.

- [ ] **Step 2: Run to verify it fails.** Run `./gradlew.bat compileGameTestJava`. Expected: FAIL, `StationUpgradeRites` / `STATION_PROJECTION` / `upgrade()` not found.

- [ ] **Step 3: Create `StationUpgradeRites.java`**

```java
package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeTier;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeableStation;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Prepares, escrows, completes and recovers every station-upgrade Cardinal Rite. */
public final class StationUpgradeRites {
    private static final String ESCROWED = "ESCROWED";
    private static final String CONSUMED = "CONSUMED";
    private static final String RETURNED = "RETURNED";

    private StationUpgradeRites() {}

    public static boolean isRite(ResourceLocation id) {
        return StationUpgradeCatalog.forRite(id).isPresent();
    }

    public static int circuits(ResourceLocation id) {
        return StationUpgradeCatalog.forRite(id).map(StationUpgradeTier::circuits).orElse(0);
    }

    public static int bloodPerCircuit(ResourceLocation id) {
        return StationUpgradeCatalog.forRite(id).map(StationUpgradeTier::bloodPerCircuit).orElse(0);
    }

    private static Optional<StationUpgradeTier> tier(ActiveCardinalRite rite) {
        return StationUpgradeCatalog.forRite(rite.getRecipeId());
    }

    private static Direction forward(ActiveCardinalRite rite) {
        return rite.getFloorForwards() == null ? Direction.NORTH : rite.getFloorForwards();
    }

    public static BlockPos seat(ActiveCardinalRite rite) {
        int distance = tier(rite).map(t -> t.station().seatDistance()).orElse(2);
        return rite.getCenterPos().above().relative(forward(rite).getOpposite(), distance);
    }

    public static BlockPos target(ActiveCardinalRite rite, int circuit) {
        Direction forward = forward(rite);
        Direction right = forward.getClockWise();
        BlockPos center = rite.getCenterPos();
        return circuits(rite.getRecipeId()) == 2
                ? center.relative(forward).relative(right, circuit == 0 ? -1 : 1)
                : center.relative(forward, 2).relative(right, circuit * 2 - 2);
    }

    public static Vec3 targetSurface(ServerLevel level, ActiveCardinalRite rite, int circuit) {
        BlockPos target = target(rite, circuit);
        BlockPos center = rite.getCenterPos();
        BlockPos air = CardinalRiteSigilRules.surfaceAirPosition(level, center,
                target.getX() - center.getX(), target.getZ() - center.getZ());
        return new Vec3(target.getX() + .5D, air.getY() + .1D, target.getZ() + .5D);
    }

    public static UpgradeableStation station(ServerLevel level, ActiveCardinalRite rite) {
        Optional<StationUpgradeTier> tier = tier(rite);
        return tier.isPresent() && level.getBlockEntity(seat(rite)) instanceof UpgradeableStation station
                && station.upgradeStation() == tier.get().station() ? station : null;
    }

    public static boolean prepare(ServerLevel level, ActiveCardinalRite rite) {
        Optional<StationUpgradeTier> tier = tier(rite);
        if (tier.isEmpty()) return true;
        UpgradeableStation station = station(level, rite);
        if (station == null || station.isRiteLocked() || station.upgradeTier() != tier.get().tier() - 1
                || !station.readyForUpgradeRite(level, forward(rite))) return false;
        List<ActiveCardinalRite.RiteOffering> offerings = rite.getOfferingItinerary();
        if (offerings.size() != StationUpgradeCatalog.OFFERINGS_PER_RITE) return false;
        for (var offering : offerings) {
            if (!(level.getBlockEntity(offering.pos()) instanceof IronBrazierBlockEntity brazier)
                    || offering.stack() == null
                    || !ItemStack.isSameItemSameComponents(offering.stack(), brazier.getOfferingForMatching()))
                return false;
        }
        CompoundTag state = rite.upgrade();
        state.putUUID("RiteIdentity", UUID.randomUUID());
        state.putUUID("SubjectIdentity", station.machineIdentity());
        state.putInt("SourceTier", station.upgradeTier());
        state.put("SubjectSnapshot", station.upgradeSnapshot(level.registryAccess()));
        ListTag escrow = new ListTag();
        state.put("Offerings", escrow);
        state.putString("EscrowState", ESCROWED);
        state.putInt("Stage", 0);
        state.putInt("BloodAtStage", 0);
        station.setRiteLocked(true);
        for (var offering : offerings) {
            ItemStack consumed = ((IronBrazierBlockEntity) level.getBlockEntity(offering.pos())).consumeOffering();
            if (consumed.isEmpty()) {
                recover(level, rite);
                return false;
            }
            escrow.add(consumed.save(level.registryAccess()));
        }
        return true;
    }

    public static boolean subjectPresent(ServerLevel level, ActiveCardinalRite rite) {
        UpgradeableStation station = station(level, rite);
        CompoundTag state = rite.upgrade();
        return station != null && state.hasUUID("SubjectIdentity")
                && station.machineIdentity().equals(state.getUUID("SubjectIdentity"))
                && station.upgradeTier() == state.getInt("SourceTier");
    }

    public static boolean focusPresent(ServerLevel level, ActiveCardinalRite rite) {
        return level.getBlockState(rite.getCenterPos()).is(BlockInit.cardinal_focus.get());
    }

    public static boolean complete(ServerLevel level, ActiveCardinalRite rite) {
        Optional<StationUpgradeTier> tier = tier(rite);
        if (tier.isEmpty()) return true;
        CompoundTag state = rite.upgrade();
        UpgradeableStation station = station(level, rite);
        UUID riteId = state.hasUUID("RiteIdentity") ? state.getUUID("RiteIdentity") : null;
        if (state.getString("EscrowState").equals(CONSUMED)) {
            if (station == null || !station.wasUpgradedBy(riteId)) return false;
            releaseStaff(level, rite, riteId);
            station.setRiteLocked(false);
            return true;
        }
        if (!rite.isComplete() || !subjectPresent(level, rite)
                || state.getInt("Stage") != tier.get().circuits()
                || !station.upgradeSnapshot(level.registryAccess()).equals(state.getCompound("SubjectSnapshot"))
                || !station.completeUpgrade(tier.get().tier(), riteId)) {
            recover(level, rite);
            return false;
        }
        notifyOwner(level, rite, station);
        state.putString("EscrowState", CONSUMED);
        state.remove("Offerings");
        releaseStaff(level, rite, riteId);
        CardinalRiteSavedData.get(level).setDirty();
        return true;
    }

    public static void recover(ServerLevel level, ActiveCardinalRite rite) {
        if (!isRite(rite.getRecipeId())) return;
        CompoundTag state = rite.upgrade();
        String escrowState = state.getString("EscrowState");
        if (escrowState.isEmpty()) {
            // Started before offerings were escrowed (pre-consolidation Scriptorium): nothing was taken.
            ItemStack staff = rite.releaseEscrowedStaff(level.registryAccess());
            if (!staff.isEmpty()) CardinalRiteSavedData.get(level.getServer().overworld()).addRecovery(
                    rite.getPlayerUUID(), UUID.nameUUIDFromBytes((rite.getPlayerUUID() + ":" + rite.getRecipeId()
                            + ":legacy_staff").getBytes(StandardCharsets.UTF_8)), List.of(staff));
            UpgradeableStation station = station(level, rite);
            if (station != null) station.setRiteLocked(false);
            return;
        }
        if (!escrowState.equals(ESCROWED)) return;
        UpgradeableStation station = station(level, rite);
        UUID riteId = state.getUUID("RiteIdentity");
        if (station != null && state.hasUUID("SubjectIdentity")
                && station.machineIdentity().equals(state.getUUID("SubjectIdentity"))
                && station.wasUpgradedBy(riteId)) {
            state.putString("EscrowState", CONSUMED);
            state.remove("Offerings");
            notifyOwner(level, rite, station);
            releaseStaff(level, rite, riteId);
            station.setRiteLocked(false);
            CardinalRiteSavedData.get(level).setDirty();
            return;
        }
        state.putString("EscrowState", RETURNED);
        List<ItemStack> items = new ArrayList<>();
        ListTag escrow = state.getList("Offerings", Tag.TAG_COMPOUND);
        for (int i = 0; i < escrow.size(); i++)
            items.add(ItemStack.parseOptional(level.registryAccess(), escrow.getCompound(i)));
        ItemStack staff = rite.releaseEscrowedStaff(level.registryAccess());
        if (!staff.isEmpty()) items.add(staff);
        CardinalRiteSavedData saved = CardinalRiteSavedData.get(level.getServer().overworld());
        saved.addRecovery(rite.getPlayerUUID(), riteId, items);
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(rite.getPlayerUUID());
        if (owner != null && owner.level() == level && owner.isAlive()) saved.deliverRecovery(owner);
        cleanup(level, rite);
    }

    public static void cleanup(ServerLevel level, ActiveCardinalRite rite) {
        if (!isRite(rite.getRecipeId())) return;
        UpgradeableStation station = station(level, rite);
        if (station != null && rite.upgrade().hasUUID("SubjectIdentity")
                && station.machineIdentity().equals(rite.upgrade().getUUID("SubjectIdentity")))
            station.setRiteLocked(false);
    }

    private static void notifyOwner(ServerLevel level, ActiveCardinalRite rite, UpgradeableStation station) {
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(rite.getPlayerUUID());
        if (owner != null) station.onUpgraded(owner);
    }

    private static void releaseStaff(ServerLevel level, ActiveCardinalRite rite, UUID riteId) {
        ItemStack staff = rite.releaseEscrowedStaff(level.registryAccess());
        if (!staff.isEmpty()) CardinalRiteSavedData.get(level.getServer().overworld())
                .addRecovery(rite.getPlayerUUID(), UUID.nameUUIDFromBytes(
                        (riteId + ":staff").getBytes(StandardCharsets.UTF_8)), List.of(staff));
    }
}
```

- [ ] **Step 4: Phase enum.** In `CardinalRitePhase`:
  - remove `SCRIPTORIAL_INSCRIPTION`;
  - rename `ALEMBIC_PROJECTION` to `STATION_PROJECTION`;
  - make `byName` map legacy names:
```java
	public static CardinalRitePhase byName(String name) {
		if (name == null || name.isBlank()) return LEGACY;
		if (name.equals("ALEMBIC_PROJECTION")) return STATION_PROJECTION;
		// The Scriptorium's orb inscription was removed; its rites never escrowed offerings, so collapse safely.
		if (name.equals("SCRIPTORIAL_INSCRIPTION")) return COLLAPSED;
		try {
			return valueOf(name);
		} catch (IllegalArgumentException ignored) {
			return LEGACY;
		}
	}
```

- [ ] **Step 5: `ActiveCardinalRite`.**
  - Delete `scriptorialBloodMl`, `scriptorialStage` (49-50), `getScriptorialStage`, `getScriptorialBlood`, `scriptorialBloodNeeded` and `fillScriptorialOrb` (298, 320-340), and their save/load lines (896-897, 1032-1034). Old saves simply ignore those tags.
  - Rename the field `alembic` to `upgrade`, and the accessor `alembic()` to `upgrade()`.
  - Replace the two `sealAltar` branches (284-291) with:
```java
		if (com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.isRite(recipeId)) {
			setPhase(CardinalRitePhase.STATION_PROJECTION);
			return true;
		}
```
  - Replace `alembicBloodNeeded` / `fillAlembicProjection` (299-319) with:
```java
	public int upgradeBloodNeeded() {
		return phase == CardinalRitePhase.STATION_PROJECTION
				? com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.bloodPerCircuit(recipeId)
						- upgrade.getInt("BloodAtStage") : 0;
	}
	public boolean fillUpgradeCircuit(int circuit, int paid) {
		if (phase != CardinalRitePhase.STATION_PROJECTION || circuit != upgrade.getInt("Stage")
				|| paid <= 0 || upgradeBloodNeeded() <= 0) return false;
		int circuitCost = com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.bloodPerCircuit(recipeId);
		int next = Math.min(circuitCost, upgrade.getInt("BloodAtStage") + paid);
		committedBloodMl += next - upgrade.getInt("BloodAtStage");
		upgrade.putInt("BloodAtStage", next);
		if (next == circuitCost) {
			upgrade.putInt("Stage", circuit + 1);
			upgrade.putInt("BloodAtStage", 0);
			if (circuit + 1 == com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.circuits(recipeId))
				setPhase(finalePhase());
		}
		return true;
	}
```
  - Progress switch (848-851): delete the `SCRIPTORIAL_INSCRIPTION` case and rename the other one:
```java
			case STATION_PROJECTION -> 0.25D + 0.55D * (upgrade.getInt("Stage")
					+ upgrade.getInt("BloodAtStage") / (double) com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.bloodPerCircuit(recipeId))
					/ com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.circuits(recipeId);
```
  - Save (926): `if (!upgrade.isEmpty()) tag.put("StationUpgrade", upgrade.copy());`
  - Load (1058): `rite.upgrade = (tag.contains("StationUpgrade") ? tag.getCompound("StationUpgrade") : tag.getCompound("AlembicUpgrade")).copy();`
  - Load (1007): after parsing, alias the renamed rite id:
```java
		ResourceLocation recipeId = ResourceLocation.parse(tag.getString("RecipeId"));
		if (recipeId.getNamespace().equals("hemomancy") && recipeId.getPath().equals("cardinal_rite/monolithic_script"))
			recipeId = ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/palimpsest");
```

- [ ] **Step 6: Call sites.** Apply these edits:
  - `HarbingerCardinalRiteEvents`: replace every `AlembicUpgradeRites.` with `StationUpgradeRites.` (lines 183-190, 221, 257, 282, 339, 355-358, 368, 377, 388, 413, 628, 704, 1267-1268).
  - Delete `ScriptoriumRites.cleanup(sLevel, removedRite);` (387).
  - Delete `if (!ScriptoriumRites.complete(sLevel, rite)) return false;` (1348). Upgrade rites already return from `completeRite` at 1267.
  - Delete the `SCRIPTORIAL_INSCRIPTION` checklist block (964-970) and the Scriptorium branch of `visibleSigilSegments` (1112-1124).
  - Replace the projection checklist block (971-981) with:
```java
		if (rite.getPhase() == CardinalRitePhase.STATION_PROJECTION) {
			int stage = rite.upgrade().getInt("Stage");
			int cost = StationUpgradeRites.bloodPerCircuit(rite.getRecipeId());
			return java.util.List.of("Station circuits: " + stage + "/" + StationUpgradeRites.circuits(rite.getRecipeId()),
					"Current circuit: " + (cost - rite.upgradeBloodNeeded()) + "/" + cost + " mL",
					"Project blood into the next station circuit");
		}
```
  - `CardinalRiteInteractionHandler` (66-101): rename the phase to `STATION_PROJECTION`, `alembic()` to `upgrade()`, `AlembicUpgradeRites.projections` to `StationUpgradeRites.circuits`, `AlembicUpgradeRites.targetSurface/target` to `StationUpgradeRites.*`, `alembicBloodNeeded` to `upgradeBloodNeeded`, and `fillAlembicProjection` to `fillUpgradeCircuit`. Delete the whole `SCRIPTORIAL_INSCRIPTION` block (86-101).
  - `CardinalRiteOrdealEngine` 99 and 212: replace `AlembicUpgradeRites` with `StationUpgradeRites`.
  - `BloodCraftingKeyPressPacket` 526: `StationUpgradeRites.isRite(recipe.getId()) ? 0`. Delete the `ScriptoriumRites.prepare` block (574-579). Replace the `AlembicUpgradeRites.prepare` block (580-589) with:
```java
				if (!com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.prepare(sLevel, rite)) {
					CardinalRiteStaffEscrow.restore(serverPlayer, rite);
					player.displayClientMessage(Component.literal(
							"Seat an idle station of the required tier behind the Focus, facing it, with its upgrade item and five offerings on lit braziers.")
							.withStyle(ChatFormatting.DARK_RED), false);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}
```
  - `GhastlyAlembicRenderer.ghostActive` (64-74): replace the hard-coded rite paths and the `"ALEMBIC_PROJECTION"` string with a catalog lookup limited to Alembic rites:
```java
		return ActiveRiteClientData.getActiveRites().stream().anyMatch(rite ->
				com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog.forRite(rite.getRecipeId())
						.filter(tier -> tier.station() == com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation.ALEMBIC).isPresent()
						&& rite.getCenter().distManhattan(station.getBlockPos()) <= 2
						&& (rite.getPhase().equals("STATION_PROJECTION")
						|| rite.getPhase().equals("OFFERING_PROCESSION")
						|| rite.getPhase().equals("CULMINATION")));
```
  - `CardinalRiteBoundaryRenderer` 1099-1110: delete the `if (!glow && ScriptoriumRites.isRite(...)) { ... }` block.
  - Delete the four old rite classes.
  - Run `grep -rn "AlembicUpgradeRites\|ResonantForgeUpgradeRites\|ArmatureUpgradeRites\|ScriptoriumRites\|ALEMBIC_PROJECTION\|SCRIPTORIAL_INSCRIPTION\|fillAlembicProjection\|alembicBloodNeeded\|\.alembic()" src` and fix every remaining hit, excluding the legacy-name strings in `CardinalRitePhase.byName`.

- [ ] **Step 7: Existing JVM tests.**
  - In `CardinalRiteStructureFailureSourceTest.java:22` change the expected string to `"\t\t\t\t\tif (StationUpgradeRites.isRite(rite.getRecipeId())) continue;\n"`.
  - In `CardinalRiteProgressionResourceTest.everyEffectOnlyHarbingerRiteHasAnExplicitCompletionPath`, replace the four `completionSource += ...` reads of deleted files with:
```java
		completionSource += Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/StationUpgradeCatalog.java"));
```

- [ ] **Step 8: Delete the superseded GameTests** (now covered by `StationUpgradeGameTests`):
  - `AdvancedBrewingGameTests`: `canceledRiteRecoversExactKitOnce`, `upgradeKeepsMachineIdentityAndContents`, `athanorUpgradeRequiresThreeOrderedProjections`, and their private rite helper (the method containing `Hemomancy.rloc(athanor ? ...)` near line 120).
  - `ResonantForgeGameTests`: `precisionRiteUsesTwoFullStagesAndPreservesMachineState`, `interruptedRiteUnlocksForgeAndReturnsEscrowOnce`, `masterRiteRequiresThreeOrderedStages`, and the `upgradeRite` helper (478-494).
  - `EnzymaticScriptoriumGameTests`: `rotatedEightfoldOrbsPersistAcrossReload` and its rite helper (around line 310-330).
  - `ClinicalBloodProgressionGameTests`: replace both `monolithic_script` references with `palimpsest`.
  - Run `grep -rn "monolithic_script\|AlembicUpgradeRites\|ScriptoriumRites" src/gameTest` and expect no hits. Remove imports left unused.

- [ ] **Step 9: Run the tests to verify they pass.**
  - `./gradlew.bat test`: expect PASS.
  - `./gradlew.bat runGameTestServer`: expect all 8 `*Upgrade` tests, `everyStationRecoversEscrowOnceAndUnlocks`, `brokenSubjectReturnsEscrowOnce` and `legacySavedRitesResumeOrUnlock` to PASS, with no other failures.
  - `./gradlew.bat advancedBrewingGameTestServer scriptoriumGameTestServer`: expect PASS.

- [ ] **Step 10: Checkpoint.** `./gradlew.bat build`. Do not commit.

---

### Task 6: The 8 rite recipes (6 offerings, greater D7s, no sigils) and the policy exemption

**Files:**
- Modify: `src/main/resources/data/hemomancy/recipe/cardinal_rite/{first_condensation,sanguine_athanor,true_groove,enduring_pattern,armature_consecration,monolithic_armature,eightfold_script}.json`
- Create: `src/main/resources/data/hemomancy/recipe/cardinal_rite/palimpsest.json`
- Delete: `src/main/resources/data/hemomancy/recipe/cardinal_rite/monolithic_script.json`
- Modify: `common/rite/CardinalRiteProgressionPolicy.java:24-34, 49`
- Modify: `common/recipe/serializer/CardinalRiteRecipeSerializer.java:264-273`
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json` (rite-name keys referencing the Monolithic Script; find them with `grep -n "Monolithic Script\|monolithic_script" en_us.json`)
- Rename: `src/main/resources/data/hemomancy/books/libersanguinium/tendency/pages/monolithic_script.json` to `palimpsest.json` (content rewritten in Task 9)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/upgrade/StationUpgradeRiteResourceTest.java`

**Interfaces:**
- Consumes: `StationUpgradeCatalog` (Task 1). Items from Task 2 must exist, or the recipe fails to load in game.
- Produces: rite ids exactly matching `StationUpgradeTier.rite()`.

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.station;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StationUpgradeRiteResourceTest {
    private static final Path RITES = Path.of("src/main/resources/data/hemomancy/recipe");

    @Test
    void everyStationRiteDeclaresItsIdAndSixOfferings() throws IOException {
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            JsonObject json = read(tier);
            String name = tier.ritePath();
            assertEquals(tier.rite().toString(), json.get("id").getAsString(), name);
            assertEquals(tier.requiredDegree(), json.get("required_degree").getAsInt(), name);
            assertEquals(tier.riteType(), json.get("riteType").getAsString(), name);
            assertEquals(tier.floor(), json.get("floor").getAsString(), name);
            assertFalse(json.get("rankup").getAsBoolean(), name);
            assertFalse(json.has("result"), name);
            JsonArray braziers = json.getAsJsonArray("brazier_signature");
            int total = braziers.asList().stream()
                    .mapToInt(e -> e.getAsJsonObject().has("count") ? e.getAsJsonObject().get("count").getAsInt() : 1).sum();
            assertEquals(StationUpgradeCatalog.OFFERINGS_PER_RITE, total, name);
            assertEquals(tier.upgradeItem().toString(), braziers.get(0).getAsJsonObject()
                    .getAsJsonObject("ingredient").get("item").getAsString(), name);
            assertEquals(tier.station().blockId().toString(), json.getAsJsonObject("required_structure")
                    .getAsJsonObject("key").getAsJsonObject("S").get("block").getAsString(), name);
        }
    }

    @Test
    void ceremoniesAreStandardAndSigilFree() throws IOException {
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            JsonObject ceremony = read(tier).getAsJsonObject("ceremony");
            String name = tier.ritePath();
            assertEquals(tier.anchors(), ceremony.getAsJsonArray("anchors").size(), name);
            assertEquals(0, ceremony.getAsJsonArray("support_sockets").size(), name);
            assertEquals(0, ceremony.getAsJsonArray("waves").size(), name);
            assertEquals("safe_retry", ceremony.get("failure").getAsString(), name);
            assertEquals("faint", ceremony.getAsJsonObject("atmosphere").get("fog").getAsString(), name);
            assertEquals("living_staff", ceremony.get("focus").getAsString(), name);
        }
        assertFalse(Files.exists(RITES.resolve("cardinal_rite/monolithic_script.json")));
    }

    @Test
    void upgradeItemsAreNeverSharedBetweenStations() throws IOException {
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            String raw = Files.readString(RITES.resolve(tier.ritePath() + ".json"));
            for (StationUpgradeTier other : StationUpgradeCatalog.all())
                if (other.station() != tier.station())
                    assertFalse(raw.contains("\"" + other.upgradeItem() + "\""),
                            tier.ritePath() + " uses " + other.upgradeItem());
        }
    }

    private static JsonObject read(StationUpgradeTier tier) throws IOException {
        return JsonParser.parseString(Files.readString(RITES.resolve(tier.ritePath() + ".json"))).getAsJsonObject();
    }
}
```

- [ ] **Step 2: Run to verify it fails.** Run `./gradlew.bat test --tests "*StationUpgradeRiteResourceTest"`. Expected: FAIL on the missing `"id"` member (NullPointerException / assertion for `first_condensation`).

- [ ] **Step 3: Write the rites.** Every file uses one of the two ceremony blocks below, with only the header and offerings changing.

Tier-1 ceremony block (4 anchors, 2400 ticks):
```json
  "ceremony": {
    "profile": "ceremonial", "layout": "cardinal",
    "anchors": [{"y": 1, "ring": 0, "order": 0}, {"y": 1, "ring": 0, "order": 1},
      {"y": 1, "ring": 0, "order": 2}, {"y": 1, "ring": 0, "order": 3}],
    "support_sockets": [], "waves": [], "guaranteed_waves": [], "fragile_offsets": [],
    "target_duration_ticks": 2400, "focus": "living_staff", "required_helpers": 0,
    "helper_roles": ["anchor", "attendant", "warden"], "still_interval_ticks": 0,
    "atmosphere": {"fog": "faint", "lightning": false, "dome": false}, "failure": "safe_retry"
  }
```
Tier-2 ceremony block (8 anchors, 3600 ticks):
```json
  "ceremony": {
    "profile": "ceremonial", "layout": "cardinal",
    "anchors": [{"y": 1, "ring": 0, "order": 0}, {"y": 1, "ring": 0, "order": 1},
      {"y": 1, "ring": 0, "order": 2}, {"y": 1, "ring": 0, "order": 3},
      {"y": 1, "ring": 1, "order": 0}, {"y": 1, "ring": 1, "order": 1},
      {"y": 1, "ring": 1, "order": 2}, {"y": 1, "ring": 1, "order": 3}],
    "support_sockets": [], "waves": [], "guaranteed_waves": [], "fragile_offsets": [],
    "target_duration_ticks": 3600, "focus": "living_staff", "required_helpers": 0,
    "helper_roles": ["anchor", "attendant", "warden"], "still_interval_ticks": 0,
    "atmosphere": {"fog": "faint", "lightning": false, "dome": false}, "failure": "safe_retry"
  }
```

`first_condensation.json` (tier-1 ceremony):
```json
{
  "type": "hemomancy:cardinal_rite_recipe",
  "id": "hemomancy:cardinal_rite/first_condensation",
  "bloodCost": 0,
  "riteType": "lesser",
  "required_degree": 3,
  "rankup": false,
  "riteName": "Rite of the First Condensation",
  "riteDescription": "Seat a Ghastly Alembic behind the Focus and close its two blood circuits.",
  "floor": "hemomancy:working_lesser",
  "required_structure": {"pattern": [[" S "], ["   "], ["   "]], "key": {"S": {"block": "hemomancy:ghastly_alembic"}}},
  "brazier_signature": [
    {"ingredient": {"item": "hemomancy:hematic_condenser_kit"}, "count": 1},
    {"ingredient": {"item": "minecraft:glass_bottle"}, "count": 1},
    {"ingredient": {"item": "minecraft:blaze_powder"}, "count": 1},
    {"ingredient": {"item": "hemomancy:sanguine_glass"}, "count": 1},
    {"ingredient": {"item": "hemomancy:ferric_binder"}, "count": 1},
    {"ingredient": {"item": "hemomancy:vivacious_enzyme"}, "count": 1}
  ],
  <TIER-1 CEREMONY BLOCK>
}
```
Replace `<TIER-1 CEREMONY BLOCK>` with the literal tier-1 block above. The same goes for `<TIER-2 CEREMONY BLOCK>` below.

`sanguine_athanor.json`: header `"id": "hemomancy:cardinal_rite/sanguine_athanor"`, `"riteType": "greater"`, `"required_degree": 5`, `"riteName": "Rite of the Sanguine Athanor"`, `"riteDescription": "Circulate blood through three alchemical bulbs above a Hematic Condenser."`, `"floor": "hemomancy:working_greater"`, structure block `hemomancy:ghastly_alembic`, tier-2 ceremony, offerings:
`hemomancy:sanguine_athanor_kit`, `minecraft:brewing_stand`, `minecraft:ghast_tear`, `hemomancy:sanguine_quintessence`, `hemomancy:chromatic_sublimate`, `hemomancy:hemolytic_solution`.

`true_groove.json`: id `.../true_groove`, greater, degree 5, name `"Rite of the True Groove"`, description unchanged, floor `working_greater`, block `hemomancy:resonant_forge`, tier-1 ceremony, offerings:
`hemomancy:precision_governor_kit`, `minecraft:grindstone`, `minecraft:honeycomb`, `minecraft:amethyst_shard`, `hemomancy:wax_cylinder`, `hemomancy:hematic_iron_block`.

`enduring_pattern.json`: id `.../enduring_pattern`, greater, degree 7, name `"Rite of the Enduring Pattern"`, description unchanged, `working_greater`, block `hemomancy:resonant_forge`, tier-2 ceremony, offerings:
`hemomancy:master_cam_kit`, `minecraft:anvil`, `minecraft:netherite_scrap`, `hemomancy:mnemonic_ambergris_block`, `hemomancy:chalybeate_sclerite`, `hemomancy:suspended_blood_crystal`.

`armature_consecration.json`: id `.../armature_consecration`, greater, degree 5, name `"Rite of Armature Consecration"`, `"riteDescription": "Consecrate the Hematic Armature with the Armature Consecration Kit and close its two blood circuits."`, `working_greater`, block `hemomancy:hematic_armature`, tier-1 ceremony, offerings:
`hemomancy:vicars_consecration_kit`, `minecraft:armor_stand`, `minecraft:chain`, `hemomancy:tendon_line`, `hemomancy:hemolytic_plating`, `hemomancy:sanguine_formation`.

`monolithic_armature.json`: id `.../monolithic_armature`, greater, degree 7, name `"Rite of the Monolithic Armature"`, description unchanged, `working_greater`, block `hemomancy:hematic_armature`, tier-2 ceremony, offerings:
`hemomancy:monolithic_cornerstone`, `minecraft:netherite_scrap`, `hemomancy:monolith_fragment`, `hemomancy:monolith_imbued_cloth`, `hemomancy:calcified_blood_spine`, `hemomancy:sanguine_quintessence`.

`eightfold_script.json`: id `.../eightfold_script`, greater, degree 5, `"rankup": false`, name `"Rite of the Eightfold Script"`, `"riteDescription": "Rule the Scriptorium's lines in red so one inscription may be singled out from the rest."`, `working_greater`. Keep the existing 5×5 `required_structure` (the `"  S  "` pattern with `hemomancy:enzymatic_scriptorium`). Tier-1 ceremony, offerings:
`hemomancy:rubricators_quill`, `minecraft:ink_sac`, `minecraft:feather`, `minecraft:lapis_block`, `hemomancy:cuttlefish_chromatophores`, `hemomancy:runic_motif_paper`.

`palimpsest.json`: id `hemomancy:cardinal_rite/palimpsest`, greater, degree 7, `"rankup": false`, name `"Rite of the Palimpsest"`, `"riteDescription": "Scrape the Scriptorium's vellum back to memory and cut the chosen inscription deeper than ink can hold."`, `working_greater`, the same 5×5 structure, tier-2 ceremony, offerings:
`hemomancy:palimpsest_burin`, `minecraft:echo_shard`, `minecraft:glow_ink_sac`, `hemomancy:mnemonic_folio`, `hemomancy:axonal_slate`, `hemomancy:myelin_sheath`.

Delete `monolithic_script.json`.

- [ ] **Step 4: Policy exemption.** In `CardinalRiteProgressionPolicy.violations`:
  - delete the `authoredScriptoriumBoundary` variable and its use (lines 28-34 become `if (ceremony.anchors().size() > limits.maxAnchors()) {`);
  - replace the offering check (line 49) with:
```java
		int maxOfferings = com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog.isUpgradeRitePath(ritePath)
				? com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog.OFFERINGS_PER_RITE
				: limits.maxOfferings();
		if (offeringCount > maxOfferings) {
			problems.add("offerings " + offeringCount + " exceed " + maxOfferings);
		}
```

In `CardinalRiteRecipeSerializer`, delete the `/unknown` name-matching block (lines 265-273). Every station rite now declares its `"id"`, so `policyPath` is always real for them.

- [ ] **Step 5: Run the tests to verify they pass.** Run `./gradlew.bat test --tests "*StationUpgradeRiteResourceTest" --tests "*CardinalRiteProgressionResourceTest"`. Expected: PASS, including the existing `everyShippedHarbingerCeremonyObeysItsDegreeCeiling` and `armatureTiersUseSequentialCardinalRitesAndTheirNamedOfferings`.

- [ ] **Step 6: Checkpoint.** Run `./gradlew.bat runServer` long enough to finish loading, and confirm the log has no `Harbinger cardinal rite ... exceeds` or recipe parse errors for the 8 rites. Do not commit.

---

### Task 7: Shared claim progress, migration and machine-use recording

**Files:**
- Create: `common/capability/player/harbinger/station/StationUpgradeProgress.java`, `common/capability/player/harbinger/station/StationUpgradeMigration.java`, `common/event/StationUpgradeEvents.java`
- Modify: `common/capability/HemoAttachmentTypes.java:57-63` (add `STATION_UPGRADES`; keep the two legacy types registered for reading)
- Modify: `common/capability/HemoCapabilityAccess.java:44-48` (add `stationUpgrades(Player)`; delete `advancedBrewing` and `resonantForge` accessors once their callers are gone)
- Modify: `common/brewing/AdvancedBrewingProgress.java`, `common/enchanting/ResonantForgeProgress.java` (reduce to legacy readers)
- Modify: `common/rite/harbinger/HarbingerCardinalRiteEvents.java:164-165`
- Modify: `common/tile/harbinger/crafting/GhastlyAlembicBlockEntity.java:414-416`
- Modify: `common/tile/harbinger/crafting/ResonantForgeBlockEntity.java` (operator + completion hook) and `common/menu/tile/crafting/ResonantForgeMenu.java:119-124`
- Modify: `common/tile/harbinger/crafting/HematicArmatureBlockEntity.java:166`
- Modify: `common/menu/tile/crafting/EnzymaticScriptoriumMenu.java:356`
- Modify callers of `distilled()`: `HarbingerAssignmentLedgerItem.java:200`, `HarbingerChapterProgression.java:27`, `ConcentratedBlood.java:49,62`, `EarlyInitiationDialogue.java:18`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/station/StationUpgradeProgressTest.java`, `StationUpgradeGameTests.java`

**Interfaces:**
- Consumes: `StationUpgradeCatalog`, `StationUpgradeRules` (Task 1).
- Produces:
  - `StationUpgradeProgress`: `recordUse(UpgradeStation, String)`, `hasUsed(UpgradeStation, String)`, `hasClaimed(UpgradeStation, int)`, `claimState(ServerPlayer, StationUpgradeTier)` → `ClaimState`, `claim(ServerPlayer, StationUpgradeTier)` → boolean, `deliver(ServerPlayer)`, `legacyMigrated()`, and package-private `markClaimed(UpgradeStation, int tier, boolean deliverItem)` and `setLegacyMigrated()`.
  - `HemoCapabilityAccess.stationUpgrades(Player)` → `StationUpgradeProgress`.
  - `StationUpgradeMigration.migrate(ServerPlayer)`.

- [ ] **Step 1: Write the failing JVM test**

```java
package com.vincenthuto.hemomancy.common.station;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StationUpgradeProgressTest {
    @Test
    void usageClaimsAndPendingSurviveSerialization() {
        StationUpgradeProgress progress = new StationUpgradeProgress();
        progress.recordUse(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT);
        progress.markClaimed(UpgradeStation.ARMATURE, 1, true);
        progress.markClaimed(UpgradeStation.ARMATURE, 2, false);
        progress.setLegacyMigrated();
        CompoundTag tag = progress.serializeNBT(null);

        StationUpgradeProgress restored = new StationUpgradeProgress();
        restored.deserializeNBT(null, tag);
        assertTrue(restored.hasUsed(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT));
        assertFalse(restored.hasUsed(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT_TARGETED));
        assertTrue(restored.hasClaimed(UpgradeStation.ARMATURE, 1));
        assertTrue(restored.hasClaimed(UpgradeStation.ARMATURE, 2));
        assertFalse(restored.hasClaimed(UpgradeStation.ALEMBIC, 1));
        assertTrue(restored.hasPending(UpgradeStation.ARMATURE, 1));
        assertFalse(restored.hasPending(UpgradeStation.ARMATURE, 2));
        assertTrue(restored.legacyMigrated());
    }
}
```

- [ ] **Step 2: Run to verify it fails.** Run `./gradlew.bat test --tests "*StationUpgradeProgressTest"`. Expected: FAIL, `StationUpgradeProgress` not found.

- [ ] **Step 3: Implement `StationUpgradeProgress`**

```java
package com.vincenthuto.hemomancy.common.station;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Per-player machine use, upgrade-item claims and undelivered claims for every upgradeable station. */
public final class StationUpgradeProgress implements INBTSerializable<CompoundTag> {
    private final EnumMap<UpgradeStation, Set<String>> used = new EnumMap<>(UpgradeStation.class);
    private final EnumMap<UpgradeStation, Integer> claimed = new EnumMap<>(UpgradeStation.class);
    private final EnumMap<UpgradeStation, Integer> pending = new EnumMap<>(UpgradeStation.class);
    private boolean legacyMigrated;

    private static int bit(int tier) { return 1 << (tier - 1); }

    public void recordUse(UpgradeStation station, String usage) {
        used.computeIfAbsent(station, ignored -> new HashSet<>()).add(usage);
    }

    public boolean hasUsed(UpgradeStation station, String usage) {
        return used.getOrDefault(station, Set.of()).contains(usage);
    }

    public boolean hasClaimed(UpgradeStation station, int tier) {
        return (claimed.getOrDefault(station, 0) & bit(tier)) != 0;
    }

    public boolean hasPending(UpgradeStation station, int tier) {
        return (pending.getOrDefault(station, 0) & bit(tier)) != 0;
    }

    public boolean legacyMigrated() { return legacyMigrated; }
    void setLegacyMigrated() { legacyMigrated = true; }

    void markClaimed(UpgradeStation station, int tier, boolean deliverItem) {
        claimed.merge(station, bit(tier), (a, b) -> a | b);
        if (deliverItem) pending.merge(station, bit(tier), (a, b) -> a | b);
    }

    public StationUpgradeRules.ClaimState claimState(ServerPlayer player, StationUpgradeTier tier) {
        boolean usageComplete = tier.requiredUsage().stream().allMatch(usage -> hasUsed(tier.station(), usage));
        boolean activeBlood = HemoCapabilityAccess.getBloodVolume(player).map(IBloodVolume::isActive).orElse(false);
        return StationUpgradeRules.claimState(HemoCapabilityAccess.getPlayerDegreeNumber(player), activeBlood,
                hasClaimed(tier.station(), tier.tier()),
                tier.tier() == 1 || hasClaimed(tier.station(), tier.tier() - 1),
                usageComplete, tier.requiredDegree());
    }

    public boolean claim(ServerPlayer player, StationUpgradeTier tier) {
        if (claimState(player, tier) != StationUpgradeRules.ClaimState.READY) return false;
        markClaimed(tier.station(), tier.tier(), true);
        awardRecipe(player, tier);
        deliver(player);
        return true;
    }

    public void deliver(ServerPlayer player) {
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            if (!hasPending(tier.station(), tier.tier())) continue;
            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(tier.upgradeItem()));
            player.getInventory().add(stack);
            if (stack.isEmpty()) pending.merge(tier.station(), bit(tier.tier()), (a, b) -> a & ~b);
        }
    }

    static void awardRecipe(ServerPlayer player, StationUpgradeTier tier) {
        player.serverLevel().getRecipeManager().byKey(tier.craftingRecipe())
                .ifPresent(recipe -> player.awardRecipes(List.of(recipe)));
    }

    @Override public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for (UpgradeStation station : UpgradeStation.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Claimed", claimed.getOrDefault(station, 0));
            entry.putInt("Pending", pending.getOrDefault(station, 0));
            ListTag usage = new ListTag();
            used.getOrDefault(station, Set.of()).forEach(key -> usage.add(StringTag.valueOf(key)));
            entry.put("Used", usage);
            tag.put(station.serializedName(), entry);
        }
        tag.putBoolean("LegacyMigrated", legacyMigrated);
        return tag;
    }

    @Override public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        used.clear(); claimed.clear(); pending.clear();
        for (UpgradeStation station : UpgradeStation.values()) {
            if (!tag.contains(station.serializedName(), Tag.TAG_COMPOUND)) continue;
            CompoundTag entry = tag.getCompound(station.serializedName());
            claimed.put(station, entry.getInt("Claimed"));
            pending.put(station, entry.getInt("Pending"));
            ListTag usage = entry.getList("Used", Tag.TAG_STRING);
            for (int i = 0; i < usage.size(); i++) recordUse(station, usage.getString(i));
        }
        legacyMigrated = tag.getBoolean("LegacyMigrated");
    }
}
```
If `HemoCapabilityAccess.getBloodVolume` returns something other than `Optional<IBloodVolume>`, adapt this one line to its return type (`grep -n "getBloodVolume" HemoCapabilityAccess.java`).

- [ ] **Step 4: Register the attachment.** In `HemoAttachmentTypes`, after `RESONANT_FORGE`, add:
```java
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<com.vincenthuto.hemomancy.common.capability.player.harbinger.station.StationUpgradeProgress>> STATION_UPGRADES =
            ATTACHMENT_TYPES.register("station_upgrades", () -> AttachmentType.serializable(
                    com.vincenthuto.hemomancy.common.capability.player.harbinger.station.StationUpgradeProgress::new).copyOnDeath().build());
```
Add a comment above `ADVANCED_BREWING` and `RESONANT_FORGE`: `// Legacy: read once by StationUpgradeMigration, then removed from the player.` In `HemoCapabilityAccess` add:
```java
public static com.vincenthuto.hemomancy.common.capability.player.harbinger.station.StationUpgradeProgress stationUpgrades(Player player) {
    return player.getData(HemoAttachmentTypes.STATION_UPGRADES);
}
```

- [ ] **Step 5: Reduce the legacy classes to readers.**
  - In `AdvancedBrewingProgress`, delete `record`, `canClaim*`, `claim*`, `deliver` and `isEnzyme`. Keep the fields, the NBT (de)serialization and the getters. Add `condenserPending()` / `athanorPending()` getters.
  - In `ResonantForgeProgress`, delete `teach`, `canClaim*`, `claim*` and `deliver`. Keep the fields and NBT. Add `precisionPending()` / `masterPending()` getters.
  - Add a class comment to both: `/** Legacy save format; migrated into StationUpgradeProgress on login. */`.

- [ ] **Step 6: Migration and events.**

```java
package com.vincenthuto.hemomancy.common.station;

import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.server.level.ServerPlayer;

public final class StationUpgradeMigration {
    static final String LEGACY_CONSECRATION_KIT_KEY = "hemomancy.vicar_consecration_kit_claimed";
    static final String LEGACY_CORNERSTONE_KEY = "hemomancy.monolithic_cornerstone_claimed";

    private StationUpgradeMigration() {}

    public static void migrate(ServerPlayer player) {
        StationUpgradeProgress progress = HemoCapabilityAccess.stationUpgrades(player);
        if (progress.legacyMigrated()) return;
        if (player.hasData(HemoAttachmentTypes.ADVANCED_BREWING)) {
            var brewing = player.getData(HemoAttachmentTypes.ADVANCED_BREWING);
            if (brewing.distilled()) progress.recordUse(UpgradeStation.ALEMBIC, StationUpgradeCatalog.DISTILL);
            if (brewing.refined()) progress.recordUse(UpgradeStation.ALEMBIC, StationUpgradeCatalog.REFINE);
            if (brewing.compounded()) progress.recordUse(UpgradeStation.ALEMBIC, StationUpgradeCatalog.COMPOUND);
            if (brewing.condenserClaimed()) progress.markClaimed(UpgradeStation.ALEMBIC, 1, brewing.condenserPending());
            if (brewing.athanorClaimed()) progress.markClaimed(UpgradeStation.ALEMBIC, 2, brewing.athanorPending());
            player.removeData(HemoAttachmentTypes.ADVANCED_BREWING);
        }
        if (player.hasData(HemoAttachmentTypes.RESONANT_FORGE)) {
            var forge = player.getData(HemoAttachmentTypes.RESONANT_FORGE);
            if (forge.precisionClaimed()) progress.markClaimed(UpgradeStation.RESONANT_FORGE, 1, forge.precisionPending());
            if (forge.masterClaimed()) progress.markClaimed(UpgradeStation.RESONANT_FORGE, 2, forge.masterPending());
            player.removeData(HemoAttachmentTypes.RESONANT_FORGE);
        }
        var persistent = player.getPersistentData();
        if (persistent.getBoolean(LEGACY_CONSECRATION_KIT_KEY)) progress.markClaimed(UpgradeStation.ARMATURE, 1, false);
        if (persistent.getBoolean(LEGACY_CORNERSTONE_KEY)) progress.markClaimed(UpgradeStation.ARMATURE, 2, false);
        persistent.remove(LEGACY_CONSECRATION_KIT_KEY);
        persistent.remove(LEGACY_CORNERSTONE_KEY);
        for (StationUpgradeTier tier : StationUpgradeCatalog.all())
            if (progress.hasClaimed(tier.station(), tier.tier())) StationUpgradeProgress.awardRecipe(player, tier);
        progress.setLegacyMigrated();
    }
}
```

```java
package com.vincenthuto.hemomancy.common.station;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class StationUpgradeEvents {
    private StationUpgradeEvents() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        StationUpgradeMigration.migrate(player);
        HemoCapabilityAccess.stationUpgrades(player).deliver(player);
    }
}
```
If the repo registers game-bus subscribers some other way (check `grep -rn "@EventBusSubscriber" src/main/java | head`), follow that convention.

In `HarbingerCardinalRiteEvents` (164-165), replace the two `deliver` calls with `HemoCapabilityAccess.stationUpgrades(player).deliver(player);`.

- [ ] **Step 7: Record machine use.**
  - `GhastlyAlembicBlockEntity.onPlayerExtract` (414-416):
```java
		if (player instanceof ServerPlayer serverPlayer)
			HemoCapabilityAccess.stationUpgrades(serverPlayer).recordUse(UpgradeStation.ALEMBIC, completedOperation);
```
  - `ResonantForgeBlockEntity`: add `private UUID operator;` and `public void setOperator(UUID operator) { this.operator = operator; }`. Save it as `"Operator"` when non-null and load it the same way. Change `completeOperation()`:
```java
    private void completeOperation() {
        Operation finished = operation;
        boolean selective = !selection.isBlank();
        boolean success = switch (operation) {
            case APPLY -> finishApply();
            case GRIND -> finishGrinding();
            case STABILIZE -> finishStabilizing();
            default -> false;
        };
        if (!success) refundAndReset(Status.INPUT_CHANGED);
        else {
            recordOperatorUse(finished, selective);
            operation = Operation.NONE;
            progress = totalTicks = reservedBlood = 0;
            status = Status.IDLE;
            sync();
        }
    }

    private void recordOperatorUse(Operation finished, boolean selective) {
        if (operator == null || !(level instanceof ServerLevel serverLevel)) return;
        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(operator);
        if (player == null) return;
        var progress = HemoCapabilityAccess.stationUpgrades(player);
        if (finished == Operation.APPLY) progress.recordUse(UpgradeStation.RESONANT_FORGE, StationUpgradeCatalog.APPLY);
        if (finished == Operation.GRIND) {
            progress.recordUse(UpgradeStation.RESONANT_FORGE, StationUpgradeCatalog.GRIND);
            if (selective) progress.recordUse(UpgradeStation.RESONANT_FORGE, StationUpgradeCatalog.GRIND_SELECTED);
        }
    }
```
  - `ResonantForgeMenu.clickMenuButton`: before the `switch`, add `if (button <= 2) forge.setOperator(player.getUUID());`.
  - `HematicArmatureBlockEntity` after line 166 (`ArtificerAssignments.onArmatureUpgrade(...)`):
```java
			var stationProgress = HemoCapabilityAccess.stationUpgrades(player);
			stationProgress.recordUse(UpgradeStation.ARMATURE, StationUpgradeCatalog.ARMOR_UPGRADE);
			if (getArmatureTier().id() >= ArmatureUpgradeRules.ArmatureTier.VICAR_CONSECRATED.id())
				stationProgress.recordUse(UpgradeStation.ARMATURE, StationUpgradeCatalog.ARMOR_UPGRADE_CONSECRATED);
```
  - `EnzymaticScriptoriumMenu` after line 356 (`actor.onEnchantmentPerformed(input, tier + 1);`):
```java
        if (actor instanceof ServerPlayer serverActor) {
            var stationProgress = HemoCapabilityAccess.stationUpgrades(serverActor);
            stationProgress.recordUse(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT);
            if (mode > 0) stationProgress.recordUse(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT_TARGETED);
        }
```
  - Replace each `HemoCapabilityAccess.advancedBrewing(player).distilled()` (the 5 callers listed in Files) with `HemoCapabilityAccess.stationUpgrades(player).hasUsed(UpgradeStation.ALEMBIC, StationUpgradeCatalog.DISTILL)`.

- [ ] **Step 8: GameTests** (add to `StationUpgradeGameTests`, with a `player(helper)` helper copied from `ResonantForgeGameTests.player` lines 513-525):

```java
    static ServerPlayer claimant(GameTestHelper helper, int degree) {
        ServerPlayer player = player(helper);
        HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(degree);
        HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> volume.setActive(true));
        return player;
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade_claims")
    public static void claimsRequireUseDegreeAndOrder(GameTestHelper helper) {
        for (UpgradeStation station : UpgradeStation.values()) {
            StationUpgradeTier first = StationUpgradeCatalog.get(station, 1);
            StationUpgradeTier second = StationUpgradeCatalog.get(station, 2);
            ServerPlayer player = claimant(helper, second.requiredDegree());
            var progress = HemoCapabilityAccess.stationUpgrades(player);
            helper.assertTrue(!progress.claim(player, first), station + " claimed without machine use");
            first.requiredUsage().forEach(use -> progress.recordUse(station, use));
            second.requiredUsage().forEach(use -> progress.recordUse(station, use));
            helper.assertTrue(!progress.claim(player, second), station + " claimed tier 2 before tier 1");
            helper.assertTrue(progress.claim(player, first) && !progress.claim(player, first), station + " tier 1 claim");
            helper.assertTrue(player.getRecipeBook().contains(first.craftingRecipe()), station + " recipe not unlocked");
            helper.assertTrue(player.getInventory().contains(new ItemStack(BuiltInRegistries.ITEM.get(first.upgradeItem()))),
                    station + " item not delivered");
            helper.assertTrue(progress.claim(player, second), station + " tier 2 claim");
        }
        ServerPlayer inactive = claimant(helper, 7);
        HemoCapabilityAccess.getBloodVolume(inactive).ifPresent(volume -> volume.setActive(false));
        var tier = StationUpgradeCatalog.get(UpgradeStation.ALEMBIC, 1);
        HemoCapabilityAccess.stationUpgrades(inactive).recordUse(UpgradeStation.ALEMBIC, StationUpgradeCatalog.DISTILL);
        helper.assertTrue(!HemoCapabilityAccess.stationUpgrades(inactive).claim(inactive, tier), "inactive blood claimed");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade_claims")
    public static void fullInventoryClaimIsDeliveredLater(GameTestHelper helper) {
        ServerPlayer player = claimant(helper, 5);
        var tier = StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 1);
        var progress = HemoCapabilityAccess.stationUpgrades(player);
        progress.recordUse(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT);
        for (int i = 0; i < player.getInventory().items.size(); i++)
            player.getInventory().items.set(i, new ItemStack(Items.STONE, 64));
        helper.assertTrue(progress.claim(player, tier) && progress.hasPending(UpgradeStation.SCRIPTORIUM, 1),
                "full-inventory claim was not held");
        player.getInventory().items.set(0, ItemStack.EMPTY);
        progress.deliver(player);
        helper.assertTrue(!progress.hasPending(UpgradeStation.SCRIPTORIUM, 1)
                && player.getInventory().contains(new ItemStack(ItemInit.rubricators_quill.get())), "pending quill not delivered");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade_claims")
    public static void legacyClaimsMigrateWithoutSecondKit(GameTestHelper helper) {
        ServerPlayer player = claimant(helper, 7);
        player.getPersistentData().putBoolean("hemomancy.vicar_consecration_kit_claimed", true);
        var brewing = player.getData(HemoAttachmentTypes.ADVANCED_BREWING);
        CompoundTag legacy = new CompoundTag();
        legacy.putBoolean("Distilled", true);
        legacy.putBoolean("CondenserClaimed", true);
        brewing.deserializeNBT(helper.getLevel().registryAccess(), legacy);
        StationUpgradeMigration.migrate(player);
        var progress = HemoCapabilityAccess.stationUpgrades(player);
        helper.assertTrue(progress.hasClaimed(UpgradeStation.ARMATURE, 1) && progress.hasClaimed(UpgradeStation.ALEMBIC, 1)
                && progress.hasUsed(UpgradeStation.ALEMBIC, StationUpgradeCatalog.DISTILL), "legacy state lost");
        helper.assertTrue(!player.getInventory().contains(new ItemStack(ItemInit.vicars_consecration_kit.get()))
                && !player.getInventory().contains(new ItemStack(ItemInit.hematic_condenser_kit.get())), "legacy claim re-issued a kit");
        helper.assertTrue(player.getRecipeBook().contains(Hemomancy.rloc("vicars_consecration_kit"))
                && player.getRecipeBook().contains(Hemomancy.rloc("hematic_condenser_kit")), "legacy claimant lacks repeat recipe");
        helper.assertTrue(!player.getPersistentData().contains("hemomancy.vicar_consecration_kit_claimed"), "legacy flag kept");
        helper.succeed();
    }
```
Delete `ResonantForgeGameTests.artificerLessonsGateOneTimeKitsAndRetryFullInventory`, which is replaced by the tests above. Review `ResonantForgeGameTests.forgeFullCycleIsTaughtAtD4WhileD3AccessRemains`: if it calls `progress.teach(...)`, remove those lines and keep its D3/D4 dialogue assertions.

- [ ] **Step 9: Run the tests to verify they pass.** `./gradlew.bat test --tests "*StationUpgradeProgressTest"` and `./gradlew.bat runGameTestServer`. Expected: PASS. Then run `grep -rn "advancedBrewing(\|resonantForge(" src` and expect hits only in `DialogueEventHandler`, `AdvancedBrewingDialogue` and `ResonantForgeDialogue`, all removed in Task 8.

- [ ] **Step 10: Checkpoint.** `./gradlew.bat build`. Do not commit.

---

### Task 8: Claim dialogue for Alchemist, Artificer, Mnemonist and Monolith

**Files:**
- Create: `common/entity/npc/dialogue/StationUpgradeDialogue.java`
- Modify: `AdvancedBrewingDialogue.java`, `ResonantForgeDialogue.java`, `HarbingerMnemonistEntity.java:125`, `DialogueEventHandler.java:79-82, 87 (new branch), 310-319, 326-333, 639-698`, `DialogueOptionPacket.java:73-99`, `DialogueRewardClaims.java`, `ArtificerProgressSnapshot.java:96`, `HarbingerArtificerEntity.java:~75-80`, `HarbingerVicarEntity.java:223, 234`, `SanguineMonolithDialogueTrees.java:30`
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`
- Modify tests: `ArtificerProgressionGameTests.armatureKitBelongsToArtificerAndKeepsLegacyClaim` (lines 114-180), `HarbingerArtificerNpcSourceTest` (if it asserts removed code), `VicarContinuingLessonsTest` (same)
- Test: `StationUpgradeGameTests.java`

**Interfaces:**
- Consumes: `StationUpgradeProgress.claim/claimState/hasClaimed` (Task 7), `StationUpgradeCatalog.get` (Task 1).
- Produces: `StationUpgradeDialogue.CLAIM_PREFIX = "station_upgrade_claim/"`, `claimEvent(StationUpgradeTier)`, `Optional<StationUpgradeTier> parseClaim(String)`, `DialogueTree append(DialogueTree, ServerPlayer, UpgradeStation, String leaveKey)`, `boolean allowedSpeaker(UpgradeStation, Entity)`.

- [ ] **Step 1: Write the failing GameTest**

```java
    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade_claims")
    public static void mnemonistOffersScriptoriumClaimsOnlyWhenReady(GameTestHelper helper) {
        ServerPlayer player = claimant(helper, 7);
        var mnemonist = com.vincenthuto.hemomancy.common.init.EntityInit.harbinger_mnemonist.get().create(helper.getLevel());
        mnemonist.setPos(player.position());
        helper.getLevel().addFreshEntity(mnemonist);
        var quill = StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 1);
        String event = com.vincenthuto.hemomancy.common.entity.npc.dialogue.StationUpgradeDialogue.claimEvent(quill);
        helper.assertTrue(!dialogueHasEvent(mnemonist.progressionDialogue(player), event), "unready claim was offered");
        HemoCapabilityAccess.stationUpgrades(player).recordUse(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT);
        helper.assertTrue(dialogueHasEvent(mnemonist.progressionDialogue(player), event), "ready claim was not offered");
        var dispatched = com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket.dispatch(player, event, mnemonist.getId());
        helper.assertTrue(dispatched != null && dispatched.isRewardDelivered()
                && player.getInventory().contains(new ItemStack(ItemInit.rubricators_quill.get())), "Mnemonist claim failed");
        var artificer = com.vincenthuto.hemomancy.common.init.EntityInit.harbinger_artificer.get().create(helper.getLevel());
        artificer.setPos(player.position());
        helper.getLevel().addFreshEntity(artificer);
        String burin = com.vincenthuto.hemomancy.common.entity.npc.dialogue.StationUpgradeDialogue.claimEvent(
                StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 2));
        helper.assertTrue(com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket.dispatch(player, burin, artificer.getId()) == null,
                "a non-Mnemonist handed out a Scriptorium claim");
        helper.succeed();
    }

    static boolean dialogueHasEvent(com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree tree, String event) {
        return tree.nodes().values().stream().flatMap(node -> node.options().stream())
                .anyMatch(option -> event.equals(option.eventId()));
    }
```
Adjust these to the real names: the `EntityInit` holders (`grep -n "mnemonist\|artificer" EntityInit.java`), the `DialogueOption` event accessor (`grep -n "record DialogueOption" -A3`), and the `DialogueEvent` delivered-reward getter (`grep -n "RewardDelivered" DialogueEvent.java`).

- [ ] **Step 2: Run to verify it fails.** Run `./gradlew.bat compileGameTestJava`. Expected: FAIL, `StationUpgradeDialogue` not found.

- [ ] **Step 3: Implement `StationUpgradeDialogue`**

```java
package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerArtificerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeTier;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/** Claim options for station upgrade items, shared by every teaching NPC. */
public final class StationUpgradeDialogue {
    public static final String CLAIM_PREFIX = "station_upgrade_claim/";

    private StationUpgradeDialogue() {}

    public static String claimEvent(StationUpgradeTier tier) {
        return CLAIM_PREFIX + tier.station().serializedName() + "/" + tier.tier();
    }

    public static Optional<StationUpgradeTier> parseClaim(String eventId) {
        if (eventId == null || !eventId.startsWith(CLAIM_PREFIX)) return Optional.empty();
        String[] parts = eventId.substring(CLAIM_PREFIX.length()).split("/");
        if (parts.length != 2) return Optional.empty();
        UpgradeStation station = UpgradeStation.byName(parts[0]);
        if (station == null || !(parts[1].equals("1") || parts[1].equals("2"))) return Optional.empty();
        return Optional.of(StationUpgradeCatalog.get(station, Integer.parseInt(parts[1])));
    }

    /** Armature claims keep their bespoke Artificer and Monolith scenes, which call the same progress. */
    public static boolean allowedSpeaker(UpgradeStation station, Entity entity) {
        return switch (station) {
            case ALEMBIC -> entity instanceof HarbingerAlchemistEntity;
            case RESONANT_FORGE -> entity instanceof HarbingerArtificerEntity;
            case SCRIPTORIUM -> entity instanceof HarbingerMnemonistEntity;
            case ARMATURE -> false;
        };
    }

    public static DialogueTree append(DialogueTree tree, ServerPlayer player, UpgradeStation station, String leaveKey) {
        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
        var progress = HemoCapabilityAccess.stationUpgrades(player);
        var nodes = new LinkedHashMap<>(tree.nodes());
        var root = tree.getStartNode();
        var options = new ArrayList<>(root.options());
        for (int number = 2; number >= 1; number--) {
            StationUpgradeTier tier = StationUpgradeCatalog.get(station, number);
            if (degree < tier.requiredDegree()) continue;
            var state = progress.claimState(player, tier);
            boolean ready = state == StationUpgradeRules.ClaimState.READY;
            String key = tier.dialogueKey();
            String nodeId = "station_upgrade_" + station.serializedName() + "_" + number;
            options.add(0, new DialogueOption(key + ".title", nodeId, ready ? claimEvent(tier) : null,
                    ready ? DialogueOptionPresentation.attention(DialogueAttention.NOTICE, key + ".prompt")
                            : DialogueOptionPresentation.prompt(key + ".prompt")));
            String line = state == StationUpgradeRules.ClaimState.CLAIMED ? key + ".claimed"
                    : ready ? key + ".ready" : key + ".requirements";
            nodes.put(nodeId, new DialogueNode(nodeId, List.of(line),
                    List.of(new DialogueOption(leaveKey, null, null))));
        }
        nodes.put(root.id(), new DialogueNode(root.id(), root.lines(), options));
        return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
                tree.entityId(), tree.theme(), tree.presentation());
    }
}
```

- [ ] **Step 4: Wire the NPCs.**
  - `AdvancedBrewingDialogue.append`: keep the `thelemic_preparations` option and node. Delete the `CONDENSER_CLAIM` / `ATHANOR_CLAIM` constants and the two claim option/node blocks (36-61). Before the final `return`, rebuild the tree with the preparations node, then pass it through `StationUpgradeDialogue.append(result, player, UpgradeStation.ALEMBIC, "hemomancy.dialogue.alchemist.option.leave")`.
  - `ResonantForgeDialogue.append`: delete `TEACH`, `PRECISION_CLAIM` and `MASTER_CLAIM` and the two `degree >= 5/7` blocks. The `resonant_forge` option becomes a plain lesson with no event: `option(..., "resonant_forge", null, false, "hemomancy.artificer.resonant_forge.prompt")`. Return `StationUpgradeDialogue.append(result, player, UpgradeStation.RESONANT_FORGE, "hemomancy.dialogue.artificer.option.leave")`.
  - `HarbingerMnemonistEntity.progressionDialogue` line 125 becomes:
```java
		tree = MnemonistScriptoriumLesson.append(tree, degree);
		tree = StationUpgradeDialogue.append(tree, serverPlayer, UpgradeStation.SCRIPTORIUM, "hemomancy.dialogue.mnemonist.option.leave");
		return ClinicalBloodDialogue.append(tree, serverPlayer, "mnemonist");
```
    Also add a small static nested helper (or a private static method in the entity) named `MnemonistScriptoriumLesson.append` that, when `degree >= 3`, adds a root option `hemomancy.mnemonist.scriptorium.title` → node `mnemonist_scriptorium` with lines `hemomancy.mnemonist.scriptorium.lesson`, `hemomancy.mnemonist.scriptorium.tiers` and a leave option. Mirror `ResonantForgeDialogue.forgeNode`.
  - `DialogueEventHandler.onDialogueOption`: right after line 86 (`ServerPlayer player = event.getPlayer();`) add:
```java
		var stationClaim = StationUpgradeDialogue.parseClaim(event.getEventId());
		if (stationClaim.isPresent()) {
			event.setRewardDelivered(HemoCapabilityAccess.stationUpgrades(player).claim(player, stationClaim.get()));
			return;
		}
```
    Then delete the five `case AdvancedBrewingDialogue.* / ResonantForgeDialogue.*` arms (310-319).
  - Armature claims in `DialogueEventHandler`:
    - `hasClaimedConsecrationKit(player)` becomes `return HemoCapabilityAccess.stationUpgrades(player).hasClaimed(UpgradeStation.ARMATURE, 1);`
    - `hasClaimedMonolithCornerstone(player)` becomes `... hasClaimed(UpgradeStation.ARMATURE, 2);`
    - Delete the two `*_CLAIM_KEY` constants (79-82).
    - Replace the body of `handleArtificerConsecrationKit` after the Artificer/distance check with:
```java
		var tier = StationUpgradeCatalog.get(UpgradeStation.ARMATURE, 1);
		var progress = HemoCapabilityAccess.stationUpgrades(player);
		var state = progress.claimState(player, tier);
		if (state != StationUpgradeRules.ClaimState.READY) {
			player.displayClientMessage(Component.translatable(state == StationUpgradeRules.ClaimState.CLAIMED
					? "hemomancy.dialogue.event.artificer_consecration_kit_known"
					: "hemomancy.dialogue.event.artificer_consecration_kit_unready").withStyle(ChatFormatting.GRAY), false);
			return false;
		}
		progress.claim(player, tier);
		player.displayClientMessage(Component.translatable("hemomancy.dialogue.event.artificer_consecration_kit_granted")
				.withStyle(ChatFormatting.DARK_RED), false);
		return true;
```
    - Do the same in `handleMonolithCornerstone` with `ARMATURE, 2` and the `monolith_cornerstone_{known,unready,granted}` keys. `claim` delivers through `deliver`, so the `giveOrDropAtEntity` calls go away.
  - `DialogueOptionPacket.dispatch`: inside the `else` block after the distance check (line 73), add:
```java
			var stationClaim = StationUpgradeDialogue.parseClaim(eventId);
			if (stationClaim.isPresent() && !StationUpgradeDialogue.allowedSpeaker(stationClaim.get().station(), entity))
				return null;
```
    Delete the two `AdvancedBrewingDialogue.*_CLAIM` lines (97-98).
  - `DialogueRewardClaims.requiresAcknowledgement`: add `if (StationUpgradeDialogue.parseClaim(eventId).isPresent()) return true;` after the null check, and delete the two `AdvancedBrewingDialogue` cases.
  - `HarbingerArtificerEntity` (~75-80): delete the legacy "award the consecration recipe on contact" block. `StationUpgradeMigration` now does this at login.
  - `ArtificerProgressSnapshot:96` and `HarbingerVicarEntity:223,234` keep calling `DialogueEventHandler.hasClaimedConsecrationKit(...)`, which is now backed by the new progress. No change needed beyond compiling.
  - Remove the now-unused accessors `HemoCapabilityAccess.advancedBrewing/resonantForge`.

- [ ] **Step 5: Lang.** In `en_us.json`:
  - Update the requirement lines for the new rules:
```json
  "hemomancy.alchemist.brewing.condenser.requirements": "Distil something at an ordinary Alembic and take the result from it yourself. Keep your blood awake and reach the third Degree; then I will trust you with a Condenser kit.",
  "hemomancy.alchemist.brewing.athanor.requirements": "Claim the Condenser first. Then finish a refinement and a two-effect compound at it, and take each result yourself. At the fifth Degree the greater circuit is yours.",
  "hemomancy.artificer.resonant_forge.precision.requirements": "Grind one pattern into wax and hammer one pattern back into a tool. At the fifth Degree I will give you a governor steady enough to separate one vibration from the rest.",
  "hemomancy.artificer.resonant_forge.master.requirements": "Fit the Precision Governor, then use it: grind a single chosen vibration out of a tool. At the seventh Degree the Master Cam is yours.",
```
  - Add the Mnemonist keys, keeping the memory vocabulary and no saintly framing:
```json
  "hemomancy.mnemonist.scriptorium.title": "The Scriptorium",
  "hemomancy.mnemonist.scriptorium.prompt": "Ask how the Scriptorium remembers what it writes.",
  "hemomancy.mnemonist.scriptorium.lesson": "The Scriptorium writes the way memory does: eight tendencies, one hand, and whatever the blood agrees to keep.",
  "hemomancy.mnemonist.scriptorium.tiers": "Rule its lines in red and it will let you single out one inscription. Scrape the page back and cut deeper, and that inscription will hold more than ink allows.",
  "hemomancy.mnemonist.scriptorium.rubricator.title": "The Rubricator's Quill",
  "hemomancy.mnemonist.scriptorium.rubricator.prompt": "Ask for the quill that marks one line in red.",
  "hemomancy.mnemonist.scriptorium.rubricator.requirements": "Write something at a Scriptorium first, and keep your blood awake. At the fifth Degree I will lend you the quill the old rubricators used.",
  "hemomancy.mnemonist.scriptorium.rubricator.ready": "You have written, and the page remembers your hand. Take the quill. Offer it at the Rite of the Eightfold Script.",
  "hemomancy.mnemonist.scriptorium.rubricator.claimed": "The quill is yours already. If you need another, you know now how it is cut.",
  "hemomancy.mnemonist.scriptorium.palimpsest.title": "The Palimpsest Burin",
  "hemomancy.mnemonist.scriptorium.palimpsest.prompt": "Ask for the burin that writes over what was written.",
  "hemomancy.mnemonist.scriptorium.palimpsest.requirements": "Claim the quill, then single out an inscription with it. At the seventh Degree the burin will cut where the quill only marked.",
  "hemomancy.mnemonist.scriptorium.palimpsest.ready": "You have chosen a line and made it stay. Take the burin to the Rite of the Palimpsest and cut it deeper.",
  "hemomancy.mnemonist.scriptorium.palimpsest.claimed": "The burin is already yours. Memory does not hand out the same edge twice.",
```
  - Replace `hemomancy.dialogue.event.artificer_consecration_kit_unready` with: `"The Artificer holds the kit back until you have worked the Armature yourself and reached the fifth Degree with your blood awake."`
  - Replace `hemomancy.dialogue.event.monolith_cornerstone_unready` with: `"The Lodestone remains silent. It answers Archons who have already worked a consecrated Armature."`
  - Add the Armature dialogue keys the catalog references (`hemomancy.artificer.armature.consecration.*` and `hemomancy.monolith.cornerstone.*`), with the suffixes `.title`, `.prompt`, `.ready`, `.claimed` and `.requirements`. Copy the wording of the existing Artificer and Monolith lines where they already exist. These keys are used by documentation and tooling only, because the Armature keeps its bespoke scenes.

- [ ] **Step 6: Update the Artificer legacy test.** In `ArtificerProgressionGameTests.armatureKitBelongsToArtificerAndKeepsLegacyClaim`:
  - replace the legacy-flag section (163-174) with a call to `StationUpgradeMigration.migrate(legacy)` before the Artificer interaction, keeping the assertions *recipe learned*, *no second free kit* and *kit not offered*;
  - before the "claimed the kit" assertion (around 150-157), add `HemoCapabilityAccess.stationUpgrades(player).recordUse(UpgradeStation.ARMATURE, StationUpgradeCatalog.ARMOR_UPGRADE);` and set blood active.

  Run `./gradlew.bat test --tests "*HarbingerArtificerNpcSourceTest" --tests "*VicarContinuingLessonsTest" --tests "*HarbingerArtificerAssignmentSourceTest"` and update any source-string assertion that referenced deleted code so it names the new call (`stationUpgrades(player).claim`).

- [ ] **Step 7: Run the tests to verify they pass.** `./gradlew.bat test` and `./gradlew.bat runGameTestServer`. Expected: PASS, including `mnemonistOffersScriptoriumClaimsOnlyWhenReady`.

- [ ] **Step 8: Checkpoint.** In `./gradlew.bat runClient`, at D5 with active blood:
  - enchant once at a Scriptorium, then talk to a Mnemonist: the Rubricator's Quill option shows the attention marker and delivers the quill;
  - the recipe book now lists the quill;
  - the Alchemist and Artificer claims behave the same way for their stations.

  Do not commit.

---

### Task 9: Documentation, books, material atlas and the full gate

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md` (station rows ~2882, Scriptorium paragraph ~641, degree tables ~634, Artificer ~2731/3507, Monolith ~2897, and every other hit of the grep below)
- Modify: `docs/LORE_REFERENCE.md` (add the Rubricator's Quill and Palimpsest Burin in Mnemonist / still-memory vocabulary)
- Modify: `src/main/resources/data/hemomancy/books/libersanguinium/alembic/pages/{first_condensation,sanguine_athanor,refined_and_compound}.json`, `resonant_forge/pages/upgrades.json`, `tendency/pages/eightfold_script.json`, `tendency/pages/palimpsest.json` (renamed in Task 6)
- Modify: `wiki/*.md` pages that describe these stations (`grep -rln "Condenser\|Athanor\|Precision Governor\|Master Cam\|Eightfold\|Monolithic Script\|Consecration Kit\|Cornerstone" wiki`)
- Modify: `docs/harbinger_progression_redesign/{00-player-journey,03-memory-alchemy-stations,HARBINGER_PROGRESSION_REDESIGN}.md` (replace `Monolithic Script` with `Rite of the Palimpsest`)
- Modify: `client/screen/skilltree/shared/MaterialsData.java` (near 1412) and `MaterialAtlasSpec.java` (near 304-307)

- [ ] **Step 1: Find every stale statement**

Run: `grep -rn "Condenser Kit\|condenser_kit\|Athanor Kit\|Precision Governor\|Master Cam\|Eightfold\|Monolithic Script\|monolithic_script\|Consecration Kit\|Cornerstone\|potion and two\|reservoir sigil\|bloodlicker\|breaking an upgraded\|returns the applied kit\|applying an\b" docs wiki src/main/resources/data/hemomancy/books`
Expected: a list of lines to rewrite. Work through it top to bottom.

- [ ] **Step 2: Rewrite `HEMOMANCY_REFERENCE.md`.** Add one section titled **Station Upgrades** containing:
  - the 8-row table from `StationUpgradeCatalog`: station, tier, degree, NPC, upgrade item, rite, machine use required, circuits × mL, and what the tier unlocks. The unlocks are:
    - Condenser: advanced brewing. Athanor: second catalyst.
    - Precision: single-enchantment grinding. Masterwork: master patterns.
    - Vicar tier: D5-D6 armor recipes. Monolithic: D7+ armor recipes.
    - Eightfold: targeted +1 enchant. Palimpsest: targeted +2 enchant.
  - the claim rules: active blood, degree, previous tier, machine use; the first claim gives the item and unlocks its recipe.
  - the rite rules: 6 braziers, tier-1 = 4 anchors / 2 circuits, tier-2 = 8 anchors / 3 circuits, no sigils or waves, `safe_retry`, escrowed offerings returned on any cancel/failure.
  - persistence: breaking keeps the tier (the item carries `stage`); no upgrade items are refunded.

  Then replace the old per-station prose (line ~2882 "applying an Armature Consecration Kit ... Breaking an upgraded Armature returns the applied kit ...", the Scriptorium paragraph at ~641, the Monolith "cornerstone is a separate once-only claim" text at ~2897, and the Artificer lines at ~2731 / ~3507) with one-line references to that section. Keep the Artificer's teaching role; the Monolith still gives the Cornerstone, now gated by consecrated-Armature use.

- [ ] **Step 3: Books.** For each book page listed in Files, rewrite the body text so it matches the new offerings list, the claim requirements and the 6-brazier rule. Keep the JSON structure and page ids unchanged, except for `palimpsest.json`. In `palimpsest.json`, rename every "Monolithic Script" to "Rite of the Palimpsest" and describe the Palimpsest Burin.

- [ ] **Step 4: Material atlas.** In `MaterialsData.java`, directly after the `vicars_consecration_kit` entry (line ~1412), add:
```java
        list.add(new MaterialEntry("rubricators_quill", "Rubricator's Quill",
                "The Mnemonist's D5 offering for the Rite of the Eightfold Script.",
                "Qliphoth Reagents", () -> new ItemStack(ItemInit.rubricators_quill.get()),
                true, UnlockPredicate.minDegree(5)));
        list.add(new MaterialEntry("palimpsest_burin", "Palimpsest Burin",
                "The Mnemonist's D7 offering for the Rite of the Palimpsest.",
                "Qliphoth Reagents", () -> new ItemStack(ItemInit.palimpsest_burin.get()),
                true, UnlockPredicate.minDegree(7)));
```
In `MaterialAtlasSpec.java`, add entries next to line 307, copying the `entryAt(...)` signature used there. Pick grid coordinates that do not collide with existing ones (search the file for the chosen `x, y` pair first), and set the parent to `"enzymatic_scriptorium"` if that id exists in the atlas, otherwise to `"vicars_consecration_kit"`.

- [ ] **Step 5: Lore.** In `docs/LORE_REFERENCE.md` under the Mnemonist / Scriptorium material, add two short entries:
  - *Rubricator's Quill*: red-ruling a remembered line so it can be found again.
  - *Palimpsest Burin*: scraping a page back to what the blood remembers and cutting deeper.

  Use still/memory language and no divine framing.

- [ ] **Step 6: Full verification gate**

Run: `./gradlew.bat alphaCheck`
Expected: BUILD SUCCESSFUL (JVM tests + dedicated GameTests).
Run: `./gradlew.bat advancedBrewingGameTestServer scriptoriumGameTestServer`
Expected: all pass.
Run: `grep -rn "AlembicUpgradeRites\|ResonantForgeUpgradeRites\|ArmatureUpgradeRites\|ScriptoriumRites\|monolithic_script\|advancedBrewing(\|resonantForge(" src docs wiki`
Expected: only the intentional legacy strings in `CardinalRitePhase.byName`, `ActiveCardinalRite` (the id alias), `StationUpgradeMigration` and `ScriptoriumLegacyMigration`.

- [ ] **Step 7: Manual acceptance in `runClient`.** For each station:
  - claim the tier-1 item after using the machine;
  - perform the rite with 6 lit braziers;
  - cancel one attempt midway and confirm all 6 offerings and the staff come back;
  - complete the rite;
  - break the station, pick it up, re-place it, and confirm it is still upgraded (Alembic athanor growth, Forge master button, Armature ornaments, Scriptorium eightfold/monolithic model).

  Report results to the user. Do not commit.

---

## Self-Review Notes

- Spec coverage:
  - decision 1 → Tasks 1, 7, 8;
  - decision 2 → Tasks 4, 5;
  - decisions 3 and 8 → Tasks 2, 6, 7;
  - decision 4 → Task 3;
  - decision 5 → Task 5 (escrow for all, including the Scriptorium);
  - decision 6 → Task 6 (policy exemption plus `"id"` declarations);
  - decision 7 → Task 1 catalog;
  - decision 9 → Task 8;
  - decision 10 → Tasks 5 (id alias) and 6;
  - decision 11 → Task 9.
- Offerings are single items per brazier. `"count"` counts braziers, which is why no `×N` stacks appear.
- Two items need the user's eye after execution: final textures for `rubricators_quill` / `palimpsest_burin` (placeholders are copies), and whether every chosen offering is obtainable by its degree (see the Review Focus note in the handoff).
