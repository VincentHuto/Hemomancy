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
public class AbocipherParticle extends TextureSheetParticle {
	private static final float FRICTION = 0.96F;
	private final float initialQuadSize;
	private final float rollSpeed;
	private final float writhePhase;
	private final float writheSpeed;
	private final float writheStrength;
	private final float counterWrithePhase;
	private final float counterWritheSpeed;

	public AbocipherParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed,
			double zSpeed, SpriteSet spriteSet) {
		super(level, x, y, z, 0.0D, 0.0D, 0.0D);
		this.pickSprite(spriteSet);
		this.xd = xSpeed;
		this.yd = ySpeed + 0.015D;
		this.zd = zSpeed;
		this.lifetime = 28 + this.random.nextInt(18);
		this.quadSize = 0.08F + this.random.nextFloat() * 0.04F;
		this.initialQuadSize = this.quadSize;
		this.alpha = 1.0F;
		this.hasPhysics = false;
		this.roll = this.random.nextFloat() * Mth.TWO_PI;
		this.oRoll = this.roll;
		this.rollSpeed = (this.random.nextFloat() - 0.5F) * 0.035F;
		this.writhePhase = this.random.nextFloat() * Mth.TWO_PI;
		this.writheSpeed = 0.15F + this.random.nextFloat() * 0.08F;
		this.writheStrength = 0.0035F + this.random.nextFloat() * 0.0035F;
		this.counterWrithePhase = this.random.nextFloat() * Mth.TWO_PI;
		this.counterWritheSpeed = 0.08F + this.random.nextFloat() * 0.07F;
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
		double wriggleX = Mth.sin(this.writhePhase + motionAge * this.writheSpeed) * this.writheStrength;
		double wriggleY = Mth.sin(this.counterWrithePhase + motionAge * this.counterWritheSpeed)
				* this.writheStrength * 0.35D;
		double wriggleZ = Mth.cos(this.counterWrithePhase + motionAge * this.writheSpeed * 0.85F)
				* this.writheStrength;

		this.move(this.xd + wriggleX, this.yd + wriggleY, this.zd + wriggleZ);
		this.xd *= FRICTION;
		this.yd *= FRICTION;
		this.zd *= FRICTION;
		this.yd += 0.001D;
		this.roll += this.rollSpeed + Mth.sin(this.writhePhase + motionAge * 0.11F) * 0.004F;

		float life = (float) this.age / (float) this.lifetime;
		this.quadSize = this.initialQuadSize * (1.0F - life * 0.35F);
		this.alpha = 1.0F - life;
	}
}
