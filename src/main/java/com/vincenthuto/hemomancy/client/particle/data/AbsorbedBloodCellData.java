package com.vincenthuto.hemomancy.client.particle.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.registry.ParticleInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Simplified verison of ElementalCraft
 * https://github.com/Sirttas/ElementalCraft/blob/b91ca42b3d139904d9754d882a595406bad1bd18/src/main/java/sirttas/elementalcraft/particle/ElementTypeParticleData.java
 */

public class AbsorbedBloodCellData implements ParticleOptions {

	public static final Codec<AbsorbedBloodCellData> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
					Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
					Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()))
			.apply(instance, AbsorbedBloodCellData::new));
	public static final Deserializer<AbsorbedBloodCellData> DESERIALIZER = new Deserializer<>() {
		@Override
		public AbsorbedBloodCellData fromCommand(ParticleType<AbsorbedBloodCellData> type, StringReader reader)
				throws CommandSyntaxException {
			reader.expect(' ');
			return new AbsorbedBloodCellData(type, ParticleColor.deserialize(reader.readString()));
		}

		@Override
		public AbsorbedBloodCellData fromNetwork(ParticleType<AbsorbedBloodCellData> type, FriendlyByteBuf buffer) {
			return new AbsorbedBloodCellData(type, ParticleColor.deserialize(buffer.readUtf()));
		}
	};

	private ParticleType<AbsorbedBloodCellData> type;

	public ParticleColor color;

	public AbsorbedBloodCellData(float r, float g, float b) {
		this.color = new ParticleColor(r, g, b);
		this.type = ParticleInit.absorbed_blood_cell.get();
	}

	public AbsorbedBloodCellData(ParticleType<AbsorbedBloodCellData> particleTypeData, ParticleColor color) {
		this.type = particleTypeData;
		this.color = color;
	}

	@Override
	public ParticleType<AbsorbedBloodCellData> getType() {
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