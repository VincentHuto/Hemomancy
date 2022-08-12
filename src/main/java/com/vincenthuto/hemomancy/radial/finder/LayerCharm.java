package com.vincenthuto.hemomancy.radial.finder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.item.ItemVasculariumCharm;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LayerCharm<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {


	public LayerCharm(LivingEntityRenderer<T, M> owner) {
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
				if (charm.getItem() instanceof ItemVasculariumCharm type) {
					matrixStack.pushPose();
					matrixStack.popPose();
				}
			});
		}
	}

}
