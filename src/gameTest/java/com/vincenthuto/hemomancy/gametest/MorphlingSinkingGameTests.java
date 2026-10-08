package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MorphlingSinkingGameTests {
    private MorphlingSinkingGameTests() {}

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 100)
    public static void idleMorphlingSinksToFloor(GameTestHelper helper) {
        checkSinking(helper, false);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 100)
    public static void morphlingSinksWithTargetAbove(GameTestHelper helper) {
        checkSinking(helper, true);
    }

    private static void checkSinking(GameTestHelper helper, boolean targetAbove) {
        for (BlockPos pos : BlockPos.betweenClosed(0, 0, 0, 8, 8, 8)) {
            boolean wall = pos.getY() == 0 || pos.getX() == 0 || pos.getX() == 8
                    || pos.getZ() == 0 || pos.getZ() == 8;
            helper.setBlock(pos, wall ? Blocks.STONE : Blocks.WATER);
        }
        MorphlingPolypEntity polyp = helper.spawn(EntityInit.morphling_polyp.get(), new BlockPos(4, 5, 4));
        double startY = polyp.getY();
        if (targetAbove) {
            ArmorStand target = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(4, 7, 4));
            target.setNoGravity(true);
            polyp.setTarget(target);
        }
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(polyp.isInWater(), "Morphling left the water fixture");
            helper.assertTrue(polyp.getY() < startY - 0.4D,
                    "Morphling floated instead of sinking: start=" + startY + ", current=" + polyp.getY());
        });
        helper.runAfterDelay(80, () -> {
            helper.assertTrue(polyp.onGround(), "Morphling did not settle on the underwater floor");
            helper.assertTrue(polyp.isAlive(), "Morphling died while sinking");
            helper.succeed();
        });
    }
}
