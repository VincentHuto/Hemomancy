package com.vincenthuto.hemomancy.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hutoslib.HutosLib;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class BloodArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

	public static final ResourceLocation fallback = new ResourceLocation(HutosLib.MOD_ID,
			"textures/entity/arm_banner/arm_banner.png");

	public BloodArmorLayer(LivingEntityRenderer<T, M> owner) {
		super(owner);
	}

	@Override
	public void render(PoseStack matrixStack, MultiBufferSource buffer, int lightness, T ent, float limbSwing,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

		if (ent instanceof Player player) {
		}
	}

	@SuppressWarnings("unused")
	private void translateToBody(PoseStack matrixStack) {
		this.getParentModel().leftArm.translateAndRotate(matrixStack);
	}

}
