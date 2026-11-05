package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.data.LidlessPassageClientState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.item.QliphothSeedTendrilEffects;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;
import com.vincenthuto.hemomancy.client.render.tile.shared.LidlessDoorRenderer;
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.ChamberProxyView;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineMonolithBlockEntity;
import com.vincenthuto.hemomancy.common.tile.shared.ChamberGateBlockEntity;
import com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService;
import com.vincenthuto.hemomancy.common.block.shared.LidlessTheme;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.client.particle.TendrilRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import javax.annotation.Nullable;

/**
 * The crossing view: the void over the whole screen with the doorway windows cut out of it. The level is drawn
 * as usual (the camera really is in the door), copied to a scene target, and the view shader paints the void over it
 * everywhere but inside the windows. In seed mode the far window gives way to a black hole, and the Qliphoth Seed is
 * drawn with its tendrils into a focus target the shader composites over the void.
 */
public final class LidlessPassageView {
	private static final int FULL_BRIGHT = 0xF000F0;
	/** The Seed hangs this far ahead at most, so it reads as a thing at the end of the walk, not a point. */
	/** The black hole's size in the world: its apparent size follows from how far off it still is, as the Seed's does. */
	private static final double HOLE_SIZE = 0.6D;
	private static final double SEED_NEAREST = 1.0D;
	/** A walk in through a gateway arrives this far inside the gate ({@code ChamberOfWillManager.enterAtGate}). */
	private static final float ARRIVAL_DEPTH = 2.0F;
	/** The walk back looks out from the husk's front ({@code ChamberProxyRules.OUTSIDE_FRONT}) and lands a couple of blocks out. */
	private static final float RETURN_START = 0.0F;
	private static final float RETURN_DEPTH = 2.0F;
	/** The Seed is drawn large in the crossing; its tentacles keep in proportion. */
	private static final float SEED_TENTACLE_SCALE = 2.0F;
	@Nullable
	private static TextureTarget scene;
	@Nullable
	private static TextureTarget focus;
	private static boolean focusDrawn;
	private static float projectionM00 = 1.0F;
	private static float projectionM11 = 1.0F;

	private LidlessPassageView() {
	}

	/** Called by {@code WorldEffectDispatcher}: remembers the level projection the windows must match. */
	public static void captureProjection(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
		Matrix4f projection = event.getProjectionMatrix();
		projectionM00 = projection.m00();
		projectionM11 = projection.m11();
	}

