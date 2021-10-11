package com.vincenthuto.hemomancy.particle.type;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.particle.data.HitColorParticleData;

import net.minecraft.core.particles.ParticleType;

public class HitGlowParticleType extends ParticleType<HitColorParticleData> {
	public HitGlowParticleType() {
		super(false, HitColorParticleData.DESERIALIZER);
	}

	@Override
	public Codec<HitColorParticleData> codec() {
		return HitColorParticleData.CODEC;
	}
}