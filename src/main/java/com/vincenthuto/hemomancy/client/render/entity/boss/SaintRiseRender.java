package com.vincenthuto.hemomancy.client.render.entity.boss;

import net.minecraft.world.entity.Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.entity.boss.saint.RisingSaint;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;

/**
 * Sinks a rising Saint's body under the floor by {@link SaintRise#depth}. Called first in a Saint renderer's
 * {@code scale}, where the pose is already flipped, so down is +Y; the floor's depth hides what is still below it.
 */
public final class SaintRiseRender {
	private SaintRiseRender() {
	}

	public static <T extends Entity & RisingSaint> void sink(PoseStack poseStack, T saint, float partialTick) {
		float depth = SaintRise.depth(saint.riseTime(partialTick), saint.getBbHeight());
		if (depth > 0.0F) poseStack.translate(0.0F, depth, 0.0F);
	}
}
