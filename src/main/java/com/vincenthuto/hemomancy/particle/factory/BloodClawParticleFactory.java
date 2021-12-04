package com.vincenthuto.hemomancy.particle.factory;

import com.vincenthuto.hemomancy.init.ParticleInit;
import com.vincenthuto.hemomancy.particle.ParticleBloodClaw;
import com.vincenthuto.hemomancy.particle.data.BloodClawData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;

public class BloodClawParticleFactory implements ParticleProvider<BloodClawData> {
	private final SpriteSet spriteSet;
	public static final String NAME = "blood_claw";

	public BloodClawParticleFactory(SpriteSet sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle createParticle(BloodClawData data, ClientLevel worldIn, double x, double y, double z, double xSpeed,
			double ySpeed, double zSpeed) {
		return new ParticleBloodClaw(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(),
				data.color.getGreen(), data.color.getBlue(), 2.0f, 0.2f, 50, this.spriteSet);

	}

	public static ParticleOptions createData(ParticleColor color) {
		return new BloodClawData(ParticleInit.blood_claw.get(), color);
	}

}