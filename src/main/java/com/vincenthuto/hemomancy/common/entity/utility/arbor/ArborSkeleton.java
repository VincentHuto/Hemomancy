package com.vincenthuto.hemomancy.common.entity.utility.arbor;

import java.util.ArrayList;
import java.util.List;

/**
 * Minecraft-independent branch skeleton shared by the Arbor's mesh and its fruit layout. The six family
 * boughs spring from the top of the heartwood: each wraps a quarter turn inside the trunk, peels away in
 * the crown and then follows one continuous willow curve, racing outward from its crest while it falls
 * ever faster into a hanging curtain. Fruit stations sit a fixed distance apart along that curve, one per
 * Degree, and each spur's fruit hangs from its tip. The crown grows with the tree until Degree 7.
 * Nothing depends on chamber size.
 */
public final class ArborSkeleton {
    public static final int FAMILY_COUNT = 6;
    public static final int MAX_WHORL = 7;
    /** Heartwood radius around peel height; boughs start inside it. */
    public static final double TRUNK_SURFACE_RADIUS = .45;
    /** Fraction of the tree height spanned by the trunk tube; everything above it is crown. */
    public static final double TRUNK_SPAN = .82;

    // Peel heights as fractions of the trunk top; neighbours alternate low/high so boughs never leave side by side.
    private static final double[] PEEL_FRACTIONS = {.86, .95, .88, .97, .90, .99};
    private static final double CREST_RISE = .5;
    private static final double CREST_RADIUS = .75;
    private static final double CREST_REACH = .6;
    // The weeping curve: how far it reaches past the crest, where it ends, and how sharply its fall accelerates.
    private static final double CURVE_REACH = 3.4;
    private static final double CURVE_END_HEIGHT = 1.5;
    private static final double CURVE_FALL_POWER = 2.2;
    // The boughs turn this far around the trunk over their whole length, so the crown spirals gently.
    private static final double CURVE_SWIRL = 1.1;
    // The first station sits this fraction along the curve; later ones follow at a fixed spacing.
    private static final double FIRST_STATION_FRACTION = .2;
    private static final double STATION_SPACING = 1.0;
    private static final double LAST_STATION_FRACTION = .97;
    private static final int CURVE_SAMPLES = 400;
    private static final double TIP_STEP = .07;
    private static final double SPUR_LENGTH = .6;
    private static final double SPUR_LIFT = .08;
    private static final double FRUIT_SPACING = .52;
    private static final double MIN_CLUSTER_RADIUS = .36;
    private static final double FRUIT_DROP = .52;
    /** Curve parameter of every station, by family, Degree (0-8) and whorl; precomputed since layout runs every frame. */
    private static final double[][][] STATION_PARAMETERS = buildStationParameters();

    private ArborSkeleton() { }

    public static double familyAngle(int family) {
        return family * (Math.PI * 2.0 / FAMILY_COUNT) - Math.PI;
    }

    /** How far the crown has grown toward its full size; it stops growing at Degree 7. */
    public static double crownScale(int degree) {
        return Math.min(1.0, ArborOfWillVisualRules.treeHeight(Math.max(1, degree))
                / ArborOfWillVisualRules.treeHeight(MAX_WHORL));
    }

    public static double trunkTop(int degree) {
        return TRUNK_SPAN * ArborOfWillVisualRules.treeHeight(MAX_WHORL) * crownScale(degree);
    }

    public static double peelHeight(int family, int degree) {
        return PEEL_FRACTIONS[Math.floorMod(family, FAMILY_COUNT)] * trunkTop(degree);
    }

    /** Highest point of a bough, just outside the trunk, where its weeping curve begins. */
    public static ArborCanopyGeometry.Point crest(int family, int degree) {
        return curvePoint(family, degree, 0);
    }

    /** Point on a bough's weeping curve; {@code u} runs from the crest (0) to the hanging end (1). */
    public static ArborCanopyGeometry.Point curvePoint(int family, int degree, double u) {
        double scale = crownScale(degree);
        double t = Math.max(0, Math.min(1, u));
        double crestRadius = CREST_RADIUS + CREST_REACH * scale;
        double crestHeight = peelHeight(family, degree) + CREST_RISE * scale;
        double radius = crestRadius + CURVE_REACH * scale * (1 - (1 - t) * (1 - t));
        double height = crestHeight - (crestHeight - CURVE_END_HEIGHT) * Math.pow(t, CURVE_FALL_POWER);
        return polar(familyAngle(family) + CURVE_SWIRL * t, radius, height);
    }

