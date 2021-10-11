package com.vincenthuto.hemomancy.particle.type;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.particle.data.BloodCellData;

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