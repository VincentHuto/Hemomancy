package com.vincenthuto.hemomancy.common.entity.boss.annetta;

/**
 * Pure rules for the Stained Priestess: her rise out of the blood, the flight mode cycle, where she hovers and how
 * her wings sit in each mode. No Minecraft types.
 */
public final class PriestessCombatRules {
    public enum Mode { EMERGING, TAKEOFF, HOVER, SWOOP, LANDED, SHIELD, WIND_BLAST, PLUMMET }

    public enum Wing { FOLDED, SPREAD, SHIELD }

    // The rise, in mode ticks.
    public static final int PUSH_UP_END = 26;
    public static final int KNEEL_END = 50;
    public static final int LANCEA_AT = 62;
    public static final int STAND_END = 80;
    public static final int EMERGE_LENGTH = 90;

    // The swoop, in mode ticks.
    public static final int SWOOP_TELEGRAPH = 12;
    public static final int SWOOP_DIVE_END = 28;
    public static final int SWOOP_LENGTH = 42;

    // The plummet: climb over the target, hang with the spear reversed, drop on them point first.
    public static final int PLUMMET_RISE_END = 18;
    public static final int PLUMMET_HANG_END = 32;
    /** The drop ends at the floor or here, whichever comes first. */
    public static final int PLUMMET_LATEST_IMPACT = 46;
    /** Planted in the floor after the impact before she wrenches the spear free and fights on foot. */
    public static final int PLUMMET_RECOVERY = 22;
    public static final int PLUMMET_LENGTH = PLUMMET_LATEST_IMPACT + PLUMMET_RECOVERY;

    // The spear combo, in action ticks: a lunging thrust, a full spinning reap, a leap and a plunge into the floor.
    public static final int LUNGE_WIND_END = 9;
    public static final int LUNGE_HIT = 11;
    public static final int REAP_WIND_START = 17;
    public static final int REAP_START = 22;
    public static final int REAP_HIT = 26;
    public static final int REAP_END = 30;
    public static final int LEAP_CROUCH = 35;
    public static final int LEAP_JUMP = 38;
    public static final int PLUNGE_DRIVE = 43;
    public static final int PLUNGE_HIT = 46;
    public static final int PLUNGE_PULL = 54;
    public static final int COMBO_LENGTH = 62;

    // The javelin, in action ticks: drawn back and charged, thrown, then grown back out of her hand.
    public static final int JAVELIN_RELEASE = 16;
    public static final int JAVELIN_REFORM_START = 28;
    public static final int JAVELIN_LENGTH = 40;

    // A planted lance or a plunge wakes the blood under the floor: thorns burst up after a short tell.
    public static final int ERUPTION_TELL = 8;
    public static final int ERUPTION_LIFE = 26;
    /** Below this share of her health she throws three lances instead of one. */
    public static final float JAVELIN_FAN_HEALTH = 0.5F;

    public static final int TAKEOFF_LENGTH = 16;
    public static final int SHIELD_LENGTH = 40;
    public static final int WIND_BLAST_LENGTH = 26;
    public static final int WIND_BLAST_RELEASE = 9;
    /** Damage taken within a few seconds that makes her wrap her wings round herself. */
    public static final float SHIELD_TRIGGER = 24.0F;
    /** Her body plus the wings' upper sweep. */
    private static final double HEADROOM = 2.4D;

    private PriestessCombatRules() {}

    public static float wingGrowth(float emergeTick) {
        return AnnettaEncounterRules.smooth((emergeTick - 18.0F) / (EMERGE_LENGTH - 18.0F));
    }

    public static float headGrowth(float emergeTick) {
        return AnnettaEncounterRules.smooth((emergeTick - 8.0F) / 50.0F);
    }

    public static boolean airborne(Mode mode) {
        return mode == Mode.TAKEOFF || mode == Mode.HOVER || mode == Mode.SWOOP || mode == Mode.PLUMMET;
    }

    public static int length(Mode mode, int roll) {
        return switch (mode) {
            case EMERGING -> EMERGE_LENGTH;
            case TAKEOFF -> TAKEOFF_LENGTH;
            case HOVER -> 120 + roll;
            case SWOOP -> SWOOP_LENGTH;
            case LANDED -> 120 + roll / 2 * 2;
            case SHIELD -> SHIELD_LENGTH;
            case WIND_BLAST -> WIND_BLAST_LENGTH;
            case PLUMMET -> PLUMMET_LENGTH;
        };
    }

    /** What follows a finished mode. A target that has fled far is always swooped. */
    public static Mode next(Mode finished, int roll, boolean targetFar) {
        return switch (finished) {
            case EMERGING, LANDED -> Mode.TAKEOFF;
            case TAKEOFF, WIND_BLAST -> Mode.HOVER;
            case HOVER -> targetFar || roll < 40 ? Mode.SWOOP : roll < 70 ? Mode.PLUMMET : Mode.LANDED;
            case SWOOP -> roll < 40 ? Mode.LANDED : Mode.HOVER;
            case SHIELD -> Mode.WIND_BLAST;
            // She comes out of the floor already on foot, spear in hand.
            case PLUMMET -> Mode.LANDED;
        };
    }

    public static Wing wing(Mode mode, boolean moving) {
        return switch (mode) {
            case SHIELD -> Wing.SHIELD;
            case LANDED -> moving ? Wing.FOLDED : Wing.SPREAD;
            case EMERGING -> Wing.FOLDED;
            default -> Wing.SPREAD;
        };
    }

    /** Thorn delays along the plunge's line: each one bursts a beat after the one before, running away from her. */
    public static int lineDelay(int index) {
        return ERUPTION_TELL + index * 2;
    }

    /** How many lances the javelin throws at this share of health. */
    public static int javelinCount(float healthFraction) {
        return healthFraction < JAVELIN_FAN_HEALTH ? 3 : 1;
    }

    /** Whether the held lancea is in her hand at this javelin tick (it leaves on release and grows back). */
    public static float heldSpear(float javelinTick) {
        if (javelinTick < JAVELIN_RELEASE) return 1.0F;
        if (javelinTick < JAVELIN_REFORM_START) return 0.0F;
        return AnnettaEncounterRules.smooth((javelinTick - JAVELIN_REFORM_START) / (JAVELIN_LENGTH - 2 - JAVELIN_REFORM_START));
    }

    /**
     * A point on her orbit round the target: radius out, height up, but never so high that her body and wings
     * would meet a roof {@code ceilingAbove} blocks over the target's feet.
     */
    public static double[] hoverPoint(double tx, double ty, double tz, double angle, double radius, double height,
            double ceilingAbove) {
        double y = Math.max(ty, Math.min(ty + height, ty + ceilingAbove - HEADROOM));
        return new double[]{tx + Math.cos(angle) * radius, y, tz + Math.sin(angle) * radius};
    }
}
