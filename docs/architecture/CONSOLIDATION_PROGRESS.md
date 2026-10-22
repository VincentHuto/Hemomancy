# Consolidation Progress Ledger

Every consolidation session reads this file first and updates it last, following
[CONSOLIDATION_SESSION_RULES](CONSOLIDATION_SESSION_RULES.md).

## Step map

| Step | Roadmap phase(s) | Status | Commit(s) | Date |
|---|---|---|---|---|
| 1 | Urgent defect fixes (F-1, F-2, F-3) | DONE | `025f2b18f` | 2026-10-21 |
| 2 | Phase 0: green baseline and test infrastructure | DONE | `c435034d4`, `ddb358304` | 2026-10-21 |
| 3 | Phase 1: verified dead-code removal and packaging | DONE | `ccc75e9a0` | 2026-10-21 |
| 4 | Phase 2: network hygiene | DONE | `d10e5e7f2` … `018394712` (10 commits) | 2026-10-21 |
| 5 | Phase 3: player-state plumbing | NOT STARTED | | |
| 6 | Phase 4: shared small utilities | NOT STARTED | | |
| 7 | Phase 5: blood accounting | NOT STARTED | | |
| 8 | Phase 7: registration and client registration | NOT STARTED | | |
| 9 | Phase 6: combat targeting and damage attribution | NOT STARTED | | |
| 10 | Phases 8 + 9: rendering plumbing and animation conventions | NOT STARTED | | |
| 11 | Phase 10: god-class decomposition | NOT STARTED | | |
| 12 | Phase 11: persistence authority migrations | NOT STARTED | | |
| 13 | Phases 12 + 13: content, worldgen, data, tooling, standards | NOT STARTED | | |

Statuses: `NOT STARTED`, `IN PROGRESS`, `DONE`, `DONE (uncommitted)`, `BLOCKED`.

## Baseline (2026-10-21, commit `3fae723d0`)

- **Compile:** pass.
- **`test`:** 3,908 tests, 20 failed. These 20 failures are the accepted baseline until
  step 2 clears them:
  - `HematicArmatureModelSourceTest` ×2
  - `VialCentrifugeModelSourceTest` ×2
  - `MnemonicReliquaryModelSourceTest`
  - `CardinalRitePlantedStaffModelTest`
  - `PhlegethonticBombardierResourceTest`
  - `BloodProfileMigrationTest`
  - `LivingSickleFeatureSourceTest`
  - `ClientModelTextureCoverageTest`
  - `EnglishLocalizationEncodingTest`
  - `GameTestHarnessSourceContractTest` ×2
  - `LegacyMainTestAdapterTest`, covering `ModelFirstTileRendererResourceTest`,
    `MorphlingLumenlaceRenameResourceTest`, `VestmentOfTheFinalMoltResourceTest`,
    `ArmorPreparationResourceTest`, `UtilityPolishResourceTest` and
    `LivingWeaponGraftRiteSourceTest`
  - `LegacyMainTestSuiteContractTest`
- **GameTests, `alphaCheck`, server and client start:** not run at baseline.
- **Full log:** [evidence/08-baseline-test.log](evidence/08-baseline-test.log).

## Baseline after step 2 (2026-10-21, commit `ddb358304`)

From step 3 onward the failure set must be empty.

- **`test`:** 3,926 tests, 0 failures, 0 skipped.
- **`alphaCheck`:** green. All 999 required GameTests passed, and so did `verifyGameTestResourceLog` and
  `verifyPackagedClientResources`.
- **Dedicated server (`runServer`, throwaway game directory):** reached `Done (6.262s)`, with 0 ERROR lines and no
  client-class or invalid-dist loading.
- **Client (`runClient`, throwaway game directory):** reached the title screen, with 0 ERROR lines.
- **Impact map:** [evidence/source-text-impact-map.csv](evidence/source-text-impact-map.csv). It lists, for each
  test, every file it reads (2,983 rows: 494 tests, 380 of them reading 967 Java files). Regenerate it with
  `python gradle/source-text-impact-map.py`.

## Decisions

The owner fills in the **Decision** column. A session may implement an item only when it
reads `APPROVED`. Any other value means the session leaves the item alone and lists it
under *Pending approvals*. IDs refer to [ARCHITECTURE_AUDIT](ARCHITECTURE_AUDIT.md).

