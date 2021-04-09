package com.huto.hemomancy.particle.factory;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.ParticleGlow;
import com.huto.hemomancy.particle.data.ColorParticleData;
import com.huto.hemomancy.particle.util.ParticleColor;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.IParticleData;

public class GlowParticleFactory implements IParticleFactory<ColorParticleData> {
	private final IAnimatedSprite spriteSet;
	public static final String NAME = "glow";

	public GlowParticleFactory(IAnimatedSprite sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle makeParticle(ColorParticleData data, ClientWorld worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new ParticleGlow(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(), data.color.getGreen(),
				data.color.getBlue(), 1.0f, .035f, 136, this.spriteSet);

	}

	public static IParticleData createData(ParticleColor color) {
		return new ColorParticleData(ParticleInit.glow.get(), color);
	}

}