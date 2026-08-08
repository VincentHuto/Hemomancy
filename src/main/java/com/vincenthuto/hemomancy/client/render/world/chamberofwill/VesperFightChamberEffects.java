package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.phys.Vec3;

/**
 * Complete Vesper fight environment module. Until later scenery pieces are authored,
 * it deliberately supplies only an absolute-black void around the fight floor.
 */
final class VesperFightChamberEffects extends AbstractChamberThemeEffects {
	VesperFightChamberEffects(ChamberSkyTheme theme) {
		super(theme);
	}

	@Override
	protected void renderBaseSkybox(ChamberThemeRenderContext context) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		ChamberOfWillRenderHelpers.renderSolidBox(
				context.poseStack(), context.tesselator(), context.skyDistance(), 0xFF000000);
	}

	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
		return Vec3.ZERO;
	}
}
