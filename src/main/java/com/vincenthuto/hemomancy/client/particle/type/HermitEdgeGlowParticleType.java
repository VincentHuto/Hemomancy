package com.vincenthuto.hemomancy.client.particle.type;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.client.particle.data.HermitEdgeGlowParticleData;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class HermitEdgeGlowParticleType extends ParticleType<HermitEdgeGlowParticleData> {
	public HermitEdgeGlowParticleType() {
		super(false);
	}

	@Override
	public MapCodec<HermitEdgeGlowParticleData> codec() {
		return HermitEdgeGlowParticleData.MAP_CODEC;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, HermitEdgeGlowParticleData> streamCodec() {
		return HermitEdgeGlowParticleData.STREAM_CODEC;
	}
}
