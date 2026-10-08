# Axonal Transduction validation (2026-10-07)

Implemented in the existing checkout. Unrelated changes were preserved; no staging, commit, worktree, push or merge was performed.

## Delivered behavior

Axonal Transduction is a PERFECTUS (Degree 6) Ductilis HEAD passive requiring 65 alignment. The Somatic Loom weaves its Memory from a blank Hematic Memory, one Naeglerophaeon Ganglion, four Neurotic enzymes and 600 mL blood. The boss's ganglion drop remains unchanged. Entry costs 150 mL before shared cost modifiers; travel has no upkeep or duration limit.

Node contact enters the connected fiber/bundle network. Forward travels toward the camera view at 32 blocks/s; backward travels away from the view; release stops; view direction selects forks; sneak bypasses entry or travels at 6 blocks/s. Bends and isolated diagonals work without skipping intervening cells. Each crossed edge is validated against loaded blocks. Another node reforms the player in safe nearby space; blocked exits permit backtracking. Entry recovery and saved exit-contact protection cover interruptions and reconnects, with a safe world-spawn fallback when the saved entry is unavailable.

Signal form has no physical collision, targeting, ordinary damage, attacks, casting, inventory manipulation, drops or automatic pickups. The server owns position and keeps vanilla connection bookkeeping and normal chunk streaming aligned. The client forces first person temporarily and restores the previous camera. A separate local mask draws dull white fibers and bright yellow nodes through terrain within sixteen blocks, over a red/black world grade; HUD rendering remains clear.

Old Synaptic Step stacks remain registered but inert. Their old craft recipe and creative entry are removed; a shapeless salvage recipe recovers one ganglion. Memory art, skill-tree placement, item inquiries, the existing Neurotic Liber page, mechanics/lore references and the Cortical Drift guide were updated.

## Initial feature verification

| Check | Result | Evidence |
| --- | --- | --- |
| Focused JVM path/acquisition tests | 12 passed: 9 path and 3 resource cases | `build/axonal-connection-green.log`; final full-suite XML also reports zero failures for both classes |
| Focused mod-loaded server tests | All 27 required passed: 15 Axonal and 12 existing Naeglerophaeon cases | `build/axonal-release-validation.log` |
| Native single-player input/rendering | Passed; captures inspected | `build/axonal-client-acceptance.log` |
| Full JVM suite | 2,487 cases, 8 failures outside this feature | `build/axonal-release-validation.log` |
| Combined server GameTests | 783 cases, 5 failures outside this feature | `build/axonal-release-validation.log` |
| Packaged resource checker | Blocked by 19 existing paths containing spaces under `textures/item/old/sanguine formation/` | `build/axonal-release-validation.log` |
| Build with tests excluded | Passed | `build/axonal-build-final.log` |

Final commands:

```powershell
.\gradlew.bat runNaeglerophaeonGameTestServer test alphaCheck --continue --console=plain
.\gradlew.bat build -x test --console=plain
.\gradlew.bat runAxonalReviewClient --console=plain
```

The native fixture uses a disposable `build/axonal-client` world with supplied progression and a constructed buried route. Real client movement reached x=35.7, then reversed to x=24.5. Passive removal returned the player to x=0.5/y=103, restored THIRD_PERSON_BACK, and left server noPhysics/noGravity false. Captures:

- [Straight buried route](../../build/axonal-client/screenshots/axonal-buried-route.png)
- [Buried branch and node](../../build/axonal-client/screenshots/axonal-reversed-route.png)
- [Reformed player](../../build/axonal-client/screenshots/axonal-reformed.png)

The recorded client run opened `AxonalReview20261007` through an operator inbox JSON command: `{"op":"open","name":"AxonalReview20261007"}`. A fresh disposable directory instead uses `{"op":"create","name":"AxonalReview20261007","seed":4242}`. The client launcher alone waits for that operator action.

Regression tests first reproduced and then passed recovery recapture, missing return dimension, retained mob targeting, physical projectile targeting, item drops/pickups, dynamic diagonal shortcuts, piston displacement, reconnect recapture and vanilla connection-tick position rollback. The last issue required updating vanilla's saved position after server-owned movement, rather than repeatedly teleporting the traveler. A fresh read-only reviewer identified the targeting, action, edge, camera and lifecycle gaps; all Important findings were addressed. Native review also verified camera alignment and node visibility over overlapping fibers.

## Remaining repository failures

The eight JVM failures are:

