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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/** Her eye pits, the pulp's eyes, sac cavities, egg pockets and vent mouths, brightening with her glow level. */
public class MycophantGlowLayer extends RenderLayer<MycophantEntity, MycophantModel> {
	private static final RenderType GLOW = RenderType.eyes(Hemomancy.rloc("textures/entity/boss/endgame/mycophant_glow.png"));

	public MycophantGlowLayer(RenderLayerParent<MycophantEntity, MycophantModel> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, MycophantEntity entity, float limbSwing,
			float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		float pulse = 0.85F + Mth.sin(ageInTicks * 0.16F) * 0.15F;
		float level = Mth.clamp(MycophantAnimState.of(entity).glow(partialTick) * pulse, 0.15F, 1.0F);
		int color = FastColor.ARGB32.colorFromFloat(1.0F, level, level, level);
		this.getParentModel().renderToBuffer(poseStack, buffer.getBuffer(GLOW), 15728640, OverlayTexture.NO_OVERLAY, color);
	}
}
