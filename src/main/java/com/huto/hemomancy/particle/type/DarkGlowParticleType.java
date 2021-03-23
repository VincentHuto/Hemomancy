package com.huto.hemomancy.particle.type;

import com.huto.hemomancy.particle.data.DarkColorParticleTypeData;
import com.mojang.serialization.Codec;

import net.minecraft.particles.ParticleType;

public class DarkGlowParticleType extends ParticleType<DarkColorParticleTypeData> {
	public DarkGlowParticleType() {
		super(false, DarkColorParticleTypeData.DESERIALIZER);
	}

	@Override
	public Codec<DarkColorParticleTypeData> func_230522_e_() {
		return DarkColorParticleTypeData.CODEC;
	}
}