package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthLayout.Bounds;
import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthLayout.Cell;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EscharianOvergrowthAnchorsTest {
    @Test void cavernSearchFindsHighWallsAndCeilingsAheadOfTheFloor() {
        var bounds = new Bounds(0, 40, 0, 7, 70, 7);
        var anchors = EscharianOvergrowthAnchors.find(bounds, cell -> true,
                (cell, normal) -> normal == Direction.DOWN && cell.y() == 70
                        || normal == Direction.EAST && cell.x() == 0 && cell.y() > 48
                        || normal == Direction.UP && cell.y() == 40, 3, 256);
        assertTrue(anchors.stream().anyMatch(a -> a.normal() == Direction.DOWN));
        assertTrue(anchors.stream().anyMatch(a -> a.normal() == Direction.EAST && a.cell().y() > 60));
        assertTrue(anchors.stream().limit(128).noneMatch(a -> a.normal() == Direction.UP));
        assertTrue(anchors.size() <= 256);
    }

    @Test void oneInEightAttemptsPrioritizeGroundAndOtherRunsRetainFallback() {
        int floorRuns = 0;
        for (long seed = 0; seed < 64; seed++) {
            var anchors = EscharianOvergrowthAnchors.find(new Bounds(0, 40, 0, 1, 40, 1), cell -> true,
                    (cell, normal) -> true, seed, 16);
            if (anchors.getFirst().normal() == Direction.UP) floorRuns++;
            assertTrue(anchors.stream().anyMatch(a -> a.normal()==Direction.UP));
        }
        assertEquals(8, floorRuns);
    }

    @Test void aCeilingColonyTriesSeveralRoofLocationsBeforeFallingBackToAnEasyWall() {
        var anchors = EscharianOvergrowthAnchors.find(new Bounds(0, 40, 0, 15, 70, 15), cell -> true,
                (cell, normal) -> normal == Direction.DOWN && cell.y() == 70
                        || normal == Direction.EAST && cell.x() == 0, 0, 256);
        assertTrue(anchors.stream().limit(12).allMatch(a -> a.normal() == Direction.DOWN));
        assertTrue(anchors.stream().anyMatch(a -> a.normal() == Direction.EAST));
    }

    @Test void occupiedCellsAreNeverAnchorsAndOrderingIsRepeatable() {
        var bounds = new Bounds(-16, 40, -16, -1, 70, -1);
        var first = EscharianOvergrowthAnchors.find(bounds, cell -> cell.y() == 70,
                (cell, normal) -> normal == Direction.DOWN, 19, 32);
        assertEquals(first, EscharianOvergrowthAnchors.find(bounds, cell -> cell.y() == 70,
                (cell, normal) -> normal == Direction.DOWN, 19, 32));
        assertEquals(32, first.size());
        assertTrue(first.stream().allMatch(a -> a.cell().y() == 70));
    }
}
