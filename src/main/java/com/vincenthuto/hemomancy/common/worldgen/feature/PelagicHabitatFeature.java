package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.vincenthuto.hemomancy.common.block.harbinger.plant.BoneWormColonyBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.GiantTubeWormColonyBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.PelagicColonyBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Adds life only to existing surfaces. Every write stays inside the decorated chunk. */
public final class PelagicHabitatFeature extends Feature<NoneFeatureConfiguration> {
    public PelagicHabitatFeature() { super(NoneFeatureConfiguration.CODEC); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        if (PelagicWorldgen.context(level.getLevel().getChunkSource().randomState()) == null) return false;
        var chunk = new ChunkPos(context.origin());
        boolean placed = false;
        for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
            for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
                for (int y = Math.max(-53, level.getMinBuildHeight() + 1); y <= 73; y++) {
                    var pos = new BlockPos(x, y, z);
                    var state = level.getBlockState(pos);
                    boolean water = PelagicHabitat.sourceWater(level, pos);
                    if (!water && !state.isAir()) continue;
                    var support = level.getBlockState(pos.below());
                    if (support.isAir() || support.is(Blocks.WATER)) continue;
                    var layer = PelagicHabitat.layer(level, pos);
                    long sample = sample(level.getSeed(), x, y, z);
                    Block growth = null;
                    if (layer == PelagicLayer.SHORE && y >= 55 && PelagicHabitat.coastalRock(support)) {
                        long patch = sample(level.getSeed() ^ 9187L, x >> 2, 0, z >> 2);
                        if (water && y <= 62 && Math.floorMod(patch, 5) == 0 && Math.floorMod(sample, 3) == 0)
                            growth = BlockInit.tidepool_anemone.get();
                        else if (Math.floorMod(patch, 3) != 0 && Math.floorMod(sample, 5) < 3)
                            growth = BlockInit.hematic_algal_crust.get();
                    } else if (water && (layer == PelagicLayer.CARRION || layer == PelagicLayer.HYDROTHERMAL)
                            && support.is(Blocks.BONE_BLOCK) && Math.floorMod(sample, 3) != 0) {
                        growth = BlockInit.bone_worm_colony.get();
                    } else if (water && layer == PelagicLayer.HYDROTHERMAL && PelagicHabitat.mineral(support)
                            && Math.floorMod(sample, 4) == 0 && PelagicHabitat.sourceWater(level, pos.above())) {
                        // Own-chunk heat prevents decoration order in an adjacent chunk changing the result.
                        boolean heated = false;
                        for (var p : BlockPos.betweenClosed(pos.offset(-4, -2, -4), pos.offset(4, 0, 4))) {
                            if ((p.getX() >> 4) == chunk.x && (p.getZ() >> 4) == chunk.z && level.getBlockState(p).is(Blocks.MAGMA_BLOCK)) {
                                heated = true; break;
                            }
                        }
                        if (heated) growth = BlockInit.giant_tube_worm_colony.get();
                    }
                    if (growth == null) continue;
                    var colony = growth.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, water);
                    if (growth == BlockInit.bone_worm_colony.get())
                        colony = colony.setValue(BoneWormColonyBlock.WORMS, 2 + (int)Math.floorMod(sample, 4));
                    else if (growth == BlockInit.giant_tube_worm_colony.get())
                        // The lowest two bits already select tube-worm placement sites.
                        colony = colony.setValue(GiantTubeWormColonyBlock.WORMS, 2 + (int)((sample >>> 2) & 3));
                    if (!colony.canSurvive(level, pos)) continue;
                    placed |= level.setBlock(pos, colony, 2);
                    if (growth == BlockInit.giant_tube_worm_colony.get()) {
                        level.setBlock(pos.above(), colony.setValue(PelagicColonyBlock.HALF, DoubleBlockHalf.UPPER), 2);
                        y++;
                    }
                }
            }
        }
        return placed;
    }
    private static long sample(long seed, int x, int y, int z) {
        long v = seed ^ x * 341873128712L ^ y * 132897987541L ^ z * 42317861L;
        v = (v ^ v >>> 30) * 0xbf58476d1ce4e5b9L;
        v = (v ^ v >>> 27) * 0x94d049bb133111ebL;
        return v ^ v >>> 31;
    }
}
