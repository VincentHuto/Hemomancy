package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import net.minecraft.util.Mth;

final class BombardierAnimationBlend {
    static final float TRANSITION_TICKS = 6.0F;

    private BombardierAnimationBlend() {}

    static float incomingWeight(float elapsedTicks) {
        float progress = Mth.clamp(elapsedTicks / TRANSITION_TICKS, 0.0F, 1.0F);
        return progress * progress * (3.0F - 2.0F * progress);
    }

    static float outgoingWeight(float elapsedTicks) {
        return 1.0F - incomingWeight(elapsedTicks);
    }
}
