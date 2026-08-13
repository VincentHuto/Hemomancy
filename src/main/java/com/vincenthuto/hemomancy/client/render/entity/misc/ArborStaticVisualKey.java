package com.vincenthuto.hemomancy.client.render.entity.misc;

/** Stable inputs that change the Arbor's expensive, non-animated mesh. */
record ArborStaticVisualKey(int degree, int chamberRadius, int foliagePermille, int packedLight) {
    static ArborStaticVisualKey create(int degree, int chamberRadius, float foliage, int packedLight) {
        int clampedDegree = Math.max(0, Math.min(8, degree));
        float normalizedFoliage = clampedDegree >= 8 ? 1.0F : Math.max(0.0F, Math.min(1.0F, foliage));
        return new ArborStaticVisualKey(clampedDegree, Math.max(1, chamberRadius),
                Math.round(normalizedFoliage * 1000.0F), packedLight);
    }

    float foliage() {
        return foliagePermille / 1000.0F;
    }
}
