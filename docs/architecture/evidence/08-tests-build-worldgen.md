# Hemomancy audit 08: tests, build, worldgen and data pipelines

**Scope:** read-only audit; no repository files were modified. Paths are repo-relative; `J/` means `src/main/java/com/vincenthuto/hemomancy/`.

**Labels:**
- **[V]** Verified: by command output or by reading the file.
- **[L]** Likely.
- **[H]** Hypothesis.

## Summary

- The JUnit suite fails on a clean HEAD: 20 of 3,908 tests fail. Every failure is resource, source or doc drift, or text encoding; none is a logic regression.
- About 31% of test files assert on Java source text.
- The suite has no shared test helpers: 228 private `assertContains` copies and 225 private `read(` copies.
- Some tests depend on the untracked `tools/` tree.
- The build has 58 Gradle run configurations, plus about 38 init-script launchers that live outside version control or in `build/`.
- Worldgen has three live jigsaw structures whose templates were never committed.

---

## 1. Test inventory

### 1.1 `src/test/java` [V]

1,242 `.java` files and 92,594 lines. `src/test/resources` holds 6 JSON files and `echo_cast_pending.txt`.

**Two test runners coexist:**
- **JUnit 5 classes:** 897 files, one per XML result in `build/test-results/test`.
- **Legacy `public static void main` tests:** 345 files and 32,397 lines with no `@Test` at all. They run only through the reflective adapter `testing/LegacyMainTestAdapterTest.java` (75 lines), a `@TestFactory` that walks `src/test/java` and invokes each `main`.
  - Some JUnit classes also call `main(new String[0])` from a `@Test`, for example `common/command/ChamberThemeCommandSourceTest`. The adapter therefore discovers 353 classes.
  - `LegacyMainTestSuiteContractTest` hard-codes the count at 355, so the count itself is a failing test.

**Classification by heuristic** (whether the file reads source, resource, doc or tool paths; a file can fall in several):

| Style | Files | Lines |
|---|---|---|
| No file I/O: pure logic tests against production classes (747 files; 258 test files import `com.vincenthuto.hemomancy.*`) | 747 | 47,764 |
| **Reads Java source text** (`src/main/java/...` or `*.java"`) | **383** | 36,741 |
| Reads JSON or resources (`src/main/resources`, `assets/hemomancy`, `data/hemomancy`) | 296 | 30,980 |
| Reads `docs/`, `wiki/` or `AGENTS.md` | 45 | 7,608 |
| Reads `tools/` (gitignored) | 8 | — |
| Reads `build.gradle` or `game-tests.gradle` | 2 | — |

Combinations among those files:
- source only: 184
- source + resource: 149
- resource only: 109
- source + resource + doc: 29
- source + doc: 11

**Naming does not match style.** 244 files are named `*SourceTest`, 113 `*ResourceTest`, 18 `*DataTest` and 5 `*Contract*`. The rest are mixed.

The `*DataTest` family checks rite and craft rosters against resource JSON: `CardinalRitePurposeDataTest`, `WorkingCraftRosterDataTest`, `RecipeMapPurposeGroupDataTest`, `SpecialFloorDataTest`.

**Registry bootstrap is almost absent.** Only `common/item/harbinger/tool/SporiticThuribleRulesTest.java:24` bootstraps Minecraft, and it does so reflectively: `Class.forName("net.minecraft.server.Bootstrap").getMethod("bootStrap")`.
- The test classpath includes the merged NeoForge jar (`build.gradle:476-478`), yet there is no shared bootstrap fixture.
- Mockito is not used.

### 1.2 Source-text tests (refactor risk) [V]

There are **383 test files** that read Java source text. Between them they contain:
- 5,626 `assertContains(` calls
- 591 `assertNotContains(` calls
- 2,213 raw `.contains(` calls
- 55 `Pattern.compile` calls

They reference about 1,200 distinct `*.java` path strings.

Most-asserted production files (test files that mention the file / its line count):
- `client/event/ClientEvents.java`: 62 / 1,495 lines
- `common/init/ItemInit.java`: 82 / 1,260
- `common/network/PacketHandler.java`: 28 / 499
- `EntityInit`, `BlockInit`, `DialogueEventHandler` (1,561), `HemoRenderTypes` (1,625), `ShaderInit` and `HarbingerCardinalRiteEvents` are also heavily asserted.

**Examples:**
- `client/render/entity/ConstrictorCordRendererSourceTest.java` (24 lines) asserts that `ClientEvents.java` contains the literal `event.registerEntityRenderer(EntityInit.constrictor_cord_projectile.get(), ThrownItemRenderer::new)`.
- `common/command/ChamberThemeCommandSourceTest.java` counts parenthesis depth in `HemoCommand.java` to prove `size` is a sibling of `theme`.
- `common/tile/LivingWeaponGraftRiteSourceTest` pins an exact particle-call argument list.
- The largest are `client/render/world/ApotheosFloorFunnelSourceTest.java` (1,094 lines) and `ChamberOfWillEffectsSourceTest.java` (945).

