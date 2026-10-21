package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SanguineCondenserBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.SanguineCondenserProjection;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.station.StationTierProperty;
import com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.station.UpgradeStation;
import com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SanguineCondenserGameTests {
    private static final BlockPos SUPPORT = new BlockPos(2, 1, 2);
    private static final BlockPos SEAT = SUPPORT.above();

    private SanguineCondenserGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 60, batch = "sanguine_condenser")
    public static void condensesOverTimeAndStopsWhenFull(GameTestHelper helper) {
        ServerPlayer owner = StationUpgradeGameTests.claimant(helper, 3);
        SanguineCondenserBlockEntity condenser = condenser(helper, owner, 1);
        helper.assertTrue(condenser.receiveProjectedBlood(owner, 750) == 750, "Latticed pool did not take 750 mL");
        tick(helper, condenser, 120);
        helper.assertTrue(condenser.heldFormations() == 1, "no formation after one cycle");
        helper.assertTrue(condenser.getBloodVolume() == 625, "one formation did not cost 125 mL");
        tick(helper, condenser, 120 * 4);
        helper.assertTrue(condenser.heldFormations() == 4, "Latticed Condenser should hold exactly four");
        helper.assertTrue(condenser.getBloodVolume() == 250, "a full platter kept spending blood");
        owner.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "sanguine_condenser")
    public static void collectInsertsActuallyAndRecordsUsage(GameTestHelper helper) {
        ServerPlayer owner = StationUpgradeGameTests.claimant(helper, 3);
        SanguineCondenserBlockEntity condenser = condenser(helper, owner, 1);
        condenser.receiveProjectedBlood(owner, 750);
        tick(helper, condenser, 120);

        for (int slot = 0; slot < 36; slot++) owner.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
        helper.assertTrue(condenser.collect(owner) == 0 && condenser.heldFormations() == 1,
                "a full inventory took or lost the formation");
        owner.setGameMode(GameType.CREATIVE);
        helper.assertTrue(condenser.collect(owner) == 0 && condenser.heldFormations() == 1,
                "Creative discard counted as collection");
        owner.setGameMode(GameType.SURVIVAL);
        var progress = HemoCapabilityAccess.stationUpgrades(owner);
        helper.assertTrue(!progress.hasUsed(UpgradeStation.CONDENSER, StationUpgradeCatalog.CONDENSE),
                "usage recorded before any formation was collected");

        owner.getInventory().setItem(0, ItemStack.EMPTY);
        helper.assertTrue(condenser.collect(owner) == 1 && condenser.heldFormations() == 0,
                "collection with room did not move the formation");
        helper.assertTrue(owner.getInventory().countItem(ItemInit.sanguine_formation.get()) == 1,
                "the collected formation is not in the inventory");
        helper.assertTrue(progress.hasUsed(UpgradeStation.CONDENSER, StationUpgradeCatalog.CONDENSE)
                        && progress.hasUsed(UpgradeStation.CONDENSER, StationUpgradeCatalog.CONDENSE_LATTICED),
                "a Latticed collection did not record both usages");
        owner.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "sanguine_condenser")
    public static void vicarProofNeedsContribution(GameTestHelper helper) {
        ServerPlayer filler = StationUpgradeGameTests.claimant(helper, 1);
        ServerPlayer bystander = StationUpgradeGameTests.claimant(helper, 1);
        SanguineCondenserBlockEntity condenser = condenser(helper, filler, 1);
        condenser.receiveProjectedBlood(filler, 750);
        tick(helper, condenser, 120);

        helper.assertTrue(condenser.collect(bystander) == 1, "the bystander could not collect");
        tick(helper, condenser, 120);
        helper.assertTrue(!FirstBloodcraftAssignment.progress(bystander).formationProjected(),
                "collecting someone else's formation counted for the Vicar");
        helper.assertTrue(HemoCapabilityAccess.stationUpgrades(bystander)
                .hasUsed(UpgradeStation.CONDENSER, StationUpgradeCatalog.CONDENSE), "shared-machine practice did not count");
        helper.assertTrue(condenser.collect(filler) == 1, "the filler could not collect");
        helper.assertTrue(FirstBloodcraftAssignment.progress(filler).formationProjected(),
                "the contributor's collection did not count for the Vicar");
        filler.discard();
        bystander.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "sanguine_condenser")
    public static void emptyStageZeroRevertsUnlessRiteLocked(GameTestHelper helper) {
        ServerPlayer owner = StationUpgradeGameTests.claimant(helper, 1);
        SanguineCondenserBlockEntity locked = condenser(helper, owner, 0);
        tick(helper, locked, 5);
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(SEAT)).is(BlockInit.sanguine_condenser.get()),
                "a freshly seated, never-filled Condenser reverted before it could be used");
        locked.receiveProjectedBlood(owner, 100);
        locked.setRiteLocked(true);
        locked.getBloodCapability().setBloodVolume(0);
        helper.assertTrue(!locked.revertIfSpent(), "a rite-locked cauldron reverted mid-rite");
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(SEAT)).is(BlockInit.sanguine_condenser.get()),
                "a rite-locked cauldron reverted mid-rite");

        locked.setRiteLocked(false);
        locked.receiveProjectedBlood(owner, 100);
        helper.assertTrue(locked.drainBlood(100) == 100, "the unlocked cauldron would not give its blood up");
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(SEAT)).is(Blocks.CAULDRON),
                "an empty Sanguine Cauldron did not revert to a vanilla cauldron");
        owner.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "sanguine_condenser")
    public static void breakingDropsByStage(GameTestHelper helper) {
        ServerPlayer owner = StationUpgradeGameTests.claimant(helper, 3);
        SanguineCondenserBlockEntity cauldron = condenser(helper, owner, 1);
        cauldron.receiveProjectedBlood(owner, 750);
        tick(helper, cauldron, 120);
        helper.getLevel().setBlock(helper.absolutePos(SEAT), helper.getLevel().getBlockState(helper.absolutePos(SEAT))
                .setValue(StationTierProperty.STAGE, 0), 3);
        helper.getLevel().destroyBlock(helper.absolutePos(SEAT), true);
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(SEAT)).isAir(), "the cauldron was not broken");
        helper.assertTrue(count(helper, Items.CAULDRON) == 1, "stage 0 did not drop one vanilla cauldron");
        helper.assertTrue(count(helper, ItemInit.sanguine_formation.get()) == 1, "the held formation was not dropped");
        helper.assertTrue(count(helper, BlockInit.sanguine_condenser.get().asItem()) == 0, "stage 0 dropped a Condenser item");
        clearItems(helper);

        condenser(helper, owner, 1);
        helper.getLevel().destroyBlock(helper.absolutePos(SEAT), true);
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area(helper),
                item -> item.getItem().is(BlockInit.sanguine_condenser.get().asItem()));
        helper.assertTrue(drops.size() == 1, "Latticed Condenser did not drop itself");
        var stateComponent = drops.getFirst().getItem().get(DataComponents.BLOCK_STATE);
        helper.assertTrue(stateComponent != null && Integer.valueOf(1).equals(stateComponent.get(StationTierProperty.STAGE)),
                "the dropped Condenser lost its stage");
        helper.assertTrue(count(helper, Items.CAULDRON) == 0, "a Latticed Condenser also dropped a cauldron");
        owner.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "sanguine_condenser")
    public static void fullCondenserRefusesBlood(GameTestHelper helper) {
        ServerPlayer owner = StationUpgradeGameTests.claimant(helper, 1);
        SanguineCondenserBlockEntity condenser = condenser(helper, owner, 0);
        helper.assertTrue(condenser.receiveProjectedBlood(owner, 500) == 300, "the cauldron overfilled");
        helper.assertTrue(condenser.receiveProjectedBlood(owner, 10) == 0, "a full cauldron accepted more blood");
        owner.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "sanguine_condenser")
    public static void projectionConvertsEmptyCauldronAndFills(GameTestHelper helper) {
        ServerPlayer player = StationUpgradeGameTests.claimant(helper, 1);
        HemoCapabilityAccess.requireBloodVolume(player).setBloodVolume(1000);
        helper.getLevel().setBlock(helper.absolutePos(SUPPORT), Blocks.STONE.defaultBlockState(), 3);
        BlockPos seat = helper.absolutePos(SEAT);
        helper.getLevel().setBlock(seat, Blocks.CAULDRON.defaultBlockState(), 3);
        var spent = SanguineCondenserProjection.tryProject(helper.getLevel(), player, seat, 20);
        helper.assertTrue(spent.isPresent() && spent.getAsDouble() == 20, "projection into a cauldron was not handled");
        helper.assertTrue(helper.getLevel().getBlockEntity(seat) instanceof SanguineCondenserBlockEntity condenser
                && condenser.getBloodVolume() == 20 && condenser.isContributor(player.getUUID())
                && player.getUUID().equals(condenser.owner()), "the cauldron did not become an owned, filled Condenser");
        helper.assertTrue(HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume() == 980,
                "the projector was not charged for the blood");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "sanguine_condenser")
    public static void projectionIgnoresFilledCauldronsAndStone(GameTestHelper helper) {
        ServerPlayer player = StationUpgradeGameTests.claimant(helper, 1);
        HemoCapabilityAccess.requireBloodVolume(player).setBloodVolume(1000);
        BlockPos seat = helper.absolutePos(SEAT);
        for (var state : java.util.List.of(Blocks.WATER_CAULDRON.defaultBlockState(), Blocks.LAVA_CAULDRON.defaultBlockState(),
                Blocks.POWDER_SNOW_CAULDRON.defaultBlockState(), Blocks.STONE.defaultBlockState())) {
            helper.getLevel().setBlock(seat, state, 3);
            helper.assertTrue(SanguineCondenserProjection.tryProject(helper.getLevel(), player, seat, 20).isEmpty(),
                    state + " was claimed by the Condenser branch");
            helper.assertTrue(helper.getLevel().getBlockState(seat) == state, state + " changed");
        }
        helper.assertTrue(HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume() == 1000,
                "a refused target cost blood");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "sanguine_condenser_rite")
    public static void upgradePreservesBloodFormationsAndContributors(GameTestHelper helper) {
        ServerPlayer owner = StationUpgradeGameTests.claimant(helper, 3);
        var tier = StationUpgradeCatalog.get(UpgradeStation.CONDENSER, 1);
        var rite = StationUpgradeGameTests.upgradeRite(helper, tier);
        var level = helper.getLevel();
        var condenser = (SanguineCondenserBlockEntity) level.getBlockEntity(StationUpgradeRites.seat(rite));
        condenser.claimOwner(owner);
        condenser.receiveProjectedBlood(owner, 300);
        tick(helper, condenser, 200);
        int held = condenser.heldFormations();
        double blood = condenser.getBloodVolume();

        helper.assertTrue(StationUpgradeRites.prepare(level, rite), "First Clotting did not prepare");
        HemoCapabilityAccess.requireBloodVolume(owner).setBloodVolume(500);
        var spent = SanguineCondenserProjection.tryProject(level, owner, condenser.getBlockPos(), 20);
        helper.assertTrue(spent.isPresent() && spent.getAsDouble() == 0 && condenser.getBloodVolume() == blood,
                "a rite-locked Condenser took projected blood");
        helper.assertTrue(condenser.collect(owner) == 0, "a rite-locked Condenser gave up its formation");

        for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, 50);
        helper.assertTrue(rite.enterInscription() && rite.sealAltar(false), "First Clotting did not seal");
        for (int circuit = 0; circuit < tier.circuits(); circuit++) rite.fillUpgradeCircuit(circuit, tier.bloodPerCircuit());
        rite.markComplete();
        helper.assertTrue(StationUpgradeRites.complete(level, rite) && condenser.stage() == 1, "First Clotting did not complete");
        helper.assertTrue(condenser.heldFormations() == held && condenser.getBloodVolume() == blood
                        && condenser.isContributor(owner.getUUID()) && owner.getUUID().equals(condenser.owner()),
                "the upgrade lost blood, formations, owner or contributors");
        owner.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "sanguine_condenser")
    public static void hopperBeneathALatticedCondenserHoldsAndDraws(GameTestHelper helper) {
        ServerPlayer owner = StationUpgradeGameTests.claimant(helper, 3);
        SanguineCondenserBlockEntity condenser = condenser(helper, owner, 1);
        BlockPos hopper = helper.absolutePos(SUPPORT);
        helper.getLevel().setBlock(hopper, Blocks.HOPPER.defaultBlockState(), 3);
        helper.getLevel().setBlock(hopper, Blocks.HOPPER.defaultBlockState()
                .setValue(net.minecraft.world.level.block.HopperBlock.ENABLED, false), 3);
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(SEAT)).is(BlockInit.sanguine_condenser.get()),
                "a hopper beneath knocked the Condenser off");
        condenser.receiveProjectedBlood(owner, 750);
        tick(helper, condenser, 120);
        var handler = helper.getLevel().getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                helper.absolutePos(SEAT), net.minecraft.core.Direction.DOWN);
        helper.assertTrue(handler != null && handler.extractItem(0, 1, false).is(ItemInit.sanguine_formation.get()),
                "a hopper beneath could not draw the formation");
        owner.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "sanguine_condenser")
    public static void absorptionRespectsTheRiteLockAndEmptiesTheCauldron(GameTestHelper helper) {
        ServerPlayer owner = StationUpgradeGameTests.claimant(helper, 1);
        SanguineCondenserBlockEntity condenser = condenser(helper, owner, 0);
        condenser.receiveProjectedBlood(owner, 100);
        var blood = HemoCapabilityAccess.requireBloodVolume(owner);
        blood.setBloodVolume(0);
        BlockPos seat = helper.absolutePos(SEAT);
        owner.moveTo(seat.getX() + .5, seat.getY(), seat.getZ() - 1.5, 0.0F, 0.0F);
        owner.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,
                new net.minecraft.world.phys.Vec3(seat.getX() + .5, seat.getY() + .3, seat.getZ() + .5));
        var aim = owner.pick(5.5D, 0.0F, true);
        helper.assertTrue(aim instanceof net.minecraft.world.phys.BlockHitResult hit && hit.getBlockPos().equals(seat),
                "the absorbing player is not looking into the Condenser: " + aim.getType() + " "
                        + (aim instanceof net.minecraft.world.phys.BlockHitResult hit ? hit.getBlockPos() : "") + " seat " + seat);
        condenser.setRiteLocked(true);
        helper.assertTrue(com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodInteractions
                        .tryAbsorbFromLookedAtBlock(helper.getLevel(), owner, 500) == 0 && condenser.getBloodVolume() == 100,
                "absorption drew from a rite-locked Condenser");
        condenser.setRiteLocked(false);
        helper.assertTrue(com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodInteractions
                .tryAbsorbFromLookedAtBlock(helper.getLevel(), owner, 500) == 100, "absorption did not empty the cauldron");
        tick(helper, condenser, 1);
        helper.assertTrue(helper.getLevel().getBlockState(seat).is(Blocks.CAULDRON),
                "a cauldron emptied by absorption did not revert");
        owner.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "sanguine_condenser")
    public static void onlyProjectionFillsACondenser(GameTestHelper helper) {
        ServerPlayer owner = StationUpgradeGameTests.claimant(helper, 1);
        SanguineCondenserBlockEntity condenser = condenser(helper, owner, 0);
        helper.assertTrue(!condenser.canReceiveBlood() && condenser.receiveBlood(100) == 0,
                "routing, couriers or generic transfers could fill a Condenser");
        helper.assertTrue(condenser.receiveProjectedBlood(owner, 100) == 100, "projection no longer fills it");
        owner.discard();
        helper.succeed();
    }

    static SanguineCondenserBlockEntity condenser(GameTestHelper helper, ServerPlayer owner, int stage) {
        helper.getLevel().setBlock(helper.absolutePos(SUPPORT), Blocks.STONE.defaultBlockState(), 3);
        BlockPos seat = helper.absolutePos(SEAT);
        helper.getLevel().setBlock(seat, Blocks.CAULDRON.defaultBlockState(), 3);
        SanguineCondenserBlockEntity condenser = SanguineCondenserBlock.convertCauldron(helper.getLevel(), seat, owner);
        helper.assertTrue(condenser != null, "an empty cauldron did not convert");
        if (stage > 0) helper.getLevel().setBlock(seat, helper.getLevel().getBlockState(seat)
                .setValue(StationTierProperty.STAGE, stage), 3);
        return condenser;
    }

    static void tick(GameTestHelper helper, SanguineCondenserBlockEntity condenser, int ticks) {
        for (int i = 0; i < ticks && !condenser.isRemoved(); i++)
            SanguineCondenserBlockEntity.serverTick(helper.getLevel(), condenser.getBlockPos(),
                    condenser.getBlockState(), condenser);
    }

    private static AABB area(GameTestHelper helper) {
        return new AABB(helper.absolutePos(SEAT)).inflate(2.0D);
    }

    private static long count(GameTestHelper helper, net.minecraft.world.item.Item item) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, area(helper), entity -> entity.getItem().is(item))
                .stream().mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    private static void clearItems(GameTestHelper helper) {
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, area(helper)).forEach(ItemEntity::discard);
    }
}
