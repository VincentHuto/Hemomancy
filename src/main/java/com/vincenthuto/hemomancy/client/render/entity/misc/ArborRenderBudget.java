package com.vincenthuto.hemomancy.client.render.entity.misc;

/** Geometry budgets kept separate from OpenGL code so regressions remain testable headlessly. */
final class ArborRenderBudget {
    static final int LEAF_RIBBON_SEGMENTS = 4;
    static final int LEAF_RIBBON_PLANES = 2;

    private ArborRenderBudget() { }

    static int leafBladeVertices() {
        return LEAF_RIBBON_SEGMENTS * LEAF_RIBBON_PLANES * 4;
    }

    static int crownBlades(float foliage) {
        float clamped = Math.max(0.0F, Math.min(1.0F, foliage));
        return 4 + Math.round(clamped * 20.0F);
    }
}
