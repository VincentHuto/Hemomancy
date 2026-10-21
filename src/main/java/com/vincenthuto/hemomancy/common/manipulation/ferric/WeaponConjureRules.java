package com.vincenthuto.hemomancy.common.manipulation.ferric;

/**
 * Presentation rules for the Living Arsenal's "blood hardening into a weapon" cue, free of Minecraft types so they can
 * be tested on the JVM. Blood leaves the forearm, sketches the weapon's outline in the air above the hand, hardens
 * into the weapon's school, then seats into the grip as the real item forms. Each weapon is a set of strokes in a
 * weapon-local plane: {@code a} runs from the grip toward the business end, {@code b} across the weapon's flat.
 */
public final class WeaponConjureRules {
    public static final int BLADE = 0, AXE = 1, SPEAR = 2, CLAWS = 3, CROSSBOW = 4, TORCH = 5, FLAIL = 6, SICKLE = 7, STAFF = 8;
    public static final int WEAPONS = 9;

    /** Client cue length, matched to the staff morph's 8-tick dissolve and 8-tick form. */
    public static final int VISUAL_TICKS = 24;
    /** Blood leaves the forearm and coils at the palm while the old form dissolves. */
    public static final int GATHER_END = 5;
    /** The blood is pulled out along the weapon's outline. */
    public static final int SKETCH_END = 11;
    /** The outline turns from fresh blood to the weapon's school. */
    public static final int HARDEN_END = 15;
    /** The hardened outline is drawn down into the grip as the real item forms. */
    public static final int SEAT_END = 18;

    /** Which material a weapon hardens into on the client. */
    public enum School { ANIMUS, FERRIC, LUX, UMBRA, DUCTILIS, FLAME, CRUOR, MORTEM }

    /** One polyline of a weapon's outline: points as (a, b) pairs and a half-width, all in blocks. */
    public record Stroke(double[] points, double width, boolean edge) {
        public int size() {
            return points.length / 2;
        }
    }

    private static final Stroke[][] SHAPES = {
            // Blade: a grip, a crossguard and a long tapering blade.
            {s(.05, -.15, 0, .9, 0), s(.035, .05, -.17, .05, .17), e(.012, .12, .045, .5, .04, .86, .006)},
            // Axe: a haft with a bearded crescent head and a back spike.
            {s(.03, -.25, 0, .8, 0), e(.04, .5, .04, .58, .22, .7, .3, .82, .24, .86, .05),
                    s(.025, .68, 0, .72, -.16)},
            // Spear: a long shaft and a leaf head.
            {s(.028, -.45, 0, 1.0, 0), e(.03, .98, 0, 1.08, .06, 1.28, 0), e(.03, .98, 0, 1.08, -.06, 1.28, 0)},
            // Claws: four hooked blades rising off the knuckles.
            {e(.022, 0, -.075, .16, -.09, .3, -.06), e(.024, 0, -.025, .18, -.03, .34, 0),
                    e(.024, 0, .025, .18, .03, .34, .04), e(.022, 0, .075, .16, .09, .3, .1)},
            // Crossbow: a stock, a bowed prod and its string.
            {s(.035, -.28, 0, .36, 0), e(.03, .26, -.34, .34, -.17, .38, 0, .34, .17, .26, .34),
                    s(.008, .26, -.34, .1, 0, .26, .34)},
            // Torch: a short haft that the flame finishes.
            {s(.035, -.12, 0, .42, 0), e(.05, .38, -.05, .44, 0, .38, .05)},
            // Flail: a handle; the chain and head hang from its tip (drawn by the geometry).
            {s(.032, -.15, 0, .32, 0), s(.02, .3, -.05, .34, .05)},
            // Sickle: a handle and a hooked crescent.
            {s(.03, -.15, 0, .26, 0), e(.035, .26, 0, .42, .12, .45, .3, .36, .43, .2, .47)},
            // Staff: a long shaft with a crook at its head.
            {s(.035, -.75, 0, .9, 0), e(.03, .9, 0, 1.0, .06, 1.02, .14, .95, .18)},
    };

