package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielChamberLightingRules.Light;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielChamberLightingRules.Phase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PutricielChamberLightingRulesTest {
    /** The generated chamber's tomb, (30, 4, 13) in a room whose origin is ROOM, foot west toward the door. */
    private static final BlockPos ROOM = new BlockPos(200, 40, -100);
    private static final BlockPos TOMB = ROOM.offset(30, 4, 13);

    @Test void burningLightsThePyresAndDimsTheShelves() {
        assertTrue(PutricielChamberLightingRules.on(Light.PYRE, Phase.BURNING));
        assertFalse(PutricielChamberLightingRules.on(Light.SHELF, Phase.BURNING));
    }

    @Test void theRotPhaseGuttersThePyresAndBloomsTheShelves() {
        assertFalse(PutricielChamberLightingRules.on(Light.PYRE, Phase.ROT));
        assertTrue(PutricielChamberLightingRules.on(Light.SHELF, Phase.ROT));
        assertEquals(Phase.ROT, Phase.of(true));
        assertEquals(Phase.BURNING, Phase.of(false));
    }

    @Test void theBoxIsExactlyTheGeneratedChamber() {
        // pieces.py: CHAMBER_L, CHAMBER_H, CHAMBER_W = 37, 17, 27
        assertEquals(new BoundingBox(ROOM.getX(), ROOM.getY(), ROOM.getZ(), ROOM.getX() + 36, ROOM.getY() + 16, ROOM.getZ() + 26),
                PutricielChamberLightingRules.chamber(TOMB, Direction.WEST));
    }

    @Test void theBoxTurnsWithTheTomb() {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            BoundingBox box = PutricielChamberLightingRules.chamber(TOMB, facing);
            assertTrue(box.isInside(TOMB.relative(facing, 30)), facing + ": the door end");
            assertFalse(box.isInside(TOMB.relative(facing, 31)), facing + ": past the door");
            assertTrue(box.isInside(TOMB.relative(facing, -6)), facing + ": the back wall");
            assertFalse(box.isInside(TOMB.relative(facing, -7)), facing + ": behind the apse");
            assertTrue(box.isInside(TOMB.relative(facing.getClockWise(), 13)), facing + ": a side wall");
            assertFalse(box.isInside(TOMB.relative(facing.getCounterClockWise(), 14)), facing + ": past a side wall");
            assertEquals(37 * 17 * 27, box.getXSpan() * box.getYSpan() * box.getZSpan(), facing + ": not the chamber's size");
        }
    }

    @Test void theChamberTakesItsCornerPyresAndWallShelvesButNotTheNextHall() {
        Direction facing = Direction.WEST;
        BlockPos home = TOMB.west(40); // where he stands does not matter once there is a tomb
        // the dais corners, a shelf high on an aisle wall, one on the processional
        for (BlockPos pos : new BlockPos[] { TOMB.offset(-2, 0, -3), TOMB.offset(2, 0, 3), ROOM.offset(11, 6, 1),
                ROOM.offset(2, 7, 25), ROOM.offset(1, 15, 13) }) {
            assertTrue(PutricielChamberLightingRules.inArea(TOMB, facing, home, pos), pos + " is in the chamber");
        }
        // the nave beyond the door, a hall past the side wall, the floor below
        for (BlockPos pos : new BlockPos[] { ROOM.offset(-1, 6, 13), ROOM.offset(15, 2, -1), ROOM.offset(15, 2, 27),
                ROOM.offset(37, 4, 13), ROOM.offset(15, -1, 13), ROOM.offset(15, 17, 13) }) {
            assertFalse(PutricielChamberLightingRules.inArea(TOMB, facing, home, pos), pos + " is outside the chamber");
        }
    }

    @Test void noTombFallsBackToACircleRoundTheFightsStart() {
        BlockPos home = new BlockPos(5, 70, -9);
        assertTrue(PutricielChamberLightingRules.inArea(null, null, home, home.offset(16, 8, 0)));
        assertTrue(PutricielChamberLightingRules.inArea(null, null, home, home.offset(0, -8, -16)));
        assertFalse(PutricielChamberLightingRules.inArea(null, null, home, home.offset(17, 0, 0)));
        assertFalse(PutricielChamberLightingRules.inArea(null, null, home, home.offset(12, 0, 12)), "the circle is round, not square");
        assertFalse(PutricielChamberLightingRules.inArea(null, null, home, home.offset(0, 9, 0)));
        BoundingBox scan = PutricielChamberLightingRules.scanBox(null, null, home);
        assertTrue(scan.isInside(home.offset(16, 8, -16)) && scan.isInside(home.offset(-16, -8, 16)));
        assertFalse(scan.isInside(home.offset(17, 0, 0)));
        // a tomb with no usable facing is treated as no chamber
        assertEquals(scan, PutricielChamberLightingRules.scanBox(TOMB, Direction.UP, home));
    }

    @Test void theChamberScanBoxIsTheChamber() {
        assertEquals(PutricielChamberLightingRules.chamber(TOMB, Direction.SOUTH),
                PutricielChamberLightingRules.scanBox(TOMB, Direction.SOUTH, TOMB.offset(40, 0, 0)));
    }
}
