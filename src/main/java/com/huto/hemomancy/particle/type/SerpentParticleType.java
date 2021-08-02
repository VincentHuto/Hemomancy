package com.huto.hemomancy.particle.type;

import com.huto.hemomancy.particle.data.SerpentParticleData;
import com.mojang.serialization.Codec;

import net.minecraft.core.particles.ParticleType;

public class SerpentParticleType extends ParticleType<SerpentParticleData> {
	public SerpentParticleType() {
		super(false, SerpentParticleData.DESERIALIZER);
	}

	@Override
	public Codec<SerpentParticleData> codec() {
		return SerpentParticleData.CODEC;
	}
}