| ID | Item | Step | Decision | Notes |
|---|---|---|---|---|
| F-1 | Reject non-finite or non-positive blood donations | 1 | APPROVED | Approved in the step 1 prompt; done in `025f2b18f` |
| F-2 | Register the 4 client-only bidirectional payloads as server-to-client only | 1 | APPROVED | Approved in the step 1 prompt; done in `025f2b18f` |
| F-3 | Remove the client-class call from `WillEntity` | 1 | APPROVED | Approved in the step 1 prompt; done in `025f2b18f` |
| M-1 | Hand-edited models vs their generators (Armature, Centrifuge, Reliquary, loom) | 2 | APPROVED | Owner chose "drop the marker checks" (2026-10-21). The models match their generators byte for byte; the failing tests only looked for a provenance string no generator writes. Done in `ddb358304` |
| M-2 | Five unregistered mobs (Abyssal Siphon, Cruor Fiend, Frozen Clot, Synapse Hound, Void Drinker) | 3 | PENDING | Delete / archive branch / keep |
| M-3 | Other "REQUIRES VERIFICATION" dead code (audit section E) | 3 | PENDING | Per item. Step 3 added three: `IBloodRoutingTarget`, `FeatureLogic` and five textures orphaned by the removals. Step 4 added two more orphaned textures: `textures/gui/vein_selection.png` and `vein_teleport.png` |
| N-6 / N-7 payload set | Fold the single-purpose VFX payloads (N-6) and delete the never-sent payloads (audit section E) under a new `PROTOCOL_VERSION` | 4 | APPROVED | Owner approved 2026-10-21. Payload byte layouts are frozen by the session rules, so this needed approval |
| F-6 | Ledger actions require the ledger item | 4 | APPROVED | Owner approved 2026-10-21 (step 4 prompt) |
| F-7 | Bloodline chat rate limit | 4 | APPROVED | Owner approved 2026-10-21: a simple per-player rate limit, with excess messages dropped. Vanilla chat integration (signing, mute, logging) stays out of scope |
| F-8 | Reject NaN pool settings; clamp client partial tick | 4 | APPROVED | Owner approved 2026-10-21 |
| F-9 | Structure paste requires operator permission and the held spawner | 4 | APPROVED | Owner approved 2026-10-21 |
| F-4 | ScaleGrip: respect cancelled deaths; persist pending returns | 5 | PENDING | |
| F-5 | OrganEcho clears the correct slot | 5 | PENDING | |
| F-19 | Station kits follow the delivery contract (alive, not observational, no Creative discard) | 5 | PENDING | |
| F-20 | Resync Ichorian knowledge on dimension change | 5 | PENDING | |
| F-12 | Projected blood honours the rite lock | 7 | PENDING | |
| F-13 | Scar upkeep becomes reserve-protected and synced | 7 | PENDING | Balance |
| F-14 | Passive scar effects survive milk | 7 | PENDING | Design |
| F-15 | Living weapons check `isActive()` and stop wiping the reserve | 7 | PENDING | Balance |
| F-22 | Hide the legacy Dictation Table from the main creative tab | 8 | PENDING | |
| G-1 / F-18 | Explicit damage stages and one death-save stage | 9 | PENDING | Balance: blood lost per hit changes |
| F-16 | Attribute AoE damage from armour, Still Arts, Sheolic and Saprovitta; respect allies and PvP | 9 | PENDING | Balance |
| F-17 | Curved Horn uses `LastRiteHelper` | 9 | PENDING | |
| G-2b | The caster's own puppets stop matching `Monster.class` filters | 9 | PENDING | |
| A-4 | Animations that would freeze under `/tick freeze` | 10 | PENDING | Per animation |
| V-3b | Ferric quad-encoder constant (128 vs 127) | 10 | PENDING | Visual check |
| F-11 | Blood-structure crafts tick once per server tick | 11 | PENDING | Crafts take their intended time |
| S-9 | Per-player `dataVersion` and gated one-shot migrations | 12 | PENDING | Save-critical |
| F-21 | Archon choice: attachment is the only authority | 12 | PENDING | Save-critical |
| S-6c | Circus progress moved into one attachment | 12 | PENDING | Save-critical |
| S-1c | Chamber access and `PendingWhispers` moved into attachments | 12 | PENDING | Save-critical |
| S-7 | Two-tier cooldown convention | 12 | PENDING | Keep each entry's tier |
| O-5 | Typed components for reworked items | 12 | PENDING | Save-critical |
| F-23 | Move tag files to singular folders (the tags start working) | 13 | PENDING | |
| F-24 | Three template-less structures and the empty mausoleum tag | 13 | PENDING | Finish / remove |
| F-25 | Register or remove the MnA config read | 13 | PENDING | |
| F-26 | Chthonian Queen build name | 13 | PENDING | Check data-fixer impact |
| F-27 | `HemolysisEffect` category consistency | 13 | PENDING | |
| T-6b | Prune unreferenced `libs/` jars; move dev-only dependencies to `localRuntime` | 13 | PENDING | |

