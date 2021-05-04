package com.huto.hemomancy.particle.factory;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.ParticleBloodCell;
import com.huto.hemomancy.particle.data.BloodCellData;
import com.hutoslib.client.particle.ParticleColor;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.IParticleData;

public class BloodCellParticleFactory implements IParticleFactory<BloodCellData> {
	private final IAnimatedSprite spriteSet;
	public static final String NAME = "blood_cell";

	public BloodCellParticleFactory(IAnimatedSprite sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle makeParticle(BloodCellData data, ClientWorld worldIn, double x, double y, double z, double xSpeed,
			double ySpeed, double zSpeed) {
		return new ParticleBloodCell(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(),
				data.color.getGreen(), data.color.getBlue(), 2.0f, .025f, 50, this.spriteSet);

	}

	public static IParticleData createData(ParticleColor color) {
		return new BloodCellData(ParticleInit.blood_cell.get(), color);
	}

}