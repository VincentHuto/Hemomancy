package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.client.particle.AbocipherParticle;
import com.vincenthuto.hemomancy.client.particle.BloodStructureFeedAbocipherParticle;
import com.vincenthuto.hemomancy.common.init.ParticleInit;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;

public class AbocipherParticleFactory implements ParticleProvider<SimpleParticleType> {
	public static final String NAME = "abocipher";
	public static final double FEED_SPIRAL_MARKER = -1000.0D;

	public static ParticleOptions createData() {
		return ParticleInit.abocipher.get();
	}

	public static ParticleOptions createFeedData() {
		return createData();
	}

	private final SpriteSet spriteSet;

	public AbocipherParticleFactory(SpriteSet spriteSet) {
		this.spriteSet = spriteSet;
	}

	@Override
	public Particle createParticle(SimpleParticleType data, ClientLevel level, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		if (ySpeed <= FEED_SPIRAL_MARKER) {
			return new BloodStructureFeedAbocipherParticle(level, x, y, z, xSpeed, zSpeed, this.spriteSet);
		}
		return new AbocipherParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
	}
}
