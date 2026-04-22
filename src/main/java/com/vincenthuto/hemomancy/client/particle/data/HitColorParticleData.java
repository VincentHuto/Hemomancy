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

/**
 * Simplified verison of ElementalCraft
 * https://github.com/Sirttas/ElementalCraft/blob/b91ca42b3d139904d9754d882a595406bad1bd18/src/main/java/sirttas/elementalcraft/particle/ElementTypeParticleData.java
 */

public class HitColorParticleData implements ParticleOptions {

	public static final MapCodec<HitColorParticleData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
			.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
					Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
					Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()))
			.apply(instance, HitColorParticleData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, HitColorParticleData> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.STRING_UTF8, d -> d.color.serialize(),
					s -> new HitColorParticleData(ParticleInit.hit_glow.get(), ParticleColor.deserialize(s)));

	private ParticleType<HitColorParticleData> type;

	public ParticleColor color;

	public HitColorParticleData(float r, float g, float b) {
		this.color = new ParticleColor(r, g, b);
		this.type = ParticleInit.hit_glow.get();
	}

	public HitColorParticleData(ParticleType<HitColorParticleData> particleTypeData, ParticleColor color) {
		this.type = particleTypeData;
		this.color = color;
	}

	@Override
	public ParticleType<HitColorParticleData> getType() {
		return type;
	}
}
