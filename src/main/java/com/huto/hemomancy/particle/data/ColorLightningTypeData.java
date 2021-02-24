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

public class ColorLightningTypeData implements IParticleData {

	private ParticleType<ColorLightningTypeData> type;
	public static final Codec<ColorLightningTypeData> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
					Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
					Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()),
					Codec.INT.fieldOf("s").forGetter(d -> d.speed), Codec.INT.fieldOf("a").forGetter(d -> d.maxAge),
					Codec.INT.fieldOf("f").forGetter(d -> d.fract),
					Codec.FLOAT.fieldOf("o").forGetter(d -> d.maxOffset)).apply(instance, ColorLightningTypeData::new));

	public ParticleColor color;
	public int speed, maxAge, fract;
	public float maxOffset;

	@SuppressWarnings("deprecation")
	public static final IParticleData.IDeserializer<ColorLightningTypeData> DESERIALIZER = new IParticleData.IDeserializer<ColorLightningTypeData>() {
		@Override
		public ColorLightningTypeData deserialize(ParticleType<ColorLightningTypeData> type, StringReader reader)
				throws CommandSyntaxException {
			reader.expect(' ');
			return new ColorLightningTypeData(type, ParticleColor.deserialize(reader.readString()), reader.readInt(),
					reader.readInt(), reader.readInt(), reader.readFloat());
		}

		@Override
		public ColorLightningTypeData read(ParticleType<ColorLightningTypeData> type, PacketBuffer buffer) {
			return new ColorLightningTypeData(type, ParticleColor.deserialize(buffer.readString()), buffer.readInt(),
					buffer.readInt(), buffer.readInt(), buffer.readFloat());
		}
	};

	public ColorLightningTypeData(float r, float g, float b, int s, int a, int f, float o) {
		this.color = new ParticleColor(r, g, b);
		this.type = ParticleInit.lightning_bolt.get();
		this.speed = s;
		this.maxAge = a;
		this.fract = f;
		this.maxOffset = o;
	}

	public ColorLightningTypeData(ParticleType<ColorLightningTypeData> particleTypeData, ParticleColor color, int s,
			int a, int f, float o) {
		this.type = particleTypeData;
		this.color = color;
		this.speed = s;
		this.maxAge = a;
		this.fract = f;
		this.maxOffset = o;
	}

	@Override
	public ParticleType<ColorLightningTypeData> getType() {
		return type;
	}

	@Override
	public void write(PacketBuffer packetBuffer) {
		packetBuffer.writeString(color.serialize());
		packetBuffer.writeInt(speed);
		packetBuffer.writeInt(maxAge);
		packetBuffer.writeInt(fract);
		packetBuffer.writeFloat(maxOffset);

	}

	@Override
	public String getParameters() {
		return type.getRegistryName().toString() + " " + color.serialize() + " " + speed + " " + maxAge + " " + fract
				+ " " + maxOffset;
	}
}