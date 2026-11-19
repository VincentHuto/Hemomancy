package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.data.FinaleCrossingClientState;
import com.vincenthuto.hemomancy.client.data.FinaleCrossingClientState.OverworldSky;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The Ringmaster finale's mask (2026-11-18): over the copied tent, the overworld's sky as it was at the crossing, so the
 * openings in the canvas show what they showed a moment before. A plain sky of the noted colours, the sun or moon where
 * the noted time put them, and the stars, held still: his mind does not keep the world's time.
 */
final class RingmasterMaskChamberEffects implements ChamberThemeEffects {
	private static final ResourceLocation SUN = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
	private static final ResourceLocation MOON = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
	/** A clear noon, for a mask drawn without a crossing to remember (a relog inside the finale). */
	private static final OverworldSky NOON = new OverworldSky(6000L, new Vec3(0.47D, 0.65D, 1.0D), 0, 0.0F);
	/** The overworld's plains fog before the time of day darkens it. */
	private static final Vec3 PLAINS_FOG = new Vec3(0.753D, 0.847D, 1.0D);

	private final ChamberSkyTheme theme;

	RingmasterMaskChamberEffects(ChamberSkyTheme theme) {
		this.theme = theme;
	}

	@Override
	public ResourceLocation id() {
		return theme.id();
	}

	private static OverworldSky noted() {
		OverworldSky sky = FinaleCrossingClientState.sky();
		return sky == null ? NOON : sky;
	}

	/** The vanilla overworld's celestial angle for a time of day. */
	static float celestialAngle(long dayTime) {
		double d = Mth.frac(dayTime / 24000.0D - 0.25D);
		double e = 0.5D - Math.cos(d * Math.PI) / 2.0D;
		return (float) (d * 2.0D + e) / 3.0F;
	}

	private static float daylight(float angle) {
		return Mth.clamp(Mth.cos(angle * Mth.TWO_PI) * 2.0F + 0.5F, 0.0F, 1.0F);
	}

	@Override
	public boolean renderSky(ChamberThemeRenderContext context) {
		OverworldSky sky = noted();
		PoseStack pose = context.poseStack();
		float distance = context.skyDistance();
		Vec3 fog = fogFor(sky);
		RenderSystem.depthMask(false);
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		drawDome(pose.last().pose(), distance, sky.sky(), fog);

		float angle = celestialAngle(sky.dayTime());
		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
		pose.mulPose(Axis.XP.rotationDegrees(angle * 360.0F));
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		if (sky.stars() > 0.0F) drawStars(pose.last().pose(), distance * 0.75F, sky.stars());
		drawBody(pose.last().pose(), SUN, distance * 0.78F, distance * 0.23F, 0.0F, 0.0F, 1.0F, 1.0F);
		int phase = Math.floorMod(sky.moonPhase(), 8);
		float u = (phase % 4) / 4.0F, v = (phase / 4) / 2.0F;
		drawBody(pose.last().pose(), MOON, -distance * 0.78F, distance * 0.15F, u, v, u + 0.25F, v + 0.5F);
		pose.popPose();

		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
		RenderSystem.enableCull();
		RenderSystem.depthMask(true);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		return true;
	}

