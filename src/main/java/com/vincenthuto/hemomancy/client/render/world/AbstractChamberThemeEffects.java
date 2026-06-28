package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.util.Random;

abstract class AbstractChamberThemeEffects implements ChamberThemeEffects {
	private final ChamberSkyTheme theme;

	protected AbstractChamberThemeEffects(ChamberSkyTheme theme) {
		this.theme = theme;
	}

	@Override
	public final ResourceLocation id() {
		return theme.id();
	}

	protected final ChamberSkyTheme theme() {
		return theme;
	}

	@Override
	public boolean renderSky(ChamberThemeRenderContext context) {
		PoseStack poseStack = context.poseStack();
		Tesselator tesselator = context.tesselator();
		ChamberSkyTheme theme = context.theme();
		float skyDistance = context.skyDistance();
		float f = context.time();
		float membranePulse = context.membranePulse();

		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(true);

		renderBaseSkybox(context);

		float scale = .80f; // give buffer so rotated cubes don't clip through main skybox
		int layers = 6;
		Random random = new Random(431);
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		RenderSystem.depthMask(false);
		if (theme.renderCloudLayers()) {
			for (int i = 0; i < layers; i++) {
				poseStack.pushPose();
				int j = layers - i - 1;
				float speed = (0.01f + i * i * 0.09f) * .015f;
				float x = (i * 68731 + f * speed * (random.nextFloat() - 0.5f)) % 360;
				float y = (i * 74869 + f * speed * (random.nextFloat() - 0.5f)) % 360;
				float z = (i * 98744 + f * speed * (random.nextFloat() - 0.5f)) % 360;
				poseStack.mulPose(Axis.XP.rotationDegrees(x));
				poseStack.mulPose(Axis.YP.rotationDegrees(y));
				poseStack.mulPose(Axis.ZP.rotationDegrees(z));
				Vector3f rgb = new Vector3f(random.nextFloat() * 0.5f + 0.5f,
						random.nextFloat() * 0.5f + 0.5f, random.nextFloat() * 0.5f + 0.5f);
				float intensity = Mth.lerp(j / (float) layers, 0.25f, 0.18f);
				intensity *= 1.0F - membranePulse * 0.32F;
				rgb.mul(intensity);
				rgb = new Vector3f(Math.min(rgb.x, 1), Math.min(rgb.y, 1), Math.min(rgb.z, 1));
				RenderSystem.setShaderColor(rgb.x, rgb.y, rgb.z, 1f);
				ChamberOfWillRenderHelpers.renderBox(poseStack, tesselator, skyDistance * scale, 0,
						4f + 2f * scale, GameRenderer::getPositionTexColorShader, theme.cloudTexture(),
						theme.cloudColor());
				poseStack.popPose();
				scale -= 0.04f; // give slight separation between layers to prevent too much zfighting/artifacting
			}
		} else {
			scale -= 0.04f * layers;
		}

		var color = ChamberOfWillRenderHelpers.colorVector(theme.nebulaPrimary());
		color.mul(0.075f);
		// use ever-enclosing z offset to ensure new planes are always in front of old planes, preventing alpha clipping
		if (theme.renderNebulaLayers()) {
			float zoff = ChamberOfWillRenderHelpers.renderNebula(poseStack, color, random, f, skyDistance,
					tesselator, scale, 0f, theme.wispTexture());
			color = ChamberOfWillRenderHelpers.colorVector(theme.nebulaSecondary());
			color.mul(0.125f);
			zoff = ChamberOfWillRenderHelpers.renderNebula(poseStack, color, random, f, skyDistance, tesselator,
					scale, zoff, theme.wispTexture());
			color = ChamberOfWillRenderHelpers.colorVector(theme.nebulaAccent());
			color.mul(0.125f);
			ChamberOfWillRenderHelpers.renderNebula(poseStack, color, random, f, skyDistance, tesselator, scale,
					zoff, theme.wispTexture());
		}

		renderAfterNebula(context);
		RenderSystem.depthMask(true);
		renderBeforeSharedLayers(context);

		int capillaryDepthLayers = theme.capillaryLayers();
		int blueVeinDepthLayers = theme.blueVeinLayers();
		int bloodVesselDepthLayers = theme.bloodVesselLayers();
		int neuralDepthLayers = theme.neuralLayers();

		//ChamberOfWillRenderHelpers.renderCorticalFolds(poseStack, tesselator, f, skyDistance);
		ChamberOfWillRenderHelpers.renderCapillaryWeb(poseStack, tesselator, f, skyDistance, capillaryDepthLayers, theme);
		ChamberOfWillRenderHelpers.renderBlueVeins(poseStack, tesselator, f, skyDistance, blueVeinDepthLayers, theme);
		ChamberOfWillRenderHelpers.renderBloodVessels(poseStack, tesselator, f, skyDistance, membranePulse, bloodVesselDepthLayers, theme);
		ChamberOfWillRenderHelpers.renderNeuralStructures(poseStack, tesselator, f, skyDistance, neuralDepthLayers, theme);
		renderAfterSharedLayers(context);
		if (theme.renderMembranePulse()) {
			ChamberOfWillRenderHelpers.renderMembranePulseVignette(poseStack, tesselator, skyDistance,
					membranePulse);
		}

		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		return true;
	}

	protected void renderBaseSkybox(ChamberThemeRenderContext context) {
		PoseStack poseStack = context.poseStack();
		Tesselator tesselator = context.tesselator();
		float skyDistance = context.skyDistance();
		ChamberSkyTheme theme = context.theme();
		if (theme.renderBaseSkybox()) {
			ChamberOfWillRenderHelpers.renderBox(poseStack, tesselator, skyDistance, 0, 1,
					GameRenderer::getPositionTexColorShader, theme.skyTexture(), theme.skyboxColor());
		}
	}

	protected void renderAfterNebula(ChamberThemeRenderContext context) {
	}

	protected void renderBeforeSharedLayers(ChamberThemeRenderContext context) {
	}

	protected void renderAfterSharedLayers(ChamberThemeRenderContext context) {
	}
}
