package com.vincenthuto.hemomancy.client.particle.type;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.client.particle.data.SporiticSporeParticleData;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class SporiticSporeParticleType extends ParticleType<SporiticSporeParticleData> {
	public SporiticSporeParticleType() {
		super(false);
	}

	@Override
	public MapCodec<SporiticSporeParticleData> codec() {
		return SporiticSporeParticleData.MAP_CODEC;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, SporiticSporeParticleData> streamCodec() {
		return SporiticSporeParticleData.STREAM_CODEC;
	}
}
