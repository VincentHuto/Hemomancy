package com.huto.hemomancy.particle.data;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.ParticleLightning;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.IParticleData;

public class ParticleLightningData implements IParticleFactory<ColorLightningTypeData> {
	protected final IAnimatedSprite spriteSet;

	public ParticleLightningData(IAnimatedSprite p_i50846_1_) {
		this.spriteSet = p_i50846_1_;
	}

	public Particle makeParticle(ColorLightningTypeData typeIn, ClientWorld worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		ParticleLightning particle = new ParticleLightning(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet,
				typeIn.color.getRed(), typeIn.color.getGreen(), typeIn.color.getBlue(), typeIn.speed, typeIn.maxAge,
				typeIn.fract, typeIn.maxOffset);
		return particle;
	}

	public static IParticleData createData(ParticleColor color, int s, int a, int f, float o) {
		return new ColorLightningTypeData(ParticleInit.lightning_bolt.get(), color, s, a, f, o);
	}
}
