package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeDeeds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeMove;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeAnimStateTest {
    @Test
    void anUnchangedTimelinePlaysOnItsOwnClock() {
        for (float t = 0; t < 60; t += 0.5F) assertEquals(t, SeraphaeAnimState.authored(t, 24, 10, 20, 24, 20), 1e-5F);
    }

    @Test
    void aShortWindupStretchesItsKeysAndTheCommitLandsOnTheAuthoredCommit() {
        // the thrown reach after a Wing Grasp winds up in 20 ticks instead of 24
        assertEquals(0.0F, SeraphaeAnimState.authored(0, 20, 10, 20, 24, 20), 1e-5F);
        assertEquals(12.0F, SeraphaeAnimState.authored(10, 20, 10, 20, 24, 20), 1e-5F);
        assertEquals(24.0F, SeraphaeAnimState.authored(20, 20, 10, 20, 24, 20), 1e-5F);
        assertEquals(29.0F, SeraphaeAnimState.authored(25, 20, 10, 20, 24, 20), 1e-5F, "the active span keeps its clock");
        var reach = SeraphaeMove.LAYING_ON_OF_HANDS;
        var hasty = reach.timeline(new SaintMoveInputs(1, SeraphaeMove.laneVariant(6, true), 0, 1));
        assertEquals(20, hasty.windup(), "the timeline this mapping answers");
    }

    @Test
    void aRecoveryCutByNoRestGivenStillSettlesBeforeTheMoveEnds() {
        int repose = 0;
        for (int bit = 0; bit < 8; bit++) if (SeraphaeDeeds.noRest(1 << bit)) repose = 1 << bit;
        assertNotEquals(0, repose, "one deed bit is No Rest Given");
        var cut = SeraphaeMove.NERVE_LASH.timeline(new SaintMoveInputs(1, 0, repose, 1));
        assertTrue(cut.recovery() < 16, "the deed cuts the lash's recovery");
        int end = cut.windup() + cut.active() + cut.recovery();
        assertEquals(36.0F, SeraphaeAnimState.authored(end, cut.windup(), cut.active(), cut.recovery(), 14, 16), 1e-4F,
                "the authored settle ends with the shortened move");
    }
}
