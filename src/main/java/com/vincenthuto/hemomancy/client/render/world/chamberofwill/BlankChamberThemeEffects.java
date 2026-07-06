package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

final class BlankChamberThemeEffects extends AbstractChamberThemeEffects {

	BlankChamberThemeEffects(ChamberSkyTheme theme) {
		super(theme);
	}

	@Override
	protected void renderBaseSkybox(ChamberThemeRenderContext context) {
		if (context.theme().renderBaseSkybox()) {
			ChamberOfWillRenderHelpers.renderSolidBox(context.poseStack(), context.tesselator(),
					context.skyDistance(), context.theme().skyboxColor());
		}
	}
}
