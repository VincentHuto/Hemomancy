package com.vincenthuto.hemomancy.client.particle;

import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HermitEdgeGlowParticle extends TextureSheetParticle {
	private final float initialQuadSize;
	private final float initialAlpha;

	public HermitEdgeGlowParticle(ClientLevel level, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed, float r, float g, float b,
			float alpha, float scale, SpriteSet sprite) {
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

		this.setColor(r, g, b);
		this.initialAlpha = alpha;
		this.alpha = alpha;
		this.initialQuadSize = scale;
		this.quadSize = scale;
		this.lifetime = 12 + this.random.nextInt(7);
		this.xd = xSpeed;
		this.yd = ySpeed;
		this.zd = zSpeed;
		this.hasPhysics = false;
		this.pickSprite(sprite);
	}

	@Override
	public int getLightColor(float partialTick) {
		return 255;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return HLRenderTypeInit.GLOW_RENDER;
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

		this.move(this.xd, this.yd, this.zd);
		this.xd *= 0.42D;
		this.yd *= 0.22D;
		this.zd *= 0.42D;

		float life = (float) this.age / (float) this.lifetime;
		float fade = 1.0F - life;
		this.quadSize = this.initialQuadSize * fade;
		this.alpha = this.initialAlpha * fade;
	}
}
