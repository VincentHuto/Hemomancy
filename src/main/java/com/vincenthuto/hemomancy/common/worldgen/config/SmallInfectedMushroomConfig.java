package com.vincenthuto.hemomancy.common.worldgen.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;




public class SmallInfectedMushroomConfig implements FeatureConfiguration {

	public static final Codec<SmallInfectedMushroomConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(BlockStateProvider.CODEC.fieldOf("toPlace").forGetter(SmallInfectedMushroomConfig::getToPlace))
			.apply(instance, SmallInfectedMushroomConfig::new));

	public final BlockStateProvider toPlace;

	public SmallInfectedMushroomConfig(BlockStateProvider toPlace) {
		this.toPlace = toPlace;
	}

	public BlockStateProvider getToPlace() {
		return toPlace;
	}
}