## Guardrail tests

Each step lists the regression and guardrail tests it adds. Every later step must keep
them green.

| Test | Added in step | Protects |
|---|---|---|
| `common/capability/player/harbinger/bloodvolume/LumpDonationValidationTest` | 1 | F-1: NaN, infinite, zero and negative lump donations give nothing |
| `architecture/ClientBoundPayloadRegistrationTest` | 1 | F-2: the 4 client-handled payloads have exactly one registration, `playToClient` |
| `architecture/WillEntityClientIsolationTest` | 1 | F-3: `WillEntity` has no `net.minecraft.client.` or `com.vincenthuto.hemomancy.client.` reference |
| `architecture/RepositoryTextEncodingTest` | 2 | Every text file under `src/`, `docs/`, `wiki/` and `tools/model_export/` is strict UTF-8 |
| `architecture/PrivateTestHelperRatchetTest` | 2 | Private `read`/`assert*` helper copies in tests may only fall (ceiling 593). Lower the ceiling when you remove copies |
| `support/SourceAssertionsTest` | 2 | The shared helpers' `methodBody`, `section`, failures and line-ending handling |
| `testing/LegacyMainTestSuiteContractTest` | 2 | The adapter runs exactly the legacy `main()` tests found in source (set comparison, no hard-coded count) |
| `architecture/SourceTreeHygieneTest` | 3 | No empty `.java` files, no empty `@SubscribeEvent` handlers, and `hemomancy.mixins.json` lists exactly the `@Mixin` classes in `mixin/core` |
| `verifyPackagedClientResources` (part of `alphaCheck`) | 3 | The jar holds no `.cache/`, `/old/`, `.bbmodel` or `bbmodel/reference/` entries |
| `architecture/PayloadDirectionTest` | 4 | Every inline send matches its payload's registered direction, and only `AntecedentEffectPacket` is bidirectional |
| `architecture/PayloadProtocolVersionTest` | 4 | The registered payload set (with directions) matches `src/test/resources/network/payload-registry.txt` for the current `PROTOCOL_VERSION` |
| `architecture/NetworkClientIsolationTest` | 4 | `common/network` has no `net.minecraft.client` reference and no `Minecraft.getInstance()` |
| `architecture/SendFacadeTest` | 4 | Only `PacketHandler` and `DialogueSender` call `PacketDistributor` |
| `architecture/ServerboundGuardsCoverageTest` | 4 | Every serverbound payload uses `ServerboundGuards` or is listed with a reason; stale exemptions fail |
| `common/network/ServerboundGuardsTest`, gameTest `ServerboundGuardsGameTests` | 4 | `finite`, `partialTick`, `menu`, `withinReach` / `nearBlock` / `nearBlockEntity`, `nearEntity` |
| `common/network/particle/VisualPayloadCodecTest` | 4 | Every `ParticleBurstPacket` and `EntityVisualPacket` kind round-trips |
| gameTest `NetworkDefectGameTests` (batch `network_defects`) | 4 | F-6 ledger in hand, F-8 non-finite pool settings, F-9 structure-paste operator and spawner |
| `common/network/capa/harbinger/BloodlineChatRateLimitTest` | 4 | F-7 sliding window and per-sender separation |

## Step entries

<!-- Append one entry per step, copying this template:

### Step N: <name>
- Status / date / commits:
- Test counts before → after:
- Metrics (classes and lines removed and added, call sites migrated, remaining legacy instances):
- Guardrails added:
- Approved items implemented:
- Pending approvals:
- Deviations from the roadmap:
- Not verified (needs in-game or visual check):
- Handoff notes for the next step:
-->

### Step 1: Urgent defect fixes
- **Status / date / commits:** DONE, 2026-10-21. Code in `025f2b18f`; this ledger update follows it in a second `consolidation(step-1):` commit.
- **Test counts before → after:**
  - `test`: 3,911 tests, 20 failed → 3,919 tests, 20 failed. The failure set is identical to the baseline list above.
  - The 8 new tests all pass. The two updated main-style tests (`CellHandRenderingResourceTest`, `WillBloodUtilitySourceTest`) pass through `LegacyMainTestAdapterTest`.
  - `runGameTestServer`: the dedicated server loads and runs all 999 GameTests. 3 fail. On the stashed pre-change tree the same run fails 4: the same 3 plus `carrioncannothealfromoverkill`, which is flaky. No new failure. See *Known issues* below.
