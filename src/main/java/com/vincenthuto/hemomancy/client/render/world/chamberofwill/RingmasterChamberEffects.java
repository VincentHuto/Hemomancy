package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * The Ringmaster's own mind (2026-11-18), seen past the finale's merge: the dark beyond a big top. Overhead a canvas
 * of his stripes, torn through in places; strings hanging out of the dark from it; a few stage-light shafts sweeping
 * slowly down toward a ring that is not there. Everything is drawn around the camera, as every Chamber sky is. Since the
 * finale's third pass (2026-11-18) the finale no longer looks through a portal into this sky: its tent wears his
 * material on its own faces ({@code FinaleVeilRenderer}); the theme stays registered as his Chamber's.
 */
final class RingmasterChamberEffects extends AbstractChamberThemeEffects {
	private static final int STRIPES = 28, STRINGS = 120, SHAFTS = 6, SHAFT_SIDES = 14;
	private static final float[] CANVAS_RED = {0.55F, 0.07F, 0.10F}, CANVAS_PALE = {0.42F, 0.33F, 0.28F};
	private static final float[] STRING = {1.0F, 0.42F, 0.38F}, LIGHT = {1.0F, 0.86F, 0.66F};

	RingmasterChamberEffects(ChamberSkyTheme theme) {
		super(theme);
	}

