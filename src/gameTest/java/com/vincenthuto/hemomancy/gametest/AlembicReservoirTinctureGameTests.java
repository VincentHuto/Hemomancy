package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.brewing.AlembicTier;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.GhastlyAlembicMenu;
import com.vincenthuto.hemomancy.common.recipe.DistillationRecipe;
import com.vincenthuto.hemomancy.common.recipe.serializer.DistillationRecipeSerializer;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("distillation_validation")
@PrefixGameTestTemplate(false)
public final class AlembicReservoirTinctureGameTests {
    @GameTest(template = "empty")
    public static void condenserCapacityAcceptsThreeFlasksAndSurvivesAthanorAndReload(GameTestHelper helper) {
        var machine = machine(helper, 4000, false);
        helper.assertTrue(machine.getMaxBloodVolume() == 5000, "Base Alembic must hold 5000 mL");
        machine.setTier(AlembicTier.CONDENSER);
        helper.assertTrue(machine.getMaxBloodVolume() == 7500 && machine.getBloodVolume() == 4000,
                "Condenser must increase capacity by 1.5x without changing stored blood");
        machine.clearContent();
        HemoCapabilityAccess.getBloodVolume(machine).orElseThrow().setBloodVolume(0);
        for (int flask = 0; flask < 3; flask++) {
            machine.setItem(1, new ItemStack(ItemInit.bloody_flask.get()));
            tick(helper, machine, 1);
            helper.assertTrue(machine.getItem(1).isEmpty()
                    && machine.getItem(4).is(HLItemInit.cured_clay_flask.get()),
                    "Expanded tank must accept all three filled flasks and return their vessels");
            machine.removeItemNoUpdate(4);
        }
        helper.assertTrue(machine.getBloodVolume() == 7500, "Three flasks must fill the expanded tank");
        machine.setItem(1, new ItemStack(ItemInit.bloody_flask.get()));
        tick(helper, machine, 1);
        helper.assertTrue(machine.getBloodVolume() == 7500 && machine.getItem(1).is(ItemInit.bloody_flask.get())
                && machine.getItem(4).isEmpty(), "Full expanded tank must reject another flask without consuming it");
        assertReloadCapacity(helper, machine);
        machine.setTier(AlembicTier.ATHANOR);
        helper.assertTrue(machine.getMaxBloodVolume() == 7500 && machine.getBloodVolume() == 7500,
                "Athanor must retain the Condenser's capacity and blood");
        assertReloadCapacity(helper, machine);
        helper.succeed();
    }

    private static void assertReloadCapacity(GameTestHelper helper, GhastlyAlembicBlockEntity machine) {
        var saved = machine.saveWithoutMetadata(helper.getLevel().registryAccess());
        var reloaded = new GhastlyAlembicBlockEntity(machine.getBlockPos(), machine.getBlockState());
        reloaded.setLevel(helper.getLevel());
        reloaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        reloaded.onLoad();
        reloaded.onLoad();
        helper.assertTrue(reloaded.getMaxBloodVolume() == 7500 && reloaded.getBloodVolume() == 7500
                && reloaded.getItem(1).is(ItemInit.bloody_flask.get()),
                "Reload must preserve expanded capacity, stored blood, and inventory without multiplying again");
    }

