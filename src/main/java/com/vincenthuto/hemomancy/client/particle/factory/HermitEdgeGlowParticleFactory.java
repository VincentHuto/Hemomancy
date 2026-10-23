package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.client.particle.HermitEdgeGlowParticle;
import com.vincenthuto.hemomancy.common.particle.data.ColorParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

public class HermitEdgeGlowParticleFactory implements ParticleProvider<ColorParticleOptions> {
	public static final String NAME = "hermit_edge_glow";


	private final SpriteSet spriteSet;

	public HermitEdgeGlowParticleFactory(SpriteSet spriteSet) {
		this.spriteSet = spriteSet;
	}

	@Override
	public Particle createParticle(ColorParticleOptions data, ClientLevel level, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new HermitEdgeGlowParticle(level, x, y, z, xSpeed, ySpeed, zSpeed,
				data.color.getRed(), data.color.getGreen(), data.color.getBlue(), 0.86f, 0.016f, this.spriteSet);
	}
}
