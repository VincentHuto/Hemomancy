# Endgame Foundations (Section 0) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** D8 becomes two equal ranks (Apotheos, Silentium), only Apotheos travels physically to the Fungal Gardens, a Harbinger reset clears endgame state, and Abhorrent Thought is spelled correctly.

**Architecture:** Path state stays in the `initiatory_degree` attachment (`EnumArchonPath`). Pure rule classes (`HarbingerPathPermissions`, `FungalProjectionRules`, `ChamberProgressionRules`, `DegreeTitles`, ...) carry each decision so JVM tests pin them; server call sites only read the player and call the rule.

**Tech Stack:** NeoForge 1.21.1, Java 21, JUnit 5 (`./gradlew.bat test`), GameTests (`./gradlew.bat runGameTestServer`).

**Spec:** `docs/superpowers/specs/2026-10-27-endgame-foundations-design.md`

## Global Constraints

- Player-facing text is an `en_us.json` key; no `Component.literal` English.
- State read and written through `HemoCapabilityAccess`; degree changes sync through `InitiatoryDegreeEvents.syncDegree`.
- No new `getPersistentData()` calls; lower `persistent-data-allow-list.txt` counts where calls are removed.
- Payload layout change: bump `PacketHandler.PROTOCOL_VERSION` to `"17"` and rewrite `src/test/resources/network/payload-registry.txt` header to `version 17`.
- No commits (user rule: leave work in the working tree).
- The mod is not live: no save migration for the rename.

## Review Focus

1. A D7 `SILENT_PENDING` player who absorbs the Memory without having defeated Vesper (traded or creative-spawned Memory) must be refused and not ascend — Task 2 test `refusesWithoutVesperVictory`.
2. An `APOTHEOS_PENDING` or `NONE` player absorbing the Memory must be refused — Task 2 same test table.
3. A non-Apotheos player already inside the Gardens must still be able to return — Task 5 keeps the return branch ahead of the gate; GameTest assertion in Task 5.
4. A Silentium must not pick up Apotheos-only effects (Monolith burden, primal morphling, Apotheos HUD/arbor, Chamber `apotheos` theme) — Task 3 rule tests.
5. A reset Silentium/Apotheos player must come back as `NONE` with no Spine — Task 6 test.

---

