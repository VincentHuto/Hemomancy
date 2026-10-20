package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CastingPlaybackTest {
    @Test void chargeRefreshAndDamageDoNotRestartOpening() {
        var playback = new CastingPlayback(4, CastPhase.OPENING, 100, 100, 1, 40);
        assertTrue(playback.update(4, CastPhase.HOLD, 100, 108, 9, 40));
        assertEquals(10.5F, playback.elapsed(110, .5F));
        assertEquals(.275F, playback.charge(110, 0), .0001F);
        playback.update(4, CastPhase.HOLD, 100, 112, 3, 40);
        assertEquals(.075F, playback.charge(112, 0), .0001F);
        assertEquals(12, playback.elapsed(112, 0));
    }

    @Test void staleHoldCannotResurrectReleasedCast() {
        var playback = new CastingPlayback(4, CastPhase.HOLD, 100, 110, 10, 40);
        assertTrue(playback.update(4, CastPhase.RELEASE, 114, 114, 14, 40));
        assertFalse(playback.update(4, CastPhase.HOLD, 100, 115, 15, 40));
        assertFalse(playback.update(3, CastPhase.RELEASE, 120, 120, 0, 0));
        assertEquals(CastPhase.RELEASE, playback.phase(114));
        assertEquals(CastPhase.RECOVERY, playback.phase(120));
        assertTrue(playback.expired(140));
    }

    @Test void newCastReplacesRecoveryAndMissingHeartbeatExpires() {
        var playback = new CastingPlayback(4, CastPhase.RELEASE, 100, 100, 0, 0);
        assertTrue(playback.update(5, CastPhase.OPENING, 106, 106, 1, 20));
        assertEquals(0, playback.elapsed(106, 0));
        assertFalse(playback.expired(110));
        assertTrue(playback.expired(150));
    }

    @Test void phaseBoundariesComeFromTrackDurations() {
        var playback = new CastingPlayback(4, CastPhase.OPENING, 100, 100, 1, 40, 10, 8, 20, 12);
        assertEquals(CastPhase.OPENING, playback.phase(109));
        assertEquals(CastPhase.HOLD, playback.phase(110));
        playback.update(4, CastPhase.RELEASE, 200, 200, 40, 40);
        assertEquals(CastPhase.RELEASE, playback.phase(207));
        assertEquals(CastPhase.RECOVERY, playback.phase(208));
        assertEquals(2, playback.phaseTime(210, 0));
        assertFalse(playback.expired(227));
        assertTrue(playback.expired(228));
        playback.update(4, CastPhase.CANCEL, 300, 300, 0, 40);
        assertFalse(playback.expired(311));
        assertTrue(playback.expired(312));
    }

    @Test void durationsCanBeReplacedWhenTheTrackChanges() {
        var playback = new CastingPlayback(4, CastPhase.RELEASE, 100, 100, 0, 0);
        playback.durations(6, 3, 5, 4);
        assertEquals(CastPhase.RECOVERY, playback.phase(103));
    }

    @Test void strainChargeIsZeroWhenNothingWasRequired() {
        var quick = new CastingPlayback(4, CastPhase.RELEASE, 100, 100, 0, 0);
        assertEquals(1, quick.charge(100, 0));
        assertEquals(0, quick.strain(100, 0));
        var charged = new CastingPlayback(4, CastPhase.HOLD, 100, 100, 20, 40);
        assertEquals(.5F, charged.strain(100, 0), 1e-6);
    }

    @Test void sustainedPacketsShowTheTrackOpeningInFull() {
        // The server flips OPENING to HOLD after 6 ticks; a 10-tick authored opening still plays to its end.
        var playback = new CastingPlayback(4, CastPhase.OPENING, 100, 100, 1, 40, 10, 8, 20, 12);
        assertTrue(playback.update(4, CastPhase.HOLD, 100, 106, 6, 40));
        assertEquals(CastPhase.OPENING, playback.phase(106));
        assertEquals(CastPhase.OPENING, playback.phase(109));
        assertEquals(CastPhase.HOLD, playback.phase(110));
        assertEquals(CastPhase.HOLD, playback.packetPhase());
    }

    @Test void holdTimeStartsWhenTheOpeningEnds() {
        // Like recovery, the hold's clock starts at its own beginning, so a loop starts from rest.
        var playback = new CastingPlayback(4, CastPhase.OPENING, 100, 100, 1, 40, 10, 8, 20, 12);
        assertEquals(9.5F, playback.phaseTime(109, .5F));
        assertEquals(0, playback.phaseTime(110, 0));
        assertEquals(5.5F, playback.phaseTime(115, .5F));
        assertEquals(15.5F, playback.elapsed(115, .5F));
    }
}
