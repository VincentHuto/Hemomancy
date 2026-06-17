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

public class HermitEdgeGlowParticleData implements ParticleOptions {
	public static final MapCodec<HermitEdgeGlowParticleData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
			.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
					Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
					Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()))
			.apply(instance, HermitEdgeGlowParticleData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, HermitEdgeGlowParticleData> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.STRING_UTF8, d -> d.color.serialize(),
					s -> new HermitEdgeGlowParticleData(ParticleInit.hermit_edge_glow.get(),
							ParticleColor.deserialize(s)));

	private final ParticleType<HermitEdgeGlowParticleData> type;
	public final ParticleColor color;

	public HermitEdgeGlowParticleData(float r, float g, float b) {
		this(ParticleInit.hermit_edge_glow.get(), new ParticleColor(r, g, b));
	}

	public HermitEdgeGlowParticleData(ParticleType<HermitEdgeGlowParticleData> type, ParticleColor color) {
		this.type = type;
		this.color = color;
	}

	@Override
	public ParticleType<HermitEdgeGlowParticleData> getType() {
		return this.type;
	}
}
