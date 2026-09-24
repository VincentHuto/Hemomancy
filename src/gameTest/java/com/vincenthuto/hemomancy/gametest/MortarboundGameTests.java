package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.entity.mob.monster.MortarboundFormRules;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.world.level.block.Blocks;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MortarboundGameTests {
    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 70, batch = "mortarbound")
    public static void patrolsWallWithoutShowingBody(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x <= 6; x++) for (int y = 1; y <= 3; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, 1)), Blocks.AIR.defaultBlockState());
        for (int x = 1; x <= 5; x++) for (int y = 1; y <= 4; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, 2)), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        var start = helper.absolutePos(new BlockPos(2, 1, 1));
        var mob = EntityInit.mortarbound.get().create(level);
        if (mob == null) throw new AssertionError("Mortarbound entity could not be created");
        mob.setPos(start.getX() + .5, start.getY() + .1, start.getZ() + .5);
        level.addFreshEntity(mob);
        helper.runAfterDelay(38, () -> {
            if (mob.formState().form() != MortarboundFormRules.Form.EMBEDDED || mob.getX() <= start.getX() + 1)
                throw new AssertionError("Mortarbound did not patrol while embedded: " + mob.position() + " " + mob.formState());
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 220, batch = "mortarbound")
    public static void meltsIntoWallAndGrowsOutOnVibration(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x <= 6; x++) for (int y = 1; y <= 3; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, 1)), Blocks.AIR.defaultBlockState());
        for (int y = 1; y <= 4; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(2, y, 2)), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        var start = helper.absolutePos(new BlockPos(2, 1, 1));
        var mob = EntityInit.mortarbound.get().create(level);
        if (mob == null) throw new AssertionError("Mortarbound entity could not be created");
        mob.setPos(start.getX() + .5, start.getY() + .1, start.getZ() + .5);
        level.addFreshEntity(mob);
        helper.runAfterDelay(3, () -> {
            if (mob.formState().form() != MortarboundFormRules.Form.EMBEDDED)
                throw new AssertionError("Unalerted Mortarbound should be embedded");
            mob.hear(mob.position().add(0, 0, -1));
        });
        helper.runAfterDelay(12, () -> {
            if (mob.formState().form() != MortarboundFormRules.Form.EMERGING || mob.emergence(0) <= 0)
                throw new AssertionError("Vibration should grow the Mortarbound out of the wall");
        });
        helper.runAfterDelay(30, () -> {
            if (mob.formState().form() != MortarboundFormRules.Form.ACTIVE)
                throw new AssertionError("Mortarbound did not finish emerging");
        });
        helper.runAfterDelay(205, () -> {
            if (mob.formState().form() != MortarboundFormRules.Form.EMBEDDED)
                throw new AssertionError("Quiet Mortarbound did not melt back into its wall");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 100, batch = "mortarbound")
    public static void followsSoundAlongARealWall(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x <= 6; x++) for (int y = 1; y <= 3; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, 1)), Blocks.AIR.defaultBlockState());
        for (int x = 1; x <= 5; x++) for (int y = 1; y <= 4; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, 2)), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        var start = helper.absolutePos(new BlockPos(2, 1, 1));
        var mob = EntityInit.mortarbound.get().create(level);
        if (mob == null) throw new AssertionError("Mortarbound entity could not be created");
        mob.setPos(start.getX() + .5, start.getY() + .1, start.getZ() + .5);
        level.addFreshEntity(mob);
        mob.hear(helper.absolutePos(new BlockPos(5, 1, 1)).getCenter());
        helper.runAfterDelay(12, () -> {
            if (mob.formState().form() != MortarboundFormRules.Form.EMBEDDED || mob.getX() <= start.getX() + .5)
                throw new AssertionError("Mortarbound should follow distant sound while hidden");
        });
        helper.runAfterDelay(60, () -> {
            if (mob.getX() <= start.getX() + 2)
                throw new AssertionError("Mortarbound did not traverse the supported wall");
            if (mob.formState().form() != MortarboundFormRules.Form.ACTIVE)
                throw new AssertionError("Mortarbound did not emerge near the disturbance");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 70, batch = "mortarbound")
    public static void woolStopsItsHunt(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x <= 6; x++) for (int y = 1; y <= 3; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, 1)), Blocks.AIR.defaultBlockState());
        for (int x = 1; x <= 5; x++) for (int y = 1; y <= 4; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, 2)), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        level.setBlockAndUpdate(helper.absolutePos(new BlockPos(4, 1, 1)), Blocks.WHITE_WOOL.defaultBlockState());
        var start = helper.absolutePos(new BlockPos(2, 1, 1));
        var mob = EntityInit.mortarbound.get().create(level);
        if (mob == null) throw new AssertionError("Mortarbound entity could not be created");
        mob.setPos(start.getX() + .5, start.getY() + .1, start.getZ() + .5);
        level.addFreshEntity(mob);
        mob.hear(helper.absolutePos(new BlockPos(5, 1, 1)).getCenter());
        helper.runAfterDelay(40, () -> {
            if (mob.formState().form() != MortarboundFormRules.Form.EMBEDDED)
                throw new AssertionError("Mortarbound emerged after a wool-blocked sound");
            helper.succeed();
        });
    }
}
