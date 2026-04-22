package com.vincenthuto.hemomancy.client.particle.type;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.client.particle.data.BloodCellData;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class BloodCellParticleType extends ParticleType<BloodCellData> {
	public BloodCellParticleType() {
		super(false);
	}

	@Override
	public MapCodec<BloodCellData> codec() {
		return BloodCellData.MAP_CODEC;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, BloodCellData> streamCodec() {
		return BloodCellData.STREAM_CODEC;
	}
}
