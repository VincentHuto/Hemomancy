package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HudHeartbeatTest {
    @Test
    void theClockStillMovesSmoothlyOnAnOldWorld() {
        long old = 20L * 60 * 60 * 300;
        float a = HudHeartbeat.seconds(old, 0f);
        float b = HudHeartbeat.seconds(old, 0.5f);
        float c = HudHeartbeat.seconds(old + 1, 0f);
        assertEquals(0.025f, b - a, 1e-4);
        assertEquals(0.025f, c - b, 1e-4);
        assertTrue(a < HudHeartbeat.CLOCK_WRAP_TICKS / 20f, "the clock wraps instead of growing: " + a);
    }

    @Test
    void beatsSlowlyAndQuickensAsBloodRuns() {
        assertEquals(25f, HudHeartbeat.bpm(1f), 1e-4);
        assertEquals(45f, HudHeartbeat.bpm(0f), 1e-4);
        assertEquals(35f, HudHeartbeat.bpm(0.5f), 1e-4);
        assertEquals(25f, HudHeartbeat.bpm(2f), 1e-4);
    }

    @Test
    void lubDubThenRest() {
        assertEquals(1f, HudHeartbeat.stroke(0.06f), 1e-3);
        float dub = HudHeartbeat.stroke(0.26f);
        assertTrue(dub > 0.5f && dub < 0.7f, "the second stroke is softer: " + dub);
        assertEquals(0f, HudHeartbeat.stroke(0.6f), 1e-6);
    }

    @Test
    void darkeningPeaksAtTheCapAndFadesNearEmpty() {
        float max = 0;
        for (int i = 0; i < 2400; i++) max = Math.max(max, HudHeartbeat.darkness(i / 1000f, 1f, 0f));
        assertEquals(0.45f, max, 0.01f);
        float lub = 0.06f * 60f / 45f;
        assertTrue(HudHeartbeat.darkness(lub, 0f, 0f) < 0.26f, "fainter near empty");
    }

    @Test
    void darkeningNeverBrightens() {
        for (boolean apotheos : new boolean[]{false, true}) {
            for (int lit : new int[]{0xD6343E, 0xE89A3A, 0x350107}) {
                int dark = HudHeartbeat.darken(lit, 0.45f, apotheos);
                for (int shift = 0; shift < 24; shift += 8) {
                    assertTrue(((dark >> shift) & 0xFF) <= Math.max((lit >> shift) & 0xFF, 0x46),
                            "channel brightened: " + Integer.toHexString(lit) + " -> " + Integer.toHexString(dark));
                }
            }
        }
        assertEquals(0xD6343E, HudHeartbeat.darken(0xD6343E, 0f, false));
    }

    @Test
    void theBeatTravels() {
        float seconds = 0.06f * 60f / 25f;
        assertTrue(HudHeartbeat.darkness(seconds, 1f, 0f) > HudHeartbeat.darkness(seconds, 1f, 1f),
                "a later section of the vein beats after the base");
    }
}
