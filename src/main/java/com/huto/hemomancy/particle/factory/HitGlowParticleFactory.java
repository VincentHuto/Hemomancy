package com.huto.hemomancy.particle.factory;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.data.HitColorParticleData;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.ParticleGlow;

import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.particles.IParticleData;

public class HitGlowParticleFactory implements ParticleProvider<HitColorParticleData> {
	private final SpriteSet spriteSet;
	public static final String NAME = "hit_glow";

	public HitGlowParticleFactory(SpriteSet sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle makeParticle(HitColorParticleData data, ClientLevel worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new ParticleGlow(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(), data.color.getGreen(),
				data.color.getBlue(), 3.0f, 1.25f, 75, this.spriteSet);
	}

	public static IParticleData createData(ParticleColor color) {
		return new HitColorParticleData(ParticleInit.hit_glow.get(), color);
	}

}