**Risk:**
- Any extraction, rename, reformat or split of `ClientEvents`, `ItemInit`, `PacketHandler` or `HemoCommand` will break tens of tests with no behaviour change. Two of today's 20 failures are of exactly this type.
- These tests lock in file layout, not behaviour.
- **Before any structural refactor,** classify these 383 tests: either rewrite them as behavioural or registry assertions, or delete the purely textual ones. Otherwise the refactor will be blocked or the tests will be bulk-edited blindly.

### 1.3 Helper duplication [V]

There is **no shared test-support package.** `testing/` holds only the legacy adapter, the count contract and `GameTestHarnessSourceContractTest` (456 lines). No class is named `*Support`, `*Helper`, `*Fixture` or `*Util`, and no test base class exists.

Each file carries its own private helpers:

| Helper | Copies |
|---|---|
| `private static String read(` | 225 |
| `static void assertContains(` | 228 |
| `assertNotContains(` | 76 |
| `source(` | 14 |
| `readSource(` | 11 |
| `readResource(` | 9 |
| `JsonObject read(` | 9 |

The `\r\n` normalisation is re-implemented per file.

**Canonical fix:** one `testing/SourceFiles` / `ResourceJson` helper, and convert the 345 `main()` tests to `@Test`. That retires the adapter and the brittle 355 count.

**Stray Gradle init scripts in `src/test`:**
- `src/test/gametest-suite-registration.init.gradle` (45 lines) is a negative-probe harness for `verifyGameTestSuites`.
- `src/test/radial-vein-review.init.gradle` (14 lines) is a JavaExec that runs `RadialVeinBorderRenderTest` as an image renderer.

### 1.4 `src/gameTest` [V]

- 198 Java files, 57,464 lines.
- **1,268 `@GameTest` methods** and 2 `@GameTestGenerator`s (`PelagicWorldValidation`, `PhlegethonticWorldValidation`).
- 38 `.nbt` templates and 8 JSON files, including 2 validation mixin configs.

**Namespaces** (declared in `gradle/game-tests.gradle:3-37`):
- 66 classes use `@GameTestHolder(Hemomancy.MOD_ID)`; the rest spread across 32 more namespaces.
- 33 suites are declared: 20 `alpha:true` and 13 opt-in.
- That puts **999 tests in the combined alpha gate and 269 in opt-in only.**

**Shared fixtures:**
- `gametest/journey/*` (around 6,000 lines: `HemoJourneyFixtures` 1,792, `HemoJourneySnapshot` 1,004, `HarbingerJourneyAutomation` 725, …) is shared by GameTests and the in-game `/hemo test journey` commands. This is good reuse.
- Smaller shared helpers: `SuccessionTestFixtures`, `LoomWeaveTestSupport`, `GameTestWorldClock`. They are used by 24 files.
- Otherwise helpers are duplicated: 46 private `player(` factories, 11 `setup(`, 11 `near(`.

**The gameTest source set also hosts about 40 non-test client "review" drivers** (`*ClientReview`, `*VisualReview`, `GameplayCampaignDriver` and others). Together they are around 7,000 lines.

The largest files:
- `ClinicalBloodProgressionGameTests.java` (4,885 lines)
- `HarbingerJourneyFixtureGameTests.java` (2,962)
- `HarbingerRepairGameTests.java` (1,737)

### 1.5 GameTest registration and `/hemo test` [V]

There are two registration paths for GameTests:

1. `build.gradle:401-404` adds `sourceSet(sourceSets.gameTest)` to the mod, so `@GameTestHolder` classes are annotation-scanned.
2. `J/common/testing/DevTestHooks.java:24-42` (53 lines) separately calls `event.register(Class.forName(...))` for 13 hard-coded classes. 11 of those 13 already carry `@GameTestHolder`.

`DevTestHooks`' javadoc says the source set "is not part of the mod annotation scan". That contradicts `build.gradle:403`.

- **[H]** Those 11 classes may be registered twice. Confirming this needs a GameTest run.
- **Canonical approach:** keep the annotation scan; reduce `DevTestHooks` to the generator classes and the command hook.

**`/hemo test` command tree.** `HemoTestCommands` (gameTest, 377 lines) is reflectively registered via `DevTestHooks.registerCommandsIfPresent`. It provides:
- `list` / `setup` / `verify` / `run` / `run_all` / `status` over `HemoTestScenarioCatalog`: 7 scenarios, mostly degree and recipe gates already covered by GameTests.
- `journey harbinger|unstained|circus …`, which drives the same journey fixtures as `HarbingerJourneyFixtureGameTests`.

