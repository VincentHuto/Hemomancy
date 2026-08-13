package com.vincenthuto.hemomancy.client.render.world;

final class QliphothBloomRenderRules {
    static final double MAX_RENDER_DISTANCE = 256.0;

    private QliphothBloomRenderRules() { }

    static boolean shouldRender(double distanceSquared, boolean inFrustum) {
        return inFrustum && distanceSquared <= MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE;
    }
}
