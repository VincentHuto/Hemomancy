package com.vincenthuto.hemomancy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LetheanDripParticle extends TextureSheetParticle {
	private final SpriteSet spriteSet;
	private final float initialQuadSize;

	public LetheanDripParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed,
			double zSpeed, SpriteSet spriteSet) {
		super(level, x, y, z, 0.0D, 0.0D, 0.0D);
		this.spriteSet = spriteSet;
		this.pickSprite(spriteSet);
		this.setColor(0.96F, 0.96F, 1.0F);
		this.alpha = 0.9F;
		this.quadSize = 0.035F + this.random.nextFloat() * 0.015F;
		this.initialQuadSize = this.quadSize;
		this.lifetime = 18 + this.random.nextInt(12);
		this.xd = 0.0D;
		this.yd = -0.012D;
		this.zd = 0.0D;
		this.gravity = 0.012F;
		this.friction = 0.96F;
		this.hasPhysics = false;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.removed) {
			this.xd = 0.0D;
			this.zd = 0.0D;
			float life = (float) this.age / (float) this.lifetime;
			this.alpha = 0.9F * (1.0F - Mth.clamp((life - 0.65F) / 0.35F, 0.0F, 1.0F));
			this.quadSize = this.initialQuadSize * (1.0F - life * 0.2F);
			this.setSpriteFromAge(this.spriteSet);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		public Provider(SpriteSet spriteSet) {
			this.spriteSet = spriteSet;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
				double xSpeed, double ySpeed, double zSpeed) {
			return new LetheanDripParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
		}
	}
}
