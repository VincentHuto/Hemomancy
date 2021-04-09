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

public class ColoredDynamicData implements IParticleData {

	public ParticleType<ColoredDynamicData> type;
	public ParticleColor color;
	public float scale;
	public int age;

	public static final Codec<ColoredDynamicData> CODEC = RecordCodecBuilder
			.create(instance -> instance
					.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
							Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
							Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()),
							Codec.FLOAT.fieldOf("scale").forGetter(d -> d.scale),
							Codec.INT.fieldOf("age").forGetter(d -> d.age))
					.apply(instance, ColoredDynamicData::new));

	@Override
	public ParticleType<?> getType() {
		return type;
	}

	@SuppressWarnings("deprecation")
	public static final IDeserializer<ColoredDynamicData> DESERIALIZER = new IDeserializer<ColoredDynamicData>() {
		@Override
		public ColoredDynamicData deserialize(ParticleType<ColoredDynamicData> type, StringReader reader)
				throws CommandSyntaxException {
			reader.expect(' ');
			return new ColoredDynamicData(type, ParticleColor.deserialize(reader.readString()), reader.readFloat(),
					reader.readInt());
		}

		@Override
		public ColoredDynamicData read(ParticleType<ColoredDynamicData> type, PacketBuffer buffer) {
			return new ColoredDynamicData(type, ParticleColor.deserialize(buffer.readString()), buffer.readFloat(),
					buffer.readInt());
		}
	};

	public ColoredDynamicData(float r, float g, float b, float scale, int age) {
		this.type = ParticleInit.line.get();
		this.color = new ParticleColor(r, g, b);
		this.scale = scale;
		this.age = age;
	}

	public ColoredDynamicData(ParticleType<ColoredDynamicData> particleTypeData, ParticleColor color,
			float scale, int age) {
		this.type = particleTypeData;
		this.color = color;
		this.scale = scale;
		this.age = age;
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeString(color.serialize());
		buffer.writeFloat(scale);
		buffer.writeInt(age);
	}

	@Override
	public String getParameters() {
		return type.getRegistryName().toString() + " " + color.serialize() + " " + scale + " " + age;
	}
}