The overlap is intentional (live-world replay of the same fixtures). The 7-scenario catalog is a third mini-framework and is a candidate to fold into GameTests.

**`docs/TESTING.md`** (726 lines) is mostly a dated repair ledger, with around 60 per-feature sections, rather than a guide. **It is not valid UTF-8:** byte `0xD7` at offset 5058. That is what makes two `GameTestHarnessSourceContractTest` cases fail.

### 1.6 Wiring into Gradle and CI [V]

- **`test` task:** JUnit Platform; depends on `createMinecraftArtifacts` (`build.gradle:501-504`); also depends on `verifyGameTestSuites` (`game-tests.gradle:137-139`).
- **`alphaCheck`** (`build.gradle:587-591`) runs:
  - `test`
  - `verifyGameTestResourceLog`: runs `runGameTestServer`, then fails on a missing "required tests passed" line or any `/ERROR]` line containing "hemomancy"
  - `verifyPackagedClientResources`: checks jar path regex, PNG magic and the font licence
- **`.github/workflows/alpha-check.yml`:**
  - Runs on a self-hosted Windows runner (`hemomancy-deps`) with `clean: false`, so ignored `libs/` and `tools/` persist.
  - Runs `verify-dependency-bundle.ps1`, then `alphaCheck`, with a 30-minute timeout.
  - **It triggers on push only to `master`/`main` and on PRs. Pushes to the working branch `neo-1.21.1` do not run it.**
- **`dialogue-inquiry-validation.yml`** runs on `ubuntu-latest`. It is an inline Python validator for `dialogue_inquiry/**` and lang. It also string-scans `HarbingerAlchemistDialogueTrees.java`, so it is another source-text test that lives outside JUnit.
- **`gradle/verify-dependency-bundle.ps1`** (49 lines) pins SHA-256 for 9 jars plus a HutosLib source digest. It does **not** cover `AstralSorcery-2.0.0.3.jar` or `observerlib-1.10.3.jar`, which are `implementation` dependencies (`build.gradle:444-445`). `docs/DEPENDENCY_PROVISIONING.md` claims "all nine active local JAR inputs", which is inaccurate.

---

## 2. Baseline results [V]

| Step | Result |
|---|---|
| `./gradlew compileJava --offline` | **BUILD SUCCESSFUL**, 7 s; all tasks UP-TO-DATE. The composite `:HutosLib` build is active because `../HutosLib` exists. |
| `./gradlew test --offline` | **FAILED**, 51 s wall-clock. **3,908 tests, 20 failed, 0 errors, 0 skipped.** Summed class time is 35.2 s. Slowest: `PhlegethonticContainmentTest` 13.0 s, `LegacyMainTestAdapterTest` 8.8 s (353 dynamic tests), `EngramRecipeSweepTest` 1.9 s. No build-collision symptoms appeared. |
| GameTest | Not run, per the brief. 1,268 methods are declared, 999 of them in the alpha gate. |
| `alphaCheck` | Not run. It would fail at `test` on the current HEAD. Its cost is JUnit (~1 min) plus one dedicated GameTest server for 21 namespaces, plus the jar build and scan. **[H]** probably 10–20 min, given the CI 30-minute cap. |

**The 20 failures, grouped:**

- **Generator-provenance marker missing (6).** The model JSON no longer contains its `tools/model_export/*.py` marker. The models were rewritten after generation (by hand or by Blockbench). Affected:
  - `HematicArmatureModelSourceTest` ×2
  - `VialCentrifugeModelSourceTest` ×2
  - `MnemonicReliquaryModelSourceTest`
  - legacy `ModelFirstTileRendererResourceTest` (somatic loom)
- **Asset or model drift (4):**
  - `CardinalRitePlantedStaffModelTest`: `living_staff_worn_vow` still has a fist element.
  - `LivingSickleFeatureSourceTest`.
  - legacy `UtilityPolishResourceTest`: ossuary clock top cap.
  - `ClientModelTextureCoverageTest`: item models `covenant_mantle` and `silent_archon_*` reference missing `hemomancy:item/...` textures. **[H]** This could be a real missing-texture bug in game.
- **Data drift (3):**
  - `PhlegethonticBombardierResourceTest`: the spawn/loot expectation is 0 but the data has 1.
  - `BloodProfileMigrationTest`: `hemojelly` gained `blood_properties/aquatic`.
  - legacy `ArmorPreparationResourceTest`: lang key `item.hemomancy.cuttlefish_chromatophores` is missing.