    public static ArborCanopyGeometry.Point station(int family, int whorl, int degree) {
        return curvePoint(family, degree, stationParameter(family, whorl, degree));
    }

    /** Bough centreline from inside the heartwood over its crest and down the weeping curve past its last visible station. */
    public static List<ArborCanopyGeometry.Point> bough(int family, int visibleWhorls, int degree) {
        int whorls = clampWhorl(visibleWhorls);
        double angle = familyAngle(family);
        double peel = peelHeight(family, degree);
        double scale = crownScale(degree);
        List<ArborCanopyGeometry.Point> result = new ArrayList<>(24);
        result.add(polar(angle - Math.PI * .5, .16, peel - .9 * scale));
        result.add(polar(angle - Math.PI * .25, .30, peel - .45 * scale));
        result.add(polar(angle, .42, peel));
        result.add(crest(family, degree));
        double previous = 0;
        for (int whorl = 1; whorl <= whorls; whorl++) {
            double u = stationParameter(family, whorl, degree);
            // Fill long gaps (mostly the arch before the first station) so the curve stays round.
            int fill = (int) Math.ceil((u - previous) / .12) - 1;
            for (int k = 1; k <= fill; k++) result.add(curvePoint(family, degree, previous + (u - previous) * k / (fill + 1)));
            result.add(curvePoint(family, degree, u));
            previous = u;
        }
        result.add(tip(family, degree, previous));
        return List.copyOf(result);
    }

    /** Where leafy streamers hang along a bough: through the arch, between stations and from the tip. */
    public static List<ArborCanopyGeometry.Point> streamerAnchors(int family, int visibleWhorls, int degree) {
        int whorls = clampWhorl(visibleWhorls);
        List<ArborCanopyGeometry.Point> anchors = new ArrayList<>();
        double first = stationParameter(family, 1, degree);
        anchors.add(curvePoint(family, degree, first * .45));
        anchors.add(curvePoint(family, degree, first * .8));
        for (int whorl = 1; whorl < whorls; whorl++) {
            double mid = (stationParameter(family, whorl, degree) + stationParameter(family, whorl + 1, degree)) * .5;
            anchors.add(curvePoint(family, degree, mid));
        }
        anchors.add(tip(family, degree, stationParameter(family, whorls, degree)));
        return List.copyOf(anchors);
    }

    /** Inner spurs point outward so clusters near the trunk do not meet; outer ones alternate sides. */
    public static ArborCanopyGeometry.Point spurTip(int family, int whorl, int degree) {
        int w = clampWhorl(whorl);
        ArborCanopyGeometry.Point station = station(family, w, degree);
        double angle = Math.atan2(station.z(), station.x());
        double side = (family + w) % 2 == 0 ? 1 : -1;
        double sweep = w == 1 ? .35 : w == 2 ? .6 : .9;
        double dx = Math.cos(angle) * Math.cos(sweep) - Math.sin(angle) * Math.sin(sweep) * side;
        double dz = Math.sin(angle) * Math.cos(sweep) + Math.cos(angle) * Math.sin(sweep) * side;
        return new ArborCanopyGeometry.Point(station.x() + dx * SPUR_LENGTH, station.y() + SPUR_LIFT,
                station.z() + dz * SPUR_LENGTH);
    }

    /** Fruit hangs in a ring under the spur tip; larger families widen the ring rather than crowd it. */
    public static ArborCanopyGeometry.Point fruitAnchor(int family, int whorl, int slot, int count, int degree) {
        ArborCanopyGeometry.Point tip = spurTip(family, whorl, degree);
        int total = Math.max(1, count);
        if (total == 1) return new ArborCanopyGeometry.Point(tip.x(), tip.y() - FRUIT_DROP, tip.z());
        double ring = Math.max(MIN_CLUSTER_RADIUS, total * FRUIT_SPACING / (Math.PI * 2.0));
        // Half-slot phase keeps every fruit off the bough line, away from the neighbouring stations.
        double angle = Math.atan2(tip.z(), tip.x()) + (slot + .5) * Math.PI * 2.0 / total;
        return new ArborCanopyGeometry.Point(tip.x() + Math.cos(angle) * ring, tip.y() - FRUIT_DROP,
                tip.z() + Math.sin(angle) * ring);
    }

