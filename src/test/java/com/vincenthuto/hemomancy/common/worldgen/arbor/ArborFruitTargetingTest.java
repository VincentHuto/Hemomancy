package com.vincenthuto.hemomancy.common.worldgen.arbor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArborFruitTargetingTest {
    @Test
    void selectsTheNearestFruitActuallyUnderTheCrosshair() {
        List<ArborFruitTargeting.Candidate> fruit = List.of(
                new ArborFruitTargeting.Candidate(1, 101, new ArborCanopyGeometry.Point(0, 0, 4), .35),
                new ArborFruitTargeting.Candidate(1, 102, new ArborCanopyGeometry.Point(.08, 0, 2), .35),
                new ArborFruitTargeting.Candidate(1, 103, new ArborCanopyGeometry.Point(1.2, 0, 1), .35),
                new ArborFruitTargeting.Candidate(1, 104, new ArborCanopyGeometry.Point(0, 0, 9), .35));

        ArborFruitTargeting.Candidate selected = ArborFruitTargeting.select(fruit,
                new ArborCanopyGeometry.Point(0, 0, 0),
                new ArborCanopyGeometry.Point(0, 0, 1), 8).orElseThrow();

        assertEquals(102, selected.skillId());
    }

    @Test
    void returnsNoHudTargetWhenTheCrosshairMissesEveryFruit() {
        List<ArborFruitTargeting.Candidate> fruit = List.of(
                new ArborFruitTargeting.Candidate(1, 101, new ArborCanopyGeometry.Point(2, 0, 3), .35));

        assertTrue(ArborFruitTargeting.select(fruit,
                new ArborCanopyGeometry.Point(0, 0, 0),
                new ArborCanopyGeometry.Point(0, 0, 1), 8).isEmpty());
    }
}