- **Encoding (3):**
  - `EnglishLocalizationEncodingTest`: mojibake in `hemomancy.acolyte.enlightened.line1`.
  - `GameTestHarnessSourceContractTest` ×2: `docs/TESTING.md` is not UTF-8.
- **Source or doc text drift (3):**
  - legacy `VestmentOfTheFinalMoltResourceTest`: helmet/hood condition.
  - legacy `LivingWeaponGraftRiteSourceTest`: particle-call signature.
  - legacy `MorphlingLumenlaceRenameResourceTest`: the word "foxfire" in `docs/MANIPULATION_BALANCE_PLAN.md`.
- **Meta (1):** `LegacyMainTestSuiteContractTest` expects 355 classes and finds 353.

**Clean-clone hazard [V].** `.gitignore:82` ignores `/tools/`, and `git ls-files tools` returns 0 files. Six tests nevertheless `Files.read`/`exists` tools files:
- `MycophantModelSourceTest:24-26`
- `HematicArmature…:20`, `VialCentrifuge…:20`, `MnemonicReliquary…:21`
- `PuppeteerCardinalOrdealResourceTest:60`
- `DendriticSelectorRulesTest:53`

These pass only on machines that carry the untracked tree.

---

## 3. Build findings [V]

**Size:**
- `build.gradle`: 599 lines
- `gradle/game-tests.gradle`: 139
- `settings.gradle`: 17
- `gradle.properties`: 48

**Gradle run configurations: 58 in total.**
- `build.gradle:134-379` declares 25: `client`, `server`, `data`, 2 observer clients and 20 review clients.
- The review clients are: bookKit, bookKitPeer, scriptorium, alphaJourney, phlegethontic, pelagic, antecedent, axonal, naeglerophaeon, annetta, mycophant, vesper, outpouring, brazier, condenser, infusion, jei, loom, riteFloor, succession.
- `game-tests.gradle:44-78` generates 32 focused GameTest servers plus the combined `gameTestServer`. That includes `fungalGardensValidationServer`, `pelagicValidationServer` and `phlegethonticValidationServer`.

**Run configs and launch scripts:**
- The review-client blocks are copy-paste. Each differs only in directory, heap, username and a `hemomancy.*Review` system property. A `reviewClient(name, prop, …)` helper would remove about 200 lines.
- `configureEach` (`:381-394`) sets `chamberLoadedValidation` and DEBUG logging on every run.
- **23 `-P` properties** are read by the build:
  - `annettaReviewOnly`, `mycophantReviewOnly`, `vesperReviewOnly`, `outpouringReviewOnly`, `riteFloorReviewSpecial`
  - `osteophageReview`, `pelagicEcologyReview`, `pelagicReviewStartScene`, `pelagicReviewWorld`
  - `pelagicSeed`, `pelagicRun`, `pelagicOrder`, `pelagicPreset`, `pelagicExpectDisabled`, `pelagicEcology`, `pelagicPavilionLocate`, `pelagicLocateX`, `pelagicLocateZ`
  - `phlegethonticSeed`, `phlegethonticRun`, `phlegethonticOrder`
  - `chamberLoadedValidation`, `combinedGameTestDirectory`
- **The code reads about 60 `hemomancy.*Review*` system properties** that no tracked build file sets. Examples: `castingReview*`, `sarcophagusReview`, `thermalReview*`, `ferricReview*`, `luxUmbra*`. Their launchers are untracked init scripts:
  - 27 `build/*.init.gradle` files, which `gradlew clean` would delete
  - 9 in gitignored `tools/oneoff/`
  - 2 in `src/test/`

  So roughly half the review harness cannot be relaunched from a fresh clone.

**`libs/` (19 jars, ~487 MB):**
- **Used by the build:** create (`clientLocalRuntime`), buildinggadgets2, worldedit, moonlight, theoneprobe, dummmmmmy, jei, observerlib, AstralSorcery, mna, TerraBlender.
- **Unreferenced:** Demonology and thaumaturge (commented out at `:449,452`); DistantHorizons in two versions, 3.0.3 and 3.3.3 (disabled at `:436`); thebrokenscript; the geckolib jar (Maven is used instead); hutoslib-7.3.5 jar and sources (the composite build is used).
- **Compiled-code consumers:**
  - Only JEI (18 importing files) and TerraBlender (8).
  - GeckoLib, Curios and MnA are imported only by the excluded `compat/mna/**` (36 files) and `compat/curios/**` (1 file). Those two packages total 3,387 lines (`build.gradle:77-80`).
  - Moonlight, TOP, dummmmmmy, BG2, WorldEdit and Astral have 0 imports. They are dev-runtime conveniences declared as `implementation`; `localRuntime` or `runtimeOnly` would be correct.
  - `neoforge.mods.toml` comments out geckolib, mna and curios.

