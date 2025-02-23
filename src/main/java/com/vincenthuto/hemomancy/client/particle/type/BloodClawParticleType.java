package com.vincenthuto.hemomancy.client.particle.type;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.client.particle.data.BloodClawData;

import net.minecraft.core.particles.ParticleType;

public class BloodClawParticleType extends ParticleType<BloodClawData> {
	public BloodClawParticleType() {
		super(false, BloodClawData.DESERIALIZER);
	}

	@Override
	public Codec<BloodClawData> codec() {
		return BloodClawData.CODEC;
	}
}