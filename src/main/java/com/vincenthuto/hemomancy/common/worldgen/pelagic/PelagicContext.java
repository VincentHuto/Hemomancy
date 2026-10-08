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
    private final ThreadLocal<Map<Long, Holder<Biome>>> surfaces = ThreadLocal.withInitial(() -> new LinkedHashMap<>(1024, .75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, Holder<Biome>> entry) { return size() > 4096; }
    });
    private final ThreadLocal<Map<Long, Sample>> samples = ThreadLocal.withInitial(() -> new LinkedHashMap<>(1024, .75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, Sample> entry) { return size() > 4096; }
    });
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
        long key = ((long)x << 32) ^ (z & 0xffffffffL);
        var cache = samples.get();
        var found = cache.get(key);
        if (found != null) return found;
        var point = new DensityFunction.SinglePointContext(x, 63, z);
        var surface = surface(x, z);
        var type = surface.is(PelagicBiomes.key(PelagicLayer.SHORE)) ? PelagicTerrainSampler.Surface.ROCKPOOL
                : surface.is(PelagicBiomes.key(PelagicLayer.REEF)) ? PelagicTerrainSampler.Surface.REEF
                : surface.is(PelagicBiomes.key(PelagicLayer.OPEN)) ? PelagicTerrainSampler.Surface.OPEN
                : PelagicTerrainSampler.Surface.VANILLA;
        double continentalness = continents.compute(point), eroded = erosion.compute(point), ridge = ridges.compute(point);
        var column = terrain.sample(x, z, continentalness, eroded, ridge, type);
        if (type != PelagicTerrainSampler.Surface.VANILLA
                || surface.unwrapKey().orElseThrow().location().getNamespace().equals("minecraft")) {
            double reefWeight = PelagicReefBlend.weight(x, z,
                    (px, pz) -> surface(px, pz).is(PelagicBiomes.key(PelagicLayer.REEF)));
            double shoreWeight = PelagicReefBlend.weight(x, z,
                    (px, pz) -> surface(px, pz).is(PelagicBiomes.key(PelagicLayer.SHORE)));
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
        found = new Sample(column, surface);
        cache.put(key, found);
        return found;
    }

    private Holder<Biome> surface(int x, int z) {
        int qx = QuartPos.fromBlock(x), qz = QuartPos.fromBlock(z);
        long key = ((long)qx << 32) ^ (qz & 0xffffffffL);
        return surfaces.get().computeIfAbsent(key, ignored ->
                ((IExtendedParameterList<Holder<Biome>>)(Object)parameters.get())
                        .findValuePositional(climate.sample(qx, 15, qz), qx, 15, qz));
    }

    public Holder<Biome> biome(int x, int y, int z, Holder<Biome> original) {
        var sample = sample(x, z);
        var column = sample.column();
        if (column.influence() == 0 || y < column.floor() - 5) return original;
        if (sample.surface().is(PelagicBiomes.key(PelagicLayer.SHORE)))
            return y < 90 ? sample.surface() : original;
        if (!sample.surface().is(BiomeTags.IS_OCEAN) || y > 63) return original;
        return y >= 34 ? sample.surface() : biomes.get(PelagicLayer.atWaterY(y));
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
