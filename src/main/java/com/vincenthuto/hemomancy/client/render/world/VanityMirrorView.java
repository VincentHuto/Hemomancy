package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * What a Scarlet Vanity's glass shows: the players standing in front of it, drawn with the real camera reflected
 * through the glass into a screen-sized texture that the glass samples by screen position, so the reflection moves as a
 * mirror's does. Players only, against the glass's own smoke: no blocks are reflected. One vanity a frame, the nearest
 * asked for: the vanity renderer asks while it draws, the pass runs at the next frame's sky stage, before any block
 * entity.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class VanityMirrorView {
	/** How near the eye must be for the glass to reflect. */
	private static final double REACH = 16.0D;
	/** How near the glass a player must stand to be seen in it. */
	private static final double SEEN_WITHIN = 10.0D;

	private record Request(BlockPos vanity, Direction facing, double distance) {
	}

	private record Live(BlockPos vanity, int texture) {
	}

	@Nullable
	private static Request pending;
	@Nullable
	private static Live live;
	@Nullable
	private static TextureTarget target;
	private static boolean drawing;

	private VanityMirrorView() {
	}

	/** A vanity whose glass is being drawn asks for its reflection; the nearest asker this frame gets it next frame. */
	public static void request(BlockPos vanity, Direction facing) {
		Vec3 eye = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		double[] glass = glass(vanity, facing);
		if (VanityMirrorRules.inFront(triple(eye), glass, facing.getStepX(), facing.getStepZ()) < VanityMirrorRules.MIN_IN_FRONT) return;
		double distance = eye.distanceTo(new Vec3(glass[0], glass[1], glass[2]));
		if (distance > REACH) return;
		if (pending == null || distance < pending.distance()) pending = new Request(vanity, facing, distance);
	}

	/** The texture holding this vanity's reflection this frame, or 0 when it has none. */
	public static int textureFor(BlockPos vanity) {
		return live != null && live.vanity().equals(vanity) ? live.texture() : 0;
	}

	public static void render(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
		Request request = pending;
		pending = null;
		live = null;
		if (request == null) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		RenderTarget main = mc.getMainRenderTarget();
		// A shader pack draws the world into its own targets; this pass would land in the wrong one.
		if (GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING) != main.frameBufferId) return;

		Camera camera = event.getCamera();
		Vec3 eye = camera.getPosition();
		int nx = request.facing().getStepX();
		int nz = request.facing().getStepZ();
		double[] glass = glass(request.vanity(), request.facing());
		float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
		List<AbstractClientPlayer> seen = new ArrayList<>();
		for (AbstractClientPlayer player : mc.level.players()) {
			if (player.isSpectator() || player.isInvisible()) continue;
			Vec3 at = player.getPosition(partialTick);
			if (VanityMirrorRules.inFront(triple(at), glass, nx, nz) <= 0.0D) continue;
			if (at.distanceToSqr(glass[0], at.y, glass[2]) > SEEN_WITHIN * SEEN_WITHIN) continue;
			seen.add(player);
		}
		if (seen.isEmpty()) return;

		ensureTarget(main);
		Matrix4f view = new Matrix4f().rotation(camera.rotation().conjugate(new Quaternionf()));
		Matrix4f modelView = view.mul(new Matrix4f().set(VanityMirrorRules.reflection(triple(eye), glass, nx, nz)));
		EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		float fogStart = RenderSystem.getShaderFogStart();
		float fogEnd = RenderSystem.getShaderFogEnd();
		var modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.identity().mul(modelView);
		RenderSystem.applyModelViewMatrix();
		target.bindWrite(true);
		RenderSystem.depthMask(true);
		RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
		RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
		drawing = true;
		try {
			RenderSystem.setShaderFogStart(Float.MAX_VALUE);
			RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			if (mc.level.effects().constantAmbientLight()) {
				Lighting.setupNetherLevel();
			} else {
				Lighting.setupLevel();
			}
			// A reflection turns every face's winding over, so the front faces are the clockwise ones here.
			GL11.glFrontFace(GL11.GL_CW);
			dispatcher.setRenderShadow(false);
			for (AbstractClientPlayer player : seen) {
				Vec3 at = player.getPosition(partialTick);
				float yaw = Mth.lerp(partialTick, player.yRotO, player.getYRot());
				dispatcher.render(player, at.x - eye.x, at.y - eye.y, at.z - eye.z, yaw, partialTick, new PoseStack(), buffers,
						dispatcher.getPackedLightCoords(player, partialTick));
			}
			buffers.endBatch();
		} finally {
			drawing = false;
			dispatcher.setRenderShadow(true);
			GL11.glFrontFace(GL11.GL_CCW);
			modelViewStack.popMatrix();
			RenderSystem.applyModelViewMatrix();
			RenderSystem.setShaderFogStart(fogStart);
			RenderSystem.setShaderFogEnd(fogEnd);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
			RenderSystem.disableBlend();
			RenderSystem.enableCull();
			main.bindWrite(true);
		}
		live = new Live(request.vanity(), target.getColorTextureId());
	}

	/** A reflected player carries no name over its head. */
	@SubscribeEvent
	public static void hideReflectedNames(RenderNameTagEvent event) {
		if (drawing) event.setCanRender(TriState.FALSE);
	}

	private static double[] glass(BlockPos vanity, Direction facing) {
		return VanityMirrorRules.glassPoint(vanity.getX(), vanity.getY(), vanity.getZ(), facing.getStepX(), facing.getStepZ());
	}

	private static double[] triple(Vec3 v) {
		return new double[] { v.x, v.y, v.z };
	}

	private static void ensureTarget(RenderTarget main) {
		if (target != null && target.width == main.width && target.height == main.height) return;
		if (target != null) target.destroyBuffers();
		target = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
		target.setFilterMode(GL11.GL_NEAREST);
	}

	/** With the level: the target is gone. */
	public static void clear() {
		pending = null;
		live = null;
		if (target != null) target.destroyBuffers();
		target = null;
	}
}
