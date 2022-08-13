package com.vincenthuto.hemomancy.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.capa.player.charm.CharmFinder;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.item.VasculariumCharmItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class VascCharmLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {


	public VascCharmLayer(LivingEntityRenderer<T, M> owner) {
		super(owner);
	}

	private void translateToBody(PoseStack matrixStack) {
		this.getParentModel().body.translateAndRotate(matrixStack);
	}

	@Override
	public void render(PoseStack matrixStack, MultiBufferSource buffer, int lightness, T ent, float limbSwing,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (ent instanceof Player player) {
			Inventory inv = player.getInventory();
			ItemStack chest = inv.getArmor(EquipmentSlot.CHEST.getIndex());
			boolean scaleFlag = chest == ItemStack.EMPTY ? false : true;
			CharmFinder.findCharm(player, true).ifPresent((getter) -> {
				ItemStack charm = getter.getCharm();
				if (charm.getItem() instanceof VasculariumCharmItem type) {
					matrixStack.pushPose();
					matrixStack.mulPose(Vector3f.XN.rotationDegrees(180f));
					matrixStack.scale(0.5f, 0.5f, 0.5f);
					matrixStack.translate(0, -.5, 0.25);
					this.getParentModel().body.translateAndRotate(matrixStack);
					Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(ItemInit.charm_of_vascularium.get()),
							TransformType.FIXED, lightness, OverlayTexture.NO_OVERLAY, matrixStack, buffer, lightness);
					matrixStack.popPose();

				}
			});
		}
	}

}
