package com.huto.hemomancy.particle.factory;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.ParticleBloodClaw;
import com.huto.hemomancy.particle.data.BloodClawData;
import com.hutoslib.client.particle.ParticleColor;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.IParticleData;

public class BloodClawParticleFactory implements IParticleFactory<BloodClawData> {
	private final IAnimatedSprite spriteSet;
	public static final String NAME = "blood_claw";

	public BloodClawParticleFactory(IAnimatedSprite sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle makeParticle(BloodClawData data, ClientWorld worldIn, double x, double y, double z, double xSpeed,
			double ySpeed, double zSpeed) {
		return new ParticleBloodClaw(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(),
				data.color.getGreen(), data.color.getBlue(), 2.0f, 0.2f, 50, this.spriteSet);

	}

	public static IParticleData createData(ParticleColor color) {
		return new BloodClawData(ParticleInit.blood_claw.get(), color);
	}

}