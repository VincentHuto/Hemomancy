package com.huto.hemomancy.particle.type;

import com.huto.hemomancy.particle.data.HitColorParticleTypeData;
import com.mojang.serialization.Codec;

import net.minecraft.particles.ParticleType;

public class HitGlowParticleType extends ParticleType<HitColorParticleTypeData> {
	public HitGlowParticleType() {
		super(false, HitColorParticleTypeData.DESERIALIZER);
	}

	@Override
	public Codec<HitColorParticleTypeData> func_230522_e_() {
		return HitColorParticleTypeData.CODEC;
	}
}