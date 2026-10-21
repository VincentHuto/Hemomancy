# Hemomancy Quick Wins

Status: **proposal**, 2026-10-21.

These are high-value changes with limited regression risk. Each one is
behaviour-preserving unless it is marked **APPROVAL**. Each fits in a small, separately
reviewable commit.

Finding IDs refer to [ARCHITECTURE_AUDIT](ARCHITECTURE_AUDIT.md). The phase column shows
where each win sits in the [CONSOLIDATION_ROADMAP](CONSOLIDATION_ROADMAP.md).

**Precondition for all of them:** the JUnit baseline is green (roadmap Phase 0).
Several items delete or move text that source-text tests read. Update those tests in the
same commit.

Column key:
- **Effort:** S is at most half a day; M is one to two days.
- **Risk:** behaviour or compatibility risk.
- **Phase:** roadmap phase the win belongs to.

| # | Win | Benefit | Affected files | Effort | Risk | Phase |
|---|---|---|---|---|---|---|
| 1 | **Fix the 20 failing JUnit tests** | An honest baseline. `alphaCheck` can pass again. | `en_us.json`, `docs/TESTING.md`, 6 model JSONs or their tests, `LegacyMainTestSuiteContractTest`, a few resource tests | M | Low. Model regeneration needs a visual check. | 0 |
| 2 | **Run CI on `neo-1.21.1` pushes** | Today the working branch is never checked automatically. | `.github/workflows/alpha-check.yml` | S | None | 0 |
| 3 | **Shared test support helpers** (`SourceAssertions`, `ResourceReader`) | Stops the 228th private `assertContains` copy. Later phases use them. | new `src/test/.../support/` | S | None | 0 |
| 4 | **Delete the 54 unused Vesper animation clips** | About 2,750 lines removed from a 3,003-line file. | `client/model/entity/boss/endgame/VesperTheEveningStarAnimations.java` | S | None. Keep IDLE, DEFEAT, HIT and REMOVE_HOOD. | 1 |
| 5 | **Delete `RenderBloodLaser` + `RenderBloodLaserEvent`** | Removes a per-frame loop over every player whose draw body is empty. | the two classes, plus the registration | S | None | 1 |
| 6 | **Delete the empty and unreferenced classes and members** from the SAFE list | Fewer false trails for readers and agents. | about 25 classes, plus `Hemomancy.instance` / `forcesLoaded` | S | None | 1 |
| 7 | **Delete unused render types and the two unused shaders** (`DISTORTED_TEXTURE`, `SCAR_GLOW`) | Shorter shader reload. Less render-type surface. | `RenderTypeInit`, `ShaderInit`, `RadiantPortalRendertype`, the shader JSON and GLSL | S | Low. Check a resource reload. | 1 |
| 8 | **Fix the jar packaging excludes** (`.cache`, `/old/`, reference PNG) | A smaller jar. No datagen hashes shipped. | `build.gradle:20-39`, move the archives out of `src/main/resources` | S | None | 1 |
| 9 | **Set a real network `PROTOCOL_VERSION`** | Mismatched client and server builds are refused cleanly at handshake. | `PacketHandler.java:77` | S | Low | 2 |
| 10 | **Register payloads in their real direction** | Closes the client-code-on-server path (F-2). Removes the guards that existed only for the wrong direction. | `PacketHandler.java` and about 32 payloads | S–M | Low | 2 |
| 11 | **APPROVAL: reject non-finite blood amounts in serverbound payloads** | Closes the NaN bloodline-pool exploit (F-1) and NaN pool settings (F-8). | `PacketLumpDonate`, `PacketUpdatePoolSettings`, `GroundBloodDrawPacket` | S | Low | 2 |
| 12 | **Delete the 18 never-sent payloads and `TeleportToVeinPacket` with its unreachable screen** | About 20 classes gone. A clearer packet inventory. | `common/network/**`, `RadialChooseVeinScreen`, the matching source tests | S | Low, if done together with #9 | 2 |
| 13 | **Add `muscleMemory()` and `dialogueKnowledge()` accessors** to `HemoCapabilityAccess` | Removes 28 direct `getData` bypasses. The AGENTS.md access rule becomes true. | `HemoCapabilityAccess`, about 15 caller files | S | None | 3 |
| 14 | **Register every session map with `TransientServerStateEvents`, with logout removal** | Closes about 11 never-cleared maps. One place to look. | `TransientServerStateEvents`, about 30 classes | M | None | 3 |
| 15 | **`AttributeModifiers.sync`, which does nothing when the value is unchanged** | Stops the Elytra modifier storm, about 10 attribute packets per second per wearer (P-1), and the scar-synergy churn. Replaces 9 hand copies. | new helper, `HarbingerEquipmentEntityEventHandler`, scar synergy and the other 7 sites | S–M | None. Same values, fewer packets. | 4 |
| 16 | **Use `getExistingData` / `hasData` in `SchoolCombatEvents.tick`** | Stops allocating the combat attachment on every mob, and stops saving a `school_combat` tag on every mob (P-2). | `SchoolCombatEvents.java:266` | S | Low. Check that every caller tolerates absence. | 4 |
| 17 | **Look up `CardinalRiteRecipe.getRiteByLocation` with `RecipeManager.byKey`** | Replaces a stream over all 91 rites with a map lookup. Up to 32 call sites per tick (P-7). | `CardinalRiteRecipe` | S | None | 4 |
| 18 | **One WIP creative-tab list** that drives both acceptance and exclusion | Removes 42 duplicated item references from `Hemomancy.java` (R-2). Probably fixes the Dictation Table leak (F-22, **APPROVAL**). | `Hemomancy.java`, `BlockInit`, `HemomancyCreativeTabsSourceTest` | S | Low. Check tab order. | 7 |
| 19 | **Remove `bus = MOD` from 6 subscribers** and normalise the `Dist.CLIENT` spelling | One subscription style. The argument is ignored in NeoForge 21.1 anyway. | 6 + 44 classes | S | None | 7 |
| 20 | **Merge the identical worldgen replaceable-block setters** into `FeatureLogic` | Two exact duplicates removed. A dead constant gets used (W-2). | `SporeNexusTower`, `TermiteMound`, `DeepOceanVent`, `ErythrocoralReef`, `FeatureLogic` | S | None. **Do not touch the noise helpers.** | 4 |

## Suggested order

1. **Start here:** #1, #2 and #3, in one PR, Phase 0.
2. **Then:** #4–#8, in one PR, Phase 1.
3. **Then:** #9–#12, in one PR, Phase 2. #11 is its own commit, for approval.
4. **Independently after that:** #13–#17 and #20, each as its own small PR.
5. **With Phase 7:** #18–#19.

## What these do not include, and why

Some valuable work is left out of this list because it needs more than a quick change:
- **Fixes that change gameplay.** Damage-stage ordering, scar upkeep and AoE
  attribution all change gameplay, so they need a design decision.
- **Save-critical moves.** Moving state between attachments and raw NBT needs fixture
  tests and a sunset plan.
- **Visual-sensitive helper merges.** The quad encoder, tube builders and shader noise
  need review-client comparison.
- **Large class splits.** `ClientEvents`, `HemoCommand`, `DialogueEventHandler` and the
  rite events are all heavily pinned by source-text tests.
