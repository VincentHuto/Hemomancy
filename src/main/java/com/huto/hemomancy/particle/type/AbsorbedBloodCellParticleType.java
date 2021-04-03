package com.huto.hemomancy.particle.type;

import com.huto.hemomancy.particle.data.AbsorbedBloodCellData;
import com.mojang.serialization.Codec;

import net.minecraft.particles.ParticleType;

public class AbsorbedBloodCellParticleType extends ParticleType<AbsorbedBloodCellData> {
	public AbsorbedBloodCellParticleType() {
		super(false, AbsorbedBloodCellData.DESERIALIZER);
	}

	@Override
	public Codec<AbsorbedBloodCellData> func_230522_e_() {
		return AbsorbedBloodCellData.CODEC;
	}
}