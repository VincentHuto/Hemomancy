package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.client.particle.AbocipherParticle;
import com.vincenthuto.hemomancy.common.init.ParticleInit;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;

public class AbocipherParticleFactory implements ParticleProvider<SimpleParticleType> {
	public static final String NAME = "abocipher";

	public static ParticleOptions createData() {
		return ParticleInit.abocipher.get();
	}

	private final SpriteSet spriteSet;

	public AbocipherParticleFactory(SpriteSet spriteSet) {
		this.spriteSet = spriteSet;
	}

	@Override
	public Particle createParticle(SimpleParticleType data, ClientLevel level, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new AbocipherParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
	}
}
