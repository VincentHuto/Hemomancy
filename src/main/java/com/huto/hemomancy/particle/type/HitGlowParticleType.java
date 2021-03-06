package com.huto.hemomancy.particle.type;

import com.huto.hemomancy.particle.data.HitColorParticleData;
import com.mojang.serialization.Codec;

import net.minecraft.particles.ParticleType;

public class HitGlowParticleType extends ParticleType<HitColorParticleData> {
	public HitGlowParticleType() {
		super(false, HitColorParticleData.DESERIALIZER);
	}

	@Override
	public Codec<HitColorParticleData> func_230522_e_() {
		return HitColorParticleData.CODEC;
	}
}