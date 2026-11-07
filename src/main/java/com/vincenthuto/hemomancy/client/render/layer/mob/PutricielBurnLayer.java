package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielFireAnchors;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielModel;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * What burns under the body fire (design revision R1.7): {@code putriciel_burning.png} (the shelf and cap rims
 * crimson with pink-white flecks, the char's alligator cracks glowing on the caps, the staff wraps in crimson streaks
 * with hot cores and the iron heated either side of them, the censer lid bars' inner faces, the crown spires' tops,
 * the hem shelf rims, the robe's lower edge and the sac tears; crimson, pink-white and soot only) as an emissive pass.
 * The model draws once per region with the other regions skipped, each at its region's fire from
 * {@link PutricielBodyFireRules#region}, so the model's own burn climbs the staff and spreads across the back with the
 * flames. It returns before drawing in a rot phase, draws in IGNITING and BURNING and the Embering only under its tick
 * 30, and in the ending's fire beats.
 */
public class PutricielBurnLayer<T extends PutricielEntity> extends RenderLayer<T, PutricielModel<T>> {
	private static final RenderType BURNING = RenderType.eyes(Hemomancy.rloc("textures/entity/boss/putriciel/putriciel_burning.png"));
	private static final PutricielFireAnchors.Region[] REGIONS = PutricielFireAnchors.Region.values();

	private final float[] levels = new float[REGIONS.length];

	public PutricielBurnLayer(RenderLayerParent<T, PutricielModel<T>> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T entity, float limbSwing, float limbAmount,
			float partialTick, float age, float yaw, float pitch) {
		if (entity.isInvisible()) return;
		var motion = PutricielAnimState.of(entity);
		boolean ending = motion.endingTime() >= 0.0F;
		if (!ending && motion.stage() == PutricielStage.ROT) return;
		var moment = motion.fireMoment();
		if (!PutricielBodyFireRules.drawsLayer(moment)) return;
		boolean any = false;
		for (PutricielFireAnchors.Region region : REGIONS) {
			// eyes is additive (ONE, ONE) and ignores alpha: the strength goes in the colour, with each region's flicker
			float flicker = PutricielBodyFireRules.flicker(age, entity.getId() * 7 + region.ordinal());
			float level = Math.min(1.0F, PutricielBodyFireRules.region(region.rules(), moment) * flicker);
			// the Embering's embers glow low and crimson: at full strength the pink-white rims over the regrowing pale
			// parts read as pink flesh rather than embers (revision polish)
			if (!ending && motion.stage() == PutricielStage.EMBERING) level *= 0.55F * (1.0F - HemoEasing.ramp(moment.stageTime(), 6.0F, 24.0F));
			levels[region.ordinal()] = level;
			any |= level > 0.01F;
		}
		if (!any) return;
		getParentModel().renderBurning(poseStack, buffer.getBuffer(BURNING), 0x00F000F0, OverlayTexture.NO_OVERLAY, levels, motion);
	}
}
