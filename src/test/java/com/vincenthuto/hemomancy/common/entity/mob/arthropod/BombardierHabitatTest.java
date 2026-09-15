package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BombardierHabitatTest {
    @Test void sixConnectedGrowthUsesTheYZXMinimumAnchor() {
        Set<BlockPos> growth = Set.of(
                new BlockPos(3, 9, 4), new BlockPos(3, 8, 4), new BlockPos(4, 8, 4),
                new BlockPos(4, 8, 5), new BlockPos(4, 9, 5), new BlockPos(5, 9, 5));
        BlockPos expected = new BlockPos(3, 8, 4);
        for (BlockPos entry : growth)
            assertEquals(expected, BombardierHabitat.resolveComponentAnchor(growth::contains, entry, 256).orElseThrow());
    }

    @Test void diagonalsRemainSeparateAndOversizedComponentsAreRejected() {
        Set<BlockPos> diagonal = Set.of(BlockPos.ZERO, new BlockPos(1, 1, 0));
        assertEquals(BlockPos.ZERO,
                BombardierHabitat.resolveComponentAnchor(diagonal::contains, BlockPos.ZERO, 256).orElseThrow());
        assertEquals(new BlockPos(1, 1, 0),
                BombardierHabitat.resolveComponentAnchor(diagonal::contains, new BlockPos(1, 1, 0), 256).orElseThrow());

        assertTrue(BombardierHabitat.resolveComponentAnchor(
                pos -> pos.getY() == 0 && pos.getZ() == 0 && pos.getX() >= 0 && pos.getX() <= 256,
                BlockPos.ZERO, 256).isEmpty());
    }
}
