package com.vincenthuto.hemomancy.client.particle.type;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.client.particle.data.BloodAvatarHitParticleData;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class BloodAvatarHitParticleType extends ParticleType<BloodAvatarHitParticleData> {
	public BloodAvatarHitParticleType() {
		super(false);
	}

	@Override
	public MapCodec<BloodAvatarHitParticleData> codec() {
		return BloodAvatarHitParticleData.MAP_CODEC;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, BloodAvatarHitParticleData> streamCodec() {
		return BloodAvatarHitParticleData.STREAM_CODEC;
	}
}
