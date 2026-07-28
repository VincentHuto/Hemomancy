package com.vincenthuto.hemomancy.client.particle;

import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * A broad, low-opacity dark glow used to build the daemon's void silhouette.
 */
@OnlyIn(Dist.CLIENT)
public final class DaemonDiffuseGlowParticle extends TextureSheetParticle {
	private final float initialQuadSize;
	private final float initialAlpha;

	public DaemonDiffuseGlowParticle(ClientLevel level, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed, float scale, SpriteSet sprites) {
		super(level, x, y, z, 0.0D, 0.0D, 0.0D);
		this.setColor(0.018F, 0.002F, 0.012F);
		this.initialAlpha = 0.18F + random.nextFloat() * 0.05F;
		this.alpha = initialAlpha;
		this.initialQuadSize = Math.max(0.18F, scale * 5.2F)
				* (0.84F + random.nextFloat() * 0.32F);
		this.quadSize = initialQuadSize;
		this.lifetime = 24 + random.nextInt(9);
		this.xd = xSpeed * 0.12D + (random.nextDouble() - 0.5D) * 0.0025D;
		this.yd = ySpeed * 0.08D + (random.nextDouble() - 0.5D) * 0.0015D;
		this.zd = zSpeed * 0.12D + (random.nextDouble() - 0.5D) * 0.0025D;
		this.hasPhysics = false;
		this.pickSprite(sprites);
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
	public void tick() {
		xo = x;
		yo = y;
		zo = z;
		if (age++ >= lifetime) {
			remove();
			return;
		}
		move(xd, yd, zd);
		xd *= 0.94D;
		yd *= 0.92D;
		zd *= 0.94D;
		float life = age / (float) lifetime;
		float fade = 1.0F - life;
		this.quadSize = initialQuadSize * (1.0F + life * 0.35F);
		this.alpha = initialAlpha * fade * fade;
	}
}
