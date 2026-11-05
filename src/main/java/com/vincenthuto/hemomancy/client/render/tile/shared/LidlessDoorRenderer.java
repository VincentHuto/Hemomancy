package com.vincenthuto.hemomancy.client.render.tile.shared;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.LidlessPassageClientState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.block.shared.LidlessDoorBlock;
import com.vincenthuto.hemomancy.common.tile.shared.LidlessDoorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * The void in a Lidless Door's panel: one quad on each face of the panel, between the jambs and under the lintel,
 * drawn with the panel shader. The door the local player is crossing draws no panel, so the crossing view's doorway
 * windows show the world and not the panel.
 */
public class LidlessDoorRenderer implements BlockEntityRenderer<LidlessDoorBlockEntity> {
	/** The jambs are 2/16 wide; the panel sits 1/16 either side of the block's centre plane. */
	private static final float INSET = 2 / 16F;
	private static final float TOP = 2 - 2 / 16F;
	private static final float HALF_THICKNESS = 1 / 16F;

	public LidlessDoorRenderer(BlockEntityRendererProvider.Context context) {
	}

	/** A stable per-door seed, so each door's void differs and stays its own between frames. */
	public static float seed(BlockPos pos) {
		return (pos.hashCode() & 0xFFFF) / 977.0F;
	}

	@Override
	public void render(LidlessDoorBlockEntity door, float partialTick, PoseStack pose, MultiBufferSource buffers,
			int light, int overlay) {
		LidlessPassageClientState.View local = LidlessPassageClientState.local();
		if (local != null && local.door().equals(door.getBlockPos())) return;
		BlockState state = door.getBlockState();
		if (!state.hasProperty(LidlessDoorBlock.FACING) || door.getLevel() == null) return;
		Direction facing = state.getValue(LidlessDoorBlock.FACING);
		float time = AnimationTime.gameTime(door.getLevel(), partialTick);
		float seed = seed(door.getBlockPos());
		Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
		// Looking into the door from its front, the exit lies behind the panel: forward is away from the viewer.
		face(pose, buffers, light, time, seed, normal, normal.scale(-1));
		face(pose, buffers, light, time, seed + 0.5F, normal.scale(-1), normal);
	}

	private static void face(PoseStack pose, MultiBufferSource buffers, int light, float time, float seed,
			Vec3 outward, Vec3 forward) {
		Matrix4f matrix = pose.last().pose();
		Vec3 up = new Vec3(0, 1, 0);
		Vec3 right = up.cross(forward);
		// Block-entity vertices are camera-relative world space, so the passage basis is passed in world space.
		VertexConsumer consumer = buffers.getBuffer(HemoRenderTypes.lidlessDoorPanel(time, seed, right, up, forward, 0.0F));
		Vec3 centre = new Vec3(0.5, 0, 0.5).add(outward.scale(HALF_THICKNESS));
		Vec3 across = right.scale(0.5 - INSET);
		Vec3 a = centre.subtract(across);
		Vec3 b = centre.add(across);
		vertex(consumer, matrix, pose, a, 0, 0, 0, outward);
		vertex(consumer, matrix, pose, b, 0, 1, 0, outward);
		vertex(consumer, matrix, pose, b, TOP, 1, 1, outward);
		vertex(consumer, matrix, pose, a, TOP, 0, 1, outward);
	}

	private static void vertex(VertexConsumer consumer, Matrix4f matrix, PoseStack pose, Vec3 at, float y, float u,
			float v, Vec3 normal) {
		consumer.addVertex(matrix, (float) at.x, (float) at.y + y, (float) at.z)
				.setColor(1F, 1F, 1F, 1F)
				.setUv(u, v)
				.setLight(0xF000F0)
				.setNormal(pose.last(), (float) normal.x, (float) normal.y, (float) normal.z);
	}

	@Override
	public AABB getRenderBoundingBox(LidlessDoorBlockEntity door) {
		return AABB.encapsulatingFullBlocks(door.getBlockPos(), door.getBlockPos().above());
	}
}
