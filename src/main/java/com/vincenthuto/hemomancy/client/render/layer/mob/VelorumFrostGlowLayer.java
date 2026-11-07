package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumModel;
import com.vincenthuto.hemomancy.client.render.world.VelorumDarkSight;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEndingRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumOpeningRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumVisualRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * The ice cores, claw tips, breast seam and the hairline frost in the mantle's folds, on the eyes render type over the
 * glow atlas. Its strength reads the lantern (the Guttering Lantern drops the crystals to a quarter, the Endless Hour
 * to the core), the Bow (the seam lit, shown only then) and the ending (fading to 0.15 as the frost falls out of the
 * light).
 * <p>
 * Revision 2026-11-06: the glow atlas carries his cruor parts at a dull red (pre-scaled so they read at about 0.15 at
 * rest) beside the lantern crystals' frost light; the Eclipse puts that whole pass out with his light, and a second pass
 * over the cruor-only atlas ({@code velorum_cruor_glow.png}) brings the cruor up to 0.8 as the room goes dark.
 */
public class VelorumFrostGlowLayer<T extends VelorumEntity> extends RenderLayer<T, VelorumModel<T>> {
	private static final RenderType GLOW = RenderType.eyes(Hemomancy.rloc("textures/entity/boss/velorum/velorum_glow.png"));
	/** The cruor alone at its full glow, for the Eclipse. */
	private static final RenderType CRUOR_GLOW = RenderType.eyes(Hemomancy.rloc("textures/entity/boss/velorum/velorum_cruor_glow.png"));
	private static final float REST = 0.72F;
	private static final float PULSE = 0.12F;
	private static final float GUTTERED = 0.25F;
	private static final float HOUR = 0.45F;

	public VelorumFrostGlowLayer(RenderLayerParent<T, VelorumModel<T>> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T entity, float limbSwing,
			float limbAmount, float partialTick, float age, float yaw, float pitch) {
		if (entity.isInvisible()) return;
		VelorumAnimState.Frame frame = VelorumAnimState.of(entity).frame();
		float strength = REST + (Mth.sin(age * 0.055F) + 1.0F) * 0.5F * PULSE;
		boolean bow = frame.opening() == VelorumOpeningRules.BOW_ID;
		if (frame.opening() == VelorumOpeningRules.GUTTER_ID) strength *= HemoEasing.lerp(1.0F, GUTTERED, frame.openingWeight());
		if (frame.hour()) strength *= HemoEasing.lerp(1.0F, HOUR, frame.moveWeight());
		if (frame.ending() >= 0.0F) {
			float fade = HemoEasing.ramp(frame.ending(), VelorumEndingRules.HOOD_BOWED, VelorumEndingRules.FROST_FALLS);
			strength = HemoEasing.lerp(strength, VelorumEndingRules.FAILED_GLOW, fade);
		}
		// the eyes render type is additive and ignores alpha: the strength scales the colour. The Eclipse puts the lantern's
		// light out, and the cruor-only pass rises to the cruor's Eclipse glow as it does
		float eclipse = VelorumDarkSight.eclipse(entity, partialTick);
		float s = Mth.clamp(strength * (1.0F - eclipse), 0.0F, 1.0F);
		int color = FastColor.ARGB32.color(255, (int) (205 * s), (int) (225 * s), (int) (255 * s));
		VelorumModel<T> model = getParentModel();
		model.glowSeam(bow);
		if (s > 0.004F) model.renderToBuffer(poseStack, buffer.getBuffer(GLOW), 0x00F000F0, OverlayTexture.NO_OVERLAY, color);
		model.glowSeam(true);
		if (eclipse > 0.004F) {
			float cruor = Mth.clamp(eclipse * VelorumVisualRules.cruorGlow(1.0F), 0.0F, 1.0F);
			int dull = FastColor.ARGB32.color(255, (int) (255 * cruor), (int) (255 * cruor), (int) (255 * cruor));
			model.renderToBuffer(poseStack, buffer.getBuffer(CRUOR_GLOW), 0x00F000F0, OverlayTexture.NO_OVERLAY, dull);
		}
	}
}
