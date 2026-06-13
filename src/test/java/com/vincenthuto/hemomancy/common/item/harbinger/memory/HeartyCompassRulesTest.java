package com.vincenthuto.hemomancy.common.item.harbinger.memory;

public final class HeartyCompassRulesTest {
    private static final float EPSILON = 0.001F;

    public static void main(String[] args) {
        assertClose(0.0F,
                HeartyCompassRules.relativeNeedleDegrees(0.0D, 0.0D, 0.0F, 0.0D, 10.0D),
                "facing south should point straight ahead to a southern death");
        assertClose(90.0F,
                HeartyCompassRules.relativeNeedleDegrees(0.0D, 0.0D, 0.0F, -10.0D, 0.0D),
                "facing south should rotate right toward a western death");
        assertClose(-90.0F,
                HeartyCompassRules.relativeNeedleDegrees(0.0D, 0.0D, 0.0F, 10.0D, 0.0D),
                "facing south should rotate left toward an eastern death");
        assertClose(0.0F,
                HeartyCompassRules.relativeNeedleDegrees(0.0D, 0.0D, -90.0F, 10.0D, 0.0D),
                "facing east should point straight ahead to an eastern death");
        assertClose(90.0F,
                HeartyCompassRules.relativeNeedleDegrees(0.0D, 0.0D, -90.0F, 0.0D, 10.0D),
                "facing east should rotate right toward a southern death");
    }

    private static void assertClose(float expected, float actual, String message) {
        if (Math.abs(expected - actual) > EPSILON) {
            throw new AssertionError(message + " expected " + expected + " but got " + actual);
        }
    }
}