	/** Sky overhead, the fog's colour at the horizon, darker below it. */
	private static void drawDome(Matrix4f pose, float d, Vec3 sky, Vec3 fog) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
				DefaultVertexFormat.POSITION_COLOR);
		float sr = (float) sky.x, sg = (float) sky.y, sb = (float) sky.z;
		float fr = (float) fog.x, fg = (float) fog.y, fb = (float) fog.z;
		float horizon = d * 0.12F;
		// The four sides: fog at the horizon fading up into the sky, and down into a dimmer fog below.
		float[][] corners = {{-d, -d}, {d, -d}, {d, d}, {-d, d}};
		for (int i = 0; i < 4; i++) {
			float[] a = corners[i], c = corners[(i + 1) % 4];
			b.addVertex(pose, a[0], d, a[1]).setColor(sr, sg, sb, 1.0F);
			b.addVertex(pose, c[0], d, c[1]).setColor(sr, sg, sb, 1.0F);
			b.addVertex(pose, c[0], horizon, c[1]).setColor(fr, fg, fb, 1.0F);
			b.addVertex(pose, a[0], horizon, a[1]).setColor(fr, fg, fb, 1.0F);
			b.addVertex(pose, a[0], horizon, a[1]).setColor(fr, fg, fb, 1.0F);
			b.addVertex(pose, c[0], horizon, c[1]).setColor(fr, fg, fb, 1.0F);
			b.addVertex(pose, c[0], -d, c[1]).setColor(fr * 0.35F, fg * 0.35F, fb * 0.35F, 1.0F);
			b.addVertex(pose, a[0], -d, a[1]).setColor(fr * 0.35F, fg * 0.35F, fb * 0.35F, 1.0F);
		}
		b.addVertex(pose, -d, d, -d).setColor(sr, sg, sb, 1.0F);
		b.addVertex(pose, -d, d, d).setColor(sr, sg, sb, 1.0F);
		b.addVertex(pose, d, d, d).setColor(sr, sg, sb, 1.0F);
		b.addVertex(pose, d, d, -d).setColor(sr, sg, sb, 1.0F);
		b.addVertex(pose, -d, -d, -d).setColor(fr * 0.35F, fg * 0.35F, fb * 0.35F, 1.0F);
		b.addVertex(pose, d, -d, -d).setColor(fr * 0.35F, fg * 0.35F, fb * 0.35F, 1.0F);
		b.addVertex(pose, d, -d, d).setColor(fr * 0.35F, fg * 0.35F, fb * 0.35F, 1.0F);
		b.addVertex(pose, -d, -d, d).setColor(fr * 0.35F, fg * 0.35F, fb * 0.35F, 1.0F);
		BufferUploader.drawWithShader(b.buildOrThrow());
	}

	/** The sun or moon: a textured square straight up the celestial axis ({@code up} negative for the moon). */
	private static void drawBody(Matrix4f pose, ResourceLocation texture, float up, float size, float u0, float v0,
			float u1, float v1) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, texture);
		BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
				DefaultVertexFormat.POSITION_TEX);
		float s = up < 0 ? -size : size;
		b.addVertex(pose, -s, up, -s).setUv(u0, v0);
		b.addVertex(pose, s, up, -s).setUv(u1, v0);
		b.addVertex(pose, s, up, s).setUv(u1, v1);
		b.addVertex(pose, -s, up, s).setUv(u0, v1);
		BufferUploader.drawWithShader(b.buildOrThrow());
	}

	/** Vanilla's star field, from its own seed, at the noted brightness. */
	private static void drawStars(Matrix4f pose, float radius, float brightness) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
				DefaultVertexFormat.POSITION_COLOR);
		RandomSource random = RandomSource.create(10842L);
		for (int i = 0; i < 1500; i++) {
			float x = random.nextFloat() * 2.0F - 1.0F, y = random.nextFloat() * 2.0F - 1.0F, z = random.nextFloat() * 2.0F - 1.0F;
			float size = 0.15F + random.nextFloat() * 0.1F;
			float len = Mth.lengthSquared(x, y, z);
			if (len <= 0.010000001F || len >= 1.0F) continue;
			Vector3f at = new Vector3f(x, y, z).normalize(radius);
			float spin = (float) (random.nextDouble() * Math.PI * 2.0D);
			Quaternionf facing = new Quaternionf().rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), at)
					.rotateZ(spin);
			float s = size * radius / 100.0F;
			for (float[] corner : new float[][] {{s, -s}, {s, s}, {-s, s}, {-s, -s}}) {
				Vector3f p = new Vector3f(corner[0], corner[1], 0.0F).rotate(facing).add(at);
				b.addVertex(pose, p.x, p.y, p.z).setColor(1.0F, 1.0F, 1.0F, brightness);
			}
		}
		BufferUploader.drawWithShader(b.buildOrThrow());
	}

	/** Vanilla's overworld fog for the noted time over plains. */
	private static Vec3 fogFor(OverworldSky sky) {
		float light = daylight(celestialAngle(sky.dayTime()));
		return new Vec3(PLAINS_FOG.x * (light * 0.94F + 0.06F), PLAINS_FOG.y * (light * 0.94F + 0.06F),
				PLAINS_FOG.z * (light * 0.91F + 0.09F));
	}

	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
		return fogFor(noted());
	}
}
