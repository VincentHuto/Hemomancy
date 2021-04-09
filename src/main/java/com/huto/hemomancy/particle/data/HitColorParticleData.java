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

public class HitColorParticleData implements IParticleData {

	private ParticleType<HitColorParticleData> type;
	public static final Codec<HitColorParticleData> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
					Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
					Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()))
			.apply(instance, HitColorParticleData::new));

	public ParticleColor color;

	@SuppressWarnings("deprecation")
	public static final IParticleData.IDeserializer<HitColorParticleData> DESERIALIZER = new IParticleData.IDeserializer<HitColorParticleData>() {
		@Override
		public HitColorParticleData deserialize(ParticleType<HitColorParticleData> type, StringReader reader)
				throws CommandSyntaxException {
			reader.expect(' ');
			return new HitColorParticleData(type, ParticleColor.deserialize(reader.readString()));
		}

		@Override
		public HitColorParticleData read(ParticleType<HitColorParticleData> type, PacketBuffer buffer) {
			return new HitColorParticleData(type, ParticleColor.deserialize(buffer.readString()));
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
	public void write(PacketBuffer packetBuffer) {
		packetBuffer.writeString(color.serialize());
	}

	@Override
	public String getParameters() {
		return type.getRegistryName().toString() + " " + color.serialize();
	}
}