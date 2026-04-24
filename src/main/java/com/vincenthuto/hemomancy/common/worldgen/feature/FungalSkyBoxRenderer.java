package com.vincenthuto.hemomancy.common.worldgen.feature;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FungalSkyBoxRenderer {

	/** The base sky box texture — uses the hemomancy copy of end_sky. */
	private static final ResourceLocation SKY_TEXTURE =
			Hemomancy.rloc("textures/environment/end_sky.png");

	/**
	 * Secondary tiled blood-spore layer rendered on top of the base sky,
	 * scrolling slowly to give the impression of drifting spores.
	 */
	private static final ResourceLocation SPORE_TEXTURE =
			Hemomancy.rloc("textures/environment/blood_fill_tiled.png");

	// ------------------------------------------------------------------ utils

	public static double heartbeatEffect(float f, double frequency, double amplitude) {
		return amplitude * Math.sin(frequency * f);
	}

	public static double oscillatingValueWithHeartbeat(float f) {
		double baseValue = 180 - Math.abs(f - 180);
		return baseValue + heartbeatEffect(f, 0, 0);
	}

	// ------------------------------------------------------------------ sky

	/**
	 * Renders a two-layer fungal void sky:
	 * <ol>
	 *   <li>A slowly rotating cube mapped with {@code end_sky.png}, tinted deep
	 *       blood-crimson so the familiar void pattern reads as an alien void.</li>
	 *   <li>A very faint scrolling tiled spore overlay for atmospheric depth.</li>
	 * </ol>
	 * Returns {@code true} to suppress vanilla sky fallback.
	 */
	public static boolean renderSky(ClientLevel level, float partialTicks, Matrix4f modelViewMatrix, Camera camera,
			Matrix4f projectionMatrix, Runnable setupFog) {

		setupFog.run();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);

		var mvStack = RenderSystem.getModelViewStack();
		mvStack.pushMatrix();
		mvStack.set(modelViewMatrix);

		// Very slow Y-axis rotation — one full turn every ~28 real-time minutes.
		float angle = ((level.getGameTime() % 100000L) + partialTicks) * (float) (Math.PI * 2.0 / 100000.0);
		mvStack.rotateY(angle);
		RenderSystem.applyModelViewMatrix();

		// --- Layer 1: base sky cube tinted deep crimson ---
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, SKY_TEXTURE);
		RenderSystem.setShaderColor(0.22f, 0.0f, 0.06f, 1.0f);

		float d = 100.0f;
		Tesselator tess = Tesselator.getInstance();

		renderBoxFace(tess, -d, -d, -d,  d, -d, -d,  d, -d,  d, -d, -d,  d);  // top
		renderBoxFace(tess, -d,  d,  d,  d,  d,  d,  d,  d, -d, -d,  d, -d);  // bottom
		renderBoxFace(tess,  d, -d, -d, -d, -d, -d, -d,  d, -d,  d,  d, -d);  // north
		renderBoxFace(tess, -d, -d,  d,  d, -d,  d,  d,  d,  d, -d,  d,  d);  // south
		renderBoxFace(tess, -d, -d, -d, -d, -d,  d, -d,  d,  d, -d,  d, -d);  // west
		renderBoxFace(tess,  d, -d,  d,  d, -d, -d,  d,  d, -d,  d,  d,  d);  // east

		// --- Layer 2: faint scrolling spore overlay ---
		// Scroll in the opposite direction and at a different speed for parallax.
		mvStack.rotateY(-angle * 0.4f);
		RenderSystem.applyModelViewMatrix();

		RenderSystem.setShaderTexture(0, SPORE_TEXTURE);
		RenderSystem.setShaderColor(0.35f, 0.0f, 0.08f, 0.35f); // semi-transparent

		float scroll = ((level.getGameTime() + partialTicks) * 0.00015f) % 1.0f;
		float sd = 95.0f; // slightly inside the base box to avoid z-fighting

		renderBoxFaceScrolled(tess, -sd, -sd, -sd,  sd, -sd, -sd,  sd, -sd,  sd, -sd, -sd,  sd, scroll);
		renderBoxFaceScrolled(tess, -sd,  sd,  sd,  sd,  sd,  sd,  sd,  sd, -sd, -sd,  sd, -sd, scroll);
		renderBoxFaceScrolled(tess,  sd, -sd, -sd, -sd, -sd, -sd, -sd,  sd, -sd,  sd,  sd, -sd, scroll);
		renderBoxFaceScrolled(tess, -sd, -sd,  sd,  sd, -sd,  sd,  sd,  sd,  sd, -sd,  sd,  sd, scroll);
		renderBoxFaceScrolled(tess, -sd, -sd, -sd, -sd, -sd,  sd, -sd,  sd,  sd, -sd,  sd, -sd, scroll);
		renderBoxFaceScrolled(tess,  sd, -sd,  sd,  sd, -sd, -sd,  sd,  sd, -sd,  sd,  sd,  sd, scroll);

		// --- Cleanup ---
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		mvStack.popMatrix();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();

		return true;
	}

	// ------------------------------------------------------------------ helpers

	/** Draws one face of the sky box with standard [0,1] UV mapping. */
	private static void renderBoxFace(Tesselator tess,
			float x0, float y0, float z0,
			float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3) {
		BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		buf.addVertex(x0, y0, z0).setUv(0f, 0f);
		buf.addVertex(x1, y1, z1).setUv(1f, 0f);
		buf.addVertex(x2, y2, z2).setUv(1f, 1f);
		buf.addVertex(x3, y3, z3).setUv(0f, 1f);
		BufferUploader.drawWithShader(buf.buildOrThrow());
	}

	/** Draws one face of the spore overlay with a time-based UV scroll offset. */
	private static void renderBoxFaceScrolled(Tesselator tess,
			float x0, float y0, float z0,
			float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3,
			float scroll) {
		float tile = 2.0f; // tile the spore texture 2× per face for smaller pattern
		BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		buf.addVertex(x0, y0, z0).setUv(scroll,          scroll);
		buf.addVertex(x1, y1, z1).setUv(scroll + tile,   scroll);
		buf.addVertex(x2, y2, z2).setUv(scroll + tile,   scroll + tile);
		buf.addVertex(x3, y3, z3).setUv(scroll,          scroll + tile);
		BufferUploader.drawWithShader(buf.buildOrThrow());
	}
}