    private WeaponConjureRules() {
    }

    /** Weapon index for a staff form name; anything unknown is the staff itself. */
    public static int weapon(String formName) {
        return switch (formName == null ? "" : formName) {
            case "conjure_blade" -> BLADE;
            case "conjure_axe" -> AXE;
            case "conjure_spear" -> SPEAR;
            case "conjure_claws" -> CLAWS;
            case "conjure_crossbow" -> CROSSBOW;
            case "conjure_torch" -> TORCH;
            case "conjure_flail" -> FLAIL;
            case "conjure_sickle" -> SICKLE;
            default -> STAFF;
        };
    }

    public static School school(int weapon) {
        return switch (weapon) {
            case AXE, STAFF -> School.FERRIC;
            case SPEAR -> School.LUX;
            case CLAWS -> School.UMBRA;
            case CROSSBOW -> School.DUCTILIS;
            case TORCH -> School.FLAME;
            case FLAIL -> School.CRUOR;
            case SICKLE -> School.MORTEM;
            default -> School.ANIMUS;
        };
    }

    /** Packs the weapon and a per-cast variation into a packet count that is always at least 1. */
    public static int encode(int weapon, int variant) {
        return 1 + Math.floorMod(weapon, WEAPONS) + WEAPONS * Math.floorMod(variant, 8);
    }

    public static int decodeWeapon(int count) {
        return Math.floorMod(Math.max(1, count) - 1, WEAPONS);
    }

    public static int decodeVariant(int count) {
        return Math.floorMod((Math.max(1, count) - 1) / WEAPONS, 8);
    }

    public static Stroke[] strokes(int weapon) {
        return SHAPES[Math.floorMod(weapon, WEAPONS)];
    }

    /** Total outline length, used to pace the sketch so every weapon draws in the same time. */
    public static double length(int weapon) {
        double total = 0;
        for (Stroke stroke : strokes(weapon))
            for (int i = 1; i < stroke.size(); i++)
                total += Math.hypot(stroke.points[i * 2] - stroke.points[i * 2 - 2], stroke.points[i * 2 + 1] - stroke.points[i * 2 - 1]);
        return total;
    }

    /** 0 → 1 as blood coils at the palm. */
    public static double gather(double age) {
        return smooth(age / GATHER_END);
    }

    /**
     * How much of stroke {@code index} of {@code count} is drawn: strokes start one after another through the sketch,
     * each easing out from its first point.
     */
    public static double sketch(double age, int index, int count) {
        double span = SKETCH_END - GATHER_END + 1;
        double start = GATHER_END - 1 + span * 0.45 * index / Math.max(1, count - 1);
        double t = (age - start) / (span * 0.55);
        return t >= 1 ? 1 : t <= 0 ? 0 : 1 - Math.pow(1 - t, 3);
    }

    /** How far (0 grip, 1 far end) blood has spread over a held weapon, evenly through the sketch. */
    public static double spread(double age) {
        return smooth((age - GATHER_END + 1) / (SKETCH_END - GATHER_END + 1));
    }

    /** 0 fresh blood → 1 the weapon's school. */
    public static double harden(double age) {
        return smooth((age - SKETCH_END + 1) / (HARDEN_END - SKETCH_END + 1));
    }

    /** Fraction of the outline's full size while it seats into the grip: 1 held in the air, 0.25 seated. */
    public static double seat(double age) {
        double t = smooth((age - HARDEN_END) / (SEAT_END - HARDEN_END));
        return 1 - 0.75 * t;
    }

    /** 0 → 1 as the seated outline frays into falling drops. */
    public static double fray(double age) {
        return smooth((age - SEAT_END) / (VISUAL_TICKS - SEAT_END));
    }

    private static Stroke s(double width, double... points) {
        return new Stroke(points, width, false);
    }

    /** A cutting edge: it gets the pale core line once hardened. */
    private static Stroke e(double width, double... points) {
        return new Stroke(points, width, true);
    }

    static double smooth(double t) {
        double c = Math.max(0, Math.min(1, t));
        return c * c * (3 - 2 * c);
    }
}
