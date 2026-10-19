package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.AnnettaLooks.Look;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaKnowlesEntity.EncounterState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnettaLooksTest {
    @Test
    void looksDependOnlyOnState() {
        assertEquals(Look.COWERING, AnnettaLooks.look(EncounterState.COWERING));
        assertEquals(Look.PHASE_ONE, AnnettaLooks.look(EncounterState.PHASE_ONE));
        assertEquals(Look.PURGED_SUPPORT, AnnettaLooks.look(EncounterState.CURED_SUPPORT));
        assertEquals(Look.PURGED_RESOLVED, AnnettaLooks.look(EncounterState.RESOLVED));
        assertEquals(Look.PHASE_ONE, AnnettaLooks.look(EncounterState.BREAKING), "she breaks in her Phase 1 look");
        assertEquals(Look.COWERING, AnnettaLooks.look(EncounterState.PURGING, 0.4F), "red until the Draught has half drained it");
        assertEquals(Look.PURGED_SUPPORT, AnnettaLooks.look(EncounterState.PURGING, 0.6F));

        for (Look look : Look.values()) {
            boolean purged = look == Look.PURGED_SUPPORT || look == Look.PURGED_RESOLVED;
            assertEquals(purged, AnnettaLooks.purged(look), look.name());
            assertEquals(look == Look.PHASE_ONE, AnnettaLooks.showsTick(look), look.name());
        }
    }

    @Test
    void tickSwellRange() {
        assertEquals(0.15F, AnnettaLooks.tickSwell(350.0F, 350.0F), 1.0E-6F);
        assertEquals(1.0F, AnnettaLooks.tickSwell(0.0F, 350.0F), 1.0E-6F);
        assertEquals(1.0F, AnnettaLooks.tickSwell(-5.0F, 350.0F), 1.0E-6F);
        assertEquals(0.15F, AnnettaLooks.tickSwell(400.0F, 350.0F), 1.0E-6F);
        assertTrue(AnnettaLooks.tickSwell(175.0F, 350.0F) > 0.15F);
        assertFalse(AnnettaLooks.tickSwell(175.0F, 350.0F) >= 1.0F);
    }
}
