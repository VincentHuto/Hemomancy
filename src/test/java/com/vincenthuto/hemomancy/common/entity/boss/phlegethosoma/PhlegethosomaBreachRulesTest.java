package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhlegethosomaBreachRulesTest {
    @Test void leapArcStartsUnderwaterPassesAboveTheSurfaceAndReachesTheStrike() {
        Vec3 start=new Vec3(0,-2,0),strike=new Vec3(8,1,0);
        assertEquals(start,PhlegethosomaBreachRules.arc(start,strike,3,0));
        assertEquals(strike,PhlegethosomaBreachRules.arc(start,strike,3,1));
        assertTrue(PhlegethosomaBreachRules.arc(start,strike,3,.5).y>1);
    }
    @Test void returnArcEndsBelowTheSurface() {
        Vec3 strike=new Vec3(8,1,0),landing=new Vec3(10,-1.6,0);
        assertEquals(landing,PhlegethosomaBreachRules.arc(strike,landing,.35,1));
        assertTrue(PhlegethosomaBreachRules.arc(strike,landing,.35,.9).y<0);
    }
    @Test void flightMovementIsBoundedAndNeverTeleports() {
        Vec3 step=PhlegethosomaBreachRules.step(Vec3.ZERO,new Vec3(10,5,0));
        assertTrue(step.length()<=.9+.000001);
        assertTrue(step.x>0 && step.y>0);
    }
    @Test void diveTelegraphAndReturnHaveIndependentDurations() {
        assertEquals(30,PhlegethosomaBreachRules.TIMELINE.windup());
        assertEquals(20,PhlegethosomaBreachRules.TIMELINE.active());
        assertEquals(24,PhlegethosomaBreachRules.TIMELINE.recovery());
    }
}
