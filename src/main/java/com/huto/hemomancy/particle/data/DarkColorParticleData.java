package com.huto.hemomancy.particle.data;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.util.ParticleColor;
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

public class DarkColorParticleData implements IParticleData {

	private ParticleType<DarkColorParticleData> type;
	public static final Codec<DarkColorParticleData> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
					Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
					Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()))
			.apply(instance, DarkColorParticleData::new));

	public ParticleColor color;

	public static final IParticleData.IDeserializer<DarkColorParticleData> DESERIALIZER = new IParticleData.IDeserializer<DarkColorParticleData>() {
		@Override
		public DarkColorParticleData deserialize(ParticleType<DarkColorParticleData> type, StringReader reader)
				throws CommandSyntaxException {
			reader.expect(' ');
			return new DarkColorParticleData(type, ParticleColor.deserialize(reader.readString()));
		}

		@Override
		public DarkColorParticleData read(ParticleType<DarkColorParticleData> type, PacketBuffer buffer) {
			return new DarkColorParticleData(type, ParticleColor.deserialize(buffer.readString()));
		}
	};

	public DarkColorParticleData(float r, float g, float b) {
		this.color = new ParticleColor(r, g, b);
		this.type = ParticleInit.dark_glow.get();
	}

	public DarkColorParticleData(ParticleType<DarkColorParticleData> particleTypeData, ParticleColor color) {
		this.type = particleTypeData;
		this.color = color;
	}

	@Override
	public ParticleType<DarkColorParticleData> getType() {
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