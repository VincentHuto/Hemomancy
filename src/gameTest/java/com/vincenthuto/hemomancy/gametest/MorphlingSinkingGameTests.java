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

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 200)
    public static void idleMorphlingSinksToFloor(GameTestHelper helper) {
        checkSinking(helper, false);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 200)
    public static void morphlingSinksWithTargetAbove(GameTestHelper helper) {
        checkSinking(helper, true);
    }

    private static java.util.List<net.minecraft.world.level.ChunkPos> forceTankChunks(GameTestHelper helper) {
        var chunks = new java.util.ArrayList<net.minecraft.world.level.ChunkPos>();
        BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(8, 8, 8));
        for (int x = Math.min(a.getX(), b.getX()) >> 4; x <= Math.max(a.getX(), b.getX()) >> 4; x++)
            for (int z = Math.min(a.getZ(), b.getZ()) >> 4; z <= Math.max(a.getZ(), b.getZ()) >> 4; z++) {
                // Only release chunks this test forced; a chunk the framework already holds stays held.
                if (helper.getLevel().getForcedChunks().contains(net.minecraft.world.level.ChunkPos.asLong(x, z))) continue;
                helper.getLevel().setChunkForced(x, z, true);
                chunks.add(new net.minecraft.world.level.ChunkPos(x, z));
            }
        return chunks;
    }

    private static void whenTicking(GameTestHelper helper, MorphlingPolypEntity polyp, int attempt, Runnable body) {
        if (polyp.tickCount > 0 || attempt >= 80) body.run();
        else helper.runAfterDelay(1, () -> whenTicking(helper, polyp, attempt + 1, body));
    }

    private static void checkSinking(GameTestHelper helper, boolean targetAbove) {
        for (BlockPos pos : BlockPos.betweenClosed(0, 0, 0, 8, 8, 8)) {
            boolean wall = pos.getY() == 0 || pos.getX() == 0 || pos.getX() == 8
                    || pos.getZ() == 0 || pos.getZ() == 8;
            helper.setBlock(pos, wall ? Blocks.STONE : Blocks.WATER);
        }
        // The tank is larger than the empty template, so it can spill into a chunk the framework never force-loads.
        // Such a chunk only entity-ticks while a neighbouring test happens to hold it, which made sinking flaky.
        var tankChunks = forceTankChunks(helper);
        MorphlingPolypEntity polyp = helper.spawn(EntityInit.morphling_polyp.get(), new BlockPos(4, 5, 4));
        if (targetAbove) {
            ArmorStand target = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(4, 7, 4));
            target.setNoGravity(true);
            polyp.setTarget(target);
        }
        // A fixture placed before its chunk starts entity-ticking leaves the polyp frozen; time the sink from its first tick.
        whenTicking(helper, polyp, 0, () -> {
            double startY = polyp.getY();
            helper.runAfterDelay(20, () -> {
                helper.assertTrue(polyp.isInWater(), "Morphling left the water fixture");
                helper.assertTrue(polyp.getY() < startY - 0.4D,
                        "Morphling floated instead of sinking: start=" + startY + ", current=" + polyp.getY());
            });
            helper.runAfterDelay(80, () -> {
                helper.assertTrue(polyp.onGround(), "Morphling did not settle on the underwater floor");
                helper.assertTrue(polyp.isAlive(), "Morphling died while sinking");
                tankChunks.forEach(chunk -> helper.getLevel().setChunkForced(chunk.x, chunk.z, false));
                helper.succeed();
            });
        });
    }
}
