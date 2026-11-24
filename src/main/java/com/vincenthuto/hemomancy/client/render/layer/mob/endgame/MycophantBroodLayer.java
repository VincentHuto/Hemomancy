package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.MycophantModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.FunglingModel;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Draws the Fungling in the grasping hand during the waking. */
public final class MycophantBroodLayer extends RenderLayer<MycophantEntity, MycophantModel> {
	private static final RenderType FLESH = RenderType.entityCutoutNoCull(
			Hemomancy.rloc("textures/entity/fungling/model_fungling.png"));
	private final ModelPart fungling;

	public MycophantBroodLayer(RenderLayerParent<MycophantEntity, MycophantModel> parent, Context context) {
		super(parent);
		fungling = context.bakeLayer(FunglingModel.LAYER_LOCATION);
	}

	@Override
	public void render(PoseStack poses, MultiBufferSource buffers, int light, MycophantEntity entity, float limbSwing,
			float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		getParentModel().renderBrood(poses, fungling, buffers.getBuffer(FLESH), light, OverlayTexture.NO_OVERLAY);
	}
}
