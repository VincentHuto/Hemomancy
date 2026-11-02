# Area 6: remaining JVM failures

7 September 2026 · [Acceptance report](06-acceptance-results.md)

`test` reports **1,775 executions: 1,723 passed, 52 failed** after the Area 6 changes. The initial run had 66 failures. These are not 52 confirmed production defects: many source tests refer to moved files or old literal layouts, while other assertions still need investigation. JUnit and the legacy-main adapter may exercise related checks separately. No failures were suppressed or excluded.

The dedicated server run is separate: **263/263 required GameTests pass**. The overall JVM/alpha gate remains red. Existing reports' earlier totals are historical snapshots.

| Test | Reported failure |
|---|---|
| `com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.ScarTreeLayoutTest.builtInAuthoredBranchesUseTheCompactedTierRings()` | org.opentest4j.AssertionFailedError: expected: <Point[x=280, y=480]> but was: <Point[x=280, y=490]> |
| `com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperCombatImplementationSourceTest.vulnerableAnchorStateIsSynchronizedLockedRenderedAndOnlyFeedbacksAcceptedHits()` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\client\render\tile\functional\SanguineConduitBlockRenderer.java |
| `com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSickleFeatureSourceTest.inventorySickleIsLiftedAndTurnedInward()` | org.opentest4j.AssertionFailedError: expected: <true> but was: <false> |
| `com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSickleFeatureSourceTest.allHeldSickleViewsUseTheSharedHandGripRotation()` | org.opentest4j.AssertionFailedError: expected: <true> but was: <false> |
| `com.vincenthuto.hemomancy.common.resource.WarpChairAssetTest.blockEntityRendererUsesTheBakedJsonModel()` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\client\render\tile\functional\WarpChairRenderer.java |
| `com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeAuxiliaryOutputSourceTest.sampledVialsProduceHematicIronPowderAsTheAuxiliaryOutput()` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\common\tile\crafting\VialCentrifugeBlockEntity.java |
| `com.vincenthuto.hemomancy.client.render.item.QliphothSeedItemRendererSourceTest` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\client\render\tile\functional\CardinalFocusRenderer.java |
| `com.vincenthuto.hemomancy.client.render.item.harbinger.LivingArmamentRenderPlacementSourceTest` | java.lang.AssertionError: torch third-person render lifts to mouth height missing: THIRD_PERSON_TORCH_LIFT = 0.14D |
| `com.vincenthuto.hemomancy.client.screen.MycelialLanternScreenPerformanceResourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\common\tile\crafting\MycelialLanternBlockEntity.java |
| `com.vincenthuto.hemomancy.common.block.LivingWorkshopResourceTest` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\common\block\harbinger\functional\GourdvineTapBlock.java |
| `com.vincenthuto.hemomancy.common.block.ModelFirstTileRendererResourceTest` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\common\block\harbinger\functional\MycelialLanternBlock.java |
| `com.vincenthuto.hemomancy.common.block.ScryingPodiumShaderResourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\client\render\tile\functional\ScryingPodiumRenderer.java |
| `com.vincenthuto.hemomancy.common.block.WaterloggableDecorativeBlocksTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\common\block\harbinger\SuspendedBloodCrystalBlock.java |
| `com.vincenthuto.hemomancy.common.block.harbinger.BloodCrystalGrowthResourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\common\block\harbinger\BloodCrystalBlock.java |
| `com.vincenthuto.hemomancy.common.block.harbinger.CovenantThroneSpectacleSourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\common\tile\functional\CovenantThroneBlockEntity.java |
| `com.vincenthuto.hemomancy.common.block.harbinger.CustomBlockItemGuiPoseSourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\client\render\item\tile\functional\EarthenVeinItemRenderer.java |
| `com.vincenthuto.hemomancy.common.block.harbinger.FaneHeartAndStakeSourceTest` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\common\tile\functional\ConsecratedBloodwellBlockEntity.java |
| `com.vincenthuto.hemomancy.common.block.harbinger.HematicArmatureBreakDropSourceTest` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\common\tile\crafting\HematicArmatureBlockEntity.java |
| `com.vincenthuto.hemomancy.common.block.harbinger.HematicArmatureEntityModelResourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\client\render\tile\crafting\HematicArmatureRenderer.java |
| `com.vincenthuto.hemomancy.common.block.harbinger.HematicArmatureInteractionSourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\common\tile\crafting\HematicArmatureBlockEntity.java |
| `com.vincenthuto.hemomancy.common.block.harbinger.HematicArmatureUpgradeFeedbackSourceTest` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\common\tile\crafting\HematicArmatureBlockEntity.java |
| `com.vincenthuto.hemomancy.common.block.harbinger.MycelialLanternRendererLightingResourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\client\render\tile\crafting\MycelialLanternRenderer.java |
| `com.vincenthuto.hemomancy.common.block.harbinger.SaintRelicEncounterResourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\common\block\harbinger\functional\SaintSarcophagusBlock.java |
| `com.vincenthuto.hemomancy.common.block.harbinger.SanguineOmenResourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\common\block\harbinger\functional\SanguineOmenBlock.java |
| `com.vincenthuto.hemomancy.common.block.harbinger.ScarStationRendererLightingResourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\client\render\tile\crafting\ScarStationRenderer.java |
| `com.vincenthuto.hemomancy.common.block.harbinger.ScarletVanityResourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\common\tile\functional\ScarletVanityBlockEntity.java |
| `com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.RegistryBackedScarRefactorSourceTest` | java.nio.file.NoSuchFileException: <workspace>\src\main\java\com\vincenthuto\hemomancy\common\block\harbinger\BrazierBlock.java |
| `com.vincenthuto.hemomancy.common.data.gen.CircusPavilionTemplateTest` | java.lang.AssertionError: central entrance aisle is obstructed |
| `com.vincenthuto.hemomancy.common.entity.mob.monster.BloodDrunkPuppeteerBehaviorSourceTest` | java.lang.AssertionError: summoned dolls should mirror the puppeteer's target: missing LivingEntity puppeteerTarget = puppeteer.getTarget(); |
| `com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerArtificerNpcSourceTest` | java.lang.AssertionError: renderer uses dedicated Artificer texture path: missing textures/entity/harbinger_artificer/harbinger_artificer.png |
| `com.vincenthuto.hemomancy.common.entity.summon.PuppeteerSummonVisualResourceTest` | java.lang.AssertionError: VeinwingVulture model uses entity id: missing Hemomancy.rloc("veinwing_vulture") |
| `com.vincenthuto.hemomancy.common.item.harbinger.PhantasmalUmbralStepSourceTest` | java.lang.AssertionError: phantasmal bypasses light only: missing if (!phantasmalStep && !BlackVeilCovenantManager.isDarkEnough |
| `com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStage4To8SourceTest` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\common\tile\functional\MorphlingCradleBlockEntity.java |
| `com.vincenthuto.hemomancy.common.item.harbinger.tile.SpecimenJarCaptureSourceTest` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\common\item\harbinger\tile\SpecimenJarBlockItem.java |
| `com.vincenthuto.hemomancy.common.item.harbinger.tile.SpecimenJarItemRendererSourceTest` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\common\item\harbinger\tile\SpecimenJarBlockItem.java |
| `com.vincenthuto.hemomancy.common.mission.HarbingerArtificerAssignmentSourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\common\tile\crafting\HematicArmatureBlockEntity.java |
| `com.vincenthuto.hemomancy.common.mission.HarbingerArtificerFittingSourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\client\render\tile\functional\ScarletVanityRenderer.java |
| `com.vincenthuto.hemomancy.common.mission.HarbingerAssignmentLedgerPortraitSourceTest` | java.lang.AssertionError: assigner portrait texture exists: assets/hemomancy/textures/entity/harbinger_vicar/harbinger_vicar_portrait.png missing: src\main\resources\assets\hemomancy\textures\entity\harbinger_vicar\harbinger_vicar_portrait.png |
| `com.vincenthuto.hemomancy.common.mission.HarbingerAssignmentReturnReadySourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\common\tile\crafting\SomaticLoomBlockEntity.java |
| `com.vincenthuto.hemomancy.common.mission.HarbingerHermitRoadMissionSourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\common\tile\crafting\SomaticLoomBlockEntity.java |
| `com.vincenthuto.hemomancy.common.mission.VeinMasonMissionSourceTest` | java.lang.AssertionError: anchorite renderer uses existing texture: missing textures/entity/harbinger_cicatrix_anchorite/harbinger_cicatrix_anchorite.png |
| `com.vincenthuto.hemomancy.common.resource.UtilityPolishResourceTest` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\common\block\shared\OssuaryClockBlock.java |
| `com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerIntegrationSourceTest` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\common\tile\crafting\PuppeteersSpindleBlockEntity.java |
| `com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonRulesTest` | java.lang.AssertionError: six summon shapes: expected 6 but got 7 |
| `com.vincenthuto.hemomancy.common.tile.BloodReservoirContractSourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\common\block\shared\BlockBloodInteractions.java |
| `com.vincenthuto.hemomancy.common.tile.IronBrazierOfferingSourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\common\tile\IronBrazierBlockEntity.java |
| `com.vincenthuto.hemomancy.common.tile.LivingWeaponGraftRiteSourceTest` | java.lang.AssertionError: missing src/main/java/com/vincenthuto/hemomancy/common/tile/IronBrazierBlockEntity.java |
| `com.vincenthuto.hemomancy.common.unstained.UnstainedProgressionRepairSourceTest` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\common\tile\crafting\StillwaterCondenserBlockEntity.java |
| `com.vincenthuto.hemomancy.common.util.CrimsonFireVisualSourceTest` | java.lang.AssertionError: missing src\main\java\com\vincenthuto\hemomancy\common\util\CrimsonFireHelper.java |
| `com.vincenthuto.hemomancy.common.util.SpecimenJarDataRulesTest` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\common\util\SpecimenJarData.java |
| `com.vincenthuto.hemomancy.common.world.fold.NonEuclideanHallwayPlacementSourceTest` | java.nio.file.NoSuchFileException: src\main\java\com\vincenthuto\hemomancy\common\block\harbinger\functional\NonEuclideanHallwayBlock.java |
| `com.vincenthuto.hemomancy.testing.LegacyMainTestSuiteContractTest.discoversEveryExistingLegacyMainTest()` | org.opentest4j.AssertionFailedError: expected: <359> but was: <360> |
