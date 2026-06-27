package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Matrix4f;

record ChamberThemeRenderContext(
		ClientLevel level,
		int ticks,
		float partialTick,
		Matrix4f modelViewMatrix,
		Camera camera,
		Matrix4f projectionMatrix,
		boolean foggy,
		Runnable setupFog,
		PoseStack poseStack,
		Tesselator tesselator,
		ChamberSkyTheme theme,
		float time,
		float skyDistance,
		float membranePulse,
		int qliphothPomesConsumed
) {
}
