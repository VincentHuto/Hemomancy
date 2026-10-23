package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.SomaticLoomEffectRules;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram.EngramDissolve;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Cortical loom's weave raises a dome of its weaver's own Chamber of Will sky. Within the dome's radius the world
 * beyond is replaced by that sky, while everything inside it (the loom, the floor, the weaver, the threads, the echo)
 * stays real, seen from inside or out.
 * <p>
 * A stencil portal at AFTER_CUTOUT_BLOCKS, while only opaque terrain is in the depth buffer:
 * <ol>
 * <li>The dome's back faces, depth-tested against the scene, mark the stencil where a view ray reaches the far wall
 * without meeting anything inside the dome.</li>
 * <li>The weaver's theme draws its camera-centred Chamber sky inside that stencil, never losing a depth test.</li>
 * <li>The back faces write their own depth inside the stencil, so whatever lies beyond the dome stays hidden while
 * entities and translucent blocks inside it draw normally over the sky.</li>
 * </ol>
 * A faint membrane and a ground ring mark the boundary. Themes are windowed, not bent: a theme renders around the
 * camera, as it does in the Chamber, and the dome is the window it is seen through.
 */
public final class SomaticLoomDomeRenderer {
	private static final int SEGMENTS = 48, RINGS = 32, MAX_DOMES = 2;
	private static final double RANGE = 64.0D;
	/** Closer than the Chamber's own 128, so short render distances never clip the sky at the far plane. */
	private static final float SKY_DISTANCE = 48.0F;
	private static final Map<BlockPos, Tracked> TRACKED = new HashMap<>();
	private static boolean warned;

	private record Tracked(SomaticLoomBlockEntity loom, long seen) {}

	private SomaticLoomDomeRenderer() {
	}

	/** Called by the loom's renderer each frame it draws: a Cortical weave in view (or one dissolving) may raise a dome. */
	public static void track(SomaticLoomBlockEntity loom, boolean dissolving) {
		if (loom.getLevel() == null || loom.upgradeTier() < 2 || !loom.isWeaving() && !dissolving) return;
		TRACKED.put(loom.getBlockPos().immutable(), new Tracked(loom, loom.getLevel().getGameTime()));
	}

