package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.armor.SilentArchonArmorModel;
import com.vincenthuto.hemomancy.client.render.armor.ArmorItemDisplayTransformHelper;
import com.vincenthuto.hemomancy.client.render.armor.ArmorItemModelPoseHelper;
import com.vincenthuto.hemomancy.client.render.armor.SilentArchonArmorRenderHelper;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SilentArchonArmorItemRenderer extends BlockEntityWithoutLevelRenderer {
	public SilentArchonArmorItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		EquipmentSlot slot = SilentArchonArmorRenderHelper.slotFor(stack);
		if (slot == null) {
			return;
		}

		boolean gui = displayContext == ItemDisplayContext.GUI;
		if (gui) {
			Lighting.setupForEntityInInventory();
		}

		poseStack.pushPose();
		ArmorItemDisplayTransformHelper.apply(displayContext, slot, poseStack);
		SilentArchonArmorModel<LivingEntity> model = SilentArchonArmorRenderHelper.modelForSlot(slot);
		ArmorItemModelPoseHelper.reset(model);
		model.setAllVisible(true);

		VertexConsumer base = buffer.getBuffer(RenderType.entityTranslucent(
				SilentArchonArmorRenderHelper.textureForSlot(slot)));
		model.renderToBuffer(poseStack, base, packedLight, OverlayTexture.NO_OVERLAY, -1);

		VertexConsumer overlay = buffer.getBuffer(SilentArchonArmorRenderHelper.overlayRenderType(stack, slot, gui));
		model.renderToBuffer(poseStack, overlay, packedLight, OverlayTexture.NO_OVERLAY,
				SilentArchonArmorRenderHelper.OVERLAY_COLOR);
		poseStack.popPose();

		if (gui) {
			Lighting.setupFor3DItems();
		}
	}
}
