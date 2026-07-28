package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.client.particle.RitePillarParticle;
import com.vincenthuto.hemomancy.client.particle.data.RitePillarData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;

public final class RitePillarParticleFactory implements ParticleProvider<RitePillarData> {
	private final SpriteSet sprites;

	public RitePillarParticleFactory(SpriteSet sprites) {
		this.sprites = sprites;
	}

	public static ParticleOptions createData(float height) {
		return new RitePillarData(height);
	}

	@Override
	public Particle createParticle(RitePillarData data, ClientLevel level,
			double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		return new RitePillarParticle(level, x, y, z, data.height(), sprites);
	}
}
