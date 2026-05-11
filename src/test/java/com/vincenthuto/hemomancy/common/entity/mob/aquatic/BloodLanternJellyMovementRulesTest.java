package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

final class BloodLanternJellyMovementRulesTest {
    public static void main(String[] args) {
        onlyDriftsWhileAliveInWater();
        verticalImpulseAvoidsSurfaceAndFloor();
        horizontalDriftStaysGentle();
    }

    private static void onlyDriftsWhileAliveInWater() {
        assertTrue(BloodLanternJellyMovementRules.canDrift(true, true), "living submerged jellies should drift");
        assertFalse(BloodLanternJellyMovementRules.canDrift(false, true), "jellies should not use water drift out of water");
        assertFalse(BloodLanternJellyMovementRules.canDrift(true, false), "dead jellies should not drift");
    }

    private static void verticalImpulseAvoidsSurfaceAndFloor() {
        assertTrue(BloodLanternJellyMovementRules.verticalImpulse(false, true, 0.0D) < 0.0D,
                "jellies should sink away from the surface when water above is blocked");
        assertTrue(BloodLanternJellyMovementRules.verticalImpulse(true, false, 0.0D) > 0.0D,
                "jellies should lift away from the ocean floor when water below is blocked");
        assertTrue(BloodLanternJellyMovementRules.verticalImpulse(true, true, 1.0D) > 0.0D,
                "open-water pulse can bob upward");
    }

    private static void horizontalDriftStaysGentle() {
        double drift = BloodLanternJellyMovementRules.horizontalDrift(1.0D);
        assertTrue(drift > 0.0D, "positive pulse should create positive drift");
        assertTrue(drift <= BloodLanternJellyMovementRules.MAX_HORIZONTAL_DRIFT,
                "ambient jelly drift should stay slow");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }
}
