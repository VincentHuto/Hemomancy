package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.client.particle.BloodClawParticle;
import com.vincenthuto.hemomancy.common.particle.data.ColorParticleOptions;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

public class BloodClawParticleFactory implements ParticleProvider<ColorParticleOptions> {
	public static final String NAME = "blood_claw";

	private final SpriteSet spriteSet;

	public BloodClawParticleFactory(SpriteSet sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle createParticle(ColorParticleOptions data, ClientLevel worldIn, double x, double y, double z, double xSpeed,
			double ySpeed, double zSpeed) {
		return new BloodClawParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(),
				data.color.getGreen(), data.color.getBlue(), 2.0f, 0.2f, 50, this.spriteSet);

	}

}