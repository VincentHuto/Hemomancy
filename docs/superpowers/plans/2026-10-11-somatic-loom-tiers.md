# Somatic Loom Tiers Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (inline) for this plan. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Make the Somatic Loom the sixth upgradeable station: base (D3), **Ganglionic** (D5) and **Cortical** (D6).
- Each stage gates which memory ranks it can weave, matching the learning gates.
- Each stage grows the echo.
- Ganglionic feelers auto-catch slack threads, and staff reach is longer.
- Everything uses the shared station-upgrade catalog, progress and rites.

**Architecture:** This plan adds the loom to the existing shared machinery and avoids parallel systems:
- `UpgradeStation`, `StationUpgradeCatalog`, `StationUpgradeProgress`, `StationUpgradeRites`, `StationTierProperty`, `UpgradeableStation`, `CreativeStationUpgrades`, `StationUpgradeDialogue` and `StationUpgradeTooltips`.
- Rank gating and auto-catch are pure rules in `SomaticWeaveRules`.
- The stage bodies come from `tools/model_export/somatic_loom.py`.

**Tech Stack:** Java 21, NeoForge 1.21.1, JUnit 5, GameTests, Python model generator.

**Spec:** `docs/superpowers/specs/2026-10-11-somatic-loom-weave-design.md` (Tiers section). **Contract:** `docs/STATION_UPGRADES.md`, which must be read and updated.

**Depends on:** Plan 1 (`2026-10-11-somatic-loom-witnessed-weave-core.md`), which must be complete.

## Global Constraints

- Follow `docs/STATION_UPGRADES.md` exactly:
  - exactly one stage at a time, upgraded in place;
  - preserve contents, stored enzymes, reservoir, facing and any paused weave;
  - personal eligibility is mandatory;
  - free kits are optional and never reset;
  - crafted and gifted kits are equally valid;
  - the server checks eligibility at rite activation.
- Degrees: Ganglionic **D5**, Cortical **D6**. Rank gates: stage 0 weaves ≤ SUMMA, stage 1 ≤ MAGISTER, stage 2 ≤ PERFECTUS, matching `ManipulationRankGates` (SUMMA D3, MAGISTER D5, PERFECTUS D6).
- Teacher: the Mnemonist, for both kits.
- Personal practice:
  - Ganglionic: complete any weave (`loom_weave`);
  - Cortical: complete a MAGISTER-or-higher weave on a stage ≥ 1 loom (`loom_weave_ganglionic`).
- Circuits: 2 × 50 mL and 3 × 50 mL.
- Offerings (kit first):
  - Ganglionic: `ganglionic_harness`, `neurotic_enzyme`, `nerve_fiber`, `puppeteering_thread`, `sanguine_formation`, `blood_crystal_shard`.
  - Cortical: `cortical_crown`, `nerve_bundle`, `synaptic_node`, `mnemonic_ambergris`, `blood_stained_stone`, `sanguine_glass`.
  - Task 1 checks `synaptic_node`'s D6 obtainability and swaps it for `naeglerophaeon_ganglion` only if it is not obtainable. Record the outcome as a ledgered ruling.
- No copper, brass or silver on blood-side models or kit sprites. Custom particles only.
- The loom's main-block files keep their committed line endings; see the Plan 1 constraint.
- Do not commit.

## Review Focus

1. A paid or paused weave must survive an upgrade rite untouched. The rite must refuse to start while a weave is paid, and the snapshot must include weave state.
2. A loom broken and replaced must keep its stage (copy_state loot), and an older loom block item without stage data must place as stage 0.
3. A rank-gated recipe must be refused at selection and at payment, never silently swapped for another output.
4. Auto-catch must only take slack threads belonging to that loom, and never a taut one.
5. Creative direct-kit use on a loom filler cell must upgrade the main loom.

---

### Task 1: Rules: rank gate, auto-catch and reach

**Files:** modify `common/tile/harbinger/crafting/SomaticWeaveRules.java` and `src/test/.../SomaticWeaveRulesTest.java`.

**Produces:**
- `int SomaticWeaveRules.maxRankOrdinal(int stage)`
- `boolean rankAllowed(EnumManipulationRank rank, int stage)`
- `EnumManipulationRank firstRankNeedingStage(int stage)` (the lowest rank refused below that stage)
- `int stageFor(EnumManipulationRank rank)`
- `double autoCatchRadius(int stage)`: 0 at base, 4.0 from Ganglionic
- `double hookReach(int stage)`: 4.5 base, 8.0 from Ganglionic

- [ ] **Step 1: Failing tests**

