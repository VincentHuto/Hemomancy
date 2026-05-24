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
public class AbsorbedBloodCellParticle extends TextureSheetParticle {
	public float colorR = 0;
	public float colorG = 0;
	public float colorB = 0;
	public float initScale = 0;
	public float initAlpha = 0;
	private final double coordX;
	private final double coordY;
	private final double coordZ;
	private final double sourceX;
	private final double sourceY;
	private final double sourceZ;
	private double targetYOffset = 1.2D;
	private Vec3 firstPersonTargetAnchor;

	public AbsorbedBloodCellParticle(ClientLevel worldIn, double x, double y, double z, double vx, double vy, double vz,
			float r, float g, float b, float a, float scale, int lifetime, SpriteSet sprite) {
		super(worldIn, x, y, z, 0, 0, 0);
		this.colorR = r;
		this.colorG = g;
		this.colorB = b;
		if (this.colorR > 1.0) {
			this.colorR = this.colorR / 255.0f;
		}
		if (this.colorG > 1.0) {
			this.colorG = this.colorG / 255.0f;
		}
		if (this.colorB > 1.0) {
			this.colorB = this.colorB / 255.0f;
		}
		this.pickSprite(sprite);
		this.coordX = x;
		this.coordY = y;
		this.coordZ = z;
		this.xd = vx;
		this.yd = vy;
		this.zd = vz;
		this.sourceX = x + vx;
		this.sourceY = y + vy;
		this.sourceZ = z + vz;

		this.xo = x + xd;
		this.yo = y + yd;
		this.zo = z + zd;
		this.x = this.xo;
		this.y = this.yo;
		this.z = this.zo;
		this.setColor(colorR, colorG, colorB);
		this.lifetime = (int) (Math.random() * 10.0D) + 30;
		this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.2F);
		this.hasPhysics = false;
		this.lifetime = (int) (Math.random() * 10.0D) + 10;
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
		int i = super.getLightColor(partialTick);
		float f = (float) this.age / (float) this.lifetime;
		f = f * f;
		f = f * f;
		int j = i & 255;
		int k = i >> 16 & 255;
		k = k + (int) (f * 15.0F * 16.0F);
		if (k > 240) {
			k = 240;
		}

		return j | k << 16;
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
	public void move(double x, double y, double z) {
		this.setBoundingBox(this.getBoundingBox().move(x, y, z));
		this.setLocationFromBoundingbox();
	}

//	@Override
//	public boolean shouldCull() {
//		return false;
//	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			float f = (float) this.age / (float) this.lifetime;
			f = 1.0F - f;
			float f1 = 1.0F - f;
			f1 = f1 * f1;
			f1 = f1 * f1;
			if (this.firstPersonTargetAnchor != null) {
				this.setPosFromFirstPersonTarget();
			} else {
				this.x = this.coordX + this.xd * f;
				this.y = this.coordY + this.yd * f - f1 * this.targetYOffset;
				this.z = this.coordZ + this.zd * f;
			}
		}
	}

	private void setPosFromFirstPersonTarget() {
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 right = new Vec3(camera.getLeftVector()).scale(-this.firstPersonTargetAnchor.x);
		Vec3 up = new Vec3(camera.getUpVector()).scale(this.firstPersonTargetAnchor.y);
		Vec3 forward = new Vec3(camera.getLookVector()).scale(this.firstPersonTargetAnchor.z);
		Vec3 target = camera.getPosition().add(right).add(up).add(forward);

		float f = (float) this.age / (float) this.lifetime;
		f = 1.0F - f;
		float f1 = 1.0F - f;
		f1 = f1 * f1;
		f1 = f1 * f1;
		this.x = target.x + (this.sourceX - target.x) * f;
		this.y = target.y + (this.sourceY - target.y) * f - f1 * this.targetYOffset;
		this.z = target.z + (this.sourceZ - target.z) * f;
	}
}
