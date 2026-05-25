package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.client.particle.BloodCellParticle;
import com.vincenthuto.hemomancy.client.particle.data.BloodCellData;
import com.vincenthuto.hemomancy.common.init.ParticleInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;

public class BloodCellParticleFactory implements ParticleProvider<BloodCellData> {
	public static final String NAME = "blood_cell";
	public static final double FEED_SPIRAL_MARKER = AbocipherParticleFactory.FEED_SPIRAL_MARKER;
	private static final float FEED_ALPHA = 1.0f;
	private static final float FEED_SCALE = 0.18f;
	private static final int FEED_LIFETIME_TICKS = 28;

	public static ParticleOptions createData(ParticleColor color) {
		return new BloodCellData(ParticleInit.blood_cell.get(), color);
	}

	public static ParticleOptions createFeedData(ParticleColor color) {
		return createData(color);
	}

	private final SpriteSet spriteSet;

	public BloodCellParticleFactory(SpriteSet sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle createParticle(BloodCellData data, ClientLevel worldIn, double x, double y, double z, double xSpeed,
			double ySpeed, double zSpeed) {
		if (ySpeed <= FEED_SPIRAL_MARKER) {
			return new BloodCellParticle(worldIn, x, y, z, xSpeed, 0.012D, zSpeed, data.color.getRed(),
					data.color.getGreen(), data.color.getBlue(), FEED_ALPHA, FEED_SCALE, FEED_LIFETIME_TICKS * 2,
					this.spriteSet);
		}
		return new BloodCellParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(),
				data.color.getGreen(), data.color.getBlue(), 2.0f, .025f, 50, this.spriteSet);

	}

}
