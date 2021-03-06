package com.huto.hemomancy.render.entity.projectile;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.projectile.EntityBloodShot;
import com.huto.hemomancy.model.entity.armor.ModelDarkArrowHorizontal;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class RenderDarkArrow<T extends EntityBloodShot> extends EntityRenderer<EntityBloodShot> {
	ModelDarkArrowHorizontal model = new ModelDarkArrowHorizontal();
	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/block/end_portal.png");

	public RenderDarkArrow(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}

	@SuppressWarnings("unused")
	public void render(EntityBloodShot entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int packedLightIn) {
		matrixStackIn.push();
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(
				MathHelper.lerp(partialTicks, entityIn.prevRotationYaw, entityIn.rotationYaw) - 90.0F));
		matrixStackIn.rotate(Vector3f.ZP
				.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.prevRotationPitch, entityIn.rotationPitch)));
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
		float f9 = (float) entityIn.arrowShake - partialTicks;
		if (f9 > 0.0F) {
			float f10 = -MathHelper.sin(f9 * 3.0F) * f9;
		}
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(0.0F));
		matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(90.0F));
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90.0F));
		matrixStackIn.translate(0.1, -1.1, 0);
		IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntityCutout(TEXTURE));
		MatrixStack.Entry matrixstack$entry = matrixStackIn.getLast();
		Matrix4f matrix4f = matrixstack$entry.getMatrix();
		Matrix3f matrix3f = matrixstack$entry.getNormal();
		model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 100, 100, 100, 1);
		
		matrixStackIn.pop();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	public void drawVertex(Matrix4f matrix, Matrix3f normals, IVertexBuilder vertexBuilder, int offsetX, int offsetY,
			int offsetZ, float textureX, float textureY, int p_229039_9_, int p_229039_10_, int p_229039_11_,
			int packedLightIn) {
		vertexBuilder.pos(matrix, (float) offsetX, (float) offsetY, (float) offsetZ).color(255, 255, 255, 255)
				.tex(textureX, textureY).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn)
				.normal(normals, (float) p_229039_9_, (float) p_229039_11_, (float) p_229039_10_).endVertex();
	}

	@Override
	public ResourceLocation getEntityTexture(EntityBloodShot entity) {
		return TEXTURE;
	}
}
