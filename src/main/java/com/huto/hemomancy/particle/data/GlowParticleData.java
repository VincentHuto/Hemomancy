package com.huto.hemomancy.particle.data;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.ParticleGlow;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.IParticleData;

public class GlowParticleData implements IParticleFactory<ColorParticleTypeData> {
	private final IAnimatedSprite spriteSet;
	public static final String NAME = "glow";

	public GlowParticleData(IAnimatedSprite sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle makeParticle(ColorParticleTypeData data, ClientWorld worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new ParticleGlow(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(), data.color.getGreen(),
				data.color.getBlue(), 1.0f, .25f, 36, this.spriteSet);
	}

	public static IParticleData createData(ParticleColor color) {
		return new ColorParticleTypeData(ParticleInit.glow.get(), color);
	}

}