package com.vincenthuto.hemomancy.client.particle;

import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WillAbsorptionGlowParticle extends TextureSheetParticle {
	private final double coordX;
	private final double coordY;
	private final double coordZ;
	private final double sourceX;
	private final double sourceY;
	private final double sourceZ;
	private final float initialQuadSize;
	private final float initialAlpha;
	private final boolean outwardPulse;
	private double targetYOffset = 0.0D;
	private Vec3 firstPersonTargetAnchor;

	public WillAbsorptionGlowParticle(ClientLevel level, double x, double y, double z,
			double vx, double vy, double vz, float r, float g, float b, float alpha, float scale, boolean outwardPulse,
			SpriteSet sprite) {
		super(level, x, y, z, 0.0D, 0.0D, 0.0D);
		if (r > 1.0F) {
			r /= 255.0F;
		}
		if (g > 1.0F) {
			g /= 255.0F;
		}
		if (b > 1.0F) {
			b /= 255.0F;
		}

		this.coordX = x;
		this.coordY = y;
		this.coordZ = z;
		this.xd = vx;
		this.yd = vy;
		this.zd = vz;
		this.outwardPulse = outwardPulse;
		this.sourceX = x + vx;
		this.sourceY = y + vy;
		this.sourceZ = z + vz;
		if (outwardPulse) {
			this.xo = x;
			this.yo = y;
			this.zo = z;
			this.x = x;
			this.y = y;
			this.z = z;
		} else {
			this.xo = this.sourceX;
			this.yo = this.sourceY;
			this.zo = this.sourceZ;
			this.x = this.sourceX;
			this.y = this.sourceY;
			this.z = this.sourceZ;
		}
		this.initialAlpha = alpha;
		this.alpha = alpha;
		this.initialQuadSize = scale * (this.random.nextFloat() * 0.45F + 0.75F);
		this.quadSize = this.initialQuadSize;
		this.lifetime = 18 + this.random.nextInt(9);
		this.hasPhysics = false;
		this.setColor(r, g, b);
		this.pickSprite(sprite);
	}

	public void setFirstPersonTargetAnchor(Vec3 firstPersonTargetAnchor) {
		this.firstPersonTargetAnchor = firstPersonTargetAnchor;
		this.targetYOffset = 0.0D;
		this.setPosFromFirstPersonTarget();
	}

	public void setTargetYOffset(double targetYOffset) {
		this.targetYOffset = targetYOffset;
	}

	@Override
	public int getLightColor(float partialTick) {
		return 255;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return HLRenderTypeInit.DARK_GLOW_RENDER;
	}

	@Override
	public boolean isAlive() {
		return this.age < this.lifetime;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
			return;
		}

		if (this.firstPersonTargetAnchor != null) {
			this.setPosFromFirstPersonTarget();
		} else if (this.outwardPulse) {
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.88D;
			this.yd *= 0.88D;
			this.zd *= 0.88D;
		} else {
			float f = 1.0F - (float) this.age / (float) this.lifetime;
			float falloff = 1.0F - f;
			falloff = falloff * falloff;
			this.x = this.coordX + this.xd * f;
			this.y = this.coordY + this.yd * f - falloff * this.targetYOffset;
			this.z = this.coordZ + this.zd * f;
		}

		float life = (float) this.age / (float) this.lifetime;
		float fade = 1.0F - life;
		this.quadSize = this.initialQuadSize * (0.45F + 0.55F * fade);
		this.alpha = this.initialAlpha * fade;
	}

	private void setPosFromFirstPersonTarget() {
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 right = new Vec3(camera.getLeftVector()).scale(-this.firstPersonTargetAnchor.x);
		Vec3 up = new Vec3(camera.getUpVector()).scale(this.firstPersonTargetAnchor.y);
		Vec3 forward = new Vec3(camera.getLookVector()).scale(this.firstPersonTargetAnchor.z);
		Vec3 target = camera.getPosition().add(right).add(up).add(forward);

		float f = 1.0F - (float) this.age / (float) this.lifetime;
		float falloff = 1.0F - f;
		falloff = falloff * falloff;
		this.x = target.x + (this.sourceX - target.x) * f;
		this.y = target.y + (this.sourceY - target.y) * f - falloff * this.targetYOffset;
		this.z = target.z + (this.sourceZ - target.z) * f;
	}
}
