package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TemperVialArtTest {
    private static final TemperVialArt ART = HudMasksFixture.vialArt();

    @Test
    void liquidSettlesIntoTheLowestGlass() {
        for (boolean right : new boolean[]{true, false}) {
            var sprite = ART.sprite(right);
            assertTrue(sprite.liquidDy().length > 20, "a vial holds a readable amount of liquid");
            for (int i = 1; i < sprite.liquidDy().length; i++) {
                assertTrue(sprite.liquidDy()[i - 1] >= sprite.liquidDy()[i], "liquid fills from the lowest row up");
            }
        }
    }

    @Test
    void fillCountsAreProportional() {
        var sprite = ART.sprite(true);
        int n = sprite.liquidDy().length;
        assertEquals(0, TemperVialArt.fillCount(sprite, 0f));
        assertEquals(n, TemperVialArt.fillCount(sprite, 1f));
        assertEquals(n, TemperVialArt.fillCount(sprite, 3f));
        assertEquals(Math.round(n * 0.5f), TemperVialArt.fillCount(sprite, 0.5f));
    }

    @Test
    void theMouthTipsTowardTheVessel() {
        var right = ART.sprite(true);
        assertTrue(Arrays.stream(right.dx()).allMatch(dx -> dx <= 2), "a right-pointing vial's body lies left of its mouth");
        assertTrue(Arrays.stream(right.dy()).allMatch(dy -> dy <= 2), "and above it, tipped to pour");
        var left = ART.sprite(false);
        assertTrue(Arrays.stream(left.dx()).allMatch(dx -> dx >= -2));
        assertTrue(Arrays.stream(right.dx()).anyMatch(dx -> dx < -9), "tipped about 50 degrees, not upright");
    }

    @Test
    void aHealedSocketHoldsNoLiquid() {
        var socket = ART.socket(true);
        assertTrue(socket.dx().length > 3);
        assertEquals(0, socket.liquidDx().length);
    }

    @Test
    void trickleNeedsTheVesselAndUsableTemper() {
        assertFalse(TemperStream.flows(false, 5, 10, false), "no vessel, nothing to pour into");
        assertFalse(TemperStream.flows(true, 0, 10, false), "an empty vial");
        assertFalse(TemperStream.flows(true, 5, 0, false), "no capacity");
        assertFalse(TemperStream.flows(true, 5, 10, true), "a dry vial");
        assertTrue(TemperStream.flows(true, 5, 10, false));
    }
}
