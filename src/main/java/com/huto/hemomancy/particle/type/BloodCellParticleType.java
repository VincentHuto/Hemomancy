package com.huto.hemomancy.particle.type;

import com.huto.hemomancy.particle.data.BloodCellTypeData;
import com.mojang.serialization.Codec;

import net.minecraft.particles.ParticleType;

public class BloodCellParticleType extends ParticleType<BloodCellTypeData> {
	public BloodCellParticleType() {
		super(false, BloodCellTypeData.DESERIALIZER);
	}

	@Override
	public Codec<BloodCellTypeData> func_230522_e_() {
		return BloodCellTypeData.CODEC;
	}
}