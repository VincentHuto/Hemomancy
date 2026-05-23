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
	private static final float VERTICAL_FRICTION = 0.88F;
	private static final float SWIM_TURN_ARC = 0.85F;
	private static final float FADE_START = 0.62F;
	private final float initialQuadSize;
	private final float rollSpeed;
	private final float swimAngle;
	private final double swimSpeed;
	private final float turnPhase;
	private final float turnSpeed;
	private final float writhePhase;
	private final float writheSpeed;
	private final double writheStrength;
	private final float counterWrithePhase;
	private final float counterWritheSpeed;

	public AbocipherParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed,
			double zSpeed, SpriteSet spriteSet) {
		super(level, x, y, z, 0.0D, 0.0D, 0.0D);
		this.pickSprite(spriteSet);
		this.xd = xSpeed * AbocipherParticleMotionRules.INHERITED_HORIZONTAL_DRIFT_SCALE;
		this.yd = ySpeed * AbocipherParticleMotionRules.UPWARD_DRIFT_SCALE;
		this.zd = zSpeed * AbocipherParticleMotionRules.INHERITED_HORIZONTAL_DRIFT_SCALE;
		this.lifetime = AbocipherParticleMotionRules.MIN_LIFETIME_TICKS
				+ this.random.nextInt(AbocipherParticleMotionRules.LIFETIME_TICK_RANGE);
		this.quadSize = 0.08F + this.random.nextFloat() * 0.04F;
		this.initialQuadSize = this.quadSize;
		this.alpha = 1.0F;
		this.hasPhysics = false;
		this.roll = this.random.nextFloat() * Mth.TWO_PI;
		this.oRoll = this.roll;
		this.rollSpeed = (this.random.nextFloat() - 0.5F) * 0.035F;
		this.swimAngle = this.random.nextFloat() * Mth.TWO_PI;
		this.swimSpeed = AbocipherParticleMotionRules.MIN_SWIM_SPEED
				+ this.random.nextDouble() * AbocipherParticleMotionRules.SWIM_SPEED_RANGE;
		this.turnPhase = this.random.nextFloat() * Mth.TWO_PI;
		this.turnSpeed = 0.025F + this.random.nextFloat() * 0.025F;
		this.writhePhase = this.random.nextFloat() * Mth.TWO_PI;
		this.writheSpeed = 0.18F + this.random.nextFloat() * 0.10F;
		this.writheStrength = 0.014D + this.random.nextDouble() * 0.014D;
		this.counterWrithePhase = this.random.nextFloat() * Mth.TWO_PI;
		this.counterWritheSpeed = 0.07F + this.random.nextFloat() * 0.05F;
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
		double turn = Mth.sin(this.turnPhase + motionAge * this.turnSpeed) * SWIM_TURN_ARC;
		double swimHeading = this.swimAngle + turn;
		double swimX = Math.cos(swimHeading) * this.swimSpeed;
		double swimZ = Math.sin(swimHeading) * this.swimSpeed;
		double sideWrithe = Mth.sin(this.writhePhase + motionAge * this.writheSpeed) * this.writheStrength;
		double wriggleX = Math.cos(swimHeading + Math.PI / 2.0D) * sideWrithe;
		double wriggleY = Mth.sin(this.counterWrithePhase + motionAge * this.counterWritheSpeed)
				* AbocipherParticleMotionRules.VERTICAL_BOB_STRENGTH;
		double wriggleZ = Math.sin(swimHeading + Math.PI / 2.0D) * sideWrithe;

		this.move(this.xd + swimX + wriggleX, this.yd + wriggleY, this.zd + swimZ + wriggleZ);
		this.xd *= FRICTION;
		this.yd *= VERTICAL_FRICTION;
		this.zd *= FRICTION;
		this.roll += this.rollSpeed + Mth.sin(this.writhePhase + motionAge * 0.11F) * 0.004F;

		float life = (float) this.age / (float) this.lifetime;
		float fade = Mth.clamp((life - FADE_START) / (1.0F - FADE_START), 0.0F, 1.0F);
		this.quadSize = this.initialQuadSize * (1.0F - fade * 0.35F);
		this.alpha = 1.0F - fade;
	}
}