```java
	@Test
	void loomStagesWeaveTheRanksTheirDegreeTeaches() {
		assertTrue(SomaticWeaveRules.rankAllowed(SUMMA, 0));
		assertFalse(SomaticWeaveRules.rankAllowed(MAGISTER, 0));
		assertTrue(SomaticWeaveRules.rankAllowed(MAGISTER, 1));
		assertFalse(SomaticWeaveRules.rankAllowed(PERFECTUS, 1));
		assertTrue(SomaticWeaveRules.rankAllowed(PERFECTUS, 2));
		assertEquals(0, SomaticWeaveRules.stageFor(HUMILIS));
		assertEquals(0, SomaticWeaveRules.stageFor(SUMMA));
		assertEquals(1, SomaticWeaveRules.stageFor(MAGISTER));
		assertEquals(2, SomaticWeaveRules.stageFor(PERFECTUS));
	}

	@Test
	void ganglionicLoomsCatchSlackThreadsAndReachFurther() {
		assertEquals(0.0, SomaticWeaveRules.autoCatchRadius(0), 1e-9);
		assertTrue(SomaticWeaveRules.autoCatchRadius(1) >= 4.0);
		assertTrue(SomaticWeaveRules.hookReach(1) > SomaticWeaveRules.hookReach(0));
		assertEquals(SomaticWeaveRules.hookReach(1), SomaticWeaveRules.hookReach(2), 1e-9);
	}
```

- [ ] **Step 2:** Run and watch it fail to compile.
- [ ] **Step 3:** Implement. Each stage owns ranks up to (stage 0 → SUMMA, 1 → MAGISTER, 2 → PERFECTUS):

```java
	public static int stageFor(EnumManipulationRank rank) {
		return switch (rank) { case HUMILIS, MEDIOCRITAS, SUMMA -> 0; case MAGISTER -> 1; case PERFECTUS -> 2; };
	}
	public static boolean rankAllowed(EnumManipulationRank rank, int stage) { return stageFor(rank) <= stage; }
	public static double autoCatchRadius(int stage) { return stage >= 1 ? 4.0D : 0.0D; }
	public static double hookReach(int stage) { return stage >= 1 ? 8.0D : 4.5D; }
```

- [ ] **Step 4:** Run and watch it pass.
- [ ] **Step 5:** Check how `synaptic_node` is obtained, with `grep -rn "synaptic_node" src/main/resources/data src/main/java/.../worldgen | head`. If it only generates in the End Cortical archipelago and is minable (D6 players reach the End), keep it; otherwise swap it. Ledger the result.

### Task 2: Catalog, enum, items, recipes, lang and sprites

**Files:**
- Modify: `common/tile/harbinger/crafting/upgrade/UpgradeStation.java` (append `LOOM("loom", "somatic_loom", 2)`) and `common/tile/harbinger/crafting/upgrade/StationUpgradeCatalog.java` (add `LOOM_WEAVE = "loom_weave"`, `LOOM_WEAVE_GANGLIONIC = "loom_weave_ganglionic"`, and two tiers).
- Modify: `common/init/ItemInit.java` (`ganglionic_harness` UNCOMMON, `cortical_crown` RARE, stack 1).
- Create: item models `assets/hemomancy/models/item/{ganglionic_harness,cortical_crown}.json`; 16×16 sprites `textures/item/{ganglionic_harness,cortical_crown}.png`, authored by a new `tools/model_export/loom_kit_sprites.py` with its palette and grids in `loom_kit_sprites.json`.
  - Ganglionic Harness: a knotted nerve ganglion in a bone harness with trailing nerve cords.
  - Cortical Crown: a pale folded cortex band bound in hematic iron, with a red vein.
- Create: shaped recipes `data/hemomancy/recipe/{ganglionic_harness,cortical_crown}.json`.
  - Harness: NBN / FGF / NTN, where N = nerve_fiber, B = bone_block, F = sanguine_formation, G = blood_crystal_shard and T = puppeteering_thread.
  - Crown: SAS / BCB / SVS, where S = nerve_bundle, A = mnemonic_ambergris, B = hematic_iron_block, C = hematic_memory and V = blood_stained_stone.
- Modify lang:
  - `item.hemomancy.ganglionic_harness` = "Ganglionic Harness"; `item.hemomancy.cortical_crown` = "Cortical Crown";
  - dialogue keys `hemomancy.mnemonist.loom.ganglionic.{title,prompt,requirements,ready,claimed}` and `hemomancy.mnemonist.loom.cortical.{...}`.
- Modify: `common/entity/npc/dialogue/StationUpgradeDialogue.java` (`allowedSpeaker`: `LOOM` → Mnemonist); `client/event/StationUpgradeTooltips.java` (`LOOM` → "Mnemonist"); `common/entity/npc/harbinger/HarbingerMnemonistEntity.java` (append the `LOOM` options after the `SCRIPTORIUM` line).
- Tests:
  - `StationUpgradeCatalogTest`: 12 tiers; the loom branch is D5/D6, 2/3 circuits, 4/8 anchors, 50 mL.
  - `StationUpgradeRecipeResourceTest`: the kit recipes, lang names and item models.

