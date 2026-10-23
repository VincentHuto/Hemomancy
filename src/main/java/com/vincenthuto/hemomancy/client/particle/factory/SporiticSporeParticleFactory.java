package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.common.particle.data.ColorParticleOptions;
import com.vincenthuto.hutoslib.client.particle.ParticleGlow;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

public class SporiticSporeParticleFactory implements ParticleProvider<ColorParticleOptions> {
	public static final String NAME = "sporitic_spore";


	private final SpriteSet spriteSet;

	public SporiticSporeParticleFactory(SpriteSet spriteSet) {
		this.spriteSet = spriteSet;
	}

	@Override
	public Particle createParticle(ColorParticleOptions data, ClientLevel level, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new ParticleGlow(level, x, y, z, xSpeed, ySpeed + 0.0125D, zSpeed, data.color.getRed(),
				data.color.getGreen(), data.color.getBlue(), 1.15f, 0.38f, 48, spriteSet);
	}
}
