package com.vincenthuto.hemomancy.render.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.EnthralledDollEntity;
import com.vincenthuto.hemomancy.model.entity.mob.EnthralledDollModel;
import com.vincenthuto.hemomancy.render.layer.mob.EnthralledDollGlowLayer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EnthralledDollRenderer extends MobRenderer<EnthralledDollEntity, EnthralledDollModel<EnthralledDollEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/enthralled_doll/model_enthralled_doll.png");
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");
	private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);
	private static final double VIEW_BOBBING_SCALE = 960.0D;

	public EnthralledDollRenderer(Context renderManagerIn) {
		super(renderManagerIn, new EnthralledDollModel<EnthralledDollEntity>(
				renderManagerIn.bakeLayer(EnthralledDollModel.LAYER_LOCATION)), 0.1F);
		this.addLayer(new EnthralledDollGlowLayer<>(this));

	}

	@Override
	public void render(EnthralledDollEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
			MultiBufferSource pBuffer, int pPackedLight) {
		super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);

		Entity owner = pEntity.getOwner();
		if (owner != null) {
			if (owner instanceof LivingEntity puppeteer) {
				pMatrixStack.pushPose();

				// Bobber renderer
//				pMatrixStack.pushPose();
//				pMatrixStack.scale(0.5F, 0.5F, 0.5F);
//				pMatrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
//				pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
//				PoseStack.Pose posestack$pose = pMatrixStack.last();
//				Matrix4f matrix4f = posestack$pose.pose();
//				Matrix3f matrix3f = posestack$pose.normal();
//				VertexConsumer vertexconsumer = pBuffer.getBuffer(RENDER_TYPE);
//				vertex(vertexconsumer, matrix4f, matrix3f, pPackedLight, 0.0F, 0, 0, 1);
//				vertex(vertexconsumer, matrix4f, matrix3f, pPackedLight, 1.0F, 0, 1, 1);
//				vertex(vertexconsumer, matrix4f, matrix3f, pPackedLight, 1.0F, 1, 1, 0);
//				vertex(vertexconsumer, matrix4f, matrix3f, pPackedLight, 0.0F, 1, 0, 0);
//				pMatrixStack.popPose();
//				
//				

				float f = puppeteer.getAttackAnim(pPartialTicks);
				float f1 = Mth.sin(Mth.sqrt(f) * (float) Math.PI);
				float f2 = Mth.lerp(pPartialTicks, puppeteer.yRotO, puppeteer.getYRot()) * ((float) Math.PI / 180F);
				double d0 = 0;
				double d1 = 0;
				double d2 = (double) 0;
				double d3 = 0.0D;
				double d4;
				double d5;
				double d6;
				float f3;
				d4 = Mth.lerp((double) pPartialTicks, puppeteer.xo, puppeteer.getX()) - d1 * d2 - d0 * 0.8D;
				d5 = puppeteer.yo + (double) puppeteer.getEyeHeight()
						+ (puppeteer.getY() - puppeteer.yo) * (double) pPartialTicks - 0.45D;
				d6 = Mth.lerp((double) pPartialTicks, puppeteer.zo, puppeteer.getZ()) - d0 * d2 + d1 * 0.8D;
				f3 = puppeteer.isCrouching() ? -0.1875F : 0.0F;

				double d9 = Mth.lerp((double) pPartialTicks, pEntity.xo, pEntity.getX());
				double d10 = Mth.lerp((double) pPartialTicks, pEntity.yo, pEntity.getY()) + 0.25D;
				double d8 = Mth.lerp((double) pPartialTicks, pEntity.zo, pEntity.getZ());
				float f4 = (float) (d4 - d9);
				float f5 = (float) (d5 - d10) + f3;
				float f6 = (float) (d6 - d8);
				VertexConsumer vertexconsumer1 = pBuffer.getBuffer(RenderType.lineStrip());
				PoseStack.Pose posestack$pose1 = pMatrixStack.last();
				int j = 16;
				Quaternion alpha = Vector3f.YP.rotationDegrees((float) puppeteer.getLevel().getGameTime());
				int alph = (int) (alpha.r() * 100);

				// System.out.println(255 + alph);
				for (int k = 0; k <= 16; ++k) {
					stringVertex(f4, f5, f6, vertexconsumer1, posestack$pose1, fraction(k, 16), fraction(k + 1, 16),
							255 + alph);
				}
				pMatrixStack.popPose();

			}
		}
	}

	private static float fraction(int p_114691_, int p_114692_) {
		return (float) p_114691_ / (float) p_114692_;
	}

	private static void vertex(VertexConsumer p_114712_, Matrix4f p_114713_, Matrix3f p_114714_, int p_114715_,
			float p_114716_, int p_114717_, int p_114718_, int p_114719_) {

		p_114712_.vertex(p_114713_, p_114716_ - 0.5F, (float) p_114717_ - 0.5F, 0.0F).color(255, 255, 255, 255)
				.uv((float) p_114718_, (float) p_114719_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_114715_)
				.normal(p_114714_, 0.0F, 1.0F, 0.0F).endVertex();
	}

	private static void stringVertex(float x, float y, float z, VertexConsumer p_174122_, PoseStack.Pose p_174123_,
			float p_174124_, float p_174125_, int alpha) {
		float f = x * p_174124_;
		float f1 = y * (p_174124_ * p_174124_ + p_174124_) * 0.5F + 0.25F;
		float f2 = z * p_174124_;
		float f3 = x * p_174125_ - f;
		float f4 = y * (p_174125_ * p_174125_ + p_174125_) * 0.5F + 0.25F - f1;
		float f5 = z * p_174125_ - f2;
		float f6 = Mth.sqrt(f3 * f3 + f4 * f4 + f5 * f5);
		f3 /= f6;
		f4 /= f6;
		f5 /= f6;
		p_174122_.vertex(p_174123_.pose(), f, f1, f2).color(alpha, 0, 0, 100).normal(p_174123_.normal(), f3, f4, f5)
				.endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(EnthralledDollEntity entity) {
		return TEXTURE;

	}

}
