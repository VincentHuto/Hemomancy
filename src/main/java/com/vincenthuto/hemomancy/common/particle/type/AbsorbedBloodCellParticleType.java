package com.vincenthuto.hemomancy.common.particle.type;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.particle.data.AbsorbedBloodCellData;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class AbsorbedBloodCellParticleType extends ParticleType<AbsorbedBloodCellData> {
	public AbsorbedBloodCellParticleType() {
		super(false);
	}

	@Override
	public MapCodec<AbsorbedBloodCellData> codec() {
		return AbsorbedBloodCellData.MAP_CODEC;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, AbsorbedBloodCellData> streamCodec() {
		return AbsorbedBloodCellData.STREAM_CODEC;
	}
}
