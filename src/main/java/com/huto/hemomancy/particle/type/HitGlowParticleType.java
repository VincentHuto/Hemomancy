package com.huto.hemomancy.particle.type;

import com.huto.hemomancy.particle.data.HitColorParticleData;
import com.mojang.serialization.Codec;

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