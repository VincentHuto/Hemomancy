package com.vincenthuto.hemomancy.client.particle.type;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.client.particle.data.WillAbsorptionGlowParticleData;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class WillAbsorptionGlowParticleType extends ParticleType<WillAbsorptionGlowParticleData> {
	public WillAbsorptionGlowParticleType() {
		super(false);
	}

	@Override
	public MapCodec<WillAbsorptionGlowParticleData> codec() {
		return WillAbsorptionGlowParticleData.MAP_CODEC;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, WillAbsorptionGlowParticleData> streamCodec() {
		return WillAbsorptionGlowParticleData.STREAM_CODEC;
	}
}
