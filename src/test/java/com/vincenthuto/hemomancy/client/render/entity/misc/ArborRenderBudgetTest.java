package com.vincenthuto.hemomancy.client.render.entity.misc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArborRenderBudgetTest {
    @Test
    void crossedRibbonLeafUsesThirtyTwoVertices() {
        assertEquals(32, ArborRenderBudget.leafBladeVertices());
        assertTrue(ArborRenderBudget.leafBladeVertices() * 6 < 224,
                "replacement leaf must remove more than five sixths of the former tube vertices");
    }

    @Test
    void fullFoliageRetainsAuthoredBladeCount() {
        assertEquals(24, ArborRenderBudget.crownBlades(1.0F));
        assertEquals(4, ArborRenderBudget.crownBlades(0.0F));
    }
}
