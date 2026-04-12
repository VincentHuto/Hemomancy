package com.vincenthuto.hemomancy.client.render.tile.functional;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.functional.VisceralMirrorBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;

/**
 * Block entity renderer for the Visceral Mirror — renders a stencil-clipped
 * player reflection on a 1.6-block-tall × 1-block-wide mirror surface, plus
 * a translucent tinted glass overlay.
 *
 * <h3>Rendering approach</h3>
 * <ol>
 *   <li>Write the mirror quad shape into the <b>stencil buffer</b>
 *       (Tesselator immediate draw — no colour/depth output).</li>
 *   <li>With stencil in read mode, render the player entity reflected
 *       across the mirror plane. The stencil clips the model cleanly
 *       to the mirror bounds.</li>
 *   <li>Disable stencil and draw a translucent glass overlay through
 *       the normal batched buffer.</li>
 * </ol>
 */
public class VisceralMirrorRenderer implements BlockEntityRenderer<VisceralMirrorBlockEntity> {

	// ── Mirror surface geometry ──────────────────────────────────────────
	/** Half-width of the mirror surface (1 block wide → ±0.5). */
	private static final float MIRROR_HALF_WIDTH = 0.5f;
	/** Mirror surface starts just above the pedestal (y = 2/16). */
	private static final float MIRROR_Y_START = 0.125f;
	/** Mirror surface is 1.6 blocks tall. */
	private static final float MIRROR_HEIGHT = 1.6f;
	/** Top edge of the mirror surface. */
	private static final float MIRROR_Y_END = MIRROR_Y_START + MIRROR_HEIGHT;
	/**
	 * Z-offset from block centre to the front face of the mirror frame.
	 * The frame spans z = 3/16 → 13/16; front face at 13/16 = 0.8125,
	 * which is 0.3125 from the centre (0.5).
	 */
	private static final float MIRROR_Z_OFFSET = 0.3125f;

	// ── Distance limits ──────────────────────────────────────────────────
	/** Beyond this distance² the reflection is not rendered. */
	private static final double MAX_REFLECTION_DIST_SQ = 64.0; // 8 blocks

	// ── Glass overlay colours ────────────────────────────────────────────
	private static final float GLASS_R = 0.35f;
	private static final float GLASS_G = 0.08f;
	private static final float GLASS_B = 0.12f;
	private static final float GLASS_A = 0.18f;