    @GameTest(template = "empty")
    public static void ordinaryDistillationAndStandaloneBottlingKeepTheirBloodRules(GameTestHelper helper) {
        var machine = machine(helper, 5000, false);
        machine.setItem(0, new ItemStack(BlockInit.devils_tooth.get()));
        machine.setItem(1, ItemStack.EMPTY);
        machine.setItem(3, ItemStack.EMPTY);
        tick(helper, machine, 100);
        helper.assertTrue(machine.getItem(2).isEmpty() && machine.getBloodVolume() == 5000
                && machine.getProcessingStatus() == GhastlyAlembicBlockEntity.Status.TANK_FULL,
                "Ordinary distillation must still wait for byproduct capacity");
        HemoCapabilityAccess.getBloodVolume(machine).orElseThrow().setBloodVolume(4800);
        tick(helper, machine, 100);
        helper.assertTrue(machine.getItem(2).is(ItemInit.foul_paste.get()) && machine.getItem(2).getCount() == 2
                && machine.getBloodVolume() == 4900, "Ordinary distillation must generate exactly 100 mL");
        machine.setItem(1, new ItemStack(HLItemInit.cured_clay_flask.get()));
        tick(helper, machine, 1);
        helper.assertTrue(machine.getBloodVolume() == 2400 && machine.getItem(1).isEmpty()
                && machine.getItem(4).is(ItemInit.bloody_flask.get()), "Standalone bottling must still use the container area");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyTinctureRecipeUsesReservoirAndEmptyPackaging(GameTestHelper helper) {
        String[] memories = {"sanguine_fists", "laboring_arms", "coursing_legs", "hushed_gait",
                "predatory_eyes", "second_pulse", "enduring_viscera", "carrion_metabolism"};
        String[] enzymes = {"fervent", "ferric", "neurotic", "umbral", "incandescent", "vivacious", "frigid", "ruinous"};
        for (int i = 0; i < memories.length; i++) for (boolean jug : new boolean[]{false, true}) {
            var machine = machine(helper, 5000, jug);
            machine.setItem(3, new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(
                    "hemomancy:" + enzymes[i] + "_enzyme"))));
            tick(helper, machine, jug ? 300 : 200);
            helper.assertTrue(machine.getItem(2).is(BuiltInRegistries.ITEM.get(ResourceLocation.parse(
                    "hemomancy:tincture_" + memories[i] + (jug ? "_jug" : ""))))
                    && machine.getBloodVolume() == (jug ? 0 : 2500)
                    && machine.getItem(0).isEmpty() && machine.getItem(1).isEmpty() && machine.getItem(3).isEmpty(),
                    "Tincture " + memories[i] + " must charge its reservoir cost and consume empty packaging");
            helper.getLevel().removeBlock(machine.getBlockPos(), false);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void recipeNetworkRoundTripKeepsBloodCostAndVesselMatch(GameTestHelper helper) {
        var recipe = (DistillationRecipe) helper.getLevel().getRecipeManager().byKey(ResourceLocation.parse(
                "hemomancy:distillation/tincture_sanguine_fists_jug")).orElseThrow().value();
        var buffer = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            DistillationRecipeSerializer.STREAM_CODEC.encode(buffer, recipe);
            var decoded = DistillationRecipeSerializer.STREAM_CODEC.decode(buffer);
            helper.assertTrue(decoded.getBloodCost() == 5000 && decoded.matchesItems(
                    new ItemStack(ItemInit.sanguine_formation.get()), new ItemStack(ItemInit.fervent_enzyme.get()),
                    new ItemStack(ItemInit.cured_clay_jug.get())) && !decoded.matchesItems(
                    new ItemStack(ItemInit.sanguine_formation.get()), new ItemStack(ItemInit.fervent_enzyme.get()),
                    new ItemStack(ItemInit.bloody_jug.get())), "Recipe sync must preserve tank cost and empty-vessel requirement");
        } finally { buffer.release(); }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void retiredSlotRecoversAndSecondCatalystSurvivesReload(GameTestHelper helper) {
        var machine = machine(helper, 3500, false);
        machine.setTier(AlembicTier.ATHANOR);
        helper.assertTrue(machine.tier() == AlembicTier.ATHANOR, "Fixture must have room for Athanor");
        var saved = machine.saveWithoutMetadata(helper.getLevel().registryAccess());
        saved.remove("ReservoirTinctures");
        var legacy = NonNullList.withSize(7, ItemStack.EMPTY);
        legacy.set(0, new ItemStack(ItemInit.sanguine_formation.get()));
        legacy.set(5, new ItemStack(ItemInit.bloody_jug.get(), 2));
        legacy.set(6, new ItemStack(Items.POTION));
        ContainerHelper.saveAllItems(saved, legacy, helper.getLevel().registryAccess());
        machine.loadWithComponents(saved, helper.getLevel().registryAccess());
        var current = machine.saveWithoutMetadata(helper.getLevel().registryAccess());
        machine.loadWithComponents(current, helper.getLevel().registryAccess());
        helper.assertTrue(machine.getBloodVolume() == 3500 && machine.getContainerSize() == 6
                && machine.getItem(5).is(Items.POTION) && machine.getItem(0).is(ItemInit.sanguine_formation.get()),
                "Slot compaction must preserve inventory, blood, and installed second catalyst across reloads");
        var recovered = machine.takeHiddenCatalystRecovery();
        helper.assertTrue(recovered.is(ItemInit.bloody_jug.get()) && recovered.getCount() == 2
                && machine.takeHiddenCatalystRecovery().isEmpty(), "Retired blood-slot contents must recover exactly once");
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new GhastlyAlembicMenu(1, player.getInventory(), machine);
        player.getInventory().setItem(9, new ItemStack(ItemInit.cured_clay_jug.get()));
        menu.quickMoveStack(player, 6);
        helper.assertTrue(menu.slots.size() == 42 && machine.getItem(1).is(ItemInit.cured_clay_jug.get())
                && machine.getItem(5).is(Items.POTION), "Compacted menu must route shift-click packaging without moving Catalyst 2");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void flaskTinctureUsesTankAndExistingContainerSlot(GameTestHelper helper) {
        var machine = machine(helper, 2600, false);
        tick(helper, machine, 199);
        helper.assertTrue(machine.getItem(2).isEmpty() && machine.getBloodVolume() == 2600
                && machine.getItem(1).is(HLItemInit.cured_clay_flask.get()),
                "Pending tincture must reserve its empty flask without auto-bottling or spending blood");
        tick(helper, machine, 1);
        helper.assertTrue(machine.getItem(2).is(ItemInit.tincture_sanguine_fists.get())
                && machine.getItem(0).isEmpty() && machine.getItem(1).isEmpty()
                && machine.getItem(3).isEmpty() && machine.getItem(4).isEmpty()
                && machine.getBloodVolume() == 100,
                "Flask tincture must consume its packaging and reagents and debit exactly 2500 tank blood");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void jugTinctureCanRunAtFullTank(GameTestHelper helper) {
        var machine = machine(helper, 5000, true);
        tick(helper, machine, 299);
        helper.assertTrue(machine.getItem(2).isEmpty() && machine.getBloodVolume() == 5000,
                "Jug tincture must wait for its complete processing time");
        tick(helper, machine, 1);
        helper.assertTrue(machine.getItem(2).is(ItemInit.tincture_sanguine_fists_jug.get())
                && machine.getItem(1).isEmpty() && machine.getBloodVolume() == 0,
                "Full tank must permit a paid jug tincture without generating blood byproduct");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void insufficientBloodAndBlockedOutputConsumeNothing(GameTestHelper helper) {
        var machine = machine(helper, 2499, false);
        tick(helper, machine, 220);
        assertUnspent(helper, machine, 2499);
        HemoCapabilityAccess.getBloodVolume(machine).orElseThrow().setBloodVolume(2500);
        machine.setItem(2, new ItemStack(Items.STONE));
        tick(helper, machine, 220);
        assertUnspent(helper, machine, 2500);
        machine.setItem(2, ItemStack.EMPTY);
        tick(helper, machine, 199);
        HemoCapabilityAccess.getBloodVolume(machine).orElseThrow().setBloodVolume(2499);
        tick(helper, machine, 1);
        assertUnspent(helper, machine, 2499);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void filledFlaskStillSuppliesTankAndThenPackagesTincture(GameTestHelper helper) {
        var machine = machine(helper, 0, false);
        machine.setItem(1, new ItemStack(ItemInit.bloody_flask.get()));
        tick(helper, machine, 1);
        helper.assertTrue(machine.getBloodVolume() == 2500 && machine.getItem(1).isEmpty()
                && machine.getItem(4).is(HLItemInit.cured_clay_flask.get()),
                "Existing container transfer must fill tank and return the empty flask");
        machine.setItem(1, machine.removeItemNoUpdate(4));
        tick(helper, machine, 200);
        helper.assertTrue(machine.getItem(2).is(ItemInit.tincture_sanguine_fists.get())
                && machine.getBloodVolume() == 0,
                "Returned packaging must work with blood now stored in the tank");
        helper.succeed();
    }

    private static GhastlyAlembicBlockEntity machine(GameTestHelper helper, int blood, boolean jug) {
        var pos = helper.absolutePos(new BlockPos(2, 3, 2));
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) for (int y = 0; y < 3; y++)
            helper.getLevel().setBlockAndUpdate(pos.offset(x, y, z), Blocks.AIR.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.below(), Blocks.MAGMA_BLOCK.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos, BlockInit.ghastly_alembic.get().defaultBlockState());
        var machine = (GhastlyAlembicBlockEntity) helper.getLevel().getBlockEntity(pos);
        machine.onLoad();
        HemoCapabilityAccess.getBloodVolume(machine).orElseThrow().setBloodVolume(blood);
        machine.setItem(0, new ItemStack(ItemInit.sanguine_formation.get()));
        machine.setItem(3, new ItemStack(ItemInit.fervent_enzyme.get()));
        machine.setItem(1, new ItemStack(jug ? ItemInit.cured_clay_jug.get() : HLItemInit.cured_clay_flask.get()));
        return machine;
    }

    private static void tick(GameTestHelper helper, GhastlyAlembicBlockEntity machine, int ticks) {
        for (int i = 0; i < ticks; i++) GhastlyAlembicBlockEntity.serverTick(helper.getLevel(),
                machine.getBlockPos(), machine.getBlockState(), machine);
    }

    private static void assertUnspent(GameTestHelper helper, GhastlyAlembicBlockEntity machine, int blood) {
        helper.assertTrue(machine.getBloodVolume() == blood
                && machine.getItem(0).is(ItemInit.sanguine_formation.get())
                && machine.getItem(1).is(HLItemInit.cured_clay_flask.get())
                && machine.getItem(3).is(ItemInit.fervent_enzyme.get())
                && machine.getItem(4).isEmpty(), "Rejected recipe must retain blood, reagents, and packaging");
    }
}
