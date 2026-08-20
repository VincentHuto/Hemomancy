package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.armor.EdaciousBloodLustArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.BloodLustArmorModel;
import com.vincenthuto.hemomancy.client.render.armor.ArmorItemDisplayTransformHelper;
import com.vincenthuto.hemomancy.client.render.armor.ArmorItemModelPoseHelper;
import com.vincenthuto.hemomancy.client.render.armor.ModelBackedArmorItemRenderHelper;
import com.vincenthuto.hemomancy.client.render.armor.ModelBackedArmorItemRenderHelper.ArmorItemRenderDefinition;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.BloodLustArmorItem;

import net.minecraft.client.model.HumanoidModel;
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

public class ModelBackedArmorItemRenderer extends BlockEntityWithoutLevelRenderer {
	public ModelBackedArmorItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		ArmorItemRenderDefinition definition = ModelBackedArmorItemRenderHelper.definitionFor(stack);
		if (definition == null) {
			return;
		}

		boolean gui = displayContext == ItemDisplayContext.GUI;
		if (gui) {
			Lighting.setupForEntityInInventory();
		}

		poseStack.pushPose();
		ArmorItemDisplayTransformHelper.apply(displayContext, definition.slot(), poseStack);
		applyManualGuiTransforms(stack, displayContext, poseStack);
		HumanoidModel<LivingEntity> model = definition.model().get();
		ArmorItemModelPoseHelper.reset(model);
		if (model instanceof BloodLustArmorModel<?> bloodLustModel) {
			bloodLustModel.setLineage(BloodLustArmorItem.getLineage(stack));
		}
		applySlotVisibility(model, definition.slot());

		VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(definition.texture()));
		if (model instanceof EdaciousBloodLustArmorModel<?> edaciousModel) {
			edaciousModel.setRenderWingsInMainPass(false);
		}
		model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
		if (model instanceof EdaciousBloodLustArmorModel<?> edaciousModel) {
			edaciousModel.setRenderWingsInMainPass(true);
		}
		poseStack.popPose();

		if (gui) {
			Lighting.setupFor3DItems();
		}
	}

	private static void applySlotVisibility(HumanoidModel<?> model, EquipmentSlot slot) {
		model.setAllVisible(false);
		switch (slot) {
			case HEAD -> {
				model.head.visible = true;
				model.hat.visible = true;
			}
			case CHEST -> {
				model.body.visible = true;
				model.rightArm.visible = true;
				model.leftArm.visible = true;
			}
			case LEGS -> {
				model.rightLeg.visible = true;
				model.leftLeg.visible = true;
			}
			case FEET -> {
				model.rightLeg.visible = true;
				model.leftLeg.visible = true;
			}
			default -> model.setAllVisible(true);
		}
	}
	private static void applyManualFirstPersonTransforms(ItemStack stack, ItemDisplayContext displayContext,
	                                             PoseStack poseStack) {
		if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND  ) {
		}
	}

	private static void applyManualGuiTransforms(ItemStack stack, ItemDisplayContext displayContext,
			PoseStack poseStack) {
		if (displayContext == ItemDisplayContext.GUI ){
			if( stack.is(ItemInit.vestment_of_the_final_molt.get())) {
				poseStack.translate(0.0F, 0F, 0.0F);
				poseStack.scale(0.6F, 0.6F, 0.6F);
			}
			if( stack.is(ItemInit.covenant_mantle.get())) {
				poseStack.translate(0.0F, 0F, 0.0F);
				poseStack.scale(0.78F, 0.78F, 0.78F);
			}
			if( stack.is(ItemInit.phantasmal_blood_lust_helm.get())) {
				poseStack.translate(0.0F, 0.02F, 0.0F);
				poseStack.scale(0.7F, 0.7F, 0.7F);
			}
		}
	}
}
