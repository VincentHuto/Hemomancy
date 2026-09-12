package com.vincenthuto.hemomancy.client.screen.radial;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RadialVeinBorderGeometryTest {
    @Test void veinsRemainInsideTheirSliceAndBand() {
        float start=-.7f,end=.4f,inner=50,outer=86;
        var paths=RadialVeinBorderGeometry.paths(start,end,inner,outer,17,12.5);
        assertFalse(paths.isEmpty());
        for(var path:paths)for(var point:path.points()) {
            assertTrue(point.angle()>=start && point.angle()<=end,"vein escaped slice angle");
            assertTrue(point.radius()>=inner && point.radius()<=outer,"vein escaped radial band");
        }
    }

    @Test void animationChangesVeinShapeWithoutChangingTopology() {
        var first=RadialVeinBorderGeometry.paths(0,1,50,86,3,0);
        var later=RadialVeinBorderGeometry.paths(0,1,50,86,3,8);
        assertEquals(first.size(),later.size());
        for(int i=0;i<first.size();i++)assertEquals(first.get(i).points().size(),later.get(i).points().size());
        assertNotEquals(first,later);
    }

    @Test void branchesStartOnTheSmoothedPerimeter() {
        var paths = RadialVeinBorderGeometry.paths(0, 1, 50, 86, 3, 12.5);
        var perimeter = paths.getFirst();
        assertTrue(perimeter.closed());
        for (var branch : paths.subList(1, paths.size())) {
            assertTrue(perimeter.points().contains(branch.points().getFirst()), "detached branch root");
            assertFalse(branch.closed());
        }
    }
}