- [ ] Write the failing catalog and recipe test changes first. Run them and see them fail (12 ≠ 10; missing recipe).
- [ ] Implement in the order listed. Generate the sprites, and run the generator's `--check`, mirroring `export_centrifuge_kit_sprites.py`.
- [ ] Run `./gradlew.bat test --tests "*StationUpgrade*"` and expect all to pass.

### Task 3: Stage on the block, blockstate, loot, Creative kits and stage models

**Files:**
- Modify: `common/block/harbinger/crafting/SomaticLoomBlock.java`:
  - `STAGE = StationTierProperty.STAGE` in the default state and `createBlockStateDefinition`;
  - call `CreativeStationUpgrades.tryApply` first in `useItemOn`;
  - placement keeps the stage from the item's `BLOCK_STATE` component (vanilla BlockItem does this).
- Modify: `data/hemomancy/loot_table/blocks/somatic_loom.json`: add `{"function":"minecraft:copy_state","block":"hemomancy:somatic_loom","properties":["stage"]}`.
- Modify: `tools/model_export/somatic_loom.py`:
  - `loom(stage)` adds Ganglionic parts at stage ≥ 1: a larger ganglion (r 2.6), nerve feelers (nerve bundle) running down each upright, and vial cradles wrapped in nerve.
  - It adds Cortical parts at stage 2: a cortical crown over the castle (pale folded hyphae lobes in an iron band), a myelin sheath on the warp threads (thin calcified-hyphae sleeves at intervals), and a red vein band.
  - It emits `somatic_loom_ganglionic.json` and `somatic_loom_cortical.json` (bodies). The glass layer is shared.
  - The blockstate becomes a multipart over facing × stage: the stage body, plus glass for every stage.
- Test: in `StationTierPersistenceResourceTest`, `UpgradeStation.values()` now includes `LOOM`, so the loot assertion covers it automatically. Add a `ModelFirstTileRendererResourceTest` assertion that the blockstate applies `somatic_loom_ganglionic` when `"stage": "1"` and `somatic_loom_cortical` when `"stage": "2"`.

- [ ] Write the failing resource assertions and run them; they fail.
- [ ] Implement the block property and Creative kit use, then compile.
- [ ] Extend the generator. Export, check coplanar 0 fights and determinism, and render the preview for all three stages.
- [ ] Run the contracts; they pass.

### Task 4: The loom as an `UpgradeableStation`

**Files:**
- Modify: `common/tile/harbinger/crafting/SomaticLoomBlockEntity.java`:
  - `implements UpgradeableStation`;
  - fields `UUID machineIdentity` (saved as `MachineIdentity`), `boolean riteLocked` and `@Nullable UUID lastUpgradeRite`;
  - `upgradeStation()` returns `LOOM`;
  - `readyForUpgradeRite` returns `!riteLocked && !isCrafting() && ritualBloodCharged == 0`;
  - `upgradeSnapshot` is `saveWithoutMetadata` minus `RiteLocked`, `LastUpgradeRite` and `neoforge:attachments`;
  - record practice in `tickCompletion`, before `clearActiveRitual`:

```java
		if (player instanceof ServerPlayer serverPlayer) {
			var stationProgress = HemoCapabilityAccess.stationUpgrades(serverPlayer);
			stationProgress.recordUse(UpgradeStation.LOOM, StationUpgradeCatalog.LOOM_WEAVE);
			if (upgradeTier() >= 1 && SomaticWeaveRules.stageFor(rank) >= 1)
				stationProgress.recordUse(UpgradeStation.LOOM, StationUpgradeCatalog.LOOM_WEAVE_GANGLIONIC);
		}
```

  Capture `rank = getWeaveRank()` at the start of `tickCompletion`, before the recipe is cleared.

  - While `riteLocked`, `addItem`, `removeItem`, charge and selection must refuse.
