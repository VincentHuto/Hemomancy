package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class HematicAlgalCrustBlock extends ErythrocoralGrowthBlock {
    public HematicAlgalCrustBlock(Properties properties) { super(properties, Block.box(0, 0, 0, 16, 1, 16)); }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return super.canSurvive(state, level, pos) && PelagicHabitat.wetRock(level, pos);
    }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3); return;
        }
        if (random.nextInt(8) != 0) return;
        var target = pos.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random)).offset(0, random.nextInt(3) - 1, 0);
        if (!PelagicHabitat.loaded(level, target) || PelagicHabitat.layer(level, target) != PelagicLayer.SHORE) return;
        if (!(level.getBlockState(target).isAir() || PelagicHabitat.sourceWater(level, target))) return;
        var growth = defaultBlockState().setValue(WATERLOGGED, PelagicHabitat.sourceWater(level, target));
        if (growth.canSurvive(level, target)) level.setBlock(target, growth, 3);
    }
}
