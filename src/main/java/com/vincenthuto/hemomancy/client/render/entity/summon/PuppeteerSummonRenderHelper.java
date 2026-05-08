package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.world.entity.LivingEntity;

final class PuppeteerSummonRenderHelper {
	private PuppeteerSummonRenderHelper() {
	}

	static boolean shouldSkipRender(LivingEntity entity) {
		if (!(entity instanceof BoundPuppeteerSummon summon)) {
			return false;
		}
		return !PuppeteerSummonRules.shouldRenderDismissingSummon(
				entity.tickCount, summon.hemomancy$getDismissalTicks());
	}

	static void applyDismissalScale(LivingEntity entity, float partialTicks, PoseStack poseStack) {
		if (!(entity instanceof BoundPuppeteerSummon summon)) {
			return;
		}
		int dismissalTicks = summon.hemomancy$getDismissalTicks();
		if (dismissalTicks <= 0) {
			return;
		}
		float fade = (float) PuppeteerSummonRules.dismissalAlpha(dismissalTicks, partialTicks);
		float scale = 0.65F + fade * 0.35F;
		poseStack.translate(0.0F, entity.getBbHeight() * (1.0F - scale) * 0.35F, 0.0F);
		poseStack.scale(scale, scale, scale);
	}
}
