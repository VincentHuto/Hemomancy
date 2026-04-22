package com.vincenthuto.hemomancy.client.particle.type;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.client.particle.data.SerpentParticleData;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class SerpentParticleType extends ParticleType<SerpentParticleData> {{
	public SerpentParticleType() {{
		super(false);
	}}

	@Override
	public MapCodec<SerpentParticleData> codec() {{
		return SerpentParticleData.MAP_CODEC;
	}}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, SerpentParticleData> streamCodec() {{
		return SerpentParticleData.STREAM_CODEC;
	}}
}}
