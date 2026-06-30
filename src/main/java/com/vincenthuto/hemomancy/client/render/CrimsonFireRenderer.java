package com.vincenthuto.hemomancy.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class CrimsonFireRenderer {
	private static final ResourceLocation CRIMSON_FIRE_SPRITE = ResourceLocation.parse("hemomancy:block/crimson_flames");

	private CrimsonFireRenderer() {
	}

	public static void renderScreenOverlay(Minecraft minecraft, PoseStack poseStack) {
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.depthFunc(519);
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		TextureAtlasSprite sprite = crimsonSprite();
		RenderSystem.setShaderTexture(0, sprite.atlasLocation());
		float u0 = sprite.getU0();
		float u1 = sprite.getU1();
		float midU = (u0 + u1) / 2.0F;
		float v0 = sprite.getV0();
		float v1 = sprite.getV1();
		float midV = (v0 + v1) / 2.0F;
		float shrink = sprite.uvShrinkRatio();
		float shrunkU0 = Mth.lerp(shrink, u0, midU);
		float shrunkU1 = Mth.lerp(shrink, u1, midU);
		float shrunkV0 = Mth.lerp(shrink, v0, midV);
		float shrunkV1 = Mth.lerp(shrink, v1, midV);

		for (int i = 0; i < 2; i++) {
			poseStack.pushPose();
			poseStack.translate((float) (-(i * 2 - 1)) * 0.24F, -0.3F, 0.0F);
			poseStack.mulPose(Axis.YP.rotationDegrees((float) (i * 2 - 1) * 10.0F));
			Matrix4f matrix = poseStack.last().pose();
			BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
					DefaultVertexFormat.POSITION_TEX_COLOR);
			builder.addVertex(matrix, -0.5F, -0.5F, -0.5F).setUv(shrunkU1, shrunkV1)
					.setColor(1.0F, 1.0F, 1.0F, 0.9F);
			builder.addVertex(matrix, 0.5F, -0.5F, -0.5F).setUv(shrunkU0, shrunkV1)
					.setColor(1.0F, 1.0F, 1.0F, 0.9F);
			builder.addVertex(matrix, 0.5F, 0.5F, -0.5F).setUv(shrunkU0, shrunkV0)
					.setColor(1.0F, 1.0F, 1.0F, 0.9F);
			builder.addVertex(matrix, -0.5F, 0.5F, -0.5F).setUv(shrunkU1, shrunkV0)
					.setColor(1.0F, 1.0F, 1.0F, 0.9F);
			BufferUploader.drawWithShader(builder.buildOrThrow());
			poseStack.popPose();
		}

		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.depthFunc(515);
	}

	public static void renderEntityFlames(PoseStack poseStack, MultiBufferSource buffer, Entity entity,
			Quaternionf quaternion) {
		TextureAtlasSprite sprite = crimsonSprite();
		poseStack.pushPose();
		float scale = entity.getBbWidth() * 1.4F;
		poseStack.scale(scale, scale, scale);
		float width = 0.5F;
		float z = 0.0F;
		float height = entity.getBbHeight() / scale;
		float yOffset = 0.0F;
		poseStack.mulPose(quaternion);
		poseStack.translate(0.0F, 0.0F, 0.3F - (float) ((int) height) * 0.02F);
		float zOffset = 0.0F;
		VertexConsumer vertexConsumer = buffer.getBuffer(Sheets.cutoutBlockSheet());

		for (PoseStack.Pose pose = poseStack.last(); height > 0.0F;) {
			float u0 = sprite.getU0();
			float v0 = sprite.getV0();
			float u1 = sprite.getU1();
			float v1 = sprite.getV1();

			fireVertex(pose, vertexConsumer, -width - z, 0.0F - yOffset, zOffset, u1, v1);
			fireVertex(pose, vertexConsumer, width - z, 0.0F - yOffset, zOffset, u0, v1);
			fireVertex(pose, vertexConsumer, width - z, 1.4F - yOffset, zOffset, u0, v0);
			fireVertex(pose, vertexConsumer, -width - z, 1.4F - yOffset, zOffset, u1, v0);
			height -= 0.45F;
			yOffset -= 0.45F;
			width *= 0.9F;
			zOffset -= 0.03F;
		}

		poseStack.popPose();
	}

	private static TextureAtlasSprite crimsonSprite() {
		return Minecraft.getInstance()
				.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
				.apply(CRIMSON_FIRE_SPRITE);
	}

	private static void fireVertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z,
			float texU, float texV) {
		buffer.addVertex(pose, x, y, z)
				.setColor(-1)
				.setUv(texU, texV)
				.setUv1(0, 10)
				.setLight(240)
				.setNormal(pose, 0.0F, 1.0F, 0.0F);
	}
}
