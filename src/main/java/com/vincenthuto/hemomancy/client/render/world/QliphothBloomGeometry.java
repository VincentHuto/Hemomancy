package com.vincenthuto.hemomancy.client.render.world;

import java.util.ArrayList;
import java.util.List;

/** Deterministic authored geometry for the Qliphoth Bloom renderer. */
public final class QliphothBloomGeometry {
    public static final double HEIGHT = 8.0;
    private static final StageSnapshot[] SNAPSHOTS = new StageSnapshot[10];
    private QliphothBloomGeometry() {}

    public static StageSnapshot snapshot(int stage) {
        int clamped = clamp(stage, 0, 9);
        StageSnapshot cached = SNAPSHOTS[clamped];
        if (cached != null) return cached;
        StageSnapshot created = new StageSnapshot(clamped, trunk(clamped, 0.0), roots(clamped, 0.0),
                mainBranches(clamped, 0.0), secondaryBranches(clamped, 0.0),
                clamped >= 9 ? crownProngs(0.0) : List.of());
        SNAPSHOTS[clamped] = created;
        return created;
    }

    public static double trunkFraction(int stage) {
        return stage >= 6 ? 1 : .25 + clamp(stage, 0, 5) * .15;
    }

    public static double rootFraction(int stage) {
        return stage >= 5 ? 1 : .15 + clamp(stage, 0, 4) * .2125;
    }

    public static double branchFraction(int stage) {
        return stage < 6 ? 0 : stage == 6 ? .4 : stage == 7 ? .7 : 1;
    }

    public static boolean hasApex(int stage) { return stage >= 9; }

    public static Limb trunk(int stage, double time) {
        double fraction = trunkFraction(stage);
        List<Point> rest = List.of(new Point(0, -.08, 0), new Point(.05, 1.15, -.04),
                new Point(-.09, 2.45, .06), new Point(.12, 3.85, .02),
                new Point(-.08, 5.25, -.08), new Point(.07, 6.55, .05), new Point(0, stage >= 9 ? 7.5 : 8, 0));
        List<Point> path = truncate(rest, fraction);
        path = bevel(animate(path, 91, time, .075, false), .16);
        List<Double> radii = profile(path.size(), .62, stage >= 6 ? .28 : .12, false);
        if (stage >= 9) radii.set(radii.size() - 1, 0.0);
        return new Limb(path, radii, 91);
    }

