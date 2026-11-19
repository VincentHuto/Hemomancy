package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import javax.annotation.Nullable;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * A dome through which a Chamber sky is seen, drawn at AFTER_CUTOUT_BLOCKS while only opaque terrain is in the depth
 * buffer (pulled out of {@link SomaticLoomDomeRenderer}, 2026-11-18):
 * <ol>
 * <li>The dome's back faces, depth-tested against the scene, mark the stencil where a view ray reaches the far wall
 * without meeting anything inside the dome.</li>
 * <li>The theme draws its camera-centred Chamber sky inside that stencil, never losing a depth test.</li>
 * <li>The back faces write their own depth inside the stencil, so whatever lies beyond the dome stays hidden while
 * entities and translucent blocks inside it draw normally over the sky.</li>
 * </ol>
 * Themes are windowed, not bent: a theme renders around the camera, as it does in the Chamber, and the dome is the
 * window it is seen through.
 *
 * <p>{@link #project} instead throws a theme's sky onto surfaces, like a projector: the Ringmaster finale shows his
 * real Chamber render on the copied tent's own block faces (2026-11-18 fourth pass).
 */
public final class ChamberDomePortal {
	private static final int SEGMENTS = 48, RINGS = 32;
	/** Closer than the Chamber's own 128, so short render distances never clip the sky at the far plane. */
	private static final float SKY_DISTANCE = 48.0F;
	private static boolean warned;

	private ChamberDomePortal() {
	}

	/**
	 * Draws {@code theme} beyond a dome of {@code radius} around {@code centre}. Returns false when the portal cannot
	 * draw this frame (no stencil yet, or a shader pack owns the targets); nothing was drawn then.
	 */
	public static boolean render(RenderLevelStageEvent event, Vec3 centre, float radius, ChamberSkyTheme theme,
			float partial) {
		Minecraft minecraft = Minecraft.getInstance();
		var target = minecraft.getMainRenderTarget();
		if (!target.isStencilEnabled()) {
			// Enabling resizes the main target; the dome starts on the next frame.
			target.enableStencil();
			return false;
		}
		// A shader pack draws the world into its own targets, whose stencil this portal does not own: skip it.
		if (GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING) != target.frameBufferId) return false;
		Vec3 cam = event.getCamera().getPosition();
		var modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.identity();
		RenderSystem.applyModelViewMatrix();
		try {
			PoseStack world = new PoseStack();
			world.mulPose(event.getModelViewMatrix());
			world.translate(centre.x - cam.x, centre.y - cam.y, centre.z - cam.z);
			world.scale(radius, radius, radius);
			Matrix4f dome = new Matrix4f(world.last().pose());
			GL11.glEnable(GL11.GL_STENCIL_TEST);
			GL11.glStencilMask(0xFF);
			GL11.glClearStencil(0);
			GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

			drawWindow(dome, event, theme, partial);
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
			modelViewStack.popMatrix();
			RenderSystem.applyModelViewMatrix();
		}
		return true;
	}

	/** The sealed window: the far wall marks the stencil and then hides what lies beyond it. */
	private static void drawWindow(Matrix4f dome, RenderLevelStageEvent event, ChamberSkyTheme theme, float partial) {
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

		// 3. The theme's Chamber sky, inside the mark only, its layers occluding one another with LEQUAL.
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
	}

	/**
	 * Projects {@code theme}'s real Chamber sky onto the surfaces {@code mask} draws, at {@code strength}. The sky is
	 * rendered camera-centred, exactly as in the Chamber, into its own target with its own depth. {@code mask} then draws
	 * its surfaces (camera-relative positions under the matrix it is given, with the position shader set) into the
	 * stencil only, depth-tested against the scene and nudged toward the camera, so only the pixels where those surfaces
	 * are the nearest thing seen are marked. Last the sky is laid over the screen through that stencil; the scene's
	 * depth is untouched, so whatever draws later draws as usual. Returns false when it cannot draw this frame (no
	 * stencil yet, or a shader pack owns the framebuffer).
	 */
	public static boolean project(RenderLevelStageEvent event, ChamberSkyTheme theme, float partial, float strength,
			java.util.function.Consumer<Matrix4f> mask) {
		if (strength <= 0.001F) return true;
		Minecraft minecraft = Minecraft.getInstance();
		RenderTarget main = minecraft.getMainRenderTarget();
		if (!main.isStencilEnabled()) {
			main.enableStencil();
			return false;
		}
		if (GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING) != main.frameBufferId) return false;
		TextureTarget sky = skyTarget(main);
		var modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.identity();
		RenderSystem.applyModelViewMatrix();
		try {
			// 1. The real sky, whole, in its own target. Its layers write alpha unevenly (a textured skybox under the
			// default blend leaves its texture's alpha); only the colour is wanted, so the alpha is filled after.
			sky.setClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			sky.clear(Minecraft.ON_OSX);
			sky.bindWrite(true);
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			renderSky(event, theme, partial);
			fillAlpha();
			main.bindWrite(true);

			// 2. The surfaces into the stencil only: depth-tested, no colour, no depth written.
			GL11.glEnable(GL11.GL_STENCIL_TEST);
			GL11.glStencilMask(0xFF);
			GL11.glClearStencil(0);
			GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
			GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
			RenderSystem.colorMask(false, false, false, false);
			RenderSystem.depthMask(false);
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			RenderSystem.disableCull();
			RenderSystem.enablePolygonOffset();
			RenderSystem.polygonOffset(-1.0F, -10.0F);
			RenderSystem.setShader(GameRenderer::getPositionShader);
			mask.accept(new Matrix4f(event.getModelViewMatrix()));
			RenderSystem.polygonOffset(0.0F, 0.0F);
			RenderSystem.disablePolygonOffset();

			// 3. The sky over the marked pixels.
			GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
			GL11.glStencilMask(0x00);
			RenderSystem.colorMask(true, true, true, true);
			RenderSystem.disableDepthTest();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			overlay(sky, strength);
		} finally {
			main.bindWrite(true);
			GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
			GL11.glStencilMask(0xFF);
			GL11.glDisable(GL11.GL_STENCIL_TEST);
			RenderSystem.polygonOffset(0.0F, 0.0F);
			RenderSystem.disablePolygonOffset();
			RenderSystem.colorMask(true, true, true, true);
			RenderSystem.depthMask(true);
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			RenderSystem.enableDepthTest();
			RenderSystem.enableCull();
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableBlend();
			modelViewStack.popMatrix();
			RenderSystem.applyModelViewMatrix();
		}
		return true;
	}

	@Nullable
	private static TextureTarget projectedSky;

	private static TextureTarget skyTarget(RenderTarget main) {
		if (projectedSky == null) projectedSky = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
		else if (projectedSky.width != main.width || projectedSky.height != main.height)
			projectedSky.resize(main.width, main.height, Minecraft.ON_OSX);
		return projectedSky;
	}

	/** Sets the bound target's alpha to 1 everywhere, its colour untouched. */
	private static void fillAlpha() {
		Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
		VertexSorting sorting = RenderSystem.getVertexSorting();
		RenderSystem.setProjectionMatrix(new Matrix4f(), VertexSorting.ORTHOGRAPHIC_Z);
		try {
			RenderSystem.colorMask(false, false, false, true);
			RenderSystem.disableDepthTest();
			RenderSystem.disableBlend();
			RenderSystem.setShader(GameRenderer::getPositionShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
			buffer.addVertex(-1.0F, -1.0F, 0.0F);
			buffer.addVertex(1.0F, -1.0F, 0.0F);
			buffer.addVertex(1.0F, 1.0F, 0.0F);
			buffer.addVertex(-1.0F, 1.0F, 0.0F);
			BufferUploader.drawWithShader(buffer.buildOrThrow());
		} finally {
			RenderSystem.colorMask(true, true, true, true);
			RenderSystem.enableDepthTest();
			RenderSystem.setProjectionMatrix(projection, sorting);
		}
	}

	/** The sky target over the whole screen at {@code alpha}, drawn in clip space. */
	private static void overlay(TextureTarget sky, float alpha) {
		Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
		VertexSorting sorting = RenderSystem.getVertexSorting();
		RenderSystem.setProjectionMatrix(new Matrix4f(), VertexSorting.ORTHOGRAPHIC_Z);
		try {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, sky.getColorTextureId());
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
			BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			buffer.addVertex(-1.0F, -1.0F, 0.0F).setUv(0.0F, 0.0F);
			buffer.addVertex(1.0F, -1.0F, 0.0F).setUv(1.0F, 0.0F);
			buffer.addVertex(1.0F, 1.0F, 0.0F).setUv(1.0F, 1.0F);
			buffer.addVertex(-1.0F, 1.0F, 0.0F).setUv(0.0F, 1.0F);
			BufferUploader.drawWithShader(buffer.buildOrThrow());
		} finally {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setProjectionMatrix(projection, sorting);
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
			if (!warned) Hemomancy.LOGGER.warn("Chamber dome could not draw Chamber theme {}", theme.id(), failure);
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
	 * theme's blood colour where it meets the ground at {@code floor} below the centre. {@code world} holds the camera's
	 * view under an identity model-view stack.
	 */
	public static void drawMembrane(PoseStack world, Vec3 centre, Vec3 cam, float radius, ChamberSkyTheme theme,
			float time, float dim, float floor) {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Vector3f skin = ChamberOfWillRenderHelpers.colorVector(theme.nebulaSecondary());
		Vector3f vein = ChamberOfWillRenderHelpers.colorVector(theme.capillaryTint());
		Vector3f blood = ChamberOfWillRenderHelpers.colorVector(theme.bloodTint());
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
		// Where the dome meets the ground, a dim band of the theme's blood colour.
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
