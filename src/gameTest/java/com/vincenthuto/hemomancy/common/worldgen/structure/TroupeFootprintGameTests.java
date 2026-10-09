package com.vincenthuto.hemomancy.common.worldgen.structure;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.Arrays;

@GameTestHolder("crimson_troupe_validation")
@PrefixGameTestTemplate(false)
public final class TroupeFootprintGameTests {
    public static void verifyNoiseColumns(GameTestHelper h) {
        var level = h.getLevel();
        var generator = level.getChunkSource().getGenerator();
        var context = new net.minecraft.world.level.levelgen.structure.Structure.GenerationContext(
                level.registryAccess(), generator, generator.getBiomeSource(), level.getChunkSource().randomState(),
                level.getStructureManager(), level.getSeed(), new net.minecraft.world.level.ChunkPos(0, 0), level, b -> true);
        var shared = new TroupeTerrainColumns(context);
        int[][] cells = {{0, 0}, {-4, -8}, {-1952, -2080}, {-480, -1024}, {292928, 1001008}, {312944, 973136}};
        for (int[] cell : cells) for (int dx = 0; dx < 4; dx++) for (int dz = 0; dz < 4; dz++) {
            int x = cell[0] + dx, z = cell[1] + dz;
            var batched = shared.get(x, z);
            var vanilla = generator.getBaseColumn(x, z, level, context.randomState());
            for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++)
                h.assertTrue(batched.getBlock(y).equals(vanilla.getBlock(y)), "Shared column differs at " + x + "," + y + "," + z);
        }
    }

    @GameTest(template="empty", timeoutTicks=20)
    public static void sharedColumnRetainsDryGroundAndHeightGates(GameTestHelper h) {
        BlockState[] blocks = new BlockState[384];
        Arrays.fill(blocks, Blocks.AIR.defaultBlockState());
        blocks[64 + 63] = Blocks.STONE.defaultBlockState();
        var column = new NoiseColumn(-64, blocks);
        h.assertTrue(StructurePlacementChecks.troupeGround(column, -64, 320).orElseThrow() == 63, "dry ground height changed");
        blocks[64 + 64] = Blocks.WATER.defaultBlockState();
        h.assertTrue(StructurePlacementChecks.troupeGround(column, -64, 320).isEmpty(), "wet ground accepted");
        blocks[64 + 64] = Blocks.AIR.defaultBlockState();
        blocks[64 + 149] = Blocks.STONE.defaultBlockState();
        h.assertTrue(StructurePlacementChecks.troupeGround(column, -64, 320).orElseThrow() == 149, "highest valid ground rejected");
        blocks[64 + 150] = Blocks.STONE.defaultBlockState();
        h.assertTrue(StructurePlacementChecks.troupeGround(column, -64, 320).isEmpty(), "height limit bypassed");
        h.succeed();
    }
}
