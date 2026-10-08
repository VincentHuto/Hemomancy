package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import com.vincenthuto.hemomancy.config.HemoCommonConfig;
import com.vincenthuto.hemomancy.mixin.core.PelagicPossibleBiomesAccessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import java.util.*;

public final class PelagicWorldgen {
    private PelagicWorldgen() {}
    public static void initialize(ServerLevel level, ChunkGenerator generator, RandomState state) {
        if (!HemoCommonConfig.enablePelagicWorldgen() || !level.dimension().equals(Level.OVERWORLD)
                || !(generator instanceof NoiseBasedChunkGenerator noise)
                || !(generator.getBiomeSource() instanceof MultiNoiseBiomeSource source)) return;
        var settings = noise.generatorSettings();
        if (!settings.is(NoiseGeneratorSettings.OVERWORLD) && !settings.is(NoiseGeneratorSettings.LARGE_BIOMES)
                && !settings.is(NoiseGeneratorSettings.AMPLIFIED)) return;
        if (noise.getMinY() != -64 || noise.getSeaLevel() != 63) return;
        var context = new PelagicContext(level.getSeed(), state, source, level.registryAccess().registryOrThrow(Registries.BIOME));
        ((PelagicRandomStateAccess)(Object)state).hemomancy$setPelagic(context);
        ((PelagicBiomeSourceAccess)(Object)source).hemomancy$setPelagic(context);
        var possible = new LinkedHashSet<>(source.possibleBiomes());
        possible.addAll(context.biomes());
        var immutable = Collections.unmodifiableSet(possible);
        ((PelagicPossibleBiomesAccessor)(Object)source).hemomancy$setPossibleBiomes(() -> immutable);
    }
    public static PelagicContext context(RandomState state) { return ((PelagicRandomStateAccess)(Object)state).hemomancy$pelagic(); }
}
