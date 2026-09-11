package com.vincenthuto.hemomancy.common.manipulation.family;

import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes.Kind;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FerricConstructShapesTest {
    @Test void aRampartBlocksItsWholeWidthWithoutAThickInvisibleFootprint() {
        var north = FerricConstructShapes.bounds(Kind.WALL, Direction.NORTH, new Vec3(10, 64, 20));
        assertEquals(5, north.getXsize()); assertEquals(3, north.getYsize()); assertEquals(.375, north.getZsize());
        assertTrue(north.clip(new Vec3(12, 65, 18), new Vec3(12, 65, 22)).isPresent());
        assertFalse(north.clip(new Vec3(13, 65, 18), new Vec3(13, 65, 22)).isPresent());
        var east = FerricConstructShapes.bounds(Kind.WALL, Direction.EAST, Vec3.ZERO);
        assertEquals(.375, east.getXsize()); assertEquals(5, east.getZsize());
    }
    @Test void contactCannotRepeatBeforeTheNextInterval() {
        assertTrue(FerricConstructShapes.canContact(0, Long.MIN_VALUE));
        assertFalse(FerricConstructShapes.canContact(109, 100));
        assertTrue(FerricConstructShapes.canContact(110, 100));
    }
}
