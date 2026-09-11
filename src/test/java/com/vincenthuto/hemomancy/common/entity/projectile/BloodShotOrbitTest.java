package com.vincenthuto.hemomancy.common.entity.projectile;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BloodShotOrbitTest {
    @Test
    void fractionalFramesFollowACircleWithoutWaitingForServerPositions() {
        var start=BloodShotOrbit.offset(60,2);
        var middle=BloodShotOrbit.offset(60.5,2);
        var end=BloodShotOrbit.offset(61,2);
        assertTrue(start.distanceTo(middle)>0);
        assertTrue(middle.distanceTo(end)>0);
        assertEquals(1.25,middle.horizontalDistance(),1e-9);
        assertEquals(start.distanceTo(middle),middle.distanceTo(end),.001);
    }

    @Test
    void allFiveSlotsKeepTheirSpacingAndBoundedHeight() {
        for(int slot=0;slot<5;slot++) for(int frame=0;frame<500;frame++) {
            var at=BloodShotOrbit.offset(frame/3.0,slot);
            var next=BloodShotOrbit.offset(frame/3.0,(slot+1)%5);
            assertEquals(1.25,at.horizontalDistance(),1e-9);
            assertTrue(at.y>=1.43 && at.y<=1.67);
            assertEquals(2*1.25*Math.sin(Math.PI/5),at.subtract(next).horizontalDistance(),1e-9);
        }
    }
}