	@Override
	protected void renderBaseSkybox(ChamberThemeRenderContext context) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		ChamberOfWillRenderHelpers.renderSolidBox(context.poseStack(), context.tesselator(), context.skyDistance(),
				context.theme().skyboxColor());
	}

	@Override
	protected void renderAfterSharedLayers(ChamberThemeRenderContext context) {
		Matrix4f pose = context.poseStack().last().pose();
		float d = context.skyDistance(), time = context.time();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);
		canvas(pose, d, time);
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		strings(pose, d, time);
		shafts(pose, d, time);
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableCull();
	}

	/** A cone of his stripes overhead, its panels torn away in a seeded few, breathing slightly. */
	private static void canvas(Matrix4f pose, float d, float time) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder b = begin();
		RandomSource random = RandomSource.create(7713L);
		float apex = d * 0.92F, rimHeight = d * 0.42F, rim = d * 0.9F;
		for (int i = 0; i < STRIPES; i++) {
			boolean torn = random.nextFloat() < 0.22F;
			float tear = 0.45F + random.nextFloat() * 0.4F;
			if (torn && random.nextBoolean()) continue;
			float[] c = i % 2 == 0 ? CANVAS_RED : CANVAS_PALE;
			float a0 = Mth.TWO_PI * i / STRIPES, a1 = Mth.TWO_PI * (i + 1) / STRIPES;
			float sag = 1.0F + 0.015F * Mth.sin(time * 0.01F + i);
			// A torn panel hangs only part way down from the apex.
			float reach = torn ? tear : 1.0F;
			float r = rim * reach * sag, y = Mth.lerp(reach, apex, rimHeight);
			b.addVertex(pose, 0.0F, apex, 0.0F).setColor(c[0] * 0.6F, c[1] * 0.6F, c[2] * 0.6F, 1.0F);
			b.addVertex(pose, 0.0F, apex, 0.0F).setColor(c[0] * 0.6F, c[1] * 0.6F, c[2] * 0.6F, 1.0F);
			b.addVertex(pose, Mth.cos(a1) * r, y, Mth.sin(a1) * r).setColor(c[0], c[1], c[2], 1.0F);
			b.addVertex(pose, Mth.cos(a0) * r, y, Mth.sin(a0) * r).setColor(c[0], c[1], c[2], 1.0F);
		}
		BufferUploader.drawWithShader(b.buildOrThrow());
	}

	/** Thin strings hanging out of the dark from the canvas, swaying, brightest where they leave it. */
	private static void strings(Matrix4f pose, float d, float time) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder b = begin();
		RandomSource random = RandomSource.create(4409L);
		float width = d * 0.0035F;
		for (int i = 0; i < STRINGS; i++) {
			float angle = random.nextFloat() * Mth.TWO_PI, out = d * (0.3F + random.nextFloat() * 0.55F);
			float top = d * (0.5F + random.nextFloat() * 0.3F), bottom = top - d * (0.25F + random.nextFloat() * 0.7F);
			float sway = d * 0.02F * Mth.sin(time * 0.012F + i * 1.7F);
			float x = Mth.cos(angle) * out, z = Mth.sin(angle) * out;
			float bx = x + Mth.cos(angle + 1.57F) * sway, bz = z + Mth.sin(angle + 1.57F) * sway;
			float alpha = 0.55F + 0.25F * random.nextFloat();
			// Two crossed ribbons so a string reads from any side.
			for (int k = 0; k < 2; k++) {
				float wx = k == 0 ? width : 0.0F, wz = k == 0 ? 0.0F : width;
				b.addVertex(pose, x - wx, top, z - wz).setColor(STRING[0], STRING[1], STRING[2], alpha);
				b.addVertex(pose, x + wx, top, z + wz).setColor(STRING[0], STRING[1], STRING[2], alpha);
				b.addVertex(pose, bx + wx, bottom, bz + wz).setColor(STRING[0], STRING[1], STRING[2], 0.0F);
				b.addVertex(pose, bx - wx, bottom, bz - wz).setColor(STRING[0], STRING[1], STRING[2], 0.0F);
			}
		}
		BufferUploader.drawWithShader(b.buildOrThrow());
	}

	/** Stage lights at the canvas's rim, each a cone sweeping slowly down toward the middle of the dark below. */
	private static void shafts(Matrix4f pose, float d, float time) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder b = begin();
		for (int i = 0; i < SHAFTS; i++) {
			float home = Mth.TWO_PI * i / SHAFTS + 0.3F;
			Vec3 source = new Vec3(Mth.cos(home) * d * 0.8F, d * 0.5F, Mth.sin(home) * d * 0.8F);
			float sweep = time * 0.004F + i * 1.3F;
			Vec3 aim = new Vec3(Mth.cos(sweep) * d * 0.35F, -d * 0.6F, Mth.sin(sweep) * d * 0.35F);
			Vec3 axis = aim.subtract(source).normalize();
			Vec3 side = axis.cross(new Vec3(0, 1, 0)).normalize(), up = side.cross(axis).normalize();
			float length = d * 0.95F, spread = d * 0.08F;
			Vec3 tip = source.add(axis.scale(length));
			for (int s = 0; s < SHAFT_SIDES; s++) {
				double t0 = Math.PI * 2 * s / SHAFT_SIDES, t1 = Math.PI * 2 * (s + 1) / SHAFT_SIDES;
				Vec3 e0 = tip.add(side.scale(Math.cos(t0) * spread)).add(up.scale(Math.sin(t0) * spread));
				Vec3 e1 = tip.add(side.scale(Math.cos(t1) * spread)).add(up.scale(Math.sin(t1) * spread));
				b.addVertex(pose, (float) source.x, (float) source.y, (float) source.z).setColor(LIGHT[0], LIGHT[1], LIGHT[2], 0.16F);
				b.addVertex(pose, (float) source.x, (float) source.y, (float) source.z).setColor(LIGHT[0], LIGHT[1], LIGHT[2], 0.16F);
				b.addVertex(pose, (float) e1.x, (float) e1.y, (float) e1.z).setColor(LIGHT[0], LIGHT[1], LIGHT[2], 0.0F);
				b.addVertex(pose, (float) e0.x, (float) e0.y, (float) e0.z).setColor(LIGHT[0], LIGHT[1], LIGHT[2], 0.0F);
			}
		}
		BufferUploader.drawWithShader(b.buildOrThrow());
	}

	private static BufferBuilder begin() {
		return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
	}

	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
		return new Vec3(0.035D, 0.008D, 0.012D);
	}
}
