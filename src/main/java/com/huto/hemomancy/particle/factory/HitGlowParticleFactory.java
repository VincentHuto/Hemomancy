package com.huto.hemomancy.particle.factory;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.data.HitColorParticleData;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.ParticleGlow;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.IParticleData;

public class HitGlowParticleFactory implements IParticleFactory<HitColorParticleData> {
	private final IAnimatedSprite spriteSet;
	public static final String NAME = "hit_glow";

	public HitGlowParticleFactory(IAnimatedSprite sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle makeParticle(HitColorParticleData data, ClientWorld worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new ParticleGlow(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(), data.color.getGreen(),
				data.color.getBlue(), 3.0f, 1.25f, 75, this.spriteSet);
	}

	public static IParticleData createData(ParticleColor color) {
		return new HitColorParticleData(ParticleInit.hit_glow.get(), color);
	}

}