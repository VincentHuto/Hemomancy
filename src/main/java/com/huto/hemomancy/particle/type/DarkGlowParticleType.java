package com.huto.hemomancy.particle.type;

import com.huto.hemomancy.particle.data.DarkColorParticleData;
import com.mojang.serialization.Codec;

import net.minecraft.particles.ParticleType;

public class DarkGlowParticleType extends ParticleType<DarkColorParticleData> {
	public DarkGlowParticleType() {
		super(false, DarkColorParticleData.DESERIALIZER);
	}

	@Override
	public Codec<DarkColorParticleData> func_230522_e_() {
		return DarkColorParticleData.CODEC;
	}
}