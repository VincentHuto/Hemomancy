package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LuxUmbraGeometryTest {
    @Test void flowFormsUseSmallFiniteSurfacesThroughoutTheirLifecycle() {
        for (Form form : Form.values()) {
            if (!LuxUmbraGeometry.handles(form)) continue;
            for (float age : new float[]{0, 3, 12, 30}) {
                var mesh = draw(form, new Vec3(0, 8, 0), age);
                assertFalse(mesh.points.isEmpty(), form.name());
                assertTrue(mesh.points.size() <= 320, form + " exceeded the flow surface budget");
                assertTrue(mesh.points.stream().allMatch(at -> Double.isFinite(at.lengthSqr())), form.name());
                assertEquals(mesh.points.size(), mesh.uvs.size());
            }
        }
    }

    @Test void verdictKeepsTheClippedEndpointAndFullWidthFromEveryDirection() {
        for (Form form : List.of(Form.VERDICT, Form.WHITE_VERDICT))
        for (Vec3 end : new Vec3[]{new Vec3(0, 8, 0), new Vec3(0, -8, 0), new Vec3(3, 2, 7)}) {
            var mesh = draw(form, end, 12);
            Vec3 axis = end.normalize();
            double widest = 0;
            for (Vec3 at : mesh.points) {
                double along = at.dot(axis);
                assertTrue(along >= -.00001 && along <= end.length() + .00001);
                double radius = at.subtract(axis.scale(along)).length();
                assertTrue(radius <= 1.00001);
                widest = Math.max(widest, radius);
            }
            assertEquals(1, widest, .00001);
        }
    }

    @Test void theEyeHasTwoCardsAndPreservesItsUvMaskWhenViewedSideways() {
        Capture mesh = new Capture();
        var packet = new ManipulationVisualPacket(Form.EYE, 4, Vec3.ZERO, Vec3.ZERO, 1, 100, 1);
        LuxUmbraGeometry.draw(packet, new PoseStack(), mesh, new Vec3(6, 2, 0), new Vec3(0, 0, -1),
                new Vec3(0, 1, 0), 100, 20, 1, 1);
        assertEquals(8, mesh.points.size());
        assertTrue(mesh.points.stream().allMatch(at -> Math.abs(at.x) < .02));
        assertEquals(1, mesh.uvs.get(1)[0] - mesh.uvs.get(0)[0], .00001);
        assertEquals(1, mesh.uvs.get(2)[1] - mesh.uvs.get(1)[1], .00001);
    }

    @Test void wellStaysBelowTheAimingLaneAndDegenerateConnectionsAreSafe() {
        var well = draw(Form.WELL, Vec3.ZERO, 12);
        assertTrue(well.points.stream().allMatch(at -> at.y <= .66));
        assertTrue(draw(Form.VERDICT, Vec3.ZERO, 12).points.isEmpty());
        Capture empty = new Capture();
        LuxUmbraGeometry.stream(new PoseStack(), empty, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO,
                .1, .2, 0, 0, 0, 0xFFFFFF, 1);
        assertTrue(empty.points.isEmpty());
    }

    @Test void teleportArrivalReformsWhileDepartureCollapses() {
        Form arrival = Form.valueOf("UMBRA_ARRIVAL");
        assertTrue(width(draw(arrival, Vec3.ZERO, 10)) > width(draw(arrival, Vec3.ZERO, 1)));
        assertTrue(width(draw(Form.TELEPORT, Vec3.ZERO, 10)) < width(draw(Form.TELEPORT, Vec3.ZERO, 1)));
    }

    @Test void curvedStreamsShareBothEdgesAndUvsAtEveryJoin() {
        for (Vec3 camera : List.of(new Vec3(.2, 1, .4), new Vec3(4, 3, -6), new Vec3(-2, 1, 3))) {
            Capture mesh = new Capture();
            LuxUmbraGeometry.stream(new PoseStack(), mesh, Vec3.ZERO, new Vec3(.1, 2.5, .2), camera,
                    .18, .3, 2.1, LuxUmbraGeometry.RIBBON, 3, 0xFFFFFF, 1);
            assertEquals(48, mesh.points.size());
            var stations = new java.util.HashMap<List<Float>, Vec3>();
            int shared = 0;
            for (int i = 0; i < mesh.points.size(); i++) {
                float[] uv = mesh.uvs.get(i);
                Vec3 previous = stations.putIfAbsent(List.of(uv[0], uv[1]), mesh.points.get(i));
                if (previous != null) {
                    assertEquals(previous, mesh.points.get(i), "The same UV station has disconnected edges");
                    shared++;
                }
            }
            assertEquals(22, shared, "Each of the eleven joins must share both edges");
        }
    }

    @Test void firedVerdictHasVisibleAreaLookingDirectlyDownItsAxis() {
        for (Vec3 axis : List.of(new Vec3(0, 0, 1), new Vec3(0, 1, 0), new Vec3(0, -1, 0))) {
            Vec3 right = VisceralGeometry.side(axis), up = axis.cross(right);
            Capture mesh = new Capture();
            var packet = new ManipulationVisualPacket(Form.WHITE_VERDICT, -1, Vec3.ZERO, axis.scale(24), 1.5f, 40, 1);
            LuxUmbraGeometry.draw(packet, new PoseStack(), mesh, Vec3.ZERO, right, up, 100, 8, 1, 1);
            assertTrue(mesh.points.stream().allMatch(at -> at.dot(axis) > .05),
                    "A strip through the eye plane leaves a horizontal line across the screen");
            double area = 0;
            for (int i = 0; i < mesh.points.size(); i += 4) {
                Vec3 a = mesh.points.get(i), b = mesh.points.get(i + 1), c = mesh.points.get(i + 2);
                if (Math.min(a.dot(axis), Math.min(b.dot(axis), c.dot(axis))) < .05) continue;
                double ax = a.dot(right) / a.dot(axis), ay = a.dot(up) / a.dot(axis);
                double bx = b.dot(right) / b.dot(axis), by = b.dot(up) / b.dot(axis);
                double cx = c.dot(right) / c.dot(axis), cy = c.dot(up) / c.dot(axis);
                area += Math.abs((bx - ax) * (cy - ay) - (by - ay) * (cx - ax));
            }
            assertTrue(area > .05, "The released beam collapsed to an edge from its caster: " + axis);
        }
    }

    @Test void firedWhiteVerdictSideProfileHasNoBorderOrEndCapLines() {
        Capture mesh = new Capture();
        var packet = new ManipulationVisualPacket(Form.WHITE_VERDICT, -1, Vec3.ZERO,
                new Vec3(0, 0, 24), 1.5f, 40, 1);
        LuxUmbraGeometry.draw(packet, new PoseStack(), mesh, new Vec3(8, 0, 12),
                new Vec3(1, 0, 0), new Vec3(0, 1, 0), 100, 8, 1, 1);

        assertEquals(4, mesh.points.size(),
                "A side-on White Verdict should be one clean sheet without thin border or end-cap quads");
        assertTrue(mesh.uvs.stream().allMatch(uv -> (int) (uv[0] / 2) == LuxUmbraGeometry.WHITE_BEAM));
    }

    @Test void shortChargeStreamsDoNotFoldTheirQuadsAtTightBends() {
        for (int time = 0; time < 120; time += 7) {
            Capture mesh = new Capture();
            var packet = new ManipulationVisualPacket(Form.VERDICT_CHARGE, -1, Vec3.ZERO, new Vec3(0, 0, 24), 1, 5, 1);
            LuxUmbraGeometry.draw(packet, new PoseStack(), mesh, Vec3.ZERO, new Vec3(1, 0, 0),
                    new Vec3(0, 1, 0), time, 60, 1, 1);
            for (int i = 0; i < mesh.points.size(); i += 4) {
                if ((int)(mesh.uvs.get(i)[0] / 2) != LuxUmbraGeometry.RIBBON) continue;
                Vec3 a = project(mesh.points.get(i)), b = project(mesh.points.get(i + 1));
                Vec3 c = project(mesh.points.get(i + 2)), d = project(mesh.points.get(i + 3));
                double first = b.subtract(a).cross(c.subtract(a)).z;
                double second = d.subtract(c).cross(a.subtract(c)).z;
                assertTrue(first * second >= 0, "Folded charge quad " + i + " at time " + time);
            }
        }
    }

    private static Vec3 project(Vec3 point) { return point.scale(1 / point.z); }

    private static double width(Capture mesh) {
        return mesh.points.stream().mapToDouble(at -> at.x).max().orElseThrow()
                - mesh.points.stream().mapToDouble(at -> at.x).min().orElseThrow();
    }

    private static Capture draw(Form form, Vec3 end, float age) {
        Capture capture = new Capture();
        var packet = new ManipulationVisualPacket(form, -1, Vec3.ZERO, end, 1, 60, 1);
        LuxUmbraGeometry.draw(packet, new PoseStack(), capture, new Vec3(4, 3, -6),
                new Vec3(1, 0, 0), new Vec3(0, 1, 0), 100 + age, age, 1, 1);
        return capture;
    }

    private static final class Capture implements VertexConsumer {
        final List<Vec3> points = new ArrayList<>();
        final List<float[]> uvs = new ArrayList<>();
        public VertexConsumer addVertex(float x, float y, float z) { points.add(new Vec3(x, y, z)); return this; }
        public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        public VertexConsumer setUv(float u, float v) { assertTrue(Float.isFinite(u) && Float.isFinite(v)); uvs.add(new float[]{u, v}); return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
