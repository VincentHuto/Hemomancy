package com.vincenthuto.hemomancy.client.particle.type;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.client.particle.data.DaemonDiffuseGlowParticleData;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class DaemonDiffuseGlowParticleType
		extends ParticleType<DaemonDiffuseGlowParticleData> {
	public DaemonDiffuseGlowParticleType() {
		super(false);
	}

	@Override
	public MapCodec<DaemonDiffuseGlowParticleData> codec() {
		return DaemonDiffuseGlowParticleData.MAP_CODEC;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, DaemonDiffuseGlowParticleData> streamCodec() {
		return DaemonDiffuseGlowParticleData.STREAM_CODEC;
	}
}
