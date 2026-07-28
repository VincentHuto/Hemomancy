package com.vincenthuto.hemomancy.client.particle.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.ParticleInit;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DaemonDiffuseGlowParticleData(float scale) implements ParticleOptions {
	public static final MapCodec<DaemonDiffuseGlowParticleData> MAP_CODEC =
			Codec.FLOAT.fieldOf("scale").xmap(
					DaemonDiffuseGlowParticleData::new,
					DaemonDiffuseGlowParticleData::scale);

	public static final StreamCodec<RegistryFriendlyByteBuf, DaemonDiffuseGlowParticleData> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.FLOAT, DaemonDiffuseGlowParticleData::scale,
					DaemonDiffuseGlowParticleData::new);

	@Override
	public ParticleType<?> getType() {
		return ParticleInit.daemon_diffuse_glow.get();
	}
}