	public VisceralMirrorRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	/** The mirror surface extends above the block boundary. */
	@Override
	public boolean shouldRenderOffScreen(VisceralMirrorBlockEntity te) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 64;
	}

	// =====================================================================
	//  Main render entry
	// =====================================================================

	@Override
	public void render(VisceralMirrorBlockEntity te, float partialTicks, PoseStack stack,
			MultiBufferSource buffer, int light, int overlay) {
		if (te.getLevel() == null) return;

		Direction facing = te.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
		Player player = Minecraft.getInstance().player;

		// Always draw the tinted glass overlay
		renderMirrorGlass(stack, buffer, facing, te);

		// Render the reflection only when a player is close and in front
		if (player != null && isPlayerInFront(te, player, facing)) {
			renderReflection(te, partialTicks, stack, facing, player, light);
		}
	}

	// =====================================================================
	//  Stencil-based reflection pipeline
	// =====================================================================

	private void renderReflection(VisceralMirrorBlockEntity te, float partialTicks,
			PoseStack stack, Direction facing, Player player, int light) {

		Minecraft mc = Minecraft.getInstance();
		mc.getMainRenderTarget().enableStencil();

		// ── Phase 1: write mirror quad into stencil ──────────────────────
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glStencilMask(0xFF);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glColorMask(false, false, false, false);
		GL11.glDepthMask(false);

		drawStencilQuad(stack, facing);

		GL11.glColorMask(true, true, true, true);
		GL11.glDepthMask(true);

		// ── Phase 2: render reflected player within stencil ──────────────
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
		GL11.glStencilMask(0x00);

		renderReflectedPlayer(te, partialTicks, stack, facing, player, light);

		// ── Phase 3: disable stencil ─────────────────────────────────────
		GL11.glDisable(GL11.GL_STENCIL_TEST);
	}

	// =====================================================================
	//  Stencil mask quad (Tesselator — drawn immediately)
	// =====================================================================

	private void drawStencilQuad(PoseStack stack, Direction facing) {
		stack.pushPose();
		applyFacingTransform(stack, facing);

		Matrix4f mat = stack.last().pose();

		RenderSystem.setShader(GameRenderer::getPositionShader);
		BufferBuilder builder = Tesselator.getInstance().getBuilder();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

		// Front face
		builder.vertex(mat, -MIRROR_HALF_WIDTH, MIRROR_Y_START, 0).endVertex();
		builder.vertex(mat, -MIRROR_HALF_WIDTH, MIRROR_Y_END,   0).endVertex();
		builder.vertex(mat,  MIRROR_HALF_WIDTH, MIRROR_Y_END,   0).endVertex();
		builder.vertex(mat,  MIRROR_HALF_WIDTH, MIRROR_Y_START, 0).endVertex();

		// Back face (ensures stencil is written regardless of camera side)
		builder.vertex(mat,  MIRROR_HALF_WIDTH, MIRROR_Y_START, 0).endVertex();
		builder.vertex(mat,  MIRROR_HALF_WIDTH, MIRROR_Y_END,   0).endVertex();
		builder.vertex(mat, -MIRROR_HALF_WIDTH, MIRROR_Y_END,   0).endVertex();
		builder.vertex(mat, -MIRROR_HALF_WIDTH, MIRROR_Y_START, 0).endVertex();

		Tesselator.getInstance().end();

		stack.popPose();
	}

	// =====================================================================
	//  Reflected player entity
	// =====================================================================

	/**
	 * Renders the client player reflected across the mirror surface.
	 * <p>
	 * The reflection is achieved by:
	 * <ol>
	 *   <li>Calculating the player's position mirrored across the plane.</li>
	 *   <li>Applying a negative scale on the mirror's normal axis, which
	 *       geometrically mirrors the entire model (including rotations,
	 *       limb positions and held items).</li>
	 * </ol>
	 * The stencil buffer (set up by the caller) clips the result to the
	 * mirror bounds.
	 */
	private void renderReflectedPlayer(VisceralMirrorBlockEntity te, float partialTicks,
			PoseStack stack, Direction facing, Player player, int light) {

		BlockPos pos = te.getBlockPos();
		Vec3 playerPos = player.getPosition(partialTicks);

		// Player position in block-local coordinates
		double localX = playerPos.x - pos.getX();
		double localY = playerPos.y - pos.getY();
		double localZ = playerPos.z - pos.getZ();

		// Mirror surface coordinate along the normal axis (block-local)
		double mirrorCoord = getMirrorSurfaceCoord(facing);
		boolean flipZ = (facing == Direction.SOUTH || facing == Direction.NORTH);

		// Reflect position across mirror plane
		double reflX = localX;
		double reflY = localY;
		double reflZ = localZ;
		if (flipZ) {
			reflZ = 2.0 * mirrorCoord - localZ;
		} else {
			reflX = 2.0 * mirrorCoord - localX;
		}

		stack.pushPose();

		// Translate to the reflected position
		stack.translate(reflX, reflY, reflZ);

		// Apply mirror flip — negative scale on the normal axis
		if (flipZ) {
			stack.scale(1.0f, 1.0f, -1.0f);
		} else {
			stack.scale(-1.0f, 1.0f, 1.0f);
		}

		// The negative scale reverses face winding; disable culling so
		// both front and back faces draw correctly.
		RenderSystem.disableCull();

		EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		dispatcher.setRenderShadow(false);

		// Use the main buffer source and flush immediately so the geometry
		// is drawn while the stencil test is still active.
		MultiBufferSource.BufferSource bs = Minecraft.getInstance().renderBuffers().bufferSource();
		dispatcher.render(player, 0.0, 0.0, 0.0, player.getYRot(), partialTicks, stack, bs, light);
		bs.endBatch();

		dispatcher.setRenderShadow(true);
		RenderSystem.enableCull();

		stack.popPose();
	}

	// =====================================================================
	//  Mirror glass overlay (batched — translucent tinted quad)
	// =====================================================================

	private void renderMirrorGlass(PoseStack stack, MultiBufferSource buffer,
			Direction facing, VisceralMirrorBlockEntity te) {
		stack.pushPose();
		applyFacingTransform(stack, facing);

		Matrix4f mat = stack.last().pose();
		VertexConsumer vc = buffer.getBuffer(RenderTypeInit.LOOM_EFFECT);

		float r = GLASS_R;
		float g = GLASS_G;
		float b = GLASS_B;
		float a = GLASS_A;

		// Subtle pulse during an active ritual
		if (te.getPhase() != VisceralMirrorBlockEntity.RitualPhase.IDLE && te.getLevel() != null) {
			float pulse = (float) (Math.sin(te.getLevel().getGameTime() * 0.1) * 0.08);
			a += pulse;
			r += 0.12f;
		}

		// Front face
		vc.vertex(mat, -MIRROR_HALF_WIDTH, MIRROR_Y_START, 0).color(r, g, b, a).endVertex();
		vc.vertex(mat, -MIRROR_HALF_WIDTH, MIRROR_Y_END,   0).color(r, g, b, a).endVertex();
		vc.vertex(mat,  MIRROR_HALF_WIDTH, MIRROR_Y_END,   0).color(r, g, b, a).endVertex();
		vc.vertex(mat,  MIRROR_HALF_WIDTH, MIRROR_Y_START, 0).color(r, g, b, a).endVertex();

		// Back face
		vc.vertex(mat,  MIRROR_HALF_WIDTH, MIRROR_Y_START, 0).color(r, g, b, a).endVertex();
		vc.vertex(mat,  MIRROR_HALF_WIDTH, MIRROR_Y_END,   0).color(r, g, b, a).endVertex();
		vc.vertex(mat, -MIRROR_HALF_WIDTH, MIRROR_Y_END,   0).color(r, g, b, a).endVertex();
		vc.vertex(mat, -MIRROR_HALF_WIDTH, MIRROR_Y_START, 0).color(r, g, b, a).endVertex();

		stack.popPose();
	}

	// =====================================================================
	//  Helpers
	// =====================================================================

	/**
	 * Applies a Y-axis rotation to the PoseStack so that the mirror surface
	 * is always on the XY plane at z = +{@link #MIRROR_Z_OFFSET} from the
	 * block centre, facing outward.
	 */
	private void applyFacingTransform(PoseStack stack, Direction facing) {
		stack.translate(0.5, 0.0, 0.5);
		float angle = switch (facing) {
			case SOUTH -> 0f;
			case WEST  -> 90f;
			case NORTH -> 180f;
			case EAST  -> 270f;
			default    -> 0f;
		};
		stack.mulPose(Vector3.YP.rotationDegrees(angle).toMoj());
		stack.translate(0.0, 0.0, MIRROR_Z_OFFSET);
	}

	/**
	 * Returns the mirror surface coordinate along its normal axis
	 * (block-local, 0–1 range).
	 */
	private double getMirrorSurfaceCoord(Direction facing) {
		return switch (facing) {
			case SOUTH -> 0.5 + MIRROR_Z_OFFSET;  // 0.8125
			case NORTH -> 0.5 - MIRROR_Z_OFFSET;  // 0.1875
			case EAST  -> 0.5 + MIRROR_Z_OFFSET;
			case WEST  -> 0.5 - MIRROR_Z_OFFSET;
			default    -> 0.5;
		};
	}

	/**
	 * Returns {@code true} when the player is in front of the mirror
	 * surface and within reflection range.
	 */
	private boolean isPlayerInFront(VisceralMirrorBlockEntity te, Player player, Direction facing) {
		BlockPos pos = te.getBlockPos();
		double cx = pos.getX() + 0.5;
		double cy = pos.getY() + 0.5;
		double cz = pos.getZ() + 0.5;

		if (player.distanceToSqr(cx, cy, cz) > MAX_REFLECTION_DIST_SQ) {
			return false;
		}

		Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
		Vec3 toPlayer = player.position().subtract(cx, pos.getY(), cz);
		return toPlayer.dot(normal) > 0;
	}
}
