package com.huto.hemomancy.particle.type;

import com.huto.hemomancy.particle.data.BloodCellData;
import com.mojang.serialization.Codec;

import net.minecraft.core.particles.ParticleType;

public class BloodCellParticleType extends ParticleType<BloodCellData> {
	public BloodCellParticleType() {
		super(false, BloodCellData.DESERIALIZER);
	}

	@Override
	public Codec<BloodCellData> codec() {
		return BloodCellData.CODEC;
	}
}