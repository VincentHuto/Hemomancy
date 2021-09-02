package com.vincenthuto.hemomancy.particle.type;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.particle.data.SerpentParticleData;

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