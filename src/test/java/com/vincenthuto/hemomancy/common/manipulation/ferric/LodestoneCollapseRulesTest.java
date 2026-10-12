package com.vincenthuto.hemomancy.common.manipulation.ferric;

import static com.vincenthuto.hemomancy.common.manipulation.ferric.LodestoneCollapseRules.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LodestoneCollapseRulesTest {
    @Test
    void shardsTravelFromTheRimToTheKnotBeforeTheCrush() {
        for (int i = 0; i < SHARDS; i++) {
            assertEquals(0, draw(FIELD_END, i), 1e-9, "shard " + i + " waits for the field");
            assertEquals(1, draw(DRAW_END, i), 1e-9, "shard " + i + " arrives by the crush");
            double[] rim = shard(i, 0, 7), knot = shard(i, 1, 7);
            assertEquals(1, rim[1], 1e-9);
            assertTrue(knot[1] < 0.1, "shards end close to the point");
            assertTrue(knot[2] < rim[2] || rim[2] == 0.15, "shards drop toward the knot");
        }
    }

    @Test
    void fieldTightensAndCrushOvershootsOnce() {
        assertEquals(1, fieldRadius(0), 1e-9);
        assertTrue(fieldRadius(DRAW_END) < 0.3);
        assertEquals(0, crush(DRAW_END), 1e-9);
        assertEquals(0, crush(CRUSH_END), 1e-9);
        assertEquals(1.25, crush((DRAW_END + CRUSH_END) / 2.0), 1e-9);
        assertEquals(0, sink(CRUSH_END), 1e-9);
        assertEquals(1, sink(VISUAL_TICKS), 1e-9);
    }
}
