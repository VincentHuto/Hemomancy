package com.vincenthuto.hemomancy.client.model.entity.boss.hemorath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HemorathSpringTest {
    @Test
    void theFirstStepStartsAtRestOnTheTarget() {
        HemorathSpring spring = new HemorathSpring();
        assertEquals(.4F, spring.step(.4F, 1, .5F, .3F));
        assertEquals(0, spring.velocity());
    }

    @Test
    void itSettlesOnItsTargetAndALightlyDampedOneRingsPastIt() {
        HemorathSpring loose = new HemorathSpring(), firm = new HemorathSpring();
        loose.reset(0);
        firm.reset(0);
        float looseMax = 0, firmMax = 0;
        for (int i = 0; i < 400; i++) {
            looseMax = Math.max(looseMax, loose.step(1, .5F, .5F, .25F));
            firmMax = Math.max(firmMax, firm.step(1, .5F, .5F, 1.0F));
        }
        assertTrue(looseMax > 1.2F, "an under-damped spring overshoots");
        assertTrue(firmMax <= 1.002F, "a critically damped spring does not");
        assertEquals(1, loose.value(), 1e-3F);
        assertEquals(1, firm.value(), 1e-3F);
    }

    @Test
    void aLagTrailsADriverThatRunsAheadThenSwingsPastWhenItStops() {
        HemorathSpring spring = new HemorathSpring();
        spring.lag(0, 1, .5F, .5F);
        float lag = 0;
        for (int i = 1; i <= 10; i++) lag = spring.lag(i * .1F, 1, .5F, .5F);
        assertTrue(lag < -.05F, "it trails a rising driver");
        float past = -1;
        for (int i = 0; i < 30; i++) past = Math.max(past, spring.lag(1, 1, .5F, .5F));
        assertTrue(past > .02F, "it swings past once the driver stops");
    }

    @Test
    void aLongFrameStaysStableAndAnEmptyFrameChangesNothing() {
        HemorathSpring spring = new HemorathSpring();
        spring.reset(0);
        float value = spring.step(1, 100, 1.5F, .2F);
        assertTrue(Float.isFinite(value) && Math.abs(value) < 3, "a hitch is integrated in short sub-steps");
        float before = spring.value();
        assertEquals(before, spring.step(5, 0, 1.5F, .2F));
    }

    @Test
    void aKickRingsOutBackToRest() {
        HemorathSpring spring = new HemorathSpring();
        spring.reset(0);
        spring.kick(.5F);
        float peak = 0;
        for (int i = 0; i < 200; i++) peak = Math.max(peak, Math.abs(spring.step(0, .25F, .55F, .32F)));
        assertTrue(peak > .3F);
        assertEquals(0, spring.value(), 1e-3F);
    }
}
