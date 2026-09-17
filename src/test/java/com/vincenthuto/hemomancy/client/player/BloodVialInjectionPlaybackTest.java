package com.vincenthuto.hemomancy.client.player;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BloodVialInjectionPlaybackTest {
    @Test void waitsForServerBeforeImpactAndEmitsOnlyOnce() {
        var playback = new BloodVialInjectionPlayback(100);
        assertTrue(playback.elapsed(120, 0) < 16);
        assertFalse(playback.confirmed());
        assertTrue(playback.confirm(120));
        assertFalse(playback.confirm(121));
        assertEquals(16, playback.elapsed(120, 0));
        assertEquals(20.5F, playback.elapsed(124, .5F));
        assertFalse(playback.expired(127));
        assertTrue(playback.expired(128));
    }

    @Test void missingCompletionCannotLeaveAStuckPose() {
        var playback = new BloodVialInjectionPlayback(100);
        assertFalse(playback.expired(116));
        assertTrue(playback.expired(160));
    }
}
