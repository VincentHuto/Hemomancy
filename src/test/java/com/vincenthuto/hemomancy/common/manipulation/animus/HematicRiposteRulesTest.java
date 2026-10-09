package com.vincenthuto.hemomancy.common.manipulation.animus;

import static com.vincenthuto.hemomancy.common.manipulation.animus.HematicRiposteRules.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HematicRiposteRulesTest {
    @Test
    void windowOpensOnTheCastTickAndClosesAfterNineTicks() {
        assertTrue(inWindow(100, 100));
        assertTrue(inWindow(100, 108));
        assertFalse(inWindow(100, 109));
        assertFalse(inWindow(100, 99));
    }

    @Test
    void frontalArcFollowsYawAndWrapsAround() {
        // Yaw 0 faces +Z.
        assertTrue(isFrontal(0, 0, 5));
        assertTrue(isFrontal(0, -5, 0));
        assertTrue(isFrontal(0, 5, 0));
        assertTrue(isFrontal(0, offsetX(100), offsetZ(100)));
        assertFalse(isFrontal(0, offsetX(101), offsetZ(101)));
        assertFalse(isFrontal(0, 0, -5));
        // Facing yaw 350 still covers a hit arriving from yaw 10.
        assertTrue(isFrontal(350, offsetX(10), offsetZ(10)));
        assertFalse(isFrontal(90, offsetX(-90), offsetZ(-90)));
        assertTrue(isFrontal(0, 0, 0), "an attacker inside the player counts as frontal");
    }

    @Test
    void seedRoundTripsSideAndTiltAndStaysPositive() {
        for (int side : new int[] { -1, 1 })
            for (int tilt = 0; tilt <= MAX_TILT; tilt++) {
                int count = encodeSeed(side, tilt);
                assertTrue(count >= 1);
                assertEquals(side, side(count));
                assertEquals(tilt, tilt(count));
            }
        assertEquals(MAX_TILT, tilt(encodeSeed(1, 99)));
    }

    @Test
    void swatCarriesOnWithTheSweepInsteadOfReturningToTheShooter() {
        for (double yaw : new double[] { 0, 45, 180, -120 }) {
            double[] forward = forward(yaw);
            double[] right = right(yaw);
            for (int side : new int[] { -1, 1 }) {
                double[] swat = swatDirection(yaw, side);
                assertEquals(1, Math.sqrt(dot(swat, swat)), 1e-9);
                assertTrue(dot(swat, forward) < 0.5, "swat should leave sideways, not straight back out");
                assertTrue(dot(swat, forward) > 0, "swat must never fold back into the player");
                assertEquals(-side, Math.signum(dot(swat, right)), "swat leaves away from the arm's shoulder");
            }
        }
    }

    @Test
    void rightHandSideMatchesMinecraftAxes() {
        double[] right = right(0);
        assertEquals(-1, right[0], 1e-9);
        assertEquals(0, right[2], 1e-9);
    }

    @Test
    void classifyOnlyAcceptsDirectMeleeInReachAndProjectiles() {
        assertEquals(Kind.MELEE, classify(false, true, false, true));
        assertEquals(Kind.NONE, classify(false, true, false, false));
        assertEquals(Kind.PROJECTILE, classify(true, false, false, false));
        assertEquals(Kind.NONE, classify(true, false, true, true));
        assertEquals(Kind.NONE, classify(false, true, true, true));
        assertEquals(Kind.NONE, classify(false, false, false, true));
    }

    @Test
    void refundIsShorterThanTheWhiffCooldown() {
        assertTrue(REFUND_COOLDOWN < COOLDOWN);
        assertTrue(PARRY_CLOSE < VISUAL_TICKS);
        assertTrue(EMERGE_END < SWEEP_END && SWEEP_END < STRIKE_END && STRIKE_END < VISUAL_TICKS);
    }

    private static double offsetX(double yaw) {
        return -Math.sin(Math.toRadians(yaw)) * 5;
    }

    private static double offsetZ(double yaw) {
        return Math.cos(Math.toRadians(yaw)) * 5;
    }

    private static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }
}
