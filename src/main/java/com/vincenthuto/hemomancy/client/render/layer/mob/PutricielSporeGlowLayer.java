package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielModel;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * The fitted mask that lights the spore sacs in cream and the gills in mould green, leaving the vestments and shelves
 * dark. Cream and green only; it fades out as the char rises, so the burned growths never glow.
 */
public class PutricielSporeGlowLayer<T extends PutricielEntity> extends RenderLayer<T, PutricielModel<T>> {
	private static final RenderType GLOW = RenderType.eyes(Hemomancy.rloc("textures/entity/boss/putriciel/putriciel_glow.png"));

	public PutricielSporeGlowLayer(RenderLayerParent<T, PutricielModel<T>> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T entity, float limbSwing, float limbAmount,
			float partialTick, float age, float yaw, float pitch) {
		if (entity.isInvisible()) return;
		var motion = PutricielAnimState.of(entity);
		float clear = 1.0F - motion.look().charAlpha();
		if (clear <= 0.02F) return;
		float pulse = (Mth.sin(age * .11F) + 1) * .5F;
		float tremor = motion.stage().fire() ? 0.0F : motion.kindling() * .25F;
		// eyes is additive (ONE, ONE) and ignores alpha: the strength goes in the colour, as every other glow layer does
		int level = (int) (Math.min(255.0F, 90 + pulse * 40 + tremor * 120) * clear);
		int color = FastColor.ARGB32.color(255, level, level, level);
		getParentModel().renderToBuffer(poseStack, buffer.getBuffer(GLOW), 0x00F000F0, OverlayTexture.NO_OVERLAY, color);
	}
}
