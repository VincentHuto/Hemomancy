package com.vincenthuto.hemomancy.client.render.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class StreamInertiaTest {
    private static final double[] SLACK = { 0, 0.5, 1 };

    private static List<Vec3> rope() {
        return new ArrayList<>(List.of(new Vec3(0, 1, 0), new Vec3(0, 1, 1), new Vec3(0, 1, 2)));
    }

    @Test
    void streamsTrailWhenTheCasterMovesAndSettleBack() {
        StreamInertia.apply(7, 0, rope(), SLACK, Vec3.ZERO, 1000, 1);
        // The caster steps a block sideways in one tick.
        Vec3 feet = new Vec3(1, 0, 0);
        List<Vec3> swung = rope();
        StreamInertia.apply(7, 0, swung, SLACK, feet, 1001, 1);
        assertEquals(new Vec3(0, 1, 0), swung.get(0), "the pinned end moves with the hand");
        assertTrue(swung.get(2).x < -0.3, "the loose end is left behind: " + swung.get(2));
        assertTrue(swung.get(2).x < swung.get(1).x, "the looser the station, the further it trails");
        List<Vec3> settled = rope();
        for (int tick = 2; tick <= 80; tick++) {
            settled = rope();
            StreamInertia.apply(7, 0, settled, SLACK, feet, 1000 + tick, 1);
        }
        assertTrue(settled.get(2).distanceTo(new Vec3(0, 1, 2)) < 0.01, "the stream swings back into place: " + settled.get(2));
    }

    @Test
    void theDrawnStreamIsKeptForTheParticlesThatRideIt() {
        List<Vec3> path = rope();
        StreamInertia.apply(9, 2, path, SLACK, new Vec3(5, 0, 5), 2000, 1);
        StreamInertia.apply(9, 2, path = rope(), SLACK, new Vec3(6, 0, 5), 2001, 1);
        Vec3[] drawn = StreamInertia.drawn(9, 2, 2001.5);
        assertEquals(path.size(), drawn.length);
        for (int i = 0; i < drawn.length; i++)
            assertTrue(drawn[i].distanceTo(path.get(i)) < 1e-9, "the particles see the stream as it was drawn");
        assertEquals(null, StreamInertia.drawn(9, 2, 2005), "a stream no longer drawn is not ridden");
        assertEquals(null, StreamInertia.drawn(9, 3, 2001), "nor one never drawn");
    }

    @Test
    void withoutAPositionTheStreamStaysRigid() {
        List<Vec3> path = rope();
        StreamInertia.apply(8, 0, path, SLACK, null, 1000, 1);
        assertEquals(rope(), path);
    }
}