- **Metrics:**
  - Main code: 25 lines added, 11 removed across 6 files. One method removed (`WillAbsorptionGlowParticleFactory.createPulseData`). Two methods added (`BloodVolumeTransferRules.donationAmount`, `HemoParticleData.willAbsorptionGlowPulse`).
  - Tests: 3 new files (113 lines). 2 source-text tests repointed (4 lines added, 3 removed).
  - Call sites migrated: 1 donation path, 4 registrations, 1 particle call. Remaining `createPulseData` callers: 0.
  - Remaining legacy: the 4 payload handlers still live in `common/network` and call `Minecraft` / `LayerEvents` directly. Moving them to `client/**` is Phase 2 item 3 (step 4).
- **Guardrails added:** `LumpDonationValidationTest`, `ClientBoundPayloadRegistrationTest`, `WillEntityClientIsolationTest` (see the table).
- **Approved items implemented:** F-1, F-2, F-3.
- **Pending approvals:** none new. Everything else in *Decisions* is untouched.
- **Deviations from the roadmap:**
  - F-2 changes only the registration direction. All four payloads are built and sent server-side through `PacketHandler.sendToPlayer`, so their handlers never run on a server and moving them was not needed. Step 4 moves them into `client/**` with the other client handlers.
  - The new guardrails read source text directly because the shared `SourceAssertions` helper arrives in step 2. Step 2 should switch them to it.
  - `alphaCheck` was not run as one task. It depends on `test` and `runGameTestServer`, which both fail on the baseline, so it cannot go green before step 2. Its three parts were checked one by one:
    - `test`: as above.
    - GameTests: as above. Neither log has a Hemomancy `/ERROR]` line, which is the resource-log rule.
    - `verifyPackagedClientResources`: fails on 18 paths under `textures/item/old/sanguine formation/` (space in the folder name, from `08eea4acd`). That predates this step and no resources were touched.
- **Not verified (needs in-game or visual check):**
  - The F-3 crash path itself: completing a Will absorption on a dedicated server. The server now loads with `WillEntity` free of client references, but no GameTest completes an absorption.
  - The Will absorption pulse visual. The particle data is byte-identical (same type, colour, `outwardPulse = true`), so no review client was run.
- **Known issues (pre-existing, unrelated to this step):**
  - GameTests failing on the baseline: `selectedloomrecipesurvivesstockandchargedreload`, `hemojellyfloatsatsurfaceandfeedsonsmallfish`, `blockitemexceptionskeeptheirowners`, and `carrioncannothealfromoverkill` (flaky).
  - The `verifyPackagedClientResources` path failure above.
- **Handoff notes for step 2:**
  - Step 2 baseline: 3,919 JUnit tests with the 20 known failures. Also record the 4 GameTest failures and the packaged-resource failure above, since `alphaCheck` must be green at its end.
  - The GameTest task is `runGameTestServer`. `AGENTS.md` and the step prompts say `gameTestServer`, which Gradle does not have.
  - When step 2 adds `support/SourceAssertions`, move the two new architecture tests onto it.

### Step 2: Phase 0, green baseline and test infrastructure
- **Status / date / commits:** DONE, 2026-10-21.
  - `c435034d4` tracks `tools/model_export/`.
  - `ddb358304` contains the fixes, helpers and guardrails.
  - A third `consolidation(step-2):` commit records this entry.
- **Test counts before → after:**
  - `test`: 3,919 tests, 20 failed → 3,926 tests, 0 failed, 0 skipped.
  - `runGameTestServer`: 999 run, 3 failed (plus 1 flaky) → all 999 required tests passed.
  - `alphaCheck`: red → green.
