package com.vincenthuto.hemomancy.common.entity;

import com.vincenthuto.hemomancy.common.entity.mob.monster.MortarboundWallRules;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MortarboundWallRulesTest {
    @Test void movesAlongWallWithoutSteppingIntoOpenAir() {
        assertTrue(MortarboundWallRules.isValidStep(Direction.NORTH, Direction.UP, true, true));
        assertTrue(MortarboundWallRules.isValidStep(Direction.NORTH, Direction.EAST, true, true));
        assertFalse(MortarboundWallRules.isValidStep(Direction.NORTH, Direction.SOUTH, true, true));
        assertFalse(MortarboundWallRules.isValidStep(Direction.NORTH, Direction.EAST, false, true));
        assertFalse(MortarboundWallRules.isValidStep(Direction.NORTH, Direction.EAST, true, false));
    }

    @Test void canTurnAroundAConvexCornerOnlyWhenNewFaceIsSupported() {
        assertTrue(MortarboundWallRules.canTurn(Direction.NORTH, Direction.EAST, true, true));
        assertFalse(MortarboundWallRules.canTurn(Direction.NORTH, Direction.EAST, false, true));
        assertFalse(MortarboundWallRules.canTurn(Direction.NORTH, Direction.EAST, true, false));
        assertFalse(MortarboundWallRules.canTurn(Direction.NORTH, Direction.SOUTH, true, true));
    }

    @Test void picksAConnectedSupportedCellTowardTheSound() {
        BlockPos start = new BlockPos(0, 0, -1);
        BlockPos east = start.east();
        var result = MortarboundWallRules.next(start, Direction.NORTH, new Vec3(3, 0, -1),
                (pos, face) -> pos.equals(east) && face == Direction.NORTH,
                pos -> true);
        assertEquals(east, result.cell());
        assertEquals(Direction.NORTH, result.face());
    }

    @Test void roundsAConvexCornerWithoutCrossingTheWall() {
        BlockPos start = new BlockPos(0, 0, -1);
        BlockPos around = new BlockPos(1, 0, 0);
        var result = MortarboundWallRules.next(start, Direction.NORTH, new Vec3(3, 0, 1),
                (pos, face) -> pos.equals(around) && face == Direction.EAST,
                pos -> !pos.equals(new BlockPos(1, 0, -1)));
        assertNull(result);
        result = MortarboundWallRules.next(start, Direction.NORTH, new Vec3(3, 0, 1),
                (pos, face) -> pos.equals(around) && face == Direction.EAST,
                pos -> true);
        assertEquals(around, result.cell());
        assertEquals(Direction.EAST, result.face());
    }
}
