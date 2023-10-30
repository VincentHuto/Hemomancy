package com.vincenthuto.hemomancy.client.particle.type;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.client.particle.data.BloodAvatarHitParticleData;

import net.minecraft.core.particles.ParticleType;

public class BloodAvatarHitParticleType extends ParticleType<BloodAvatarHitParticleData> {
	public BloodAvatarHitParticleType() {
		super(false, BloodAvatarHitParticleData.DESERIALIZER);
	}

	@Override
	public Codec<BloodAvatarHitParticleData> codec() {
		return BloodAvatarHitParticleData.CODEC;
	}
}