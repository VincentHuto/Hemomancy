package com.huto.hemomancy.render.entity.projectile;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.projectile.EntityBloodShot;
import com.huto.hemomancy.model.entity.armor.ModelDarkArrowHorizontal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RenderDarkArrow<T extends EntityBloodShot> extends EntityRenderer<EntityBloodShot> {
	ModelDarkArrowHorizontal model = new ModelDarkArrowHorizontal();
	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/block/end_portal.png");

	public RenderDarkArrow(Context renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	@SuppressWarnings("unused")
	public void render(EntityBloodShot entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int packedLightIn) {
		matrixStackIn.pushPose();
		matrixStackIn
				.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot()) - 90.0F));
		matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot())));
		int i = 0;
		float f = 0.0F;
		float f1 = 0.5F;
		float f2 = 0.0F;
		float f3 = 0.15625F;
		float f4 = 0.0F;
		float f5 = 0.15625F;
		float f6 = 0.15625F;
		float f7 = 0.3125F;
		float f8 = 0.05625F;
		float f9 = entityIn.shakeTime - partialTicks;
		if (f9 > 0.0F) {
			float f10 = -Mth.sin(f9 * 3.0F) * f9;
		}
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(0.0F));
		matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
		matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
		matrixStackIn.translate(0.1, -1.1, 0);
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.entityCutout(TEXTURE));
		PoseStack.Pose matrixstack$entry = matrixStackIn.last();
		Matrix4f matrix4f = matrixstack$entry.pose();
		Matrix3f matrix3f = matrixstack$entry.normal();
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 100, 100, 100, 1);

		matrixStackIn.popPose();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	public void drawVertex(Matrix4f matrix, Matrix3f normals, VertexConsumer vertexBuilder, int offsetX, int offsetY,
			int offsetZ, float textureX, float textureY, int p_229039_9_, int p_229039_10_, int p_229039_11_,
			int packedLightIn) {
		vertexBuilder.vertex(matrix, offsetX, offsetY, offsetZ).color(255, 255, 255, 255)
				.uv(textureX, textureY).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn)
				.normal(normals, p_229039_9_, p_229039_11_, p_229039_10_).endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(EntityBloodShot entity) {
		return TEXTURE;
	}
}