**Other build items:**
- **Orphan MnA mixin config:** `src/main/resources/hemomancy.mna.mixins.json` is not listed in `neoforge.mods.toml` (only `hemomancy.mixins.json` is, at `:35-36`). Its target `mixin/core/MixinHUDOverlayRenderer.java` is a 5-line empty stub "kept for the optional MnA mixin configuration".
- **`.cache` leak:** `exclude("src/generated/**/.cache")` (`build.gradle:26`) is evaluated relative to each source directory, so it never matches. The 4 datagen hash files sit in `build/resources/main/.cache/` and ship in the jar.
- **Debt-only complexity:**
  - The resource excludes at `:28-39` hide authoring archives that live inside `src/main/resources` (`* - Copy.*`, `old/`, bbmodel imports, mp3s).
  - The `doFirst` fresh-world guards (`:510-517`, `:578-585`) exist for the validation servers.
  - `verifyGameTestSuites` regex-parses gameTest sources to keep namespaces and launchers in sync (`game-tests.gradle:80-135`). It is useful, but only because launchers are generated per namespace.
- **Stale files:**
  - `gradle.properties:42-48` carries stale TODOs.
  - `build/test-path-rewrite-20260912-a/` contains a stale copy of test sources.

---

## 4. Worldgen: competing mechanisms

**Size [V]:** `common/worldgen` has 144 files and 15,531 lines.

| Subpackage | Files |
|---|---|
| root | 35 |
| feature | 33 |
| structure | 30 |
| pelagic | 16 |
| terrablender | 12 |
| arbor | 8 |
| config | 4 |
| village | 2 |
| carver | 1 |

Related init files: `BiomeInit` 475, `PlacedFeatureInit` 313, `ConfiguredFeatureInit` 288, `BaseFeatureInit` 94, `StructureInit` 87.

### 4.1 Structure mechanisms [V]

There are six distinct mechanisms:

1. **Jigsaw clones.** Thirteen custom `Structure` classes each copy the vanilla `JigsawStructure` codec (6 identical fields) and call `JigsawPlacement.addPieces`. Registered at `J/common/init/StructureInit.java:34-74`. Examples: blood_temple, unstained_church, broken_church, hermitage_remnant, masons_respite, harbinger_voyager_*, harbinger_outpost, mausoleum, crimson_lodge_annex.
   - Several add code edits in `afterPlace`: `HarbingerOutpostStructure.java:103-177` and `BrokenChurchStructure.java:147-165`.
2. **Code-composed `PoolElementStructurePiece`s from NBT:** `DynastyCastleStructure.java:60-98`.
3. **A block-by-block custom `StructurePiece`:** `structure/VagrantMindPiece.java:151-248`. Piece type `hemomancy:vagrant_mind`, NBT keys `OriginX/Y/Z` and `Seed`.
4. **Village pool injection** at `ServerAboutToStartEvent`: `village/VillageEvents.java:104-118`, using the processor registered in `VillagerInit.java:35`.
5. **Mixin into ancient-city jigsaw:** `mixin/core/MixinAntecedentCity` together with `common/antecedent/VigilPlacement.java:31-130`. This lives outside `worldgen`.
6. **Runtime `setBlock` construction by managers:** Chamber rooms (`ChamberOfWillManager.java:378-452`), the Vesper arena (`VesperOrdealManager.java:357-370`) and the Mycophant nursery. These build inside a flat void dimension.

Separately, terrain features are block-by-block `Feature` subclasses. The Fungal Gardens is a pure data dimension.

**NBT authoring pipelines compete:**
- Java datagen: `common/data/gen/CircusPavilionStructureProvider` (live) and `DynastyCastlePieces` (514 lines, dormant at `DataGeneration.java:25-28`).
- Python: `tools/worldgen/generate_harbinger_outpost.py` writes 12 NBTs.
- Python: `tools/circus/build_*.py` writes troupe NBTs and pools, and reads the datagen `circus_pavilion.nbt` as its input.

**Recommendation:**
- One abstract `HemoJigsawStructure`: shared codec builder, plus hooks for the placement predicate and `afterPlace`. Difficulty S–M.
  - Compatibility risk: StructureType IDs and codec field names must stay byte-identical. Pieces serialize as vanilla jigsaw pieces, so there is no chunk-NBT risk.
- Keep NBT/jigsaw for authored buildings and code for procedural geometry. Do not convert either way, because conversion changes output.
- Choose one NBT generator home: either the Python tools, version-controlled, or datagen.
- Delete or clearly mark the dormant Dynasty and Circus code generators.

### 4.2 Broken or never-generating structures [V]

