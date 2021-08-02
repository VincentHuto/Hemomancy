package com.huto.hemomancy.particle.factory;

import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.particle.ParticleAbsorbedBloodCell;
import com.huto.hemomancy.particle.data.AbsorbedBloodCellData;
import com.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.particles.IParticleData;

public class AbsrobedBloodCellParticleFactory implements ParticleProvider<AbsorbedBloodCellData> {
	private final SpriteSet spriteSet;
	public static final String NAME = "absorbed_blood_cell";

	public AbsrobedBloodCellParticleFactory(SpriteSet sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle makeParticle(AbsorbedBloodCellData data, ClientLevel worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new ParticleAbsorbedBloodCell(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(),
				data.color.getGreen(), data.color.getBlue(), 2.0f, .025f, 20, this.spriteSet);

	}

	public static IParticleData createData(ParticleColor color) {
		return new AbsorbedBloodCellData(ParticleInit.absorbed_blood_cell.get(), color);
	}

}