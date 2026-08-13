package com.vincenthuto.hemomancy.common.worldgen.arbor;

import java.util.ArrayList;
import java.util.List;

/** Pure curved skeleton shared by rendering and interaction geometry. */
public final class ArborCanopyGeometry {
    private ArborCanopyGeometry() { }

    public static List<Point> familyLimb(int familyIndex, double height, double extent) {
        List<Point> result = new ArrayList<>();
        double baseAngle = familyIndex * Math.PI * 2.0 / 6.0 - Math.PI;
        for (int i = 0; i < 15; i++) {
            double t = i / 14.0;
            double opening = smoothstep(Math.max(0.0, (t - .34) / .66));
            double radius = lerp(.42 + .42 * t, extent, opening);
            double angle = baseAngle + t * 4.05 + Math.sin(t * Math.PI) * .20;
            double y = .24 + height * (.06 + .76 * t) + Math.sin(t * Math.PI) * height * .12;
            result.add(new Point(Math.cos(angle) * radius, y, Math.sin(angle) * radius));
        }
        return List.copyOf(result);
    }

    public static List<Point> hangingStem(Point fruit, double lift, double inward) {
        double radial = Math.max(.001, fruit.radialDistance());
        Point start = new Point(fruit.x - fruit.x / radial * inward, fruit.y + lift,
                fruit.z - fruit.z / radial * inward);
        Point c1 = new Point(start.x * 1.03, start.y + .14, start.z * 1.03);
        Point c2 = new Point(fruit.x * .98, fruit.y + lift * .46, fruit.z * .98);
        List<Point> result = new ArrayList<>();
        for (int i = 0; i < 6; i++) result.add(cubic(start, c1, c2, fruit, i / 5.0));
        return List.copyOf(result);
    }

