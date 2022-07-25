package com.vincenthuto.hemomancy.particle.factory;

import com.vincenthuto.hemomancy.init.ParticleInit;
import com.vincenthuto.hemomancy.particle.data.BloodAvatarHitParticleData;
import com.vincenthuto.hutoslib.client.particle.ParticleGlow;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;

public class BloodAvatarHitParticleFactory implements ParticleProvider<BloodAvatarHitParticleData> {
	private final SpriteSet spriteSet;
	public static final String NAME = "blood_avatar_hit";

	public BloodAvatarHitParticleFactory(SpriteSet sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle createParticle(BloodAvatarHitParticleData data, ClientLevel worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new ParticleGlow(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(), data.color.getGreen(),
				data.color.getBlue(), 3.0f, 1.25f, 75, this.spriteSet);
	}

	public static ParticleOptions createData(ParticleColor color) {
		return new BloodAvatarHitParticleData(ParticleInit.blood_avatar_hit.get(), color);
	}

}