    public static List<Limb> roots(int stage, double time) {
        double growth = rootFraction(stage);
        List<Limb> result = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI * 2 / 8;
            double turn = (hash(i + 40) - .5) * 1.05;
            double length = (2.35 + hash(i + 4) * 1.65) * growth;
            int joints = 5 + (int)Math.floor(hash(i + 876) * 3);
            List<Point> path = new ArrayList<>();
            path.add(new Point(Math.cos(a) * .32, .06, Math.sin(a) * .32));
            double initialSide = hash(i + 945) < .5 ? -1 : 1;
            int forcedReversal = 2 + (int)Math.floor(hash(i + 1111) * Math.max(1, joints - 2));
            double side = initialSide;
            for (int joint = 1; joint <= joints; joint++) {
                double t = joint / (double)joints;
                if (joint == forcedReversal || hash(i * 31 + joint + 901) < .34) side *= -1;
                double lateralSlash = side * (.17 + hash(i * 19 + joint + 971) * .46);
                double angle = a + turn * t + lateralSlash;
                double radius = .32 + (length - .32) * Math.pow(t, .86 + hash(i + 990) * .24);
                double y = -.012 - t * (.12 + hash(i + 1004) * .15)
                        + (hash(i * 23 + joint + 1017) - .5) * .13;
                path.add(new Point(Math.cos(angle) * radius, y, Math.sin(angle) * radius));
            }
            path = bevel(animate(path, 200 + i, time, .15, true), .13);
            result.add(new Limb(path, profile(path.size(), .31, 0, true), 200 + i));
        }
        return result;
    }

    public static List<Limb> mainBranches(int stage, double time) {
        double growth = branchFraction(stage);
        if (growth == 0) return List.of();
        List<Limb> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            double a = i * Math.PI * 2 / 7 + .23;
            double y = 3.0 + i * .48;
            double length = (3.8 + hash(i + 61) * 2.5) * growth;
            double bend = (hash(i + 80) - .5) * .72;
            List<Point> path = new ArrayList<>();
            path.add(new Point(Math.cos(a) * .25, y, Math.sin(a) * .25));
            path.add(new Point(Math.cos(a) * .72, y + .22, Math.sin(a) * .72));
            path.add(new Point(Math.cos(a + bend * .25) * length * .42, y + .82 + length * .10,
                    Math.sin(a + bend * .25) * length * .42));
            path.add(new Point(Math.cos(a - bend * .25) * length * .72, y + 1.25 + length * .17,
                    Math.sin(a - bend * .25) * length * .72));
            path.add(new Point(Math.cos(a + bend) * length, y + 1.75 + length * .22,
                    Math.sin(a + bend) * length));
            path = bevel(animate(path, 300 + i, time, .16, true), .18);
            result.add(new Limb(path, profile(path.size(), .25, 0, true), 300 + i));
        }
        return result;
    }

    public static List<Limb> secondaryBranches(int stage, double time) {
        if (stage < 7) return List.of();
        List<Limb> result = new ArrayList<>();
        for (Limb parent : mainBranches(stage, time)) {
            int i = parent.seed() - 300;
            Point joint = parent.points().get(2);
            Point direction = parent.points().get(3).subtract(parent.points().get(2)).normalized();
            Point side = new Point(-direction.z, .28, direction.x).normalized();
            double sign = (i & 1) == 0 ? 1 : -1;
            List<Point> path = List.of(joint,
                    joint.add(direction.scale(.38)).add(side.scale(.24 * sign)),
                    joint.add(direction.scale(1.05)).add(side.scale(.88 * sign)).add(new Point(0, .42, 0)),
                    joint.add(direction.scale(1.55)).add(side.scale(1.28 * sign)).add(new Point(0, .72, 0)));
            path = bevel(path, .18);
            result.add(new Limb(path, profile(path.size(), .12, 0, true), 400 + i));
        }
        return result;
    }

    public static List<Pome> pomes(int stage, double time) {
        List<Pome> result = new ArrayList<>();
        for (int i = 0; i < clamp(stage, 0, 9); i++) {
            double y = 1.25 + i * .58;
            double angle = i * 2.399963 + .5;
            double r = .43 - i * .015;
            result.add(new Pome(new Point(Math.cos(angle) * r, y, Math.sin(angle) * r),
                    .17 + (i % 3) * .018, angle,
                    .76 + hash(i + 731) * .48, .82 + hash(i + 751) * .55,
                    hash(i + 771) * Math.PI * 2));
        }
        return result;
    }

    public static List<Crystal> crystals(int stage, double time) {
        int count = stage < 7 ? 0 : stage == 7 ? 3 : stage == 8 ? 6 : 9;
        List<Crystal> result = new ArrayList<>();
        List<Limb> branches = snapshot(Math.max(8, stage)).mainBranches();
        for (int i = 0; i < count; i++) {
            Limb branch = branches.get(i % branches.size());
            Point anchor = branch.points().get(branch.points().size() - 1);
            double swing = Math.sin(time * .025 + i * 1.7) * .08;
            double cord = .42 + hash(i + 500) * .48;
            result.add(new Crystal(anchor, anchor.add(new Point(swing, -cord, -swing)),
                    .18 + hash(i + 520) * .09, i, 3 + i % 4,
                    (hash(i + 541) - .5) * .72));
        }
        return result;
    }

    public static Limb fruitStem(Crystal fruit, double time) {
        Point delta = fruit.center.subtract(fruit.anchor);
        Point side = new Point(-delta.z, 0, delta.x).normalized();
        double jerk = Math.sin(time * .035 + fruit.seed * 2.17) * .06;
        List<Point> points = List.of(fruit.anchor,
                fruit.anchor.add(delta.scale(.12)).add(side.scale(.09 + jerk)),
                fruit.anchor.add(delta.scale(.38)).add(side.scale(-.07 - jerk)),
                fruit.anchor.add(delta.scale(.70)).add(side.scale(.05 + jerk * .5)),
                fruit.center);
        return new Limb(points, List.of(.060, .043, .035, .030, .026), 800 + fruit.seed);
    }

    public static List<Double> textureV(Limb limb, double repeatsPerBlock) {
        List<Double> result = new ArrayList<>();
        double distance = 0;
        result.add(0.0);
        for (int i = 1; i < limb.points.size(); i++) {
            distance += limb.points.get(i).distanceTo(limb.points.get(i - 1));
            result.add(distance * repeatsPerBlock);
        }
        return result;
    }

    public static List<Limb> crownProngs(double time) {
        List<Limb> result = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            double a = i * Math.PI * 2 / 5;
            List<Point> path = List.of(new Point(Math.cos(a) * .20, 6.25, Math.sin(a) * .20),
                    new Point(Math.cos(a) * .52, 7.15, Math.sin(a) * .52),
                    new Point(Math.cos(a) * .76, 7.5, Math.sin(a) * .76));
            path = bevel(animate(path, 600 + i, time, .07, false), .16);
            result.add(new Limb(path, profile(path.size(), .19, 0, true), 600 + i));
        }
        return result;
    }

    public static Point apexCenter(double time) {
        return new Point(Math.sin(time * .018) * .035, 8.05 + Math.sin(time * .03) * .045,
                Math.cos(time * .021) * .035);
    }

    public static List<Limb> apexLoops(double time) {
        List<Limb> result = new ArrayList<>();
        Point center = apexCenter(time);
        for (int ring = 0; ring < 4; ring++) {
            List<Point> points = new ArrayList<>();
            double tilt = .35 + ring * .38;
            double yaw = time * (.012 + ring * .003) + ring * 1.31;
            int segments = 32;
            for (int s = 0; s <= segments; s++) {
                double a = s * Math.PI * 2 / segments;
                double swim = time * (.052 + ring * .006);
                double radius = .92 + ring * .16
                        + Math.sin(a * (3 + ring % 2) + swim + ring) * .105
                        + Math.sin(a * 7 - swim * 1.37 + ring * .7) * .038;
                double x = Math.cos(a) * radius;
                double z = Math.sin(a) * radius;
                double y = -z * Math.sin(tilt) + Math.sin(a * 5 + swim * 1.8 + ring) * .075;
                z *= Math.cos(tilt);
                points.add(center.add(new Point(x * Math.cos(yaw) - z * Math.sin(yaw), y,
                        x * Math.sin(yaw) + z * Math.cos(yaw))));
            }
            points.set(points.size() - 1, points.get(0));
            result.add(new Limb(points, constant(points.size(), .014 + ring * .0035), 700 + ring));
        }
        return result;
    }

    public static List<Point> surfaceVein(Limb limb, int index) {
        List<Point> result = new ArrayList<>();
        for (int i = 0; i < limb.points.size(); i++) {
            Point p = limb.points.get(i);
            Point tangent = limb.points.get(Math.min(i + 1, limb.points.size() - 1))
                    .subtract(limb.points.get(Math.max(0, i - 1))).normalized();
            Point ref = Math.abs(tangent.y) > .9 ? new Point(1, 0, 0) : new Point(0, 1, 0);
            Point n = tangent.cross(ref).normalized();
            Point b = tangent.cross(n).normalized();
            double angle = index * 2.1 + i * .72;
            double offset = limb.radii.get(i) + .018;
            result.add(p.add(n.scale(Math.cos(angle) * offset)).add(b.scale(Math.sin(angle) * offset)));
        }
        return result;
    }

    private static List<Point> animate(List<Point> rest, int seed, double time, double maximum, boolean fixStart) {
        List<Point> result = new ArrayList<>(rest.size());
        double twitchWave = Math.sin(time * .173 + seed * 7.13);
        double twitchGate = Math.pow(Math.max(0, twitchWave - .82) / .18, 3);
        for (int i = 0; i < rest.size(); i++) {
            double t = i / (double) Math.max(1, rest.size() - 1);
            if (fixStart && i == 0) { result.add(rest.get(i)); continue; }
            double amp = maximum * t;
            double x = Math.sin(time * .021 + seed * .91 + t * 4.2) * amp;
            double z = Math.cos(time * .018 + seed * 1.17 + t * 3.6) * amp;
            x += twitchGate * maximum * .42 * Math.sin(seed * 3.7);
            z += twitchGate * maximum * .42 * Math.cos(seed * 2.9);
            result.add(rest.get(i).add(new Point(x, Math.sin(time * .016 + seed + t * 2) * amp * .22, z)));
        }
        return result;
    }

    private static List<Point> bevel(List<Point> path, double fraction) {
        if (path.size() < 3) return path;
        List<Point> result = new ArrayList<>();
        result.add(path.get(0));
        for (int i = 1; i < path.size() - 1; i++) {
            Point corner = path.get(i);
            result.add(corner.lerp(path.get(i - 1), fraction));
            result.add(corner.lerp(path.get(i + 1), fraction));
        }
        result.add(path.get(path.size() - 1));
        return result;
    }

    private static List<Point> truncate(List<Point> path, double fraction) {
        double scaled = fraction * (path.size() - 1);
        int full = Math.min((int) scaled, path.size() - 1);
        List<Point> result = new ArrayList<>(path.subList(0, full + 1));
        if (full < path.size() - 1) result.add(path.get(full).lerp(path.get(full + 1), scaled - full));
        return result;
    }

    private static List<Double> profile(int size, double start, double end, boolean pointed) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double t = i / (double) Math.max(1, size - 1);
            double r = start + (end - start) * t;
            if (pointed) r *= (1 - t) * (1 - .25 * t);
            result.add(Math.max(0, r));
        }
        return result;
    }

    private static List<Double> constant(int size, double value) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < size; i++) result.add(value);
        return result;
    }

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    private static double hash(int i) { double x = Math.sin(i * 127.1 + 311.7) * 43758.5453; return x - Math.floor(x); }

    public record Limb(List<Point> points, List<Double> radii, int seed) {
        public Limb {
            points = List.copyOf(points);
            radii = List.copyOf(radii);
        }
    }
    public record StageSnapshot(int stage, Limb trunk, List<Limb> roots, List<Limb> mainBranches,
                                List<Limb> secondaryBranches, List<Limb> crownProngs) {
        public StageSnapshot {
            roots = List.copyOf(roots);
            mainBranches = List.copyOf(mainBranches);
            secondaryBranches = List.copyOf(secondaryBranches);
            crownProngs = List.copyOf(crownProngs);
        }
    }
    public record Pome(Point center, double radius, double angle, double aspectX, double aspectY,
                       double wispPhase) {}
    public record Crystal(Point anchor, Point center, double size, int seed, int lobes, double skew) {}
    public record Point(double x, double y, double z) {
        public Point add(Point p) { return new Point(x + p.x, y + p.y, z + p.z); }
        public Point subtract(Point p) { return new Point(x - p.x, y - p.y, z - p.z); }
        public Point scale(double s) { return new Point(x * s, y * s, z * s); }
        public double length() { return Math.sqrt(x * x + y * y + z * z); }
        public Point normalized() { double l = length(); return l < 1e-9 ? new Point(0, 1, 0) : scale(1 / l); }
        public Point cross(Point p) { return new Point(y * p.z - z * p.y, z * p.x - x * p.z, x * p.y - y * p.x); }
        public double distanceTo(Point p) { return subtract(p).length(); }
        public double radialDistance() { return Math.sqrt(x * x + z * z); }
        public Point lerp(Point p, double t) { return new Point(x + (p.x - x) * t, y + (p.y - y) * t, z + (p.z - z) * t); }
    }
}
