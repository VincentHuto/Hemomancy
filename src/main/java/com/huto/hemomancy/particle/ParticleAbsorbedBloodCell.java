package com.huto.hemomancy.particle;

import com.huto.hemomancy.init.RenderTypeInit;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParticleAbsorbedBloodCell extends SpriteTexturedParticle {
	public float colorR = 0;
	public float colorG = 0;
	public float colorB = 0;
	public float initScale = 0;
	public float initAlpha = 0;
	private final double coordX;
	private final double coordY;
	private final double coordZ;

	public ParticleAbsorbedBloodCell(ClientWorld worldIn, double x, double y, double z, double vx, double vy, double vz,
			float r, float g, float b, float a, float scale, int lifetime, IAnimatedSprite sprite) {
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
		this.selectSpriteRandomly(sprite);
		this.coordX = x;
		this.coordY = y;
		this.coordZ = z;
		this.motionX = vx;
		this.motionY = vy;
		this.motionZ = vz;

		this.prevPosX = x + motionX;
		this.prevPosY = y + motionY;
		this.prevPosZ = z + motionZ;
		this.posX = this.prevPosX;
		this.posY = this.prevPosY;
		this.posZ = this.prevPosZ;
		this.setColor(colorR, colorG, colorB);
		this.maxAge = (int) (Math.random() * 10.0D) + 30;
		this.particleScale = 0.1F * (this.rand.nextFloat() * 0.5F + 0.2F);
		this.canCollide = false;
		this.maxAge = (int) (Math.random() * 10.0D) + 10;
	}

	@Override
	public boolean shouldCull() {
		return false;
	}

	
	@Override
	public IParticleRenderType getRenderType() {
		return RenderTypeInit.DARK_GLOW_RENDER;
	}

	@Override
	public void tick() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		if (this.age++ >= this.maxAge) {
			this.setExpired();
		} else {
			float f = (float) this.age / (float) this.maxAge;
			f = 1.0F - f;
			float f1 = 1.0F - f;
			f1 = f1 * f1;
			f1 = f1 * f1;
			this.posX = this.coordX + this.motionX * (double) f;
			this.posY = this.coordY + this.motionY * (double) f - (double) (f1 * 1.2F);
			this.posZ = this.coordZ + this.motionZ * (double) f;
		}
	}

	@Override
	public void move(double x, double y, double z) {
		this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
		this.resetPositionToBB();
	}

	@Override
	public boolean isAlive() {
		return this.age < this.maxAge;
	}

	@Override
	public int getBrightnessForRender(float partialTick) {
		int i = super.getBrightnessForRender(partialTick);
		float f = (float) this.age / (float) this.maxAge;
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
}