- **The 20 JUnit failures, by cause:**
  - **Generator markers (6, owner decision M-1):** removed only the assertions that looked for `tools/model_export/<gen>.py` inside model JSON, in `HematicArmatureModelSourceTest`, `VialCentrifugeModelSourceTest`, `MnemonicReliquaryModelSourceTest` and `ModelFirstTileRendererResourceTest`. Structural checks stay. No generator has ever written that string. Every generator reproduces its committed JSON byte for byte.
  - **Encoding (3):**
    - `en_us.json`: 240 values double-encoded in `08eea4acd` were repaired (`Â§`→`§` ×469, `Â·`→`·` ×9, `â†’`→`→`, and `Thelēma`). Keys, order and line endings are unchanged.
    - Stray cp1252 `×` bytes were fixed in `docs/TESTING.md` and `docs/validation/PRIORITY_FINDINGS_2026_10_02.md`.
  - **Missing asset (1):** restored the five `textures/item/{covenant_mantle,silent_archon_*}.png` deleted in `08eea4acd`. They are byte-identical to the `textures/item/old/` copies. Their models had fallen back to missingno for particles and sprite lookups.
  - **Stale tests (10).** Each was re-pinned to current, intentional code or data, with the cause traced to a commit:
    - bombardier loot and hemojelly aquatic membership: `08eea4acd`
    - Chromatophores display name: `08eea4acd`
    - Vestment rig: `31e640b83`
    - graft-rite particle signature: `3fae723d0`
    - Vesper sickle grip: `31e640b83`. The test now checks that the sickle still renders through the item's hand transform.
    - planted-staff fist: generated fittings append the fist. The test now locates the 27-element run instead of using fixed indices.
    - utility polish: barometer shapes, crucible spout and basin constants, ossuary bounds (now a JSON bounds check), and the candle flame (now in the lit model)
    - loom renderer hooks: `dcdf960e5`
    - rename scan: reworded the retired pre-Lumenlace strain name in four tracked docs, and the scan now treats `docs/OrchestratorReports/` as an archive, like `docs/done/`.
  - **Stale count (1):** `LegacyMainTestSuiteContractTest` now compares the set of source-discovered `main()` tests with the adapter's set.
- **GameTests fixed (all test-side):**
  - `selectedLoomRecipeSurvivesStockAndChargedReload`: compares thread spans before and after reload. Engram Weaving stores 3 spans per enzyme, and payment spends none.
  - Hemojelly, saved-capture and siphonophore pool tests: their mobs are persistent, so a player in a concurrent test can no longer far-despawn them.
  - `blockItemExceptionsKeepTheirOwners`: compares exact id sets (17/6/7). `085166b7d` had removed two custom block items.
  - The flaky `carrionCannotHealFromOverkill` (and its choir and stillness siblings): the combat fixture now loads every chunk in the rectangle between its corners, not just the two corner chunks.
- **Other Phase 0 items:**
  - **Shared test support:** `support/RepoPaths`, `SourceAssertions` and `ResourceReader`. Converted tests: the two step 1 guardrails, `VestmentOfTheFinalMoltResourceTest`, `ArmorPreparationResourceTest`, `LivingWeaponGraftRiteSourceTest`, `PhlegethonticBombardierResourceTest` (partly) and `LegacyMainTestSuiteContractTest`.
  - **`tools/`:** `.gitignore` now ignores `/tools/*` except `tools/model_export/`, which holds 59 text generators, about 1.3 MB, with no secrets or user paths. `PuppeteerCardinalOrdealResourceTest`'s skill-tree-editor check moved to its own test, which `assumeTrue`-skips when the editor is absent.
  - **CI:** `alpha-check.yml` also runs on pushes to `neo-1.21.1`.
  - **Packaging:** `build.gradle`'s exclude for the unused `sanguine formation` frames now points at `textures/item/old/`, where `08eea4acd` moved them.
- **Metrics:**
  - Production: 240 lang values repaired, 5 textures restored, 1 build exclude corrected. No Java changed.
  - Tests: `src/test` +507/−179 across 23 files. Support and architecture: +338/−12. GameTests: +49/−20.
  - Private helper copies: 593 remain, and the ratchet holds them at that ceiling.
  - New tooling: `gradle/source-text-impact-map.py` (140 lines) and the 2,983-row CSV.
- **Guardrails added:** `RepositoryTextEncodingTest`, `PrivateTestHelperRatchetTest`, `SourceAssertionsTest`, and the rewritten `LegacyMainTestSuiteContractTest` (see the table).
- **Approved items implemented:** M-1 (drop the marker checks).
- **Pending approvals:** none new.
- **Deviations from the roadmap:**
  - M-1 turned out not to be "regenerate vs accept hand edits". There were no hand edits, so no model changed, and no visual check was needed.
  - `/tools/`: the roadmap offered "track or skip". Model generators are tracked, and only the editor check skips.
  - The local-only `docs/MANIPULATION_BALANCE_PLAN.md` was reworded and `docs/done/.../orcadian-horseman/README.md` was re-encoded, so local runs match CI.
  - Dedicated-server and client starts used throwaway game directories under `build/` (from a scratch init script), so the shared `run/world` save was not touched.
- **Not verified (needs in-game or visual check):**
  - The restored armour inventory textures are visible only as break and item particles. Their in-hand rendering comes from the 3D armour BEWLR and is unchanged.
  - The `§` colour codes in the repaired dialogue lines were not checked on screen.