### Task 1: Rename the Silent path and its permission

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/degree/EnumArchonPath.java`, `HarbingerPathPermissions.java`, `InitiatoryDegreeEvents.java` (`migrateLegacyArchonChoice`)
- Modify: `common/command/SilentArchonCommandRules.java`
- Modify every `EnumArchonPath.SILENT_ARCHON` / `isProvenSilentArchon` site: `SilentArchonArmorRules`, `WillEntity`, `BoundSummonBehavior`, `QliphothBloomBlock`, `DialogueHubFactory`, `FungalGardenTravelHelper.legacyArchonChoice`, `ChamberOfWillManager.progressionFacts`, `HarbingerAssignmentLedgerScreen`, `LivingWeaponGraftRite` (compile only here; behaviour in Task 2), `VesperOrdealManager` (compile only here)
- Modify gameTests: `ClinicalBloodProgressionGameTests`, `HemoJourneyChecks`, `PlayerMigrationGameTests`, `PuppeteeringGameTests`, `SuccessionClientReview` (set degree 8 wherever they set `SILENTIUM` as a finished state)
- Test: `HarbingerPathPermissionsTest`, `SilentArchonVerifiedPathTest`, `SilentArchonCommandRulesTest`, `ArchonEndingDialogueTest`

**Interfaces:**
- Produces: `EnumArchonPath.SILENTIUM`; `HarbingerPathPermissions.isSilentium(int degree, EnumArchonPath path)` = `degree >= 8 && path == SILENTIUM`; `isChoiceUnresolved` resolved set = {`APOTHEOS`, `SILENTIUM`}; `SilentArchonCommandRules.transition("complete", d)` = `Transition(8, SILENTIUM, false)`; `SilentArchonArmorRules.SILENT_ARCHON_DEGREE` = 8.

- [ ] **Step 1: Update tests first.** `SilentArchonVerifiedPathTest`: `isSilentium(8, SILENTIUM)` true; `isSilentium(7, SILENTIUM)` false; `isSilentium(8, APOTHEOS)` false; `canRefuseDeath(true, 8, SILENTIUM, …)` true. `SilentArchonCommandRulesTest`: complete → degree 8, `SILENTIUM`. `ArchonEndingDialogueTest`: `SILENTIUM` at degree 8 → `"silent_archon"` line. `HarbingerPathPermissionsTest`: resolved set uses `SILENTIUM`.
- [ ] **Step 2: Run** `./gradlew.bat test --tests "*SilentArchon*" --tests "*HarbingerPathPermissions*" --tests "*ArchonEndingDialogue*"` → compile FAIL (no `SILENTIUM`).
- [ ] **Step 3: Rename and repoint.** Rename the enum constant; rename `isProvenSilentArchon` → `isSilentium` with the D8 rule; update every site listed above (switch cases and equality checks become `SILENTIUM`). Legacy migration: `silent` → `hasDegree(degree, 8) ? SILENTIUM : SILENT_PENDING`. `PlayerMigrationGameTests` expectation follows that mapping.
- [ ] **Step 4: Run** the same tests → PASS; `./gradlew.bat compileGameTestJava` → BUILD SUCCESSFUL.

### Task 2: Silentium is reached by absorbing Vesper's memory

**Files:**
- Modify: `common/capability/player/harbinger/degree/DegreeProgression.java`
- Create: `common/capability/player/harbinger/degree/SilentiumAscensionRules.java`
- Modify: `common/item/harbinger/memories/LivingWeaponGraftRite.java`, `common/encounter/VesperOrdealManager.java`
- Test: `src/test/java/.../degree/SilentiumAscensionRulesTest.java`; update `ArchonPathRiteSourceTest`; update Vesper victory GameTests in `ClinicalBloodProgressionGameTests` (victory leaves `SILENT_PENDING`, degree 7)

**Interfaces:**
- Produces: `DegreeProgression.advance(ServerPlayer player, int target, EnumArchonPath path)` (sets `path` when non-null, before sync and rewards); `advance(player, target)` = `advance(player, target, target == 8 ? APOTHEOS : null)`.
- Produces: `SilentiumAscensionRules.canAbsorbVesperMemory(int degree, EnumArchonPath path, boolean vesperDefeated)` = `degree == 7 && path == SILENT_PENDING && vesperDefeated`.

- [ ] **Step 1: Write `SilentiumAscensionRulesTest.refusesWithoutVesperVictory`** — table: (7, SILENT_PENDING, true) → true; (7, SILENT_PENDING, false) → false; (7, APOTHEOS_PENDING, true) → false; (7, NONE, true) → false; (8, SILENTIUM, true) → false.
- [ ] **Step 2: Run** `./gradlew.bat test --tests "*SilentiumAscensionRules*"` → FAIL (class missing).
- [ ] **Step 3: Implement.** Add the rule class and the `advance` overload. `VesperOrdealManager.completeVictory`: delete the `setArchonPath(SILENTIUM)` block (eligibility check stays). `LivingWeaponGraftRite.checkVesperMemory` uses the rule with `HarbingerAdvancementGranter.hasAdvancement(player, ADV_VESPER_DEFEATED)`; `complete` re-runs `checkVesperMemory` before `awakenVesperMemory`, and after a successful awaken calls `DegreeProgression.advance(player, 8, EnumArchonPath.SILENTIUM)`. Update `ArchonPathRiteSourceTest` to assert the graft rite source contains `EnumArchonPath.SILENTIUM` and the ordeal no longer does.
- [ ] **Step 4: Run** `./gradlew.bat test --tests "*SilentiumAscensionRules*" --tests "*ArchonPathRiteSource*"` → PASS.

### Task 3: D8 identity split (titles, advancement, Apotheos-only visuals and rules)

**Files:**
- Create: `common/capability/player/harbinger/degree/DegreeTitles.java`; `src/main/resources/data/hemomancy/advancement/hemomancy/degree_8_silentium.json`
- Modify: `en_us.json` (`hemomancy.degree.8`, `hemomancy.degree.8.apotheos`, `hemomancy.degree.8.silentium`, `advancements.hemomancy.degree_8_silentium.title` "Silentium of the Hematic Order" / `.description` "Draw Vesper's memory into your Living Staff and stand outside the hive", `manipulation_blocked_until_apotheos` value "Manipulation blocked until your path is complete")
- Modify: `DegreeProgression` message, `client/screen/overlay/BloodVolumeOverlay.java`, `common/command/DegreeSubcommand.java`, `common/event/HarbingerAdvancementGranter.java` (`ADV_DEGREE_8_SILENTIUM`, path-aware `grantDegree`), `LiberDiscoveryEvents` (register silentium → `TRUTH`)
- Modify: `common/entity/utility/ArborOfWillEntity.java` (+ `ArborOfWillVisualRules.visualDegree`), `common/item/harbinger/MonolithFragmentBurdenRules.java` + `MonolithFragmentItem` + `MonolithFragmentItemRenderer`, `PrimalMorphlingRules` + `MorphicNectarEvents`, `ItemInquiryContext`, `ChamberProgressionRules` + `ChamberOfWillManager`
- Modify: `src/test/resources/architecture/persistent-data-allow-list.txt` (ItemInquiryContext count −2)
- Test: `DegreeTitlesTest` (new), `ChamberProgressionRulesTest`, `MonolithFragmentBurdenRulesTest`, `PrimalMorphlingRulesTest`, `ArborOfWillVisualRules` test (new or existing), inquiry tests

**Interfaces:**
- Produces: `DegreeTitles.titleKey(int degree, EnumArchonPath path) -> String` (8+APOTHEOS → `hemomancy.degree.8.apotheos`; 8+SILENTIUM → `.silentium`; else `hemomancy.degree.<clamped 0..8>`), `DegreeTitles.title(...) -> Component`.
- Produces: `MonolithFragmentBurdenRules.shouldBurdenCarrier(EnumArchonPath path)`; `PrimalMorphlingRules.canPrimalize(int maturity, boolean primalized, boolean apotheos)`; `ArborOfWillVisualRules.visualDegree(int degree, EnumArchonPath path)` (`APOTHEOS` → degree, else `min(degree, 7)`); `ChamberProgressionRules.Facts(int degree, boolean firstScarLearned, boolean qliphothStarted, boolean silentium, boolean apotheos)`.
- HUD: `BloodVolumeOverlay` computes `int visualDegree = apotheos ? 8 : min(degree, 7)` for every art decision and uses `DegreeTitles.title(degree, path)` for the label.

- [ ] **Step 1: Write/adjust tests.** `DegreeTitlesTest`: the three D8 cases plus (7, SILENT_PENDING) → `hemomancy.degree.7`. `ChamberProgressionRulesTest`: silentium@8 → State(3, "silent_archon") and themes exclude `apotheos`; apotheos@8 → State(3, "apotheos"). `MonolithFragmentBurdenRulesTest`: only `APOTHEOS` burdens. `PrimalMorphlingRulesTest`: apex + apotheos → true; apex + not apotheos → false. Arbor: (8, SILENTIUM) → 7, (8, APOTHEOS) → 8.
- [ ] **Step 2: Run** those tests → FAIL (signatures).
- [ ] **Step 3: Implement** per the Interfaces block and spec D4/D5. Advancement JSON copies `degree_8_apotheos.json` with icon `hemomancy:memory_of_vesper` and the silentium translate keys.
- [ ] **Step 4: Run** `./gradlew.bat test --tests "*DegreeTitles*" --tests "*ChamberProgressionRules*" --tests "*MonolithFragment*" --tests "*PrimalMorphling*" --tests "*Inquiry*" --tests "*PersistentDataAllowList*" --tests "*Arbor*"` → PASS.

### Task 4: Ledger shows the absorption step

**Files:**
- Modify: `common/network/mission/OpenHarbingerAssignmentLedgerPacket.java` (add `boolean vesperDefeated` after `severedPortalOpen`, encode/decode), `common/item/harbinger/HarbingerAssignmentLedgerItem.java` (fill from `ADV_VESPER_DEFEATED`), `client/network/ClientboundScreenHandlers.java`, `client/screen/item/HarbingerAssignmentLedgerScreen.java`
- Modify: `en_us.json` (`blood_beneath.absorb`, reword `blood_beneath.complete` to "The Evening Star is broken and its memory is yours. You stand outside the hive, Silentium of the Order."), `PacketHandler.PROTOCOL_VERSION` → `"17"`, `payload-registry.txt` header → `version 17`
- Test: `PayloadProtocolVersionTest` (existing)

- [ ] **Step 1:** Bump the registry header to 17 and run `./gradlew.bat test --tests "*PayloadProtocolVersion*"` → FAIL.
- [ ] **Step 2:** Add the field end to end. Ledger order: `APOTHEOS` → apotheos_complete; `APOTHEOS_PENDING` → apotheos_pending; `SILENTIUM` → complete; `severedPortalOpen` → ordeal; `SILENT_PENDING && vesperDefeated` → absorb; `SILENT_PENDING` → prune; then existing. Progress counts `vesperDefeated` as one movement. Bump `PROTOCOL_VERSION`.
- [ ] **Step 3: Run** `./gradlew.bat test --tests "*PayloadProtocolVersion*" --tests "*Ledger*"` → PASS.

### Task 5: Only Apotheos travels physically

**Files:**
- Modify: `common/encounter/FungalProjectionRules.java`, `common/encounter/FungalGardenTravelHelper.java`, `en_us.json`
- Test: `FungalProjectionRulesTest` (create or extend)

**Interfaces:**
- Produces: `FungalProjectionRules.TravelKind { FIRST_PROJECTION, PHYSICAL, REFUSED }`; `travelKind(boolean revelationWitnessed, int degree, boolean spineGranted, EnumArchonPath path)`.

- [ ] **Step 1: Test** `travelKind`: (false, 7, true, NONE) → FIRST_PROJECTION; (true, 8, true, APOTHEOS) → PHYSICAL; (true, 7, true, SILENT_PENDING) → REFUSED; (true, 8, true, SILENTIUM) → REFUSED; (true, 7, true, APOTHEOS_PENDING) → REFUSED.
- [ ] **Step 2: Run** `./gradlew.bat test --tests "*FungalProjectionRules*"` → FAIL.
- [ ] **Step 3: Implement.** In the overworld branch of `handleTravelUse`, after the Spine check and before `drainTravelBlood`, compute the kind; `REFUSED` shows `message.hemomancy.fungal_garden_travel_helper.the_spine_will_not_carry_you` (dark gray italic, action bar) and returns `SUCCESS` without cooldown or blood; `FIRST_PROJECTION`/`PHYSICAL` keep today's calls. Creative and in-Gardens return branches are untouched.
- [ ] **Step 4: Run** → PASS.

### Task 6: Harbinger reset clears endgame state

**Files:**
- Modify: `IInitiatoryDegree.java`, `InitiatoryDegree.java`, `common/capability/PathMutualExclusionHelper.java`
- Test: `InitiatoryDegreeMycophantStateTest` (extend) or new `InitiatoryDegreeEndgameResetTest`

**Interfaces:**
- Produces: `void resetEndgameState()` on `IInitiatoryDegree`.

- [ ] **Step 1: Test** a degree with path `SILENTIUM`, revelation witnessed, Spine granted, Mycophant exposure 100 / cooldown 50 / defeated → after `resetEndgameState()` path `NONE`, false, false, 0, 0, false.
- [ ] **Step 2: Run** → FAIL.
- [ ] **Step 3: Implement**; `resetHarbingerProgress` adds `path != NONE || hasFungalSpineGranted()` to `hadProgress` and calls `resetEndgameState()` after `resetPomeCommunion()`.
- [ ] **Step 4: Run** → PASS.

### Task 7: Abhorrent Thought spelling

**Files:** every hit of `abhorent` / `Abhorent` under `src/`, `tools/worldgen/fungal_gardens_worldgen.py`, `tools/dialogue_editor/en_us.json`, `docs/HEMOMANCY_REFERENCE.md`, `docs/MOB_DROP_CATALOGUE.md`; file and folder renames (`AbhorentThought*.java`, `*AbhorentThought*.bbmodel`, `spawn_egg_abhorent_thought.json`, `sounds/entity/abhorent_thought/`, `textures/entity/abhorent_thought/` + `model_abhorent_thought.png`, `clairaudiograph_sounds/abhorent_thought.json`, `loot_table/entities/abhorent_thought.json`). Leave `.superpowers/`, `.codex-tmp/`, `bin/`, `libs/`, `docs/output/`, `docs/done/` and `docs/architecture/evidence/` history alone.

- [ ] **Step 1:** `git mv` the files/folders, then replace `abhorent` → `abhorrent` and `Abhorent` → `Abhorrent` in the listed text files (preserve each file's line endings).
- [ ] **Step 2: Verify** `grep -ri abhorent src tools/worldgen tools/dialogue_editor docs/HEMOMANCY_REFERENCE.md docs/MOB_DROP_CATALOGUE.md` → no hits.
- [ ] **Step 3: Run** `./gradlew.bat test --tests "*EntityLootTableCoverage*" --tests "*CorticalDrift*"` → PASS.

### Task 8: Docs and full verification

**Files:** `docs/HEMOMANCY_REFERENCE.md` (degree table, §5 Silent/Apotheos ending text ~849–868 and ~1150–1168, Monolith burden ~3060), `wiki/Harbinger-Path.md` (~537), `docs/LORE_REFERENCE.md` (anchor 34, 352), `docs/architecture/evidence/source-text-impact-map.csv`.

- [ ] **Step 1:** Rewrite the Silent-at-D7 lines to Silentium at D8 by Memory absorption; add the Apotheos-only travel rule; Monolith burden is Apotheos-only.
- [ ] **Step 2:** `python gradle/source-text-impact-map.py`.
- [ ] **Step 3: Run** `./gradlew.bat test` → all PASS; `./gradlew.bat build` → BUILD SUCCESSFUL; `./gradlew.bat runGameTestServer` → only the three pre-existing failures (centrifuge journey fixture, registry snapshot / block-item exceptions on `brood_tumor`), plus a fixed registry snapshot entry for the rename if it lists entity ids.
