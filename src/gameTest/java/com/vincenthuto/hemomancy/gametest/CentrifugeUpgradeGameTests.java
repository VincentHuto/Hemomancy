package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.station.StationTierProperty;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeStartupResult;
import com.vincenthuto.hemomancy.common.station.UpgradeStation;
import com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.StationUpgradeDialogue;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.common.item.harbinger.ConsecratedSyringeItem;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("station_upgrade_validation")
@PrefixGameTestTemplate(false)
public final class CentrifugeUpgradeGameTests {
    private CentrifugeUpgradeGameTests() {}

    private static VialCentrifugeBlockEntity machine(GameTestHelper helper, int stage) {
        var state = BlockInit.vial_centrifuge.get().defaultBlockState();
        helper.assertTrue(state.hasProperty(StationTierProperty.STAGE), "centrifuge lacks installed stages");
        var pos = helper.absolutePos(new BlockPos(3, 3, 3));
        helper.getLevel().setBlockAndUpdate(pos, state.setValue(StationTierProperty.STAGE, stage));
        var machine = (VialCentrifugeBlockEntity) helper.getLevel().getBlockEntity(pos);
        machine.setItem(2, sample("minecraft:cow"));
        machine.setItem(6, sample("minecraft:cow"));
        return machine;
    }

    static ItemStack sample(String source) {
        var sample = new ItemStack(ItemInit.bloody_vial.get());
        var tag = new CompoundTag();
        tag.putString(BloodVialItem.TAG_ENTITY_TYPE, source);
        tag.putBoolean(BloodVialItem.TAG_STATE, true);
        sample.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return sample;
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "centrifuge_duration")
    public static void eachStageUsesItsOwnDurationAndReturnsVials(GameTestHelper helper) {
        for (int stage = 0; stage < 3; stage++) {
            var machine = machine(helper, stage);
            int duration = 200 - stage * 50;
            helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "balanced spin refused");
            helper.assertTrue(machine.dataAccess.get(0) == duration && machine.dataAccess.get(1) == duration,
                    "wrong stage duration/progress total");
            for (int tick = 0; tick < duration; tick++)
                VialCentrifugeBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
            helper.assertTrue(!machine.getItem(10).isEmpty() && !machine.getItem(14).isEmpty(), "missing primary results");
            helper.assertTrue(!machine.getItem(2).has(DataComponents.CUSTOM_DATA)
                    && !machine.getItem(6).has(DataComponents.CUSTOM_DATA), "filled samples were not returned empty");
            helper.assertTrue(machine.getBloodVolume() == 250, "changed operation blood income");
            if (stage == 2) helper.assertTrue(machine.getItem(18).getCount() == 2, "D6 powder was not guaranteed");
            helper.getLevel().removeBlock(machine.getBlockPos(), false);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "centrifuge_atomic_output")
    public static void powderCapacityBlocksTheWholeBatchBeforeConsumption(GameTestHelper helper) {
        var machine = machine(helper, 2);
        machine.setItem(18, new ItemStack(ItemInit.hematic_iron_powder.get(), 63));
        helper.assertTrue(machine.attemptStartup(null) != VialCentrifugeStartupResult.SUCCESS,
                "spin would lose a guaranteed byproduct");
        helper.assertTrue(machine.getItem(2).has(DataComponents.CUSTOM_DATA) && machine.getItem(10).isEmpty()
                && machine.getItem(18).getCount() == 63, "blocked startup changed inventory");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "centrifuge_saved_batch")
    public static void blockedCompletionRetainsResultsAcrossReloadAndRetry(GameTestHelper helper) {
        var machine = machine(helper, 2);
        helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "startup failed");
        machine.setItem(10, new ItemStack(Items.DIAMOND));
        for (int tick = 0; tick < 100; tick++)
            VialCentrifugeBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
        var registries = helper.getLevel().registryAccess();
        var saved = machine.saveWithoutMetadata(registries);
        helper.assertTrue(saved.contains("PendingBatch"), "blocked completion discarded its rolled batch");
        var expected = saved.getCompound("PendingBatch").copy();
        machine.loadWithComponents(saved, registries);
        helper.assertTrue(expected.equals(machine.saveWithoutMetadata(registries).getCompound("PendingBatch")),
                "reload rerolled persisted output");
        var expectedPrimary = net.minecraft.core.NonNullList.withSize(8, ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(expected.getCompound("Primary"), expectedPrimary, registries);
        machine.setItem(10, ItemStack.EMPTY);
        helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "saved batch cannot retry");
        helper.assertTrue(ItemStack.matches(expectedPrimary.get(0), machine.getItem(10))
                && !machine.saveWithoutMetadata(registries).contains("PendingBatch"), "retry changed or retained settled output");
        helper.assertTrue(!machine.getItem(10).isEmpty() && machine.getItem(18).getCount() == 2,
                "retry did not settle all output exactly once");
        var count = machine.getItem(10).getCount();
        VialCentrifugeBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
        helper.assertTrue(machine.getItem(10).getCount() == count && machine.getBloodVolume() == 250,
                "retry duplicated output/blood");
        helper.succeed();
    }

    private static void finish(GameTestHelper helper, VialCentrifugeBlockEntity machine) {
        int duration = machine.dataAccess.get(0);
        for (int tick = 0; tick < duration; tick++)
            VialCentrifugeBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "centrifuge_native_fractions")
    public static void mixedSamplesRecoverOnlyDistinctNativeFractions(GameTestHelper helper) {
        var machine = machine(helper, 2);
        helper.getLevel().random.setSeed(90210L);
        int secondaryCount = 0;
        for (int spin = 0; spin < 48; spin++) {
            machine.clearContent();
            machine.setItem(2, sample("minecraft:chicken"));
            machine.setItem(6, sample("minecraft:chicken"));
            helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "mixed spin refused");
            finish(helper, machine);
            for (int index : new int[]{0, 4}) {
                var primary = machine.getItem(10 + index);
                var secondary = machine.getItem(20 + index);
                helper.assertTrue(primary.is(ItemInit.vivacious_enzyme.get()) || primary.is(ItemInit.neurotic_enzyme.get()),
                        "primary fraction absent from chicken profile");
                if (secondary.isEmpty()) continue;
                secondaryCount++;
                helper.assertTrue(secondary.getCount() == 1 && !secondary.is(primary.getItem())
                        && (secondary.is(ItemInit.vivacious_enzyme.get()) || secondary.is(ItemInit.neurotic_enzyme.get())),
                        "secondary fraction duplicated primary, invented a tendency, or gained a quantity bonus");
            }
        }
        helper.assertTrue(secondaryCount > 20 && secondaryCount < 76, "second-fraction roll not near half: " + secondaryCount);
        machine.clearContent();
        machine.setItem(2, sample("minecraft:cow"));
        machine.setItem(6, sample("minecraft:cow"));
        helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "single fraction spin refused");
        finish(helper, machine);
        helper.assertTrue(machine.getItem(20).isEmpty() && machine.getItem(24).isEmpty(), "single tendency invented a second fraction");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "centrifuge_running_reload")
    public static void runningBatchReloadKeepsDurationAndExactResults(GameTestHelper helper) {
        var machine = machine(helper, 1);
        helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "startup failed");
        for (int tick = 0; tick < 37; tick++)
            VialCentrifugeBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
        var registries = helper.getLevel().registryAccess();
        var saved = machine.saveWithoutMetadata(registries);
        var batch = saved.getCompound("PendingBatch").copy();
        machine.loadWithComponents(saved, registries);
        helper.assertTrue(machine.dataAccess.get(0) == 113 && machine.dataAccess.get(1) == 150
                && batch.equals(machine.saveWithoutMetadata(registries).getCompound("PendingBatch")),
                "running reload reset time or rerolled results");
        finish(helper, machine);
        helper.assertTrue(!machine.getItem(10).isEmpty() && machine.getBloodVolume() == 250,
                "saved running batch did not settle once");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "centrifuge_secondary_capacity")
    public static void secondaryBlockagePreservesTheWholeBatchForRetry(GameTestHelper helper) {
        var machine = machine(helper, 2);
        int blockedIndex = -1;
        for (int attempt = 0; attempt < 32; attempt++) {
            machine.clearContent();
            machine.setItem(2, sample("minecraft:chicken"));
            machine.setItem(6, sample("minecraft:chicken"));
            helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "startup failed");
            var secondary = net.minecraft.core.NonNullList.withSize(8, ItemStack.EMPTY);
            net.minecraft.world.ContainerHelper.loadAllItems(machine.saveWithoutMetadata(helper.getLevel().registryAccess())
                    .getCompound("PendingBatch").getCompound("Secondary"), secondary, helper.getLevel().registryAccess());
            for (int index = 0; index < 8; index++) if (!secondary.get(index).isEmpty()) { blockedIndex = index; break; }
            if (blockedIndex >= 0) break;
            finish(helper, machine);
        }
        helper.assertTrue(blockedIndex >= 0, "no second fraction found");
        double blood = machine.getBloodVolume();
        machine.setItem(20 + blockedIndex, new ItemStack(Items.DIAMOND));
        finish(helper, machine);
        helper.assertTrue(machine.getItem(10).isEmpty() && machine.getItem(14).isEmpty() && machine.getItem(18).isEmpty()
                && machine.getItem(2).has(DataComponents.CUSTOM_DATA) && machine.getBloodVolume() == blood,
                "blocked secondary consumed samples, emitted partial output, or granted blood");
        machine.setItem(20 + blockedIndex, ItemStack.EMPTY);
        helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS
                && machine.getItem(20 + blockedIndex).getCount() == 1 && machine.getItem(18).getCount() == 2,
                "secondary retry lost its fraction or powder");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "centrifuge_locking")
    public static void riteLockStopsProcessingInventoryAndBloodTransfer(GameTestHelper helper) {
        var machine = machine(helper, 1);
        machine.getBloodCapability().setBloodVolume(500);
        machine.setRiteLocked(true);
        var snapshot = machine.upgradeSnapshot(helper.getLevel().registryAccess());
        helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.RITE_LOCKED
                && machine.removeItem(2, 1).isEmpty(), "rite lock permitted processing or extraction");
        machine.setItem(2, ItemStack.EMPTY);
        helper.assertTrue(machine.getItem(2).has(DataComponents.CUSTOM_DATA)
                && machine.receiveBlood(50) == 0 && machine.drainBlood(50) == 0, "rite lock changed contents/blood");
        VialCentrifugeBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
        helper.assertTrue(machine.getBloodVolume() == 500 && !machine.isSpinning(), "locked machine tick mutated state");
        helper.assertTrue(snapshot.equals(machine.upgradeSnapshot(helper.getLevel().registryAccess())),
                "refused operation changed the locked rite subject snapshot");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "centrifuge_hopper_lock")
    public static void hopperInsertionRespectsLocksAndOutputSlots(GameTestHelper helper) {
        var machine = machine(helper, 1);
        machine.setRiteLocked(true);
        var incoming = sample("minecraft:cow");
        var remaining = net.minecraft.world.level.block.entity.HopperBlockEntity.addItem(null, machine, incoming.copy(), null);
        helper.assertTrue(ItemStack.matches(incoming, remaining) && machine.getItem(0).isEmpty(),
                "hopper accepted and lost an item during a rite");
        machine.setRiteLocked(false);
        for (int slot : new int[]{0, 10, 18, 19, 20})
            helper.assertTrue(!machine.canPlaceItem(slot, new ItemStack(ItemInit.vivacious_enzyme.get())),
                    "automation may insert into reserved/output slot " + slot);
        helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "spin refused");
        helper.assertTrue(!machine.canPlaceItem(3, incoming), "automation may insert into a running rotor");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "centrifuge_partial_recovery")
    public static void partialSharedRecoveryPreservesQuantitiesAndProcessingStages(GameTestHelper helper) {
        var first = StationUpgradeGameTests.claimant(helper, 6);
        var second = StationUpgradeGameTests.claimant(helper, 6);
        try {
            var machine = machine(helper, 0);
            for (int attempt = 0; attempt < 16; attempt++) {
                machine.clearContent();
                machine.setItem(2, sample("minecraft:cow"));
                machine.setItem(6, sample("minecraft:cow"));
                helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "spin refused");
                finish(helper, machine);
                if (machine.getItem(10).getCount() >= 3) break;
            }
            int baseCount = machine.getItem(10).getCount();
            helper.assertTrue(baseCount >= 3, "no multi-item base result");
            helper.getLevel().setBlockAndUpdate(machine.getBlockPos(), machine.getBlockState().setValue(StationTierProperty.STAGE, 1));
            machine.setItem(2, sample("minecraft:cow"));
            machine.setItem(6, sample("minecraft:cow"));
            helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "stacked calibrated spin refused");
            finish(helper, machine);
            var result = machine.getItem(10).copyWithCount(63);
            for (int slot = 0; slot < 36; slot++) first.getInventory().setItem(slot, new ItemStack(Items.DIAMOND, 64));
            first.getInventory().setItem(0, result);
            var menu = new VialCentrifugeMenu(1, first.getInventory(), machine);
            int before = machine.getItem(10).getCount();
            menu.quickMoveStack(first, 9);
            helper.assertTrue(machine.getItem(10).getCount() == before - 1, "shift extraction was not partial");
            var firstProgress = HemoCapabilityAccess.stationUpgrades(first);
            helper.assertTrue(firstProgress.hasUsed(UpgradeStation.CENTRIFUGE, StationUpgradeCatalog.SEPARATE)
                    && !firstProgress.hasUsed(UpgradeStation.CENTRIFUGE, StationUpgradeCatalog.SEPARATE_CALIBRATED),
                    "partial base extraction consumed or credited later calibrated output");
            var registries = helper.getLevel().registryAccess();
            machine.loadWithComponents(machine.saveWithoutMetadata(registries), registries);
            machine.onPlayerExtract(second, 10, machine.removeItem(10, baseCount - 1));
            var secondProgress = HemoCapabilityAccess.stationUpgrades(second);
            helper.assertTrue(secondProgress.hasUsed(UpgradeStation.CENTRIFUGE, StationUpgradeCatalog.SEPARATE)
                    && !secondProgress.hasUsed(UpgradeStation.CENTRIFUGE, StationUpgradeCatalog.SEPARATE_CALIBRATED),
                    "remaining base output lost its own recovery credit after reload");
            machine.onPlayerExtract(second, 10, machine.removeItem(10, 1));
            helper.assertTrue(secondProgress.hasUsed(UpgradeStation.CENTRIFUGE, StationUpgradeCatalog.SEPARATE_CALIBRATED),
                    "stacked calibrated result lost its recovery credit");
        } finally { first.discard(); second.discard(); }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "centrifuge_secondary_recovery")
    public static void secondaryFractionRecoveryCountsAsPersonalPractice(GameTestHelper helper) {
        var player = StationUpgradeGameTests.claimant(helper, 6);
        try {
            var machine = machine(helper, 2);
            int slot = -1;
            for (int attempt = 0; attempt < 16 && slot < 0; attempt++) {
                machine.clearContent();
                machine.setItem(2, sample("minecraft:chicken"));
                machine.setItem(6, sample("minecraft:chicken"));
                helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "spin refused");
                finish(helper, machine);
                for (int index = 20; index < 28; index++) if (!machine.getItem(index).isEmpty()) { slot = index; break; }
            }
            helper.assertTrue(slot >= 0, "no secondary output found");
            machine.onPlayerExtract(player, slot, machine.removeItem(slot, 1));
            helper.assertTrue(HemoCapabilityAccess.stationUpgrades(player).eligible(player,
                    StationUpgradeCatalog.get(UpgradeStation.CENTRIFUGE, 2)), "secondary recovery supplied no practice");
        } finally { player.discard(); }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "centrifuge_prior_evidence")
    public static void existingFirstSeparationRecoverySuppliesOnlyBasicPractice(GameTestHelper helper) {
        var player = StationUpgradeGameTests.claimant(helper, 6);
        try {
            var progress = HemoCapabilityAccess.stationUpgrades(player);
            com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.grantIfNotDone(player,
                    com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE);
            helper.assertTrue(progress.eligible(player, StationUpgradeCatalog.get(UpgradeStation.CENTRIFUGE, 1))
                    && !progress.eligible(player, StationUpgradeCatalog.get(UpgradeStation.CENTRIFUGE, 2)),
                    "existing separation was lost or invented calibrated practice");
        } finally { player.discard(); }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "centrifuge_personal_recovery")
    public static void recoveryCreditsActualProcessingStageAndSharedUse(GameTestHelper helper) {
        var player = StationUpgradeGameTests.claimant(helper, 6);
        var progress = HemoCapabilityAccess.stationUpgrades(player);
        try {
            var machine = machine(helper, 0);
            helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "base spin refused");
            finish(helper, machine);
            helper.getLevel().setBlockAndUpdate(machine.getBlockPos(), machine.getBlockState().setValue(StationTierProperty.STAGE, 1));
            var menu = new VialCentrifugeMenu(1, player.getInventory(), machine);
            helper.assertTrue(!menu.quickMoveStack(player, 9).isEmpty(), "personal shift extraction failed");
            helper.assertTrue(progress.hasUsed(UpgradeStation.CENTRIFUGE,
                    StationUpgradeCatalog.SEPARATE), "base recovery was not credited");
            helper.assertTrue(!progress.hasUsed(UpgradeStation.CENTRIFUGE,
                    StationUpgradeCatalog.SEPARATE_CALIBRATED),
                    "changing the stage after processing invented calibrated practice");
            machine.clearContent();
            machine.setItem(2, sample("minecraft:cow"));
            machine.setItem(6, sample("minecraft:cow"));
            helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "shared calibrated spin refused");
            finish(helper, machine);
            var saved = machine.saveWithoutMetadata(helper.getLevel().registryAccess());
            machine.loadWithComponents(saved, helper.getLevel().registryAccess());
            helper.assertTrue(!menu.quickMoveStack(player, 9).isEmpty(), "saved calibrated extraction failed");
            helper.assertTrue(progress.eligible(player, StationUpgradeCatalog.get(
                    UpgradeStation.CENTRIFUGE, 2)), "shared calibrated practice lost eligibility");
        } finally { player.discard(); }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "alchemist_degree_gates")
    public static void alchemistKitsRequireFourAndSixAndTheCorrectTeacher(GameTestHelper helper) {
        var player = StationUpgradeGameTests.claimant(helper, 3);
        try {
            for (var station : new UpgradeStation[]{
                    UpgradeStation.ALEMBIC,
                    UpgradeStation.CENTRIFUGE}) {
                var first = StationUpgradeCatalog.get(station, 1);
                var second = StationUpgradeCatalog.get(station, 2);
                var progress = HemoCapabilityAccess.stationUpgrades(player);
                first.requiredUsage().forEach(use -> progress.recordUse(station, use));
                second.requiredUsage().forEach(use -> progress.recordUse(station, use));
                HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(3);
                helper.assertTrue(!progress.eligible(player, first), "D3 bypassed D4 gate");
                HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(4);
                helper.assertTrue(progress.eligible(player, first), "D4 first upgrade refused");
                HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(5);
                helper.assertTrue(!progress.eligible(player, second), "D5 bypassed D6 gate");
                HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(6);
                helper.assertTrue(progress.eligible(player, second), "D6 second upgrade refused");
                var alchemist = EntityInit.harbinger_alchemist.get().create(helper.getLevel());
                var artificer = EntityInit.harbinger_artificer.get().create(helper.getLevel());
                helper.assertTrue(StationUpgradeDialogue.allowedSpeaker(station, alchemist)
                        && !StationUpgradeDialogue.allowedSpeaker(station, artificer),
                        "wrong station teacher accepted");
            }
        } finally { player.discard(); }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "centrifuge_sacred_outputs")
    public static void sacredSyringesRetainOneResiduumAndWardenRemainsInvalid(GameTestHelper helper) {
        var machine = machine(helper, 2);
        machine.clearContent();
        var syringe = new ItemStack(ItemInit.consecrated_syringe.get());
        ConsecratedSyringeItem.setSaintType(syringe,
                EnumSaintType.HEMORATH);
        machine.setItem(2, syringe.copy());
        machine.setItem(6, syringe.copy());
        helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.SUCCESS, "sacred spin refused");
        finish(helper, machine);
        helper.assertTrue(machine.getItem(10).is(ItemInit.hallowed_residuum_hemorath.get())
                && machine.getItem(10).getCount() == 1 && machine.getItem(14).getCount() == 1,
                "sacred currency was multiplied");
        helper.assertTrue(machine.getItem(18).isEmpty() && machine.getItem(20).isEmpty()
                && machine.getItem(24).isEmpty(), "syringes gained ordinary vial byproducts");
        machine.clearContent();
        machine.setItem(2, sample("minecraft:warden"));
        machine.setItem(6, sample("minecraft:warden"));
        helper.assertTrue(machine.attemptStartup(null) == VialCentrifugeStartupResult.INVALID_SAMPLE
                && machine.getItem(2).has(DataComponents.CUSTOM_DATA), "Warden bypassed sample restrictions");
        helper.succeed();
    }
}
