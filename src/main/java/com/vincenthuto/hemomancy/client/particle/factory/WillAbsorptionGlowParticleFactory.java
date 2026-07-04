package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.client.particle.WillAbsorptionGlowParticle;
import com.vincenthuto.hemomancy.client.particle.data.WillAbsorptionGlowParticleData;
import com.vincenthuto.hemomancy.common.init.ParticleInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;

public class WillAbsorptionGlowParticleFactory implements ParticleProvider<WillAbsorptionGlowParticleData> {
	public static final String NAME = "will_absorption_glow";

	public static ParticleOptions createData(ParticleColor color) {
		return new WillAbsorptionGlowParticleData(ParticleInit.will_absorption_glow.get(), color, false);
	}

	public static ParticleOptions createPulseData(ParticleColor color) {
		return new WillAbsorptionGlowParticleData(ParticleInit.will_absorption_glow.get(), color, true);
	}

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
