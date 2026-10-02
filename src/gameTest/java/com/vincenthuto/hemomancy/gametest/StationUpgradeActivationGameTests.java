package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.*;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.harbinger.*;
import com.vincenthuto.hemomancy.common.station.*;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("station_upgrade_validation")
@PrefixGameTestTemplate(false)
public final class StationUpgradeActivationGameTests {
    private StationUpgradeActivationGameTests() {}

    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "creative_station_upgrades")
    public static void creativeKitsUpgradeEveryStationInPlace(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = StationUpgradeGameTests.player(helper);
        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> volume.setActive(false));
        BlockPos pos = helper.absolutePos(new BlockPos(5, 3, 5));
        for (var station : UpgradeStation.values()) {
            for (int target = 1; target <= 2; target++) {
                var state = StationUpgradeGameTests.withFacing(BuiltInRegistries.BLOCK.get(station.blockId()).defaultBlockState());
                level.setBlockAndUpdate(pos, state);
                var entity = level.getBlockEntity(pos);
                var machine = (UpgradeableStation) entity;
                var identity = machine.machineIdentity();
                entity.getPersistentData().putString("CreativeUpgradeProbe", "preserved");
                if (entity instanceof net.minecraft.world.Container inventory)
                    inventory.setItem(0, new ItemStack(Items.DIAMOND, 3));
                if (entity instanceof com.vincenthuto.hemomancy.common.tile.IBloodReservoir reservoir)
                    reservoir.getBloodCapability().setBloodVolume(123);
                machine.setRiteLocked(true);
                var snapshot = machine.upgradeSnapshot(level.registryAccess());
                var kit = new ItemStack(BuiltInRegistries.ITEM.get(StationUpgradeCatalog.get(station, target).upgradeItem()), 2);
                clickKit(level, pos, player, kit);
                var after = machine.upgradeSnapshot(level.registryAccess());
                helper.assertTrue(machine.upgradeTier() == target, station + " Creative kit did not apply tier " + target);
                if (entity instanceof com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity alembic)
                    helper.assertTrue(alembic.getMaxBloodVolume() == 7500 && alembic.getBloodVolume() == 123,
                            "Creative Alembic kit must expand capacity without adding or losing blood");
                helper.assertTrue(level.getBlockEntity(pos) == entity && identity.equals(machine.machineIdentity())
                        && snapshot.equals(after) && machine.isRiteLocked(),
                        station + " Creative upgrade lost station state or changed an existing rite lock: "
                                + snapshot + " -> " + after);
                helper.assertTrue(kit.getCount() == 2, "Creative kit was consumed");
                helper.assertTrue(level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING) == Direction.NORTH,
                        station + " Creative upgrade changed facing");
                clickKit(level, pos, player, new ItemStack(BuiltInRegistries.ITEM.get(StationUpgradeCatalog.get(station, 1).upgradeItem())));
                helper.assertTrue(machine.upgradeTier() == target, "Creative kit downgraded the station");
                level.removeBlock(pos, false);
            }
        }
        player.discard();
        helper.succeed();
    }

    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "creative_station_upgrades")
    public static void directKitUseRejectsSurvivalAndWrongStations(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = StationUpgradeGameTests.player(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(5, 3, 5));
        for (var station : UpgradeStation.values()) {
            var state = StationUpgradeGameTests.withFacing(BuiltInRegistries.BLOCK.get(station.blockId()).defaultBlockState());
            level.setBlockAndUpdate(pos, state);
            for (int target = 1; target <= 2; target++) {
                var kit = new ItemStack(BuiltInRegistries.ITEM.get(StationUpgradeCatalog.get(station, target).upgradeItem()));
                clickKit(level, pos, player, kit);
                helper.assertTrue(StationTierProperty.stage(level.getBlockState(pos)) == 0 && kit.getCount() == 1,
                        station + " Survival gained a direct kit shortcut");
            }
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
            for (var tier : StationUpgradeCatalog.all()) {
                if (tier.station() == station) continue;
                clickKit(level, pos, player, new ItemStack(BuiltInRegistries.ITEM.get(tier.upgradeItem())));
                helper.assertTrue(StationTierProperty.stage(level.getBlockState(pos)) == 0, "wrong station kit upgraded " + station);
            }
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            level.removeBlock(pos, false);
        }
        player.discard();
        helper.succeed();
    }

    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "creative_station_footprint")
    public static void creativeAlembicBypassesClearanceWithoutDeletingBlocks(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = StationUpgradeGameTests.player(helper);
        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        BlockPos pos = helper.absolutePos(new BlockPos(5, 3, 5));
        var state = StationUpgradeGameTests.withFacing(BlockInit.ghastly_alembic.get().defaultBlockState());
        AlembicFootprintGameTests.clearSpace(helper, pos);
        level.setBlockAndUpdate(pos, state);
        BlockPos blocked = pos.above();
        level.setBlockAndUpdate(blocked, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        clickKit(level, pos, player, new ItemStack(ItemInit.sanguine_athanor_kit.get()));
        helper.assertTrue(StationTierProperty.stage(level.getBlockState(pos)) == 2,
                "Creative Athanor still required clearance");
        helper.assertTrue(level.getBlockState(blocked).is(net.minecraft.world.level.block.Blocks.STONE),
                "Creative upgrade deleted the obstructing block");
        level.removeBlock(pos, false);
        level.removeBlock(blocked, false);

        pos = pos.offset(4, 0, 0);

        AlembicFootprintGameTests.clearSpace(helper, pos);
        level.setBlockAndUpdate(pos, state);
        ((com.vincenthuto.hemomancy.common.block.harbinger.crafting.GhastlyAlembicBlock) state.getBlock())
                .placeFillers(level, pos, state);
        BlockPos filler = pos.above();
        helper.assertTrue(level.getBlockState(filler).is(BlockInit.filler_block.get()), "base Alembic filler missing");
        clickKit(level, filler, player, new ItemStack(ItemInit.hematic_condenser_kit.get()));
        helper.assertTrue(StationTierProperty.stage(level.getBlockState(pos)) == 1, "linked filler click did not upgrade controller");
        clickKit(level, filler, player, new ItemStack(ItemInit.sanguine_athanor_kit.get()));
        helper.assertTrue(StationTierProperty.stage(level.getBlockState(pos)) == 2, "second linked filler upgrade failed");
        player.discard();
        helper.succeed();
    }

    private static void clickKit(net.minecraft.server.level.ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack kit) {
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, kit);
        level.getBlockState(pos).useItemOn(kit, level, player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(pos.getCenter(), Direction.UP, pos, false));
    }

    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "station_lessons")
    public static void personalEligibilityUnlocksUpgradeLessonsWithoutClaims(GameTestHelper helper) {
        StationUpgradeGameTests.personalEligibilityUnlocksUpgradeLessonsWithoutClaims(helper);
    }

    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "station_lessons")
    public static void upgradeLessonsBackfillOnLoginAndRespectEarlierPractice(GameTestHelper helper) {
        StationUpgradeGameTests.upgradeLessonsBackfillOnLoginAndRespectEarlierPractice(helper);
    }

    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "station_claims")
    public static void claimGatesAndOneTimeRewards(GameTestHelper helper) {
        StationUpgradeGameTests.claimsRequireUseDegreeAndOrder(helper);
    }

    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "station_claims")
    public static void fullInventoryRewardDelivery(GameTestHelper helper) {
        StationUpgradeGameTests.fullInventoryClaimIsDeliveredLater(helper);
    }

    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "station_optional_claims")
    public static void secondKitCanBeClaimedWithoutAcceptingTheFirst(GameTestHelper helper) {
        for (var station : UpgradeStation.values()) {
            ServerPlayer player = StationUpgradeGameTests.claimant(helper, 7);
            var tier = StationUpgradeCatalog.get(station, 2);
            var progress = HemoCapabilityAccess.stationUpgrades(player);
            practice(player, tier);
            helper.assertTrue(progress.claim(player, tier) && !progress.hasClaimed(station, 1),
                    "eligible player was required to accept the first free item");
            helper.assertTrue(!progress.claim(player, tier), "duplicate second kit claim");
            progress.deserializeNBT(player.registryAccess(), new net.minecraft.nbt.CompoundTag());
            helper.assertTrue(progress.claim(player, tier) == false && !progress.eligible(player, tier),
                    "cleared personal evidence still permitted claim/use");
            player.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "station_alembic_activation")
    public static void alembicActivationAndFinale(GameTestHelper helper) { upgradeBoth(helper, UpgradeStation.ALEMBIC); }
    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "station_forge_activation")
    public static void forgeActivationAndFinale(GameTestHelper helper) { upgradeBoth(helper, UpgradeStation.RESONANT_FORGE); }
    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "station_armature_activation")
    public static void armatureActivationAndFinale(GameTestHelper helper) { upgradeBoth(helper, UpgradeStation.ARMATURE); }
    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "station_scriptorium_activation")
    public static void scriptoriumActivationAndFinale(GameTestHelper helper) { upgradeBoth(helper, UpgradeStation.SCRIPTORIUM); }

    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "station_centrifuge_activation")
    public static void centrifugeActivationAndFinale(GameTestHelper helper) { upgradeBoth(helper, UpgradeStation.CENTRIFUGE); }

    private static BlockPos prepare(GameTestHelper helper, StationUpgradeTier tier) {
        var level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(5, 3, 5));
        // Lesser and greater floors have different socket positions; don't carry prior offerings forward.
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-7, 1, -7), center.offset(7, 1, 7)))
            if (level.getBlockState(pos).is(BlockInit.iron_brazier.get())) level.removeBlock(pos, false);
        var floor = CardinalRiteFloorRegistry.get(net.minecraft.resources.ResourceLocation.parse(tier.floor())).orElseThrow();
        for (var pair : floor.pattern().getBlockPosBlockList()) {
            BlockPos p = pair.getPos();
            if (pair.getBlock() != null) level.setBlock(center.offset(p.getX() - floor.focus().getX(),
                    p.getY(), floor.focus().getZ() - p.getZ()), pair.getBlock().defaultBlockState(), Block.UPDATE_ALL);
        }
        Block block = BuiltInRegistries.BLOCK.get(tier.station().blockId());
        var state = block.defaultBlockState().setValue(StationTierProperty.STAGE, tier.tier() - 1);
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) state = state.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
        BlockPos seat = center.above().relative(Direction.SOUTH, tier.station().seatDistance());
        if (tier.station() == UpgradeStation.ALEMBIC) AlembicFootprintGameTests.clearSpace(helper, seat);
        level.setBlockAndUpdate(seat, state);
        block.setPlacedBy(level, seat, state, null, new ItemStack(block));
        var recipe = CardinalRiteRecipe.getRiteByLocation(level, tier.rite());
        int i = 0;
        for (var requirement : recipe.getBrazierSignature()) {
            BlockPos socket = floor.brazierSockets().get(i++);
            BlockPos pos = center.offset(socket.getX(), socket.getY(), -socket.getZ());
            level.setBlockAndUpdate(pos, BlockInit.iron_brazier.get().defaultBlockState().setValue(BrazierBlock.RITUAL_PHASE, 1));
            ((IronBrazierBlockEntity) level.getBlockEntity(pos)).insertOffering(null, requirement.ingredient().getItems()[0].copyWithCount(1));
        }
        helper.assertTrue(CardinalRiteStationMatcher.find(level, center, recipe).isPresent(), tier.ritePath() + " real offering/floor match failed");
        return center;
    }

    private static void practice(ServerPlayer player, StationUpgradeTier tier) {
        var progress = HemoCapabilityAccess.stationUpgrades(player);
        for (int number = 1; number <= tier.tier(); number++)
            StationUpgradeCatalog.get(tier.station(), number).requiredUsage().forEach(use -> progress.recordUse(tier.station(), use));
    }

    private static void aim(ServerPlayer player, Vec3 target) {
        Vec3 delta = target.subtract(player.getEyePosition());
        player.setYRot((float) Math.toDegrees(Math.atan2(-delta.x, delta.z)));
        player.setXRot((float) -Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z))));
    }

    private static void upgradeBoth(GameTestHelper helper, UpgradeStation station) {
        ServerPlayer player = StationUpgradeGameTests.claimant(helper, 7);
        var saved = CardinalRiteSavedData.get(helper.getLevel());
        try {
            for (int number = 1; number <= 2; number++) {
                var tier = StationUpgradeCatalog.get(station, number);
                BlockPos center = prepare(helper, tier);
                if (station == UpgradeStation.ALEMBIC) {
                    var seat = center.above().relative(Direction.SOUTH, station.seatDistance());
                    HemoCapabilityAccess.getBloodVolume(helper.getLevel().getBlockEntity(seat)).orElseThrow()
                            .setBloodVolume(4000);
                }
                practice(player, tier);
                player.setPos(Vec3.atCenterOf(center).add(0, 1, 0));
                player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.living_staff.get()));
                var start = BloodCraftingKeyPressPacket.tryStartCardinalRite(player, center, CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
                helper.assertTrue(start == CardinalRiteActivationRules.ActivationAttempt.STARTED, tier.ritePath() + " activation refused eligible unclaimed player");
                var rite = saved.getRite(player.getUUID());
                helper.assertTrue(rite != null && rite.getRecipeId().equals(tier.rite()), "wrong rite activated");
                for (var offering : rite.getOfferingItinerary())
                    helper.assertTrue(((IronBrazierBlockEntity) helper.getLevel().getBlockEntity(offering.pos())).getOfferingForMatching().isEmpty(), "offering not escrowed");
                for (int anchor = 0; anchor < rite.getAnchorBloodMl().length; anchor++) rite.fillAnchor(anchor, 50);
                helper.assertTrue(rite.enterInscription() && rite.sealAltar(false), "altar did not seal");
                HemoCapabilityAccess.requireBloodVolume(player).setBloodVolume(1000);
                for (int circuit = 0; circuit < tier.circuits(); circuit++) {
                    aim(player, StationUpgradeRites.targetSurface(helper.getLevel(), rite, circuit));
                    var result = CardinalRiteInteractionHandler.tryProject(helper.getLevel(), player, tier.bloodPerCircuit());
                    helper.assertTrue(result.bloodSpent() == tier.bloodPerCircuit(), "real projection did not spend circuit blood");
                }
                var recipe = CardinalRiteRecipe.getRiteByLocation(helper.getLevel(), tier.rite());
                for (int ticks = 0; ticks < 3000 && !rite.isComplete(); ticks++)
                    CardinalRiteOrdealEngine.tick(helper.getLevel(), player, rite, recipe);
                helper.assertTrue(rite.isComplete() && rite.getOfferingVisitIndex() == 6, "natural procession/finale did not complete");
                helper.assertTrue(StationUpgradeRites.complete(helper.getLevel(), rite), "completion refused");
                var subject = StationUpgradeRites.station(helper.getLevel(), rite);
                helper.assertTrue(subject.upgradeTier() == number && !subject.isRiteLocked(), "wrong completed station tier");
                if (subject instanceof com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity alembic)
                    helper.assertTrue(alembic.getMaxBloodVolume() == 7500 && alembic.getBloodVolume() == 4000,
                            "Alembic rite must finish with expanded capacity and preserve existing blood");
                if (station == UpgradeStation.ALEMBIC) {
                    helper.getLevel().setBlockAndUpdate(subject.getBlockPos().below(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                    helper.assertTrue(com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity
                            .isHeatSource(helper.getLevel(), subject.getBlockPos()) == (number == 2),
                            "Only the completed Athanor rite must provide permanent heat");
                }
                saved.deliverRecovery(player);
                helper.assertTrue(player.getInventory().contains(new ItemStack(ItemInit.living_staff.get())), "staff not returned");
                saved.removeRite(player.getUUID());
                helper.getLevel().removeBlock(subject.getBlockPos(), false);
                helper.getLevel().getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity.class,
                        new net.minecraft.world.phys.AABB(center).inflate(10)).forEach(net.minecraft.world.entity.Entity::discard);
            }
            helper.succeed();
        } finally {
            var rite = saved.getRite(player.getUUID());
            if (rite != null) StationUpgradeRites.recover(helper.getLevel(), rite);
            saved.removeRite(player.getUUID());
            player.discard();
        }
    }

    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "station_personal_eligibility")
    public static void giftedKitCannotBypassPersonalPractice(GameTestHelper helper) {
        ServerPlayer player = StationUpgradeGameTests.claimant(helper, 7);
        var saved = CardinalRiteSavedData.get(helper.getLevel());
        try {
            for (var tier : StationUpgradeCatalog.all()) {
                BlockPos center = prepare(helper, tier);
                player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.living_staff.get()));
                var progress = HemoCapabilityAccess.stationUpgrades(player);
                if (tier.tier() == 2) tier.requiredUsage().forEach(use -> progress.recordUse(tier.station(), use));
                var start = BloodCraftingKeyPressPacket.tryStartCardinalRite(player, center, CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
                helper.assertTrue(start != CardinalRiteActivationRules.ActivationAttempt.STARTED && !saved.hasActiveRite(player.getUUID()), "gifted kit bypassed personal/previous practice");
                helper.assertTrue(player.getInventory().contains(new ItemStack(ItemInit.living_staff.get())), "refusal lost staff");
                var recipe = CardinalRiteRecipe.getRiteByLocation(helper.getLevel(), tier.rite());
                helper.assertTrue(CardinalRiteStationMatcher.find(helper.getLevel(), center, recipe).isPresent(), "refusal consumed offerings");
                helper.getLevel().removeBlock(center.above().relative(Direction.SOUTH, tier.station().seatDistance()), false);
            }
            helper.succeed();
        } finally { saved.removeRite(player.getUUID()); player.discard(); }
    }

    @GameTest(templateNamespace = "station_upgrade_validation", template = "empty", timeoutTicks = 100, batch = "station_recovery_counts")
    public static void brokenStationsRefundEveryOfferingExactlyOnce(GameTestHelper helper) {
        for (var station : UpgradeStation.values()) {
            ServerPlayer player = StationUpgradeGameTests.claimant(helper, 7);
            var rite = StationUpgradeGameTests.upgradeRite(helper, StationUpgradeCatalog.get(station, 1), player.getUUID());
            helper.assertTrue(StationUpgradeRites.prepare(helper.getLevel(), rite), "prepare failed");
            helper.getLevel().removeBlock(StationUpgradeRites.seat(rite), false);
            StationUpgradeRites.recover(helper.getLevel(), rite);
            var saved = CardinalRiteSavedData.get(helper.getLevel().getServer().overworld());
            saved.deliverRecovery(player);
            helper.assertTrue(player.getInventory().countItem(Items.STICK) == 5
                    && player.getInventory().countItem(BuiltInRegistries.ITEM.get(StationUpgradeCatalog.get(station, 1).upgradeItem())) == 1, "refund did not return all six items");
            StationUpgradeRites.recover(helper.getLevel(), rite);
            saved.deliverRecovery(player);
            helper.assertTrue(player.getInventory().countItem(Items.STICK) == 5, "duplicate refund");
            player.discard();
        }
        helper.succeed();
    }
}
