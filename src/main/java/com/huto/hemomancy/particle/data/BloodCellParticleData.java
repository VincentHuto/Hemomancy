package com.huto.hemomancy.particle.data;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.ParticleBloodCell;
import com.huto.hemomancy.particle.ParticleColor;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.IParticleData;

public class BloodCellParticleData implements IParticleFactory<BloodCellTypeData> {
	private final IAnimatedSprite spriteSet;
	public static final String NAME = "blood_cell";

	public BloodCellParticleData(IAnimatedSprite sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle makeParticle(BloodCellTypeData data, ClientWorld worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new ParticleBloodCell(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(),
				data.color.getGreen(), data.color.getBlue(), 1.0f, .025f, 32, this.spriteSet);

	}

	public static IParticleData createData(ParticleColor color) {
		return new BloodCellTypeData(ParticleInit.blood_cell.get(), color);
	}

}