- **Missing templates.** `crimson_lodge_annex`, `bog_body_ossuary_niche` and `sanguine_surveyor_bivouac` have live structure, structure_set (spacing 40/28/28) and non-empty biome tags. Their pools point at `hemomancy:crimson_lodge_annex` and similar, but no such `.nbt` exists, nor did one ever exist in git (introduced in `f473301a4`).
  - **[H]** The game logs missing-template errors when these attempt to place and produces empty starts.
- **Mausoleum never generates:** `tags/worldgen/biome/has_structure/mausoleum.json` has `"values": []`.
- **Dormant circus chain:** the live structure uses `crimson_troupe_pavilion/start_pool`. Still present but unused: `template_pool/circus_pavilion/start_pool.json`, the generated `circus_pavilion.nbt` and the still-registered `CircusPavilionStructureProvider`. `CircusPavilionWorldgenResourceTest` validates this unused path.

### 4.3 Duplicated helpers [V]

- **Identical replaceable-placement setters:**
  - `SporeNexusTowerFeature.java:431` and `TermiteMoundFeature.java:335` are identical.
  - `DeepOceanVentFeature.java:189` and `ErythrocoralReefFeature.java:160` are identical.
  - Variants: `CorticalArchipelagoFeature:373`, `CanopyMushroomFeature:323`, `ToothGeodeFeature:112`, `BrokenChurchStructure:163`, `ChamberOfWillManager:479`.
  - `feature/FeatureLogic.java:13` already defines `IS_REPLACEABLE_AIR` but has zero references.
- **At least 7 hash/value-noise copies:**
  - `CorticalIslandShape:131-163`
  - `MycophantNurseryLayout:127`
  - `PelagicTerrainSampler:179-210`
  - `PhlegethonticRules:15-32`
  - `PhlegethonticBasinLayout:152`
  - `FungalFloorPattern:35`
  - `VagrantMindGeometry:101-120`

**Recommendation:** merge only the exactly identical setters. **Do not unify the hashes.** Their constants and smoothing differ, so merging them would change terrain for existing seeds.

### 4.4 Managers living in `worldgen` [V]

These are encounter, travel and state systems, not worldgen. They belong in `common/encounter` or `common/dimension`.

- **`ChamberOfWillManager`** (785 lines): `SavedData` named `chamber_of_will_data`. Ticked from `Hemomancy.java:134,141-142`.
- **`MycophantEncounterManager`** (434 lines): `@EventBusSubscriber`; arena at x=8192.
- **`VesperOrdealManager`** (430 lines): arena at x=4096.
- **`VagrantMindEncounterManager`** with SavedData `hemomancy_vagrant_mind_encounters`.
- **`ChamberVisitService`** (350 lines): 18 `hemomancy:chamber_visit_*` player keys.
- **`FungalGardenTravelHelper`** (305 lines).
- **`CorticalArcManager`**.
- **Client and VFX classes are also misplaced here:** `FungalSkyBoxRenderer` (client, `@OnlyIn(CLIENT)`) and `PhlegethonticVisuals`.

**Moving them is safe if every key is preserved:**
- SavedData names.
- `ChamberOfWillManager` tags: `nextId`, `ids`, `returns`, `states`, `skyThemeOverrides`, `chamberRadiusOverrides`, `builtRadii`.
- The constants `CHAMBER_SPACING=256` and `FLOOR_Y=2`.
- The player-persisted keys `hemomancy:mycophant_*`, `hemomancy:vesper_*`, `hemomancy:fungal_*` and `hemomancy.circus_discovered`. Note the dot in the last one.
- The entity tag `HemomancyVesperEncounterPuppet`.

Difficulty M: each class has 15 or more importers.

### 4.5 Python generators [V] (from the delegated check)

- **`tools/worldgen/fungal_gardens_worldgen.py`** (505 lines) writes JSON: density functions, noise, noise_settings, the dimension, and patches to the biome features and spawners.
- **`generate_harbinger_outpost.py`** (565 lines) writes 12 gzipped NBTs with DataVersion 3955.
- Neither writes a provenance marker.
- Re-running both in a temp copy reproduced the committed outputs byte-for-byte, with the NBTs identical after decompression. The worldgen outputs are therefore **not** hand-edited. The model-export outputs, by contrast, have drifted (§2).
- **Both scripts are untracked** because `/tools/` is gitignored. This is the single largest reproducibility gap in the repo.

### 4.6 TerraBlender and duplicated bootstrap [V]

