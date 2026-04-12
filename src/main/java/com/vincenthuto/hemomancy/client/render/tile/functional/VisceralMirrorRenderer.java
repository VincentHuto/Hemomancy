package com.vincenthuto.hemomancy.client.render.tile.functional;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.block.functional.VisceralMirrorBlock;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.functional.VisceralMirrorBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class VisceralMirrorRenderer implements BlockEntityRenderer<VisceralMirrorBlockEntity> {

	private static final float MIRROR_WIDTH = 1.0F;
	private static final float MIRROR_HALF_WIDTH = MIRROR_WIDTH * 0.5F;
	private static final float MIRROR_HEIGHT = 1.6F;
	private static final float MIRROR_BOTTOM = 0.2F;
	private static final float MIRROR_TOP = MIRROR_BOTTOM + MIRROR_HEIGHT;
	private static final float SURFACE_DEPTH = 0.502F;
	private static final float REFLECTION_SCALE = 0.8F;
	private static final float REFLECTION_DEPTH_SCALE = 0.08F;
	private static final float MAX_REFLECTION_DISTANCE = 6.0F;

	public VisceralMirrorRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public boolean shouldRenderOffScreen(VisceralMirrorBlockEntity blockEntity) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 64;
	}

	@Override
	public void render(VisceralMirrorBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		if (blockEntity.getLevel() == null) {
			return;
		}

		Direction facing = blockEntity.getBlockState().getValue(VisceralMirrorBlock.FACING);
		AbstractClientPlayer player = getReflectedPlayer();

		poseStack.pushPose();
		poseStack.translate(0.5D, 0.0D, 0.5D);
		poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

		if (player != null) {
			renderReflection(blockEntity, player, partialTicks, poseStack, buffer, combinedLight);
		}

		renderMirrorPlane(blockEntity, partialTicks, poseStack, buffer);
		poseStack.popPose();
	}

	private AbstractClientPlayer getReflectedPlayer() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.player.isInvisible()) {
			return null;
		}
		return minecraft.player;
	}

	private void renderReflection(VisceralMirrorBlockEntity blockEntity, AbstractClientPlayer player, float partialTicks,
			PoseStack poseStack, MultiBufferSource buffer, int combinedLight) {
		Direction facing = blockEntity.getBlockState().getValue(VisceralMirrorBlock.FACING);
		Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
		Vec3 right = new Vec3(-normal.z, 0.0D, normal.x);
		Vec3 planeCenter = getPlaneCenter(blockEntity.getBlockPos(), normal);
		Vec3 playerFeet = player.position();
		Vec3 playerHorizontalOffset = new Vec3(playerFeet.x - planeCenter.x, 0.0D, playerFeet.z - planeCenter.z);

		double distanceFromMirror = playerHorizontalOffset.dot(normal);
		if (distanceFromMirror <= 0.0D || distanceFromMirror > MAX_REFLECTION_DISTANCE) {
			return;
		}

		float lateralOffset = Mth.clamp((float) playerHorizontalOffset.dot(right),
				-MIRROR_HALF_WIDTH + 0.12F, MIRROR_HALF_WIDTH - 0.12F);
		float verticalOffset = MIRROR_BOTTOM + Mth.clamp((float) (playerFeet.y - blockEntity.getBlockPos().getY()),
				-0.25F, 0.55F);
		float reflectedDepth = -Mth.clamp((float) distanceFromMirror * 0.045F, 0.015F, 0.16F);

		EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		dispatcher.setRenderShadow(false);

		poseStack.pushPose();
		poseStack.translate(lateralOffset, verticalOffset, SURFACE_DEPTH + reflectedDepth);
		poseStack.scale(-REFLECTION_SCALE, REFLECTION_SCALE, REFLECTION_DEPTH_SCALE);
		dispatcher.render(player, 0.0D, 0.0D, 0.0D, player.getViewYRot(partialTicks), partialTicks, poseStack, buffer,
				combinedLight);
		poseStack.popPose();

		dispatcher.setRenderShadow(true);
	}

	private void renderMirrorPlane(VisceralMirrorBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer) {
		float time = blockEntity.getLevel().getGameTime() + partialTicks;
		float pulse = 0.78F + 0.12F * Mth.sin(time * 0.08F);
		float inner = 0.22F * pulse;
		float rim = 0.35F * pulse;

		VertexConsumer consumer = buffer.getBuffer(RenderTypeInit.MIRROR_SURFACE);
		Matrix4f matrix = poseStack.last().pose();

		addQuad(consumer, matrix, -MIRROR_HALF_WIDTH, MIRROR_BOTTOM, SURFACE_DEPTH, MIRROR_HALF_WIDTH, MIRROR_TOP,
				SURFACE_DEPTH, 0.09F, 0.0F, 0.12F, inner);
		addQuad(consumer, matrix, -MIRROR_HALF_WIDTH, MIRROR_BOTTOM, SURFACE_DEPTH - 0.001F, MIRROR_HALF_WIDTH,
				MIRROR_TOP, SURFACE_DEPTH - 0.001F, 0.45F, 0.1F, 0.18F, rim);
	}

	private void addQuad(VertexConsumer consumer, Matrix4f matrix, float minX, float minY, float z, float maxX,
			float maxY, float maxZ, float red, float green, float blue, float alpha) {
		consumer.vertex(matrix, minX, minY, z).color(red, green, blue, alpha).endVertex();
		consumer.vertex(matrix, minX, maxY, z).color(red, green, blue, alpha).endVertex();
		consumer.vertex(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
		consumer.vertex(matrix, maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();

		consumer.vertex(matrix, maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
		consumer.vertex(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
		consumer.vertex(matrix, minX, maxY, z).color(red, green, blue, alpha).endVertex();
		consumer.vertex(matrix, minX, minY, z).color(red, green, blue, alpha).endVertex();
	}

	private Vec3 getPlaneCenter(BlockPos pos, Vec3 normal) {
		return new Vec3(pos.getX() + 0.5D, pos.getY() + MIRROR_BOTTOM + MIRROR_HEIGHT * 0.5D, pos.getZ() + 0.5D)
				.add(normal.scale(SURFACE_DEPTH));
	}
}
