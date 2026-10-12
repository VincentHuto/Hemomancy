package com.vincenthuto.hemomancy.client.render.entity.misc;

/** Geometry budgets kept separate from OpenGL code so regressions remain testable headlessly. */
final class ArborRenderBudget {
    static final int LEAF_RIBBON_SEGMENTS = 4;
    static final int LEAF_RIBBON_PLANES = 2;

    // Roots, trunk, leaders and boughs read at close range; spurs and stems are only a few pixels wide.
    static final int MAJOR_TUBE_SIDES = 10;
    static final int MAJOR_TUBE_SUBDIVISIONS = 3;
    static final int TWIG_TUBE_SIDES = 6;
    static final int STEM_TUBE_SIDES = 5;
    static final int VEIN_TUBE_SIDES = 5;
    static final int STREAMER_TUBE_SIDES = 4;
    static final int MINOR_TUBE_SUBDIVISIONS = 2;

    // Fruit bodies are the focal read; leaf buds, bud lobes and calyx sepals sit inside other geometry.
    static final int FOCAL_ELLIPSOID_LATITUDES = 7;
    static final int FOCAL_ELLIPSOID_LONGITUDES = 12;
    static final int MINOR_ELLIPSOID_LATITUDES = 5;
    static final int MINOR_ELLIPSOID_LONGITUDES = 8;

    private ArborRenderBudget() { }

    static int leafBladeVertices() {
        return LEAF_RIBBON_SEGMENTS * LEAF_RIBBON_PLANES * 4;
    }

    static int crownBlades(float foliage) {
        float clamped = Math.max(0.0F, Math.min(1.0F, foliage));
        return 4 + Math.round(clamped * 20.0F);
    }
}
