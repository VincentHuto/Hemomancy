package com.vincenthuto.hemomancy.common.manipulation.tenebris;

/**
 * Presentation timeline for Umbral Rend's tear, kept free of Minecraft types so it can be tested on the JVM.
 * The shadow already on the victim is drawn tight, three rends tear across the body, the wounds gape, then fray.
 */
public final class UmbralRendRules {
    public static final int GATHER_END = 3;
    public static final int REND_END = 7;
    public static final int GAPE_END = 10;
    public static final int VISUAL_TICKS = 20;
    /** Diagonal tilts the rends may take; packed with a mirror flag into the packet count. */
    public static final int TILTS = 5;

    private UmbralRendRules() {
    }

    /** Packs a seeded tilt (0..TILTS-1) and mirror into a count that is always at least one. */
    public static int encode(int tilt, boolean mirrored) {
        return 1 + Math.floorMod(tilt, TILTS) * 2 + (mirrored ? 1 : 0);
    }

    public static int tilt(int count) {
        return Math.floorMod(Math.max(1, count) - 1, TILTS * 2) / 2;
    }

    public static boolean mirrored(int count) {
        return Math.floorMod(Math.max(1, count) - 1, 2) == 1;
    }

    /** Rend slant in radians from horizontal: steep diagonals from about 35 to 65 degrees. */
    public static double slant(int count) {
        double degrees = 35 + 30 * tilt(count) / (double) (TILTS - 1);
        return Math.toRadians(mirrored(count) ? -degrees : degrees);
    }

    /** How far the clinging ink has been drawn in, 0 loose to 1 tight. */
    public static double gather(double age) {
        return smooth(age / GATHER_END);
    }

    /** How far each rend has torn across the body; later claws trail the first by a tick. */
    public static double tear(double age, int claw) {
        double start = GATHER_END + claw;
        return easeInOut((age - start) / (REND_END - GATHER_END - 2.0));
    }

    /** How wide the wounds stand open: they gape after the tear and close as the effect frays. */
    public static double gape(double age) {
        return smooth((age - REND_END + 1) / (GAPE_END - REND_END + 1)) * (1 - fray(age) * 0.7);
    }

    /** Dissolve progress, 0 intact to 1 gone. */
    public static double fray(double age) {
        return smooth((age - GAPE_END) / (VISUAL_TICKS - GAPE_END));
    }

    public static double smooth(double t) {
        double c = Math.max(0, Math.min(1, t));
        return c * c * (3 - 2 * c);
    }

    static double easeInOut(double t) {
        double c = Math.max(0, Math.min(1, t));
        return c < 0.5 ? 4 * c * c * c : 1 - Math.pow(-2 * c + 2, 3) / 2;
    }
}
