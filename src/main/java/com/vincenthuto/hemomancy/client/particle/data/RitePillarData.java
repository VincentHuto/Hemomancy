package com.vincenthuto.hemomancy.client.particle.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.init.ParticleInit;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record RitePillarData(float height) implements ParticleOptions {
	public static final MapCodec<RitePillarData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					com.mojang.serialization.Codec.FLOAT.fieldOf("height").forGetter(RitePillarData::height))
					.apply(instance, RitePillarData::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, RitePillarData> STREAM_CODEC =
			StreamCodec.composite(ByteBufCodecs.FLOAT, RitePillarData::height, RitePillarData::new);

	public RitePillarData {
		height = Math.max(0.25F, Math.min(16.0F, height));
	}

	@Override
	public ParticleType<?> getType() {
		return ParticleInit.rite_pillar.get();
	}
}