- Create rite JSONs: `data/hemomancy/recipe/cardinal_rite/{ganglionic_weave,cortical_weave}.json`, copied from `eightfold_script.json` / `palimpsest.json`. Change the id, degree (5/6), name, description, the `S` block (`hemomancy:somatic_loom`), the brazier signature (from the constraints, kit first) and the anchors/duration of each tier.
- Register the Liber lessons: `LiberEntryDefinitions.registerUpgradeLesson("cardinal_rite/ganglionic_weave", ...)` and the Cortical one, with book pages under `data/hemomancy/books/libersanguinium/<the station lessons category>/pages/`, mirroring the Scriptorium lessons. Update `BookResourceBindingTest`'s list.
- GameTests: add loom entries to `StationUpgradeGameTests` (the block list, one test per tier, lessons; a `clearSpace` special case if the multiblock needs it) and `StationUpgradeActivationGameTests` (`upgradeBoth` for `LOOM`).
- New `SomaticLoomWeavingGameTests` cases:
  - `upgradeRiteRefusesWhileAWeaveIsPaid`;
  - `completingAWeaveRecordsLoomPractice`;
  - `magisterWeaveOnAGanglionicLoomRecordsCorticalPractice`.

- [ ] Write the failing GameTests and the rite resource tests (`StationUpgradeRiteResourceTest` loops over the catalog and will fail on missing JSON). Run them; they fail.
- [ ] Implement. Run `runSomaticLoomGameTestServer`, `runStationUpgradeGameTestServer` and the station JVM tests; all pass.

### Task 5: Rank gate in the weave, auto-catch and staff reach

**Files:**
- Modify: `SomaticLoomBlockEntity`:
  - in `refreshRecipe` and `selectRecipe`, a recipe whose result rank is not allowed at `upgradeTier()` sets `curRecipe = null`;
  - selection replies "This memory is too refined for this loom. It needs a {Ganglionic|Cortical} Loom." (lang key `hemomancy.loom.tier_required.{1|2}`);
  - `chooseNextRecipe` still lists it, with the same message;
  - `tryChargeRitualBlood` refuses it.
- Modify: in `tickWeave`, after reconcile:

```java
		double catchRadius = SomaticWeaveRules.autoCatchRadius(upgradeTier());
		if (catchRadius > 0 && player != null)
			for (FrayedThreadEntity thread : looseThreadEntities(server))
				if (thread.isSlack() && thread.distanceToSqr(echoAnchor()) <= catchRadius * catchRadius)
					weaveHome(thread, player);
```

- Modify: `LivingStaffItem.use`: before `startUsingItem`, if the player looks at a slack `FrayedThreadEntity` within `SomaticWeaveRules.hookReach(stage of its loom)`, hook it and return success. Use `ProjectileUtil.getEntityHitResult` as in `WillBloodUtilityInteractions.findLookedAtFalteringWill`.
- GameTests:
  - `magisterRecipesNeedAGanglionicLoom` (stage 0 refuses Axonal Transduction, PERFECTUS, and a MAGISTER fixture recipe; stage 1 accepts the MAGISTER one; stage 2 accepts PERFECTUS);
  - `ganglionicFeelersCatchSlackThreadsNearTheEcho`;
  - `feelersNeverTakeATautThread`;
  - `aStaffHooksASlackThreadAtRange`.

  Find a MAGISTER weaving recipe with `grep` over the recipe results and `ManipulationInit` ranks, and use it.

  The existing Axonal (PERFECTUS) tests from Plan 1 now need the fixture loom at stage 2. Set it in their `setup` with `level.setBlock(pos, defaultBlockState().setValue(STAGE, 2), 3)`. Ledger this ruling.

- [ ] Write the failing tests, then implement, then rerun the suite.

### Task 6: Echo by tier

**Files:** modify `client/render/tile/harbinger/crafting/SomaticLoomEcho.java`.

| Stage | Scale (weaving → climax) | Tint | Alpha | Motion |
|---|---|---|---|---|
| 0 | 0.42 → 0.8 | ASH only, monochrome | 0.4–0.85, with a flicker term | Flickers |
| 1 | 0.5 → 0.9 | School wash 0.3–0.45 | Steadier | — |
| 2 | 1.0 (life-size) → 1.0 | Full colour | 0.6–0.95 | Stands on the warp; climax does not grow, it brightens |

Add `"echoStage(te)"` to the contract. Verify in a native capture with three looms at stages 0/1/2.

### Task 7: Docs, teaching and verification

- `docs/STATION_UPGRADES.md`: add the loom to the tier table (D5/D6, Mnemonist, proofs, rites, circuits), the offerings table and its stage effects (rank gates, auto-catch, reach, echo).
- `docs/HEMOMANCY_REFERENCE.md`: the loom row and §25.7.
- `wiki/Harbinger-Path.md`: the loom tiers.
- Mnemonist dialogue lines: the claim prompts and requirement text.
- `docs/validation/SOMATIC_LOOM_REDESIGN_2026_10_10.md`: a Tiers section.
- Verify:
  - all station JVM tests;
  - `runStationUpgradeGameTestServer`;
  - `runSomaticLoomGameTestServer`;
  - the main `runGameTestServer`, naming unrelated failures;
  - a native capture of the three stage bodies and their echoes.
