package com.vincenthuto.hemomancy.common.worldgen.feature;

import org.joml.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FungalSkyBoxRenderer {

	public FungalSkyBoxRenderer() {
	}

	public static double heartbeatEffect(float f, double frequency, double amplitude) {
		return amplitude * Math.sin(frequency * f);
	}

	public static double oscillatingValueWithHeartbeat(float f) {
		double baseValue = 180 - Math.abs(f - 180);
		return baseValue + heartbeatEffect(f, 0, 0);
	}

	/**
	 * TODO(1.21 port): restore custom fungal sky rendering.
	 * Current fallback returns false so vanilla sky rendering continues.
	 */
	public static boolean renderSky(ClientLevel level, float partialTicks, PoseStack stack, Camera camera,
			Matrix4f projectionMatrix, Runnable setupFog) {
		return false;
	}
}
