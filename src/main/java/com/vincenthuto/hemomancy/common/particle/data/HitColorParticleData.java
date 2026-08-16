package com.vincenthuto.hemomancy.common.particle.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.init.ParticleInit;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record HitColorParticleData(float red, float green, float blue) implements ParticleOptions {
	public static final MapCodec<HitColorParticleData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
			.group(Codec.FLOAT.fieldOf("r").forGetter(HitColorParticleData::red),
					Codec.FLOAT.fieldOf("g").forGetter(HitColorParticleData::green),
					Codec.FLOAT.fieldOf("b").forGetter(HitColorParticleData::blue))
			.apply(instance, HitColorParticleData::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, HitColorParticleData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, HitColorParticleData::red,
			ByteBufCodecs.FLOAT, HitColorParticleData::green,
			ByteBufCodecs.FLOAT, HitColorParticleData::blue,
			HitColorParticleData::new);

	@Override @SuppressWarnings("unchecked")
	public ParticleType<HitColorParticleData> getType() {
		return (ParticleType<HitColorParticleData>) (ParticleType<?>) ParticleInit.hit_glow.get();
	}
}
