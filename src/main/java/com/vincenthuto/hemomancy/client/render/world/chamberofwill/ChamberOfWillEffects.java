package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.vincenthuto.hemomancy.client.data.ChamberOfWillClientData;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ChamberOfWillEffects extends DimensionSpecialEffects {
	public ChamberOfWillEffects() {
		super(Float.NaN, false, SkyType.NONE, false, false);
	}

	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera,
							 Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
		PoseStack poseStack = new PoseStack();
		poseStack.mulPose(modelViewMatrix);
		ChamberSkyTheme theme = ChamberSkyThemeRegistry.activeTheme();
		float f = (ticks + partialTick) * theme.motionMultiplier();
		float membranePulse = ChamberOfWillRenderHelpers.membranePulse(f) * theme.pulseStrength();
		int qliphothPomeCount = ChamberOfWillClientData.qliphothPomesConsumed();
		ChamberThemeRenderContext context = new ChamberThemeRenderContext(level, ticks, partialTick, modelViewMatrix,
				camera, projectionMatrix, isFoggy, setupFog, poseStack, Tesselator.getInstance(), theme, f, 128.0F,
				membranePulse, qliphothPomeCount);
		return ChamberSkyThemeRegistry.activeEffects().renderSky(context);
	}

	public void renderBorderAura(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix,
								 Camera camera, Matrix4f projectionMatrix) {
		ChamberOfWillRenderHelpers.renderBorderAura(level, ticks, partialTick, modelViewMatrix, camera,
				projectionMatrix);
	}

	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
		return ChamberSkyThemeRegistry.activeEffects().getBrightnessDependentFogColor(fogColor, brightness);
	}

	@Override
	public boolean isFoggyAt(int x, int y) {
		return ChamberSkyThemeRegistry.activeEffects().isFoggyAt(x, y);
	}
}