- All regions register in `BiomeInit.commonSetup` (`BiomeInit.java:434-473`): Pelagic or Erythrocoral, Phlegethontic (Nether), FungalGardens.
- `TestSurfaceRuleData` is the *production* surface rule set despite its name (`:28,38`).
- `BiomeInit.bootstrapBiomes` (`:58-70`), `PelagicBiomes.bootstrap`, `ConfiguredFeatureInit.bootstrap:103` and `PlacedFeatureInit.bootstrap:83` duplicate the JSON. Their only callers are the unreferenced `HemoFeatureUtils` and `HemoPlacementUtils`.
- JSON is authoritative. The `ResourceKey` constants are still used.

---

## 5. Data pipelines and loaders [V]

**Datagen** (`J/common/data/gen/DataGeneration.java`, 30 lines) runs four providers: blockstates, item models, en_us language, and the circus pavilion NBT.

**Never registered** (16 files, 2,182 lines in `gen/`):
- `HemoRecipeProvider` (196 lines)
- `HemoBlockTagProvider`, `HemoEntityTagProvider`, `HemoBlockLootTableProvider`, `HemoEntityLootProvider`
- `DataGenParsers` (207 lines)
- `HemoFeatureUtils`, `HemoPlacementUtils`

**Generated vs main resources:**
- `src/generated/resources` has 103 files; `src/main/resources` has 6,899.
- **46 generated files are shadowed** by main copies through `DuplicatesStrategy.EXCLUDE`: 45 spawn-egg models plus `lang/en_us.json`.
  - The generated lang has 1,252 keys, and 358 of them hold junk values such as `"Block.minecraft.air"`.
  - So the language provider's output never ships.
- **Only about 57 generated files (~0.8%) are live.** The rest are hand-authored or written by the untracked `tools/` generators:
  - `tools/model_export`: 47 `.py` files
  - `tools/circus`: 12 `build_*.py` files
  - `tools/book_kit`
  - the TypeScript skill-tree and dialogue editors, which rewrite marked Java regions

**Reload listeners** (registered at `Hemomancy.java:340-349`, except `SuccessionProfessions`):
- Nine hand-parsed Gson loaders. **None uses `Codec`.**
- Only two use the shared base `common/data/JsonResourceReloadListener` (61 lines): `CardinalRiteFloorLoader` and `IchorianSigilLoader`.
- **Six re-implement the same list/strip/read/log/publish loop on raw `SimplePreparableReloadListener`:**
  - `DiscoveryInscriptionLoader`
  - `ScriptoriumAffinities`
  - `ItemInquiryLoader` (241 lines)
  - `BloodInjectionData`
  - `BloodProfileData`
  - `ClairaudiographCatalogue`
- **Three separate raw-JSON-string sync packets** (floor, injection, profile) each re-parse on the client.
- **Three classpath reads bypass datapacks:** `VigilModules:24`, `NaeglerophaeonBlockModel:42`, `EngramTextureCache:38`.

**Canonical approach:** make `JsonResourceReloadListener<T>` take a `Codec<T>` (via `JsonOps`), with an optional generic sync payload sent on `OnDatapackSyncEvent`, then migrate the six loaders and three packets onto it. Keep the folder names unchanged. This preserves `/reload` support, which datapack registries lack.

**Books and dialogue:**
- **Hemomancy correctly reuses HutosLib** for: `BookPlaceboReloadListener` (books JSON uses only `hutoslib:*` types), `FieldNotes`, `BookKnowledge` (via `LiberKnowledge`) and `IBookPageFilter`.
- **Duplicates or parallels of HutosLib:**
  - `common/data/book/HemoBookReloadListener` (21 lines) duplicates HutosLib's listener and is never registered.
  - `PacketSyncLiberKnowledge` (181 lines) parallels HutosLib's `PacketSyncBookKnowledge`. **[H]** These could merge.
  - The `blood_structure_page` serializer is registered, but no book JSON uses it.
- **HutosLib contains no dialogue code.** The AGENTS.md claim that HutosLib owns part of the dialogue pipeline is stale.
- **Dialogue is authored five ways:**
  1. 17 `*DialogueTrees.java` files, 5,143 lines.
  2. 17 procedural `*Dialogue.java` classes, 892 lines.
  3. 353 inquiry JSON files.
  4. Lang prose.
  5. Untracked tool round-trips: the TypeScript Studio, plus older Python export/import scripts and stale JSON snapshots.

**`client/data/`** (21 files, 1,948 lines) holds synced runtime state such as `ActiveRiteClientData` (668 lines), not data definitions. A name like `client/state` would describe it accurately.

---

## 6. Dead or abandoned items

**Verified:**

