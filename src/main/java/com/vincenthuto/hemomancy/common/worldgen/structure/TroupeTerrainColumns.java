package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.vincenthuto.hemomancy.mixin.core.TroupeNoiseGeneratorAccessor;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.Structure;

/** One footprint's raw terrain, sharing vanilla's noise interpolation within each cell. */
final class TroupeTerrainColumns {
    private final Structure.GenerationContext context;
    private final NoiseGeneratorSettings settings;
    private final NoiseSettings noise;
    private final Aquifer.FluidPicker fluids;
    private final Long2ObjectOpenHashMap<NoiseColumn[]> cells = new Long2ObjectOpenHashMap<>();

    TroupeTerrainColumns(Structure.GenerationContext context) {
        this.context = context;
        if (context.chunkGenerator().getClass() == NoiseBasedChunkGenerator.class) {
            var generator = (NoiseBasedChunkGenerator) context.chunkGenerator();
            settings = generator.generatorSettings().value();
            var clamped = settings.noiseSettings().clampToHeightAccessor(context.heightAccessor());
            // Other generators and nonaligned custom height accessors retain their original column implementation.
            noise = clamped.height() > 0 && clamped.minY() % clamped.getCellHeight() == 0
                    && clamped.height() % clamped.getCellHeight() == 0 ? clamped : null;
            fluids = ((TroupeNoiseGeneratorAccessor) generator).hemomancy$fluidPicker().get();
        } else {
            settings = null;
            noise = null;
            fluids = null;
        }
    }

    NoiseColumn get(int x, int z) {
        if (noise == null)
            return context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        int width = noise.getCellWidth();
        int cellX = Math.floorDiv(x, width), cellZ = Math.floorDiv(z, width);
        long key = ((long)cellX << 32) ^ (cellZ & 0xffffffffL);
        var columns = cells.get(key);
        if (columns == null) {
            columns = new Cell(context.randomState(), cellX * width, cellZ * width, noise, settings, fluids).columns();
            cells.put(key, columns);
        }
        return columns[Math.floorMod(x, width) * width + Math.floorMod(z, width)];
    }

    private static final class Cell extends NoiseChunk {
        private final int x, z;
        private final NoiseSettings noise;
        private final BlockState defaultBlock;

        Cell(RandomState random, int x, int z, NoiseSettings noise, NoiseGeneratorSettings settings,
                Aquifer.FluidPicker fluids) {
            super(1, random, x, z, noise, DensityFunctions.BeardifierMarker.INSTANCE, settings, fluids, Blender.empty());
            this.x = x;
            this.z = z;
            this.noise = noise;
            defaultBlock = settings.defaultBlock();
        }

        NoiseColumn[] columns() {
            int width = noise.getCellWidth(), height = noise.getCellHeight();
            var states = new BlockState[width * width][noise.height()];
            initializeForFirstCellX();
            advanceCellX(0);
            try {
                for (int cellY = noise.height() / height - 1; cellY >= 0; cellY--) {
                    selectCellYZ(cellY, 0);
                    for (int dy = height - 1; dy >= 0; dy--) {
                        int index = cellY * height + dy;
                        updateForY(noise.minY() + index, dy / (double)height);
                        for (int dx = 0; dx < width; dx++) {
                            updateForX(x + dx, dx / (double)width);
                            for (int dz = 0; dz < width; dz++) {
                                updateForZ(z + dz, dz / (double)width);
                                var state = getInterpolatedState();
                                states[dx * width + dz][index] = state == null ? defaultBlock : state;
                            }
                        }
                    }
                }
            } finally {
                stopInterpolation();
            }
            var result = new NoiseColumn[states.length];
            for (int i = 0; i < result.length; i++) result[i] = new NoiseColumn(noise.minY(), states[i]);
            return result;
        }
    }
}
