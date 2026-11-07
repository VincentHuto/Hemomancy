package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathModel;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathVisualRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

/**
 * Hemorath's vessels lit from within: the glow mask (only vessel core lines and spike tips are in it) drawn through one
 * static {@link RenderType#eyes} constant, at 0.15 hollow rising to 0.75 full, lifted to 0.9 while Gorged or Strained,
 * with a heartbeat that quickens as he fills. He glows only when fed.
 */
public class HemorathVesselLayer<T extends HemorathEntity> extends RenderLayer<T, HemorathModel<T>> {
	private static final ResourceLocation GLOW_TEXTURE = Hemomancy.rloc("textures/entity/boss/hemorath/hemorath_overload.png");
	private static final RenderType GLOW = RenderType.eyes(GLOW_TEXTURE);
	private static final int FULL_BRIGHT = 0x00F000F0;
	/** The heartbeat's share of the glow. */
	private static final float BEAT_SHARE = 0.25F;

	public HemorathVesselLayer(RenderLayerParent<T, HemorathModel<T>> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing,
			float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInvisible()) return;
		float swell = HemorathAnimState.of(entity).swell();
		boolean feeding = HemorathOpenings.feeds(entity.syncedOpening()) || entity.thirsting();
		float beat = HemorathVisualRules.heartbeat(ageInTicks, swell);
		float alpha = Math.min(1.0F, HemorathVisualRules.glow(swell, feeding) * (1.0F - BEAT_SHARE + BEAT_SHARE * beat * 2.0F));
		int color = FastColor.ARGB32.color((int) (alpha * 255), 255, (int) (200 + beat * 40), 196);
		getParentModel().renderToBuffer(poseStack, buffer.getBuffer(GLOW), FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
	}
}