- `PhlegethonticBombardierResourceTest.naturalSpawnIsBasinOnlyAndLootIsExplicitlyEmpty`: existing loot entry conflicts with the empty-loot assertion.
- `BloodProfileMigrationTest.preservesEveryShippedMembership`: Hemojelly's aquatic membership differs from the fixture.
- `ClientModelTextureCoverageTest.inventoryAndCrossbowModelsResolveTheirLocalTextures`: missing Covenant Mantle and Silent Archon item textures.
- `EnglishLocalizationEncodingTest.playerFacingTextDoesNotContainMojibake`: existing malformed `hemomancy.acolyte.enlightened.line1` text.
- `GameTestHarnessSourceContractTest.isolatedJourneyClientRunAndOperatorGuideRemainAvailable` and `GameTestHarnessSourceContractTest.devOnlyHarnessProvidesCommandsAndEarlyHarbingerScenarios`: malformed source encoding during UTF-8 reads.
- Legacy `MorphlingLumenlaceRenameResourceTest`: an existing historical Orchestrator report contains Foxfire.
- Legacy `ArmorPreparationResourceTest`: the existing chromatophore display name differs from its assertion.

The five combined GameTest failures are `fullCorpusBindsOnDedicatedServer`, `blockedCentrifugeCompletionCannotProduceFreeBloodOrPowder`, `cowAndGoatBatchesConserveContainersThroughReload`, `hemojellyFloatsAtSurfaceAndFeedsOnSmallFish`, and `nativeSpawnerProducesPyrosomesWhileSquidCapacityIsFull`. The manipulation roster/passive-set counts and dormant Synaptic Step contracts now pass with the new behavior.

Full alpha remains red because of these failures and the legacy packaged paths. This is feature validation, not a clean release certification. Loaded fixtures cover server handlers and NBT/lifecycle callbacks; native acceptance covers single-player input and rendering. A separate multiplayer-client session and Survival-earned progression were not run.

## Camera-relative input repair

The initial path stored W/S as a persistent travel direction, so turning the camera could select a fork but could not reverse an already selected edge. Travel now derives its requested direction from the current camera yaw and pitch on every server tick: forward moves toward the view along the cable, and backward moves away. Turning around while holding forward reverses within an edge, at a cell center, or at a blocked node. Incoming fibers participate in fork selection; unambiguous bends still follow automatically, and perpendicular floating-point noise does not flip direction.

Six new path regressions failed before the repair, covering camera reversal, backward input after turning, vertical/diagonal runs, blocked nodes, and refusing an initial connection opposite the requested view. Two mod-loaded regressions also failed with actual vanilla rotation packets. All now pass.

| Check | Result | Evidence |
| --- | --- | --- |
| Focused JVM tests | 18 passed: 15 path and 3 acquisition/resource cases | `build/axonal-camera-green.log`; full-suite XML confirms zero failures in both classes |
| Focused mod-loaded server tests | All 29 required passed: 17 Axonal and 12 existing cases | `build/axonal-camera-green.log` |
| Native camera/input regression | Passed with matching client and server positions | `build/axonal-camera-client.log`, `build/axonal-camera-native-results.json` |
| Full JVM suite | 2,493 cases, the same 8 failures listed above | `build/axonal-camera-full-test.log` |
| Build with tests excluded and GameTest compilation | Passed | `build/axonal-camera-build.log` |

Native travel first reached x=35.7 facing east (yaw -90). Turning west (yaw 90) and holding forward for eight ticks returned to x=22.9. Holding backward for five ticks with the same west-facing camera then moved east to x=30.9. All three samples remained in signal form. Passive removal restored x=0.5/y=103, THIRD_PERSON_BACK, and server noPhysics/noGravity false. The captured west-facing view was inspected.

- [Forward while looking back](../../build/axonal-client/screenshots/axonal-camera-forward.png)
- [Backward with the same camera direction](../../build/axonal-client/screenshots/axonal-camera-backward.png)
- [Reformed after the camera test](../../build/axonal-client/screenshots/axonal-camera-reformed.png)

Commands for this repair:

```powershell
.\gradlew.bat test --tests '*AxonalTravelPathTest' --tests '*NaeglerophaeonResourcesTest' runNaeglerophaeonGameTestServer --console=plain
.\gradlew.bat test --console=plain
.\gradlew.bat build -x test compileGameTestJava --console=plain
.\gradlew.bat runAxonalReviewClient --console=plain
```

The native run used `008-camera-open.json` with the same disposable-world open command described above. The tooltip, Liber page, mechanics reference and Cortical Drift guide now describe the camera-relative controls. The combined server suite and `alphaCheck` were not repeated for this input repair.