	/**
	 * Called by {@code WorldEffectDispatcher} after the level: in seed mode, draws the Qliphoth Seed and its tendrils
	 * on the passage axis ahead of the camera into the focus target, so the view can lay them over the void.
	 */
	public static void renderFocus(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
		focusDrawn = false;
		LidlessPassageClientState.View view = LidlessPassageClientState.local();
		Minecraft mc = Minecraft.getInstance();
		if (view == null || view.mode() != LidlessPassageService.Mode.SEED || mc.level == null) return;
		RenderTarget main = mc.getMainRenderTarget();
		if (main.width <= 0 || main.height <= 0) return;
		targets(main);
		float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
		Vec3 cameraPos = event.getCamera().getPosition();
		Vec3 forward = Vec3.atLowerCornerOf(view.passageForward().getNormal());
		// Where it really is, so it starts small down the passage and perspective brings it closer with every step.
		double away = Math.max(SEED_NEAREST, view.length() - LidlessPassageClientState.progress(partialTick));
		// On the line of sight, the same axis the black hole is centred on.
		Vec3 seedPos = cameraPos.add(forward.scale(away));
		float time = AnimationTime.gameTime(mc.level, partialTick);
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		buffers.endBatch();
		// AFTER_LEVEL fires after the level renderer has popped the camera's rotation off the model-view stack. It goes
		// back on for this pass, so the Seed stands on the passage's axis in the world, not wherever the camera points.
		var modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.mul(event.getModelViewMatrix());
		RenderSystem.applyModelViewMatrix();
		focus.clear(Minecraft.ON_OSX);
		focus.bindWrite(false);
		try {
			PoseStack pose = event.getPoseStack();
			// The tendrils grow from the Seed: seeded on its key, drawn against the camera like the level's own.
			QliphothSeedTendrilEffects.spawnAt(view.door().asLong() ^ 0x5EEDL, seedPos, mc.level.getGameTime(), partialTick, SEED_TENTACLE_SCALE);
			pose.pushPose();
			pose.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
			TendrilRenderer.INSTANCE.render(partialTick, pose, buffers);
			pose.popPose();
			buffers.endBatch();
			// A red glow behind the Seed, facing the camera, breathing with it.
			float breath = 0.5F + 0.5F * (float) Math.sin(time * 0.15F);
			float glowRadius = 0.7F + 0.2F * breath;
			Vec3 glowRight = new Vec3(event.getCamera().getLeftVector()).scale(-glowRadius);
			Vec3 glowUp = new Vec3(event.getCamera().getUpVector()).scale(glowRadius);
			Vec3 glowCentre = seedPos.subtract(cameraPos);
			com.mojang.blaze3d.vertex.VertexConsumer glow = buffers.getBuffer(net.minecraft.client.renderer.RenderType.lightning());
			Matrix4f glowMatrix = pose.last().pose();
			glowQuad(glow, glowMatrix, glowCentre, glowRight, glowUp, 0.8F + 0.2F * breath);
			glowQuad(glow, glowMatrix, glowCentre, glowRight.scale(0.5), glowUp.scale(0.5), 0.85F);
			buffers.endBatch(net.minecraft.client.renderer.RenderType.lightning());
			// That type's output state hands the main target back on teardown; the Seed still belongs in the focus.
			focus.bindWrite(false);
			pose.pushPose();
			pose.translate(seedPos.x - cameraPos.x, seedPos.y - cameraPos.y, seedPos.z - cameraPos.z);
			float scale = 2.0F + 0.25F * breath;
			pose.scale(scale, scale, scale);
			pose.mulPose(Axis.YP.rotationDegrees(time * 2.0F));
			mc.getItemRenderer().renderStatic(new ItemStack(ItemInit.qliphoth_seed.get()), ItemDisplayContext.FIXED,
					FULL_BRIGHT, OverlayTexture.NO_OVERLAY, pose, buffers, mc.level, 0);
			pose.popPose();
			buffers.endBatch();
			focusDrawn = true;
		} finally {
			modelViewStack.popMatrix();
			RenderSystem.applyModelViewMatrix();
			main.bindWrite(false);
		}
	}

	private static void targets(RenderTarget main) {
		if (scene != null && focus != null && scene.width == main.width && scene.height == main.height) return;
		clear();
		scene = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
		scene.setFilterMode(GL11.GL_LINEAR);
		focus = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
		focus.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		focus.setFilterMode(GL11.GL_LINEAR);
	}

	/**
	 * The veil over a dimension change: the void in {@code theme}'s colours (by {@code chamberTurn}) with a black hole of
	 * {@code holeRadius} radians ahead, from a camera looking straight down the passage. Nothing of the scene shows.
	 */
	public static void renderVeil(GuiGraphics graphics, int width, int height, float partialTick, LidlessTheme theme,
			float seed, float chamberTurn, float holeRadius, float eyeShape, int liveTexture) {
		ShaderInstance shader = ShaderInit.LIDLESS_PASSAGE_VIEW.getInstance().get();
		Minecraft mc = Minecraft.getInstance();
		if (shader == null) return;
		RenderTarget main = mc.getMainRenderTarget();
		if (main.width <= 0 || main.height <= 0) return;
		targets(main);
		focus.clear(Minecraft.ON_OSX);
		main.bindWrite(false);
		float time = mc.level != null ? AnimationTime.gameTime(mc.level, partialTick) : (System.currentTimeMillis() % 200_000L) / 50.0F;
		shader.safeGetUniform("HemoTime").set(time);
		shader.safeGetUniform("Progress").set((float) LidlessPassageService.GATEWAY_LENGTH);
		shader.safeGetUniform("Length").set((float) LidlessPassageService.GATEWAY_LENGTH);
		shader.safeGetUniform("DoorSeed").set(seed);
		shader.safeGetUniform("CamRight").set(1.0F, 0.0F, 0.0F);
		shader.safeGetUniform("CamUp").set(0.0F, 1.0F, 0.0F);
		shader.safeGetUniform("CamForward").set(0.0F, 0.0F, 1.0F);
		shader.safeGetUniform("FovTan").set(1.0F / projectionM11);
		shader.safeGetUniform("Aspect").set(projectionM11 / projectionM00);
		shader.safeGetUniform("EyeHeight").set(1.62F);
		shader.safeGetUniform("Mode").set(1.0F);
		shader.safeGetUniform("Hole").set(holeRadius, 1.0F);
		shader.safeGetUniform("EyeShape").set(eyeShape);
		shader.safeGetUniform("ChamberTurn").set(chamberTurn);
		shader.safeGetUniform("Theme").set((float) theme.ordinal());
		shader.safeGetUniform("Live").set(liveTexture != 0 ? 1.0F : 0.0F);
		drawFullScreen(graphics, width, height, shader, liveTexture);
	}

