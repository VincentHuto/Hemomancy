package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import com.vincenthuto.hemomancy.common.item.harbinger.memory.MemoryThreadCurveRules.Point;

import java.util.List;

public final class MemoryThreadCurveRulesTest {
    private static final double EPSILON = 0.0001D;

    public static void main(String[] args) {
        List<Point> controls = List.of(new Point(0.0D, 0.0D, 0.0D), new Point(4.0D, 0.0D, 0.0D),
                new Point(4.0D, 0.0D, 4.0D), new Point(8.0D, 0.0D, 4.0D));
        List<Point> smoothed = MemoryThreadCurveRules.smooth(controls, 4);

        assertEquals(13, smoothed.size(), "three segments sampled four times plus final point");
        assertPoint(controls.get(0), smoothed.get(0), "curve should start at first control point");
        assertPoint(controls.get(controls.size() - 1), smoothed.get(smoothed.size() - 1),
                "curve should end at last control point");
        assertTrue(smoothed.get(5).x() > 4.0D && smoothed.get(5).z() > 0.0D && smoothed.get(5).z() < 4.0D,
                "corner segment should ease past the control point instead of duplicating an angular bend");
    }

    private static void assertPoint(Point expected, Point actual, String message) {
        if (Math.abs(expected.x() - actual.x()) > EPSILON
                || Math.abs(expected.y() - actual.y()) > EPSILON
                || Math.abs(expected.z() - actual.z()) > EPSILON) {
            throw new AssertionError(message + " expected " + expected + " but got " + actual);
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " expected " + expected + " but got " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
