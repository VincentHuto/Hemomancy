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

public class WillAbsorptionGlowParticleData implements ParticleOptions {
	public static final MapCodec<WillAbsorptionGlowParticleData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
			.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
					Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
					Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()),
					Codec.BOOL.fieldOf("outward_pulse").forGetter(d -> d.outwardPulse))
			.apply(instance, WillAbsorptionGlowParticleData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, WillAbsorptionGlowParticleData> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.STRING_UTF8, d -> d.color.serialize(),
					ByteBufCodecs.BOOL, d -> d.outwardPulse,
					(s, outwardPulse) -> new WillAbsorptionGlowParticleData(ParticleInit.will_absorption_glow.get(),
							ParticleColor.deserialize(s), outwardPulse));

	private final ParticleType<WillAbsorptionGlowParticleData> type;
	public final ParticleColor color;
	public final boolean outwardPulse;

	public WillAbsorptionGlowParticleData(float r, float g, float b, boolean outwardPulse) {
		this(ParticleInit.will_absorption_glow.get(), new ParticleColor(r, g, b), outwardPulse);
	}

	public WillAbsorptionGlowParticleData(ParticleType<WillAbsorptionGlowParticleData> type, ParticleColor color,
			boolean outwardPulse) {
		this.type = type;
		this.color = color;
		this.outwardPulse = outwardPulse;
	}

	@Override
	public ParticleType<WillAbsorptionGlowParticleData> getType() {
		return type;
	}
}
