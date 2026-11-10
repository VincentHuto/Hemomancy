package com.vincenthuto.hemomancy.client.render.item.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingSyringeMotionTest {
    @Test
    void loadingAndEjectingSnapTheChamberBetweenItsStops() {
        var motion = new LivingSyringeMotion();
        assertEquals(-67.5F, motion.sample(0, 0, 10).hinge());
        assertEquals(-67.5F, motion.sample(3, 0, 11).hinge());
        float halfway = motion.sample(3, 0, 13).hinge();
        assertTrue(halfway > -67.5F && halfway < 0);
        assertEquals(0, motion.sample(3, 0, 16).hinge());
        assertEquals(0, motion.sample(0, 0, 17).hinge());
        assertEquals(-67.5F, motion.sample(0, 0, 22).hinge());
    }

    @Test
    void eachSampleIndexesTheNextVialWhileTheChamberStaysShut() {
        var motion = new LivingSyringeMotion();
        assertEquals(90, motion.sample(5, 0, 10).drum());
        assertEquals(90, motion.sample(6, 0, 11).drum());
        var moving = motion.sample(6, 0, 15);
        assertEquals(112.5F, moving.drum());
        assertEquals(0, moving.hinge());
        assertEquals(135, motion.sample(6, 0, 19).drum());
        assertEquals(360, LivingSyringeMotion.resting(11).drum());
    }

    @Test
    void changingHotbarSlotsDoesNotAnimateThePreviousSyringe() {
        var motion = new LivingSyringeMotion();
        motion.sample(3, 0, 10);
        assertEquals(-67.5F, motion.sample(0, 1, 11).hinge());
    }
}
