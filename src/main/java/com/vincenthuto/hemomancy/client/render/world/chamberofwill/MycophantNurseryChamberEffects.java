package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.phys.Vec3;

/** Complete organic Chamber environment used by the Mycophant Nursery and its command preview. */
final class MycophantNurseryChamberEffects extends AbstractChamberThemeEffects {
	MycophantNurseryChamberEffects(ChamberSkyTheme theme) {
		super(theme);
	}

	@Override
	protected void renderBaseSkybox(ChamberThemeRenderContext context) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		ChamberOfWillRenderHelpers.renderSolidBox(context.poseStack(), context.tesselator(),
				context.skyDistance(), context.theme().skyboxColor());
	}

	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
		float light = 0.42F + brightness * 0.28F;
		return new Vec3(0.055D * light, 0.13D * light, 0.065D * light);
	}

	@Override
	public boolean isFoggyAt(int x, int y) {
		return true;
	}
}
