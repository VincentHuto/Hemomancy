package com.vincenthuto.hemomancy.common.item.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TinctureDoseRulesTest {
    @Test
    void legacyFlasksDefaultToThreeDoses() {
        assertEquals(3, TinctureDoseRules.normalizeRemaining(null, 3));
    }

    @Test
    void dosesClampAndDecrementWithoutGoingNegative() {
        assertEquals(6, TinctureDoseRules.normalizeRemaining(99, 6));
        assertEquals(0, TinctureDoseRules.consume(0));
        assertEquals(2, TinctureDoseRules.consume(3));
    }
}
