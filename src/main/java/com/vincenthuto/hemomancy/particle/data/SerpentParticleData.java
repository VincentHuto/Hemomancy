package com.vincenthuto.hemomancy.particle.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.init.ParticleInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Simplified verison of ElementalCraft
 * https://github.com/Sirttas/ElementalCraft/blob/b91ca42b3d139904d9754d882a595406bad1bd18/src/main/java/sirttas/elementalcraft/particle/ElementTypeParticleData.java
 */

public class SerpentParticleData implements ParticleOptions {

	public static final Codec<SerpentParticleData> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
					Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
					Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()))
			.apply(instance, SerpentParticleData::new));
	public static final Deserializer<SerpentParticleData> DESERIALIZER = new Deserializer<>() {
		@Override
		public SerpentParticleData fromCommand(ParticleType<SerpentParticleData> type, StringReader reader)
				throws CommandSyntaxException {
			reader.expect(' ');
			return new SerpentParticleData(type, ParticleColor.deserialize(reader.readString()));
		}

		@Override
		public SerpentParticleData fromNetwork(ParticleType<SerpentParticleData> type, FriendlyByteBuf buffer) {
			return new SerpentParticleData(type, ParticleColor.deserialize(buffer.readUtf()));
		}
	};

	private ParticleType<SerpentParticleData> type;

	public ParticleColor color;

	public SerpentParticleData(float r, float g, float b) {
		this.color = new ParticleColor(r, g, b);
		this.type = ParticleInit.serpent.get();
	}

	public SerpentParticleData(ParticleType<SerpentParticleData> particleTypeData, ParticleColor color) {
		this.type = particleTypeData;
		this.color = color;
	}

	@Override
	public ParticleType<SerpentParticleData> getType() {
		return type;
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf packetBuffer) {
		packetBuffer.writeUtf(color.serialize());
	}

	@Override
	public String writeToString() {
		return ForgeRegistries.PARTICLE_TYPES.getKey(type).toString() + " " + color.serialize();
	}
}