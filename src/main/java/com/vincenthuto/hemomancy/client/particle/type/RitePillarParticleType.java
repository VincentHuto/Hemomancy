package com.vincenthuto.hemomancy.client.particle.type;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.client.particle.data.RitePillarData;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class RitePillarParticleType extends ParticleType<RitePillarData> {
	public RitePillarParticleType() {
		super(false);
	}

	@Override
	public MapCodec<RitePillarData> codec() {
		return RitePillarData.MAP_CODEC;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, RitePillarData> streamCodec() {
		return RitePillarData.STREAM_CODEC;
	}
}
