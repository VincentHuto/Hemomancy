package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.entity.mob.monster.MortarboundFormRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.MortarboundEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.world.level.block.Blocks;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MortarboundGameTests {
    private static final TicketType<java.util.UUID> FIXTURE_TICKET =
            TicketType.create("hemomancy_mortarbound_fixture", java.util.UUID::compareTo);

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 130, batch = "mortarbound")
    public static void patrolsWallWithoutShowingBody(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x <= 6; x++) for (int y = 1; y <= 3; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, 1)), Blocks.AIR.defaultBlockState());
        for (int x = 1; x <= 5; x++) for (int y = 1; y <= 4; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, 2)), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        var start = helper.absolutePos(new BlockPos(2, 1, 1));
        var mob = EntityInit.mortarbound.get().create(level);
        helper.assertTrue(mob != null, "Mortarbound entity could not be created");
        mob.setPos(start.getX() + .5, start.getY() + .1, start.getZ() + .5);
        level.addFreshEntity(mob);
        whenFixtureTicks(helper, mob, () -> helper.runAfterDelay(38, () -> {
            helper.assertTrue(mob.formState().form() == MortarboundFormRules.Form.EMBEDDED
                            && mob.getX() > start.getX() + 1,
                    "Mortarbound did not patrol while embedded: " + mob.position() + " " + mob.formState());
            helper.succeed();
        }));
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 280, batch = "mortarbound")
    public static void meltsIntoWallAndGrowsOutOnVibration(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x <= 6; x++) for (int y = 1; y <= 3; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, 1)), Blocks.AIR.defaultBlockState());
        for (int y = 1; y <= 4; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(2, y, 2)), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        var start = helper.absolutePos(new BlockPos(2, 1, 1));
        var mob = EntityInit.mortarbound.get().create(level);
        helper.assertTrue(mob != null, "Mortarbound entity could not be created");
        mob.setPos(start.getX() + .5, start.getY() + .1, start.getZ() + .5);
        level.addFreshEntity(mob);
        whenFixtureTicks(helper, mob, () -> {
            helper.runAfterDelay(3, () -> {
                helper.assertTrue(mob.formState().form() == MortarboundFormRules.Form.EMBEDDED,
                        "Unalerted Mortarbound should be embedded");
                mob.hear(mob.position().add(0, 0, -1));
            });
            helper.runAfterDelay(12, () -> {
                helper.assertTrue(mob.formState().form() == MortarboundFormRules.Form.EMERGING && mob.emergence(0) > 0,
                        "Vibration should grow the Mortarbound out of the wall: form=" + mob.formState()
                                + ", ticks=" + mob.tickCount + ", alive=" + mob.isAlive()
                                + ", indexed=" + (level.getEntity(mob.getUUID()) == mob)
                                + ", entitiesLoaded=" + level.areEntitiesLoaded(ChunkPos.asLong(mob.blockPosition()))
                                + ", pos=" + mob.position() + ", face=" + mob.wallFace() + ", removal=" + mob.getRemovalReason());
            });
            helper.runAfterDelay(30, () -> {
                helper.assertTrue(mob.formState().form() == MortarboundFormRules.Form.ACTIVE,
                        "Mortarbound did not finish emerging");
            });
            helper.runAfterDelay(205, () -> {
                helper.assertTrue(mob.formState().form() == MortarboundFormRules.Form.EMBEDDED,
                        "Quiet Mortarbound did not melt back into its wall");
                helper.succeed();
            });
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 160, batch = "mortarbound")
    public static void followsSoundAlongARealWall(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x <= 6; x++) for (int y = 1; y <= 3; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, 1)), Blocks.AIR.defaultBlockState());
        for (int x = 1; x <= 5; x++) for (int y = 1; y <= 4; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, 2)), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        var start = helper.absolutePos(new BlockPos(2, 1, 1));
        var mob = EntityInit.mortarbound.get().create(level);
        helper.assertTrue(mob != null, "Mortarbound entity could not be created");
        mob.setPos(start.getX() + .5, start.getY() + .1, start.getZ() + .5);
        level.addFreshEntity(mob);
        whenFixtureTicks(helper, mob, () -> {
            mob.hear(helper.absolutePos(new BlockPos(5, 1, 1)).getCenter());
            helper.runAfterDelay(12, () -> {
                helper.assertTrue(mob.formState().form() == MortarboundFormRules.Form.EMBEDDED
                                && mob.getX() > start.getX() + .5,
                        "Mortarbound should follow distant sound while hidden");
            });
            helper.runAfterDelay(60, () -> {
                helper.assertTrue(mob.getX() > start.getX() + 2,
                        "Mortarbound did not traverse the supported wall");
                helper.assertTrue(mob.formState().form() == MortarboundFormRules.Form.ACTIVE,
                        "Mortarbound did not emerge near the disturbance");
                helper.succeed();
            });
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 130, batch = "mortarbound")
    public static void woolStopsItsHunt(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x <= 6; x++) for (int y = 1; y <= 3; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, 1)), Blocks.AIR.defaultBlockState());
        for (int x = 1; x <= 5; x++) for (int y = 1; y <= 4; y++)
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, 2)), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        level.setBlockAndUpdate(helper.absolutePos(new BlockPos(4, 1, 1)), Blocks.WHITE_WOOL.defaultBlockState());
        var start = helper.absolutePos(new BlockPos(2, 1, 1));
        var mob = EntityInit.mortarbound.get().create(level);
        helper.assertTrue(mob != null, "Mortarbound entity could not be created");
        mob.setPos(start.getX() + .5, start.getY() + .1, start.getZ() + .5);
        level.addFreshEntity(mob);
        whenFixtureTicks(helper, mob, () -> {
            mob.hear(helper.absolutePos(new BlockPos(5, 1, 1)).getCenter());
            helper.runAfterDelay(40, () -> {
                helper.assertTrue(mob.formState().form() == MortarboundFormRules.Form.EMBEDDED,
                        "Mortarbound emerged after a wool-blocked sound");
                helper.succeed();
            });
        });
    }

    private static void whenFixtureTicks(GameTestHelper helper, MortarboundEntity mob, Runnable checks) {
        mob.setPersistenceRequired();
        var requiredChunks = new java.util.HashSet<ChunkPos>();
        for (int x = 0; x <= 6; x++) for (int z = 1; z <= 2; z++)
            requiredChunks.add(new ChunkPos(helper.absolutePos(new BlockPos(x, 1, z))));
        var owner = java.util.UUID.randomUUID();
        var source = helper.getLevel().getChunkSource();
        requiredChunks.forEach(chunk -> source.addRegionTicket(FIXTURE_TICKET, chunk, 2, owner, true));
        Runnable cleanup = () -> {
            mob.discard();
            requiredChunks.forEach(chunk -> source.removeRegionTicket(FIXTURE_TICKET, chunk, 2, owner, true));
        };
        helper.testInfo.addListener(new GameTestListener() {
            public void testStructureLoaded(GameTestInfo test) { }
            public void testPassed(GameTestInfo test, GameTestRunner runner) { cleanup.run(); }
            public void testFailed(GameTestInfo test, GameTestRunner runner) { cleanup.run(); }
            public void testAddedForRerun(GameTestInfo oldTest, GameTestInfo newTest, GameTestRunner runner) { }
        });
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(mob.isAlive() && mob.tickCount > 0,
                "Mortarbound fixture must receive its first natural entity tick"))
                .thenExecute(() -> {
                    try { checks.run(); }
                    catch (RuntimeException error) { helper.fail("Mortarbound fixture failed: " + error); }
                });
    }
}
