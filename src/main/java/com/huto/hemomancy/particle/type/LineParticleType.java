package com.huto.hemomancy.particle.type;

import com.huto.hemomancy.particle.data.ColoredDynamicData;
import com.mojang.serialization.Codec;

import net.minecraft.particles.ParticleType;

public class LineParticleType extends ParticleType<ColoredDynamicData> {
	public LineParticleType() {
		super(false, ColoredDynamicData.DESERIALIZER);
	}

	@Override
	public Codec<ColoredDynamicData> func_230522_e_() {
		return ColoredDynamicData.CODEC;
	}
}