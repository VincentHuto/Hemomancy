package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.HematicStakeBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HematicStakeBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;

import org.joml.Matrix4f;

public class HematicStakeRenderer implements BlockEntityRenderer<HematicStakeBlockEntity> {
	private static final int BLACK = 0xFF020000;
	private static final int FACETS = 9;
	private static final int TIME_CYCLE_MS = 240000;

	public HematicStakeRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(HematicStakeBlockEntity be, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}

		float timeSeconds = ((mc.level.getGameTime() + partialTick) % TIME_CYCLE_MS) / 20.0f;
		float shardSeed = Math.floorMod(be.getBlockPos().asLong(), 10007L) / 10007.0f;
		VertexConsumer vc = buffer.getBuffer(HemoRenderTypes.monolithFragment(timeSeconds, shardSeed, 0.0f, 0.72f, 0.0f));

		poseStack.pushPose();
		applyAttachmentTransform(be, poseStack);
		poseStack.mulPose(Axis.YP.rotationDegrees(seedNoise(shardSeed, 17) * 28.0f));
		drawJaggedStakeShard(vc, poseStack.last().pose(), LightTexture.FULL_BRIGHT, shardSeed, 1.0f);
		poseStack.popPose();
	}

	private static void applyAttachmentTransform(HematicStakeBlockEntity be, PoseStack poseStack) {
		AttachFace face = be.getBlockState().getValue(HematicStakeBlock.FACE);
		Direction facing = be.getBlockState().getValue(HematicStakeBlock.FACING);
		if (face == AttachFace.CEILING) {
			poseStack.translate(0.5, 0.98, 0.5);
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
		} else if (face == AttachFace.WALL) {
			poseStack.translate(0.5 - facing.getStepX() * 0.48, 0.5, 0.5 - facing.getStepZ() * 0.48);
			rotateUpToFacing(poseStack, facing);
		} else {
			poseStack.translate(0.5, 0.02, 0.5);
		}
	}

	private static void rotateUpToFacing(PoseStack poseStack, Direction facing) {
		switch (facing) {
			case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
			case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
			case EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
			case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
			default -> {
			}
		}
	}

	public static void drawJaggedStakeShard(VertexConsumer vc, Matrix4f mat, int light, float seed, float scale) {
		float height = (1.18f + seedNoise(seed, 3) * 0.18f) * scale;
		float baseRadius = 0.235f * scale;
		float midRadius = 0.175f * scale;
		float waistY = height * (0.36f + seedNoise(seed, 5) * 0.05f);
		float shoulderY = height * (0.74f + seedNoise(seed, 7) * 0.04f);
		float tipX = seedNoise(seed, 11) * 0.055f * scale;
		float tipZ = seedNoise(seed, 13) * 0.055f * scale;
		Point tip = new Point(tipX, height, tipZ, 0.5f, 0.0f);

		Point[] base = ring(seed, 23, FACETS, 0.0f, baseRadius, 1.0f);
		Point[] waist = ring(seed, 41, FACETS, waistY, midRadius, 0.62f);
		Point[] shoulder = ring(seed, 59, FACETS, shoulderY, baseRadius * 0.54f, 0.28f);

		for (int i = 0; i < FACETS; i++) {
			int next = (i + 1) % FACETS;
			drawTriangle(vc, mat, base[i], waist[i], waist[next], light);
			drawTriangle(vc, mat, base[i], waist[next], base[next], light);
			drawTriangle(vc, mat, waist[i], shoulder[i], shoulder[next], light);
			drawTriangle(vc, mat, waist[i], shoulder[next], waist[next], light);
			drawTriangle(vc, mat, shoulder[i], tip, shoulder[next], light);
		}

		for (int spur = 0; spur < 3; spur++) {
			int i = Math.floorMod((int) (seed * 997.0f) + spur * 3, FACETS);
			int next = (i + 1) % FACETS;
			Point root = waist[i];
			Point rootNext = waist[next];
			float angle = (float) ((i + 0.5f) * Math.PI * 2.0 / FACETS);
			float out = (0.10f + Math.abs(seedNoise(seed, 80 + spur)) * 0.05f) * scale;
			float y = waistY + (0.12f + spur * 0.07f) * scale;
			Point spurTip = new Point((float) Math.cos(angle) * (baseRadius + out), y,
					(float) Math.sin(angle) * (baseRadius + out), 0.85f, 0.45f + spur * 0.08f);
			drawTriangle(vc, mat, root, spurTip, rootNext, light);
		}
	}

	private static Point[] ring(float seed, int offset, int count, float y, float radius, float v) {
		Point[] points = new Point[count];
		for (int i = 0; i < count; i++) {
			float angle = (float) (i * Math.PI * 2.0 / count);
			float radial = radius * (0.82f + Math.abs(seedNoise(seed, offset + i * 7)) * 0.36f);
			float twist = seedNoise(seed, offset + i * 11) * 0.13f;
			points[i] = new Point((float) Math.cos(angle + twist) * radial, y,
					(float) Math.sin(angle + twist) * radial, i / (float) count, v);
		}
		return points;
	}

	private static void drawTriangle(VertexConsumer vc, Matrix4f mat, Point a, Point b, Point c, int light) {
		int alpha = (BLACK >>> 24) & 0xFF;
		int red = (BLACK >>> 16) & 0xFF;
		int green = (BLACK >>> 8) & 0xFF;
		int blue = BLACK & 0xFF;
		vc.addVertex(mat, a.x(), a.y(), a.z()).setColor(red, green, blue, alpha).setUv(a.u(), a.v()).setLight(light);
		vc.addVertex(mat, b.x(), b.y(), b.z()).setColor(red, green, blue, alpha).setUv(b.u(), b.v()).setLight(light);
		vc.addVertex(mat, c.x(), c.y(), c.z()).setColor(red, green, blue, alpha).setUv(c.u(), c.v()).setLight(light);
	}

	private static float seedNoise(float seed, int salt) {
		int x = (int) (seed * 1_000_000.0f) + salt * 73428767;
		x ^= x >>> 13;
		x *= 1274126177;
		x ^= x >>> 16;
		return ((x & 0xFFFF) / 32767.5f) - 1.0f;
	}

	private record Point(float x, float y, float z, float u, float v) {
	}
}
