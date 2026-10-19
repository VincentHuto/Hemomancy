package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperMountModel;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** The mount's veined wing membranes, drawn translucent so light shows through them. */
public final class VesperMountWingMembraneLayer
		extends RenderLayer<VesperTheCrownedRefusalEntity, VesperMountModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/boss/endgame/vesper_mount.png");

	public VesperMountWingMembraneLayer(RenderLayerParent<VesperTheCrownedRefusalEntity, VesperMountModel> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			VesperTheCrownedRefusalEntity entity, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInvisible() || !entity.hasWingsGrown()) return;
		int alpha = Mth.clamp(Math.round(255 * getParentModel().mountOpacity()), 0, 255);
		getParentModel().renderMembranes(poseStack, buffer.getBuffer(RenderType.entityTranslucent(TEXTURE)),
				packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), (alpha << 24) | 0xFFFFFF);
	}
}
