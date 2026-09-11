package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChargeVisualProgressTest {
    @Test
    void growthAdvancesBetweenTicksAndDoesNotJumpOnPacketArrival() {
        var progress = new ChargeVisualProgress(.2f, 100);
        assertEquals(.1f, progress.sample(102), .00001f);
        assertTrue(progress.sample(102.5) > progress.sample(102));
        assertEquals(.2f, progress.sample(104), .00001f);
        progress.update(.4f, 104);
        assertEquals(.2f, progress.sample(104), .00001f);
        assertEquals(.3f, progress.sample(106), .00001f);
    }

    @Test
    void interruptionRetargetsFromTheVisibleValueAndStopsAtTarget() {
        var progress = new ChargeVisualProgress(.8f, 0);
        float before = progress.sample(2.5);
        progress.update(.1f, 2.5);
        assertEquals(before, progress.sample(2.5));
        assertTrue(progress.sample(3) < before);
        assertEquals(.1f, progress.sample(100), .00001f);
    }
}
