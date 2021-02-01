package com.huto.hemomancy.particle.data;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.ParticleSerpent;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.IParticleData;

public class SerpentParticleData implements IParticleFactory<SerpentParticleTypeData> {
	private final IAnimatedSprite spriteSet;
	public static final String NAME = "serpent";

	public SerpentParticleData(IAnimatedSprite sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle makeParticle(SerpentParticleTypeData data, ClientWorld worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new ParticleSerpent(worldIn, x, y, z, xSpeed, ySpeed , zSpeed, data.color.getRed(), data.color.getGreen(),
				data.color.getBlue(), 3.0f, .15f, 106, this.spriteSet);
	}

	public static IParticleData createData(ParticleColor color) {
		return new SerpentParticleTypeData(ParticleInit.serpent.get(), color);
	}

}