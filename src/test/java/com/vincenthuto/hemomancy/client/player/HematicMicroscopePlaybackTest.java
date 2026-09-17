package com.vincenthuto.hemomancy.client.player;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HematicMicroscopePlaybackTest {
    @Test void progressLoopRequiresSeatedUnidentifiedSampleAndStopsOnCompletion() {
        var p = new HematicMicroscopePlayback(100, true);
        assertFalse(p.shouldLoop(111));
        assertTrue(p.shouldLoop(112));
        assertTrue(p.shouldLoop(139));
        p.complete();
        assertFalse(p.shouldLoop(140));
        assertEquals(16, p.poseTick(180, 0));
        assertFalse(new HematicMicroscopePlayback(100, false).shouldLoop(125));
    }

    @Test void releaseStopsSoundAndReversesFromTheCurrentPoseOnlyOnce() {
        var p = new HematicMicroscopePlayback(100, true);
        p.release(114);
        assertFalse(p.shouldLoop(114));
        assertEquals(14, p.poseTick(114, 0));
        p.release(116);
        assertTrue(p.poseTick(117, 0) < 14);
        assertFalse(p.expired(119));
        assertTrue(p.expired(120));
        assertEquals(0, p.overlayAlpha(114, 0));
    }

    @Test void lateTrackingStartsAtViewingPoseWithoutReplayingInsertion() {
        var p = new HematicMicroscopePlayback(100, false);
        assertEquals(16, p.poseTick(300, 0));
        assertFalse(p.shouldLoop(300));
    }
}
