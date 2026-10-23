package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.client.particle.WillAbsorptionGlowParticle;
import com.vincenthuto.hemomancy.common.particle.data.WillAbsorptionGlowParticleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

public class WillAbsorptionGlowParticleFactory implements ParticleProvider<WillAbsorptionGlowParticleData> {
	public static final String NAME = "will_absorption_glow";


	private final SpriteSet spriteSet;

	public WillAbsorptionGlowParticleFactory(SpriteSet spriteSet) {
		this.spriteSet = spriteSet;
	}

	@Override
	public Particle createParticle(WillAbsorptionGlowParticleData data, ClientLevel level, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
		return new WillAbsorptionGlowParticle(level, x, y, z, xSpeed, ySpeed, zSpeed,
				data.color.getRed(), data.color.getGreen(), data.color.getBlue(), 0.86F, 0.105F, data.outwardPulse,
				this.spriteSet);
	}
}
