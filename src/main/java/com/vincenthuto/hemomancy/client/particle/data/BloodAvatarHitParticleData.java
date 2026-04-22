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

public class BloodAvatarHitParticleData implements ParticleOptions {

	public static final MapCodec<BloodAvatarHitParticleData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
			.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
					Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
					Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()))
			.apply(instance, BloodAvatarHitParticleData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BloodAvatarHitParticleData> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.STRING_UTF8, d -> d.color.serialize(),
					s -> new BloodAvatarHitParticleData(ParticleInit.blood_avatar_hit.get(), ParticleColor.deserialize(s)));

	private ParticleType<BloodAvatarHitParticleData> type;

	public ParticleColor color;

	public BloodAvatarHitParticleData(float r, float g, float b) {
		this.color = new ParticleColor(r, g, b);
		this.type = ParticleInit.blood_avatar_hit.get();
	}

	public BloodAvatarHitParticleData(ParticleType<BloodAvatarHitParticleData> particleTypeData, ParticleColor color) {
		this.type = particleTypeData;
		this.color = color;
	}

	@Override
	public ParticleType<BloodAvatarHitParticleData> getType() {
		return type;
	}
}
