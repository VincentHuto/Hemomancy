package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielModel;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * The fire on the body: {@code putriciel_embers.png} (the eschar seams, the sacs' splits, the censers' insides, the
 * palm and its nodules, in crimson, pink-white and soot only) as an emissive pass. It returns before drawing in a rot
 * phase, so no fire texel ever shows outside the cleansing interval, the Embering's first ticks and the ending.
 */
public class PutricielEmberLayer<T extends PutricielEntity> extends RenderLayer<T, PutricielModel<T>> {
	private static final RenderType EMBERS = RenderType.eyes(Hemomancy.rloc("textures/entity/boss/putriciel/putriciel_embers.png"));

	public PutricielEmberLayer(RenderLayerParent<T, PutricielModel<T>> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T entity, float limbSwing, float limbAmount,
			float partialTick, float age, float yaw, float pitch) {
		if (entity.isInvisible()) return;
		var motion = PutricielAnimState.of(entity);
		boolean ending = motion.endingTime() >= 0.0F;
		if (!ending && motion.stage() == PutricielStage.ROT) return;
		float ember = motion.look().ember();
		if (ember <= 0.01F) return;
		float flicker = .85F + .15F * Mth.sin(age * .7F + entity.getId());
		// eyes is additive (ONE, ONE) and ignores alpha: the strength goes in the colour
		int level = (int) (Math.min(1.0F, ember * flicker) * 255);
		int color = FastColor.ARGB32.color(255, level, level, level);
		getParentModel().renderToBuffer(poseStack, buffer.getBuffer(EMBERS), 0x00F000F0, OverlayTexture.NO_OVERLAY, color);
	}
}
