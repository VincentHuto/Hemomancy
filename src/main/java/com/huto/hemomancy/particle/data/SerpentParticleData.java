package com.huto.hemomancy.particle.data;

import com.huto.hemomancy.init.ParticleInit;
import com.hutoslib.client.particle.util.ParticleColor;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;

/**
 * Simplified verison of ElementalCraft
 * https://github.com/Sirttas/ElementalCraft/blob/b91ca42b3d139904d9754d882a595406bad1bd18/src/main/java/sirttas/elementalcraft/particle/ElementTypeParticleData.java
 */

public class SerpentParticleData implements IParticleData {

	private ParticleType<SerpentParticleData> type;
	public static final Codec<SerpentParticleData> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
					Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
					Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()))
			.apply(instance, SerpentParticleData::new));

	public ParticleColor color;

	public static final IParticleData.IDeserializer<SerpentParticleData> DESERIALIZER = new IParticleData.IDeserializer<SerpentParticleData>() {
		@Override
		public SerpentParticleData deserialize(ParticleType<SerpentParticleData> type, StringReader reader)
				throws CommandSyntaxException {
			reader.expect(' ');
			return new SerpentParticleData(type, ParticleColor.deserialize(reader.readString()));
		}

		@Override
		public SerpentParticleData read(ParticleType<SerpentParticleData> type, PacketBuffer buffer) {
			return new SerpentParticleData(type, ParticleColor.deserialize(buffer.readString()));
		}
	};

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
	public void write(PacketBuffer packetBuffer) {
		packetBuffer.writeString(color.serialize());
	}

	@Override
	public String getParameters() {
		return type.getRegistryName().toString() + " " + color.serialize();
	}
}