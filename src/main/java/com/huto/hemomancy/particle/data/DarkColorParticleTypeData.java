package com.huto.hemomancy.particle.data;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.ParticleColor;
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

public class DarkColorParticleTypeData implements IParticleData {

	private ParticleType<DarkColorParticleTypeData> type;
	public static final Codec<DarkColorParticleTypeData> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
					Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
					Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()))
			.apply(instance, DarkColorParticleTypeData::new));

	public ParticleColor color;

	public static final IParticleData.IDeserializer<DarkColorParticleTypeData> DESERIALIZER = new IParticleData.IDeserializer<DarkColorParticleTypeData>() {
		@Override
		public DarkColorParticleTypeData deserialize(ParticleType<DarkColorParticleTypeData> type, StringReader reader)
				throws CommandSyntaxException {
			reader.expect(' ');
			return new DarkColorParticleTypeData(type, ParticleColor.deserialize(reader.readString()));
		}

		@Override
		public DarkColorParticleTypeData read(ParticleType<DarkColorParticleTypeData> type, PacketBuffer buffer) {
			return new DarkColorParticleTypeData(type, ParticleColor.deserialize(buffer.readString()));
		}
	};

	public DarkColorParticleTypeData(float r, float g, float b) {
		this.color = new ParticleColor(r, g, b);
		this.type = ParticleInit.dark_glow.get();
	}

	public DarkColorParticleTypeData(ParticleType<DarkColorParticleTypeData> particleTypeData, ParticleColor color) {
		this.type = particleTypeData;
		this.color = color;
	}

	@Override
	public ParticleType<DarkColorParticleTypeData> getType() {
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