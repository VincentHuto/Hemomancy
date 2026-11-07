package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeHoverTest {
    @Test
    void herNumbersMatchTheServersHoverAndGlide() {
        assertEquals((float) SeraphaeEntity.HOVER, SeraphaeHover.HOVER, 1e-6F);
        assertEquals((float) SeraphaeEntity.GLIDE_SPEED, SeraphaeHover.GLIDE, 1e-6F);
        assertEquals(24.0F + 0.35F * 8.0F, SeraphaeHover.FLOOR, 1e-5F);
    }

    @Test
    void herVelocityIsReadInHerOwnFrame() {
        // facing south (yaw 0): south is forward, west (-x) is her right
        float[] south = SeraphaeHover.local(0, 0.1, 0);
        assertEquals(0.1F, south[0], 1e-6F);
        assertEquals(0.0F, south[1], 1e-6F);
        float[] west = SeraphaeHover.local(-0.1, 0, 0);
        assertEquals(0.0F, west[0], 1e-6F);
        assertEquals(0.1F, west[1], 1e-6F);
        // facing west (yaw 90): west is forward
        assertEquals(0.1F, SeraphaeHover.local(-0.1, 0, 90)[0], 1e-6F);
        assertEquals(1.0F, SeraphaeHover.glide(0.11F, 4), 1e-5F);
        assertEquals(-1.0F, SeraphaeHover.glide(-1.0F, 1), 1e-5F);
    }

    @Test
    void anAimsBearingIsPositiveToHerRight() {
        assertEquals(0.0F, SeraphaeHover.bearing(0, 5, 0), 1e-5F);
        assertEquals(Math.PI / 2, SeraphaeHover.bearing(-5, 0, 0), 1e-5F);
        assertEquals(-Math.PI / 2, SeraphaeHover.bearing(5, 0, 0), 1e-5F);
        assertEquals(Math.PI, Math.abs(SeraphaeHover.bearing(0, -5, 0)), 1e-5F);
        assertEquals(0.0F, SeraphaeHover.bearing(0, 0, 37), 1e-6F, "an aim on top of her is straight ahead");
    }

    @Test
    void theLegsFoldOnlyAsFarAsTheFloorAsks() {
        float thigh = -0.07F, knee = 0.24F;
        float rest = SeraphaeHover.reach(thigh, knee);
        assertEquals(0.0F, SeraphaeHover.foldToClear(thigh, knee, rest + 1.0F), "room to hang: no fold");
        for (float room = rest; room > 4.5F; room -= 0.5F) {
            float fold = SeraphaeHover.foldToClear(thigh, knee, room);
            assertTrue(fold > 0.0F);
            float reach = SeraphaeHover.reach(thigh - fold * SeraphaeHover.THIGH_SHARE, knee + fold);
            assertTrue(reach <= room - SeraphaeHover.CLEARANCE + 1e-3F, "the sole clears the floor at room " + room);
            assertTrue(reach > room - SeraphaeHover.CLEARANCE - 0.05F, "and folds no further than it must");
        }
        assertEquals(SeraphaeHover.FOLD_MAX, SeraphaeHover.foldToClear(thigh, knee, -5.0F), 1e-6F, "a full kneel at most");
    }

    @Test
    void aFlaredPanelsHemLiesOnTheFloor() {
        float length = 11.5F;
        assertEquals(0.0F, SeraphaeHover.flare(1.0F, 0.0F, length + 2.0F, length), "a hem over the floor stays put");
        for (float lean : new float[]{-0.3F, 0.0F, 0.2F, 0.6F}) {
            float down = (float) Math.cos(lean), out = (float) -Math.sin(lean);
            for (float room = 10.0F; room > 1.0F; room -= 1.5F) {
                float angle = SeraphaeHover.flare(down, out, room, length);
                float hem = length * (down * (float) Math.cos(angle) + out * (float) Math.sin(angle));
                assertTrue(angle >= 0.0F);
                if (angle > 0.0F) assertEquals(room - SeraphaeHover.CLEARANCE, hem, 1e-3F, "lean " + lean + " room " + room);
                else assertTrue(hem <= room - SeraphaeHover.CLEARANCE, "lean " + lean + " room " + room);
            }
        }
    }

    @Test
    void aRecurringGestureBeatsOncePerPeriodAndAlternates() {
        assertEquals(0.0F, SeraphaeHover.every(0, 96, 0, 0, 14, 28, 46), 1e-6F);
        assertEquals(1.0F, SeraphaeHover.every(20, 96, 0, 0, 14, 28, 46), 1e-6F);
        assertEquals(1.0F, SeraphaeHover.every(20 + 96 * 3, 96, 0, 0, 14, 28, 46), 1e-6F);
        assertEquals(0.0F, SeraphaeHover.every(60, 96, 0, 0, 14, 28, 46), 1e-6F);
        assertNotEquals(SeraphaeHover.cycle(10, 96, 0) & 1, SeraphaeHover.cycle(110, 96, 0) & 1);
    }
}
