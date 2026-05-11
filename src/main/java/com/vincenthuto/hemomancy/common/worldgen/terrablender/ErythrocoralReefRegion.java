package com.vincenthuto.hemomancy.common.worldgen.terrablender;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.ParameterUtils.Continentalness;
import terrablender.api.ParameterUtils.Depth;
import terrablender.api.ParameterUtils.Erosion;
import terrablender.api.ParameterUtils.Humidity;
import terrablender.api.ParameterUtils.ParameterPointListBuilder;
import terrablender.api.ParameterUtils.Temperature;
import terrablender.api.ParameterUtils.Weirdness;
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
				.temperature(Temperature.WARM, Temperature.HOT)
				.humidity(Humidity.WET, Humidity.HUMID)
				.continentalness(Continentalness.OCEAN, Continentalness.COAST)
				.erosion(Erosion.EROSION_4, Erosion.EROSION_5, Erosion.EROSION_6)
				.depth(Depth.SURFACE)
				.weirdness(Weirdness.MID_SLICE_NORMAL_ASCENDING, Weirdness.MID_SLICE_VARIANT_ASCENDING)
				.offset(0.0F)
				.build()
				.forEach(point -> this.addBiome(mapper, point, BiomeInit.ERYTHROCORAL_REEF));
	}
}
