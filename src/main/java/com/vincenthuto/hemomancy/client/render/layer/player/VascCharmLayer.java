package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.capability.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.VasculariumCharmItem;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class VascCharmLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

	public VascCharmLayer(LivingEntityRenderer<T, M> owner) {
		super(owner);
	}

	@Override
	public void render(PoseStack matrixStack, MultiBufferSource buffer, int lightness, T ent, float limbSwing,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (ent instanceof Player player) {
			player.getCapability(RunesCapabilities.RUNES).ifPresent(inv -> {
				if (inv.getStackInSlot(4).getItem() instanceof VasculariumCharmItem charm) {
					matrixStack.pushPose();
					matrixStack.mulPose(Vector3.XN.rotationDegrees(180f).toMoj());
					matrixStack.scale(0.25f, 0.25f, 0.25f);
					matrixStack.translate(0, -.45, 0.55);
					matrixStack.mulPose(Vector3.XN.rotationDegrees(7f).toMoj());
					this.getParentModel().body.translateAndRotate(matrixStack);
					Minecraft.getInstance().getItemRenderer().renderStatic(
							new ItemStack(ItemInit.charm_of_vascularium.get()), ItemDisplayContext.FIXED, lightness,
							OverlayTexture.NO_OVERLAY, matrixStack, buffer, ent.level(), lightness);
					matrixStack.popPose();
				}

			});
		}
	}

	private void translateToBody(PoseStack matrixStack) {
		this.getParentModel().body.translateAndRotate(matrixStack);
	}

}
