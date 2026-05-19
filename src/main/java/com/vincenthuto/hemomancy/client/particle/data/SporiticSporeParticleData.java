package com.vincenthuto.hemomancy.client.particle.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.init.ParticleInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SporiticSporeParticleData implements ParticleOptions {
	public static final MapCodec<SporiticSporeParticleData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
			.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
					Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
					Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()))
			.apply(instance, SporiticSporeParticleData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SporiticSporeParticleData> STREAM_CODEC =
			StreamCodec.composite(ByteBufCodecs.STRING_UTF8, d -> d.color.serialize(),
					s -> new SporiticSporeParticleData(ParticleInit.sporitic_spore.get(),
							ParticleColor.deserialize(s)));

	private final ParticleType<SporiticSporeParticleData> type;
	public final ParticleColor color;

	public SporiticSporeParticleData(float r, float g, float b) {
		this(ParticleInit.sporitic_spore.get(), new ParticleColor(r, g, b));
	}

	public SporiticSporeParticleData(ParticleType<SporiticSporeParticleData> type, ParticleColor color) {
		this.type = type;
		this.color = color;
	}

	@Override
	public ParticleType<SporiticSporeParticleData> getType() {
		return type;
	}
}
