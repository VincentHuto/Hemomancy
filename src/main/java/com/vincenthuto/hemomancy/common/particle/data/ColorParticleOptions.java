package com.vincenthuto.hemomancy.common.particle.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A particle that carries only a colour: blood cells, absorbed cells, claw and avatar-hit sparks, serpents, spores and
 * the hermit's edge glow. The registered type tells them apart. In a command or datapack the colour is three float
 * fields {@code r}, {@code g}, {@code b}; on the network it is {@link ParticleColor#serialize()} as one string.
 */
public final class ColorParticleOptions implements ParticleOptions {
	private final ParticleType<ColorParticleOptions> type;
	public final ParticleColor color;

	public ColorParticleOptions(ParticleType<ColorParticleOptions> type, ParticleColor color) {
		this.type = type;
		this.color = color;
	}

	@Override
	public ParticleType<ColorParticleOptions> getType() {
		return type;
	}

	/** The command and datapack form for {@code type}. */
	public static MapCodec<ColorParticleOptions> codec(ParticleType<ColorParticleOptions> type) {
		return RecordCodecBuilder.mapCodec(instance -> instance
				.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
						Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
						Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()))
				.apply(instance, (r, g, b) -> new ColorParticleOptions(type, new ParticleColor(r, g, b))));
	}

	/** The network form for {@code type}. */
	public static StreamCodec<RegistryFriendlyByteBuf, ColorParticleOptions> streamCodec(
			ParticleType<ColorParticleOptions> type) {
		return StreamCodec.composite(ByteBufCodecs.STRING_UTF8, d -> d.color.serialize(),
				s -> new ColorParticleOptions(type, ParticleColor.deserialize(s)));
	}
}
