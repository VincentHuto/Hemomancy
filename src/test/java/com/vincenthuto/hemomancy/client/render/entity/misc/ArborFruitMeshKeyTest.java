package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.vincenthuto.hemomancy.common.worldgen.arbor.ArborOfWillVisualRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

class ArborFruitMeshKeyTest {
    @Test
    void clampsLevelsAndNormalizesUnknownFamilies() {
        ArborFruitMeshKey key = ArborFruitMeshKey.create("unknown",
                ArborOfWillVisualRules.GrowthState.RIPE_FRUIT, 9, 3);

        assertEquals("core", key.family());
        assertEquals(3, key.level());
        assertEquals(3, key.maxLevel());
    }

    @Test
    void stateAndLevelSelectDifferentTemplates() {
        ArborFruitMeshKey ripe = ArborFruitMeshKey.create("mycelial",
                ArborOfWillVisualRules.GrowthState.RIPE_FRUIT, 1, 3);

        assertNotEquals(ripe, ArborFruitMeshKey.create("mycelial",
                ArborOfWillVisualRules.GrowthState.CLOSED_CALYX, 1, 3));
        assertNotEquals(ripe, ArborFruitMeshKey.create("mycelial",
                ArborOfWillVisualRules.GrowthState.RIPE_FRUIT, 2, 3));
    }

    @Test
    void everyFamilyAndGrowthStateHasAStableTemplateSelection() {
        Set<ArborFruitMeshKey> keys = new HashSet<>();
        for (String family : com.vincenthuto.hemomancy.common.worldgen.arbor.ArborOfWillLayout.orderedFamilies()) {
            for (ArborOfWillVisualRules.GrowthState state : ArborOfWillVisualRules.GrowthState.values()) {
                ArborFruitMeshKey first = ArborFruitMeshKey.create(family, state, 2, 3);
                ArborFruitMeshKey second = ArborFruitMeshKey.create(family, state, 2, 3);
                assertEquals(first, second);
                assertTrue(keys.add(first), "family/state pair must choose a unique template key");
            }
        }
        assertEquals(24, keys.size());
    }
}
