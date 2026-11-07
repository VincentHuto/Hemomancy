package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeLimbTrailTest {
    @Test
    void aReadBetweenFramesFollowsThePathTheLimbDrew() {
        SeraphaeLimbTrail trail = new SeraphaeLimbTrail();
        double[] out = new double[3];
        assertFalse(trail.at(0, out), "nothing kept yet");
        for (int frame = 0; frame <= 10; frame++) trail.record(frame * 0.5F, frame, 2 * frame, -frame);
        assertTrue(trail.at(1.25F, out));
        assertArrayEquals(new double[]{2.5, 5.0, -2.5}, out, 1e-6);
        assertTrue(trail.at(5.3F, out), "a read just past the newest frame returns it");
        assertArrayEquals(new double[]{10, 20, -10}, out, 1e-6);
        assertFalse(trail.at(6.0F, out), "a read beyond the lead is unknown");
        assertFalse(trail.at(-1.0F, out), "a read older than the kept frames is unknown");
        assertEquals(0.0F, trail.oldest());
        assertEquals(5.0F, trail.newest());
    }

    @Test
    void theSameFrameDrawnTwiceIsKeptOnceAndAReloadStartsOver() {
        SeraphaeLimbTrail trail = new SeraphaeLimbTrail();
        trail.record(1, 0, 0, 0);
        trail.record(2, 1, 1, 1);
        trail.record(2, 9, 9, 9);
        assertEquals(2, trail.size());
        double[] out = new double[3];
        assertTrue(trail.at(2, out));
        assertArrayEquals(new double[]{1, 1, 1}, out, 1e-9, "the second draw of a frame changes nothing");
        trail.record(0.25F, 4, 4, 4);
        assertEquals(1, trail.size(), "time ran backwards: the body was reloaded");
    }

    @Test
    void itKeepsAboutASecondOfFrames() {
        SeraphaeLimbTrail trail = new SeraphaeLimbTrail();
        for (int frame = 0; frame < 200; frame++) trail.record(frame / 3.0F, frame, 0, 0);
        assertEquals(SeraphaeLimbTrail.FRAMES, trail.size());
        double[] out = new double[3];
        assertTrue(trail.at(trail.oldest(), out));
        assertEquals(200 - SeraphaeLimbTrail.FRAMES, out[0], 1e-6);
        assertFalse(trail.at(trail.oldest() - 0.1F, out));
    }
}