    private static ArborCanopyGeometry.Point tip(int family, int degree, double lastStation) {
        if (lastStation + TIP_STEP <= 1) return curvePoint(family, degree, lastStation + TIP_STEP);
        return curvePoint(family, degree, 1).add(new ArborCanopyGeometry.Point(0, -.3 * crownScale(degree), 0));
    }

    /** Position of a station along its bough's weeping curve, from the crest (0) to the hanging end (1). */
    public static double stationParameter(int family, int whorl, int degree) {
        return STATION_PARAMETERS[Math.floorMod(family, FAMILY_COUNT)][Math.max(0, Math.min(8, degree))][clampWhorl(whorl) - 1];
    }

    /** Places stations at even arc-length spacing along each curve, so the hanging part is not crowded. */
    private static double[][][] buildStationParameters() {
        double[][][] result = new double[FAMILY_COUNT][9][MAX_WHORL];
        for (int family = 0; family < FAMILY_COUNT; family++) {
            for (int degree = 0; degree <= 8; degree++) {
                double[] length = new double[CURVE_SAMPLES + 1];
                ArborCanopyGeometry.Point previous = curvePoint(family, degree, 0);
                for (int i = 1; i <= CURVE_SAMPLES; i++) {
                    ArborCanopyGeometry.Point next = curvePoint(family, degree, i / (double) CURVE_SAMPLES);
                    length[i] = length[i - 1] + previous.distanceTo(next);
                    previous = next;
                }
                double total = length[CURVE_SAMPLES];
                for (int whorl = 1; whorl <= MAX_WHORL; whorl++) {
                    double target = Math.min(LAST_STATION_FRACTION * total,
                            FIRST_STATION_FRACTION * total + (whorl - 1) * STATION_SPACING);
                    int i = 0;
                    while (i < CURVE_SAMPLES && length[i] < target) i++;
                    result[family][degree][whorl - 1] = i / (double) CURVE_SAMPLES;
                }
            }
        }
        return result;
    }

    /** Curved stem from a spur tip out and down into the fruit's crown. */
    public static List<ArborCanopyGeometry.Point> fruitStem(ArborCanopyGeometry.Point spurTip,
            ArborCanopyGeometry.Point fruit) {
        ArborCanopyGeometry.Point c1 = new ArborCanopyGeometry.Point(
                spurTip.x() + (fruit.x() - spurTip.x()) * .6, spurTip.y() + .05,
                spurTip.z() + (fruit.z() - spurTip.z()) * .6);
        ArborCanopyGeometry.Point c2 = new ArborCanopyGeometry.Point(fruit.x(),
                fruit.y() + (spurTip.y() - fruit.y()) * .6, fruit.z());
        List<ArborCanopyGeometry.Point> result = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) result.add(cubic(spurTip, c1, c2, fruit, i / 5.0));
        return List.copyOf(result);
    }

    public static int clampWhorl(int whorl) {
        return Math.max(1, Math.min(MAX_WHORL, whorl));
    }

    private static ArborCanopyGeometry.Point polar(double angle, double radius, double height) {
        return new ArborCanopyGeometry.Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }

    private static ArborCanopyGeometry.Point cubic(ArborCanopyGeometry.Point a, ArborCanopyGeometry.Point b,
            ArborCanopyGeometry.Point c, ArborCanopyGeometry.Point d, double t) {
        double u = 1.0 - t;
        double ka = u * u * u, kb = 3 * u * u * t, kc = 3 * u * t * t, kd = t * t * t;
        return new ArborCanopyGeometry.Point(ka * a.x() + kb * b.x() + kc * c.x() + kd * d.x(),
                ka * a.y() + kb * b.y() + kc * c.y() + kd * d.y(),
                ka * a.z() + kb * b.z() + kc * c.z() + kd * d.z());
    }
}
