package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeModel;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeDeeds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEndingRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeMove;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * Her white vessel light: the vessel mask at full brightness, breathing on a 120-tick cycle. During a tell the light
 * surges through one region: both arms fill white shoulder to fingertip through the reach's wind-up (hidden under
 * Unwatched Hands), her chest opens for the Eye, her whole body in the Convulsion's stutters. It retreats to nothing
 * as she is contained. White only: gold belongs to {@link SeraphaeNerveLayer}.
 */
public class SeraphaeVesselLayer<T extends SeraphaeEntity> extends RenderLayer<T, SeraphaeModel<T>> {
	private static final ResourceLocation VESSELS = Hemomancy.rloc("textures/entity/boss/seraphae/seraphae_vessels.png");
	private static final RenderType GLOW = RenderType.eyes(VESSELS);
	private static final int FULL_BRIGHT = 0x00F000F0;
	public static final float BASE = 0.55F;
	public static final float BREATH = 0.08F;
	public static final float BREATH_CYCLE = 120.0F;
	public static final float TELL = 0.45F;

	public SeraphaeVesselLayer(RenderLayerParent<T, SeraphaeModel<T>> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInvisible()) return;
		float light = BASE + BREATH * Mth.sin(ageInTicks * Mth.TWO_PI / BREATH_CYCLE);
		float ending = entity.view().endingTime(partialTicks);
		if (ending >= 0.0F) light *= SeraphaeEndingRules.light(ending);
		if (light <= 0.01F) return;
		VertexConsumer vertices = buffer.getBuffer(GLOW);
		getParentModel().renderToBuffer(poseStack, vertices, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, white(light));
		SeraphaeMove move = SeraphaeMove.byId(entity.view().moveId());
		float moveTime = entity.view().moveTime(partialTicks);
		if (move == null || moveTime < 0.0F) return;
		var inputs = entity.view().inputs();
		BossMoveTimeline timeline = move.timeline(inputs);
		float windup = HemoEasing.smoothstep(HemoEasing.clamp01(moveTime / Math.max(1, timeline.windup())));
		boolean winding = moveTime < timeline.windup();
		switch (move) {
			case LAYING_ON_OF_HANDS, STRAINING_REACH -> {
				if (winding && !SeraphaeDeeds.unwatchedHands(inputs.deedMask())) {
					getParentModel().renderRegion(SeraphaeModel.Region.ARMS, poseStack, vertices, FULL_BRIGHT,
							OverlayTexture.NO_OVERLAY, white(TELL * windup));
				}
			}
			case UNCLOSING_EYE -> {
				float active = moveTime < timeline.windup() + timeline.active() ? 1.0F : 0.4F;
				getParentModel().renderRegion(SeraphaeModel.Region.CHEST, poseStack, vertices, FULL_BRIGHT,
						OverlayTexture.NO_OVERLAY, white(TELL * windup * active));
			}
			case CONVULSION, CONVULSION_WIDE -> {
				if (winding) {
					getParentModel().renderRegion(SeraphaeModel.Region.BODY, poseStack, vertices, FULL_BRIGHT,
							OverlayTexture.NO_OVERLAY, white(TELL * 0.5F * windup));
				}
			}
			default -> {
			}
		}
	}

	/** The eyes render type adds light, so strength rides on the colour, not the alpha. */
	private static int white(float strength) {
		int value = Mth.clamp((int) (strength * 255.0F), 0, 255);
		return FastColor.ARGB32.color(255, value, value, value);
	}
}
