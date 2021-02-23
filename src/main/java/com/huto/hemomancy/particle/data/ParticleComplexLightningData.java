package com.huto.hemomancy.particle.data;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.ParticleLightning;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.IParticleData;

public class ParticleComplexLightningData implements IParticleFactory<ColorParticleTypeData> {
	protected final IAnimatedSprite spriteSet;

	public ParticleComplexLightningData(IAnimatedSprite p_i50846_1_) {
		this.spriteSet = p_i50846_1_;
	}

	public ParticleComplexLightningData(IAnimatedSprite p_i50846_1_, int speed, int maxAge) {
		this.spriteSet = p_i50846_1_;
	}

	public Particle makeParticle(ColorParticleTypeData typeIn, ClientWorld worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		ParticleLightning particle = new ParticleLightning(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet,
				typeIn.color.getRed(), typeIn.color.getGreen(), typeIn.color.getBlue(), -3, 640);
		return particle;
	}

	public static IParticleData createData(ParticleColor color) {
		return new ColorParticleTypeData(ParticleInit.lightning_bolt.get(), color);
	}

}
