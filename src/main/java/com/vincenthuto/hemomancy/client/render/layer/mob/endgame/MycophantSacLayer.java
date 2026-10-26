package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.MycophantAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.MycophantModel;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.FastColor;

/** The great gestation sac's shell, drawn translucent and self-lit over the egg mass inside it. */
public class MycophantSacLayer extends RenderLayer<MycophantEntity, MycophantModel> {
	private static final RenderType SAC = RenderType.entityTranslucentEmissive(Hemomancy.rloc("textures/entity/boss/endgame/mycophant_sac.png"));

	public MycophantSacLayer(RenderLayerParent<MycophantEntity, MycophantModel> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, MycophantEntity entity, float limbSwing,
			float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		float level = Math.min(1.0F, 0.75F + MycophantAnimState.of(entity).glow(partialTick) * 0.25F);
		int color = FastColor.ARGB32.colorFromFloat(1.0F, level, level, level);
		this.getParentModel().renderSacShell(poseStack, buffer.getBuffer(SAC), packedLight,
				LivingEntityRenderer.getOverlayCoords(entity, 0.0F), color);
	}
}
