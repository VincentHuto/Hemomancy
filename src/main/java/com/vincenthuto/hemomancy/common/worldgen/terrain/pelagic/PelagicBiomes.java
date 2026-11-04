package com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.PlacedFeatureInit;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class PelagicBiomes {
    private PelagicBiomes() {}
    public static ResourceKey<Biome> key(PelagicLayer layer) { return ResourceKey.create(Registries.BIOME, Hemomancy.rloc(layer.id)); }

}
