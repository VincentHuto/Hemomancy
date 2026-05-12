package com.vincenthuto.hemomancy.common.worldgen.terrablender;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.ParameterUtils.Depth;
import terrablender.api.ParameterUtils.ParameterPointListBuilder;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

public class ErythrocoralReefRegion extends Region {
	public ErythrocoralReefRegion(ResourceLocation name, int weight) {
		super(name, RegionType.OVERWORLD, weight);
	}

	@Override
	public void addBiomes(Registry<Biome> registry,
			Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
		new ParameterPointListBuilder()
				.temperature(ErythrocoralReefClimateRules.temperatures())
				.humidity(ErythrocoralReefClimateRules.humidities())
				.continentalness(ErythrocoralReefClimateRules.continentalness())
				.erosion(ErythrocoralReefClimateRules.erosions())
				.depth(Depth.SURFACE)
				.weirdness(ErythrocoralReefClimateRules.weirdnesses())
				.offset(0.0F)
				.build()
				.forEach(point -> this.addBiome(mapper, point, BiomeInit.ERYTHROCORAL_REEF));
	}
}
