package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

interface ChamberThemeEffects {
	ResourceLocation id();

	boolean renderSky(ChamberThemeRenderContext context);

	default Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
		return fogColor;
	}

	default boolean isFoggyAt(int x, int y) {
		return false;
	}
}
