package com.vincenthuto.hemomancy.common.worldgen.feature;

import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class PelagicSeabedFeature extends Feature<NoneFeatureConfiguration> {
    public PelagicSeabedFeature() { super(NoneFeatureConfiguration.CODEC); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        var geology = com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicWorldgen.context(level.getLevel().getChunkSource().randomState());
        if (geology == null) return false;
        var chunk = new net.minecraft.world.level.ChunkPos(context.origin());
        var plan = com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLandmarks.plan(geology.terrain(), chunk.x, chunk.z,
                (x, z) -> geology.sample(x, z).column());
        boolean changed = false;
        for (var voxel : plan) {
            var pos = new net.minecraft.core.BlockPos(voxel.x(), voxel.y(), voxel.z());
            var existing = level.getBlockState(pos);
            boolean dryStack = voxel.material() == com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLandmarks.Material.STONE
                    && existing.isAir();
            if (!dryStack && !existing.is(net.minecraft.world.level.block.Blocks.WATER)
                    && !existing.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD)
                    && !existing.is(net.minecraft.world.level.block.Blocks.CLAY)
                    && !existing.is(net.minecraft.world.level.block.Blocks.GRAVEL)) continue;
            var block = switch (voxel.material()) {
                case BASALT -> net.minecraft.world.level.block.Blocks.SMOOTH_BASALT;
                case MAGMA -> net.minecraft.world.level.block.Blocks.MAGMA_BLOCK;
                case TUFF -> net.minecraft.world.level.block.Blocks.TUFF;
                case BONE -> net.minecraft.world.level.block.Blocks.BONE_BLOCK;
                case STONE -> net.minecraft.world.level.block.Blocks.STONE;
            };
            changed |= level.setBlock(pos, block.defaultBlockState(), 2);
        }
        return changed;
    }
}
