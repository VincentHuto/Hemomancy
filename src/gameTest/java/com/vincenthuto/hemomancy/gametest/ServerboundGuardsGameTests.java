package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.ScarStationMenu;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The serverbound guards accept what a nearby player really has open or in reach, and nothing else. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ServerboundGuardsGameTests {
    private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

    private ServerboundGuardsGameTests() {}

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void menuGuardMatchesOnlyTheOpenMenuType(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(ServerboundGuards.menu(player, InventoryMenu.class) == player.inventoryMenu,
                "The player's own open inventory must pass the menu guard");
        helper.assertTrue(ServerboundGuards.menu(player, ScarStationMenu.class) == null,
                "A menu the player does not have open must fail the guard");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void blockGuardsRequireTheBlockWithinReach(GameTestHelper helper) {
        BlockPos chest = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(chest, Blocks.CHEST.defaultBlockState());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);

        player.moveTo(chest.getX() + 0.5D, chest.getY(), chest.getZ() + 6.5D);
        helper.assertTrue(ServerboundGuards.nearBlock(player, chest, Blocks.CHEST, ServerboundGuards.BLOCK_REACH_SQR),
                "A chest six blocks away is in reach");
        helper.assertTrue(!ServerboundGuards.nearBlock(player, chest, Blocks.FURNACE, ServerboundGuards.BLOCK_REACH_SQR),
                "The block must be the expected one");
        helper.assertTrue(ServerboundGuards.nearBlockEntity(player, chest, ChestBlockEntity.class,
                ServerboundGuards.BLOCK_REACH_SQR) != null, "The chest's block entity is in reach");
        helper.assertTrue(ServerboundGuards.nearBlockEntity(player, chest, FurnaceBlockEntity.class,
                ServerboundGuards.BLOCK_REACH_SQR) == null, "The block entity must be the expected type");
        helper.assertTrue(!ServerboundGuards.withinReach(player, null, ServerboundGuards.BLOCK_REACH_SQR),
                "A missing position is never in reach");

        player.moveTo(chest.getX() + 0.5D, chest.getY(), chest.getZ() + 9.5D);
        helper.assertTrue(!ServerboundGuards.nearBlock(player, chest, Blocks.CHEST, ServerboundGuards.BLOCK_REACH_SQR),
                "A chest nine blocks away is out of reach");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void entityGuardRequiresALiveEntityWithinDistance(GameTestHelper helper) {
        var cow = helper.spawn(EntityType.COW, new BlockPos(1, 1, 1));
        cow.setNoAi(true);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);

        player.moveTo(cow.getX(), cow.getY(), cow.getZ() + 7.0D);
        helper.assertTrue(ServerboundGuards.nearEntity(player, cow.getId(), 8.0D) == cow, "A cow seven blocks away is near");
        player.moveTo(cow.getX(), cow.getY(), cow.getZ() + 9.0D);
        helper.assertTrue(ServerboundGuards.nearEntity(player, cow.getId(), 8.0D) == null, "A cow nine blocks away is not");
        helper.assertTrue(ServerboundGuards.nearEntity(player, Integer.MAX_VALUE, 8.0D) == null, "An unknown id is not near");
        cow.discard();
        helper.succeed();
    }
}
