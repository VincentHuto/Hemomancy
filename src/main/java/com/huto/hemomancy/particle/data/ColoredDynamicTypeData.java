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

public class ColoredDynamicTypeData implements IParticleData {

	public ParticleType<ColoredDynamicTypeData> type;
	public ParticleColor color;
	float scale;
	public int age;

	public static final Codec<ColoredDynamicTypeData> CODEC = RecordCodecBuilder
			.create(instance -> instance
					.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
							Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
							Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()),
							Codec.FLOAT.fieldOf("scale").forGetter(d -> d.scale),
							Codec.INT.fieldOf("age").forGetter(d -> d.age))
					.apply(instance, ColoredDynamicTypeData::new));

	@Override
	public ParticleType<?> getType() {
		return type;
	}

	@SuppressWarnings("deprecation")
	public static final IDeserializer<ColoredDynamicTypeData> DESERIALIZER = new IDeserializer<ColoredDynamicTypeData>() {
		@Override
		public ColoredDynamicTypeData deserialize(ParticleType<ColoredDynamicTypeData> type, StringReader reader)
				throws CommandSyntaxException {
			reader.expect(' ');
			return new ColoredDynamicTypeData(type, ParticleColor.deserialize(reader.readString()), reader.readFloat(),
					reader.readInt());
		}

		@Override
		public ColoredDynamicTypeData read(ParticleType<ColoredDynamicTypeData> type, PacketBuffer buffer) {
			return new ColoredDynamicTypeData(type, ParticleColor.deserialize(buffer.readString()), buffer.readFloat(),
					buffer.readInt());
		}
	};

	public ColoredDynamicTypeData(float r, float g, float b, float scale, int age) {
		this.type = ParticleInit.line.get();
		this.color = new ParticleColor(r, g, b);
		this.scale = scale;
		this.age = age;
	}

	public ColoredDynamicTypeData(ParticleType<ColoredDynamicTypeData> particleTypeData, ParticleColor color,
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