- **Known issues (pre-existing, not fixed here):**
  - `models/item/hematic_microscope_viewing_left` references a missing `hemomancy:item/hematic_microscope` texture. The client logs it as a WARN.
  - `tools/model_export/station_kit.py` was edited after its outputs were last generated. Re-running it would change the glass of the lantern, scar station, incubator and crucible: the panes lose their thin edge faces. Re-run it deliberately, with a visual check.
- **Handoff notes for step 3:**
  - The baseline is fully green. From now on any new failure blocks.
  - Use the impact map before moving or deleting code, and use the `support/` helpers in every test you touch. Lower `PrivateTestHelperRatchetTest.CEILING` whenever copies are removed.
  - Never write the retired pre-Lumenlace strain name in any doc, including this ledger: the rename scan reads all of `docs/`.
  - The dedicated-server start used the same pattern as the review clients. A scratch init script added `consolidationServer` and `consolidationClient` runs with game directories under `build/`.

### Step 3: Phase 1, verified dead-code removal and packaging
- **Status / date / commits:** DONE, 2026-10-21. The code is in `ccc75e9a0`; a second `consolidation(step-3):` commit records this entry and the audit-table moves.
- **Test counts before → after:**
  - `test`: 3,926 → 3,928, 0 failures. One test was deleted with the dead code it pinned; three hygiene tests were added.
  - `alphaCheck`: green, with all 999 required GameTests passing.
  - Client start (shader reload): reached the title screen with 0 ERROR lines and no shader failures.
  - Dedicated server start: reached `Done (5.149s)` with 0 ERROR lines.
- **Removed (all SAFE items in audit section E, each re-verified with `git grep` and an impact-map pass):**
  - 54 unused Vesper clips (2,741 lines).
  - `RenderBloodLaser` and `RenderBloodLaserEvent`. This removes the per-frame `RenderLevelStageEvent` loop over all players.
  - The 4 empty classes, plus 9 unreferenced classes from the list.
  - `HemoBookReloadListener`, `MixinScreenRenderState` (an empty file), `AbstractFungusFeature`, `BigMushgloomFeature`, `FungusFeatureConfig`, and `TestRegion1` and `TestRegion2`.
  - These members: `Hemomancy.instance` and `forcesLoaded`, `AttributeInit.canFly`, `ScarInit.getAllEntries`, `VillagerInit.HEMOPOTHECARY_PROCESSOR_LIST_KEY`, `BaseFeatureInit.setup`, the empty `EffectInit.setupPotionRecipes` and `EntityInit.commonSetup` handlers, `BloodManipulation.performDrudgeAction`, `IBloodVolume.drainFromSource` (and its 2 implementations), and `BloodCraftingKeyPressPacket.getMatchBounds`.
  - 11 unused `RenderTypeInit` entries. `LASER_MAIN_ADDITIVE` became dead once the laser was removed.
  - `RadiantPortalRendertype.END_PORTAL`, `textWithRayShader` and their ray shard and transparency state.
  - The `ray`, `distorted_texture` and `scar_glow` shaders and their 6 program files. `screen_generic.vsh` is kept because 3 other shaders use it.
  - The `item_blood_volume` attachment registration. Item stacks cannot hold attachments in 1.21.1, and the only write was removed in `b363f22d7`. The item capability key with the same id is unchanged.
- **Packaging:**
  - The `.cache` exclude now matches; it is relative to each resource root.
  - `assets/hemomancy/**/old/**` and `models/entity/bbmodel/reference/**` are excluded. This replaces three narrower `old/` excludes.
  - The jar went from 95,622,844 to 91,543,031 bytes (−4.08 MB). Entries went from 12,348 to 12,131, and classes from 4,920 to 4,899. The jar now holds 0 `/old/`, 0 `.cache` and 0 reference entries.
- **Local cleanup:**
  - Deleted the 4 untracked `*.java~` backups. Each matched a committed revision of its file.
  - Deleted the tracked `bash.exe.stackdump`.
- **Metrics:**
  - `src/main`: +0 / −3,951 lines across 22 deleted classes, 6 deleted shader files, and edits to 20 other files.
  - `src/test`: +82 / −25 (the new hygiene test, plus 5 tests updated or trimmed).
  - Remaining legacy instances in scope: 0.
- **Tests updated with the removals:**
  - `RegistryBackedScarRefactorSourceTest`: dropped the `getAllEntries()` pin.
  - `BloodVolumeTransferRulesTest` and `BloodRoutingRulesTest`: dropped the `drainFromSource` stub overrides.
  - `MemoryWeavingRedesignSourceTest`: dropped the `LOOM_EFFECT_CORE` pins, which gave false coverage.
  - `AvatarFirstPersonRendererTest`: deleted `avatarShellDoesNotWriteDepthOverHeldItems`, which pinned the never-called `firstPersonEnergySwirl`.
