package com.vincenthuto.hemomancy.client.model.entity.boss.putriciel;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PutricielTrailTest {
    @Test
    void theTrailInterpolatesBetweenFramesAndKnowsNothingOutsideThem() {
        var trail = new PutricielTrail();
        assertNull(trail.at(0));
        trail.record(new Vec3(0, 0, 0), 10.0F);
        assertNull(trail.at(10.0F), "one frame is not a path");
        trail.record(new Vec3(2, 0, 0), 11.0F);
        trail.record(new Vec3(2, 4, 0), 12.0F);
        assertEquals(1.0D, trail.at(10.5F).x, 1e-6D);
        assertEquals(2.0D, trail.at(11.5F).y, 1e-6D);
        assertEquals(new Vec3(2, 4, 0), trail.at(12.3F), "the newest frame holds a moment past it");
        assertNull(trail.at(13.0F));
        assertNull(trail.at(9.0F));
        assertEquals(new Vec3(2, 4, 0), trail.latest());
    }

    @Test
    void aSecondRecordInOneFrameIsIgnoredAndARewindStartsAgain() {
        var trail = new PutricielTrail();
        trail.record(new Vec3(0, 0, 0), 1.0F);
        trail.record(new Vec3(1, 0, 0), 2.0F);
        trail.record(new Vec3(9, 9, 9), 2.0F);
        assertEquals(new Vec3(1, 0, 0), trail.latest());
        trail.record(new Vec3(5, 0, 0), -10.0F);
        assertNull(trail.at(-10.0F), "a rewound clock (a reload) keeps only the new frame");
        assertEquals(new Vec3(5, 0, 0), trail.latest());
    }

    @Test
    void theTrailKeepsAboutASecondAndMeasuresItsSpeed() {
        var trail = new PutricielTrail();
        for (int i = 0; i < 200; i++) trail.record(new Vec3(i * 0.5D, 0, 0), i * 0.25F);
        float newest = 199 * 0.25F;
        assertNotNull(trail.at(newest - 10.0F));
        assertNull(trail.at(newest - 20.0F), "older frames than the buffer holds are gone");
        assertEquals(2.0D, trail.speed(4.0F), 1e-6D, "half a block a frame at four frames a tick");
    }
}
