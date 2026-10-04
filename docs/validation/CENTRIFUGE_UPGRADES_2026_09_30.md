# Centrifuge upgrades validation - 2026-09-30

Implemented in the current checkout using the shared station catalog, personal progress, teacher claims, Creative exception, and Cardinal rites. Alembic and Centrifuge kits now open at D4/D6 through the Alchemist. Existing unrelated checkout changes were preserved.

## Installed behavior

| Stage | Spin time | Ordinary primary quantity | Powder per ordinary vial | Secondary output |
|---|---|---|---|---|
| Base | 200 ticks / 10 seconds | Existing health-based roll | 50% chance of one | None |
| Calibrated Rotor, D4 | 150 ticks / 7.5 seconds | +25% on average, fractional rounding | 75% chance of one | None |
| Fractionating Rotor, D6 | 100 ticks / 5 seconds | +50% on average, fractional rounding | One guaranteed | 50% chance of one different native fraction |

Quantity is bounded by the item's stack limit. Tier-two values replace tier-one values. Balance, empty vial returns, 250 mL operation income, and sample restrictions remain. Consecrated Syringes yield one matching residuum and gain only the speed benefit.

D4 practice requires personal recovery of ordinary specimen output; existing First Separation completion counts. D6 additionally requires recovery of ordinary output processed at stage one or higher. Recovery from either output bank counts. Stored quantities retain their actual processing stage across reload, upgrade, stacking, and partial extraction; other players can recover the remaining results. Automation removal supplies no personal practice. Earlier gift claims are optional.

Rites seat the machine two blocks behind the Focus on a Greater floor. Steady Separation consumes the kit, Iron Ingot, Chain, Sanguine Glass, Ferric Binder, and Neurotic Enzyme, with four anchors and two 50 mL circuits (300 mL total). Second Fraction consumes the kit, Amethyst Shard, Ghast Tear, Hematic Iron Block, Chromatic Sublimate, and Enzyme Primer, with eight anchors and three 50 mL circuits (550 mL total). Shared locking, in-place preservation, escrow recovery, and eligibility checks apply.

## Passed checks

```powershell
./gradlew.bat test --tests 'com.vincenthuto.hemomancy.common.station.*' --tests 'com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifuge*' runStationUpgradeGameTestServer runClinicalGameTestServer runAdvancedBrewingGameTestServer assemble --offline --console=plain
```

- 21 focused JVM tests, zero failures/errors.
- 32 station runtime GameTests, all required tests passed.
- 43 clinical progression GameTests, all required tests passed.
- 7 advanced-brewing GameTests, all required tests passed.
- Assembled `build/libs/hemomancy-6.0.1-neoforge.1.21.1.0.jar`.
- UTF-8 parsing and duplicate-key validation passed for all 77 changed/new JSON resource files in this checkout.
- `git diff --check` passed; Git emitted line-ending notices only.

Final combined log: `build/centrifuge-final-validation.log`. JVM XML: `build/test-results/test/`.

Runtime coverage includes both real rite upgrades for all five stations; degree/teacher and personal-use gates; optional prior claims; Creative preservation; sacred and invalid samples; secondary native fractions; powder and secondary capacity; atomic completion/retry; running and blocked-batch reload; actual processing-stage credit; partial recovery by separate server players; and rite locks.

The read-only review identified hopper insertion bypass, locked Start snapshot mutation, lost remaining-stack practice, and missing secondary recovery. New regressions reproduced all four before fixes (`build/centrifuge-review-red.log`); all pass in the final run. The reviewer found no remaining blocker on re-review.

## Full JVM suite failures

The full JVM run completed 2,368 tests with 20 failures before the final focused rerun. These failures concern existing work outside the Centrifuge/D4-D6 changes; no unrelated fixes were made. This is not a claim that the entire repository is green. Log: `build/centrifuge-validation.log`.

- `LuxUmbraCombatContractTest > existingCombatAndTendrilContractsRemainValid()`
- `BloodFillPixelsTest > upperFrameShoulderDoesNotCoverFillWithSilverPixels()`
- `BloodFillPixelsTest > fullFillTopMatchesStaticVesselTextures()`
- `GourdHudSpriteResourceTest > shellsLeaveTheBloodWindowOpenAndVariantsRemainDistinct()`
- `CraftingVesselShapeTest > ghastlyAlembicHasTwoBlockTallShape()`
- `MyelinBorerBoringSourceTest > mendingOnlyFillsGenuineGaps()`
- `PhlegethonticBombardierResourceTest > entityHasAuthoredClientResourcesAndFerventClassification()`
- `BloodProfileMigrationTest > shipsExactlyTheApprovedRestrictions()`
- `BodyIdiomWiringSourceTest > bodyIdiomsAreWiredThroughCastingDamageHudAndMemoryWeaving()`
- `HematicCommandWiringSourceTest > rebukeAndImpressmentAreWiredThroughProgressionAndMemoryWeaving()`
- `LegacyMainTestAdapterTest > legacyMainTests() > com.vincenthuto.hemomancy.common.block.WaterloggableDecorativeBlocksTest`
- `LegacyMainTestAdapterTest > legacyMainTests() > com.vincenthuto.hemomancy.common.entity.mob.monster.TendencyMobSpawnRulesSourceTest`
- `LegacyMainTestAdapterTest > legacyMainTests() > com.vincenthuto.hemomancy.common.init.CreativeTabTechnicalBlockGuardTest`
- `LegacyMainTestAdapterTest > legacyMainTests() > com.vincenthuto.hemomancy.common.init.RegistryCreativeOrderSourceTest`
- `LegacyMainTestAdapterTest > legacyMainTests() > com.vincenthuto.hemomancy.common.item.harbinger.memories.ConjureLivingStaffMemoryResourceTest`
- `LegacyMainTestAdapterTest > legacyMainTests() > com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingLumenlaceRenameResourceTest`
- `LegacyMainTestAdapterTest > legacyMainTests() > com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffArmamentSourceTest`
- `LegacyMainTestAdapterTest > legacyMainTests() > com.vincenthuto.hemomancy.common.manipulation.LuxTenebrisCombatStyleSourceTest`
- `LegacyMainTestAdapterTest > legacyMainTests() > com.vincenthuto.hemomancy.common.manipulation.ManipulationGapCombatStyleSourceTest`
- `LegacyMainTestAdapterTest > legacyMainTests() > com.vincenthuto.hemomancy.common.mission.HarbingerHermitRoadMissionSourceTest`

## Acceptance limits

GameTests prefill ritual anchor/seal state and accelerate procession ticks. They establish server contracts, not natural ceremony timing, survival resource availability, or a session with two connected clients.

The isolated review client launched through `runScriptoriumClient` and loaded resources. The supported computer-use capture failed first with `FrameArrived timed out: timed out waiting on channel`; refreshing the window and retrying failed with `window capture timed out: timed out waiting on channel`. No live centrifuge GUI, tooltips, or animation inspection is claimed. The launched review client was stopped after the failed retry.

Detailed stage machine models remain deferred as specified by the plan. The Governor kit uses the previously authored native governor sprite; the Fractionating Rotor kit currently references existing Condenser artwork. The original placed centrifuge model remains, with tier-aware spinning speed and the new secondary output bank in its menu.

## Model follow-up

The two tier bodies were subsequently authored and integrated in this chat. See [Centrifuge model validation](CENTRIFUGE_MODELS_2026_09_30.md) for editable sources, the textured preview, fresh checks, and the live-client limitation. The earlier deferred-model note describes the gameplay implementation at its original handoff.
