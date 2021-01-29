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

public class HitColorParticleTypeData implements IParticleData {

	private ParticleType<HitColorParticleTypeData> type;
	public static final Codec<HitColorParticleTypeData> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
					Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
					Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()))
			.apply(instance, HitColorParticleTypeData::new));

	public ParticleColor color;

	@SuppressWarnings("deprecation")
	public static final IParticleData.IDeserializer<HitColorParticleTypeData> DESERIALIZER = new IParticleData.IDeserializer<HitColorParticleTypeData>() {
		@Override
		public HitColorParticleTypeData deserialize(ParticleType<HitColorParticleTypeData> type, StringReader reader)
				throws CommandSyntaxException {
			reader.expect(' ');
			return new HitColorParticleTypeData(type, ParticleColor.deserialize(reader.readString()));
		}

		@Override
		public HitColorParticleTypeData read(ParticleType<HitColorParticleTypeData> type, PacketBuffer buffer) {
			return new HitColorParticleTypeData(type, ParticleColor.deserialize(buffer.readString()));
		}
	};

	public HitColorParticleTypeData(float r, float g, float b) {
		this.color = new ParticleColor(r, g, b);
		this.type = ParticleInit.hit_glow.get();
	}

	public HitColorParticleTypeData(ParticleType<HitColorParticleTypeData> particleTypeData, ParticleColor color) {
		this.type = particleTypeData;
		this.color = color;
	}

	@Override
	public ParticleType<HitColorParticleTypeData> getType() {
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