- **Guardrails added:** `SourceTreeHygieneTest`, plus the authoring-file check in `verifyPackagedClientResources` (see the table).
- **Approved items implemented:** none needed. Everything removed was SAFE.
- **Pending approvals (moved from SAFE to REQUIRES VERIFICATION, recorded under M-3 and in the audit):**
  - `IBloodRoutingTarget`: `HEMOMANCY_REFERENCE.md` documents it as the routing hook.
  - `FeatureLogic`: the target of the W-2 setter merge.
  - The orphaned textures `textures/misc/laser.png`, `laser2.png`, `laser_glow.png`, `vine.png` and `textures/entity/chitinite/model_chitinite_warn.png`.
- **Deviations from the roadmap:**
  - The roadmap says to move the `/old/` archives and the reference sheet out of `src/main/resources`. The audit evidence and the existing `build.gradle` convention ("Preserve authoring archives in source") instead keep them in source and exclude them from packaging, which is what this step does. Deleting or relocating them is left to the owner.
- **Not verified (needs an in-game check):** none beyond the client and server starts. Every removal was unreferenced, so it has no visible effect.
- **Known issues (pre-existing, not fixed):**
  - `BloodCraftingKeyPressPacket` imports `HarbingerCardinalRiteEvents` without using it.
  - `IWhiteHumorVolume.drainFromSource` has no callers. It is not on the SAFE list.
  - `docs/done/consolidation/capability-storage-index.md:40` calls `item_blood_volume` "serialized".
  - `SourceTreeHygieneTest` reads only `hemomancy.mixins.json`. If the dormant MnA mixins are re-enabled, extend it to `hemomancy.mna.mixins.json`.
- **Handoff notes for step 4 (Phase 2, network hygiene):**
  - The baseline is fully green: 3,928 JUnit tests and 999 GameTests.
  - `PacketHandler.java` is pinned by about 23 tests. Check `docs/architecture/evidence/source-text-impact-map.csv` before moving registrations.
  - The step 1 guardrail `ClientBoundPayloadRegistrationTest` expects exactly one `playToClient` registration for each of the 4 F-2 payloads. Moving their handlers into `client/**` (Phase 2 item 3) is compatible with it.
  - Use the scratch init-script pattern from step 2 for the server and client starts, so `run/world` stays untouched.

### Step 4: Phase 2, network hygiene
- **Status / date / commits:** DONE, 2026-10-21. Ten commits on `neo-1.21.1`:
  - `d10e5e7f2`: `PROTOCOL_VERSION`, and the 18 never-sent payloads deleted
  - `3f86520b8`: registrations in their real direction
  - `b8062c743`: the send facade
  - `95cb2e1ef`: `ServerboundGuards`
  - `599762d61`: the VFX fold
  - `36c4558a1`: F-6
  - `e62941c32`: F-8
  - `3c3241f9a`: F-9
  - `060bf8423`: F-7
  - `018394712`: review fixes and docs

  A further `consolidation(step-4):` commit records this entry.
- **Test counts before → after:**
  - `test`: 3,928 → 3,939, 0 failures.
  - GameTests: 999 → 1,005 required, all passing in the final `alphaCheck`. See *Known issues* for flakiness that predates this step.
  - Dedicated server start: `Done (5.4s)`, 0 ERROR lines. Client start: title screen reached, 0 ERROR lines.
- **What changed:**
  - **Protocol version:** `PacketHandler.PROTOCOL_VERSION` is now `"3"`. Before this step it was effectively the mod ID. Mismatched client and server builds are refused at the handshake.
  - **Payload set:** 181 → 157.
    - 18 never-sent payloads deleted, plus `TeleportToVeinPacket` and the unreachable `RadialChooseVeinScreen`.
    - 8 single-purpose VFX payloads folded into `ParticleBurstPacket` and `EntityVisualPacket`.
    - Owner-approved; see *Decisions*.
  - **Directions:** 15 client-only bidirectional payloads are now `playToServer`. `AntecedentEffectPacket` is the one documented bidirectional payload. The `isClientSide` wrappers that existed only because of the bidirectional registration are gone.
  - **Client code in `common/network`:**
    - The 3 `PacketHandler` lambdas became `handle` methods.
    - 6 handlers read the level from `ctx.player()` instead of `Minecraft.getInstance()`.
    - `common/network` no longer references vanilla client classes.
  - **Sending:**
    - All 68 direct `PacketDistributor` calls in 45 files go through the `PacketHandler` facade (`sendToPlayer`, `sendNear`, `sendTracking`, `sendToAll`, `sendToDimension`, `sendToServer`).
    - Dialogue routing moved to `DialogueSender`.
    - The per-effect helpers are thin wrappers over the facade.
  - **Validation:** `ServerboundGuards` holds `menu`, `withinReach` / `nearBlock` / `nearBlockEntity`, `nearEntity`, `heldItem`, `finite` and `partialTick`.
    - 13 payloads use it; every other serverbound payload has a stated reason in `ServerboundGuardsCoverageTest`.
    - The identical cast-eligibility core became `BloodManipulation.isEquippedCastReady`, used by the charge visual and the casting animation. The other cast checks differ on purpose (paralysis flavour, availability, retirement) and were left alone.
