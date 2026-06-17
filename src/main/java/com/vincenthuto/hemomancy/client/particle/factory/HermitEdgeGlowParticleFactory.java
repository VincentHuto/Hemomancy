package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.client.particle.HermitEdgeGlowParticle;
import com.vincenthuto.hemomancy.client.particle.data.HermitEdgeGlowParticleData;
import com.vincenthuto.hemomancy.common.init.ParticleInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;

public class HermitEdgeGlowParticleFactory implements ParticleProvider<HermitEdgeGlowParticleData> {
	public static final String NAME = "hermit_edge_glow";

	public static ParticleOptions createData(ParticleColor color) {
		return new HermitEdgeGlowParticleData(ParticleInit.hermit_edge_glow.get(), color);
	}

	private final SpriteSet spriteSet;

	public HermitEdgeGlowParticleFactory(SpriteSet spriteSet) {
		this.spriteSet = spriteSet;
	}

	@Override
	public Particle createParticle(HermitEdgeGlowParticleData data, ClientLevel level, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new HermitEdgeGlowParticle(level, x, y, z, xSpeed, ySpeed, zSpeed,
				data.color.getRed(), data.color.getGreen(), data.color.getBlue(), 0.86f, 0.016f, this.spriteSet);
	}
}
