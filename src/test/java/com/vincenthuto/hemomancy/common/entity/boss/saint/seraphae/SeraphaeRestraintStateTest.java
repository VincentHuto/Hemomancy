package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeRestraintStateTest {
    @Test void statesAndProgressRoundTripThroughTheTwoInts() {
        int[] states = {SeraphaeRestraintState.HELD, SeraphaeRestraintState.SLIPPED, SeraphaeRestraintState.BINDING,
                SeraphaeRestraintState.HELD, SeraphaeRestraintState.DORMANT, SeraphaeRestraintState.HELD};
        int packed = SeraphaeRestraintState.pack(states);
        for (int slot = 0; slot < states.length; slot++) assertEquals(states[slot], SeraphaeRestraintState.state(packed, slot));
        int progress = SeraphaeRestraintState.packProgress(new float[]{0, 0, 0.5F, 0, 1, 0});
        assertEquals(0.5F, SeraphaeRestraintState.progress(progress, 2), 1.0F / 15.0F);
        assertEquals(1.0F, SeraphaeRestraintState.progress(progress, 4), 1.0E-6F);
    }

    @Test void slippedAnchorsLengthenAndHeldReservesShortenTheChain() {
        int[] states = {SeraphaeRestraintState.HELD, SeraphaeRestraintState.SLIPPED, SeraphaeRestraintState.BINDING,
                SeraphaeRestraintState.HELD, SeraphaeRestraintState.HELD, SeraphaeRestraintState.DORMANT};
        int packed = SeraphaeRestraintState.pack(states);
        assertEquals(2, SeraphaeRestraintState.slipped(packed));
        assertEquals(1, SeraphaeRestraintState.reservesHeld(packed));
    }

    @Test void anchorsHoldTheirIronByWhereTheyStand() {
        assertEquals(SeraphaeRestraint.RIGHT_WRIST, SeraphaeRestraint.forAnchor(false, true));
        assertEquals(SeraphaeRestraint.LEFT_WRIST, SeraphaeRestraint.forAnchor(false, false));
        assertEquals(SeraphaeRestraint.RIGHT_WING, SeraphaeRestraint.forAnchor(true, true));
        assertEquals(SeraphaeRestraint.LEFT_WING, SeraphaeRestraint.forAnchor(true, false));
    }
}
