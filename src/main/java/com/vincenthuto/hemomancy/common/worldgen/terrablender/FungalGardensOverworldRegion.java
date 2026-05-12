package com.vincenthuto.hemomancy.common.worldgen.terrablender;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.common.init.BiomeInit;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

public class FungalGardensOverworldRegion extends Region {
	public static final String REGION_PATH = "fungal_gardens_overworld";

	@SuppressWarnings("unchecked")
	private static final ResourceKey<Biome>[] REPLACEMENT_TARGETS = new ResourceKey[] {
			Biomes.JUNGLE,
			Biomes.MUSHROOM_FIELDS,
			Biomes.MANGROVE_SWAMP,
			Biomes.SWAMP
	};

	public FungalGardensOverworldRegion(ResourceLocation name, int weight) {
		super(name, RegionType.OVERWORLD, weight);
	}

	@Override
	public void addBiomes(Registry<Biome> registry,
			Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
		this.addModifiedVanillaOverworldBiomes(mapper, builder -> {
			for (ResourceKey<Biome> target : REPLACEMENT_TARGETS) {
				builder.replaceBiome(target, BiomeInit.FUNGAL_GARDENS);
			}
		});
	}

	static ResourceKey<Biome>[] replacementTargets() {
		return REPLACEMENT_TARGETS.clone();
	}

	static String[] replacementTargetPaths() {
		String[] paths = new String[REPLACEMENT_TARGETS.length];
		for (int index = 0; index < REPLACEMENT_TARGETS.length; index++) {
			paths[index] = REPLACEMENT_TARGETS[index].location().toString();
		}
		return paths;
	}
}
