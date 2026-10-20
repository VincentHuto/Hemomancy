package com.vincenthuto.hemomancy.client.render.outpouring;

import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringMarks.Mark;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Detail;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Mode;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OutpouringMarksTest {
    private static Mark mark(int x, long born, int life) {
        return new Mark(x, 63, 0, 1, .5, .5, EnumBloodTendency.ANIMUS, .5f, born, life, 7, false, false);
    }

    @Test void capEvictsOldest() {
        var marks = new OutpouringMarks();
        for (int i = 0; i < 300; i++) marks.add(mark(i, 0, 100));
        assertEquals(OutpouringMarks.CAP, marks.live().size());
        assertTrue(marks.live().stream().noneMatch(m -> m.bx() == 0), "the oldest mark gave way");
    }

    @Test void pruneRemovesExpired() {
        var marks = new OutpouringMarks();
        marks.add(mark(0, 0, 10));
        marks.add(mark(1, 0, 100));
        marks.prune(20);
        assertEquals(1, marks.live().size());
    }

    @Test void clearEmptiesStore() {
        var marks = new OutpouringMarks();
        marks.add(mark(0, 0, 10));
        marks.clear();
        assertTrue(marks.live().isEmpty());
    }

    @Test void marksOnlyOnExposedFaces() {
        assertFalse(OutpouringMarks.exposed(1, true));
        assertTrue(OutpouringMarks.exposed(1, false));
    }

    @Test void quadStaysOnFace() {
        var m = new Mark(0, 63, 0, 1, .98, .5, EnumBloodTendency.MORTEM, .5f, 0, 100, 3, false, false);
        double[] q = OutpouringMarks.quad(m, 10, 0);
        for (int i = 0; i < 4; i++) {
            double x = q[i * 5], y = q[i * 5 + 1], z = q[i * 5 + 2];
            assertTrue(x >= -1e-6 && x <= 1 + 1e-6 && z >= -1e-6 && z <= 1 + 1e-6, "corner " + i + " leaves the face");
            assertEquals(64.004, y, 1e-9);
        }
        for (int face = 0; face < 6; face++) {
            var side = new Mark(5, 70, -3, face, .5, .5, EnumBloodTendency.FERRIC, .4f, 0, 100, 1, false, false);
            double[] s = OutpouringMarks.quad(side, 10, 0);
            var n = OutpouringGeometry.faceNormal(face);
            double plane = n.x != 0 ? s[0] : n.y != 0 ? s[1] : s[2];
            double expected = n.x > 0 ? 6.004 : n.x < 0 ? 4.996 : n.y > 0 ? 71.004 : n.y < 0 ? 69.996 : n.z > 0 ? -1.996 : -3.004;
            assertEquals(expected, plane, 1e-9, "face " + face);
        }
    }

    @Test void reducedKeepsOnlyMarksAtTheFeet() {
        var near = new OutpouringSim.Contact(0, 63, 0, 1, .5, 64, .5, .2f, false);
        var far = new OutpouringSim.Contact(4, 63, 0, 1, 4.5, 64, .5, .2f, false);
        assertEquals(2, OutpouringMarks.keep(List.of(near, far), Detail.FULL, Mode.FULL, .5, 64, .5).size());
        assertEquals(List.of(near), OutpouringMarks.keep(List.of(near, far), Detail.REDUCED, Mode.FULL, .5, 64, .5));
        assertEquals(List.of(near), OutpouringMarks.keep(List.of(near, far), Detail.FULL, Mode.TOGGLE, .5, 64, .5));
        assertTrue(OutpouringMarks.keep(List.of(near, far), Detail.VEINS, Mode.FULL, .5, 64, .5).isEmpty());
    }

    @Test void hitPointBecomesFaceCoordinates() {
        var contact = new OutpouringSim.Contact(2, 64, 7, 4, 2, 64.25, 7.75, .2f, false);
        double[] uv = OutpouringMarks.faceUv(contact);
        assertEquals(.75, uv[0], 1e-9);
        assertEquals(.25, uv[1], 1e-9);
    }

    @Test void nearbyPoolsMergeIntoOneGrowingPool() {
        var marks = new OutpouringMarks();
        marks.add(new Mark(0, 63, 0, 1, .5, .5, EnumBloodTendency.ANIMUS, .4f, 0, 100, 1, true, false));
        marks.add(new Mark(0, 63, 0, 1, .7, .55, EnumBloodTendency.ANIMUS, .4f, 5, 100, 2, true, false));
        marks.add(new Mark(1, 63, 0, 1, .1, .5, EnumBloodTendency.ANIMUS, .4f, 6, 100, 3, true, false));
        assertEquals(1, marks.live().size(), "blood landing in a pool feeds that pool");
        var pool = marks.live().getFirst();
        assertTrue(pool.size() > .4f, "and the pool grows");
        assertTrue(pool.born() + pool.life() >= 106, "and lasts from the latest drop");
        marks.add(new Mark(5, 63, 0, 1, .5, .5, EnumBloodTendency.ANIMUS, .4f, 7, 100, 4, true, false));
        assertEquals(2, marks.live().size(), "a far pool stays separate");
    }

    @Test void poolCentreIsUnclamped() {
        var pool = new Mark(0, 63, 0, 1, .95, .5, EnumBloodTendency.ANIMUS, .4f, 0, 100, 0, true, false);
        double[] c = OutpouringMarks.poolCentre(pool, 0, 0);
        assertEquals(64, c[1], 1e-9);
        assertTrue(c[0] > .8 && c[0] < 1.1);
    }

    @Test void poolsStopAtWalls() {
        OutpouringMarks.Floor wallAtOne = (x, z) -> x < 1;
        assertTrue(OutpouringMarks.fit(.5, .5, .6, wallAtOne) <= .5 + 1e-9, "the disc ends at the wall");
        assertTrue(OutpouringMarks.fit(.5, .5, .6, wallAtOne) >= .4, "but still fills the open floor");
        assertTrue(OutpouringMarks.fit(.98, .5, .6, wallAtOne) < .05, "a pool pressed against the wall shrinks to nothing");
        assertEquals(.6, OutpouringMarks.fit(.5, .5, .6, (x, z) -> true), 1e-9, "open floor keeps the full pool");
    }

    @Test void poolsOnlyDriftWhereTheyFit() {
        var pool = new Mark(0, 63, 0, 1, .5, .5, EnumBloodTendency.ANIMUS, .8f, 0, 400, 0, true, false);
        OutpouringMarks.Floor onlyHome = (x, z) -> x == 0 && z == 0;
        for (long t = 0; t < 200; t += 7) {
            double[] c = OutpouringMarks.poolCentre(pool, t, 0, onlyHome);
            double r = OutpouringMarks.fit(c[0], c[2], .3, onlyHome);
            assertTrue(c[0] - r >= -1e-6 && c[0] + r <= 1 + 1e-6 && c[2] - r >= -1e-6 && c[2] + r <= 1 + 1e-6,
                    "the pool never leaves its block of floor at t=" + t);
        }
    }

    @Test void poolsStayWhereTheyLanded() {
        var pool = new Mark(0, 63, 0, 1, .5, .5, EnumBloodTendency.ANIMUS, .4f, 0, 200, 9, true, false);
        assertArrayEquals(OutpouringMarks.poolCentre(pool, 0, 0), OutpouringMarks.poolCentre(pool, 120, .5f), 1e-9);
    }
}
