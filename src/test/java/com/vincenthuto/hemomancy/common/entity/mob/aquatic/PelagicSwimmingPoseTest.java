package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PelagicSwimmingPoseTest {
    @Test void longBodyAxisFollowsHorizontalClimbingAndDivingMovement() {
        for (double[] motion : new double[][]{{.05,0,0},{0,0,-.05},{-.05,0,.02},
                {.02,.04,.03},{-.03,-.04,.02},{0,.05,0},{0,-.05,0}}) {
            var pose = new PelagicSwimmingPose();
            for (int i=0;i<40;i++) pose.tick(motion[0],motion[1],motion[2],true);
            double yaw = Math.toRadians(pose.headingDegrees(1)), lean = pose.leanRadians(1);
            double length = Math.sqrt(motion[0]*motion[0]+motion[1]*motion[1]+motion[2]*motion[2]);
            // The model's upward longitudinal axis after the Minecraft body-yaw transform.
            assertEquals(motion[0]/length, -Math.sin(yaw)*Math.sin(lean), .00001);
            assertEquals(motion[1]/length, Math.cos(lean), .00001);
            assertEquals(motion[2]/length, Math.cos(yaw)*Math.sin(lean), .00001);
        }
    }

    @Test void turnsAndRenderInterpolationAreSmoothAcrossTheHeadingSeam() {
        var pose = new PelagicSwimmingPose();
        pose.tick(0,0,.05,true);
        assertTrue(pose.leanRadians(1) > 0 && pose.leanRadians(1) < Math.PI/2);
        assertEquals(pose.leanRadians(1)*.5F, pose.leanRadians(.5F), .000001);
        for (int i=0;i<40;i++) pose.tick(-Math.sin(Math.toRadians(179))*.05,0,Math.cos(Math.toRadians(179))*.05,true);
        float oldHeading = pose.headingDegrees(1);
        pose.tick(-Math.sin(Math.toRadians(-179))*.05,0,Math.cos(Math.toRadians(-179))*.05,true);
        float delta = wrap(pose.headingDegrees(1)-oldHeading);
        assertEquals(2,delta,.0001);
        assertEquals(1,wrap(pose.headingDegrees(.5F)-oldHeading),.0001);
    }

    @Test void restingAndNonSwimmingCopiesReturnUprightWithoutSharingState() {
        var east = new PelagicSwimmingPose(); var west = new PelagicSwimmingPose();
        for (int i=0;i<40;i++) { east.tick(.05,0,0,true); west.tick(-.05,0,0,true); }
        assertEquals(-90,east.headingDegrees(1),.0001); assertEquals(90,west.headingDegrees(1),.0001);
        for (int i=0;i<40;i++) east.tick(.00001,0,.00001,true);
        assertEquals(0,east.leanRadians(1)); assertEquals(-90,east.headingDegrees(1),.0001);
        assertEquals(Math.PI/2,west.leanRadians(1),.00001);
        for (int i=0;i<40;i++) west.tick(.1,-.1,0,false);
        assertEquals(0,west.leanRadians(1));
        assertEquals(0,new PelagicSwimmingPose().leanRadians(1));
    }

    private static float wrap(float degrees) {
        return (degrees%360+540)%360-180;
    }
}
