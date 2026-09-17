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
}