	/** Draws the view when the local player is crossing; false when they are not. */
	public static boolean renderGrade(GuiGraphics graphics, int width, int height, float partialTick) {
		LidlessPassageClientState.View view = LidlessPassageClientState.local();
		if (view == null) return false;
		ShaderInstance shader = ShaderInit.LIDLESS_PASSAGE_VIEW.getInstance().get();
		Minecraft mc = Minecraft.getInstance();
		if (shader == null || mc.player == null || mc.level == null) return false;
		RenderTarget main = mc.getMainRenderTarget();
		if (main.width <= 0 || main.height <= 0) return false;
		targets(main);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, scene.frameBufferId);
		GL30.glBlitFramebuffer(0, 0, main.width, main.height, 0, 0, scene.width, scene.height,
				GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
		main.bindWrite(false);
		if (!focusDrawn) {
			focus.clear(Minecraft.ON_OSX);
			main.bindWrite(false);
		}

		Camera camera = mc.gameRenderer.getMainCamera();
		Vec3 forward = Vec3.atLowerCornerOf(view.passageForward().getNormal());
		Vec3 up = new Vec3(0, 1, 0);
		Vec3 right = up.cross(forward);
		Vec3 camForward = new Vec3(camera.getLookVector());
		Vec3 camUp = new Vec3(camera.getUpVector());
		Vec3 camRight = new Vec3(camera.getLeftVector()).scale(-1);
		float progress = LidlessPassageClientState.progress(partialTick);
		float along = view.length() <= 0 ? 0 : progress / view.length();
		LidlessPassageService.Mode mode = view.mode();
		boolean hole = mode != LidlessPassageService.Mode.DOOR;
		shader.safeGetUniform("HemoTime").set(AnimationTime.gameTime(mc.level, partialTick));
		shader.safeGetUniform("Progress").set(progress);
		shader.safeGetUniform("Length").set(view.length());
		shader.safeGetUniform("DoorSeed").set(LidlessDoorRenderer.seed(view.door()));
		setPassage(shader, "CamRight", camRight, right, up, forward);
		setPassage(shader, "CamUp", camUp, right, up, forward);
		setPassage(shader, "CamForward", camForward, right, up, forward);
		shader.safeGetUniform("FovTan").set(1.0F / projectionM11);
		shader.safeGetUniform("Aspect").set(projectionM11 / projectionM00);
		shader.safeGetUniform("EyeHeight").set(mc.player.getEyeHeight());
		shader.safeGetUniform("Mode").set(hole ? 1.0F : 0.0F);
		// The Seed walk's hole grows from a point to most of the view, the warp following later. A gateway walk's hole is
		// the eye's own diamond: as wide as it looked from the threshold when the walk starts, past the view by the end.
		boolean gateway = mode == LidlessPassageService.Mode.GATE || mode == LidlessPassageService.Mode.RETURN;
		float holeRadius = !hole ? 0.0F
				: gateway ? LidlessHoleRules.gatewayRadius(along)
				: (float) Math.atan2(HOLE_SIZE, Math.max(SEED_NEAREST, view.length() - progress));
		shader.safeGetUniform("EyeShape").set(gateway ? 1.0F : 0.0F);
		float warp = hole ? HemoEasing.smoothstep(0.2F, 1.0F, along) : 0.0F;
		shader.safeGetUniform("Hole").set(holeRadius, warp);
		shader.safeGetUniform("ChamberTurn").set(switch (mode) {
			case DOOR -> 0.0F;
			case SEED -> HemoEasing.smoothstep(0.1F, 0.95F, along);
			case GATE -> 1.0F;
			case RETURN -> 1.0F - HemoEasing.smoothstep(0.1F, 0.9F, along);
		});
		shader.safeGetUniform("Theme").set((float) view.theme().ordinal());
		// Walking in through a gateway, the hole is the eye's window carried along: the Chamber drawn from a camera
		// that goes in through the gate as far as the walk has, to where the walk arrives.
		int live = 0;
		if (mode == LidlessPassageService.Mode.GATE && mc.level.getBlockEntity(view.door()) instanceof SanguineMonolithBlockEntity husk) {
			ChamberProxyView.requestCrossing(husk, view.facing(), ARRIVAL_DEPTH * along);
			live = ChamberProxyView.textureFor(view.door());
		} else if (mode == LidlessPassageService.Mode.RETURN && mc.level.getBlockEntity(view.door()) instanceof ChamberGateBlockEntity gate) {
			// And the walk back: the overworld around the husk, from just outside its front to where the walk lands.
			ChamberProxyView.requestReturn(gate, RETURN_START + RETURN_DEPTH * along);
			live = ChamberProxyView.textureFor(view.door());
		}
		shader.safeGetUniform("Live").set(live != 0 ? 1.0F : 0.0F);

		drawFullScreen(graphics, width, height, shader, live);
		return true;
	}

