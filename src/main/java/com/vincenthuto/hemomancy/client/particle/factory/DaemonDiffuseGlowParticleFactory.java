package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.client.particle.DaemonDiffuseGlowParticle;
import com.vincenthuto.hemomancy.client.particle.data.DaemonDiffuseGlowParticleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;

public final class DaemonDiffuseGlowParticleFactory
		implements ParticleProvider<DaemonDiffuseGlowParticleData> {
	private final SpriteSet sprites;

	public DaemonDiffuseGlowParticleFactory(SpriteSet sprites) {
		this.sprites = sprites;
	}

	public static ParticleOptions createData(float scale) {
		return new DaemonDiffuseGlowParticleData(
				Math.max(0.02F, Math.min(1.0F, scale)));
	}

	@Override
	public Particle createParticle(DaemonDiffuseGlowParticleData data,
			ClientLevel level, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new DaemonDiffuseGlowParticle(level, x, y, z,
				xSpeed, ySpeed, zSpeed, data.scale(), sprites);
	}
}
