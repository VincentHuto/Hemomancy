package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.HematicCleatsModel;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.CleatsFitting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/** Draws fitted Hematic Cleats over the worn boots while they are on (CN-10, NQ-05-12). */
public class HematicCleatsLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/models/armor/hematic_cleats.png");

	public HematicCleatsLayer(RenderLayerParent<T, M> renderer) {
		super(renderer);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInvisible() || !CleatsFitting.isOn(entity.getItemBySlot(EquipmentSlot.FEET))) return;
		HematicCleatsModel<LivingEntity> model = HematicCleatsModel.spurs.get();
		getParentModel().copyPropertiesTo((HumanoidModel<T>) (HumanoidModel<?>) model);
		model.setAllVisible(false);
		model.rightLeg.visible = true;
		model.leftLeg.visible = true;
		model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.armorCutoutNoCull(TEXTURE)), packedLight,
				OverlayTexture.NO_OVERLAY, -1);
	}
}
