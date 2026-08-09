package com.vincenthuto.hemomancy.client.render.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.item.hematic.LivingSickleItemRenderer;
import com.vincenthuto.hemomancy.common.entity.projectile.LivingSickleHookEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class LivingSickleHookRenderer extends EntityRenderer<LivingSickleHookEntity> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_living_sickle.png");
	private static final ItemStack SICKLE = new ItemStack(ItemInit.living_sickle.get());

	public LivingSickleHookRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(LivingSickleHookEntity entity, float yaw, float partialTick, PoseStack poseStack,
			MultiBufferSource buffers, int light) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
		poseStack.mulPose(Axis.XP.rotationDegrees((entity.tickCount + partialTick) * 55.0F));
		poseStack.scale(0.9F, 0.9F, 0.9F);
		LivingSickleItemRenderer.renderModel(SICKLE, ItemDisplayContext.FIXED, poseStack, buffers, light, entity.getId());
		poseStack.popPose();
		super.render(entity, yaw, partialTick, poseStack, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(LivingSickleHookEntity entity) {
		return TEXTURE;
	}
}
