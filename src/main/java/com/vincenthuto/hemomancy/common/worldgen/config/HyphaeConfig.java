package com.vincenthuto.hemomancy.common.worldgen.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;




public class HyphaeConfig implements FeatureConfiguration {

	public static final Codec<HyphaeConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(BlockStateProvider.CODEC.fieldOf("toPlace").forGetter(HyphaeConfig::getToPlace))
			.apply(instance, HyphaeConfig::new));

	public final BlockStateProvider toPlace;

	public HyphaeConfig(BlockStateProvider toPlace) {
		this.toPlace = toPlace;
	}

	public BlockStateProvider getToPlace() {
		return toPlace;
	}
}
