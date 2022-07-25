package com.vincenthuto.hemomancy.particle.type;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.particle.data.BloodAvatarHitParticleData;
import com.vincenthuto.hemomancy.particle.data.HitColorParticleData;

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