package com.vincenthuto.hemomancy.client.screen.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GourdHudFillPixelsTest {
    @Test
    void fillStaysInsideTheHourglassWindow() {
        int[] empty = GourdHudFillPixels.render(0.0, 0, 0, 0.0f);
        int[] half = GourdHudFillPixels.render(0.5, 0, 0, 0.0f);
        int[] full = GourdHudFillPixels.render(1.0, 0, 0, 0.0f);

        assertEquals(32 * 64, full.length);
        assertEquals(0, empty[43 * 32 + 16]);
        assertEquals(0, half[26 * 32 + 16]);
        assertNotEquals(0, half[43 * 32 + 16]);
        assertNotEquals(0, full[26 * 32 + 16]);
        assertEquals(0, full[30 * 32 + 4]);
    }

    @Test
    void communionChangesGourdBloodLikeTheMainVessel() {
        int[] clear = GourdHudFillPixels.render(1.0, 7, 0, 1.0f);
        int[] corrupted = GourdHudFillPixels.render(1.0, 7, 9, 1.0f);
        assertNotEquals(clear[43 * 32 + 16], corrupted[43 * 32 + 16]);
    }

    @Test
    void lowFillRemainsReadableWithCorruptedBlood() {
        int[] pixels = GourdHudFillPixels.render(0.208, 7, 9, 1.0f);
        int liquid = pixels[45 * 32 + 16];
        assertEquals(0, pixels[39 * 32 + 16]);
        assertEquals(255, liquid >>> 24);
        assertTrue((liquid >>> 16 & 0xFF) >= 150);
    }
}
