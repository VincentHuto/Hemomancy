package com.huto.hemomancy.particle.type;

import com.huto.hemomancy.particle.data.SerpentParticleTypeData;
import com.mojang.serialization.Codec;

import net.minecraft.particles.ParticleType;

public class SerpentParticleType extends ParticleType<SerpentParticleTypeData> {
	public SerpentParticleType() {
		super(false, SerpentParticleTypeData.DESERIALIZER);
	}

	@Override
	public Codec<SerpentParticleTypeData> func_230522_e_() {
		return SerpentParticleTypeData.CODEC;
	}
}