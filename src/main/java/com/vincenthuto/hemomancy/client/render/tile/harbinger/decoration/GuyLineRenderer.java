package com.vincenthuto.hemomancy.client.render.tile.harbinger.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.GuyLineRules;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.TroupeGuyStakeBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.GuyStakeBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * A guy stake's line: a taut crimson-thread rope from the knot on the stake to the top of the tent wall it faces, lit
 * by the world along its length. Two crossed ribbons so it reads from every side. The tie-off point is read from the
 * level now and then, so a wall torn down drops its line.
 */
public class GuyLineRenderer implements BlockEntityRenderer<GuyStakeBlockEntity> {
	private static final RenderType ROPE = RenderType.entityCutoutNoCull(
			ResourceLocation.withDefaultNamespace("textures/misc/white.png"));
	/** The troupe's crimson thread, a darker turn every other segment. */
	private static final int THREAD = 0x8E1A2A, THREAD_DARK = 0x5E0F1B;
	private static final float HALF_WIDTH = 1.0F / 40.0F;
	private static final double SAG_PER_BLOCK = 0.025D;
	private static final int SEGMENTS = 20;
	/** How many frames a found tie-off is kept before it is read again, and how soon a missing one is retried. */
	private static final long REREAD_FRAMES = 600L;
	private static final long RETRY_FRAMES = 30L;

	public GuyLineRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(GuyStakeBlockEntity stake, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
			int light, int overlay) {
		Level level = stake.getLevel();
		if (level == null) return;
		Direction facing = stake.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
		// Counted in frames drawn, not game time, so a frozen tick still finds a wall whose chunk arrived late.
		long frame = stake.framesSinceRead() + 1;
		long every = stake.anchor() == null ? RETRY_FRAMES : REREAD_FRAMES;
		if (frame >= every || stake.framesSinceRead() < 0)
			stake.setAnchor(GuyLineRules.anchor(stake.getBlockPos(), facing,
					pos -> level.getBlockState(pos).is(TroupeGuyStakeBlock.ANCHORS)).orElse(null), 0L);
		else stake.setAnchor(stake.anchor(), frame);
		Vec3 anchor = stake.anchor();
		if (anchor == null) return;
		BlockPos origin = stake.getBlockPos();
		Vec3 from = GuyLineRules.knot(origin, facing).subtract(Vec3.atLowerCornerOf(origin));
		Vec3 to = anchor.subtract(Vec3.atLowerCornerOf(origin));
		int lightFrom = LevelRenderer.getLightColor(level, origin);
		int lightTo = LevelRenderer.getLightColor(level, BlockPos.containing(anchor).relative(facing.getOpposite()));
		VertexConsumer rope = buffers.getBuffer(ROPE);
		Matrix4f matrix = poseStack.last().pose();
		Vec3 along = to.subtract(from);
		Vec3 sideways = along.cross(new Vec3(0.0D, 1.0D, 0.0D));
		sideways = sideways.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : sideways.normalize();
		Vec3 upward = sideways.cross(along).normalize();
		double sag = SAG_PER_BLOCK * along.length();
		for (int i = 0; i < SEGMENTS; i++) {
			Vec3 a = point(from, along, sag, i / (float) SEGMENTS);
			Vec3 b = point(from, along, sag, (i + 1) / (float) SEGMENTS);
			int colour = i % 2 == 0 ? THREAD : THREAD_DARK;
			int la = blend(lightFrom, lightTo, i / (float) SEGMENTS);
			int lb = blend(lightFrom, lightTo, (i + 1) / (float) SEGMENTS);
			ribbon(rope, poseStack, matrix, a, b, sideways, colour, la, lb);
			ribbon(rope, poseStack, matrix, a, b, upward, colour, la, lb);
		}
	}

	private static Vec3 point(Vec3 from, Vec3 along, double sag, float t) {
		return from.add(along.scale(t)).subtract(0.0D, 4.0D * sag * t * (1.0F - t), 0.0D);
	}

	private static void ribbon(VertexConsumer rope, PoseStack poseStack, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 across,
			int colour, int lightA, int lightB) {
		float wx = (float) across.x * HALF_WIDTH, wy = (float) across.y * HALF_WIDTH, wz = (float) across.z * HALF_WIDTH;
		vertex(rope, poseStack, matrix, a.x - wx, a.y - wy, a.z - wz, colour, lightA);
		vertex(rope, poseStack, matrix, a.x + wx, a.y + wy, a.z + wz, colour, lightA);
		vertex(rope, poseStack, matrix, b.x + wx, b.y + wy, b.z + wz, colour, lightB);
		vertex(rope, poseStack, matrix, b.x - wx, b.y - wy, b.z - wz, colour, lightB);
	}

	private static void vertex(VertexConsumer rope, PoseStack poseStack, Matrix4f matrix, double x, double y, double z,
			int colour, int light) {
		rope.addVertex(matrix, (float) x, (float) y, (float) z)
				.setColor(colour >> 16 & 0xFF, colour >> 8 & 0xFF, colour & 0xFF, 255).setUv(0.0F, 0.0F)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
	}

	/** Block and sky light, each blended along the rope. */
	private static int blend(int from, int to, float t) {
		int block = Math.round((from & 0xFFFF) + ((to & 0xFFFF) - (from & 0xFFFF)) * t);
		int sky = Math.round((from >>> 16) + ((to >>> 16) - (from >>> 16)) * t);
		return sky << 16 | block;
	}

	@Override
	public boolean shouldRenderOffScreen(GuyStakeBlockEntity stake) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 96;
	}

	@Override
	public AABB getRenderBoundingBox(GuyStakeBlockEntity stake) {
		return new AABB(stake.getBlockPos()).inflate(GuyLineRules.REACH + 1, 17, GuyLineRules.REACH + 1);
	}
}
