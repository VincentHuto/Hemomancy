package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.vincenthuto.hemomancy.common.worldgen.arbor.ArborOfWillLayout;
import com.vincenthuto.hemomancy.common.worldgen.arbor.ArborOfWillVisualRules;

record ArborFruitMeshKey(String family, ArborOfWillVisualRules.GrowthState state, int level, int maxLevel) {
    static ArborFruitMeshKey create(String family, ArborOfWillVisualRules.GrowthState state,
                                    int level, int maxLevel) {
        String normalizedFamily = ArborOfWillLayout.orderedFamilies().contains(family) ? family : "core";
        int normalizedMax = Math.max(1, maxLevel);
        int normalizedLevel = Math.max(1, Math.min(normalizedMax, level));
        return new ArborFruitMeshKey(normalizedFamily, state, normalizedLevel, normalizedMax);
    }
}
