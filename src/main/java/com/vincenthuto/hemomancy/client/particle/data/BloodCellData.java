package com.vincenthuto.hemomancy.client.particle.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.init.ParticleInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Simplified verison of ElementalCraft
 * https://github.com/Sirttas/ElementalCraft/blob/b91ca42b3d139904d9754d882a595406bad1bd18/src/main/java/sirttas/elementalcraft/particle/ElementTypeParticleData.java
 */

public class BloodCellData implements ParticleOptions {

	public static final Codec<BloodCellData> CODEC = RecordCodecBuilder
			.create(instance -> instance
					.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
							Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
							Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()))
					.apply(instance, BloodCellData::new));
	public static final Deserializer<BloodCellData> DESERIALIZER = new Deserializer<>() {
		@Override
		public BloodCellData fromCommand(ParticleType<BloodCellData> type, StringReader reader)
				throws CommandSyntaxException {
			reader.expect(' ');
			return new BloodCellData(type, ParticleColor.deserialize(reader.readString()));
		}

		@Override
		public BloodCellData fromNetwork(ParticleType<BloodCellData> type, FriendlyByteBuf buffer) {
			return new BloodCellData(type, ParticleColor.deserialize(buffer.readUtf()));
		}
	};

	private ParticleType<BloodCellData> type;

	public ParticleColor color;

	public BloodCellData(float r, float g, float b) {
		this.color = new ParticleColor(r, g, b);
		this.type = ParticleInit.blood_cell.get();
	}

	public BloodCellData(ParticleType<BloodCellData> particleTypeData, ParticleColor color) {
		this.type = particleTypeData;
		this.color = color;
	}

	@Override
	public ParticleType<BloodCellData> getType() {
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