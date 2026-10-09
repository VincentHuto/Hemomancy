package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import net.minecraft.core.*;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.*;
import terrablender.mixin.MultiNoiseBiomeSourceAccess;
import terrablender.worldgen.IExtendedParameterList;
import java.util.*;

/** One Overworld's immutable inputs. Caches are bounded and private to each generation thread. */
public final class PelagicContext {
    private final PelagicTerrainSampler terrain;
    private final DensityFunction continents, erosion, ridges;
    private final Climate.Sampler climate;
    private final EnumMap<PelagicLayer, Holder<Biome>> biomes = new EnumMap<>(PelagicLayer.class);
    private final ThreadLocal<Climate.ParameterList<Holder<Biome>>> parameters;
    private final ThreadLocal<PelagicColumnCache<Holder<Biome>>> surfaces =
            ThreadLocal.withInitial(PelagicColumnCache::new);
    private final ThreadLocal<PelagicColumnCache<PelagicTerrainSampler.Column>> columns =
            ThreadLocal.withInitial(PelagicColumnCache::new);
    private final ThreadLocal<PelagicReefBlend> reefBlend = ThreadLocal.withInitial(PelagicReefBlend::new);
    private final ThreadLocal<PelagicReefBlend> shoreBlend = ThreadLocal.withInitial(PelagicReefBlend::new);
    // Keep callbacks on the world context, not in worker ThreadLocal values that would retain it after unload.
    private final PelagicColumnCache.Sampler<Holder<Biome>> surfaceSampler = this::sampleSurface;
    private final PelagicColumnCache.Sampler<PelagicTerrainSampler.Column> columnSampler = this::sampleColumn;
    private final PelagicReefBlend.ReefMask reefMask = (x, z) -> surface(x, z).is(PelagicBiomes.key(PelagicLayer.REEF));
    private final PelagicReefBlend.ReefMask shoreMask = (x, z) -> surface(x, z).is(PelagicBiomes.key(PelagicLayer.SHORE));
    public record Sample(PelagicTerrainSampler.Column column, Holder<Biome> surface) {}

    public PelagicContext(long seed, RandomState state, MultiNoiseBiomeSource source, Registry<Biome> registry) {
        terrain = new PelagicTerrainSampler(seed);
        continents = state.router().continents();
        erosion = state.router().erosion();
        ridges = state.router().ridges();
        climate = state.sampler();
        for (var layer : PelagicLayer.values()) biomes.put(layer, registry.getHolderOrThrow(PelagicBiomes.key(layer)));
        var original = ((MultiNoiseBiomeSourceAccess)(Object)source).getParameters()
                .map(value -> value, value -> value.value().parameters());
        parameters = ThreadLocal.withInitial(() -> {
            var copy = ((IExtendedParameterList<Holder<Biome>>)(Object)original).clone();
            var extension = (IExtendedParameterList<Holder<Biome>>)(Object)copy;
            if (extension.isInitialized()) extension.recreateUniqueness();
            return copy;
        });
    }

    public Sample sample(int x, int z) {
        return new Sample(column(x, z), surface(x, z));
    }

    public PelagicTerrainSampler.Column column(int x, int z) {
        return columns.get().get(x, z, columnSampler);
    }

    private PelagicTerrainSampler.Column sampleColumn(int x, int z) {
        var point = new DensityFunction.SinglePointContext(x, 63, z);
        double continentalness = continents.compute(point);
        // Outside both influence bands every profile is inactive, regardless of neighboring biomes.
        if (!PelagicTerrainSampler.mayShape(continentalness)) return PelagicTerrainSampler.INACTIVE;
        var surface = surface(x, z);
        var type = surface.is(PelagicBiomes.key(PelagicLayer.SHORE)) ? PelagicTerrainSampler.Surface.ROCKPOOL
                : surface.is(PelagicBiomes.key(PelagicLayer.REEF)) ? PelagicTerrainSampler.Surface.REEF
                : surface.is(PelagicBiomes.key(PelagicLayer.OPEN)) ? PelagicTerrainSampler.Surface.OPEN
                : PelagicTerrainSampler.Surface.VANILLA;
        double eroded = erosion.compute(point), ridge = ridges.compute(point);
        var column = terrain.sample(x, z, continentalness, eroded, ridge, type);
        if (type != PelagicTerrainSampler.Surface.VANILLA
                || surface.unwrapKey().orElseThrow().location().getNamespace().equals("minecraft")) {
            double reefWeight = reefBlend.get().cachedWeight(x, z, reefMask);
            double shoreWeight = shoreBlend.get().cachedWeight(x, z, shoreMask);
            // Narrow beaches need a fully shaped core for enclosed pools and grounded rock stacks.
            shoreWeight = PelagicTerrainSampler.smooth(0, .75, shoreWeight);
            var open = terrain.sample(x, z, continentalness, eroded, ridge, PelagicTerrainSampler.Surface.OPEN);
            var reef = reefWeight > 0
                    ? terrain.sample(x, z, continentalness, eroded, ridge, PelagicTerrainSampler.Surface.REEF) : open;
            // Reserve the coastal contribution, then blend reef and open water in the remaining space.
            column = PelagicTerrainSampler.blend(open, reef,
                    shoreWeight < 1 ? Math.min(1, reefWeight / (1 - shoreWeight)) : 0);
            if (shoreWeight > 0) {
                var shore = terrain.sample(x, z, continentalness, eroded, ridge, PelagicTerrainSampler.Surface.ROCKPOOL);
                column = PelagicTerrainSampler.blend(column, shore, shoreWeight);
            }
        }
        // A foreign TerraBlender region owns its geology, including islands inside ocean climates.
        if (type == PelagicTerrainSampler.Surface.VANILLA
                && !surface.unwrapKey().orElseThrow().location().getNamespace().equals("minecraft"))
            column = new PelagicTerrainSampler.Column(column.floor(), 0, column.form(), column.incision(), false);
        return column;
    }

    private Holder<Biome> surface(int x, int z) {
        return surfaces.get().get(QuartPos.fromBlock(x), QuartPos.fromBlock(z), surfaceSampler);
    }

    private Holder<Biome> sampleSurface(int qx, int qz) {
        return ((IExtendedParameterList<Holder<Biome>>)(Object)parameters.get())
                .findValuePositional(climate.sample(qx, 15, qz), qx, 15, qz);
    }

    public Holder<Biome> biome(int x, int y, int z, Holder<Biome> original) {
        var column = column(x, z);
        if (column.influence() == 0 || y < column.floor() - 5) return original;
        var surface = surface(x, z);
        if (surface.is(PelagicBiomes.key(PelagicLayer.SHORE)))
            return y < 90 ? surface : original;
        if (!surface.is(BiomeTags.IS_OCEAN) || y > 63) return original;
        return y >= 34 ? surface : biomes.get(PelagicLayer.atWaterY(y));
    }

    public Collection<Holder<Biome>> biomes() { return Collections.unmodifiableCollection(biomes.values()); }
    public PelagicTerrainSampler terrain() { return terrain; }

    public NoiseRouter shape(NoiseRouter router) {
        return new NoiseRouter(router.barrierNoise(), router.fluidLevelFloodednessNoise(), router.fluidLevelSpreadNoise(),
                router.lavaNoise(), router.temperature(), router.vegetation(), router.continents(), router.erosion(),
                router.depth(), router.ridges(),
                new PelagicDensityFunction(router.initialDensityWithoutJaggedness(), this, true),
                new PelagicDensityFunction(router.finalDensity(), this, false),
                router.veinToggle(), router.veinRidged(), router.veinGap());
    }
}
