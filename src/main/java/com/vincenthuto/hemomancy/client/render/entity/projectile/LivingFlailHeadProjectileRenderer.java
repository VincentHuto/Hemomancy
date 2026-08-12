package com.vincenthuto.hemomancy.client.render.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.LivingFlailModel;
import com.vincenthuto.hemomancy.common.entity.projectile.LivingFlailHeadProjectileEntity;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class LivingFlailHeadProjectileRenderer extends EntityRenderer<LivingFlailHeadProjectileEntity> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_living_flail.png");
	private final LivingFlailModel<?> model;

	public LivingFlailHeadProjectileRenderer(EntityRendererProvider.Context context) {
		super(context);
		model = new LivingFlailModel<>(context.bakeLayer(LivingFlailModel.LAYER_LOCATION));
	}

	@Override
	public void render(LivingFlailHeadProjectileEntity entity, float yaw, float partialTick, PoseStack poseStack,
			MultiBufferSource buffers, int light) {
		Vec3 interpolated = entity.getPosition(partialTick);
		float scale = LivingFlailRules.visualScale(entity.getCharge());
		poseStack.pushPose();
		poseStack.translate(interpolated.x - entity.getX(), interpolated.y - entity.getY(),
				interpolated.z - entity.getZ());
		poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
		poseStack.mulPose(Axis.XP.rotationDegrees((entity.tickCount + partialTick) * (28.0F + 34.0F * entity.getCharge())));
		poseStack.mulPose(Axis.ZP.rotationDegrees((entity.tickCount + partialTick) * 17.0F));
		poseStack.scale(scale, scale, scale);
		VertexConsumer base = buffers.getBuffer(RenderType.entityTranslucent(TEXTURE));
		model.renderHead(poseStack, base, light, OverlayTexture.NO_OVERLAY, -1);
		VertexConsumer glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
		model.renderHead(poseStack, glint, light, OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();
		super.render(entity, yaw, partialTick, poseStack, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(LivingFlailHeadProjectileEntity entity) {
		return TEXTURE;
	}
}
