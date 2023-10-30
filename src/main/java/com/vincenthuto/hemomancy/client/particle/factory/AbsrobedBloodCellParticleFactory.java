package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.client.particle.AbsorbedBloodCellParticle;
import com.vincenthuto.hemomancy.client.particle.data.AbsorbedBloodCellData;
import com.vincenthuto.hemomancy.common.registry.ParticleInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;

public class AbsrobedBloodCellParticleFactory implements ParticleProvider<AbsorbedBloodCellData> {
	public static final String NAME = "absorbed_blood_cell";
	public static ParticleOptions createData(ParticleColor color) {
		return new AbsorbedBloodCellData(ParticleInit.absorbed_blood_cell.get(), color);
	}

	private final SpriteSet spriteSet;

	public AbsrobedBloodCellParticleFactory(SpriteSet sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle createParticle(AbsorbedBloodCellData data, ClientLevel worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new AbsorbedBloodCellParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(),
				data.color.getGreen(), data.color.getBlue(), 2.0f, .025f, 20, this.spriteSet);

	}

}