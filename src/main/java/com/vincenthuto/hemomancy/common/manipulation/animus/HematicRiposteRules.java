package com.vincenthuto.hemomancy.common.manipulation.animus;

/**
 * Pure timing, facing and encoding rules for Hematic Riposte. Kept free of Minecraft types so the parry contract
 * can be checked without a running game; vectors are {@code {x, y, z}} arrays in Minecraft's world axes.
 */
public final class HematicRiposteRules {
    public static final int COST = 60;
    public static final int COOLDOWN = 40;
    /** Cooldown left after any successful parry in the cast. */
    public static final int REFUND_COOLDOWN = 10;

    /**
     * Server parry window, inclusive ticks after the cast. It opens immediately because the server only hears the
     * key press after network latency, and vanilla melee has no wind-up to read.
     */
    public static final int PARRY_OPEN = 0;
    public static final int PARRY_CLOSE = 8;

    /** Client arm presentation; the swipe is visual only and never gates the server window. */
    public static final int VISUAL_TICKS = 16;
    public static final int EMERGE_END = 2;
    public static final int SWEEP_END = 6;
    public static final int STRIKE_END = 8;

    /** Half-angle, in degrees either side of body facing, that the forward swipe covers. */
    public static final double FRONT_HALF_ARC = 100.0D;

    public static final int STUN_MOB = 30;
    public static final int STUN_PLAYER = 12;
    public static final double KNOCKBACK = 0.55D;
    /** Melee attackers further than this from the parrying player are treated as reach exploits, not parried. */
    public static final double MELEE_REACH = 6.0D;

    public static final double SWAT_LATERAL = 0.9D;
    public static final double SWAT_FORWARD = 0.3D;
    public static final double SWAT_LIFT = 0.15D;
    public static final double SWAT_MIN_SPEED = 0.6D;

    public static final int MAX_TILT = 4;

    public enum Kind { MELEE, PROJECTILE, NONE }

    private HematicRiposteRules() {
    }

    public static boolean inWindow(long castTick, long now) {
        long elapsed = now - castTick;
        return elapsed >= PARRY_OPEN && elapsed <= PARRY_CLOSE;
    }

    /** Whether a hit arriving from offset ({@code dx}, {@code dz}) lies inside the frontal arc of {@code yawDegrees}. */
    public static boolean isFrontal(double yawDegrees, double dx, double dz) {
        if (dx * dx + dz * dz < 1.0E-8D) return true;
        double toward = Math.toDegrees(Math.atan2(-dx, dz));
        return Math.abs(wrapDegrees(toward - yawDegrees)) <= FRONT_HALF_ARC;
    }

    public static double wrapDegrees(double degrees) {
        double wrapped = degrees % 360.0D;
        if (wrapped >= 180.0D) wrapped -= 360.0D;
        if (wrapped < -180.0D) wrapped += 360.0D;
        return wrapped;
    }

    /** Packs the sweep side (+1 right shoulder, -1 left) and tilt into a visual packet count that is always positive. */
    public static int encodeSeed(int side, int tilt) {
        int clampedTilt = Math.max(0, Math.min(MAX_TILT, tilt));
        return 1 + (side > 0 ? 1 : 0) + 2 * clampedTilt;
    }

    public static int side(int count) {
        return ((Math.max(1, count) - 1) & 1) == 1 ? 1 : -1;
    }

    public static int tilt(int count) {
        return Math.max(0, Math.min(MAX_TILT, (Math.max(1, count) - 1) >> 1));
    }

    /** Horizontal facing for a Minecraft yaw: yaw 0 faces +Z. */
    public static double[] forward(double yawDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        return new double[] { -Math.sin(yaw), 0.0D, Math.cos(yaw) };
    }

    /** The wearer's right hand side; positive local X is the wearer's left when facing +Z. */
    public static double[] right(double yawDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        return new double[] { -Math.cos(yaw), 0.0D, -Math.sin(yaw) };
    }

    /**
     * Direction a swatted projectile leaves in: it carries on with the sweep (away from the shoulder the arm came
     * over), slightly forward and up, so it never returns down the shooter's line.
     */
    public static double[] swatDirection(double yawDegrees, int side) {
        double[] f = forward(yawDegrees);
        double[] r = right(yawDegrees);
        double s = side >= 0 ? 1.0D : -1.0D;
        double x = -s * r[0] * SWAT_LATERAL + f[0] * SWAT_FORWARD;
        double y = SWAT_LIFT;
        double z = -s * r[2] * SWAT_LATERAL + f[2] * SWAT_FORWARD;
        double length = Math.sqrt(x * x + y * y + z * z);
        return new double[] { x / length, y / length, z / length };
    }

    /**
     * Classifies an incoming hit. Thorns, explosions, magic and invulnerability-bypassing sources are never parried,
     * and neither are melee hits from beyond reach.
     */
    public static Kind classify(boolean directIsProjectile, boolean directIsCausingLiving, boolean excludedSource,
            boolean withinReach) {
        if (excludedSource) return Kind.NONE;
        if (directIsProjectile) return Kind.PROJECTILE;
        if (directIsCausingLiving && withinReach) return Kind.MELEE;
        return Kind.NONE;
    }
}
