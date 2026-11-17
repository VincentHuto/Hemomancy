package com.vincenthuto.hemomancy.common.worldgen.structure.circus;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.Arrays;
import com.vincenthuto.hemomancy.common.worldgen.structure.StructurePlacementChecks;

@GameTestHolder("crimson_troupe_validation")
@PrefixGameTestTemplate(false)
public final class TroupeFootprintGameTests {
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
