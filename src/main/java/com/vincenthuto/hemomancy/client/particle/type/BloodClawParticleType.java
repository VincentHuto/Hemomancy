package com.vincenthuto.hemomancy.client.particle.type;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.client.particle.data.BloodClawData;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class BloodClawParticleType extends ParticleType<BloodClawData> {
	public BloodClawParticleType() {
		super(false);
	}

	@Override
	public MapCodec<BloodClawData> codec() {
		return BloodClawData.MAP_CODEC;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, BloodClawData> streamCodec() {
		return BloodClawData.STREAM_CODEC;
	}
}
