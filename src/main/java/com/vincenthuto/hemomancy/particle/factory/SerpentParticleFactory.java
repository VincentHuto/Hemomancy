package com.vincenthuto.hemomancy.particle.factory;

import com.vincenthuto.hemomancy.init.ParticleInit;
import com.vincenthuto.hemomancy.particle.ParticleSerpent;
import com.vincenthuto.hemomancy.particle.data.SerpentParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;

public class SerpentParticleFactory implements ParticleProvider<SerpentParticleData> {
	private final SpriteSet spriteSet;
	public static final String NAME = "serpent";

	public SerpentParticleFactory(SpriteSet sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle createParticle(SerpentParticleData data, ClientLevel worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new ParticleSerpent(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(), data.color.getGreen(),
				data.color.getBlue(), 3.0f, .15f, 106, this.spriteSet);
	}

	public static ParticleOptions createData(ParticleColor color) {
		return new SerpentParticleData(ParticleInit.serpent.get(), color);
	}

}