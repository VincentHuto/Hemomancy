package com.huto.hemomancy.particle.type;

import com.huto.hemomancy.particle.data.ColorLightningTypeData;
import com.mojang.serialization.Codec;

import net.minecraft.particles.ParticleType;

public class LightningParticleType extends ParticleType<ColorLightningTypeData> {
	public LightningParticleType() {
		super(false, ColorLightningTypeData.DESERIALIZER);
	}

	@Override
	public Codec<ColorLightningTypeData> func_230522_e_() {
		return ColorLightningTypeData.CODEC;
	}
}