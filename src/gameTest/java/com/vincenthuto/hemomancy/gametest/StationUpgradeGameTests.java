package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.StationUpgradeDialogue;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket;
import com.vincenthuto.hemomancy.common.station.StationUpgradeMigration;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameType;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites;
import com.vincenthuto.hemomancy.common.station.StationTierProperty;
import com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.station.StationUpgradeTier;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import com.vincenthuto.hemomancy.common.station.UpgradeStation;
import com.vincenthuto.hemomancy.common.station.UpgradeableStation;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.HematicArmatureBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ResonantForgeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
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
import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StationUpgradeGameTests {
    private StationUpgradeGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade")
    public static void upgradedStationsDropTheirStage(GameTestHelper helper) {
        for (Block block : List.of(BlockInit.ghastly_alembic.get(), BlockInit.resonant_forge.get(),
                BlockInit.hematic_armature.get(), BlockInit.enzymatic_scriptorium.get(), BlockInit.vial_centrifuge.get())) {
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

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade")
    public static void everyStationUpgradesOneStepThroughTheSharedInterface(GameTestHelper helper) {
        for (UpgradeStation station : UpgradeStation.values()) {
            BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
            Block block = BuiltInRegistries.BLOCK.get(station.blockId());
            helper.getLevel().setBlockAndUpdate(pos, withFacing(block.defaultBlockState()));
            var subject = (UpgradeableStation) helper.getLevel().getBlockEntity(pos);
            UUID rite = UUID.randomUUID();
            helper.assertTrue(subject.upgradeStation() == station, station + " reported the wrong station");
            subject.setRiteLocked(true);
            helper.assertTrue(!subject.completeUpgrade(2, rite), station + " skipped a tier");
            helper.assertTrue(subject.completeUpgrade(1, rite) && subject.upgradeTier() == 1
                    && subject.wasUpgradedBy(rite) && !subject.isRiteLocked(), station + " did not upgrade");
            UUID identity = subject.machineIdentity();
            CompoundTag reloaded = helper.getLevel().getBlockEntity(pos).saveWithoutMetadata(helper.getLevel().registryAccess());
            helper.assertTrue(reloaded.hasUUID("MachineIdentity") && reloaded.getUUID("MachineIdentity").equals(identity),
                    station + " does not persist its identity");
            helper.getLevel().removeBlock(pos, false);
        }
        helper.succeed();
    }

    // ---- rite helpers ----
    static ActiveCardinalRite upgradeRite(GameTestHelper helper, StationUpgradeTier tier) {
        return upgradeRite(helper, tier, UUID.randomUUID());
    }

    static ActiveCardinalRite upgradeRite(GameTestHelper helper, StationUpgradeTier tier, UUID owner) {
        BlockPos center = helper.absolutePos(new BlockPos(5, 3, 5));
        ActiveCardinalRite rite = ActiveCardinalRite.interactive(owner, center, tier.rite(), 2400,
                tier.requiredDegree(), tier.requiredDegree(), false, 0, tier.anchors());
        rite.setMatchedFloor(ResourceLocation.parse(tier.floor()), Direction.NORTH, Direction.UP);
        helper.getLevel().setBlockAndUpdate(center, BlockInit.cardinal_focus.get().defaultBlockState());
        BlockPos seat = StationUpgradeRites.seat(rite);
        if (tier.station() == UpgradeStation.ALEMBIC) AlembicFootprintGameTests.clearSpace(helper, seat);
        Block block = BuiltInRegistries.BLOCK.get(tier.station().blockId());
        BlockState state = withFacing(block.defaultBlockState()).setValue(StationTierProperty.STAGE, tier.tier() - 1);
        helper.getLevel().setBlockAndUpdate(seat, state);
        block.setPlacedBy(helper.getLevel(), seat, state, null, new ItemStack(block));
        List<ActiveCardinalRite.RiteOffering> offerings = new ArrayList<>();
        for (int i = 0; i < StationUpgradeCatalog.OFFERINGS_PER_RITE; i++) {
            BlockPos brazierPos = center.offset(i - 3, 0, -3);
            helper.getLevel().setBlockAndUpdate(brazierPos, BlockInit.iron_brazier.get().defaultBlockState());
            ItemStack offering = i == 0 ? new ItemStack(BuiltInRegistries.ITEM.get(tier.upgradeItem()))
                    : new ItemStack(Items.STICK);
            ((IronBrazierBlockEntity) helper.getLevel().getBlockEntity(brazierPos)).insertOffering(null, offering.copy());
            offerings.add(new ActiveCardinalRite.RiteOffering(brazierPos, offering, true));
        }
        rite.captureOfferingItinerary(offerings);
        return rite;
    }

    static void assertUpgradeCompletes(GameTestHelper helper, UpgradeStation station, int tierNumber) {
        StationUpgradeTier tier = StationUpgradeCatalog.get(station, tierNumber);
        ActiveCardinalRite rite = upgradeRite(helper, tier);
        var level = helper.getLevel();
        helper.assertTrue(StationUpgradeRites.prepare(level, rite), tier.ritePath() + " preparation failed");
        UpgradeableStation subject = StationUpgradeRites.station(level, rite);
        helper.assertTrue(subject.isRiteLocked(), tier.ritePath() + " did not lock");
        for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, 50);
        helper.assertTrue(rite.enterInscription() && rite.sealAltar(false)
                && rite.getPhase() == CardinalRitePhase.STATION_PROJECTION, tier.ritePath() + " did not enter projection");
        helper.assertTrue(!rite.fillUpgradeCircuit(1, tier.bloodPerCircuit()), "accepted an out-of-order circuit");
        for (int circuit = 0; circuit < tier.circuits(); circuit++)
            helper.assertTrue(rite.fillUpgradeCircuit(circuit, tier.bloodPerCircuit()),
                    tier.ritePath() + " rejected circuit " + circuit);
        rite.markComplete();
        helper.assertTrue(StationUpgradeRites.complete(level, rite)
                && subject.upgradeTier() == tierNumber && !subject.isRiteLocked()
                && rite.upgrade().getString("EscrowState").equals("CONSUMED"), tier.ritePath() + " did not complete");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_alembic_1")
    public static void alembicCondenserUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, UpgradeStation.ALEMBIC, 1); }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_alembic_2")
    public static void alembicAthanorUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, UpgradeStation.ALEMBIC, 2); }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_forge_1")
    public static void forgePrecisionUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, UpgradeStation.RESONANT_FORGE, 1); }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_forge_2")
    public static void forgeMasterworkUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, UpgradeStation.RESONANT_FORGE, 2); }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_armature_1")
    public static void armatureConsecrationUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, UpgradeStation.ARMATURE, 1); }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_armature_2")
    public static void armatureMonolithicUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, UpgradeStation.ARMATURE, 2); }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_scriptorium_1")
    public static void scriptoriumEightfoldUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, UpgradeStation.SCRIPTORIUM, 1); }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_scriptorium_2")
    public static void scriptoriumPalimpsestUpgrade(GameTestHelper h) { assertUpgradeCompletes(h, UpgradeStation.SCRIPTORIUM, 2); }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_recover")
    public static void everyStationRecoversEscrowOnceAndUnlocks(GameTestHelper helper) {
        for (UpgradeStation station : UpgradeStation.values()) {
            ActiveCardinalRite rite = upgradeRite(helper, StationUpgradeCatalog.get(station, 1));
            var level = helper.getLevel();
            helper.assertTrue(StationUpgradeRites.prepare(level, rite), station + " prepare");
            UpgradeableStation subject = StationUpgradeRites.station(level, rite);
            StationUpgradeRites.recover(level, rite);
            helper.assertTrue(!subject.isRiteLocked() && subject.upgradeTier() == 0
                    && rite.upgrade().getString("EscrowState").equals("RETURNED"), station + " recovery");
            helper.assertTrue(rite.upgrade().getList("Offerings", Tag.TAG_COMPOUND).size()
                    == StationUpgradeCatalog.OFFERINGS_PER_RITE, station + " escrow size");
            StationUpgradeRites.recover(level, rite);
            helper.assertTrue(rite.upgrade().getString("EscrowState").equals("RETURNED"), station + " recovered twice");
            level.removeBlock(subject.getBlockPos(), false);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_recover")
    public static void brokenSubjectReturnsEscrowOnce(GameTestHelper helper) {
        ActiveCardinalRite rite = upgradeRite(helper, StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 1));
        var level = helper.getLevel();
        helper.assertTrue(StationUpgradeRites.prepare(level, rite), "prepare");
        level.removeBlock(StationUpgradeRites.seat(rite), false);
        helper.assertTrue(!StationUpgradeRites.subjectPresent(level, rite), "missing subject still counted as present");
        StationUpgradeRites.recover(level, rite);
        helper.assertTrue(rite.upgrade().getString("EscrowState").equals("RETURNED"), "escrow not returned");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_recover")
    public static void legacySavedRitesResumeOrUnlock(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        var level = helper.getLevel();
        ActiveCardinalRite forgeRite = upgradeRite(helper, StationUpgradeCatalog.get(UpgradeStation.RESONANT_FORGE, 1));
        helper.assertTrue(StationUpgradeRites.prepare(level, forgeRite), "prepare");
        CompoundTag saved = forgeRite.serialize(registries);
        saved.putString("Phase", "ALEMBIC_PROJECTION");
        saved.put("AlembicUpgrade", saved.getCompound("StationUpgrade"));
        saved.remove("StationUpgrade");
        ActiveCardinalRite resumed = ActiveCardinalRite.deserialize(saved, registries);
        helper.assertTrue(resumed.getPhase() == CardinalRitePhase.STATION_PROJECTION
                && resumed.upgrade().getString("EscrowState").equals("ESCROWED"), "legacy projection did not resume");
        StationUpgradeRites.recover(level, resumed);
        level.removeBlock(StationUpgradeRites.seat(forgeRite), false);

        ActiveCardinalRite scriptRite = upgradeRite(helper, StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 2));
        UpgradeableStation subject = StationUpgradeRites.station(level, scriptRite);
        subject.setRiteLocked(true);
        CompoundTag old = scriptRite.serialize(registries);
        old.putString("RecipeId", "hemomancy:cardinal_rite/monolithic_script");
        old.putString("Phase", "SCRIPTORIAL_INSCRIPTION");
        old.remove("StationUpgrade");
        ActiveCardinalRite legacy = ActiveCardinalRite.deserialize(old, registries);
        helper.assertTrue(legacy.getRecipeId().getPath().equals("cardinal_rite/palimpsest")
                && legacy.getPhase() == CardinalRitePhase.COLLAPSED, "legacy Scriptorium rite was not collapsed onto the new id");
        StationUpgradeRites.recover(level, legacy);
        helper.assertTrue(!subject.isRiteLocked(), "legacy Scriptorium rite left the station locked");
        helper.succeed();
    }

    // ---- final-review regressions ----
    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade_review")
    public static void legacyAlembicTierSurvivesHeatChangeOnFirstTick(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.getLevel().setBlockAndUpdate(pos, BlockInit.ghastly_alembic.get().defaultBlockState());
        var alembic = (GhastlyAlembicBlockEntity) helper.getLevel().getBlockEntity(pos);
        CompoundTag tag = alembic.saveWithoutMetadata(registries);
        tag.putInt("AlembicTier", 2);
        tag.putBoolean("Heated", true); // no heat source below, so the first tick flips LIT
        alembic.loadWithComponents(tag, registries);
        GhastlyAlembicBlockEntity.serverTick(helper.getLevel(), pos, helper.getLevel().getBlockState(pos), alembic);
        helper.assertTrue(StationTierProperty.stage(helper.getLevel().getBlockState(pos)) == 2,
                "the first tick's LIT update reverted the migrated Alembic tier");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade_review")
    public static void riteLockedScriptoriumRefusesBloodTransfer(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.getLevel().setBlockAndUpdate(pos, withFacing(BlockInit.enzymatic_scriptorium.get().defaultBlockState()));
        var scriptorium = (com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity)
                helper.getLevel().getBlockEntity(pos);
        scriptorium.setRiteLocked(true);
        helper.assertTrue(!scriptorium.canReceiveBlood() && !scriptorium.canProvideBlood(),
                "a rite-locked Scriptorium still moves blood, which breaks its upgrade snapshot");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade_review")
    public static void legacyScriptoriumRiteReturnsAbsorbedOfferings(GameTestHelper helper) {
        ServerPlayer owner = player(helper);
        ActiveCardinalRite rite = upgradeRite(helper, StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 1), owner.getUUID());
        var level = helper.getLevel();
        // Pre-consolidation rites consumed braziers one by one during the offering procession, with no escrow.
        for (int i = 0; i < 2; i++) {
            var offering = rite.getOfferingItinerary().get(i);
            ((IronBrazierBlockEntity) level.getBlockEntity(offering.pos())).consumeOffering();
            rite.absorbCurrentOffering();
        }
        StationUpgradeRites.recover(level, rite);
        com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level.getServer().overworld()).deliverRecovery(owner);
        helper.assertTrue(owner.getInventory().contains(new ItemStack(ItemInit.rubricators_quill.get()))
                && owner.getInventory().countItem(Items.STICK) == 1,
                "offerings a legacy rite had already absorbed were not returned");
        owner.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "station_upgrade_review")
    public static void legacySnapshotKeysAndOperatorDoNotRefundAValidRite(GameTestHelper helper) {
        StationUpgradeTier tier = StationUpgradeCatalog.get(UpgradeStation.RESONANT_FORGE, 1);
        ActiveCardinalRite rite = upgradeRite(helper, tier);
        var level = helper.getLevel();
        helper.assertTrue(StationUpgradeRites.prepare(level, rite), "prepare");
        var forge = (ResonantForgeBlockEntity) StationUpgradeRites.station(level, rite);
        // Snapshots saved before consolidation carried the tier; a stray button press may change the operator.
        rite.upgrade().getCompound("SubjectSnapshot").putInt("Tier", 0);
        forge.setOperator(UUID.randomUUID());
        for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, 50);
        helper.assertTrue(rite.enterInscription() && rite.sealAltar(false), "seal");
        for (int circuit = 0; circuit < tier.circuits(); circuit++) rite.fillUpgradeCircuit(circuit, tier.bloodPerCircuit());
        rite.markComplete();
        helper.assertTrue(StationUpgradeRites.complete(level, rite) && forge.upgradeTier() == 1,
                "a legacy-format snapshot or operator change refunded a valid rite");
        helper.succeed();
    }

    // ---- claims ----
    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade_claims")
    public static void personalEligibilityUnlocksUpgradeLessonsWithoutClaims(GameTestHelper helper) {
        record Lesson(UpgradeStation station, int tier, String path) {}
        for (var lesson : List.of(
                new Lesson(UpgradeStation.ALEMBIC, 1, "alembic/pages/first_condensation"),
                new Lesson(UpgradeStation.ALEMBIC, 1, "alembic/pages/refined_and_compound"),
                new Lesson(UpgradeStation.ALEMBIC, 2, "alembic/pages/sanguine_athanor"),
                new Lesson(UpgradeStation.ALEMBIC, 2, "alembic/pages/bound_potion"),
                new Lesson(UpgradeStation.CENTRIFUGE, 1, "centrifuge/pages/steady_separation"),
                new Lesson(UpgradeStation.CENTRIFUGE, 2, "centrifuge/pages/second_fraction"),
                new Lesson(UpgradeStation.RESONANT_FORGE, 1, "resonant_forge/pages/upgrades"),
                new Lesson(UpgradeStation.SCRIPTORIUM, 1, "tendency/pages/eightfold_script"),
                new Lesson(UpgradeStation.SCRIPTORIUM, 2, "tendency/pages/palimpsest"))) {
            UpgradeStation station = lesson.station();
            int number = lesson.tier();
            var tier = StationUpgradeCatalog.get(station, number);
            var entry = Hemomancy.rloc("libersanguinium/" + lesson.path());
            ServerPlayer player = claimant(helper, tier.requiredDegree());
            var progress = HemoCapabilityAccess.stationUpgrades(player);
            progress.sync(player, true);
            helper.assertTrue(!LiberKnowledgeHelper.hasEntry(player, entry), "Rank alone revealed " + entry);
            for (int earlier = 1; earlier <= number; earlier++)
                StationUpgradeCatalog.get(station, earlier).requiredUsage()
                        .forEach(use -> progress.recordUse(station, use));
            progress.sync(player, false);
            helper.assertTrue(LiberEntryDefinitions.get(entry).isPresent(), "Filtered book cannot display " + entry);
            helper.assertTrue(LiberKnowledgeHelper.hasEntry(player, entry), "Personal eligibility did not reveal " + entry);
            progress.sync(player, false);
            helper.assertTrue(!progress.hasClaimed(station, number) && player.getInventory().isEmpty()
                    && HemoCapabilityAccess.getPlayerDegreeNumber(player) == tier.requiredDegree(),
                    "Lesson sync claimed a kit, delivered an item, or promoted the player");
            player.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade_claims")
    public static void upgradeLessonsBackfillOnLoginAndRespectEarlierPractice(GameTestHelper helper) {
        ServerPlayer player = claimant(helper, 6);
        var progress = HemoCapabilityAccess.stationUpgrades(player);
        var entry = Hemomancy.rloc("libersanguinium/alembic/pages/sanguine_athanor");
        progress.recordUse(UpgradeStation.ALEMBIC, StationUpgradeCatalog.REFINE);
        progress.recordUse(UpgradeStation.ALEMBIC, StationUpgradeCatalog.COMPOUND);
        player.getInventory().add(new ItemStack(ItemInit.sanguine_athanor_kit.get()));
        progress.sync(player, true);
        helper.assertTrue(!LiberKnowledgeHelper.hasEntry(player, entry), "Gifted kit bypassed earlier personal practice");
        progress.recordUse(UpgradeStation.ALEMBIC, StationUpgradeCatalog.DISTILL);
        HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> volume.setActive(false));
        progress.sync(player, true);
        helper.assertTrue(!LiberKnowledgeHelper.hasEntry(player, entry), "Inactive blood revealed the lesson");
        HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> volume.setActive(true));
        com.vincenthuto.hemomancy.common.station.StationUpgradeEvents.onLogin(
                new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(player));
        helper.assertTrue(LiberKnowledgeHelper.hasEntry(player, entry), "Login did not backfill earned teaching");
        helper.assertTrue(!progress.hasClaimed(UpgradeStation.ALEMBIC, 1)
                && !progress.hasClaimed(UpgradeStation.ALEMBIC, 2)
                && player.getInventory().countItem(ItemInit.sanguine_athanor_kit.get()) == 1,
                "Login backfill changed free-kit claims or duplicated the gifted kit");
        player.discard();
        helper.succeed();
    }

    static ServerPlayer player(GameTestHelper helper) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "station-test"), false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(player.server, connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(new BlockPos(5, 3, 5)).getCenter());
        return player;
    }

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
            helper.assertTrue(progress.eligible(player, second), station + " required a claim instead of eligibility");
            helper.assertTrue(progress.claim(player, first) && !progress.claim(player, first), station + " tier 1 claim");
            helper.assertTrue(player.getRecipeBook().contains(first.craftingRecipe()), station + " recipe not unlocked");
            helper.assertTrue(player.getInventory().contains(new ItemStack(BuiltInRegistries.ITEM.get(first.upgradeItem()))),
                    station + " item not delivered");
            helper.assertTrue(progress.claim(player, second), station + " tier 2 claim");
            player.discard();
        }
        ServerPlayer inactive = claimant(helper, 7);
        HemoCapabilityAccess.getBloodVolume(inactive).ifPresent(volume -> volume.setActive(false));
        StationUpgradeTier condenser = StationUpgradeCatalog.get(UpgradeStation.ALEMBIC, 1);
        HemoCapabilityAccess.stationUpgrades(inactive).recordUse(UpgradeStation.ALEMBIC, StationUpgradeCatalog.DISTILL);
        helper.assertTrue(!HemoCapabilityAccess.stationUpgrades(inactive).claim(inactive, condenser), "inactive blood claimed");
        ServerPlayer early = claimant(helper, 4);
        HemoCapabilityAccess.stationUpgrades(early).recordUse(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT);
        helper.assertTrue(!HemoCapabilityAccess.stationUpgrades(early).claim(early,
                StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 1)), "degree 4 claimed a D5 item");
        inactive.discard();
        early.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade_claims")
    public static void fullInventoryClaimIsDeliveredLater(GameTestHelper helper) {
        ServerPlayer player = claimant(helper, 5);
        StationUpgradeTier tier = StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 1);
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
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade_claims")
    public static void legacyClaimsMigrateWithoutSecondKit(GameTestHelper helper) {
        ServerPlayer player = claimant(helper, 7);
        player.getPersistentData().putBoolean("hemomancy.vicar_consecration_kit_claimed", true);
        CompoundTag legacy = new CompoundTag();
        legacy.putBoolean("Distilled", true);
        legacy.putBoolean("CondenserClaimed", true);
        player.getData(HemoAttachmentTypes.ADVANCED_BREWING).deserializeNBT(helper.getLevel().registryAccess(), legacy);
        StationUpgradeMigration.migrate(player);
        var progress = HemoCapabilityAccess.stationUpgrades(player);
        helper.assertTrue(progress.hasClaimed(UpgradeStation.ARMATURE, 1) && progress.hasClaimed(UpgradeStation.ALEMBIC, 1)
                && progress.hasUsed(UpgradeStation.ALEMBIC, StationUpgradeCatalog.DISTILL), "legacy state lost");
        helper.assertTrue(!player.getInventory().contains(new ItemStack(ItemInit.vicars_consecration_kit.get()))
                && !player.getInventory().contains(new ItemStack(ItemInit.hematic_condenser_kit.get())),
                "legacy claim re-issued a kit");
        helper.assertTrue(player.getRecipeBook().contains(Hemomancy.rloc("vicars_consecration_kit"))
                && player.getRecipeBook().contains(Hemomancy.rloc("hematic_condenser_kit")),
                "legacy claimant lacks the repeat recipe");
        helper.assertTrue(!player.getPersistentData().contains("hemomancy.vicar_consecration_kit_claimed"),
                "legacy flag kept");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade_claims")
    public static void mnemonistOffersScriptoriumClaimsOnlyWhenReady(GameTestHelper helper) {
        ServerPlayer player = claimant(helper, 7);
        var mnemonist = EntityInit.harbinger_mnemonist.get().create(helper.getLevel());
        mnemonist.setPos(player.position());
        helper.getLevel().addFreshEntity(mnemonist);
        StationUpgradeTier quill = StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 1);
        String event = StationUpgradeDialogue.claimEvent(quill);
        helper.assertTrue(!dialogueHasEvent(mnemonist.progressionDialogue(player), event), "unready claim was offered");
        HemoCapabilityAccess.stationUpgrades(player).recordUse(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT);
        helper.assertTrue(dialogueHasEvent(mnemonist.progressionDialogue(player), event), "ready claim was not offered");
        var dispatched = DialogueOptionPacket.dispatch(player, event, mnemonist.getId());
        helper.assertTrue(dispatched != null && dispatched.wasRewardDelivered()
                && player.getInventory().contains(new ItemStack(ItemInit.rubricators_quill.get())), "Mnemonist claim failed");
        var artificer = EntityInit.harbinger_artificer.get().create(helper.getLevel());
        artificer.setPos(player.position());
        helper.getLevel().addFreshEntity(artificer);
        String burin = StationUpgradeDialogue.claimEvent(StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 2));
        helper.assertTrue(DialogueOptionPacket.dispatch(player, burin, artificer.getId()) == null,
                "a non-Mnemonist handed out a Scriptorium claim");
        mnemonist.discard();
        artificer.discard();
        player.discard();
        helper.succeed();
    }

    static boolean dialogueHasEvent(DialogueTree tree, String event) {
        return tree.nodes().values().stream().flatMap(node -> node.options().stream())
                .anyMatch(option -> event.equals(option.eventId()));
    }

    static BlockState withFacing(BlockState state) {
        return state.hasProperty(HorizontalDirectionalBlock.FACING)
                ? state.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH) : state;
    }
}
