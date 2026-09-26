package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.crafting.MycelialCrucibleBlock;
import com.vincenthuto.hemomancy.common.event.MachineAccessEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.crafting.MycelialCrucibleBlockItem;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MycelialCrucibleBlockEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.stats.Stats;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder("registration_validation")
@PrefixGameTestTemplate(false)
public final class RegistrationGameTests {
    @GameTest(template = "empty")
    public static void blockItemExceptionsKeepTheirOwners(GameTestHelper helper) {
        int custom = 0, manual = 0, itemless = 0;
        for (var entry : BlockInit.getAllBlockEntriesAsStream().toList()) {
            var decision = BlockInit.itemDecision(entry.getId());
            if (decision.itemRoute() == BlockInit.ItemRoute.CUSTOM) custom++;
            if (decision.itemRoute() == BlockInit.ItemRoute.MANUAL) manual++;
            if (decision.itemRoute() == BlockInit.ItemRoute.NONE) itemless++;
            if (decision.itemId() != null) {
                helper.assertTrue(BuiltInRegistries.ITEM.containsKey(decision.itemId()),
                        "Block item owner is missing for " + entry.getId());
            }
        }
        helper.assertTrue(custom == 19 && manual == 6 && itemless == 7,
                "Block item exception counts changed without an explicit decision");
        BlockInit.POTTEDBLOCKS.getEntries().forEach(entry -> helper.assertTrue(
                BlockInit.itemDecision(entry.getId()).itemRoute() == BlockInit.ItemRoute.NONE,
                "Potted block unexpectedly gained an item: " + entry.getId()));
        BlockInit.LIQUIDBLOCKS.getEntries().forEach(entry -> helper.assertTrue(
                BlockInit.itemDecision(entry.getId()).itemRoute() == BlockInit.ItemRoute.NONE,
                "Liquid block unexpectedly gained an item: " + entry.getId()));
        var conduit = BlockInit.itemDecision(BlockInit.sanguine_conduit.getId());
        helper.assertTrue(conduit.itemRoute() == BlockInit.ItemRoute.MANUAL
                && conduit.itemId().equals(BlockInit.sanguine_conduit.getId()),
                "Sanguine Conduit must retain its same-ID manual item");
        var seeds = BlockInit.itemDecision(BlockInit.gourd_stem.getId());
        helper.assertTrue(seeds.itemRoute() == BlockInit.ItemRoute.MANUAL
                && seeds.itemId().getPath().equals("gourd_seeds"),
                "Gourd stem must remain seed-placed");
        var crucible = BlockInit.itemDecision(BlockInit.mycelial_crucible.getId());
        helper.assertTrue(crucible.itemRoute() == BlockInit.ItemRoute.CUSTOM
                && crucible.itemId().equals(BlockInit.mycelial_crucible.getId()),
                "Crucible must keep its custom same-ID BlockItem");
        helper.assertTrue(BlockInit.mycelial_crucible.get().asItem() instanceof MycelialCrucibleBlockItem
                && ItemInit.lethean_poppy_wreath.get().getDefaultMaxStackSize() == 16,
                "Crucible item class or Lethean wreath stack limit changed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void survivalBreakDropsCrucibleAndContentsOnce(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
        var block = (MycelialCrucibleBlock) BlockInit.mycelial_crucible.get();
        var state = block.defaultBlockState();
        level.setBlockAndUpdate(pos, state);
        block.placeFillers(level, pos, state);
        var crucible = (MycelialCrucibleBlockEntity) level.getBlockEntity(pos);
        crucible.setItem(MycelialCrucibleBlockEntity.SLOT_OUTPUT, new ItemStack(Items.DIAMOND, 3));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "crucible-break"));
        player.setGameMode(GameType.SURVIVAL);
        player.getStats().setValue(player, Stats.ITEM_CRAFTED.get(block.asItem()), 1);
        helper.assertTrue(MachineAccessEvents.hasPersonalAccess(player, block),
                "Crafted Crucible ownership was not recorded for the survival player");
        player.setPos(pos.getCenter());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.NETHERITE_PICKAXE));
        helper.assertTrue(player.gameMode.destroyBlock(pos), "Survival player could not break the Crucible");
        int blocks = 0;
        int contents = 0;
        for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2))) {
            if (drop.getItem().is(BlockInit.mycelial_crucible.get().asItem())) blocks += drop.getItem().getCount();
            if (drop.getItem().is(Items.DIAMOND)) contents += drop.getItem().getCount();
        }
        helper.assertTrue(blocks == 1 && contents == 3,
                "Survival break must drop one Crucible and its three stored diamonds exactly once");
        helper.succeed();
    }
}