    public static List<Point> root(int index, double radius) {
        double angle = index * Math.PI * 2.0 / 7.0 + .18 * Math.sin(index * 2.1);
        List<Point> result = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            double t = i / 7.0;
            double bend = angle + .28 * Math.sin(t * Math.PI + index);
            double r = radius * t;
            result.add(new Point(Math.cos(bend) * r, .12 * (1.0 - t) - .045 * t,
                    Math.sin(bend) * r));
        }
        return List.copyOf(result);
    }

    /**
     * Samples a centripetal-looking Catmull-Rom path while retaining the authored
     * control points as exact segment boundaries. Duplicate end controls keep the
     * first and last points fixed, which is important for roots meeting the floor
     * and fruit stems meeting their fruit.
     */
    public static List<Point> smooth(List<Point> path, int subdivisions) {
        if (path.size() < 3 || subdivisions < 2) return List.copyOf(path);
        List<Point> result = new ArrayList<>((path.size() - 1) * subdivisions + 1);
        for (int segment = 0; segment < path.size() - 1; segment++) {
            Point p0 = path.get(Math.max(0, segment - 1));
            Point p1 = path.get(segment);
            Point p2 = path.get(segment + 1);
            Point p3 = path.get(Math.min(path.size() - 1, segment + 2));
            for (int sample = 0; sample < subdivisions; sample++) {
                result.add(catmullRom(p0, p1, p2, p3, sample / (double) subdivisions));
            }
        }
        result.add(path.get(path.size() - 1));
        return List.copyOf(result);
    }

    /** Builds the perimeter used to close a tube perpendicular to its tangent. */
    public static List<Point> capRing(Point center, Point tangent, double radius, int sides) {
        if (sides < 3 || radius <= 0) return List.of();
        Point direction = tangent.normalized();
        Point reference = Math.abs(direction.y) > .92 ? new Point(1, 0, 0) : new Point(0, 1, 0);
        Point n1 = direction.cross(reference).normalized();
        Point n2 = direction.cross(n1).normalized();
        List<Point> result = new ArrayList<>(sides);
        for (int side = 0; side < sides; side++) {
            double angle = side * Math.PI * 2.0 / sides;
            result.add(center.add(n1.scale(Math.cos(angle) * radius)).add(n2.scale(Math.sin(angle) * radius)));
        }
        return List.copyOf(result);
    }

    /**
     * Places a colored cambium cord against the changing outside radius of a
     * branch. A small embed keeps it visually grafted without burying it.
     */
    public static List<Point> surfaceVein(List<Point> path, double branchStartRadius, double branchEndRadius,
            double veinStartRadius, double veinEndRadius, double embed) {
        if (path.isEmpty()) return List.of();
        List<Point> result = new ArrayList<>(path.size());
        for (int i = 0; i < path.size(); i++) {
            double t = path.size() == 1 ? 0 : i / (double)(path.size() - 1);
            double offset = lerp(branchStartRadius, branchEndRadius, t)
                    + lerp(veinStartRadius, veinEndRadius, t) - embed;
            Point point = path.get(i);
            double radial = point.radialDistance();
            double outwardX = radial < 1.0e-6 ? 1 : point.x / radial;
            double outwardZ = radial < 1.0e-6 ? 0 : point.z / radial;
            result.add(new Point(point.x + outwardX * offset, point.y, point.z + outwardZ * offset));
        }
        return List.copyOf(result);
    }

    /** Places a terminal crown just beyond the tube cap along its final tangent. */
    public static Point terminalFoliageCenter(List<Point> path, double forward) {
        if (path.isEmpty()) return new Point(0, 0, 0);
        Point end = path.get(path.size() - 1);
        if (path.size() == 1 || forward <= 0) return end;
        Point tangent = end.subtract(path.get(path.size() - 2)).normalized();
        return end.add(tangent.scale(forward));
    }

    /** Continues a branch into its foliage bud so its wooden end narrows instead of being cut flat. */
    public static List<Point> terminalTaper(List<Point> path, double forward) {
        if (path.size() < 2 || forward <= 0) return List.copyOf(path);
        Point end = path.get(path.size() - 1);
        Point tangent = end.subtract(path.get(path.size() - 2)).normalized();
        List<Point> result = new ArrayList<>(path.size() + 3);
        result.addAll(path);
        result.add(end.add(tangent.scale(forward / 3.0)));
        result.add(end.add(tangent.scale(forward * 2.0 / 3.0)));
        result.add(end.add(tangent.scale(forward)));
        return List.copyOf(result);
    }

    public static float pointedRadius(float progress, float taperStart,
            float startRadius, float branchEndRadius) {
        if (progress <= taperStart || taperStart >= 1.0F) {
            float local = taperStart <= 0 ? 1 : progress / taperStart;
            return startRadius + (branchEndRadius - startRadius) * local;
        }
        float local = (progress - taperStart) / (1.0F - taperStart);
        float incomingSlope = (branchEndRadius - startRadius) / Math.max(taperStart, 1.0e-6F);
        float span = 1.0F - taperStart;
        float tangent = incomingSlope * span;
        float h00 = 2 * local * local * local - 3 * local * local + 1;
        float h10 = local * local * local - 2 * local * local + local;
        return Math.max(0F, h00 * branchEndRadius + h10 * tangent);
    }

    public static float foliageBudRadius(float crownDiameter, float foliage) {
        foliage = Math.max(0F, Math.min(1F, foliage));
        return crownDiameter * (.11666667F + .00833333F * foliage);
    }

    /** Builds a compact rosette of long blades whose bases overlap at the branch. */
    public static List<Leaflet> foliageCrown(Point center, double diameter, double foliage, int seed) {
        if (diameter <= 0) return List.of();
        foliage = Math.max(0.0, Math.min(1.0, foliage));
        int count = 4 + (int)Math.round(foliage * 20.0);
        List<Leaflet> result = new ArrayList<>(count);
        double phase = Math.floorMod(seed, 29) * .173;
        for (int i = 0; i < count; i++) {
            double layer = i / (double)Math.max(1, count - 1);
            double azimuth = phase + i * 2.399963229728653;
            double vertical = i == 0 ? .82 : i == 1 ? -.22 : .48 - layer * .56
                    + .16 * Math.sin(azimuth * 1.7);
            double horizontal = Math.sqrt(Math.max(.05, 1.0 - vertical * vertical));
            Point direction = new Point(Math.cos(azimuth) * horizontal, vertical,
                    Math.sin(azimuth) * horizontal).normalized();

            // Every blade starts in the same woody bud. The tiny offset prevents z-fighting
            // without breaking the crown into the former cloud of detached ellipsoids.
            double baseOffset = diameter * (.025 + .008 * (i % 3));
            Point leafBase = center.add(direction.scale(baseOffset));
            double length = diameter * (.34 + .055 * ((i + Math.floorMod(seed, 7)) % 4));
            double width = length * (.28 + .035 * ((i + seed) & 1));
            double thickness = width * (.12 + .025 * (i % 3));
            double curl = diameter * ((i % 5 - 2) * .075);
            result.add(new Leaflet(leafBase, direction, length, width, thickness, azimuth, curl));
        }
        return List.copyOf(result);
    }

    private static Point catmullRom(Point p0, Point p1, Point p2, Point p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        return new Point(
                .5 * ((2 * p1.x) + (-p0.x + p2.x) * t
                        + (2*p0.x - 5*p1.x + 4*p2.x - p3.x) * t2
                        + (-p0.x + 3*p1.x - 3*p2.x + p3.x) * t3),
                .5 * ((2 * p1.y) + (-p0.y + p2.y) * t
                        + (2*p0.y - 5*p1.y + 4*p2.y - p3.y) * t2
                        + (-p0.y + 3*p1.y - 3*p2.y + p3.y) * t3),
                .5 * ((2 * p1.z) + (-p0.z + p2.z) * t
                        + (2*p0.z - 5*p1.z + 4*p2.z - p3.z) * t2
                        + (-p0.z + 3*p1.z - 3*p2.z + p3.z) * t3));
    }

    private static Point cubic(Point a, Point b, Point c, Point d, double t) {
        double u = 1.0 - t;
        return new Point(u*u*u*a.x + 3*u*u*t*b.x + 3*u*t*t*c.x + t*t*t*d.x,
                u*u*u*a.y + 3*u*u*t*b.y + 3*u*t*t*c.y + t*t*t*d.y,
                u*u*u*a.z + 3*u*u*t*b.z + 3*u*t*t*c.z + t*t*t*d.z);
    }

    private static double smoothstep(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return t * t * (3.0 - 2.0 * t);
    }

    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }

    public record Leaflet(Point center, Point direction, double length, double width,
                          double thickness, double yaw, double curl) { }

    public record Point(double x, double y, double z) {
        public double radialDistance() { return Math.sqrt(x * x + z * z); }
        public double angle() { return Math.atan2(z, x); }
        public double length() { return Math.sqrt(x*x + y*y + z*z); }
        public double dot(Point other) { return x*other.x + y*other.y + z*other.z; }
        public Point add(Point other) { return new Point(x+other.x, y+other.y, z+other.z); }
        public Point subtract(Point other) { return new Point(x-other.x, y-other.y, z-other.z); }
        public Point scale(double amount) { return new Point(x*amount, y*amount, z*amount); }
        public Point normalized() {
            double magnitude = length();
            return magnitude < 1.0e-9 ? new Point(0, 1, 0) : scale(1.0 / magnitude);
        }
        public Point cross(Point other) {
            return new Point(y*other.z-z*other.y, z*other.x-x*other.z, x*other.y-y*other.x);
        }
        public double distanceTo(Point other) {
            double dx=x-other.x, dy=y-other.y, dz=z-other.z;
            return Math.sqrt(dx*dx + dy*dy + dz*dz);
        }
    }
}
