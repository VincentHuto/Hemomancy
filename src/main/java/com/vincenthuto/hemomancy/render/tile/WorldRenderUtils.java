/*
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Matrix3f
 *  com.mojang.math.Matrix4f
 *  com.mojang.math.Quaternion
 *  com.mojang.math.Vector3f
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.vincenthuto.hemomancy.render.tile;

import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.event.ClientTickHandler;
import com.vincenthuto.hemomancy.init.RenderTypeInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WorldRenderUtils {
	private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0) / 2.0);
	public static final int FULL_BRIGHTNESS = 0xF000F0;

	public static void renderRadiant(float ageTicks, PoseStack matrixStack, MultiBufferSource bufferIn,
			int[] innerColor, int[] outerColor, int alpha, float scale, boolean grow) {
		float rotationByAge = ageTicks / 200.0f;
		Random random = new Random(432L);
		VertexConsumer vertexconsumer2 = bufferIn.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);
		matrixStack.pushPose();
		if (grow) {
			float growth = 1.0f + scale * 25.0f;
			matrixStack.scale(growth, growth, growth);
		} else {
			matrixStack.scale(scale, scale, scale);
		}
		alpha = Math.min(alpha, 64);
		for (int i = 0; i < 40; ++i) {
			matrixStack.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0f));
			matrixStack.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0f));
			matrixStack.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0f));
			matrixStack.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0f));
			matrixStack.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0f));
			matrixStack.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0f + 90.0f * rotationByAge));
			float hOffset = i % 3 == 0 ? random.nextFloat() * 0.55f : random.nextFloat() * 0.25f;
			float vOffset = random.nextFloat() * 0.25f;
			Matrix4f matrix4f = matrixStack.last().pose();
			WorldRenderUtils.vertex01(vertexconsumer2, matrix4f, innerColor, alpha);
			WorldRenderUtils.vertex2(vertexconsumer2, matrix4f, hOffset, vOffset, outerColor);
			WorldRenderUtils.vertex3(vertexconsumer2, matrix4f, hOffset, vOffset, innerColor);
			WorldRenderUtils.vertex01(vertexconsumer2, matrix4f, innerColor, alpha);
			WorldRenderUtils.vertex3(vertexconsumer2, matrix4f, hOffset, vOffset, outerColor);
			WorldRenderUtils.vertex4(vertexconsumer2, matrix4f, hOffset, vOffset, innerColor);
			WorldRenderUtils.vertex01(vertexconsumer2, matrix4f, innerColor, alpha);
			WorldRenderUtils.vertex4(vertexconsumer2, matrix4f, hOffset, vOffset, outerColor);
			WorldRenderUtils.vertex2(vertexconsumer2, matrix4f, hOffset, vOffset, innerColor);
		}
		matrixStack.popPose();
	}

	public static void renderRadiant(Entity entityIn, PoseStack matrixStackIn, MultiBufferSource bufferIn,
			int[] innerColor, int[] outerColor, int alpha, float scale) {
		WorldRenderUtils.renderRadiant(entityIn.tickCount, matrixStackIn, bufferIn, innerColor, outerColor, alpha,
				scale, true);
	}

	public static void renderRadiantWithDirection(Entity entityIn, PoseStack matrixStackIn, MultiBufferSource bufferIn,
			int[] innerColor, int[] outerColor, int alpha, float scale) {
		float rotationByAge = (float) entityIn.tickCount / 220.0f;
		Random random = new Random(432L);
		VertexConsumer lightingBuilder = bufferIn.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);
		matrixStackIn.pushPose();
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(entityIn.getYRot() - 90.0f));
		matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(entityIn.getXRot() + 90.0f));
		matrixStackIn.translate(0.0, -0.25, 0.0);
		matrixStackIn.scale(scale, scale * 3.0f, scale);
		for (int i = 0; i < 20; ++i) {
			matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0f + 90.0f * rotationByAge));
			float hOffset = random.nextFloat() * 0.25f;
			float vOffset = random.nextFloat() * 0.25f;
			Matrix4f currentMatrix = matrixStackIn.last().pose();
			WorldRenderUtils.vertex01(lightingBuilder, currentMatrix, innerColor, alpha);
			WorldRenderUtils.vertex2(lightingBuilder, currentMatrix, hOffset, vOffset, outerColor);
			WorldRenderUtils.vertex3(lightingBuilder, currentMatrix, hOffset, vOffset, innerColor);
			WorldRenderUtils.vertex01(lightingBuilder, currentMatrix, innerColor, alpha);
			WorldRenderUtils.vertex3(lightingBuilder, currentMatrix, hOffset, vOffset, outerColor);
			WorldRenderUtils.vertex4(lightingBuilder, currentMatrix, hOffset, vOffset, innerColor);
			WorldRenderUtils.vertex01(lightingBuilder, currentMatrix, innerColor, alpha);
			WorldRenderUtils.vertex4(lightingBuilder, currentMatrix, hOffset, vOffset, outerColor);
			WorldRenderUtils.vertex2(lightingBuilder, currentMatrix, hOffset, vOffset, innerColor);
		}
		matrixStackIn.popPose();
	}

	public static void renderBeam(Level world, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn,
			int packedLightIn, Vec3 start, Vec3 end, float lengthPct, ParticleColor color, RenderType renderType) {
		WorldRenderUtils.renderBeam(world, partialTicks, matrixStackIn, bufferIn, packedLightIn, start, end, lengthPct,
				color, 0.01f, renderType);
	}

	public static void renderBeam(Level world, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn,
			int packedLightIn, Vec3 start, Vec3 end, float lengthPct, ParticleColor color, float width,
			RenderType renderType) {
		WorldRenderUtils.renderBeam(world, partialTicks, matrixStackIn, bufferIn, packedLightIn, start, end, lengthPct,
				color, 176, width, renderType);
	}

	public static void renderBeam(Level world, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn,
			int packedLightIn, Vec3 start, Vec3 end, float lengthPct, ParticleColor color, int alpha, float width,
			RenderType renderType) {
		float worldPartialTicks = (float) ClientTickHandler.ticksInGame + partialTicks;
		float uvSlideRate = worldPartialTicks * 0.3f % 1.0f;
		float rotation = 0.0f;
        Vec3 direction = end.subtract(start);
        float beamLength = (float)direction.length() * lengthPct;
        direction = direction.normalize();
        float aCosDirectionY = (float)Math.acos(direction.y);
        float atan2DirectionXZ = (float)Math.atan2(direction.z, direction.x);
		int red = (int) color.getRed();
		int green = (int) color.getGreen();
		int blue = (int) color.getBlue();
		float beamWidth = width;
		float x1Start = Mth.cos((float) (rotation + (float) Math.PI)) * beamWidth;
		float x1End = Mth.cos((float) rotation) * beamWidth;
		float y1Start = Mth.sin((float) (rotation + (float) Math.PI)) * beamWidth;
		float y1End = Mth.sin((float) rotation) * beamWidth;
		float x2Start = Mth.cos((float) (rotation + 1.5707964f)) * beamWidth;
		float x2End = Mth.cos((float) (rotation + 4.712389f)) * beamWidth;
		float y2Start = Mth.sin((float) (rotation + 1.5707964f)) * beamWidth;
		float y2End = Mth.sin((float) (rotation + 4.712389f)) * beamWidth;
		float vSlide = -1.0f - uvSlideRate;
		float f30 = lengthPct * 2.5f + vSlide;
		matrixStackIn.pushPose();
		  matrixStackIn.mulPose(Vector3f.YP .rotationDegrees ((1.5707964f - atan2DirectionXZ) * 57.295776f));
	        matrixStackIn.mulPose(Vector3f.XP .rotationDegrees (aCosDirectionY * 57.295776f));
		VertexConsumer vertexBuilder = bufferIn.getBuffer(renderType);
		PoseStack.Pose activeStack = matrixStackIn.last();
		Matrix4f renderMatrix = activeStack.pose();
		Matrix3f normalMatrix = activeStack.normal();
		float uMin = 0.05f;
		float uMax = 0.95f;
//		WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, (float) start.x, (float) start.y,
//				(float) start.z, red, green, blue, alpha, uMax, f30);
//		WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, (float) start.x, (float) start.y,
//				(float) start.z, red, green, blue, alpha, uMax, vSlide);
//		WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, (float) end.x, (float) end.y,
//				(float) end.z, red, green, blue, alpha, uMin, vSlide);
//		WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, (float) end.x, (float) end.y,
//				(float) end.z, red, green, blue, alpha, uMin, f30);

		WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, x1Start, lengthPct, y1Start, red,
				green, blue, alpha, uMax, f30);
		WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, x1Start, 0.0f, y1Start, red, green,
				blue, alpha, uMax, vSlide);
		WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, x1End, 0.0f, y1End, red, green, blue,
				alpha, uMin, vSlide);
		WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, x1End, lengthPct, y1End, red, green,
				blue, alpha, uMin, f30);
		WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, x2Start, lengthPct, y2Start, red,
				green, blue, alpha, uMax, f30);
		WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, x2Start, 0.0f, y2Start, red, green,
				blue, alpha, uMax, vSlide);
		WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, x2End, 0.0f, y2End, red, green, blue,
				alpha, uMin, vSlide);
		WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, x2End, lengthPct, y2End, red, green,
				blue, alpha, uMin, f30);
		matrixStackIn.popPose();
	}

//	public static void renderManaweavePattern(ManaweavingPattern pattern, Quaternion rotation, PoseStack matrixStackIn,
//			MultiBufferSource bufferIn, boolean inventory) {
//		byte[][] points = pattern.get();
//		float offsetX = (float) points.length / 2.0f;
//		float offsetY = (float) points[0].length / 2.0f;
//		float baseScale = 0.15f;
//		matrixStackIn.pushPose();
//		matrixStackIn.translate(0.0, 2.0, 0.0);
//		matrixStackIn.mulPose(rotation);
//		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0f));
//		matrixStackIn.translate(-3.6 * (double) baseScale, -3.6 * (double) baseScale, 0.0);
//		matrixStackIn.scale(baseScale, baseScale, baseScale);
//		PoseStack.Pose matrixstack$entry = matrixStackIn.last();
//		Matrix4f renderMatrix = matrixstack$entry.pose();
//		Matrix3f normalMatrix = matrixstack$entry.normal();
//		VertexConsumer vertexBuilder = bufferIn.getBuffer (MARenderTypes.RENDER_TYPE_MANAWEAVE);
//		for (int i = 0; i < points.length; ++i) {
//			for (int j = 0; j < points[i].length; ++j) {
//				if (points[i][j] != 1)
//					continue;
//				float originX = offsetX - (float) j * 0.5f;
//				float originY = offsetY - (float) i * 0.5f;
//				WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, 0.0f + originX, 0.0f + originY,
//						0.0f, 109, 227, 220, 176, 0.0f, 1.0f);
//				WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, 1.0f + originX, 0.0f + originY,
//						0.0f, 109, 227, 220, 176, 1.0f, 1.0f);
//				WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, 1.0f + originX, 1.0f + originY,
//						0.0f, 109, 227, 220, 176, 1.0f, 0.0f);
//				WorldRenderUtils.createVertex(vertexBuilder, renderMatrix, normalMatrix, 0.0f + originX, 1.0f + originY,
//						0.0f, 109, 227, 220, 176, 0.0f, 0.0f);
//			}
//		}
//		matrixStackIn.popPose();
//	}

	private static void vertex01(VertexConsumer vertexConsumer, Matrix4f renderMatrix, int[] rgb, int alpha) {
		vertexConsumer.vertex(renderMatrix, 0.0f, 0.0f, 0.0f).color(255, 255, 255, alpha).endVertex();
		vertexConsumer.vertex(renderMatrix, 0.0f, 0.0f, 0.0f).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
	}

	private static void vertex2(VertexConsumer vertexConsumer, Matrix4f renderMatrix, float y, float length,
			int[] rgb) {
		vertexConsumer.vertex(renderMatrix, -HALF_SQRT_3 * length, y, -0.2f * length).color(rgb[0], rgb[1], rgb[2], 0)
				.endVertex();
	}

	private static void vertex3(VertexConsumer vertexConsumer, Matrix4f renderMatrix, float y, float length,
			int[] rgb) {
		vertexConsumer.vertex(renderMatrix, HALF_SQRT_3 * length, y, -0.2f * length).color(rgb[0], rgb[1], rgb[2], 0)
				.endVertex();
	}

	private static void vertex4(VertexConsumer vertexConsumer, Matrix4f renderMatrix, float y, float length,
			int[] rgb) {
		vertexConsumer.vertex(renderMatrix, 0.0f, y, 1.0f * length).color(rgb[0], rgb[1], rgb[2], 0).endVertex();
	}

	private static void createVertex(VertexConsumer vertexBuilder, Matrix4f renderMatrix, Matrix3f normalMatrix,
			float x, float y, float z, int colorRed, int colorGreen, int colorBlue, int alpha, float u, float v) {
		vertexBuilder.vertex(renderMatrix, x, y, z).color(colorRed, colorGreen, colorBlue, alpha).uv(u, v)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(normalMatrix, 1.0f, 0.0f, 0.0f)
				.endVertex();
	}
}
