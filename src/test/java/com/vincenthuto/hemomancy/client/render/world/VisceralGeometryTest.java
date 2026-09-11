package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VisceralGeometryTest {
    @Test void eachCrownSwordStartsSmallInsteadOfAppearingMostlyGrown() {
        for(int slot=0;slot<8;slot++) {
            assertEquals(0,VisceralGeometry.crownSwordGrowth(slot/8.0,slot));
            assertEquals(.08,VisceralGeometry.crownSwordGrowth(slot/8.0+.01,slot),1e-9);
            assertEquals(1,VisceralGeometry.crownSwordGrowth((slot+1)/8.0,slot));
        }
    }

    @Test void curvedStrandsKeepTheirActualEndpointsIncludingVerticalCasts() {
        for (Vec3 end : new Vec3[]{new Vec3(0, 8, 0), new Vec3(8, 0, 0), new Vec3(0, 0, -8)}) {
            assertEquals(Vec3.ZERO, VisceralGeometry.strandPoint(Vec3.ZERO, end, 0, 0.4, 9));
            assertTrue(end.distanceTo(VisceralGeometry.strandPoint(Vec3.ZERO, end, 1, 0.4, 9)) < 1e-9);
            assertTrue(VisceralGeometry.strandPoint(Vec3.ZERO, end, 0.5, 0.4, 9).distanceTo(end.scale(.5)) > .01);
        }
    }

    @Test void wellStreamsRemainLowAndConvergeToTheThroat() {
        for (int arm = 0; arm < 5; arm++) {
            for (int step = 0; step <= 32; step++) {
                Vec3 point = VisceralGeometry.wellPoint(6, arm, step / 32.0, 100);
                assertTrue(point.y >= .02 && point.y <= .4, "The field must not fill the aiming lane");
                assertTrue(point.horizontalDistance() <= 6.001);
            }
            assertTrue(VisceralGeometry.wellPoint(6, arm, 1, 100).horizontalDistance() < .35);
        }
    }

    @Test void beamCrossSectionIsPerpendicularForEveryAimDirection() {
        for (Vec3 end : new Vec3[]{new Vec3(0, 12, 0), new Vec3(0, -12, 0), new Vec3(12, 3, -4)}) {
            Vec3 side = VisceralGeometry.side(end);
            assertEquals(1, side.length(), 1e-9);
            assertEquals(0, side.dot(end), 1e-9);
        }
    }

    @Test void bellHasAFlaredMouthAndFiniteThicknessAllTheWayUp() {
        assertTrue(VisceralGeometry.bellRadius(0) > VisceralGeometry.bellRadius(.85) * 2);
        for (int i = 0; i <= 12; i++) assertTrue(VisceralGeometry.bellRadius(i / 12.0) > .1);
    }
}
