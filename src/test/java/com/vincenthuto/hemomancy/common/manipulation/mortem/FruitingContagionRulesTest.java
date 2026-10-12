package com.vincenthuto.hemomancy.common.manipulation.mortem;

import static com.vincenthuto.hemomancy.common.manipulation.mortem.FruitingContagionRules.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FruitingContagionRulesTest {
    @Test
    void variantsAreDistinctAndPositive() {
        assertTrue(spreading(SPREADING));
        assertFalse(spreading(FRUITING));
        assertTrue(FRUITING >= 1);
    }

    @Test
    void fruitBurstsBeforeTheArcLandsAndTheBloomFollows() {
        assertEquals(1, growth(FRUIT_END), 1e-9);
        assertTrue(burst((FRUIT_END + BURST_END) / 2.0) > 0.8);
        assertEquals(0, arc(FRUIT_END), 1e-9);
        assertEquals(1, arc(ARC_END), 1e-9);
        assertEquals(0, bloom(ARC_END - 1), 1e-9);
        assertTrue(bloom(ARC_END + 2) > 0.5);
        assertEquals(1, wither(VISUAL_TICKS), 1e-9);
        assertEquals(0, lift(0), 1e-9);
        assertEquals(0, lift(1), 1e-9);
        assertTrue(lift(0.5) > lift(0.2));
    }
}
