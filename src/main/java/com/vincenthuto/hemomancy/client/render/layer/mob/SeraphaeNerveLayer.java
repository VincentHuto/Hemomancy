package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeModel;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEndingRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeNerveFlashRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * Her gold nerve light, drawn per nerve part with that part's own strength from {@link SeraphaeNerveFlashRules}: idle,
 * one seeded trunk fires every 70 to 110 ticks; at a strike's commit the striking trunk fires root to tip; in the
 * Convulsion every nerve fires at once in three stutters. The arms' nerve lines fire with the reach's commit and with
 * each pulse of her hold. No shader: each part is its own draw in its own strength. Gold only: the white vessel light
 * is {@link SeraphaeVesselLayer}'s.
 */
public class SeraphaeNerveLayer<T extends SeraphaeEntity> extends RenderLayer<T, SeraphaeModel<T>> {
	private static final ResourceLocation NERVES = Hemomancy.rloc("textures/entity/boss/seraphae/seraphae_nerves.png");
	private static final RenderType GLOW = RenderType.eyes(NERVES);
	private static final int FULL_BRIGHT = 0x00F000F0;
	/** A faint standing glow along every nerve, so the trunks read as nerves between flashes. */
	public static final float RESTING = 0.12F;

	public SeraphaeNerveLayer(RenderLayerParent<T, SeraphaeModel<T>> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInvisible()) return;
		float ending = entity.view().endingTime(partialTicks);
		float fade = ending < 0.0F ? 1.0F : endingFade(ending);
		if (fade <= 0.0F) return;
		SeraphaeMove move = SeraphaeMove.byId(entity.view().moveId());
		float moveTime = entity.view().moveTime(partialTicks);
		float stutter = 0.0F;
		float since = -1.0F;
		int firing = -1;
		float arms = 0.0F;
		if (move != null && moveTime >= 0.0F) {
			var inputs = entity.view().inputs();
			BossMoveTimeline timeline = move.timeline(inputs);
			float afterCommit = moveTime - timeline.windup();
			boolean left = inputs.variant() == 0;
			switch (move) {
				case CONVULSION, CONVULSION_WIDE -> stutter = SeraphaeNerveFlashRules.stutter(moveTime);
				case NERVE_LASH -> {
					firing = SeraphaeNerveFlashRules.trunk(left, 2);
					since = afterCommit + 2.0F;
				}
				case COUNTER_LASH -> {
					firing = SeraphaeNerveFlashRules.trunk(left, 0);
					since = afterCommit + 2.0F;
				}
				case AXON_LANCE, AXON_MIRROR -> {
					firing = SeraphaeNerveFlashRules.trunk(left, 1);
					since = afterCommit + 4.0F;
				}
				case LAYING_ON_OF_HANDS, STRAINING_REACH -> arms = pulse(afterCommit + 4.0F);
				default -> {
				}
			}
		}
		if (entity.syncedHoldPlayer() >= 0) arms = Math.max(arms, pulse(ageInTicks % 10.0F));
		int seed = entity.getId();
		final int fire = firing;
		final float sinceFire = since;
		final float all = stutter;
		VertexConsumer vertices = buffer.getBuffer(GLOW);
		getParentModel().renderNerves(poseStack, vertices, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, nerve -> {
			int trunk = SeraphaeNerveFlashRules.trunk(nerve.side() == 0, nerve.place());
			float light = SeraphaeNerveFlashRules.idle(seed, trunk, nerve.position(), ageInTicks);
			if (trunk == fire) light = Math.max(light, SeraphaeNerveFlashRules.front(sinceFire, nerve.position()));
			light = Math.max(light, all);
			return gold(Math.max(RESTING, light) * fade);
		});
		if (arms > 0.01F) {
			getParentModel().renderRegion(SeraphaeModel.Region.ARMS, poseStack, vertices, FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, gold(arms * fade));
		}
	}

	/** A short flash rising over 1 tick and gone 4 ticks after. */
	private static float pulse(float since) {
		if (since < 0.0F || since > SeraphaeNerveFlashRules.AFTERGLOW + 1.0F) return 0.0F;
		return since < 1.0F ? since : 1.0F - (since - 1.0F) / SeraphaeNerveFlashRules.AFTERGLOW;
	}

	/** Her gold stops one trunk at a time as she is contained, the last at tick 88. */
	private static float endingFade(float tick) {
		return Mth.clamp((SeraphaeEndingRules.LAST_FLASH - tick) / (SeraphaeEndingRules.LAST_FLASH - SeraphaeEndingRules.REEL_END),
				0.0F, 1.0F);
	}

	/** The mask is gold already: the colour only carries the strength (the eyes type adds light). */
	private static int gold(float strength) {
		int value = Mth.clamp((int) (strength * 255.0F), 0, 255);
		return value <= 0 ? 0 : FastColor.ARGB32.color(255, value, value, value);
	}
}
