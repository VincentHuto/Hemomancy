package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnettaPoolLayerTest {
    /** Pools spilled one after another get consecutive ids; none of them may share a plane or they z-fight. */
    @Test
    void consecutivePoolsLieOnDistinctLayers() {
        Set<Float> heights = new HashSet<>();
        for (int id = 100; id < 108; id++) heights.add(AnnettaPoolLayers.layer(id));
        assertEquals(8, heights.size());
    }

    @Test
    void layersStayJustAboveTheFloor() {
        for (int id = -20; id < 20; id++) {
            float y = AnnettaPoolLayers.layer(id);
            assertTrue(y > 0.005F && y < 0.04F, "layer " + y);
        }
    }
}