	private static void drawFullScreen(GuiGraphics graphics, int width, int height, ShaderInstance shader) {
		drawFullScreen(graphics, width, height, shader, 0);
	}

	private static void drawFullScreen(GuiGraphics graphics, int width, int height, ShaderInstance shader, int live) {
		RenderSystem.setShaderTexture(0, scene.getColorTextureId());
		RenderSystem.setShaderTexture(1, focus.getColorTextureId());
		RenderSystem.setShaderTexture(2, live != 0 ? live : focus.getColorTextureId());
		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.setShader(() -> shader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		Matrix4f matrix = graphics.pose().last().pose();
		BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		builder.addVertex(matrix, 0, height, -90).setUv(0, 1).setColor(1F, 1F, 1F, 1F);
		builder.addVertex(matrix, width, height, -90).setUv(1, 1).setColor(1F, 1F, 1F, 1F);
		builder.addVertex(matrix, width, 0, -90).setUv(1, 0).setColor(1F, 1F, 1F, 1F);
		builder.addVertex(matrix, 0, 0, -90).setUv(0, 0).setColor(1F, 1F, 1F, 1F);
		BufferUploader.drawWithShader(builder.buildOrThrow());
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
	}

	/** A camera-facing disc of red light: bright at the centre, fading to nothing at the rim. */
	private static void glowQuad(com.mojang.blaze3d.vertex.VertexConsumer consumer, Matrix4f matrix, Vec3 centre, Vec3 right,
			Vec3 up, float alpha) {
		final int segments = 18;
		for (int i = 0; i < segments; i++) {
			double a0 = i * Math.PI * 2.0 / segments;
			double a1 = (i + 1) * Math.PI * 2.0 / segments;
			Vec3 p0 = centre.add(right.scale(Math.cos(a0))).add(up.scale(Math.sin(a0)));
			Vec3 p1 = centre.add(right.scale(Math.cos(a1))).add(up.scale(Math.sin(a1)));
			consumer.addVertex(matrix, (float) centre.x, (float) centre.y, (float) centre.z).setColor(0.95F, 0.14F, 0.06F, alpha);
			consumer.addVertex(matrix, (float) p0.x, (float) p0.y, (float) p0.z).setColor(0.6F, 0.03F, 0.02F, 0.0F);
			consumer.addVertex(matrix, (float) p1.x, (float) p1.y, (float) p1.z).setColor(0.6F, 0.03F, 0.02F, 0.0F);
			consumer.addVertex(matrix, (float) centre.x, (float) centre.y, (float) centre.z).setColor(0.95F, 0.14F, 0.06F, alpha);
		}
	}

	/** A world vector expressed in passage space (right, up, forward toward the exit). */
	private static void setPassage(ShaderInstance shader, String name, Vec3 world, Vec3 right, Vec3 up, Vec3 forward) {
		shader.safeGetUniform(name).set((float) world.dot(right), (float) world.dot(up), (float) world.dot(forward));
	}

	public static void clear() {
		if (scene != null) scene.destroyBuffers();
		if (focus != null) focus.destroyBuffers();
		scene = null;
		focus = null;
		focusDrawn = false;
	}
}
