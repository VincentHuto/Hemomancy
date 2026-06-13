package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import java.util.ArrayList;
import java.util.List;

public final class MemoryThreadCurveRules {
    private MemoryThreadCurveRules() {
    }

    public static List<Point> smooth(List<Point> controlPoints, int samplesPerSegment) {
        if (controlPoints.size() < 3 || samplesPerSegment <= 1) {
            return controlPoints;
        }
        List<Point> smoothed = new ArrayList<>((controlPoints.size() - 1) * samplesPerSegment + 1);
        for (int i = 0; i < controlPoints.size() - 1; i++) {
            Point p0 = controlPoints.get(Math.max(0, i - 1));
            Point p1 = controlPoints.get(i);
            Point p2 = controlPoints.get(i + 1);
            Point p3 = controlPoints.get(Math.min(controlPoints.size() - 1, i + 2));
            for (int sample = 0; sample < samplesPerSegment; sample++) {
                double t = sample / (double) samplesPerSegment;
                smoothed.add(catmullRom(p0, p1, p2, p3, t));
            }
        }
        smoothed.add(controlPoints.get(controlPoints.size() - 1));
        return smoothed;
    }

    public static Point catmullRom(Point p0, Point p1, Point p2, Point p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        return new Point(catmullRom(p0.x, p1.x, p2.x, p3.x, t, t2, t3),
                catmullRom(p0.y, p1.y, p2.y, p3.y, t, t2, t3),
                catmullRom(p0.z, p1.z, p2.z, p3.z, t, t2, t3));
    }

    private static double catmullRom(double p0, double p1, double p2, double p3, double t, double t2, double t3) {
        return 0.5D * ((2.0D * p1) + (-p0 + p2) * t
                + (2.0D * p0 - 5.0D * p1 + 4.0D * p2 - p3) * t2
                + (-p0 + 3.0D * p1 - 3.0D * p2 + p3) * t3);
    }

    public record Point(double x, double y, double z) {
    }
}
