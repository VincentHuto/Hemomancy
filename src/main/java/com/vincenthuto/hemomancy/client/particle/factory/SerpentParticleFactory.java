package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.client.particle.SerpentParticle;
import com.vincenthuto.hemomancy.common.particle.data.ColorParticleOptions;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

public class SerpentParticleFactory implements ParticleProvider<ColorParticleOptions> {
	public static final String NAME = "serpent";

	private final SpriteSet spriteSet;

	public SerpentParticleFactory(SpriteSet sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle createParticle(ColorParticleOptions data, ClientLevel worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new SerpentParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(), data.color.getGreen(),
				data.color.getBlue(), 3.0f, .15f, 106, this.spriteSet);
	}

}