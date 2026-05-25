package com.vincenthuto.hemomancy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BloodStructureFeedAbocipherParticle extends TextureSheetParticle {
	private static final int LIFETIME_TICKS = 28;
	private static final int LIFETIME_VARIANCE_TICKS = 8;
	private static final double DRIFT_DAMPING = 0.90D;
	private static final float FADE_START = 0.55F;
	private final float initialQuadSize;
	private final float rollSpeed;
	private final float curlPhase;

	public BloodStructureFeedAbocipherParticle(ClientLevel level, double x, double y, double z,
			double xSpeed, double zSpeed, SpriteSet spriteSet) {
		super(level, x, y, z, 0.0D, 0.0D, 0.0D);
		this.pickSprite(spriteSet);
		this.xd = xSpeed;
		this.yd = 0.010D + this.random.nextDouble() * 0.006D;
		this.zd = zSpeed;
		this.lifetime = LIFETIME_TICKS + this.random.nextInt(LIFETIME_VARIANCE_TICKS);
		this.quadSize = 0.065F + this.random.nextFloat() * 0.025F;
		this.initialQuadSize = this.quadSize;
		this.alpha = 1.0F;
		this.hasPhysics = false;
		this.roll = this.random.nextFloat() * Mth.TWO_PI;
		this.oRoll = this.roll;
		this.rollSpeed = (this.random.nextFloat() - 0.5F) * 0.055F;
		this.curlPhase = this.random.nextFloat() * Mth.TWO_PI;
	}

	@Override
	public int getLightColor(float partialTick) {
		return LightTexture.FULL_BRIGHT;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.oRoll = this.roll;

		if (this.age++ >= this.lifetime) {
			this.remove();
			return;
		}

		float motionAge = this.age;
		double curl = Mth.sin(this.curlPhase + motionAge * 0.31F) * 0.005D;
		this.move(this.xd + curl, this.yd, this.zd - curl);
		this.xd *= DRIFT_DAMPING;
		this.yd *= 0.94D;
		this.zd *= DRIFT_DAMPING;
		this.roll += this.rollSpeed;

		float life = (float) this.age / (float) this.lifetime;
		float fade = Mth.clamp((life - FADE_START) / (1.0F - FADE_START), 0.0F, 1.0F);
		this.quadSize = this.initialQuadSize * (1.0F - fade * 0.45F);
		this.alpha = 1.0F - fade;
	}
}
