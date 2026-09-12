package com.vincenthuto.hemomancy.client.screen.radial;

import java.util.ArrayList;
import java.util.List;

final class RadialVeinBorderGeometry {
    record Point(float angle, float radius) {
        float x() { return (float)Math.cos(angle) * radius; }
        float y() { return (float)Math.sin(angle) * radius; }
    }

    record VeinPath(List<Point> points, boolean closed, float strength) {}

    private RadialVeinBorderGeometry() {}

    static List<VeinPath> paths(float start, float end, float inner, float outer, int seed, double time) {
        float inset = Math.min(1.2f, (outer - inner) * .2f);
        float first = start + Math.min(.012f, (end - start) * .05f);
        float last = end - Math.min(.012f, (end - start) * .05f);
        float innerEdge = inner + inset, outerEdge = outer - inset;
        float wave = Math.min(.5f, (outer - inner) * .05f);
        List<Point> perimeter = new ArrayList<>();
        traceArc(perimeter, first, last, outerEdge, -wave, seed, time);
        traceEdge(perimeter, last, outerEdge, innerEdge, -1, seed + 13, time);
        traceArc(perimeter, last, first, innerEdge, wave, seed + 19, time);
        traceEdge(perimeter, first, innerEdge, outerEdge, 1, seed + 37, time);
        // Corner cutting rounds the whole loop, including the four arc-to-edge transitions.
        perimeter = smoothLoop(smoothLoop(perimeter));
        List<VeinPath> result = new ArrayList<>();
        result.add(new VeinPath(perimeter, true, .9f));
        for (int i = 1; i <= 3; i++) {
            boolean fromInner = i % 2 == 0;
            float angle = lerp(first, last, i / 4f);
            Point root = closest(perimeter, new Point(angle, fromInner ? innerEdge : outerEdge));
            float reach = Math.min(6.5f, (outer - inner) * .3f);
            float bend = (fromInner ? -1 : 1) * reach * .5f / Math.max(1, root.radius());
            Point control = new Point(clamp(root.angle() + bend, first, last), root.radius());
            Point tip = new Point(control.angle(), root.radius() + (fromInner ? reach : -reach));
            List<Point> branch = new ArrayList<>();
            for (int step = 0; step <= 12; step++) {
                float t = step / 12f;
                branch.add(mix(mix(root, control, t), mix(control, tip, t), t));
            }
            result.add(new VeinPath(branch, false, .6f));
        }
        return result;
    }

    private static void traceArc(List<Point> points, float start, float end, float radius,
            float wave, int seed, double time) {
        int steps = Math.max(8, (int)Math.ceil(Math.abs(end - start) * radius / 5));
        for (int i = 0; i < steps; i++) {
            float t = i / (float)steps;
            float angle = lerp(start, end, t);
            float drift = (float)(Math.sin(t * Math.PI)
                    * (.5 + .5 * Math.sin(angle * radius * .17 + seed + time * .08)));
            points.add(new Point(angle, radius + drift * wave));
        }
    }

    private static void traceEdge(List<Point> points, float angle, float from, float to,
            int direction, int seed, double time) {
        int steps = Math.max(4, (int)Math.ceil(Math.abs(to - from) / 5));
        for (int i = 0; i < steps; i++) {
            float t = i / (float)steps;
            float drift = (float)(Math.sin(t * Math.PI)
                    * (.6 + .4 * Math.sin(t * Math.PI * 2 + seed + time * .08)));
            points.add(new Point(angle + direction * drift * .008f, lerp(from, to, t)));
        }
    }

    private static List<Point> smoothLoop(List<Point> points) {
        List<Point> smooth = new ArrayList<>(points.size() * 2);
        for (int i = 0; i < points.size(); i++) {
            Point from = points.get(i), to = points.get((i + 1) % points.size());
            smooth.add(mix(from, to, .25f));
            smooth.add(mix(from, to, .75f));
        }
        return smooth;
    }

    private static Point closest(List<Point> points, Point target) {
        Point closest = points.getFirst();
        double distance = Double.MAX_VALUE;
        for (Point point : points) {
            double squared = Math.pow(point.x() - target.x(), 2) + Math.pow(point.y() - target.y(), 2);
            if (squared < distance) {
                distance = squared;
                closest = point;
            }
        }
        return closest;
    }

    private static Point mix(Point from, Point to, float t) {
        return new Point(lerp(from.angle(), to.angle(), t), lerp(from.radius(), to.radius(), t));
    }

    private static float lerp(float from, float to, float progress) { return from + (to - from) * progress; }
    private static float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }
}
