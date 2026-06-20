package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.joml.Matrix4f;

public class MonolithicCornerstoneItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final int BLACK = 0xFF010000;
	private static final int TIME_CYCLE_MS = 240000;

	public MonolithicCornerstoneItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		applyDisplayTransform(displayContext, poseStack);

		float timeSeconds = (System.currentTimeMillis() % (long) TIME_CYCLE_MS) / 1000.0f;
		float seed = Math.floorMod(System.identityHashCode(stack), 10007) / 10007.0f + stack.getCount() * 0.019f;
		float guiClamp = displayContext == ItemDisplayContext.GUI ? 1.0f : 0.0f;
		VertexConsumer vc = buffer.getBuffer(HemoRenderTypes.monolithFragment(timeSeconds, seed, 0.28f, 0.92f, guiClamp));
		drawCube(vc, poseStack.last().pose(), LightTexture.FULL_BRIGHT, displayContext == ItemDisplayContext.GUI);

		poseStack.popPose();
	}

	private static void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
		if (displayContext == ItemDisplayContext.GUI) {
			poseStack.mulPose(Axis.XP.rotationDegrees(24.0f));
			poseStack.mulPose(Axis.YP.rotationDegrees(-36.0f));
			poseStack.mulPose(Axis.ZP.rotationDegrees(-8.0f));
			poseStack.scale(1.42f, 1.42f, 1.42f);
		} else if (displayContext == ItemDisplayContext.GROUND) {
			poseStack.mulPose(Axis.XP.rotationDegrees(62.0f));
			poseStack.mulPose(Axis.YP.rotationDegrees(18.0f));
			poseStack.scale(0.82f, 0.82f, 0.82f);
		} else if (displayContext == ItemDisplayContext.FIXED) {
			poseStack.mulPose(Axis.XP.rotationDegrees(18.0f));
			poseStack.mulPose(Axis.YP.rotationDegrees(-32.0f));
			poseStack.scale(1.12f, 1.12f, 1.12f);
		} else if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
				|| displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
			poseStack.mulPose(Axis.XP.rotationDegrees(16.0f));
			poseStack.mulPose(Axis.YP.rotationDegrees(-18.0f));
			poseStack.mulPose(Axis.ZP.rotationDegrees(-9.0f));
			poseStack.scale(0.72f, 0.72f, 0.72f);
		} else {
			poseStack.mulPose(Axis.XP.rotationDegrees(18.0f));
			poseStack.mulPose(Axis.YP.rotationDegrees(-28.0f));
			poseStack.scale(0.72f, 0.72f, 0.72f);
		}
	}

	private static void drawCube(VertexConsumer vc, Matrix4f mat, int light, boolean gui) {
		float half = gui ? 0.205f : 0.235f;
		drawFace(vc, mat, light, new Vec3f(-half, -half, half), new Vec3f(half, -half, half),
				new Vec3f(half, half, half), new Vec3f(-half, half, half), 0.38f, 0.38f); // front
		drawFace(vc, mat, light, new Vec3f(half, -half, -half), new Vec3f(-half, -half, -half),
				new Vec3f(-half, half, -half), new Vec3f(half, half, -half), 0.56f, 0.38f); // back
		drawFace(vc, mat, light, new Vec3f(half, -half, half), new Vec3f(half, -half, -half),
				new Vec3f(half, half, -half), new Vec3f(half, half, half), 0.38f, 0.56f); // right
		drawFace(vc, mat, light, new Vec3f(-half, -half, -half), new Vec3f(-half, -half, half),
				new Vec3f(-half, half, half), new Vec3f(-half, half, -half), 0.56f, 0.56f); // left
		drawFace(vc, mat, light, new Vec3f(-half, half, -half), new Vec3f(-half, half, half),
				new Vec3f(half, half, half), new Vec3f(half, half, -half), 0.47f, 0.29f); // top
		drawFace(vc, mat, light, new Vec3f(-half, -half, -half), new Vec3f(half, -half, -half),
				new Vec3f(half, -half, half), new Vec3f(-half, -half, half), 0.47f, 0.65f); // bottom
	}

	private static void drawFace(VertexConsumer vc, Matrix4f mat, int light, Vec3f a, Vec3f b, Vec3f c, Vec3f d,
			float u, float v) {
		float span = 0.16f;
		Point p0 = new Point(a.x(), a.y(), a.z(), u, v + span);
		Point p1 = new Point(b.x(), b.y(), b.z(), u + span, v + span);
		Point p2 = new Point(c.x(), c.y(), c.z(), u + span, v);
		Point p3 = new Point(d.x(), d.y(), d.z(), u, v);
		drawQuad(vc, mat, p0, p1, p2, p3, light);
	}

	private static void drawQuad(VertexConsumer vc, Matrix4f mat, Point a, Point b, Point c, Point d, int light) {
		drawTriangle(vc, mat, a, b, c, light);
		drawTriangle(vc, mat, a, c, d, light);
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

	private record Point(float x, float y, float z, float u, float v) {
	}

	private record Vec3f(float x, float y, float z) {
	}
}
