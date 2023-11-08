package com.vincenthuto.hemomancy.client.render.entity.mob;

import java.util.List;
import java.util.Optional;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.EnthralledDollModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.EnthralledDollGlowLayer;
import com.vincenthuto.hemomancy.common.entity.mob.BloodDrunkPuppeteerEntity;
import com.vincenthuto.hemomancy.common.entity.mob.EnthralledDollEntity;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class EnthralledDollRenderer
		extends MobRenderer<EnthralledDollEntity, EnthralledDollModel<EnthralledDollEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/enthralled_doll/model_enthralled_doll.png");
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");
	private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);
	private static final double VIEW_BOBBING_SCALE = 960.0D;

	private static float fraction(int p_114691_, int p_114692_) {
		return (float) p_114691_ / (float) p_114692_;
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

	private static void vertex(VertexConsumer p_114712_, Matrix4f p_114713_, Matrix3f p_114714_, int p_114715_,
			float p_114716_, int p_114717_, int p_114718_, int p_114719_) {

		p_114712_.vertex(p_114713_, p_114716_ - 0.5F, p_114717_ - 0.5F, 0.0F).color(255, 255, 255, 255)
				.uv(p_114718_, p_114719_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_114715_)
				.normal(p_114714_, 0.0F, 1.0F, 0.0F).endVertex();
	}

	public EnthralledDollRenderer(Context renderManagerIn) {
		super(renderManagerIn, new EnthralledDollModel<EnthralledDollEntity>(
				renderManagerIn.bakeLayer(EnthralledDollModel.LAYER_LOCATION)), 0.1F);
		this.addLayer(new EnthralledDollGlowLayer<>(this));

	}

	@Override
	public ResourceLocation getTextureLocation(EnthralledDollEntity entity) {
		return TEXTURE;

	}

	@Override
	public void render(EnthralledDollEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
			MultiBufferSource pBuffer, int pPackedLight) {
		super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);

		List<BloodDrunkPuppeteerEntity> owners = pEntity.level().getNearbyEntities(BloodDrunkPuppeteerEntity.class,
				TargetingConditions.DEFAULT, pEntity, pEntity.getBoundingBox().inflate(12.0D, 6.0D, 12.0D));
		Optional<BloodDrunkPuppeteerEntity> owner = owners.stream()
				.filter(o -> o.getUUID().equals(pEntity.getOwnerUUID())).findFirst();

		if (owners != null) {
			if (owner.isPresent()) {
				pMatrixStack.pushPose();

				LivingEntity puppeteer = owner.get();
				float f = puppeteer.getAttackAnim(pPartialTicks);
				float f1 = Mth.sin(Mth.sqrt(f) * (float) Math.PI);
				float f2 = Mth.lerp(pPartialTicks, puppeteer.yRotO, puppeteer.getYRot()) * ((float) Math.PI / 180F);
				double d0 = 0;
				double d1 = 0;
				double d2 = 0;
				double d3 = 0.0D;
				double d4;
				double d5;
				double d6;
				float f3;
				d4 = Mth.lerp(pPartialTicks, puppeteer.xo, puppeteer.getX()) - d1 * d2 - d0 * 0.8D;
				d5 = puppeteer.yo + puppeteer.getEyeHeight() + (puppeteer.getY() - puppeteer.yo) * pPartialTicks
						- 0.45D;
				d6 = Mth.lerp(pPartialTicks, puppeteer.zo, puppeteer.getZ()) - d0 * d2 + d1 * 0.8D;
				f3 = puppeteer.isCrouching() ? -0.1875F : 0.0F;

				double d9 = Mth.lerp(pPartialTicks, pEntity.xo, pEntity.getX());
				double d10 = Mth.lerp(pPartialTicks, pEntity.yo, pEntity.getY()) + 0.25D;
				double d8 = Mth.lerp(pPartialTicks, pEntity.zo, pEntity.getZ());
				float f4 = (float) (d4 - d9);
				float f5 = (float) (d5 - d10) + f3;
				float f6 = (float) (d6 - d8);
				VertexConsumer vertexconsumer1 = pBuffer.getBuffer(RenderType.lineStrip());
				PoseStack.Pose posestack$pose1 = pMatrixStack.last();
				int j = 16;
				Quaternion alpha = Vector3.YP.rotationDegrees(puppeteer.level().getGameTime());
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

}
