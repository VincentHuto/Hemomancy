package com.vincenthuto.hemomancy.client.particle.type;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.client.particle.data.AbsorbedBloodCellData;

import net.minecraft.core.particles.ParticleType;

public class AbsorbedBloodCellParticleType extends ParticleType<AbsorbedBloodCellData> {
	public AbsorbedBloodCellParticleType() {
		super(false, AbsorbedBloodCellData.DESERIALIZER);
	}

	@Override
	public Codec<AbsorbedBloodCellData> codec() {
		return AbsorbedBloodCellData.CODEC;
	}
}