- **Metrics:**
  - `src/main`: +971 / −2,452 lines, 28 files deleted and 6 added.
  - `src/test`: +637 / −105. `src/gameTest`: +362 / −2.
  - Remaining legacy instances:
    - direct `PacketDistributor` use outside the facade: 0
    - client references in `common/network`: 0
    - undocumented bidirectional registrations: 0
- **Approved items implemented:** the payload-set changes (N-6 / N-7), F-6, F-7, F-8 and F-9.
  - **F-9 behaviour note:** the Structure Spawner now needs operator permission. In a single-player world created without cheats, the owner is not an operator, so the spawner does nothing there.
  - **F-7 limit:** 3 messages per sender per 100 server ticks; the excess is dropped with an action-bar note.
- **Pending approvals:** none new. Two more orphaned textures were added to M-3.
- **Deviations from the roadmap:**
  - Roadmap item 3 asks to move the client handling of the 8 inline files into `client/**`. The 6 live ones only looked an entity up on the client level, so they now use `ctx.player().level()` (an existing side-agnostic handler style) instead of a new client class. The other 2 files were dead and are deleted.
  - Roadmap item 4 says to replace the per-effect helpers with generic `sendNear` / `sendTracking`. Tests and many call sites use the helper names, so the helpers stay as one-line wrappers.
- **Visual check (VFX fold):**
  - `PayloadFoldVisualReview` (opt-in review harness in `src/gameTest`) captured all nine effects through their unchanged `PacketHandler` helpers, before and after the fold.
  - Screenshots: `build/payload-fold-review/before/`, `after/`, and the side-by-side sheets `compare-0.png`, `compare-1.png` and `compare-2.png`.
  - Match confirmed: Monolith shatter, shard pulse, pome pulse and the Dislocation darkening look the same.
  - Not visible in either capture: the avatar-hit, flask, claw and tool-break particles (too small at that range) and Crimson Fire (it needs a burning entity). For those, the evidence is the verbatim client code and `VisualPayloadCodecTest`.
  - The run configuration came from a scratch init script (`payloadFoldReviewClient`, game directory `build/payload-fold-review`, username `FoldReview`).
- **Not verified (needs in-game check):**
  - Multiplayer: an old client against a new server, to see the handshake refusal.
  - F-7's feel in real chat.
  - The small VFX listed above.
- **Known issues (pre-existing, not fixed here):**
  - **Flaky GameTests.** The pre-step-4 commit `2d96be3a5`, run three times, failed `circussuccessionrunnercompletesatdegreefour`, `ferricrampartletsfriendlyshotsthrough`, `magnetismcrusheswhatitpulledwhenthepillarexpires` and `bloodcloudkeepsitscastmastery` in run 1, passed all tests in run 2, and failed `loadedduplicatevespersleaveoneownedboss` in run 3. During this step `completeexitcleansownedfixtureandoutput` and `mandatoryjigsawwingsassemble` also failed intermittently, then passed on rerun. A later step should stabilise them; they need their own investigation.
  - **Two payloads now arrive separately.** `BloodInjectionEvents` used to send its two sync payloads as one bundle; they now go as two packets in the same order.
- **Handoff notes for step 5 (Phase 3, player-state plumbing):**
  - Add every new payload through `PacketHandler`. Bump `PROTOCOL_VERSION` and rewrite `payload-registry.txt` whenever the set, a direction or a byte layout changes.
  - Use `ServerboundGuards` for serverbound checks, or add an exemption with a reason.
  - Send only through the facade.
  - For world-dependent fixtures, `NetworkDefectGameTests.connectedPlayer` shows how to capture every outgoing packet without adding the player to the level. A live fixture player makes parallel GameTests flaky.
