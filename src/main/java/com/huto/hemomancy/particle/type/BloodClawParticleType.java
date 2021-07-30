package com.huto.hemomancy.particle.type;

import com.huto.hemomancy.particle.data.BloodClawData;
import com.mojang.serialization.Codec;

import net.minecraft.core.particles.ParticleType;

public class BloodClawParticleType extends ParticleType<BloodClawData> {
	public BloodClawParticleType() {
		super(false, BloodClawData.DESERIALIZER);
	}

	@Override
	public Codec<BloodClawData> func_230522_e_() {
		return BloodClawData.CODEC;
	}
}