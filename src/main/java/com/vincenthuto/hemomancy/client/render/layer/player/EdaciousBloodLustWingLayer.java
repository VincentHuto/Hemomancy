package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.EdaciousBloodLustArmorModel;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class EdaciousBloodLustWingLayer<T extends LivingEntity, M extends HumanoidModel<T>>
		extends RenderLayer<T, M> {
	private static final ResourceLocation TEXTURE = Hemomancy
			.rloc("textures/models/armor/edacious_blood_lust_layer_1.png");

	public EdaciousBloodLustWingLayer(LivingEntityRenderer<T, M> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
			float netHeadYaw, float headPitch) {
		ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
		if (!chest.is(ItemInit.edacious_blood_lust_chest.get())) {
			return;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		EdaciousBloodLustArmorModel<T> model = (EdaciousBloodLustArmorModel) EdaciousBloodLustArmorModel.chest.get();
		this.getParentModel().copyPropertiesTo(model);
		model.animateArmorDetails(entity, ageInTicks);
		model.setRenderWingsInMainPass(true);

		VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
		model.renderWingsToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
	}
}
