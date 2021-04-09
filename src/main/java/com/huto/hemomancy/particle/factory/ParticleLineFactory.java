package com.huto.hemomancy.particle.factory;

import java.util.Random;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.ParticleLine;
import com.huto.hemomancy.particle.data.ColoredDynamicData;
import com.huto.hemomancy.particle.util.ParticleColor;
import com.huto.hemomancy.particle.util.ParticleUtil;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.IParticleData;

public class ParticleLineFactory implements IParticleFactory<ColoredDynamicData> {
	private final IAnimatedSprite spriteSet;
	public static final String NAME = "line";

	public static Random random = new Random();

	public ParticleLineFactory(IAnimatedSprite sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle makeParticle(ColoredDynamicData data, ClientWorld worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new ParticleLine(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(), data.color.getGreen(),
				data.color.getBlue(), data.scale, data.age, this.spriteSet);

	}

	public static IParticleData createData(ParticleColor color) {
		return new ColoredDynamicData(ParticleInit.line.get(), color, (float) ParticleUtil.inRange(0.05, 0.15),
				40 + random.nextInt(20));
	}

	public static IParticleData createData(ParticleColor color, float scale, int age) {
		return new ColoredDynamicData(ParticleInit.line.get(), color, scale, age);
	}
}