package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import net.minecraft.util.Mth;

final class VesperFightFissureStyle {
	static final float GLOW_Y = -0.032F;
	static final float SURFACE_Y = -0.030F;

	private VesperFightFissureStyle() {
	}

	static float alphaScale(float time, int fissureIndex, int segmentIndex, int segmentCount) {
		float breath = 0.42F + 0.58F * (0.5F + 0.5F * Mth.sin(time * 0.06F + fissureIndex * 1.37F));
		float branchPosition = (segmentIndex + 1.0F) / (segmentCount + 1.0F);
		float branchFade = 0.35F + 0.65F * Mth.sin(Mth.PI * branchPosition);
		return Mth.clamp(breath * branchFade, 0.0F, 1.0F);
	}

	static int coreAlpha(int baseAlpha, float alphaScale) {
		return Mth.clamp(Math.round(baseAlpha * alphaScale), 0, 255);
	}

	static int glowAlpha(int baseAlpha, float alphaScale) {
		return Mth.clamp(Math.round(baseAlpha * alphaScale * 0.38F), 0, 255);
	}
}
