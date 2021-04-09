package com.huto.hemomancy.particle.factory;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.ParticleSerpent;
import com.huto.hemomancy.particle.data.SerpentParticleData;
import com.huto.hemomancy.particle.util.ParticleColor;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.IParticleData;

public class SerpentParticleFactory implements IParticleFactory<SerpentParticleData> {
	private final IAnimatedSprite spriteSet;
	public static final String NAME = "serpent";

	public SerpentParticleFactory(IAnimatedSprite sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle makeParticle(SerpentParticleData data, ClientWorld worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new ParticleSerpent(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(), data.color.getGreen(),
				data.color.getBlue(), 3.0f, .15f, 106, this.spriteSet);
	}

	public static IParticleData createData(ParticleColor color) {
		return new SerpentParticleData(ParticleInit.serpent.get(), color);
	}

}