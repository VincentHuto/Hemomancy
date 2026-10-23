package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.common.particle.data.HitColorParticleData;
import com.vincenthuto.hutoslib.client.particle.ParticleGlow;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

public class HitGlowParticleFactory implements ParticleProvider<HitColorParticleData> {
	public static final String NAME = "hit_glow";

	private final SpriteSet spriteSet;

	public HitGlowParticleFactory(SpriteSet sprite) {
		this.spriteSet = sprite;
	}

	@Override
	public Particle createParticle(HitColorParticleData data, ClientLevel worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new ParticleGlow(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.red(), data.green(),
				data.blue(), 3.0f, 1.25f, 75, this.spriteSet);
	}

}
