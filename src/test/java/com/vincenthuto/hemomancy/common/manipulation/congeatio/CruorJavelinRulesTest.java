package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import static com.vincenthuto.hemomancy.common.manipulation.congeatio.CruorJavelinRules.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CruorJavelinRulesTest {
    @Test
    void phasesFollowFormFlyStrikeShatter() {
        assertEquals(0, formation(0), 1e-9);
        assertEquals(1, formation(FORM_END), 1e-9);
        assertEquals(0, travel(FORM_END), 1e-9);
        assertEquals(1, travel(FLIGHT_END), 1e-9);
        assertTrue(travel(FORM_END + 1) < 0.5, "the throw accelerates");
        assertEquals(0, impact(FLIGHT_END), 1e-9);
        assertEquals(1, impact(IMPACT_END), 1e-9);
        assertEquals(0, shatter(IMPACT_END), 1e-9);
        assertEquals(1, shatter(VISUAL_TICKS), 1e-9);
    }
}
