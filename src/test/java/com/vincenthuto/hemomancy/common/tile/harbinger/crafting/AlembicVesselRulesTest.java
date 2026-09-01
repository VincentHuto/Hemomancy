package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlembicVesselRulesTest {
    @Test
    void flaskAndJugUseTheirFullBloodCapacities() {
        assertEquals(2_500, AlembicVesselRules.requiredBlood(AlembicVesselRules.Vessel.FLASK));
        assertEquals(5_000, AlembicVesselRules.requiredBlood(AlembicVesselRules.Vessel.JUG));
    }
}
