package com.vincenthuto.hemomancy.client.render.world;

final class QliphothBloomRenderRules {
    static final double MAX_RENDER_DISTANCE = 256.0;
    // ponytail: hard distance LOD; add gradual detail tiers only if the 48-block cutoff is visibly abrupt.
    static final double DYNAMIC_EFFECT_DISTANCE = 48.0;

    private QliphothBloomRenderRules() { }

    static boolean shouldRender(double distanceSquared, boolean inFrustum) {
        return inFrustum && distanceSquared <= MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE;
    }

    static boolean shouldRenderDynamicEffects(double distanceSquared) {
        return distanceSquared <= DYNAMIC_EFFECT_DISTANCE * DYNAMIC_EFFECT_DISTANCE;
    }
}
