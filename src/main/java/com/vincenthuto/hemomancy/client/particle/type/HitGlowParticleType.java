package com.vincenthuto.hemomancy.client.particle.type;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.client.particle.data.HitColorParticleData;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class HitGlowParticleType extends ParticleType<HitColorParticleData> {{
	public HitGlowParticleType() {{
		super(false);
	}}

	@Override
	public MapCodec<HitColorParticleData> codec() {{
		return HitColorParticleData.MAP_CODEC;
	}}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, HitColorParticleData> streamCodec() {{
		return HitColorParticleData.STREAM_CODEC;
	}}
}}
