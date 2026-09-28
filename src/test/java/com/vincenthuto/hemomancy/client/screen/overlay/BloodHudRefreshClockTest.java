package com.vincenthuto.hemomancy.client.screen.overlay;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BloodHudRefreshClockTest {
    @Test
    void requestsOncePerIntervalRegardlessOfRenderedFrames() {
        BloodHudRefreshClock clock = new BloodHudRefreshClock();

        assertTrue(clock.due(100));
        assertFalse(clock.due(100));
        assertFalse(clock.due(109));
        assertTrue(clock.due(110));
        assertFalse(clock.due(110));
        assertTrue(clock.due(0));
    }

    @Test
    void resetAllowsImmediateRefresh() {
        BloodHudRefreshClock clock = new BloodHudRefreshClock();
        assertTrue(clock.due(100));
        clock.reset();
        assertTrue(clock.due(101));
    }
}