| Item | Detail |
|---|---|
| `HemoBookReloadListener` | Never registered |
| 7 datagen classes and `DataGenParsers` | Never registered |
| `hemomancy.mna.mixins.json` and `MixinHUDOverlayRenderer` | Orphan config; empty stub class |
| `TestRegion1`, `TestRegion2` | Unused |
| `AbstractFungusFeature`, `FungusFeatureConfig`, `BigMushgloomFeature`, `FeatureLogic` | Unused |
| `TerraBiomeInit` | Empty class |
| `StructureInit.BIOME_MODIFIER_SERIALIZERS` | Never registered (`:31`) |
| `BIOME_REGISTER` | Empty (`Hemomancy.java:98`) |
| Biome and feature `bootstrap` methods | Only reachable from dead utilities |
| Feature types `small_fungus`, `patch_hyphae`, `infested_venous_stone_blob` | Registered (`BaseFeatureInit:23,44,47`) but no JSON uses them. Keep the IDs if any world might reference them; they are low risk. |
| `vanilla_mushrooms.json` configured feature | Unreferenced |
| Mausoleum `start_pool`, root `mausoleum.nbt`, dormant circus pool and NBT | Unreferenced |
| Mausoleum structure | Never generates (empty biome tag) |
| 3 structures | Templates missing |
| `PlaceStructurePacket.java~` | Backup file inside the source tree |
| 7 unreferenced jars in `libs/` | Demonology, thaumaturge, DistantHorizons ×2, thebrokenscript, geckolib jar, hutoslib jar and sources |
| `mna_guide`, `geo/`, `animations/` assets | Ship although their only consumers are excluded |
| Generated `.cache` | Ships in the jar |
| Generated `en_us.json` | Fully shadowed |
| 7 `@GameTest`-less `/hemo test` scenarios | **[L]** Redundant with GameTests |

**Likely:**
- **Possibly-unused NBTs** (could still be placed via `/place`): `harbinger_outpost.nbt` at the root, `broken_unstained_church.nbt`, `qliphoth_bloom.nbt`, `qliphoth_fane.nbt`, `tower/blood_tower_*.nbt`, `lowtide_ruin/*.nbt`.
- **`BloodStructurePageTemplate`** and its two renderers.
- **The superseded Python dialogue import/export scripts.**

**Hypothesis:**
- `DevTestHooks` registers 11 GameTest classes twice.
- `ClientModelTextureCoverageTest` points at a real in-game missing-texture bug for `covenant_mantle` and `silent_archon_*`.

---

## 7. Hotspot files

| File | Lines | Why it matters |
|---|---|---|
| `J/common/command/HemoCommand.java` | 1,982 | One literal tree (`:146-592`, all `requires(2)`) with 18 top-level branches: blood, bloodline, degree, morphling, qliphoth, archon, pome, skills, unstained, organs, tendency, bloodmoon, fane, chamber, will, summons, manipulations, slots. It also holds 60 static handlers at `:594-1982` and has 75 imports. Debug-only branches: qliphoth tree spawn/remove, archon, pome, bloodmoon, fane preview, chamber theme/size, will ambush, morphling stage. 9 source-text tests pin its text (`ChamberThemeCommandSourceTest`, `FanePreviewCommandSourceTest`, `WillAmbushCommandSourceTest`, …). **Split:** one `HemoSubcommand` class per branch returning a `LiteralArgumentBuilder`, keeping the root `hemo` and permission level 2. Rewrite those 9 tests first. |
| `J/client/event/ClientEvents.java` | 1,495 | Most-changed file since September (25 commits); 62 test files mention it. |
| `J/common/init/ItemInit.java` | 1,260 | 82 test files mention it; 19 commits. |
| `J/common/network/PacketHandler.java` | 499 | 28 test files mention it. |
| `HemoRenderTypes` (1,625), `DialogueEventHandler` (1,561), `EntityInit` (1,139), `BlockInit` (1,041) | — | Heavily asserted as text by source tests. |
| `J/common/worldgen/ChamberOfWillManager.java` | 785 | `SavedData` with compatibility-critical keys; 10 test files mention it. |
| `src/gameTest/.../ClinicalBloodProgressionGameTests.java` | 4,885 | Largest test file in the repo. |
| `src/test/.../ApotheosFloorFunnelSourceTest.java`, `ChamberOfWillEffectsSourceTest.java` | 1,094 / 945 | Largest source-text tests. |
| `build.gradle` | 599 | 25 copy-paste run blocks. |
| `gradle/game-tests.gradle` | 139 | 33 suites, generated launchers. |
| `docs/TESTING.md` | 726 | Ledger, invalid UTF-8. |

**Suggested order for any refactor:**
1. Make JUnit green: fix the encoding and the drift.
2. Version-control `tools/` and the review init scripts, or move the scripts into `build.gradle`.
3. Introduce shared test helpers and convert the `main()` tests.
4. Triage the 383 source-text tests.
5. Only then split `HemoCommand`, `ClientEvents` or the init classes, and relocate the worldgen managers with their keys preserved.
