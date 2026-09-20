package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BombardierAnimationBlendTest {
    @Test
    void transitionStartsOnTheOutgoingPoseAndEndsOnTheIncomingPose() {
        assertEquals(0.0F, BombardierAnimationBlend.incomingWeight(0.0F), 0.0001F);
        assertEquals(1.0F, BombardierAnimationBlend.outgoingWeight(0.0F), 0.0001F);
        assertEquals(1.0F, BombardierAnimationBlend.incomingWeight(
                BombardierAnimationBlend.TRANSITION_TICKS), 0.0001F);
        assertEquals(0.0F, BombardierAnimationBlend.outgoingWeight(
                BombardierAnimationBlend.TRANSITION_TICKS), 0.0001F);
    }

    @Test
    void transitionUsesAClampedSmoothCurve() {
        float quarter = BombardierAnimationBlend.incomingWeight(
                BombardierAnimationBlend.TRANSITION_TICKS * .25F);
        float halfway = BombardierAnimationBlend.incomingWeight(
                BombardierAnimationBlend.TRANSITION_TICKS * .5F);
        float threeQuarters = BombardierAnimationBlend.incomingWeight(
                BombardierAnimationBlend.TRANSITION_TICKS * .75F);

        assertTrue(quarter > 0.0F && quarter < .25F);
        assertEquals(.5F, halfway, .0001F);
        assertTrue(threeQuarters > .75F && threeQuarters < 1.0F);
        assertEquals(0.0F, BombardierAnimationBlend.incomingWeight(-2.0F), .0001F);
        assertEquals(1.0F, BombardierAnimationBlend.incomingWeight(100.0F), .0001F);
    }
}
