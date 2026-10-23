package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.common.particle.data.ColorParticleOptions;
import com.vincenthuto.hutoslib.client.particle.ParticleGlow;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

public class BloodAvatarHitParticleFactory implements ParticleProvider<ColorParticleOptions> {
	public static final String NAME = "blood_avatar_hit";

	private final SpriteSet spriteSet;

	public BloodAvatarHitParticleFactory(SpriteSet sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle createParticle(ColorParticleOptions data, ClientLevel worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new ParticleGlow(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(), data.color.getGreen(),
				data.color.getBlue(), 3.0f, 1.25f, 75, this.spriteSet);
	}

}