	public static void render(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS || TRACKED.isEmpty()) return;
		Minecraft minecraft = Minecraft.getInstance();
		var level = minecraft.level;
		if (level == null) {
			TRACKED.clear();
			return;
		}
		long now = level.getGameTime();
		Vec3 cam = event.getCamera().getPosition();
		TRACKED.values().removeIf(t -> t.loom().isRemoved() || t.loom().getLevel() != level || now - t.seen() > 5
				|| !t.loom().isWeaving() && EngramDissolve.progress(t.loom().getBlockPos(), level, 0F) < 0F
				|| t.loom().upgradeTier() < 2);
		List<Tracked> near = new ArrayList<>();
		for (Tracked t : TRACKED.values())
			if (Vec3.atCenterOf(t.loom().getBlockPos()).distanceToSqr(cam) < RANGE * RANGE) near.add(t);
		if (near.isEmpty()) return;
		near.sort(Comparator.comparingDouble(t -> Vec3.atCenterOf(t.loom().getBlockPos()).distanceToSqr(cam)));
		float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
		for (int i = 0; i < Math.min(MAX_DOMES, near.size()); i++) renderDome(event, near.get(i).loom(), cam, partial);
	}

	private static void renderDome(RenderLevelStageEvent event, SomaticLoomBlockEntity loom, Vec3 cam, float partial) {
		float climax = loom.isInClimax()
				? Mth.clamp((loom.getClimaxTicks() + partial) / SomaticWeaveRules.CLIMAX_TICKS, 0, 1) : 0;
		// A broken weave's dome sinks away from where it stood, under the sky it was raised with.
		float dissolve = loom.isWeaving() ? -1F : EngramDissolve.progress(loom.getBlockPos(), loom.getLevel(), partial);
		EngramDissolve.Snapshot snapshot = dissolve >= 0F ? EngramDissolve.snapshot(loom.getBlockPos()) : null;
		float radius = snapshot != null ? SomaticLoomEffectRules.dissolvingDomeRadius(snapshot.domeRadius(), dissolve)
				: SomaticLoomEffectRules.domeRadius(loom.upgradeTier(), loom.isWeaving(),
						loom.getWeaveTicks() + (loom.isPaused() ? 0 : partial), loom.isInClimax(), climax);
		if (radius < 0.25F) return;
		Minecraft minecraft = Minecraft.getInstance();
		var target = minecraft.getMainRenderTarget();
		if (!target.isStencilEnabled()) {
			// Enabling resizes the main target; the dome starts on the next frame.
			target.enableStencil();
			return;
		}
		// A shader pack draws the world into its own targets, whose stencil this portal does not own: skip it.
		if (GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING) != target.frameBufferId) return;
		ResourceLocation themeId = snapshot != null && snapshot.theme() != null ? snapshot.theme()
				: loom.getWeaverTheme() == null ? ChamberSkyThemeRegistry.DEFAULT.id() : loom.getWeaverTheme();
		ChamberSkyTheme theme = ChamberSkyThemeRegistry.byId(themeId);
		Vec3 centre = Vec3.atBottomCenterOf(loom.getBlockPos()).add(0, 0.5D, 0);

		var modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.identity();
		RenderSystem.applyModelViewMatrix();
		try {
			PoseStack world = new PoseStack();
			world.mulPose(event.getModelViewMatrix());
			world.pushPose();
			world.translate(centre.x - cam.x, centre.y - cam.y, centre.z - cam.z);
			world.scale(radius, radius, radius);
			Matrix4f dome = new Matrix4f(world.last().pose());
			world.popPose();
			try {
				GL11.glEnable(GL11.GL_STENCIL_TEST);
				GL11.glStencilMask(0xFF);
				GL11.glClearStencil(0);
				GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

				// 1. Mark where the far wall is the first thing a view ray meets.
				GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
				GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
				RenderSystem.colorMask(false, false, false, false);
				RenderSystem.depthMask(false);
				RenderSystem.enableDepthTest();
				RenderSystem.depthFunc(GL11.GL_LEQUAL);
				RenderSystem.enableCull();
				GL11.glCullFace(GL11.GL_FRONT);
				drawSphere(dome, 0xFFFFFFFF);

				// 2. Inside the mark, push depth to the far plane, as the Chamber's own sky starts from a cleared buffer.
				GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
				GL11.glStencilMask(0x00);
				RenderSystem.depthMask(true);
				RenderSystem.depthFunc(GL11.GL_ALWAYS);
				GL11.glDepthRange(1.0D, 1.0D);
				drawSphere(dome, 0xFFFFFFFF);
				GL11.glDepthRange(0.0D, 1.0D);

				// 3. The weaver's Chamber sky, inside the mark only, its layers occluding one another with LEQUAL.
				RenderSystem.colorMask(true, true, true, true);
				RenderSystem.depthFunc(GL11.GL_LEQUAL);
				GL11.glCullFace(GL11.GL_BACK);
				renderSky(event, theme, partial);

				// 4. The far wall's depth, so nothing beyond the dome draws over the sky afterwards.
				RenderSystem.colorMask(false, false, false, false);
				RenderSystem.depthMask(true);
				RenderSystem.enableDepthTest();
				RenderSystem.depthFunc(GL11.GL_ALWAYS);
				RenderSystem.enableCull();
				GL11.glCullFace(GL11.GL_FRONT);
				drawSphere(dome, 0xFFFFFFFF);
			} finally {
				GL11.glDepthRange(0.0D, 1.0D);
				GL11.glCullFace(GL11.GL_BACK);
				GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
				GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
				GL11.glStencilMask(0xFF);
				GL11.glDisable(GL11.GL_STENCIL_TEST);
				RenderSystem.colorMask(true, true, true, true);
				RenderSystem.depthMask(true);
				RenderSystem.depthFunc(GL11.GL_LEQUAL);
				RenderSystem.enableDepthTest();
				RenderSystem.enableCull();
				RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
				RenderSystem.defaultBlendFunc();
				RenderSystem.disableBlend();
			}
			float time = AnimationTime.gameTime(loom.getLevel(), partial) * theme.motionMultiplier();
			drawMembrane(world, centre, cam, radius, theme, time, loom.isPaused());
			SomaticLoomSeamRenderer.render(world, loom, centre, cam, radius, theme, partial);
		} finally {
			modelViewStack.popMatrix();
			RenderSystem.applyModelViewMatrix();
			RenderSystem.depthMask(true);
			RenderSystem.enableCull();
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableBlend();
		}
	}

	private static void renderSky(RenderLevelStageEvent event, ChamberSkyTheme theme, float partial) {
		var level = Minecraft.getInstance().level;
		PoseStack sky = new PoseStack();
		sky.mulPose(event.getModelViewMatrix());
		float time = (event.getRenderTick() + partial) * theme.motionMultiplier();
		float pulse = ChamberOfWillRenderHelpers.membranePulse(time) * theme.pulseStrength();
		ChamberThemeRenderContext context = new ChamberThemeRenderContext(level, event.getRenderTick(), partial,
				event.getModelViewMatrix(), event.getCamera(), event.getProjectionMatrix(), false, () -> {}, sky,
				Tesselator.getInstance(), theme, time, SKY_DISTANCE, pulse, 0);
		try {
			ChamberSkyThemeRegistry.effectsById(theme.id()).renderSky(context);
		} catch (RuntimeException failure) {
			// A theme authored for the Chamber that cannot draw here falls back to the default sky.
			if (!warned) Hemomancy.LOGGER.warn("Somatic Loom dome could not draw Chamber theme {}", theme.id(), failure);
			warned = true;
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			ChamberSkyThemeRegistry.effectsById(ChamberSkyThemeRegistry.DEFAULT.id()).renderSky(new ChamberThemeRenderContext(
					level, event.getRenderTick(), partial, event.getModelViewMatrix(), event.getCamera(),
					event.getProjectionMatrix(), false, () -> {}, sky, Tesselator.getInstance(),
					ChamberSkyThemeRegistry.DEFAULT, time, SKY_DISTANCE, pulse, 0));
		}
	}

	/** The unit sphere, outward-facing (counter-clockwise from outside), in `pose`. */
	private static void drawSphere(Matrix4f pose, int argb) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		for (int ring = 0; ring < RINGS; ring++) {
			double t0 = Math.PI * ring / RINGS, t1 = Math.PI * (ring + 1) / RINGS;
			for (int seg = 0; seg < SEGMENTS; seg++) {
				double a0 = Math.PI * 2 * seg / SEGMENTS, a1 = Math.PI * 2 * (seg + 1) / SEGMENTS;
				vertex(buffer, pose, t0, a0, argb);
				vertex(buffer, pose, t0, a1, argb);
				vertex(buffer, pose, t1, a1, argb);
				vertex(buffer, pose, t1, a0, argb);
			}
		}
		BufferUploader.drawWithShader(buffer.buildOrThrow());
	}

	private static void vertex(BufferBuilder buffer, Matrix4f pose, double theta, double angle, int argb) {
		float r = (float) Math.sin(theta);
		buffer.addVertex(pose, (float) (Math.cos(angle) * r), (float) Math.cos(theta), (float) (Math.sin(angle) * r)).setColor(argb);
	}

	/**
	 * The dome's skin: faint in the theme's nebula colour, brightening toward its silhouette, with a band of the
	 * theme's blood colour where it meets the ground.
	 */
	private static void drawMembrane(PoseStack world, Vec3 centre, Vec3 cam, float radius, ChamberSkyTheme theme,
			float time, boolean paused) {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Vector3f skin = ChamberOfWillRenderHelpers.colorVector(theme.nebulaSecondary());
		Vector3f vein = ChamberOfWillRenderHelpers.colorVector(theme.capillaryTint());
		Vector3f blood = ChamberOfWillRenderHelpers.colorVector(theme.bloodTint());
		float dim = paused ? 0.45F : 1F;
		world.pushPose();
		world.translate(centre.x - cam.x, centre.y - cam.y, centre.z - cam.z);
		Matrix4f pose = world.last().pose();
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		Vec3 toCam = cam.subtract(centre);
		for (int ring = 0; ring < RINGS; ring++) {
			double t0 = Math.PI * ring / RINGS, t1 = Math.PI * (ring + 1) / RINGS;
			for (int seg = 0; seg < SEGMENTS; seg++) {
				double a0 = Math.PI * 2 * seg / SEGMENTS, a1 = Math.PI * 2 * (seg + 1) / SEGMENTS;
				skinVertex(buffer, pose, radius, t0, a0, toCam, skin, vein, time, dim);
				skinVertex(buffer, pose, radius, t0, a1, toCam, skin, vein, time, dim);
				skinVertex(buffer, pose, radius, t1, a1, toCam, skin, vein, time, dim);
				skinVertex(buffer, pose, radius, t1, a0, toCam, skin, vein, time, dim);
			}
		}
		// Where the dome meets the ground (the loom's floor), a dim band of the theme's blood colour under the seam's tissue.
		float floor = -0.49F;
		float ground = (float) Math.sqrt(Math.max(0, radius * radius - floor * floor));
		for (int seg = 0; seg < SEGMENTS * 2; seg++) {
			double a0 = Math.PI * 2 * seg / (SEGMENTS * 2), a1 = Math.PI * 2 * (seg + 1) / (SEGMENTS * 2);
			float beat = 0.55F + 0.45F * Mth.sin(time * 0.08F + seg * 0.4F);
			ringVertex(buffer, pose, ground - 0.35F, floor, a0, blood, 0F);
			ringVertex(buffer, pose, ground + 0.05F, floor, a0, blood, 0.2F * beat * dim);
			ringVertex(buffer, pose, ground + 0.05F, floor, a1, blood, 0.2F * beat * dim);
			ringVertex(buffer, pose, ground - 0.35F, floor, a1, blood, 0F);
		}
		BufferUploader.drawWithShader(buffer.buildOrThrow());
		world.popPose();
	}

	private static void skinVertex(BufferBuilder buffer, Matrix4f pose, float radius, double theta, double angle,
			Vec3 toCam, Vector3f skin, Vector3f vein, float time, float dim) {
		float r = (float) Math.sin(theta);
		float x = (float) (Math.cos(angle) * r), y = (float) Math.cos(theta), z = (float) (Math.sin(angle) * r);
		Vec3 view = toCam.subtract(x * radius, y * radius, z * radius);
		double facing = view.lengthSqr() < 1.0E-6D ? 1 : Math.abs((x * view.x + y * view.y + z * view.z) / view.length());
		float rim = (float) Math.pow(1 - facing, 3);
		// Slow veins of the theme's capillary colour crawl over the skin.
		float crawl = Math.max(0, Mth.sin((float) (angle * 7 + theta * 5) + time * 0.02F)) * 0.5F;
		float alpha = (0.04F + 0.3F * rim) * dim;
		buffer.addVertex(pose, x * radius, y * radius, z * radius).setColor(
				Mth.lerp(crawl, skin.x(), vein.x()), Mth.lerp(crawl, skin.y(), vein.y()), Mth.lerp(crawl, skin.z(), vein.z()),
				alpha);
	}

	private static void ringVertex(BufferBuilder buffer, Matrix4f pose, float r, float y, double angle, Vector3f c,
			float alpha) {
		buffer.addVertex(pose, (float) Math.cos(angle) * r, y, (float) Math.sin(angle) * r).setColor(c.x(), c.y(), c.z(), alpha);
	}
}
