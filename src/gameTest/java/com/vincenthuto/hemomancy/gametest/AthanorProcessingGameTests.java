package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.brewing.AlembicTier;
import com.vincenthuto.hemomancy.common.brewing.BrewingResolver;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.GhastlyAlembicMenu;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("distillation_validation")
@PrefixGameTestTemplate(false)
public final class AthanorProcessingGameTests {
    @GameTest(template = "empty")
    public static void waterloggingDoesNotExtinguishAthanorHeat(GameTestHelper helper) {
        var machine = machine(helper, AlembicTier.ATHANOR, 0);
        tick(helper, machine, 1);
        var state = machine.getBlockState();
        var block = (net.minecraft.world.level.block.SimpleWaterloggedBlock) state.getBlock();
        helper.assertTrue(block.placeLiquid(helper.getLevel(), machine.getBlockPos(), state,
                net.minecraft.world.level.material.Fluids.WATER.getSource(false)), "Athanor must accept waterlogging");
        helper.assertTrue(machine.getBlockState().getValue(AbstractFurnaceBlock.LIT),
                "Waterlogging must not extinguish permanent Athanor heat");
        tick(helper, machine, 1);
        helper.assertTrue(machine.getBlockState().getValue(AbstractFurnaceBlock.LIT)
                && GhastlyAlembicBlockEntity.isHeatSource(helper.getLevel(), machine.getBlockPos()),
                "Waterlogged Athanor must keep its heat on subsequent ticks");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lowerTiersStillNeedExternalHeatAndOrdinaryTiming(GameTestHelper helper) {
        for (var tier : new AlembicTier[]{AlembicTier.BASE, AlembicTier.CONDENSER}) {
            var machine = machine(helper, tier, 0);
            machine.setItem(0, new ItemStack(BlockInit.devils_tooth.get()));
            tick(helper, machine, 110);
            helper.assertTrue(machine.getItem(2).isEmpty()
                    && machine.getProcessingStatus() == GhastlyAlembicBlockEntity.Status.NO_HEAT,
                    tier + " must still require external heat");
            helper.getLevel().setBlockAndUpdate(machine.getBlockPos().below(), Blocks.MAGMA_BLOCK.defaultBlockState());
            tick(helper, machine, 99);
            helper.assertTrue(machine.getItem(2).isEmpty(), tier + " must retain the original 100-tick duration");
            tick(helper, machine, 1);
            helper.assertTrue(machine.getItem(2).is(ItemInit.foul_paste.get()) && machine.getBloodVolume() == 100,
                    tier + " must finish at 100 ticks with unchanged blood byproduct");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void athanorDistilsAtDoubleSpeedWithoutABlockBelow(GameTestHelper helper) {
        var machine = machine(helper, AlembicTier.ATHANOR, 0);
        machine.setItem(0, new ItemStack(BlockInit.devils_tooth.get(), 2));
        tick(helper, machine, 49);
        helper.assertTrue(machine.getItem(2).isEmpty() && machine.getBloodVolume() == 0
                && helper.getLevel().getBlockState(machine.getBlockPos()).getValue(AbstractFurnaceBlock.LIT),
                "Athanor must heat itself without producing an early result");
        tick(helper, machine, 1);
        helper.assertTrue(machine.getItem(2).is(ItemInit.foul_paste.get()) && machine.getItem(2).getCount() == 2
                && machine.getItem(0).getCount() == 1 && machine.getBloodVolume() == 100,
                "Athanor must finish ordinary distillation at 50 ticks with normal consumption");
        helper.getLevel().setBlockAndUpdate(machine.getBlockPos().below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(machine.getBlockPos().below(), Blocks.AIR.defaultBlockState());
        helper.assertTrue(helper.getLevel().getBlockState(machine.getBlockPos()).getValue(AbstractFurnaceBlock.LIT),
                "Neighbor changes must not extinguish the Athanor's permanent heat");
        tick(helper, machine, 50);
        helper.assertTrue(machine.getItem(2).getCount() == 4 && machine.getItem(0).isEmpty()
                && machine.getBloodVolume() == 200, "Repeated batches must retain double speed and normal yields");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void athanorTincturesHalveTimeAndKeepTheirReservoirCosts(GameTestHelper helper) {
        for (boolean jug : new boolean[]{false, true}) {
            int cost = jug ? 5000 : 2500;
            int ticks = jug ? 150 : 100;
            var machine = machine(helper, AlembicTier.ATHANOR, cost + 100);
            machine.setItem(0, new ItemStack(ItemInit.sanguine_formation.get()));
            machine.setItem(3, new ItemStack(ItemInit.fervent_enzyme.get()));
            machine.setItem(1, new ItemStack(jug ? ItemInit.cured_clay_jug.get() : HLItemInit.cured_clay_flask.get()));
            tick(helper, machine, ticks / 2);
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            var menu = (GhastlyAlembicMenu) machine.createMenu(1, player.getInventory(), player);
            helper.assertTrue(machine.advancedTotalTicks() == ticks && menu.getBurnProgress() == 12 && menu.isHeated(),
                    "Tincture time preview, progress bar, and heat indicator must agree with Athanor speed");
            tick(helper, machine, ticks - ticks / 2 - 1);
            helper.assertTrue(machine.getItem(2).isEmpty() && machine.getBloodVolume() == cost + 100,
                    "Tinctures must spend nothing before their shortened duration completes");
            tick(helper, machine, 1);
            helper.assertTrue(machine.getItem(2).is(jug ? ItemInit.tincture_sanguine_fists_jug.get()
                    : ItemInit.tincture_sanguine_fists.get()) && machine.getBloodVolume() == 100
                    && machine.getItem(0).isEmpty() && machine.getItem(1).isEmpty() && machine.getItem(3).isEmpty(),
                    "Faster tinctures must retain their output, blood cost, and vessel consumption");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyAdvancedOperationRunsAtDoubleSpeedWithoutExternalHeat(GameTestHelper helper) {
        ItemStack strength = PotionContents.createItemStack(Items.POTION, Potions.STRENGTH);
        ItemStack speed = PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS);
        ItemStack fire = PotionContents.createItemStack(Items.POTION, Potions.FIRE_RESISTANCE);
        var bound = BrewingResolver.resolve(helper.getLevel(), AlembicTier.ATHANOR, strength, speed, fire).result();
        ItemStack[][] operations = {
                {strength, new ItemStack(ItemInit.ferric_enzyme.get()), ItemStack.EMPTY},
                {speed, new ItemStack(ItemInit.neurotic_enzyme.get()), ItemStack.EMPTY},
                {fire, new ItemStack(ItemInit.fervent_enzyme.get()), ItemStack.EMPTY},
                {strength, speed, ItemStack.EMPTY},
                {strength, speed, fire},
                {bound, speed, fire}
        };
        for (var ingredients : operations) {
            var candidate = BrewingResolver.resolve(helper.getLevel(), AlembicTier.ATHANOR,
                    ingredients[0], ingredients[1], ingredients[2]);
            helper.assertTrue(candidate != null, "Advanced timing fixture must resolve a real formula");
            int ticks = (candidate.ticks() + 1) / 2;
            var machine = machine(helper, AlembicTier.ATHANOR, 2000);
            machine.setItem(0, ingredients[0].copy());
            machine.setItem(3, ingredients[1].copy());
            machine.setItem(5, ingredients[2].copy());
            tick(helper, machine, ticks / 2);
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            var menu = (GhastlyAlembicMenu) machine.createMenu(1, player.getInventory(), player);
            helper.assertTrue(machine.advancedTotalTicks() == ticks && menu.getBurnProgress() == 12 && menu.isHeated(),
                    candidate.operation() + " must show the shortened time and correct progress");
            tick(helper, machine, ticks - ticks / 2 - 1);
            helper.assertTrue(machine.getItem(2).isEmpty() && machine.getBloodVolume() == 2000,
                    candidate.operation() + " must not consume blood or produce an early result");
            tick(helper, machine, 1);
            helper.assertTrue(ItemStack.isSameItemSameComponents(machine.getItem(2), candidate.result())
                    && machine.getBloodVolume() == 2000 - candidate.blood()
                    && machine.getItem(0).isEmpty() && machine.getItem(3).isEmpty() && machine.getItem(5).isEmpty(),
                    candidate.operation() + " must complete at half time with unchanged cost and formula");
        }
        helper.succeed();
    }

    private static GhastlyAlembicBlockEntity machine(GameTestHelper helper, AlembicTier tier, int blood) {
        var pos = helper.absolutePos(new BlockPos(4, 3, 4));
        AlembicFootprintGameTests.clearSpace(helper, pos);
        helper.getLevel().setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
        helper.getLevel().removeBlock(pos, false);
        helper.getLevel().setBlockAndUpdate(pos, BlockInit.ghastly_alembic.get().defaultBlockState());
        var machine = (GhastlyAlembicBlockEntity) helper.getLevel().getBlockEntity(pos);
        machine.onLoad();
        machine.setTier(tier);
        helper.assertTrue(machine.tier() == tier, "Fixture must have room for " + tier);
        machine.getBloodCapability().setBloodVolume(blood);
        return machine;
    }

    private static void tick(GameTestHelper helper, GhastlyAlembicBlockEntity machine, int ticks) {
        for (int i = 0; i < ticks; i++) GhastlyAlembicBlockEntity.serverTick(helper.getLevel(),
                machine.getBlockPos(), machine.getBlockState(), machine);
    }
}
