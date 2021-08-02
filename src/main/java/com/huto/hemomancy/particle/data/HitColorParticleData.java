package com.huto.hemomancy.particle.data;

import com.huto.hemomancy.init.ParticleInit;
import com.hutoslib.client.particle.util.ParticleColor;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Simplified verison of ElementalCraft
 * https://github.com/Sirttas/ElementalCraft/blob/b91ca42b3d139904d9754d882a595406bad1bd18/src/main/java/sirttas/elementalcraft/particle/ElementTypeParticleData.java
 */

public class HitColorParticleData implements ParticleOptions {

	private ParticleType<HitColorParticleData> type;
	public static final Codec<HitColorParticleData> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
					Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
					Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()))
			.apply(instance, HitColorParticleData::new));

	public ParticleColor color;

	@SuppressWarnings("deprecation")
	public static final Deserializer<HitColorParticleData> DESERIALIZER = new Deserializer<HitColorParticleData>() {
		@Override
		public HitColorParticleData fromCommand(ParticleType<HitColorParticleData> type, StringReader reader)
				throws CommandSyntaxException {
			reader.expect(' ');
			return new HitColorParticleData(type, ParticleColor.deserialize(reader.readString()));
		}

		@Override
		public HitColorParticleData fromNetwork(ParticleType<HitColorParticleData> type, FriendlyByteBuf buffer) {
			return new HitColorParticleData(type, ParticleColor.deserialize(buffer.readUtf()));
		}
	};

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

	@Override
	public void writeToNetwork(FriendlyByteBuf packetBuffer) {
		packetBuffer.writeUtf(color.serialize());
	}

	@Override
	public String writeToString() {
		return type.getRegistryName().toString() + " " + color.serialize();
	}
}